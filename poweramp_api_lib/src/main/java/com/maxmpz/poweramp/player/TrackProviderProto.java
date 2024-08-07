/*
Copyright (C) 2011-2020 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.maxmpz.poweramp.player;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.util.Log;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.FileDescriptor;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Simple "seekable" socket protocol:
 * - unix domain socket is used instead of a pipe for the duplex communication
 * - seek command exposed from {@link #sendData} and can be processed as needed
 * - {@link #sendData} is blocking almost in the same way as standard ParcelFileDescriptor pipe write is
 *
 * NOTE: it's not possible to use timeouts on this side of the socket as Poweramp may open and hold the socket while in paused state for indefinite time
 */
public class TrackProviderProto implements AutoCloseable {
	private static final String TAG = "TrackProviderProto";
	private static final boolean LOG = false;
	/** If true, a bit more checks happen. Disable for production builds */
	private static final boolean DEBUG_CHECKS = false;

	/** Invalid seek position */
	public static final long INVALID_SEEK_POS = Long.MIN_VALUE;

	/** Maximum number of data bytes we're sending in the packet (excluding header overhead) */
	public static final int MAX_DATA_SIZE = 4 * 1024;

	/** Packet header is TAG(4) + PACKET_TYPE(2) + DATA_SIZE(2) + RESERVED(4) => 12 */
	private static final int MAX_PACKET_HEADER_SIZE = 12;

	/** Header(12) + fileLength(8) + size(4) + */
	private static final int INITIAL_PACKET_SIZE = MAX_PACKET_HEADER_SIZE + 8 + 4;

	/** Index of the first data byte in the packet */
	private static final int PACKET_DATA_IX = MAX_PACKET_HEADER_SIZE;

	/** Index of the data size (short) */
	private static final int PACKET_DATA_SIZE_IX = 6;

	private static final int PACKET_TAG = 0xF1F20001;

	private static final short PACKET_TYPE_HEADER   = 1;
	private static final short PACKET_TYPE_DATA     = 2;
	private static final short PACKET_TYPE_SEEK     = 3;
	private static final short PACKET_TYPE_SEEK_RES = 4;

	private static final int STATE_INITIAL = 0;
	private static final int STATE_CLOSED  = 1;
	private static final int STATE_DATA    = 2;

	private static final int LONG_BYTES    = 8;
	private static final int INTEGER_BYTES = 4;


	private final @NonNull FileDescriptor mSocket;
	/** Buffer for header + some extra space for few small packet types */
	private final @NonNull ByteBuffer mHeaderBuffer;
	private final long mFileLength;
	private int mState = STATE_INITIAL;
	private final StructPollfd @NonNull[] mStructPollFds;
	private final @NonNull SeekRequest mTempSeekRequest = new SeekRequest();


	/** Raised if we failed with the connection/action and can't continue anymore */
	@SuppressWarnings("serial")
	public static class TrackProviderProtoException extends RuntimeException {
		public TrackProviderProtoException(Throwable ex) {
			super(ex);
		}

		public TrackProviderProtoException(String msg) {
			super(msg);
		}
	}

	/** Raised when connection is closed by Poweramp */
	@SuppressWarnings("serial")
	public static class TrackProviderProtoClosed extends RuntimeException {
		public TrackProviderProtoClosed(Throwable ex) {
			super(ex);
		}
	}

	/**
	 * Simple data structure with seek offsetBytes and seek milliseconds.<br>
	 * NOTE: there is only on instance of SeekRequest per TrackProviderProto instance, don't share or use from other threads
	 */
	public static class SeekRequest {
		/** >=0 for seek from start of the file, < 0 for seek from the end of the file */
		public long offsetBytes;
		/*
		 *If >=0, provides a hint for the seek request in milliseconds timebase
		 * @since 883
		 */
		public int ms = Integer.MIN_VALUE;

		@Override
		public String toString() {
			return super.toString() + " offsetBytes=" + offsetBytes + " ms=" + ms;
		}
	}


