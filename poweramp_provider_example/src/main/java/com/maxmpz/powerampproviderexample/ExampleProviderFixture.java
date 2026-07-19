package com.maxmpz.powerampproviderexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemClock;
import android.provider.DocumentsContract.Document;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.maxmpz.poweramp.player.TrackProviderConsts;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Objects;

final class ExampleProviderFixture {
	private static final String TAG = "ExampleProviderFixture";
	private static final boolean LOG = true;

	private static final String CALL_CONFIGURE = "scanner_fixture_configure";
	private static final String CALL_REMOVE_SUBTREE = "scanner_fixture_remove_subtree";
	private static final String CALL_RESET = "scanner_fixture_reset";
	private static final String CALL_STATUS = "scanner_fixture_status";

	private static final int SCHEMA = 1;
	private static final String ROOT_DOCUMENT_ID = "root2/__scanner_test__";

	private static final String PREFS = "scanner-fixture";
	private static final String PREF_ENABLED = "enabled";
	private static final String PREF_RUN_ID = "run-id";
	private static final String PREF_FANOUT = "fanout";
	private static final String PREF_DEPTH = "depth";
	private static final String PREF_DEEP_DEPTH = "deep-depth";
	private static final String PREF_SEED_DEPTH = "seed-depth";
	private static final String PREF_SEED_PADDING_FILES = "seed-padding-files";
	private static final String PREF_QUERY_DELAY_MS = "query-delay-ms";
	private static final String PREF_REMOVED_DOCUMENT_ID = "removed-document-id";
	private static final String PREF_MODIFIED_AT_MS = "modified-at-ms";
	private static final String PREF_ROOT_MODIFIED_AT_MS = "root-modified-at-ms";

	private static final int MAX_DIRECTORIES = 100000;
	private static final int MAX_PADDING_FILES = 100000;
	private static final int MAX_QUERY_DELAY_MS = 1000;

	private static final int DIR_ROOT = 0;
	private static final int DIR_SEED = 1;
	private static final int DIR_BROAD = 2;
	private static final int DIR_DEEP = 3;

	private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[] {
		Document.COLUMN_DOCUMENT_ID,
		Document.COLUMN_MIME_TYPE,
		Document.COLUMN_DISPLAY_NAME,
		Document.COLUMN_LAST_MODIFIED,
		Document.COLUMN_FLAGS,
		Document.COLUMN_SIZE,
	};

	private static final class Config {
		final String runId;
		final int fanout;
		final int depth;
		final int deepDepth;
		final int seedDepth;
		final int seedPaddingFiles;
		final int queryDelayMs;
		final String removedDocumentId;
		final long modifiedAtMs;

		Config(
			@NonNull String runId,
			int fanout,
			int depth,
			int deepDepth,
			int seedDepth,
			int seedPaddingFiles,
			int queryDelayMs,
			@Nullable String removedDocumentId,
			long modifiedAtMs
		) {
			this.runId = runId;
			this.fanout = fanout;
			this.depth = depth;
			this.deepDepth = deepDepth;
			this.seedDepth = seedDepth;
			this.seedPaddingFiles = seedPaddingFiles;
			this.queryDelayMs = queryDelayMs;
			this.removedDocumentId = removedDocumentId;
			this.modifiedAtMs = modifiedAtMs;
		}

		Config withRemovedDocumentId(@NonNull String documentId, long modifiedAtMs) {
			return new Config(
				runId,
				fanout,
				depth,
				deepDepth,
				seedDepth,
				seedPaddingFiles,
				queryDelayMs,
				documentId,
				modifiedAtMs
			);
		}
	}

	private static final class Dir {
		final int kind;
		final int level;
		final int index;

		Dir(int kind, int level, int index) {
			this.kind = kind;
			this.level = level;
			this.index = index;
		}
	}

	private final Context mContext;
	private final Object mLock = new Object();
	private volatile Config mConfig;
	private volatile long mRootModifiedAtMs;

	ExampleProviderFixture(@NonNull Context context, long apkInstallTimeMs) {
		mContext = context;
		loadState(apkInstallTimeMs);
	}

