package com.maxmpz.powerampproviderexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.ProxyFileDescriptorCallback;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;
import com.maxmpz.poweramp.player.TrackProviderConsts;
import com.maxmpz.poweramp.player.TrackProviderHelper;
import com.maxmpz.poweramp.player.TrackProviderProto;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

/**
 * Example provider demonstrating:
 * - providing tracks hierarchy for the Poweramp scanner
 * - providing tracks via direct file discriptor and via seekable socket protocol
 */
public class ExampleProvider extends DocumentsProvider {
	private static final String TAG = "ExampleProvider";
	private static final boolean LOG = true;

	/**
	 * If true, we're copying our mp3s from assets to local filesystem. This is because Poweramp can't reliable play assets fd - fd pointing to apk file itself, mostly as
	 * end of file can't be properly detected for many formats without proper length header
	 */
	private static final boolean USE_MP3_COPY = true;

	/**
	 * If true, use {@link android.os.storage.StorageManager#openProxyFileDescriptor} on Android 8+ which provides seekable file descriptors implemented by Android Framework.<br>
	 * It's much easier to implement vs Seekable sockets protocol and recommended for apps targeting Android 8+
	 */
	private static final boolean USE_OPEN_PROXY_FILE_DESCRIPTOR = true;

	/** Link to mp3 track to demonstrate http track with the duration */
	private static final String DUBSTEP_HTTP_URL = "https://raw.githubusercontent.com/maxmpz/powerampapi/master/poweramp_provider_example/app/src/main/assets/bensound-dubstep.mp3";

	/** Link to mp3 track to demonstrate http track with the duration */
	private static final String SUMMER_HTTP_URL = "https://raw.githubusercontent.com/maxmpz/powerampapi/master/poweramp_provider_example/app/src/main/assets/bensound-summer.mp3";

	/** Link to flac track to demonstrate http track with the duration */
	private static final String DUBSTEP_FLAC_HTTP_URL = "https://raw.githubusercontent.com/maxmpz/powerampapi/master/poweramp_provider_example/app/src/main/assets/bensound-dubstep.flac";

	/** Docid suffix for static url tracks */
	private static final String DOCID_STATIC_URL_SUFFIX = ".url";

	/** Docid suffix for dynamic url tracks */
	private static final String DOCID_DYNAMIC_URL_SUFFIX = ".dynamicurl";

	private static final long DUBSTEP_SIZE = 2044859L;
	private static final long DUBSTEP_DURATION_MS = 125000L;
	private static final long SUMMER_SIZE = 4620151L;
	private static final long SUMMER_DURATION_MS = 217000L;

	private static final long DUBSTEP_FAKE_FLAC_SIZE = 14000000;

	// ~116 bytes per ms in the real bensound-dubstep.flac, but "fake" value is an approximation
	private static final long DUBSTEP_FAKE_AVERAGE_BYTES_PER_MS = Math.round((float)DUBSTEP_FAKE_FLAC_SIZE / (float) DUBSTEP_DURATION_MS);


	/** Default columns returned for roots */
	private static final String[] DEFAULT_ROOT_PROJECTION =	new String[] {
			Root.COLUMN_ROOT_ID,
			Root.COLUMN_TITLE,
			Root.COLUMN_SUMMARY,
			Root.COLUMN_FLAGS,
			Root.COLUMN_ICON,
			Root.COLUMN_DOCUMENT_ID,
	};

	/** Default columns returned for documents - folders and tracks (not including metadata) */
	private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[] {
			Document.COLUMN_DOCUMENT_ID,
			Document.COLUMN_MIME_TYPE,
			Document.COLUMN_DISPLAY_NAME,
			Document.COLUMN_LAST_MODIFIED,
			Document.COLUMN_FLAGS,
			Document.COLUMN_SIZE,
	};

	/**
	 * Default columns returned for tracks, including metadata.<br>
	 * NOTE: where possible, we're using standard MediaStore or MediaFormat column names<br><br>
	 *
	 * NOTE: if TITLE and DURATION columns are missing, empty, or null, Poweramp assumes cursor doesn't contain metadata and doesn't check other metadata columns.<br>
	 * Instead of relying on the cursor metadata, Poweramp will try to open the file via openDocument and will scan tags in it.
	 * If TITLE or DURATION exists in cursor, Poweramp won't attempt to read any tags directly from the track, using what is given in cursor.
	 * NOTE: if TITLE and DURATION are missing, Poweramp also won't use thumbnail API (even if FLAG_SUPPORTS_THUMBNAIL is set). Instead Poweramp reads cover directly
	 * from track (via another openDocument call)
	 */
	private static final String[] DEFAULT_TRACK_AND_METADATA_PROJECTION = new String[] {
			Document.COLUMN_DOCUMENT_ID,
			Document.COLUMN_MIME_TYPE,
			Document.COLUMN_DISPLAY_NAME,
			Document.COLUMN_LAST_MODIFIED,
			Document.COLUMN_FLAGS,
			Document.COLUMN_SIZE,

			MediaStore.MediaColumns.TITLE,
			MediaStore.Audio.AudioColumns.DURATION,
			MediaStore.Audio.AudioColumns.ARTIST,
			MediaStore.Audio.AudioColumns.ALBUM,
			MediaStore.Audio.AudioColumns.YEAR,
			TrackProviderConsts.COLUMN_ALBUM_ARTIST,
			MediaStore.Audio.AudioColumns.COMPOSER,
			TrackProviderConsts.COLUMN_GENRE,
			MediaStore.Audio.AudioColumns.TRACK,
			TrackProviderConsts.COLUMN_TRACK_ALT,
			MediaFormat.KEY_SAMPLE_RATE,
			MediaFormat.KEY_CHANNEL_COUNT,
			MediaFormat.KEY_BIT_RATE,
			TrackProviderConsts.COLUMN_BITS_PER_SAMPLE
	};


	private long mApkInstallTime;


	@Override
	public boolean onCreate() {
		// Code to retrieve own apk install time. We use this here as lastModified. Not needed for real providers which can retrieve lastModified from the
		// content itself (either from network or filesystem)
		PackageManager pm = getContext().getPackageManager();
		try {
			PackageInfo pakInfo = pm.getPackageInfo(getContext().getApplicationInfo().packageName, 0);
			mApkInstallTime = pakInfo.lastUpdateTime > 0 ? pakInfo.lastUpdateTime : pakInfo.firstInstallTime;

			if(LOG) Log.w(TAG, "onCreate mApkInstallTime=" + mApkInstallTime);
		} catch(PackageManager.NameNotFoundException ex) {
			Log.e(TAG, "", ex);
			mApkInstallTime = System.currentTimeMillis();
		}

		if(USE_MP3_COPY) {
			// Extract our mp3s to storage as Poweramp won't properly play asset apk fd (fd points to apk itself, so Poweramp tries to play the apk file itself,
			// basically playing first found mp3 from it)
			File dir = getContext().getFilesDir();
			copyAsset("bensound-dubstep.mp3", dir, false);
			copyAsset("bensound-dubstep.flac", dir, false);
			copyAsset("bensound-summer.mp3", dir, false);
			copyAsset("streams-playlist.m3u8", dir, false);
		}

		return true;
	}