	/**
	 * @param pfd the socket pfd created by ParcelFileDescriptor.createSocketPair
	 * @param fileLength the actual total length of the track being played
	 */
	@SuppressLint("NewApi")
	public TrackProviderProto(@NonNull ParcelFileDescriptor pfd, long fileLength) {
		if(fileLength <= 0) throw new IllegalArgumentException("bad fileLength=" + fileLength);
		FileDescriptor socket = pfd.getFileDescriptor();
		try {
			if(socket == null || !OsConstants.S_ISSOCK(Os.fstat(socket).st_mode)) throw new IllegalArgumentException("bad pfd=" + pfd);
		} catch(ErrnoException ex) {
			throw new TrackProviderProtoException(ex);
		}
		mSocket = socket;
		mFileLength = fileLength;

		ByteBuffer headerBuffer = ByteBuffer.allocateDirect(INITIAL_PACKET_SIZE);
		if(headerBuffer == null) throw new TrackProviderProtoException("headerBuffer");
		mHeaderBuffer = headerBuffer;
		mHeaderBuffer.order(ByteOrder.nativeOrder());

		mStructPollFds = new StructPollfd[] {
			new StructPollfd()
		};
		mStructPollFds[0].fd = mSocket;
		mStructPollFds[0].events = (short)OsConstants.POLLIN;
	}

	@Override
	public void close() {
		if(LOG) Log.w(TAG, "close");
		if(DEBUG_CHECKS && mState == STATE_CLOSED) throw new AssertionError();
		if(mState != STATE_CLOSED) {
			try {
				Os.shutdown(mSocket, 0);
			} catch(ErrnoException ex) {
				Log.e(TAG, "", ex);
			}
			try {
				Os.close(mSocket);
			} catch(ErrnoException ex) {
				Log.e(TAG, "", ex);
			}
			mState = STATE_CLOSED;
			if(LOG) Log.w(TAG, "close OK");
		}
	}

	/** Prepares packet header buffer */
	private @NonNull ByteBuffer preparePacketHeader(short packetType, int dataSize) {
		ByteBuffer buf = mHeaderBuffer;
		buf.clear();
		buf.putInt(PACKET_TAG);
		buf.putShort(packetType);
		if(DEBUG_CHECKS && dataSize < 0 || dataSize > MAX_DATA_SIZE) throw new AssertionError(dataSize);
		buf.putShort((short)dataSize);
		buf.putInt(0);
		return buf;
	}

	/** Send the header required to be sent after the socket is connected */
	public void sendHeader() {
		if(LOG) Log.w(TAG, "sendHeader");
		if(mState == STATE_INITIAL) {
			try {
				ByteBuffer buf = preparePacketHeader(PACKET_TYPE_HEADER, LONG_BYTES + INTEGER_BYTES);
				buf.putLong(mFileLength);
				buf.putInt(MAX_DATA_SIZE);
				buf.flip();

				while(buf.hasRemaining()) {
					int res = Os.sendto(mSocket, buf, 0, null, 0); // sendto updates buffer position
					if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(buf, res);
				}

				mState = STATE_DATA;
				if(LOG) Log.w(TAG, "sendHeader OK");

			} catch(ErrnoException|SocketException ex) {
				if(LOG) Log.e(TAG, "", ex);
				throw new TrackProviderProtoException(ex);
			}
		} else if(DEBUG_CHECKS) throw new AssertionError(mState);
	}

	/**
	 * Send the data to Poweramp. This will block until Poweramp is resumed, playing, and requiring more data<br>
	 * If Poweramp is paused this may be blocked for a very long time, until Poweramp resumes, exists, force-closes, etc.<br><br>
	 *
	 * When Poweramp requests seeking, this method returns an appropriate long seek position
	 * (>= 0 for the seeking from the start, < 0 for the seeking from the end)<br>
	 * Poweramp then waits for the PACKET_TYPE_SEEK_RES packet type, ignoring any other packets, so it's required to send
	 * PACKET_TYPE_SEEK_RES packet, or close the connection.
	 *
	 * @param data buffer to send. Should be properly flipped prior the sending
	 * @return request for the new seek position, or INVALID_SEEK_POS(==Long.MIN_VALUE) if none requested
	 */
	public long sendData(@NonNull ByteBuffer data) {
		SeekRequest request = sendData2(data);
		if(request != null) {
			return request.offsetBytes;
		}
		return INVALID_SEEK_POS;
	}