	boolean isDocumentId(@Nullable String documentId) {
		return documentId != null &&
			(documentId.equals(ROOT_DOCUMENT_ID) || documentId.startsWith(ROOT_DOCUMENT_ID + "/"));
	}

	long getRootModifiedAtMs() {
		return mRootModifiedAtMs;
	}

	@Nullable Bundle call(@Nullable String method, @Nullable Bundle extras) {
		if(CALL_CONFIGURE.equals(method)) {
			return handleConfigure(extras);
		} else if(CALL_REMOVE_SUBTREE.equals(method)) {
			return handleRemoveSubtree(extras);
		} else if(CALL_RESET.equals(method)) {
			return handleReset(extras);
		} else if(CALL_STATUS.equals(method)) {
			return handleStatus();
		}
		return null;
	}

	Cursor queryDocument(@NonNull String documentId, @Nullable String[] projection) throws FileNotFoundException {
		Config config = mConfig;
		if(config == null) throw new FileNotFoundException(documentId);

		Dir dir = parseDir(config, documentId, false);
		MatrixCursor cursor = new MatrixCursor(resolveDocumentProjection(projection));
		if(dir != null) {
			fillFolderRow(
				documentId,
				cursor.newRow(),
				dirHasSubDirs(config, dir) ? TrackProviderConsts.FLAG_HAS_SUBDIRS : TrackProviderConsts.FLAG_NO_SUBDIRS,
				config.modifiedAtMs
			);
			return cursor;
		}
		if(isPaddingDocument(config, documentId)) {
			fillPaddingRow(documentId, cursor.newRow(), config.modifiedAtMs);
			return cursor;
		}
		throw new FileNotFoundException(documentId);
	}

	Cursor queryChildDocuments(@NonNull String parentDocumentId, @Nullable String[] projection) throws FileNotFoundException {
		Config config = mConfig;
		if(config == null) throw new FileNotFoundException(parentDocumentId);
		Dir parent = parseDir(config, parentDocumentId, false);
		if(parent == null) throw new FileNotFoundException(parentDocumentId);
		if(config.queryDelayMs > 0) SystemClock.sleep(config.queryDelayMs);

		MatrixCursor cursor = new MatrixCursor(resolveDocumentProjection(projection));
		switch(parent.kind) {
			case DIR_ROOT:
				addFolder(config, cursor, seedDocumentId(config, 0), true);
				break;

			case DIR_SEED:
				for(int index = 0; index < config.seedPaddingFiles; index++) {
					String childDocumentId = parentDocumentId + "/" + paddingName(parent.level, index);
					if(!documentRemoved(config, childDocumentId)) {
						fillPaddingRow(childDocumentId, cursor.newRow(), config.modifiedAtMs);
					}
				}
				if(parent.level < config.seedDepth) {
					addFolder(config, cursor, parentDocumentId + "/" + levelName(parent.level), true);
				} else {
					addFolder(config, cursor, broadRootDocumentId(config), true);
					addFolder(config, cursor, deepRootDocumentId(config), true);
				}
				break;

			case DIR_BROAD:
				if(parent.level < config.depth) {
					for(int childIndex = 0; childIndex < config.fanout; childIndex++) {
						String childDocumentId = parentDocumentId + "/" +
							broadName(parent.level, parent.index, childIndex);
						addFolder(config, cursor, childDocumentId, parent.level + 1 < config.depth);
					}
				}
				break;

			case DIR_DEEP:
				if(parent.level < config.deepDepth) {
					String childDocumentId = parentDocumentId + "/" + levelName(parent.level);
					addFolder(config, cursor, childDocumentId, parent.level + 1 < config.deepDepth);
				}
				break;

			default:
				throw new AssertionError(parent.kind);
		}
		return cursor;
	}

	void addRootIfEnabled(@NonNull MatrixCursor cursor) {
		Config config = mConfig;
		if(config == null) return;
		fillFolderRow(
			ROOT_DOCUMENT_ID,
			cursor.newRow(),
			TrackProviderConsts.FLAG_HAS_SUBDIRS,
			config.modifiedAtMs
		);
	}

