package com.maxmpz.poweramp.plugin;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

public class PluginMsgHelper {
	public static class PluginMsgException extends RuntimeException {
		private static final long serialVersionUID = -5131019933670409856L;
		
		public PluginMsgException() {}
		
		public PluginMsgException(String msg) {
			super(msg);
		}
	}
	
	public static int MSG_TAG 			 = 0xF1F2F3F4; 
	public static int HEADER_SIZE_INTS   = 8;
	public static int MAX_SIZE_INTS		 = 1024; 
	public static int MAX_SIZE_BYTES	 = MAX_SIZE_INTS * 4;
	public static int HEADER_SIZE_BYTES  = HEADER_SIZE_INTS * 4; 
	
	public static int IX_PLUGIN_ID      = 0;
	public static int IX_PLUGIN_ID_INT  = 0;
	public static int IX_TAG            = 4 * 4; // 1
	public static int IX_MSG_ID         = 5 * 4; // 2
	public static int IX_FLAGS          = 6 * 4; // 3
	public static int IX_DATA_SIZE      = 7 * 4; // 4
	public static int IX_DATA           = 8 * 4;
	public static int IX_DATA_INT       = 8;
	
	public static int FLAG_SYNC_NO_CONTEXT =  0x0001;
	
	public static int calcBufferSizeInts(int desiredSizeInts) {
		return HEADER_SIZE_INTS + desiredSizeInts; 
	}

	public static int calcBufferSizeBytes(int desiredSizeBytes) {
		return HEADER_SIZE_BYTES + desiredSizeBytes; 
	}

	private static void writeHeader(int[] buf, int pluginID, int msgID, int flags, int desiredSizeInts) {
		buf[0] = pluginID;
		// 3 ints are zeros (reserved for Poweramp msg header).
		buf[IX_TAG / 4] = MSG_TAG;
		buf[IX_MSG_ID / 4] = msgID;
		buf[IX_FLAGS / 4] = flags;
		buf[IX_DATA_SIZE / 4] = desiredSizeInts * 4;
	}

	private static void writeHeader(ByteBuffer buf, int pluginID, int msgID, int flags, int desiredSizeBytes) {
		buf.putInt(pluginID);
		// 3 ints are zeros (reserved for Poweramp msg header).
		buf.position(IX_TAG);
		buf.putInt(MSG_TAG);
		buf.putInt(msgID);
		buf.putInt(flags);
		buf.putInt(desiredSizeBytes);
	}

	public static int[] createIntMsgBuffer(int pluginID, int msgID, int flags, int desiredSizeInts) {
		if(desiredSizeInts > MAX_SIZE_INTS) {
			throw new PluginMsgException("bad desiredSizeInts=" + desiredSizeInts + " MAX_SIZE_INTS=" + MAX_SIZE_INTS);
		}
		int[] buf = new int[calcBufferSizeInts(desiredSizeInts)];
		writeHeader(buf, pluginID, msgID, flags, desiredSizeInts);
		return buf;
	}
	
	// NOTE: returned ByteBuffer is positioned to the first data position
	// NOTE: direct buffer makes no sense in our case and is slower.
	public static ByteBuffer createBufferMsgBuffer(int pluginID, int msgID, int flags, int desiredSizeBytes) {
		if(desiredSizeBytes > MAX_SIZE_BYTES) {
			throw new PluginMsgException("bad desiredSizeBytes=" + MAX_SIZE_BYTES + " MAX_SIZE_BYTES=" + MAX_SIZE_BYTES);
		}
		ByteBuffer buf = ByteBuffer.allocate(calcBufferSizeBytes(desiredSizeBytes));
		buf.order(ByteOrder.LITTLE_ENDIAN);
		writeHeader(buf, pluginID, msgID, flags, desiredSizeBytes);
		return buf;
	}
	
	public static String msgBufferAsString(int[] buf) {
		if(buf.length < HEADER_SIZE_INTS) {
			throw new PluginMsgException("bad buf length=" + buf.length);
		}
		return toString(buf);
	}

	public static String msgBufferAsString(ByteBuffer buf) {
		if(buf.capacity() < HEADER_SIZE_BYTES) {
			throw new PluginMsgException("bad buf capacity=" + buf.capacity());
		}
		int pos = buf.position();
		buf.position(0);
		IntBuffer intBuf = buf.asIntBuffer();
		buf.position(pos);
		int ar[] = new int[intBuf.capacity()];
		intBuf.get(ar);
		return toString(ar);
	}
	
    private static String toString(int[] array) {
        if (array == null) {
            return "null";
        }
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(array.length * 6);
        sb.append('[');
        sb.append(array[0]); 
        for (int i = 1; i < array.length; i++) {
            sb.append(", 0x");
            sb.append(Integer.toHexString(array[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}