	/**
	 * Send the data to Poweramp. This will block until Poweramp is resumed, playing, and requires more data<br>
	 * If Poweramp is paused this may be blocked for a very long time, until Poweramp resumes, exists, force-closes, etc.<br><br>
	 *
	 * When Poweramp request seek, this method returns appropriate long seek position (>= 0 for seek from start, < 0 for seek from end)<br>
	 * Poweramp then waits for the PACKET_TYPE_SEEK_RES packet type, ignoring any other packets, so it's required to send PACKET_TYPE_SEEK_RES packet,
	 * or close the connection.
	 *
	 * @param data buffer to send. Should be properly flipped prior sending
	 * @return request for the new seek position, or null if none requested
	 */
	public @Nullable SeekRequest sendData2(@NonNull ByteBuffer data) {
		if(LOG) Log.w(TAG, "sendData2 data.remaining=" + data.remaining());
		if(mState == STATE_DATA) {
			@SuppressWarnings("unused")
			int packetsSent = 0;
			final int originalDataLimit = data.limit(); // Keep original limit as we'll modify it to send up to MAX_DATA_SIZE bytes per packet
			while(data.hasRemaining()) {
				try {
					int size = data.remaining();
					if(size > MAX_DATA_SIZE) { // Sending up to MAX_DATA_SIZE
						size = MAX_DATA_SIZE;
					}
					data.limit(data.position() + size);

					ByteBuffer buf = preparePacketHeader(PACKET_TYPE_DATA, size);
					buf.flip();
					while(buf.hasRemaining()) {
						int res = Os.sendto(mSocket, buf, 0, null, 0);
						if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(buf, res);
					}
					while(data.hasRemaining()) {
						int res = Os.sendto(mSocket, data, 0, null, 0); // data.position changed by # of bytes actually sent
						if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(data, res);
					}
					packetsSent++;

					buf.clear();

				} catch(ErrnoException ex) {
					if(ex.errno == OsConstants.ECONNRESET || ex.errno == OsConstants.EPIPE) throw new TrackProviderProtoClosed(ex);
					if(LOG) Log.e(TAG, "", ex);
					throw new TrackProviderProtoException(ex);
				} catch(SocketException ex) {
					if(LOG) Log.e(TAG, "", ex);
					throw new TrackProviderProtoException(ex);
				} finally {
					data.limit(originalDataLimit); //  Restore limit
				}

				try {
					int fdsReady = Os.poll(mStructPollFds, 0); // Check for possible incoming packet header

					if(fdsReady == 1) {
						SeekRequest seekPosEncoded = readSeekRequest(true); // This shouldn't block as we checked we have some incoming data
						if(seekPosEncoded != null) {
							return seekPosEncoded; // Got valid seek request, return it
						}
					}

				} catch(ErrnoException ex) {
					if(LOG) Log.e(TAG, "", ex);
				}
			}
			if(LOG) Log.w(TAG, "sendDataPackets OK packetsSent=" + packetsSent);

		} else if(DEBUG_CHECKS) throw new AssertionError(mState);

		return null;
	}

	/**
	 * Wait until Poweramp sends seek request or closes socket
	 * @return request for the new seek position, or INVALID_SEEK_POS(==Long.MIN_VALUE) if none requested, socket closed, error happened, etc.
	 */
	public long sendEOFAndWaitForSeekOrClose() {
		if(LOG) Log.w(TAG, "sendEOFAndWaitForSeekOrClose");

		SeekRequest seekRequest = sendEOFAndWaitForSeekOrClose2();
		if(seekRequest != null) {
			if(LOG) Log.w(TAG, "sendEOFAndWaitForSeekOrClose got seek=>" + seekRequest.offsetBytes);
			return seekRequest.offsetBytes;
		}
		if(LOG) Log.w(TAG, "sendEOFAndWaitForSeekOrClose DONE");
		return INVALID_SEEK_POS;
	}

	/**
	 * Wait until Poweramp sends seek request or closes socket
	 * @return request for the new seek position, or null if none requested, socket closed, error happened, etc.
	 */
	public @Nullable SeekRequest sendEOFAndWaitForSeekOrClose2() {
		if(LOG) Log.w(TAG, "waitForSeekOrClose");

		try {
			// Send EOF (empty data buffer)
			ByteBuffer buf = preparePacketHeader(PACKET_TYPE_DATA, 0);
			buf.flip();
			while(buf.hasRemaining()) {
				int res = Os.sendto(mSocket, buf, 0, null, 0);
				if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(buf, res);
			}
		} catch(ErrnoException|SocketException ex) {
			if(LOG) Log.e(TAG, "", ex);
			throw new TrackProviderProtoException(ex);
		}

		try {
			return readSeekRequest(false);
		} catch(TrackProviderProtoException ex) {
			return null;
		}
	}