	@Override
	public Cursor queryRoots(String[] projection) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "queryRoots projection=" + Arrays.toString(projection));
		try {

			MatrixCursor c = new MatrixCursor(resolveRootProjection(projection));
			MatrixCursor.RowBuilder row;

			// Items without metadata provided by the provider (Poweramp reads track metadata from track itself)
			row = c.newRow();
			row.add(Root.COLUMN_ROOT_ID, "rootId1");
			row.add(Root.COLUMN_TITLE, "Root 1");
			row.add(Root.COLUMN_SUMMARY, "Poweramp Example Provider");
			row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_IS_CHILD); // Required
			row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);
			row.add(Root.COLUMN_DOCUMENT_ID, "root1");

			// Items with metadata (Poweramp gets metadata from cursor and doesn't try to read tags from tracks)
			row = c.newRow();
			row.add(Root.COLUMN_ROOT_ID, "rootId2");
			row.add(Root.COLUMN_TITLE, "Root 2");
			row.add(Root.COLUMN_SUMMARY, "Poweramp Example Provider");
			row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_IS_CHILD); // Required
			row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);
			row.add(Root.COLUMN_DOCUMENT_ID, "root2");

			// Streams: m3u8 playlist, http stream with the duration, http no-duration stream (radio)
			row = c.newRow();
			row.add(Root.COLUMN_ROOT_ID, "rootId3");
			row.add(Root.COLUMN_TITLE, "Root 3 (Streams)");
			row.add(Root.COLUMN_SUMMARY, "Poweramp Example Provider");
			row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_IS_CHILD); // Required
			row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);
			row.add(Root.COLUMN_DOCUMENT_ID, "root3");

			return c;

		} catch(Throwable th) {
			Log.e(TAG, "", th);
		}
		return null;
	}

	/**
	 * Query document is used:
	 * - by Android picker to show appropriate directory and tracks
	 * - by Poweramp to retrieve track metadata
	 */
	@Override
	public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "queryDocument documentId=" + documentId + " projection=" + Arrays.toString(projection));

		try {

			// NOTE: using simplified root/folder/file detection here. Real provider should base this on actual hierarchy, either retrieved from network
			// or from filesystem

			// If this is root, just return static root data
			if(!documentId.contains("/") && documentId.startsWith("root")) {
				final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));
				MatrixCursor.RowBuilder row = c.newRow();
				AssetManager assets = getContext().getResources().getAssets();
				fillFolderRow(documentId, row, hasSubDirs(assets, documentId) ? TrackProviderConsts.FLAG_HAS_SUBDIRS : TrackProviderConsts.FLAG_NO_SUBDIRS);
				// NOTE: we return display name derived from documentId here VS returning the same label as used for Root.COLUMN_TITLE
				// Real app should use same labels in both places (roots and queryDocument) for same root
				row.add(Document.COLUMN_DISPLAY_NAME, capitalize(documentId));
				return c;

			} else if(documentId.startsWith("root3") && documentId.endsWith(DOCID_STATIC_URL_SUFFIX)) {
				// Url mp3 with a duration. We must provide duration here to avoid endless/non-seekable stream

				final MatrixCursor c = new MatrixCursor(resolveTrackProjection(projection));
				int trackNum = extractTrackNum(documentId);
				if(documentId.contains("dubstep")) {
					fillURLRow(documentId, c.newRow(), DUBSTEP_HTTP_URL, DUBSTEP_SIZE, "Dubstep", trackNum == 1 ? 0 : DUBSTEP_DURATION_MS, true, true, true); // Send wave
				} else {
					boolean emptyWave = trackNum < 4; // 1..4 summer tracks with empty wave, for the others - allow Poweramp to scan them
					fillURLRow(documentId, c.newRow(), SUMMER_HTTP_URL, SUMMER_SIZE, "Summer", trackNum == 1 ? 0 : SUMMER_DURATION_MS, true, false, emptyWave);
				}
				return c;

			} else if(documentId.startsWith("root3") && documentId.endsWith(DOCID_DYNAMIC_URL_SUFFIX)) {
				// Dynamic url to mp3 with a duration. We must provide duration here to avoid endless/non-seekable stream
				// NOTE: we use TrackProviderConsts.DYNAMIC_URL as URL here to indicate dynamic url track

				final MatrixCursor c = new MatrixCursor(resolveTrackProjection(projection));
				int trackNum = extractTrackNum(documentId);
				if(documentId.contains("dubstep")) {
					fillURLRow(documentId, c.newRow(), TrackProviderConsts.DYNAMIC_URL, DUBSTEP_SIZE, "Dubstep", DUBSTEP_DURATION_MS, true, true, true); // Send wave
				} else {
					boolean emptyWave = trackNum < 4; // 1..4 summer tracks with empty wave, for the others - allow Poweramp to scan them
					fillURLRow(documentId, c.newRow(), TrackProviderConsts.DYNAMIC_URL, SUMMER_SIZE, "Summer", SUMMER_DURATION_MS, true, false, emptyWave);
				}
				return c;

			} else if(documentId.endsWith(".mp3") || documentId.endsWith(".flac")) { // Seems like a track
				// We are adding metadata for root2 and check if it's actually requested as a small optimization (which can be big if track metadata retrieval requires additional processing)
				boolean addMetadata = documentId.startsWith("root2/") && projection != null && arrayContains(projection, MediaStore.MediaColumns.TITLE);
				final MatrixCursor c = new MatrixCursor(resolveTrackProjection(projection));

				boolean addLyrics = projection != null && arrayContains(projection, TrackProviderConsts.COLUMN_TRACK_LYRICS);
				boolean sendWave = documentId.contains("dubstep") && projection != null && arrayContains(projection, TrackProviderConsts.COLUMN_TRACK_WAVE);
				fillTrackRow(documentId, c.newRow(), addMetadata, sendWave, addLyrics, extractTrackNum(documentId), 0); // Adding wave as well to root2 tracks
				return c;

			} else if(documentId.endsWith(".m3u")) { // Seems like a playlist
				final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));
				fillPlaylistRow(documentId, c.newRow());
				return c;

			} else { // This must be a directory
				final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));
				AssetManager assets = getContext().getResources().getAssets();
				fillFolderRow(documentId, c.newRow(), hasSubDirs(assets, documentId) ? TrackProviderConsts.FLAG_HAS_SUBDIRS : TrackProviderConsts.FLAG_NO_SUBDIRS);
				return c;
			}

		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + documentId, th);
		}
		return null;
	}

	private void fillURLRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row, @NonNull String url, long size, @NonNull String title,
	                        long duration, boolean sendMetadata, boolean sendWave, boolean sendEmptyWave
	) {
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, "audio/mpeg");
		// The display name defines name of the track "file" in "Show File Names" mode. There is also a title via MediaStore.MediaColumns.TITLE.
		// It's up to you how you define display name, it can be anything filename alike, or it can just match track title
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		// As our assets data is always static, we just return own apk installation time. For real folder structure, preferable last modified for given folder should be returned.
		// This ensures Poweramp incremental scanning process. If we return <= 0 value here, Poweramp will be forced to rescan whole provider hierarchy each time it scans
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);
		// Optional, real provider should preferable return real track file size here or 0
		row.add(Document.COLUMN_SIZE, size);
		// Setting this will cause Poweramp to ask for the track album art via getDocumentThumbnail, but only if other metadata (MediaStore.MediaColumns.TITLE/MediaStore.MediaColumns.DURATION) exists
		row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL);

		row.add(TrackProviderConsts.COLUMN_URL, url);
		row.add(MediaStore.Audio.AudioColumns.DURATION, duration); // Milliseconds, long. If duration <= 0, this is endless non-seekable stream (e.g. radio)

		if(sendMetadata) { // NOTE: Poweramp doesn't need extra metadata (except COLUMN_URL/DURATION for streams) for queryDocuments, but requires that for queryDocument
			if(TrackProviderConsts.DYNAMIC_URL.equals(url)) {
				title += " Dynamic";
			}

			String prefix = title + " ";

			// Some dump tags logic - as we have 2 static files here as an example, but they have docId like dubstep1.mp3, summer2.mp3, etc.
			// Real provider should get this info from network or extract from the file
			int trackNum = extractTrackNum(documentId);

			row.add(MediaStore.MediaColumns.TITLE, prefix + "URL Track " + trackNum);
			row.add(MediaStore.Audio.AudioColumns.ARTIST, prefix + "URL Artist");
			row.add(MediaStore.Audio.AudioColumns.ALBUM, prefix + "URL Album");
			row.add(MediaStore.Audio.AudioColumns.YEAR, 2020); // Integer
			row.add(TrackProviderConsts.COLUMN_ALBUM_ARTIST, prefix + "URL Album Artist");
			row.add(MediaStore.Audio.AudioColumns.COMPOSER, prefix + "URL Composer");
			row.add(TrackProviderConsts.COLUMN_GENRE, prefix + "URL Genre");
			// Track number. Optional, but needed for proper sorting in albums
			row.add(MediaStore.Audio.AudioColumns.TRACK, trackNum);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_SAMPLE_RATE, 44100);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_CHANNEL_COUNT, 2);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_BIT_RATE, 128000);
			// Optional, used just for Info/Tags and lists  (for hi-res)
			row.add(TrackProviderConsts.COLUMN_BITS_PER_SAMPLE, 16);
			if(sendWave && duration > 0) {
				row.add(TrackProviderConsts.COLUMN_TRACK_WAVE, TrackProviderHelper.floatsToBytes(genRandomWave())); // We must put byte[] array here
			} else if(sendEmptyWave) {
				// Add this for the default waveseek if you don't want URL to be downloaded one more time and scanned for the wave
				row.add(TrackProviderConsts.COLUMN_TRACK_WAVE, new byte[0]);
			} // Else we allow Poweramp to scan URL for wave
		}
	}

	private float[] genRandomWave() {
		float[] wave = new float[100];
		for(int i = 0; i < wave.length; i++) {
			wave[i] = (float)(Math.random() * 2.0 - 1.0);
		}
		return wave;
	}

	private void fillFolderRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row, int flags) {
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
		// Here we're returning actual folder name, but Poweramp supports anything in display name for folders, not necessary the name matching or related to the documentId or path.
		row.add(Document.COLUMN_DISPLAY_NAME, getShortDirName(documentId));
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);

		boolean hasThumb = documentId.endsWith("1");
		if(hasThumb) {
			row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL); // Thumbnails for folders are supported since build 869
		}
		// If asked to add the subfolders hint, add it
		if(flags != 0) {
			row.add(TrackProviderConsts.COLUMN_FLAGS, flags);
		}
	}

	private void fillPlaylistRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row) {
		// NOTE: for playlists, the playlist documentId should preferable end with some extension. Poweramp also looks into mime type, or assumes it's .m3u8 playlist if no mime type
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, "audio/mpegurl");
		// The display name defines name of the track "file" in "Show File Names" mode. There is also a title via MediaStore.MediaColumns.TITLE.
		// It's up to you how you define display name, it can be anything filename alike, or it can just match track title
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		// As our assets data is always static, we just return own apk installation time. For real folder structure, preferable last modified for given folder should be returned.
		// This ensures Poweramp incremental scanning process. If we return <= 0 value here, Poweramp will be forced to rescan whole provider hierarchy each time it scans
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);
	}

	private void fillTrackRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row, boolean addMetadata, boolean sendWave, boolean sendLyrics,
	                          int trackNum, int trackNumAlt
	) {
		boolean isFlac = documentId.endsWith(".flac");
		boolean isDubstep = documentId.contains("dubstep");

		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, isFlac ? "audio/flac" : "audio/mpeg");
		// The display name defines name of the track "file" in "Show File Names" mode. There is also a title via MediaStore.MediaColumns.TITLE.
		// It's up to you how you define display name, it can be anything filename alike, or it can just match track title
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		// As our assets data is always static, we just return own apk installation time. For real folder structure, preferable last modified for given folder should be returned.
		// This ensures Poweramp incremental scanning process. If we return <= 0 value here, Poweramp will be forced to rescan whole provider hierarchy each time it scans
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);

		String filePath = docIdToFileName(documentId); // We only have 2 real mp3s here for many "virtual" tracks

		// Optional, real provider should preferable return real track file size here or 0.
		row.add(Document.COLUMN_SIZE, getAssetFileSize(getContext().getResources().getAssets(), filePath));

		// NOTE: Poweramp doesn't need extra metadata (except COLUMN_URL/DURATION for streams) for queryDocuments,
		// but requires that for queryDocument for tracks, which are not direct fd. Direct fd tracks still can be quickly scanned by Poweramp, but
		// socket/pipe/url tracks won't be scanned and thus metadata is required for them

		// If provided, COLUMN_TRACK_ALT will sort tracks differently (for "by track #" sorting) in Folders/Folders Hierarchy
		if(trackNumAlt > 0) {
			row.add(TrackProviderConsts.COLUMN_TRACK_ALT, trackNumAlt);
		}

		if(addMetadata) {
			// Some dump tags logic - as we have 2 static files here as an example, but they have docId like dubstep1.mp3, summer2.mp3, etc.
			// Real provider should get this info from network or extract from the file
			String prefix = isDubstep ? "Dubstep " : "Summer ";

			int flags = 0;

			// Setting this will cause Poweramp to ask for track album art via getDocumentThumbnail, but only if other metadata (MediaStore.MediaColumns.TITLE/MediaStore.MediaColumns.DURATION) exists
			flags |= Document.FLAG_SUPPORTS_THUMBNAIL;

			row.add(Document.COLUMN_FLAGS, flags);
			row.add(MediaStore.MediaColumns.TITLE, prefix + "Track " + trackNum);
			row.add(MediaStore.Audio.AudioColumns.ARTIST, prefix + "Artist");
			row.add(MediaStore.Audio.AudioColumns.DURATION, isDubstep ? 125000L : 217000L); // Milliseconds, long
			row.add(MediaStore.Audio.AudioColumns.ALBUM, prefix + "Album");
			row.add(MediaStore.Audio.AudioColumns.YEAR, isDubstep ? 2020 : 2019); // Integer
			row.add(TrackProviderConsts.COLUMN_ALBUM_ARTIST, prefix + "Album Artist");
			row.add(MediaStore.Audio.AudioColumns.COMPOSER, prefix + " Composer");
			row.add(TrackProviderConsts.COLUMN_GENRE, prefix + " Genre");
			// Track number. Optional, but needed for proper sorting in albums.
			// If not defined (or set to <= 0), Poweramp will use cursor position for track - this may be useful for folders where we want default cursor based ordering of items -
			// exactly as provided by cursor. Just don't send MediaStore.Audio.AudioColumns.TRACK column for such tracks.
			// NOTE: Poweramp won't scan track number from filename for provider provided tracks, nor it will cut number (e.g. "01-" from "01-trackname") from displayName
			// as it does by default for normal filesystem tracks
			row.add(MediaStore.Audio.AudioColumns.TRACK, trackNum);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_SAMPLE_RATE, isDubstep ? 44100 : 48000);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_CHANNEL_COUNT, 2);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_BIT_RATE, isFlac ? 720000 : (isDubstep ? 128000 : 192000));
			// Optional, used just for Info/Tags and lists  (for hi-res)
			row.add(TrackProviderConsts.COLUMN_BITS_PER_SAMPLE, 16);

			if(sendWave) {
				row.add(TrackProviderConsts.COLUMN_TRACK_WAVE, TrackProviderHelper.floatsToBytes(genRandomWave()));
			}

			if(sendLyrics) {
				row.add(TrackProviderConsts.COLUMN_TRACK_LYRICS, "La la la\nLyrics for track " + prefix + "Track " + trackNum);
			}
		}
	}

	/**
	 * @param sortOrder this field is not used directly as sorting order as Poweramp always use some user defined sorting which is based on track # or other user selected
	 * criteria. Instead, we use sortOrder as optional additional parameter for things like
	 */
	@Override
	public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "queryChildDocuments parentDocumentId=" + parentDocumentId + " projection=" + Arrays.toString(projection));

		try {
			AssetManager assets = getContext().getResources().getAssets();
			String[] filesAndDirs = assets.list(parentDocumentId);

			// To demonstrate folders and files sorting based on cursor position, sort and reverse the array. Do this for Root1
			if(parentDocumentId.equals("root1")) {
				Arrays.sort(filesAndDirs, 0, filesAndDirs.length, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o2.compareToIgnoreCase(o1);
					}
				});
			}

			// We are adding metadata for root2 and check if it's actually requested as a small optimization (which can be big if track metadata retrieval requires additional processing)
			boolean addMetadata = parentDocumentId.startsWith("root2") && projection != null && arrayContains(projection, MediaStore.MediaColumns.TITLE);

			if(LOG) Log.w(TAG, "queryChildDocuments filesAndDirs=" + Arrays.toString(filesAndDirs));

			final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));

			int ix = 0;
			for(String fileOrDir : filesAndDirs) {
				String path = parentDocumentId + "/" + fileOrDir; // Path is our documentId. Note that this provider defines paths/documentIds format. Poweramp treats them as opaque string

				if(isAssetDir(assets, path)) {
					fillFolderRow(path, c.newRow(), hasSubDirs(assets, path) ? TrackProviderConsts.FLAG_HAS_SUBDIRS : TrackProviderConsts.FLAG_NO_SUBDIRS);

				} // Else this is empty.txt file, we skip it
			}

			// Generate some number of files for given folder

			// NOTE: Poweramp scans each directly once for its normal scans, and never rescans them again, until:
			// - parent folder lastModified changed
			// - full rescan required by user or external intent

			int count = parentDocumentId.length(); // Just various number based on parent document path length

			String docId;

			if(parentDocumentId.equals("root3")) {
				// For root3 add m3u8 playlist
				fillPlaylistRow(parentDocumentId + "/" + "streams-playlist.m3u8", c.newRow());

				// Add dynamic URL tracks
				docId = parentDocumentId + "/" + "dubstep" + "-" + 1 + DOCID_DYNAMIC_URL_SUFFIX;
				fillURLRow(docId, c.newRow(),
						TrackProviderConsts.DYNAMIC_URL,
						DUBSTEP_SIZE,
						null, // NOTE: titles not sent here
						DUBSTEP_DURATION_MS,
						false, false, false); // Not sending metadata here

				docId = parentDocumentId + "/" + "summer" + "-" + 2 + DOCID_DYNAMIC_URL_SUFFIX;
				fillURLRow(docId, c.newRow(),
						TrackProviderConsts.DYNAMIC_URL,
						SUMMER_SIZE,
						null, // NOTE: titles not sent here
						SUMMER_DURATION_MS,
						false, false, false); // Not sending metadata here

				// And fill with random number of http links to the tracks
				for(int i = 0; i < count; i++) {
					boolean isDubstep = (i & 1) != 0;
					boolean isStream = i == 0; // First track here will be a "stream" - non seekable, no duration
					docId = parentDocumentId + "/" + (isDubstep ? "dubstep" : "summer") + "-" + (i + 3) + DOCID_STATIC_URL_SUFFIX;
					fillURLRow(docId, c.newRow(),
							isDubstep ? DUBSTEP_HTTP_URL : SUMMER_HTTP_URL,
							isDubstep ? DUBSTEP_SIZE : SUMMER_SIZE,
							null, // NOTE: titles not sent here
							isStream ? 0 : (isDubstep ? DUBSTEP_DURATION_MS : SUMMER_DURATION_MS),
							false, false, false);
				}

			} else {
				// For root1 and root2 generate docId like root1/Folder2/dubstep-10.mp3

				// For root1, demonstrate Folders/Folders Hierarchy sorting based on alternative track number
				// We reverse number positions of tracks here, but still providing non-reversed track number to use as tag number in albums and other non-folder categories
				for(int i = 0; i < count; i++) {
					docId = parentDocumentId + "/" + ((i & 1) != 0 ? "dubstep" : "summer") + "-" + (i + 1) + (i == 1 ? ".flac" : ".mp3"); // First dubstep track will be flac
					int sort = i + 1;
					int sortAlt = 0;
					if(parentDocumentId.equals("root1")) {
						sortAlt = count - i;
					}
					fillTrackRow(docId, c.newRow(), addMetadata, false, false, sort, sortAlt);
				}
			}

			return c;
		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + parentDocumentId, th);
		}

		return null;
	}

	/**
	 * Simple method to check our assets directory structure for children folders. We have only folders and empty.txt files there
	 */
	private boolean hasSubDirs(AssetManager assets, String path) {
		try {
			String[] filesAndDirs = assets.list(path);
			if(filesAndDirs != null && filesAndDirs.length > 0) {
				for(String child : filesAndDirs) {
					if(!child.endsWith(".txt")) {
						return true;
					}
				}
			}
		} catch(IOException e) {
			Log.e(TAG, path, e);
		}
		return false;
	}

	/** Send album art for tracks with track-provided metadata */
	@Override
	public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {

		String imageSrc = null;

		if(documentId.endsWith(".mp3") || documentId.endsWith(".flac") || documentId.endsWith(DOCID_STATIC_URL_SUFFIX)
			|| documentId.endsWith(DOCID_DYNAMIC_URL_SUFFIX)
		) {
			// We have just 2 images here and we ignore sizeHint. Poweramp preferred image size is 1024x1024px
			boolean isDubstep = documentId.contains("dubstep");
			imageSrc = isDubstep ? "cover-1.jpg" : "cover-2.jpg";

		} else {
			// Assume this is a folder. Real provider should verify if documentId is a real folder
			imageSrc = "folder-1.jpg";
		}

		if(LOG) Log.w(TAG, "openDocumentThumbnail documentId=" + documentId + " sizeHint=" + sizeHint + " imageSrc=" + imageSrc);

		if(imageSrc == null) throw new FileNotFoundException(documentId);


		try {
			return getContext().getResources().getAssets().openFd(imageSrc);
		} catch(IOException e) {
			throw new FileNotFoundException(documentId);
		}
	}

	/**
	 * Send actual track data as direct file descriptor or seekable socket protocol
	 */
	@Override
	public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openDocument documentId=" + documentId + " mode=" + mode + " callingPak=" + getCallingPackage());
		int accessMode = ParcelFileDescriptor.parseMode(mode);
		if((accessMode & ParcelFileDescriptor.MODE_READ_ONLY) == 0) throw new IllegalAccessError("documentId=" + documentId + " mode=" + mode);

		// Poweramp provides CancellationSignal, so we may want to check that on open and periodically (for example, in case of using pipe here)
		if(signal != null) {
			signal.throwIfCanceled();
		}

		String filePath = docIdToFileName(documentId);
		if(filePath == null) throw new FileNotFoundException(documentId);

		// Let's send root2 "dubstep" via seekable socket and other files - via direct fd. Don't do this for root1 where no metadata given and Poweramp expects direct fd tracks
		// Check package name for Poweramp (or other known client) which supports this fd protocol
		String pak = getCallingPackage();
		if(pak != null && pak.equals(PowerampAPIHelper.getPowerampPackageName(getContext()))
				&& documentId.startsWith("root2/") && documentId.contains("dubstep")
		) {
			// Let's open dubstep-2 via milliseconds based seekbable sockets, and other dubsteps - via byte offset seekable sockets
			if(documentId.endsWith("-2.flac")) {
				return openViaSeekableSocket2(documentId, filePath, signal);
			} else {
				return openViaSeekableSocket(documentId, filePath, signal);
			}
		}

		// Let's send root2 "summer" via seekable proxy file descriptors. No need to check for client package as these file descriptors should be supported everywhere
		if(Build.VERSION.SDK_INT >= 26 && USE_OPEN_PROXY_FILE_DESCRIPTOR && documentId.startsWith("root2/") && documentId.contains("summer")) {
			return openViaProxyFd(documentId, filePath, signal);
		}

		return openViaDirectFD(documentId, filePath);
	}

	/** For given docId, return appropriate asset file name */
	private @Nullable String docIdToFileName(String documentId) {
		if(documentId.endsWith(".m3u8")) {
			return "streams-playlist.m3u8";

		} else if(documentId.endsWith(".mp3")) {
			boolean isDubstep = documentId.contains("dubstep");
			return isDubstep ? "bensound-dubstep.mp3" : "bensound-summer.mp3"; // We only have 2 real mp3s here for many "virtual" tracks

		} else if(documentId.endsWith(".flac")) {
			return "bensound-dubstep.flac";

		}
		return null;
	}

	/** This version of the method uses byte offset based seeks */
	private ParcelFileDescriptor openViaSeekableSocket(final String documentId, String filePath, final CancellationSignal signal) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openViaSeekableSocket documentId=" + documentId + " filePath=" + filePath);
		try {
			final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createSocketPair();
			final File file = new File(getContext().getFilesDir(), filePath);
			final long fileLength = file.length();

			// NOTE: it's not possible to use timeouts on this side of the socket as Poweramp may open and hold the socket for an indefinite time while in the paused state
			// NOTE: don't use AsyncTask or other short-time thread pools here, as:
			// - this thread will be alive as long as Poweramp holds the file
			// - this can take an indefinite time, as Poweramp can be paused on the file

			new Thread(new Runnable() {
				public void run() {
					// NOTE: we can use arbitrary buffer size here >0, but increasing buffer will increase non-seekable "window" at the end of file
					// Using buffer size > MAX_DATA_SIZE will cause buffer to be split into multiple packets
					ByteBuffer buf = ByteBuffer.allocateDirect(TrackProviderProto.MAX_DATA_SIZE);
					buf.order(ByteOrder.nativeOrder());

					try(FileInputStream fis = new FileInputStream(file)) {
						FileChannel fc = fis.getChannel(); // We'll be using nio for the buffer loading
						try(TrackProviderProto proto = new TrackProviderProto(fds[1], fileLength)) {

							proto.sendHeader(); // Send initial header

							while(true) {
								int len;
								while((len = fc.read(buf)) > 0) {
									buf.flip();

									// Send some data to Poweramp and optionally receive seek request
									// NOTE: avoid sending empty buffers here (!buf.hasRemaining()), as this will cause premature EOF
									long seekRequestPos = proto.sendData(buf);

									handleSeekRequest(proto, seekRequestPos, fc, fileLength); // May be handle seek request

									buf.clear();
								}

								// Here we're almost done with the file and hit EOF. Still keep file and socket opened until Poweramp closes socket
								//
								// As we may send number of pre-loaded buffers to Poweramp we may hit EOF much earlier prior the track actually finishes playing:
								// - we hit EOF here and may attempt to exit this thread/close socket
								// - Poweramp plays some buffered data
								// - user seeks the track. Poweramp will fail to seek it as our provider is done with the track and socket is closed
								//
								// Solution to this is to keep file and socket opened here and to continue seek commands processing until Poweramp actually closes socket.
								// This scenario can be easily tested by pausing Poweramp close to the track end and seeking while paused

								long seekRequestPos = proto.sendEOFAndWaitForSeekOrClose();
								if(handleSeekRequest(proto, seekRequestPos, fc, fileLength)) {
									if(LOG) Log.w(TAG, "openViaSeekableSocket file seek past EOF documentId=" + documentId);
									continue; // We've just processed extra seek request, continue sending buffers
								} else {
									break; // We done here, Poweramp closed socket
								}
							}

							if(LOG) Log.w(TAG, " openViaSeekableSocket file DONE documentId=" + documentId);
						}
					} catch(TrackProviderProto.TrackProviderProtoClosed ex) {
						if(LOG) Log.w(TAG, "openViaSeekableSocket closed documentId=" + documentId + " " + ex.getMessage());
					} catch(Throwable th) {
						// If we're here, we can't do much - close connection, release resources, and exit
						Log.e(TAG, "documentId=" + documentId, th);
					}
				}
			}).start();

			return fds[0];

		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + documentId, th);
			throw new FileNotFoundException(documentId);
		}
	}

	/** This version of the method uses milliseconds offset based seek requests */
	private ParcelFileDescriptor openViaSeekableSocket2(final String documentId, String filePath, final CancellationSignal signal) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openViaSeekableSocket2 documentId=" + documentId + " filePath=" + filePath);
		try {
			final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createSocketPair();
			final File file = new File(getContext().getFilesDir(), filePath);

			//long fileLength = file.length();

			// NOTE: for the sake of testing, let's send some approximation of the file instead, based on
			// duration and average bytes per ms
			// The real DUBSTEP_AVERAGE_BYTES_PER_MS depends on compression and file format
			final long fileLength = DUBSTEP_FAKE_AVERAGE_BYTES_PER_MS * DUBSTEP_DURATION_MS;

			// NOTE: it's not possible to use timeouts on this side of the socket as Poweramp may open and hold the socket for an indefinite time while in the paused state
			// NOTE: don't use AsyncTask or other short-time thread pools here, as:
			// - this thread will be alive as long as Poweramp holds the file
			// - this can take an indefinite time, as Poweramp can be paused on the file

			new Thread(new Runnable() {
				public void run() {
					// NOTE: we can use arbitrary buffer size here >0, but increasing buffer will increase non-seekable "window" at the end of file
					// Using buffer size > MAX_DATA_SIZE will cause buffer to be split into multiple packets
					ByteBuffer buf = ByteBuffer.allocateDirect(TrackProviderProto.MAX_DATA_SIZE);
					buf.order(ByteOrder.nativeOrder());

					try(FileInputStream fis = new FileInputStream(file)) {
						FileChannel fc = fis.getChannel(); // We'll be using nio for the buffer loading
						try(TrackProviderProto proto = new TrackProviderProto(fds[1], fileLength)) {

							proto.sendHeader(); // Send initial header

							while(true) {
								int len;
								while((len = fc.read(buf)) > 0) {
									buf.flip();

									// Send some data to Poweramp and optionally receive seek request
									// NOTE: avoid sending empty buffers here (!buf.hasRemaining()), as this will cause premature EOF
									TrackProviderProto.SeekRequest seekRequest = proto.sendData2(buf);

									handleSeekRequest2(proto, seekRequest, fc, fileLength); // May be handle seek request

									buf.clear();
								}

								// Here we're almost done with the file and hit EOF. Still keep file and socket opened until Poweramp closes socket
								//
								// As we may send number of pre-loaded buffers to Poweramp we may hit EOF much earlier prior the track actually finishes playing:
								// - we hit EOF here and may attempt to exit this thread/close socket
								// - Poweramp plays some buffered data
								// - user seeks the track. Poweramp will fail to seek it as our provider is done with the track and socket is closed
								//
								// Solution to this is to keep file and socket opened here and to continue seek commands processing until Poweramp actually closes socket.
								// This scenario can be easily tested by pausing Poweramp close to the track end and seeking while paused

								TrackProviderProto.SeekRequest seekRequest = proto.sendEOFAndWaitForSeekOrClose2();
								if(handleSeekRequest2(proto, seekRequest, fc, fileLength)) {
									if(LOG) Log.w(TAG, "openViaSeekableSocket2 file seek past EOF documentId=" + documentId);
									continue; // We've just processed extra seek request, continue sending buffers
								} else {
									break; // We done here, Poweramp closed socket
								}
							}

							if(LOG) Log.w(TAG, " openViaSeekableSocket2 file DONE documentId=" + documentId);
						}
					} catch(TrackProviderProto.TrackProviderProtoClosed ex) {
						if(LOG) Log.w(TAG, "openViaSeekableSocket2 closed documentId=" + documentId + " " + ex.getMessage());
					} catch(Throwable th) {
						// If we're here, we can't do much - close connection, release resources, and exit
						Log.e(TAG, "documentId=" + documentId, th);
					}
				}
			}).start();

			return fds[0];

		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + documentId, th);
			throw new FileNotFoundException(documentId);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private ParcelFileDescriptor openViaProxyFd(final String documentId, final String filePath, final CancellationSignal signal) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openViaProxyFd documentId=" + documentId + " filePath=" + filePath);
		FileInputStream fis = null;
		try {
			final File file = new File(getContext().getFilesDir(), filePath);
			final long fileLength = file.length();
			fis = new FileInputStream(file);
			final FileDescriptor fd = fis.getFD();

			final HandlerThread thread = new HandlerThread(documentId); // This is the thread we're handling fd reading on
			thread.start();
			Handler handler = new Handler(thread.getLooper());

			StorageManager storageManager = (StorageManager)getContext().getSystemService(Context.STORAGE_SERVICE);

			final FileInputStream finalFis = fis;

			ParcelFileDescriptor pfd = storageManager.openProxyFileDescriptor(ParcelFileDescriptor.MODE_READ_ONLY, new ProxyFileDescriptorCallback() {
				@Override
				public long onGetSize() throws ErrnoException {
					if(LOG) Log.w(TAG, "onGetSize fileLength=" + fileLength + " documentId=" + documentId);
					return fileLength;
				}

				public int onRead(long offset, int size, byte[] data) throws ErrnoException {
					try {
						// NOTE: doing direct low level reads on file descriptor
						// Real provider, for example, for http streaming, could track offset and request remote seek if needed,
						// then read data from http stream to byte[] data

						if(LOG) Log.w(TAG, "onRead documentId=" + documentId + " offset=" + offset + " size=" + size + " thread=" + Thread.currentThread());

						return Os.pread(fd, data, 0, size, offset);

					} catch(ErrnoException errno) {
						Log.e(TAG, "documentId=" + documentId + " filePath=" + filePath, errno);
						throw errno; // Rethrow

					} catch(Throwable e) {
						Log.e(TAG, "documentId=" + documentId + " filePath=" + filePath, e);
						throw new ErrnoException("onRead", OsConstants.EBADF);
					}
				}

				@Override
				public void onRelease() {
					if(LOG) Log.w(TAG, "openViaProxyFd onRelease");

					thread.quitSafely();

					closeSilently(finalFis);

					if(LOG) Log.w(TAG, "openViaProxyFd DONE");
				}
			}, handler);

			return pfd;
		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + documentId, th);
			closeSilently(fis); // If we here, we failed with or prior the proxy, so close everything
			throw new FileNotFoundException(documentId);
		}
	}

	/**
	 * THREADING: worker thread
	 * @return true if we actually handled seek request, false otherwise
	 */
	private boolean handleSeekRequest(@NonNull TrackProviderProto proto, long seekRequestPos, @NonNull FileChannel fc, long fileLength) {
		if(seekRequestPos != TrackProviderProto.INVALID_SEEK_POS) {
			// We have a seek request.
			// Your code may take any reasonable time to fulfil the seek request, e.g. it can reopen http connection with appropriate offset, etc.
			// Poweramp just waits for the seek result packet (the waiting is limited by the user-set timeout).
			// Now we should either send appropriate seek result packet or close the connection (e.g. on some error).
			// Poweramp ignores any other packets sent before the seek result packet.

			if(LOG) Log.w(TAG, "handleSeekRequest seekRequestPos=" + seekRequestPos + " fileLength=" + fileLength);

			long newPos = seekTrack(fc, seekRequestPos, fileLength);
			proto.sendSeekResult(newPos);
			return true;
		}
		return false;
	}

	/**
	 * THREADING: worker thread<br>
	 * NOTE: this version handles seek request based on byte offset BUT it can also handle it based on milliseconds.<br>
	 * Still, we need to send some byte based newPos offset back to Poweramp. The resulting byte offset may be an approximation
	 * @return true if we actually handled seek request, false otherwise
	 */
	private boolean handleSeekRequest2(@NonNull TrackProviderProto proto, @Nullable TrackProviderProto.SeekRequest seekRequest,
	                                   @NonNull FileChannel fc, long fileLength
	) {
		if(seekRequest != null && seekRequest.offsetBytes != TrackProviderProto.INVALID_SEEK_POS) {
			// We have a seek request.
			// Your code may take any reasonable time to fulfil the seek request, e.g. it can reopen http connection with appropriate offset, etc.
			// Poweramp just waits for the seek result packet (the waiting is limited by the user-set timeout).
			// Now we should either send appropriate seek result packet or close the connection (e.g. on some error).
			// Poweramp ignores any other packets sent before the seek result packet.

			if(LOG) Log.w(TAG, "handleSeekRequest seekRequestPos=" + seekRequest + " fileLength=" + fileLength);

			// NOTE: we could seek track here based on seekRequest.ms field.
			// In this case we still need to send back newPos as new byte offset.
			// This may be a rough approximation, e.g. something like: newPos = seekRequest.ms * averageBytesPerMsInThisFile

			// For the sake of testing, we'll seek track properly here, but will send "fake" newPos

			long newPos = seekTrack(fc, seekRequest.offsetBytes, fileLength);

			final long fakeAverageBytesPerMs = 116; // ~116 bytes per ms in bensound-dubstep.flac
			long fakeNewPos = seekRequest.ms * fakeAverageBytesPerMs;

			proto.sendSeekResult(fakeNewPos);
			return true;
		}
		return false;
	}

	/**
	 * THREADING: worker thread.
	 * @return new position within the track, or <0 on error
	 * */
	private long seekTrack(@NonNull FileChannel channel, long seekPosBytes, long fileLength) {
		// Out seeking is simple as we just seek the FileChannel
		try {
			if(seekPosBytes >= 0) {
				channel.position(seekPosBytes);
			} else { // If seekPos < 0, this is a seek request from the end of the file
				channel.position(fileLength + seekPosBytes);
			}

			return channel.position();

		} catch(IOException ex) {
			Log.e(TAG, "seekPosBytes=" + seekPosBytes, ex);
			return -1;
		}
	}

	/**
	 * For tracks available on the device as file, it's much easier to send direct file descriptor pointing to the file itself. The file descriptor is seekable
	 * and track can be reopened multiple times in this case, e.g. if tags, seek-wave, or album art scanning is needed.
	 */
	private ParcelFileDescriptor openViaDirectFD(@NonNull String documentId, @NonNull String filePath) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openViaDirectFD documentId=" + documentId + " filePath=" + filePath);
		try {
			if(USE_MP3_COPY) {
				File file = new File(getContext().getFilesDir(), filePath);
				return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
			} else {
				AssetFileDescriptor afd = getContext().getResources().getAssets().openFd(filePath);
				ParcelFileDescriptor pfd = afd.getParcelFileDescriptor();
				Os.lseek(pfd.getFileDescriptor(), afd.getStartOffset(), OsConstants.SEEK_SET);
				if(LOG) Log.w(TAG, "openDocument afd.offset=" + afd.getStartOffset() + " len=" + afd.getLength() + " lseek=" + Os.lseek(pfd.getFileDescriptor(), 0, OsConstants.SEEK_CUR));
				return pfd;
			}
		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + documentId, th);
			throw new FileNotFoundException(documentId);
		}
	}

	/**
	 * Implementing CALL_GET_URL here to return dynamic URL for given track. Document uri is passed as string in arg
	 */
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		Bundle res = super.call(method, arg, extras);
		if(res == null) {
			if(TrackProviderConsts.CALL_GET_URL.equals(method)) {
				return handleGetUrl(arg, extras);

			} else if(TrackProviderConsts.CALL_RESCAN.equals(method)) {
				return handleRescan(arg, extras);

			} else if(TrackProviderConsts.CALL_GET_DIR_METADATA.equals(method)) {
				return handleGetDirMetadata(arg, extras);

			} else Log.e(TAG, "call bad method=" + method, new Exception());
		}
		return res;
	}

	/**
	 * Returns dynamic URL for given track. Document uri is passed as string in arg
	 * @return bundle with the appropriate data is expected, or null error
	 */
	private Bundle handleGetUrl(String arg, Bundle extras) {
		if(TextUtils.isEmpty(arg)) throw new IllegalArgumentException(arg);
		Uri uri = Uri.parse(arg);
		enforceTree(uri);
		String documentId = DocumentsContract.getDocumentId(uri);
		if(documentId.endsWith(DOCID_DYNAMIC_URL_SUFFIX)) {
			boolean isDubstep = documentId.contains("dubstep");
			Bundle res = new Bundle();
			String url = isDubstep ? DUBSTEP_HTTP_URL :  SUMMER_HTTP_URL;
			res.putString(TrackProviderConsts.COLUMN_URL, url);

			// Optionally add some headers to send with given url. These headers are used just once for this track playback and are not persisted
			res.putString(TrackProviderConsts.COLUMN_HEADERS, "Debug-header1: some\r\nDebug-header2: another\r\n");

			// Optionally add some cookies. These cookies are used just once for this track playback and are not persisted
			res.putString(TrackProviderConsts.COLUMN_COOKIES, "cookie1=value1; Secure\\ncookie2=value; SameSite=Strict");

			// Optionally set some http method. By default it's GET, setting GET here for the demonstration purpose
			res.putString(TrackProviderConsts.COLUMN_HTTP_METHOD, "GET");

			if(LOG) Log.w(TAG, "call CALL_GET_URL url=>" + url);
			return res;
		} else Log.e(TAG, "call CALL_GET_URL bad documentId=" + documentId, new Exception());
		return null;
	}

	/**
	 * Provider is informed regarding the automatic or user initiated scan.<br>
	 * This is called prior Poweramp calls any {@link android.provider.DocumentsProvider#queryChildDocuments} and other methods to rescan the actual files hierarchy and metadata.<br><br>
	 *
	 * Depending on ths passed arguments, provider may do some refresh on data - if absolutely needed. Poweramp (and user) waits until this method returns, so in most cases,
	 * it should return as soon as possible.<br><br>
	 *
	 * If we're sending the rescan intent from this app (see {@link MainActivity#sendScanThisSubdir(View)}), we're getting the extras we sent.
	 */
	private Bundle handleRescan(String arg, Bundle extras) {
		if(LOG) Log.w(TAG, "handleRescan extras=" + dumpBundle(extras));

		// Analyze optional EXTRA_PROVIDER and EXTRA_PATH here.
		// If EXTRA_PROVIDER matches this provider, we may update the cached remote data
		String targetProvider = extras.getString(PowerampAPI.Scanner.EXTRA_PROVIDER);

		if(TextUtils.equals(targetProvider, getContext().getPackageName())) { // Assuming authority == package name for this provider

			String path = extras.getString(PowerampAPI.Scanner.EXTRA_PATH);

			if(!TextUtils.isEmpty(path)) {

				// - update data just for the EXTRA_PATH sub-directory hierarchy

				if(LOG) Log.w(TAG, "handleRescan targeted rescan path=" + path);

			} else {

				// - update data from the remote server

				if(LOG) Log.w(TAG, "handleRescan targeted provider rescan");

			}
		} else if(TextUtils.isEmpty(targetProvider)) {

			// This is non-targeted scan request:
			// - from the Poweramp UI for categories outside this provider folders
			// - from the Poweramp Settings, including Full Rescan
			// We can ignore those, or we can handle specific cases, e.g. we may want to reload everything from the remote server if this is a Full Rescan
			// (Poweramp indicates this with both EXTRA_ERASE_TAGS and EXTRA_FULL_RESCAN set)

			if(extras.getBoolean(PowerampAPI.Scanner.EXTRA_ERASE_TAGS) && extras.getBoolean(PowerampAPI.Scanner.EXTRA_FULL_RESCAN)) {

				// - force update all data

				if(LOG) Log.w(TAG, "handleRescan forced full update");

			}

		} else {
			// This is targeted to some other provider, ignore
		}

		return null;
	}

	/**
	 * @param arg the directory uri
	 * @return bundle filled with extras: {@link TrackProviderConsts#EXTRA_ANCESTORS}
	 */
	private Bundle handleGetDirMetadata(String arg, Bundle extras) {
		Uri uri = Uri.parse(arg);
		enforceTree(uri);

		// DocumentId for the directory which hierarchy (up to the root) Poweramp wants to retrieve
		String docId = DocumentsContract.getDocumentId(uri);

		// We should return array of ancestor documentIds for given documentId from root to the direct parent of the given documentId

		// As for this provider, we have full path information in the document id itself, we just provide at as is.
		// Real provider may additionally specifically process the directory docId as needed to generate the valid hierarchy path

		String[] segments = docId.split("/");

		if(segments.length <= 1) { // If we have just one segment, this is a root documentId and no parents exist
			if(LOG) Log.w(TAG, "handleGetDirMetadata IGNORE ROOT docId=" + docId + " uri=" + uri);
			return Bundle.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		String[] ancestors = new String[segments.length - 1]; // We want to return all ancestor document ids from root to parent dir, not including the dir in question
		for(int i = 0; i < ancestors.length; i++) {
			sb.append(segments[i]);
			ancestors[i] = sb.toString(); // Return full documentId for each ancestor
			sb.append('/');
		}

		Bundle res =  new Bundle();
		res.putStringArray(TrackProviderConsts.EXTRA_ANCESTORS, ancestors);

		if(LOG) Log.w(TAG, "handleGetDirMetadata uri=" + uri + " docId=" + docId + " ancestors=" + Arrays.toString(ancestors) + " res=" + dumpBundle(res));
		return res;
	}


	/** We need to override this DocumentProvider API method, which is called by super class to check if given documentId is a correct child of parentDocumentId */
	@Override
	public boolean isChildDocument(String parentDocumentId, String documentId) {
		try {
			// As our hierarchy is defined by assets/, we could just return true here, but for a sake of example, let's verify that given documentId is inside the folder
			// This is track, we randomly generate track entries, so we can't verify them
			boolean res = documentId.endsWith(".mp3") || documentId.endsWith(".m3u8") || documentId.endsWith(DOCID_STATIC_URL_SUFFIX)
					|| documentId.startsWith(parentDocumentId) && isAssetDir(getContext().getResources().getAssets(), documentId);

			if(LOG) Log.w(TAG, "isChildDocument =>" + res + " parentDocumentId=" + parentDocumentId + " documentId=" + documentId);

			return res;
		} catch(Throwable th) {
			Log.e(TAG, "parentDocumentId=" + parentDocumentId + " documentId=" + documentId, th);
		}
		return false;
	}

	/**
	 * Very inefficient way of checking folders vs files in assets, but Android SDK doesn't provide anything better.
	 * This shouldn't be used in production providers anyway
	 */
	private boolean isAssetDir(AssetManager assets, @NonNull String path) {
		// Ignore empty.txt
		return !path.endsWith("/empty.txt");
	}

	private long getAssetFileSize(AssetManager assets, String path) {
		try {
			try(AssetFileDescriptor afd = assets.openFd(path)) {
				return afd.getLength();
			}
		} catch(IOException ex) {
			return 0L;
		}
	}

	private static String[] resolveRootProjection(String[] projection) {
		return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
	}

	private static String[] resolveDocumentProjection(String[] projection) {
		return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
	}

	private static String[] resolveTrackProjection(String[] projection) {
		return projection != null ? projection : DEFAULT_TRACK_AND_METADATA_PROJECTION;
	}

	/** Assumes docId is track path, thus we can extract digits after - and before .mp3, e.g. trackNum = 10 for dubstep-10.mp3 */
	private static int extractTrackNum(String docId) {
		int startIx = docId.lastIndexOf('-');
		int endIx = docId.lastIndexOf('.');
		if(startIx < 0 || endIx < 0 || startIx >= endIx) {
			return 0;
		}
		try {
			return Integer.parseInt(docId.substring(startIx + 1, endIx), 10);
		} catch(NumberFormatException ex) {
			Log.e(TAG, "docId=" + docId, ex);
		}
		return 0;
	}

	/**
	 * Returns last segment of the path before last /, or the path itself
	 * NOTE: for /foo/bar/file.mp3 it returns file.mp3, but for /foo/bar or /foo/bar/ it returns bar, so it assumes folder path, not any path
	 * @param path should be a _folder_ path
	 */
	@SuppressWarnings("null")
	private static @NonNull String getShortDirName(@NonNull String path) {
		int ix = path.lastIndexOf('/');
		if(ix == -1) {
			return path;
		}
		int end = path.length();
		if(ix == end - 1) {
			end--;
			ix = path.lastIndexOf('/', end - 1);
		}
		// if ix== -1 it will be 0 in substring (-1 + 1)
		return path.substring(ix + 1, end);
	}

	/**
	 * @return last segment of the path (after the last /), this is usually filename with extension, or the path itself if no slashes
	 */
	@SuppressWarnings("null")
	private static @NonNull String getShortName(@NonNull String path) {
		int ix = path.lastIndexOf('/'); //
		if(ix == -1) {
			ix = path.lastIndexOf('\\');
		}
		if(ix == -1) {
			return path;
		}
		return path.substring(ix + 1);
	}

	/** To provide direct file descriptors, we need to have tracks extracted to local filesystem files */
	private void copyAsset(String assetFile, File targetDir, boolean overwrite) {
		File outFile = new File(targetDir, getShortName(assetFile));
		if(!overwrite && outFile.exists()) {
			if(LOG) Log.w(TAG, "copyAsset IGNORE exists assetFile=" + assetFile + " =>" + outFile);
			return;
		}
		try {
			try(InputStream in = getContext().getResources().getAssets().open(assetFile)) {
				try(OutputStream out = new FileOutputStream(outFile)) {
					byte[] buffer = new byte[16 * 1024];
					int read;
					while((read = in.read(buffer)) != -1){
						out.write(buffer, 0, read);
					}
				}
			}
			if(LOG) Log.w(TAG, "copyAsset assetFile=" + assetFile + " =>" + outFile);
		} catch(IOException ex) {
			Log.e(TAG, "", ex);
		}
	}

	private <T> boolean arrayContains(@NonNull T[] array, T needle) {
		for(T item : array) {
			if(Objects.equals(item, needle)) {
				//if(LOG) Log.w(TAG, "arrayContains FOUND needle=" + needle);
				return true;
			}
		}
		//if(LOG) Log.w(TAG, "arrayContains FAILED needle=" + needle + " array=" + Arrays.toString(array));
		return false;
	}

	private @Nullable String capitalize(@Nullable String documentId) {
		if(documentId != null && documentId.length() > 0) {
			return Character.toUpperCase(documentId.charAt(0)) + documentId.substring(1);
		}
		return documentId;
	}

	@SuppressLint("NewApi")
	private void enforceTree(Uri documentUri) {
		if(DocumentsContract.isTreeUri(documentUri)) { // Exists in SDK=21, but hidden there
			final String parent = DocumentsContract.getTreeDocumentId(documentUri);
			final String child = DocumentsContract.getDocumentId(documentUri);
			if (Objects.equals(parent, child)) {
				return;
			}
			if (!isChildDocument(parent, child)) {
				throw new SecurityException(
						"Document " + child + " is not a descendant of " + parent);
			}
		}
	}

	private static void closeSilently(@Nullable Closeable c) {
		if(c != null) {
			try {
				c.close();
			} catch(IOException ex) {
				Log.e(TAG, "", ex);
			}
		}
	}

	@SuppressWarnings("null")
	private static @NonNull String dumpBundle(@Nullable Bundle bundle) {
		if(bundle == null) {
			return "null bundle";
		}
		StringBuilder sb = new StringBuilder();
		Set<String> keys = bundle.keySet();
		sb.append("\n");
		for(String key : keys) {
			sb.append('\t').append(key).append("=");
			Object val = bundle.get(key);
			sb.append(val);
			if(val != null) {
				sb.append(" ").append(val.getClass().getSimpleName());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