	boolean isChildDocument(@NonNull String parentDocumentId, @NonNull String documentId) {
		Config config = mConfig;
		if(config == null) return false;
		boolean parentExists = parentDocumentId.equals("root2") || parseDir(config, parentDocumentId, false) != null;
		boolean documentExists = parseDir(config, documentId, false) != null || isPaddingDocument(config, documentId);
		boolean result = parentExists && documentExists && documentId.startsWith(parentDocumentId + "/");
		if(LOG) Log.w(TAG, "isChildDocument =>" + result +
			" parentDocumentId=" + parentDocumentId + " documentId=" + documentId);
		return result;
	}

	private Bundle handleConfigure(@Nullable Bundle extras) {
		enforceControlCaller();
		if(extras == null) throw new IllegalArgumentException("Missing scanner fixture extras");

		String runId = requireRunId(extras);
		int fanout = extras.getInt("fanout", 4);
		int depth = extras.getInt("depth", 3);
		int deepDepth = extras.getInt("deep_depth", 32);
		int seedDepth = extras.getInt("seed_depth", 4);
		int seedPaddingFiles = extras.getInt("seed_padding_files", 64);
		int queryDelayMs = extras.getInt("query_delay_ms", 0);
		boolean force = extras.getBoolean("force", false);

		synchronized(mLock) {
			Config current = mConfig;
			if(current != null && !current.runId.equals(runId) && !force) {
				throw new IllegalStateException(
					"Scanner fixture is owned by run-id=" + current.runId + ", requested=" + runId
				);
			}

			long modifiedAtMs = nextModifiedAtMs(current);
			Config configured = new Config(
				runId,
				fanout,
				depth,
				deepDepth,
				seedDepth,
				seedPaddingFiles,
				queryDelayMs,
				null,
				modifiedAtMs
			);
			validateConfig(configured);
			persistConfig(configured, modifiedAtMs);
			mRootModifiedAtMs = modifiedAtMs;
			mConfig = configured;
			if(LOG) Log.w(TAG, "Configured " + statusBundle(configured));
			return statusBundle(configured);
		}
	}

	private Bundle handleRemoveSubtree(@Nullable Bundle extras) {
		enforceControlCaller();
		if(extras == null) throw new IllegalArgumentException("Missing scanner fixture extras");

		String runId = requireRunId(extras);
		String documentId = extras.getString("document_id");
		if(TextUtils.isEmpty(documentId)) throw new IllegalArgumentException("Missing document_id");

		synchronized(mLock) {
			Config current = requireOwnedConfig(runId);
			Dir dir = parseDir(current, documentId, true);
			if(dir == null || ROOT_DOCUMENT_ID.equals(documentId)) {
				throw new IllegalArgumentException("Not a removable scanner fixture directory: " + documentId);
			}

			long modifiedAtMs = nextModifiedAtMs(current);
			Config modified = current.withRemovedDocumentId(documentId, modifiedAtMs);
			persistConfig(modified, modifiedAtMs);
			mRootModifiedAtMs = modifiedAtMs;
			mConfig = modified;
			if(LOG) Log.w(TAG, "Removed subtree documentId=" + documentId + " runId=" + runId);
			return statusBundle(modified);
		}
	}

	private Bundle handleReset(@Nullable Bundle extras) {
		enforceControlCaller();
		String runId = extras != null ? extras.getString("run_id") : null;
		boolean force = extras != null && extras.getBoolean("force", false);

		synchronized(mLock) {
			Config current = mConfig;
			if(current == null) return statusBundle(null);
			if(!force && !current.runId.equals(runId)) {
				throw new IllegalStateException(
					"Scanner fixture is owned by run-id=" + current.runId + ", requested=" + runId
				);
			}

			long modifiedAtMs = nextModifiedAtMs(current);
			SharedPreferences.Editor editor = preferences().edit();
			editor.clear();
			editor.putLong(PREF_ROOT_MODIFIED_AT_MS, modifiedAtMs);
			if(!editor.commit()) throw new IllegalStateException("Failed to reset scanner fixture preferences");
			mRootModifiedAtMs = modifiedAtMs;
			mConfig = null;
			if(LOG) Log.w(TAG, "Reset runId=" + current.runId + " force=" + force);
			return statusBundle(null);
		}
	}