	/**
	 * Try to read Poweramp sends seek request. Blocks until socket has some data
	 * @return request for the new seek position, or null if none requested
	 */
	private @Nullable SeekRequest readSeekRequest(boolean noBlock) {
		if(LOG) Log.w(TAG, "readSeekRequest");
		try {
			ByteBuffer buf = mHeaderBuffer;
			buf.clear();
			buf.limit(MAX_PACKET_HEADER_SIZE); // Read just header

			int res = Os.recvfrom(mSocket, buf, 0, null);
			if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(buf, res);

			if(res == MAX_PACKET_HEADER_SIZE) {
				int type = getPacketType(buf);
				int dataSize = getPacketDataSize(buf);
				if(type == PACKET_TYPE_SEEK && dataSize >= LONG_BYTES) {
					if(LOG) Log.w(TAG, "readSeekRequest got PACKET_TYPE_SEEK dataSize=>" + dataSize);
					buf.limit(buf.limit() + dataSize);

					res = Os.recvfrom(mSocket, buf, noBlock ? OsConstants.O_NONBLOCK : 0, null); // Read seek position
					if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(buf, res);

					if(res >= LONG_BYTES) {
						SeekRequest seekRequest = mTempSeekRequest;
						seekRequest.offsetBytes = buf.getLong(PACKET_DATA_IX);
						if(LOG) Log.w(TAG, "readSeekRequest got offsetBytes=>" + seekRequest.offsetBytes);
						if(res >= LONG_BYTES + INTEGER_BYTES) {
							seekRequest.ms = buf.getInt(PACKET_DATA_IX + LONG_BYTES);
							if(LOG) Log.w(TAG, "readSeekRequest got ms=>" + seekRequest.ms);
						} else {
							seekRequest.ms = Integer.MIN_VALUE;
						}
						return seekRequest;

					} else Log.e(TAG, "readSeekRequest FAIL recvfrom data res=" + res);
				} else
					Log.e(TAG, "readSeekRequest FAIL recvfrom type=" + type + " dataSize=" + dataSize);
			} else if(res == 0) {
				// EOF
				if(LOG) Log.w(TAG, "readSeekRequest EOF");
				return null;
			} else Log.e(TAG, "readSeekRequest FAIL recvfrom res=" + res);
		} catch(ErrnoException ex) {
			if(LOG) Log.e(TAG, "", ex);
			if(ex.errno == OsConstants.ECONNRESET || ex.errno == OsConstants.EPIPE) throw new TrackProviderProtoClosed(ex);
			if(ex.errno == OsConstants.EAGAIN) { // Timed out
				return null;
			}
			throw new TrackProviderProtoException(ex);
		} catch(SocketException ex) {
			if(LOG) Log.e(TAG, "", ex);
			throw new TrackProviderProtoException(ex);
		}
		return null;
	}

	/**
	 * Send a seek result as a response to the seek request
	 * @param newPos if >= 0 - indicates a new byte position within the track, or <0 if the seek failed
	 */

	public void sendSeekResult(long newPos) {
		if(LOG) Log.w(TAG, "sendSeekResult newPos=" + newPos);
		ByteBuffer buf = preparePacketHeader(PACKET_TYPE_SEEK_RES, LONG_BYTES);
		buf.putLong(newPos);
		buf.flip();
		try {
			while(buf.hasRemaining()) {
				int res = Os.sendto(mSocket, buf, 0, null, 0);
				if(Build.VERSION.SDK_INT == 21) maybeUpdateBufferPosition(buf, res);
			}
		} catch(ErrnoException|SocketException ex) {
			if(LOG) Log.e(TAG, "", ex);
			throw new TrackProviderProtoException(ex);
		}
	}

	/**
	 * @return packetType > 0, or -1 on failure
	 */
	private static int getPacketType(@NonNull ByteBuffer buf) {
		// Packet header is TAG(4) + PACKET_TYPE(2) + DATA_SIZE(2) + DATA_SERIAL(4) => 12
		if(buf.limit() >= MAX_PACKET_HEADER_SIZE && buf.getInt(0) ==  PACKET_TAG) {
			int type = buf.getShort(4);
			int dataSize = getPacketDataSize(buf);
			if(type > 0 && dataSize > 0) {
				return type;
			}
		}
		return -1;
	}

	/**
	 * @return packet data size
	 */
	private static int getPacketDataSize(@NonNull ByteBuffer buf) {
		return buf.getShort(PACKET_DATA_SIZE_IX);
	}

	/** Required for Android 5.0.0 which doesn't update buffers */
	private static void maybeUpdateBufferPosition(ByteBuffer buffer, int bytesReadOrWritten) {
		if(bytesReadOrWritten > 0) {
			buffer.position(bytesReadOrWritten + buffer.position());
		}
	}
}
