package com.maxmpz.powerampproviderexample;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.media.MediaFormat;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maxmpz.poweramp.player.TrackProviderConsts;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;


public class ExampleProvider extends DocumentsProvider {
	private static final String TAG = "ExampleProvider";
	private static final boolean LOG = true;

	/**
	 * If true, we're copying our mp3s from assets to local filesystem. This is because Poweramp can't reliable play assets fd - fd pointing to apk file itself, mostly as
	 * end of file can't be properly detected for many formats without proper length header
	 */
	private static final boolean USE_MP3_COPY = true;

	/** Link to mp3 track to demonstrate http track with the duration */
	private static final String DUBSTEP_HTTP_URL = "https://raw.githubusercontent.com/maxmpz/powerampapi/master/poweramp_provider_example/app/src/main/assets/bensound-dubstep.mp3";
	/** Link to mp3 track to demonstrate http track with the duration */
	private static final String SUMMER_HTTP_URL = "https://raw.githubusercontent.com/maxmpz/powerampapi/master/poweramp_provider_example/app/src/main/assets/bensound-summer.mp3";
	/** Docid suffix for http tracks */
	private static final String DOCID_HTTP_SUFFIX = ".http";

	private static final long DUBSTEP_SIZE = 2044859L;
	private static final long DUBSTEP_DURATION = 125000L;
	private static final long SUMMER_SIZE = 4620151L;
	private static final long SUMMER_DURATION = 217000L;


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
			MediaStore.MediaColumns.DURATION,
			MediaStore.Audio.AudioColumns.ARTIST,
			MediaStore.Audio.AudioColumns.ALBUM,
			MediaStore.Audio.AudioColumns.YEAR,
			TrackProviderConsts.COLUMN_ALBUM_ARTIST,
			MediaStore.Audio.AudioColumns.COMPOSER,
			TrackProviderConsts.COLUMN_GENRE,
			MediaStore.Audio.AudioColumns.TRACK,
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
			// Extract our mp3s to storage as Poweramp won't properly play asset apk fd (fd points to apk itself, so Poweramp tries to play apk file, basically playing first found mp3 from it)
			File dir = getContext().getFilesDir();
			copyAsset("bensound-dubstep.mp3", dir, false);
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