	private Bundle handleStatus() {
		enforceControlCaller();
		return statusBundle(mConfig);
	}

	private void enforceControlCaller() {
		if((mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
			throw new SecurityException("Scanner fixture controls require a debug provider build");
		}
		if(Binder.getCallingUid() != Process.SHELL_UID) {
			throw new SecurityException("Scanner fixture controls require the shell UID");
		}
	}

	private @NonNull String requireRunId(@NonNull Bundle extras) {
		String runId = extras.getString("run_id");
		if(TextUtils.isEmpty(runId) || runId.length() > 128 || !runId.matches("[A-Za-z0-9_.-]+")) {
			throw new IllegalArgumentException("Invalid scanner fixture run_id=" + runId);
		}
		return runId;
	}

	private @NonNull Config requireOwnedConfig(@NonNull String runId) {
		Config current = mConfig;
		if(current == null) throw new IllegalStateException("Scanner fixture is not configured");
		if(!current.runId.equals(runId)) {
			throw new IllegalStateException(
				"Scanner fixture is owned by run-id=" + current.runId + ", requested=" + runId
			);
		}
		return current;
	}

	private void loadState(long apkInstallTimeMs) {
		SharedPreferences prefs = preferences();
		mRootModifiedAtMs = Math.max(
			apkInstallTimeMs,
			prefs.getLong(PREF_ROOT_MODIFIED_AT_MS, apkInstallTimeMs)
		);
		if(!prefs.getBoolean(PREF_ENABLED, false)) return;

		try {
			Config configured = new Config(
				Objects.requireNonNull(prefs.getString(PREF_RUN_ID, null)),
				prefs.getInt(PREF_FANOUT, 0),
				prefs.getInt(PREF_DEPTH, 0),
				prefs.getInt(PREF_DEEP_DEPTH, 0),
				prefs.getInt(PREF_SEED_DEPTH, 0),
				prefs.getInt(PREF_SEED_PADDING_FILES, 0),
				prefs.getInt(PREF_QUERY_DELAY_MS, 0),
				prefs.getString(PREF_REMOVED_DOCUMENT_ID, null),
				prefs.getLong(PREF_MODIFIED_AT_MS, 0)
			);
			validateConfig(configured);
			if(configured.removedDocumentId != null && parseDir(configured, configured.removedDocumentId, true) == null) {
				throw new IllegalStateException("Invalid removed scanner fixture directory");
			}
			mRootModifiedAtMs = Math.max(mRootModifiedAtMs, configured.modifiedAtMs);
			mConfig = configured;
			if(LOG) Log.w(TAG, "Loaded " + statusBundle(configured));
		} catch(RuntimeException ex) {
			Log.e(TAG, "Discarding invalid scanner fixture preferences", ex);
			long modifiedAtMs = Math.max(System.currentTimeMillis(), mRootModifiedAtMs + 1000L);
			SharedPreferences.Editor editor = prefs.edit();
			editor.clear();
			editor.putLong(PREF_ROOT_MODIFIED_AT_MS, modifiedAtMs);
			editor.commit();
			mRootModifiedAtMs = modifiedAtMs;
			mConfig = null;
		}
	}

	private SharedPreferences preferences() {
		return mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}

	private void persistConfig(@NonNull Config config, long rootModifiedAtMs) {
		SharedPreferences.Editor editor = preferences().edit()
			.putBoolean(PREF_ENABLED, true)
			.putString(PREF_RUN_ID, config.runId)
			.putInt(PREF_FANOUT, config.fanout)
			.putInt(PREF_DEPTH, config.depth)
			.putInt(PREF_DEEP_DEPTH, config.deepDepth)
			.putInt(PREF_SEED_DEPTH, config.seedDepth)
			.putInt(PREF_SEED_PADDING_FILES, config.seedPaddingFiles)
			.putInt(PREF_QUERY_DELAY_MS, config.queryDelayMs)
			.putLong(PREF_MODIFIED_AT_MS, config.modifiedAtMs)
			.putLong(PREF_ROOT_MODIFIED_AT_MS, rootModifiedAtMs);
		if(config.removedDocumentId == null) {
			editor.remove(PREF_REMOVED_DOCUMENT_ID);
		} else {
			editor.putString(PREF_REMOVED_DOCUMENT_ID, config.removedDocumentId);
		}
		if(!editor.commit()) throw new IllegalStateException("Failed to persist scanner fixture preferences");
	}

	private long nextModifiedAtMs(@Nullable Config current) {
		long previous = mRootModifiedAtMs;
		if(current != null) previous = Math.max(previous, current.modifiedAtMs);
		return Math.max(System.currentTimeMillis(), previous + 1000L);
	}

	private void validateConfig(@NonNull Config config) {
		if(TextUtils.isEmpty(config.runId) || config.runId.length() > 128 || !config.runId.matches("[A-Za-z0-9_.-]+")) {
			throw new IllegalArgumentException("Invalid scanner fixture run-id=" + config.runId);
		}
		if(config.fanout < 1 || config.depth < 1 || config.deepDepth < 1 || config.seedDepth < 1) {
			throw new IllegalArgumentException(
				"Scanner fixture topology values must be positive: fanout=" + config.fanout +
				" depth=" + config.depth + " deepDepth=" + config.deepDepth + " seedDepth=" + config.seedDepth
			);
		}
		if(config.seedPaddingFiles < 0 || config.queryDelayMs < 0 || config.queryDelayMs > MAX_QUERY_DELAY_MS) {
			throw new IllegalArgumentException(
				"Invalid scanner fixture padding/delay: padding=" + config.seedPaddingFiles + " delayMs=" + config.queryDelayMs
			);
		}

		long broadDirectories = broadDirectoryCount(config.fanout, config.depth);
		long totalDirectories = 1L + config.seedDepth + 1L + broadDirectories + config.deepDepth + 1L;
		long paddingFiles = ((long)config.seedDepth + 1L) * config.seedPaddingFiles;
		if(totalDirectories > MAX_DIRECTORIES || paddingFiles > MAX_PADDING_FILES) {
			throw new IllegalArgumentException(
				"Scanner fixture is too large: directories=" + totalDirectories + " paddingFiles=" + paddingFiles
			);
		}
		if(config.modifiedAtMs <= 0) throw new IllegalArgumentException("Invalid scanner fixture modifiedAtMs=" + config.modifiedAtMs);
	}

	private long broadDirectoryCount(int fanout, int depth) {
		long count = 0;
		long levelCount = 1;
		for(int level = 0; level <= depth; level++) {
			count += levelCount;
			if(count > MAX_DIRECTORIES || level == depth) break;
			if(levelCount > MAX_DIRECTORIES / (long)fanout) {
				levelCount = MAX_DIRECTORIES + 1L;
			} else {
				levelCount *= fanout;
			}
		}
		return count;
	}

	private Bundle statusBundle(@Nullable Config config) {
		Bundle result = new Bundle();
		result.putInt("schema", SCHEMA);
		result.putBoolean("enabled", config != null);
		result.putString("document_id", ROOT_DOCUMENT_ID);
		result.putLong("root_modified_at_ms", mRootModifiedAtMs);
		if(config == null) return result;

		long broadDirectories = broadDirectoryCount(config.fanout, config.depth);
		long totalDirectories = 1L + config.seedDepth + 1L + broadDirectories + config.deepDepth + 1L;
		result.putString("run_id", config.runId);
		result.putInt("fanout", config.fanout);
		result.putInt("depth", config.depth);
		result.putInt("deep_depth", config.deepDepth);
		result.putInt("seed_depth", config.seedDepth);
		result.putInt("seed_padding_files", config.seedPaddingFiles);
		result.putInt("query_delay_ms", config.queryDelayMs);
		result.putLong("modified_at_ms", config.modifiedAtMs);
		result.putLong("broad_directories", broadDirectories);
		result.putLong("total_directories", totalDirectories);
		result.putLong("total_padding_files", ((long)config.seedDepth + 1L) * config.seedPaddingFiles);
		if(config.removedDocumentId != null) result.putString("removed_document_id", config.removedDocumentId);
		return result;
	}

	private void addFolder(
		@NonNull Config config,
		@NonNull MatrixCursor cursor,
		@NonNull String documentId,
		boolean hasSubDirs
	) {
		if(documentRemoved(config, documentId)) return;
		fillFolderRow(
			documentId,
			cursor.newRow(),
			hasSubDirs ? TrackProviderConsts.FLAG_HAS_SUBDIRS : TrackProviderConsts.FLAG_NO_SUBDIRS,
			config.modifiedAtMs
		);
	}

	private boolean dirHasSubDirs(@NonNull Config config, @NonNull Dir dir) {
		switch(dir.kind) {
			case DIR_ROOT:
			case DIR_SEED:
				return true;
			case DIR_BROAD:
				return dir.level < config.depth;
			case DIR_DEEP:
				return dir.level < config.deepDepth;
			default:
				throw new AssertionError(dir.kind);
		}
	}

	private @Nullable Dir parseDir(@NonNull Config config, @Nullable String documentId, boolean includeRemoved) {
		if(documentId == null || !isDocumentId(documentId)) return null;
		if(!includeRemoved && documentRemoved(config, documentId)) return null;
		if(documentId.equals(ROOT_DOCUMENT_ID)) return new Dir(DIR_ROOT, 0, 0);

		String seedRoot = ROOT_DOCUMENT_ID + "/seed";
		if(documentId.equals(seedRoot)) return new Dir(DIR_SEED, 0, 0);
		if(documentId.startsWith(seedRoot + "/")) {
			String suffix = documentId.substring(seedRoot.length() + 1);
			String[] segments = suffix.split("/");
			if(segments.length <= config.seedDepth) {
				boolean valid = true;
				for(int level = 0; level < segments.length; level++) {
					if(!segments[level].equals(levelName(level))) {
						valid = false;
						break;
					}
				}
				if(valid) return new Dir(DIR_SEED, segments.length, 0);
			}
		}

		String broadRoot = broadRootDocumentId(config);
		if(documentId.equals(broadRoot)) return new Dir(DIR_BROAD, 0, 0);
		if(documentId.startsWith(broadRoot + "/")) {
			String[] segments = documentId.substring(broadRoot.length() + 1).split("/");
			if(segments.length <= config.depth) {
				int parentIndex = 0;
				boolean valid = true;
				for(int level = 0; level < segments.length; level++) {
					int matchingChild = -1;
					for(int childIndex = 0; childIndex < config.fanout; childIndex++) {
						if(segments[level].equals(broadName(level, parentIndex, childIndex))) {
							matchingChild = childIndex;
							break;
						}
					}
					if(matchingChild < 0) {
						valid = false;
						break;
					}
					parentIndex = parentIndex * config.fanout + matchingChild;
				}
				if(valid) return new Dir(DIR_BROAD, segments.length, parentIndex);
			}
		}

		String deepRoot = deepRootDocumentId(config);
		if(documentId.equals(deepRoot)) return new Dir(DIR_DEEP, 0, 0);
		if(documentId.startsWith(deepRoot + "/")) {
			String[] segments = documentId.substring(deepRoot.length() + 1).split("/");
			if(segments.length <= config.deepDepth) {
				boolean valid = true;
				for(int level = 0; level < segments.length; level++) {
					if(!segments[level].equals(levelName(level))) {
						valid = false;
						break;
					}
				}
				if(valid) return new Dir(DIR_DEEP, segments.length, 0);
			}
		}
		return null;
	}

	private boolean isPaddingDocument(@NonNull Config config, @NonNull String documentId) {
		if(documentRemoved(config, documentId)) return false;
		int separator = documentId.lastIndexOf('/');
		if(separator <= 0) return false;
		Dir parent = parseDir(config, documentId.substring(0, separator), false);
		if(parent == null || parent.kind != DIR_SEED) return false;

		String name = documentId.substring(separator + 1);
		String prefix = String.format(Locale.ROOT, "padding-%02d-", parent.level);
		String suffix = ".scannerfixture";
		if(!name.startsWith(prefix) || !name.endsWith(suffix)) return false;
		try {
			int index = Integer.parseInt(name.substring(prefix.length(), name.length() - suffix.length()), 10);
			return index >= 0 && index < config.seedPaddingFiles && name.equals(paddingName(parent.level, index));
		} catch(NumberFormatException ex) {
			return false;
		}
	}

	private boolean documentRemoved(@NonNull Config config, @NonNull String documentId) {
		String removed = config.removedDocumentId;
		return removed != null && (documentId.equals(removed) || documentId.startsWith(removed + "/"));
	}

	private String seedDocumentId(@NonNull Config config, int level) {
		StringBuilder result = new StringBuilder(ROOT_DOCUMENT_ID).append("/seed");
		for(int index = 0; index < level; index++) result.append('/').append(levelName(index));
		return result.toString();
	}

	private String broadRootDocumentId(@NonNull Config config) {
		return seedDocumentId(config, config.seedDepth) + "/broad";
	}

	private String deepRootDocumentId(@NonNull Config config) {
		return seedDocumentId(config, config.seedDepth) + "/deep";
	}

	private String levelName(int level) {
		return String.format(Locale.ROOT, "level-%02d", level);
	}

	private String broadName(int level, int parentIndex, int childIndex) {
		return String.format(Locale.ROOT, "n%02d_%04d_%02d", level, parentIndex, childIndex);
	}

	private String paddingName(int level, int index) {
		return String.format(Locale.ROOT, "padding-%02d-%04d.scannerfixture", level, index);
	}

	private void fillFolderRow(
		@NonNull String documentId,
		@NonNull MatrixCursor.RowBuilder row,
		int flags,
		long modifiedAtMs
	) {
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
		row.add(Document.COLUMN_DISPLAY_NAME, getShortDirName(documentId));
		row.add(Document.COLUMN_LAST_MODIFIED, modifiedAtMs);
		row.add(Document.COLUMN_FLAGS, 0);
		row.add(Document.COLUMN_SIZE, 0);
		row.add(TrackProviderConsts.COLUMN_FLAGS, flags);
	}

	private void fillPaddingRow(
		@NonNull String documentId,
		@NonNull MatrixCursor.RowBuilder row,
		long modifiedAtMs
	) {
		row.add(Document.COLUMN_DOCUMENT_ID, documentId);
		row.add(Document.COLUMN_MIME_TYPE, "application/octet-stream");
		row.add(Document.COLUMN_DISPLAY_NAME, getShortName(documentId));
		row.add(Document.COLUMN_LAST_MODIFIED, modifiedAtMs);
		row.add(Document.COLUMN_FLAGS, 0);
		row.add(Document.COLUMN_SIZE, 0);
	}

	private static String[] resolveDocumentProjection(@Nullable String[] projection) {
		return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
	}

	@SuppressWarnings("null")
	private static @NonNull String getShortDirName(@NonNull String path) {
		int separator = path.lastIndexOf('/');
		if(separator == -1) return path;
		int end = path.length();
		if(separator == end - 1) {
			end--;
			separator = path.lastIndexOf('/', end - 1);
		}
		return path.substring(separator + 1, end);
	}

	@SuppressWarnings("null")
	private static @NonNull String getShortName(@NonNull String path) {
		int separator = path.lastIndexOf('/');
		if(separator == -1) separator = path.lastIndexOf('\\');
		if(separator == -1) return path;
		return path.substring(separator + 1);
	}
}