			// Items without metadata (Poweramp reads track metadata from track itself)
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
				fillFolderRow(documentId, row);
				// NOTE: we return display name derived from documentId here VS returning the same label as used for Root.COLUMN_TITLE
				// Real app should use same labels in both places (roots and queryDocument) for same root
				row.add(Document.COLUMN_DISPLAY_NAME, capitalize(documentId));
				return c;

			} else if(documentId.startsWith("root3") && documentId.endsWith(DOCID_HTTP_SUFFIX)) { // Http link to mp3 with a duration. We must provide duration here to avoid endless/non-seekable stream

				final MatrixCursor c = new MatrixCursor(resolveTrackProjection(projection));
				int trackNum = extractTrackNum(documentId);
				if(documentId.contains("dubstep")) {
					fillStreamRow(documentId, c.newRow(), DUBSTEP_HTTP_URL, DUBSTEP_SIZE, "Dubstep", trackNum == 1 ? 0 : DUBSTEP_DURATION);
				} else {
					fillStreamRow(documentId, c.newRow(), SUMMER_HTTP_URL, SUMMER_SIZE, "Summer", trackNum == 1 ? 0 : SUMMER_DURATION);
				}
				return c;

			} else if(documentId.endsWith(".mp3")) { // Seems like a track
				// We are adding metadata for root2 and check if it's actually requested as a small optimization (which can be big if track metadata retrieval requires additional processing)
				boolean addMetadata = documentId.startsWith("root2/") && projection != null && arrayContains(projection, MediaStore.MediaColumns.TITLE);
				final MatrixCursor c = new MatrixCursor(resolveTrackProjection(projection));
				fillTrackRow(documentId, c.newRow(), addMetadata);
				return c;

			} else if(documentId.endsWith(".m3u")) { // Seems like a playlist
				final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));
				fillPlaylistRow(documentId, c.newRow());
				return c;

			} else { // This must be a directory
				final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));
				fillFolderRow(documentId, c.newRow());
				return c;
			}

		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + documentId, th);
		}
		return null;
	}

	private void fillStreamRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row, @NonNull String url, long size, @NonNull String title, long duration) {
		// Here we're returning actual folder name, but Poweramp supports anything in display name for folders, not necessary the name matching or related to documentId or path
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, "audio/mpeg");
		// Poweramp doesn't use display name for tracks. It uses either last segment from documentId (as a filename) or MediaStore.MediaColumns.TITLE. This is still used by Android picker
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		// As our assets data is always static, we just return own apk installation time. For real folder structure, preferable last modified for given folder should be returned.
		// This ensures Poweramp incremental scanning process. If we return <= 0 value here, Poweramp will be forced to rescan whole provider hierarchy each time it scans
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);
		// Optional, real provider should preferable return real track file size here or 0
		row.add(Document.COLUMN_SIZE, size);
		// Setting this will cause Poweramp to ask for track album art via getDocumentThumbnail, but only if other metadata (MediaStore.MediaColumns.TITLE/MediaStore.MediaColumns.DURATION) exists
		row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL);

		row.add(TrackProviderConsts.COLUMN_URL, url);

		String prefix = title + " ";

		// Some dump tags logic - as we have 2 static files here as an example, but they have docId like dubstep1.mp3, summer2.mp3, etc.
		// Real provider should get this info from network or extract from the file
		int trackNum = extractTrackNum(documentId);

		row.add(MediaStore.MediaColumns.TITLE, prefix + "Http Track " + trackNum);
		row.add(MediaStore.Audio.AudioColumns.ARTIST, prefix + "Http Artist");
		row.add(MediaStore.MediaColumns.DURATION, duration); // Milliseconds, long. If duration <= 0, this is endless non-seekable stream (e.g. radio)
		row.add(MediaStore.Audio.AudioColumns.ALBUM, prefix + "Http Album");
		row.add(MediaStore.Audio.AudioColumns.YEAR, 2020); // Integer
		row.add(TrackProviderConsts.COLUMN_ALBUM_ARTIST, prefix + "Http Album Artist");
		row.add(MediaStore.Audio.AudioColumns.COMPOSER, prefix + "Http Composer");
		row.add(TrackProviderConsts.COLUMN_GENRE, prefix + "Http Genre");
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
	}

	private void fillFolderRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row) {
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
		// Here we're returning actual folder name, but Poweramp supports anything in display name for folders, not necessary the name matching or related to documentId or path
		row.add(Document.COLUMN_DISPLAY_NAME, getShortDirName(documentId));
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);
	}

	private void fillPlaylistRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row) {
		// NOTE: for playlists, the playlist documentId should preferable end with extension. If not, Poweramp assumes that is .m3u8 playlist
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, "audio/mpegurl");
		// Poweramp doesn't use display name for tracks. It uses either last segment from documentId (as a filename) or MediaStore.MediaColumns.TITLE. This still is used by Android picker
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		// As our assets data is always static, we just return own apk installation time. For real folder structure, preferable last modified for given folder should be returned.
		// This ensures Poweramp incremental scanning process. If we return <= 0 value here, Poweramp will be forced to rescan whole provider hierarchy each time it scans
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);
	}

	private void fillTrackRow(@NonNull String documentId, @NonNull MatrixCursor.RowBuilder row, boolean addMetadata) {
		// Here we're returning actual folder name, but Poweramp supports anything in display name for folders, not necessary the name matching or related to documentId or path
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, "audio/mpeg");
		// Poweramp doesn't use display name for tracks. It uses either last segment from documentId (as a filename) or MediaStore.MediaColumns.TITLE. This still used by Android picker
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		// As our assets data is always static, we just return own apk installation time. For real folder structure, preferable last modified for given folder should be returned.
		// This ensures Poweramp incremental scanning process. If we return <= 0 value here, Poweramp will be forced to rescan whole provider hierarchy each time it scans
		row.add(Document.COLUMN_LAST_MODIFIED, mApkInstallTime);
		// Optional, real provider should preferable return real track file size here or 0
		row.add(Document.COLUMN_SIZE, getAssetFileSize(getContext().getResources().getAssets(), documentId));

		if(addMetadata) {
			// Setting this will cause Poweramp to ask for track album art via getDocumentThumbnail, but only if other metadata (MediaStore.MediaColumns.TITLE/MediaStore.MediaColumns.DURATION) exists
			row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL);

			// Some dump tags logic - as we have 2 static files here as an example, but they have docId like dubstep1.mp3, summer2.mp3, etc.
			// Real provider should get this info from network or extract from the file
			boolean isDubstep = documentId.contains("dubstep");
			String prefix = isDubstep ? "Dubstep " : "Summer ";
			int trackNum = extractTrackNum(documentId);

			row.add(MediaStore.MediaColumns.TITLE, prefix + "Track " + trackNum);
			row.add(MediaStore.Audio.AudioColumns.ARTIST, prefix + "Artist");
			row.add(MediaStore.MediaColumns.DURATION, isDubstep ? 125000L : 217000L); // Milliseconds, long
			row.add(MediaStore.Audio.AudioColumns.ALBUM, prefix + "Album");
			row.add(MediaStore.Audio.AudioColumns.YEAR, isDubstep ? 2020 : 2019); // Integer
			row.add(TrackProviderConsts.COLUMN_ALBUM_ARTIST, prefix + "Album Artist");
			row.add(MediaStore.Audio.AudioColumns.COMPOSER, prefix + " Composer");
			row.add(TrackProviderConsts.COLUMN_GENRE, prefix + " Genre");
			// Track number. Optional, but needed for proper sorting in albums
			row.add(MediaStore.Audio.AudioColumns.TRACK, trackNum);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_SAMPLE_RATE, isDubstep ? 44100 : 48000);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_CHANNEL_COUNT, 2);
			// Optional, used just for Info/Tags
			row.add(MediaFormat.KEY_BIT_RATE, isDubstep ? 128000 : 192000);
			// Optional, used just for Info/Tags and lists  (for hi-res)
			row.add(TrackProviderConsts.COLUMN_BITS_PER_SAMPLE, 16);
		}
	}


	@Override
	public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "queryChildDocuments parentDocumentId=" + parentDocumentId + " projection=" + Arrays.toString(projection));

		try {
			AssetManager assets = getContext().getResources().getAssets();
			String[] filesAndDirs = assets.list(parentDocumentId);

			// We are adding metadata for root2 and check if it's actually requested as a small optimization (which can be big if track metadata retrieval requires additional processing)
			boolean addMetadata = parentDocumentId.startsWith("root2") && projection != null && arrayContains(projection, MediaStore.MediaColumns.TITLE);

			if(LOG) Log.w(TAG, "queryChildDocuments filesAndDirs=" + Arrays.toString(filesAndDirs));

			final MatrixCursor c = new MatrixCursor(resolveDocumentProjection(projection));

			for(String fileOrDir : filesAndDirs) {
				String path = parentDocumentId + "/" + fileOrDir; // Path is our documentId

				if(isAssetDir(assets, path)) {
					fillFolderRow(path, c.newRow());

				} // Else this is empty.txt file, we skip it
			}

			// Generate random # of files (1-20) for given folder

			// NOTE: Poweramp scans each directly once for its normal scans, and never rescans them again, until:
			// - parent folder lastModified changed
			// - full rescan required by user or external intent

			int count = 1 + (int) Math.round(Math.random() * 19.0);

			if(parentDocumentId.equals("root3")) {
				// For root3 add m3u8 playlist
				fillPlaylistRow(parentDocumentId + "/" + "streams-playlist.m3u8", c.newRow());

				// And fill with random number of http links to the tracks
				for(int i = 0; i < count; i++) {
					boolean isDubstep = (i & 1) != 0;
					boolean isStream = i == 0; // First track here will be a "stream" - non seekable, no duration
					String docId = parentDocumentId + "/" + (isDubstep ? "dubstep" : "summer") + "-" + (i + 1) + DOCID_HTTP_SUFFIX;
					fillStreamRow(docId, c.newRow(),
							isDubstep ? DUBSTEP_HTTP_URL : SUMMER_HTTP_URL,
							isDubstep ? DUBSTEP_SIZE : SUMMER_SIZE,
							isDubstep ? "Dubstep" : "Summer",
							isStream ? 0 : (isDubstep ? DUBSTEP_DURATION : SUMMER_DURATION));
				}

			} else {
				// For root1 and root2 generate docId like root1/Folder2/dubstep-10.mp3
				for(int i = 0; i < count; i++) {
					String docId = parentDocumentId + "/" + ((i & 1) != 0 ? "dubstep" : "summer") + "-" + (i + 1) + ".mp3";
					fillTrackRow(docId, c.newRow(), addMetadata);
				}
			}

			if(LOG) Log.w(TAG, "queryChildDocuments generated files=" + count);

			return c;
		} catch(Throwable th) {
			Log.e(TAG, "documentId=" + parentDocumentId, th);
		}

		return null;
	}

	@Override
	public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openDocumentThumbnail documentId=" + documentId + " sizeHint=" + sizeHint);
		if(!documentId.endsWith(".mp3") && !documentId.endsWith(DOCID_HTTP_SUFFIX)) throw new FileNotFoundException(documentId);

		// We have just 2 images here and we ignore sizeHint. Poweramp preferred image size is 1024x1024px

		boolean isDubstep = documentId.contains("dubstep");
		String imageSrc = isDubstep ? "cover-1.jpg" : "cover-2.jpg";

		try {
			return getContext().getResources().getAssets().openFd(imageSrc);
		} catch(IOException e) {
			throw new FileNotFoundException(documentId);
		}
	}

	@Override
	public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
		if(LOG) Log.w(TAG, "openDocument documentId=" + documentId + " mode=" + mode);
		int accessMode = ParcelFileDescriptor.parseMode(mode);
		if((accessMode & ParcelFileDescriptor.MODE_READ_ONLY) == 0) throw new IllegalAccessError("documentId=" + documentId + " mode=" + mode);
		String filePath;

		if(documentId.endsWith(DOCID_HTTP_SUFFIX)) throw new FileNotFoundException(documentId);

		if(documentId.endsWith(".m3u8")) {
			filePath = "streams-playlist.m3u8";

		} else if(documentId.endsWith(".mp3")) {
			boolean isDubstep = documentId.contains("dubstep");
			filePath = isDubstep ? "bensound-dubstep.mp3" : "bensound-summer.mp3"; // We only have 2 real mp3s here for many "virtual" tracks

		} else throw new FileNotFoundException(documentId);
		// NOTE: we can't open in any write modes here, so we always open in read mode and ignore write modes

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
		}
		throw new FileNotFoundException(documentId);
	}

	/** Important method, which is called by DocumentProvider to check if documentId is a correct child of given parent */
	@Override
	public boolean isChildDocument(String parentDocumentId, String documentId) {
		try {
			// As our hierarchy is defined by assets/, we could just return true here, but for a sake of example, let's verify that given documentId is inside the folder
			if(documentId.endsWith(".mp3") || documentId.endsWith(".m3u8") || documentId.endsWith(DOCID_HTTP_SUFFIX)) {
				return true; // This is track, we randomly generate track entries, so we can't verify them
			}

			return isAssetDir(getContext().getResources().getAssets(), parentDocumentId + "/" + documentId);
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
		try {
			AssetFileDescriptor afd = assets.openFd(path);
			afd.close();
			return false; // If we successfully opened it, it's a file
		} catch(IOException ex) {
			return true; // This is a folder or may be it's missing completely
		}
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
				Log.w(TAG, "arrayContains FOUND needle=" + needle);
				return true;
			}
		}
		Log.w(TAG, "arrayContains FAILED needle=" + needle + " array=" + Arrays.toString(array));
		return false;
	}

	private @Nullable String capitalize(@Nullable String documentId) {
		if(documentId != null && documentId.length() > 0) {
			return Character.toUpperCase(documentId.charAt(0)) + documentId.substring(1);
		}
		return documentId;
	}

}
