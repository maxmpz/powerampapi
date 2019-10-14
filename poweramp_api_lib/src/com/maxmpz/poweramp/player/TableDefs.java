/*
Copyright (C) 2011-2019 Maksim Petrov

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

import org.eclipse.jdt.annotation.NonNull;
import android.provider.BaseColumns;

public interface TableDefs {
	/**
	 * Alias used for category. Useful when query is actually a multi table join
	 */
	public static final @NonNull String CATEGORY_ALIAS = "cat";

	/** 
	 * Id used for all "unknown" categories. Also see {@link PowerampAPI.NO_ID}
	 */
	public static final long UNKNOWN_ID = 1000L;

	/**
	 * Alias used for category aliased table _id
	 */
	public static final @NonNull String CATEGORY_ALIAS_ID = CATEGORY_ALIAS + "._id";

	/**
	 * Tracks
	 */
	public interface Files {
		public static final @NonNull String TABLE = "folder_files";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Short filename<br>
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";

		/**
		 * Track number (extracted from filename)<br>
		 * INTEGER
		 */
		public static final @NonNull String TRACK_NUMBER = "track_number";

		/**
		 * Track name without number. For streams - this is name of stream, if available<br>
		 * TEXT
		 */
		public static final @NonNull String NAME_WITHOUT_NUMBER = "name_without_number";

		/**
		 * One of the TAG_* constants<br>
		 * INTEGER
		 */
		public static final @NonNull String TAG_STATUS = "tag_status";

		/**
		 * Track # tag<br>
		 * INTEGER
		 */
		public static final @NonNull String TRACK_TAG = "track_tag";

		/**
		 * Parent folder id<br>
		 * INTEGER
		 */
		public static final @NonNull String FOLDER_ID = "folder_id";

		/**
		 * Title tag<br>
		 * NOTE: important to have it w/o table for headers-enabled compound selects<br>
		 * TEXT
		 */
		public static final @NonNull String TITLE_TAG = "title_tag";

		/**
		 * NOTE: non-null for streams only<br>
		 * TEXT
		 */
		public static final @NonNull String ALBUM_TAG = "album_tag"; 

		/**
		 * NOTE: non-null for streams only<br>
		 * TEXT
		 */
		public static final @NonNull String ARTIST_TAG = "artist_tag"; 

		/**
		 * Duration in milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";

		/**
		 * Time of update in epoch seconds<br>
		 * INTEGER
		 */
		public static final @NonNull String UPDATED_AT = TABLE + ".updated_at";

		/**
		 * One of the file types - {@link PowerampAPI.Track.FileType}<br>
		 */
		public static final @NonNull String FILE_TYPE = "file_type";

		/**
		 * Milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String PLAYED_AT = TABLE + ".played_at";

		/**
		 * Seconds<br>
		 * This is file last modified time actually, for most Android variants<br>
		 * INTEGER
		 */
		public static final @NonNull String FILE_CREATED_AT = "file_created_at";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";

		/**
		 * Full path. Works only if the query is joined with the folders, otherwise this will fail<br>
		 * TEXT
		 */
		public static final @NonNull String FULL_PATH = "COALESCE(" + Folders.PATH + "||" + NAME + "," + NAME + ")";

		/**
		 * INTEGER
		 */
		public static final @NonNull String RATING = "rating";

		/**
		 * INTEGER
		 */
		public static final @NonNull String PLAYED_TIMES = TABLE + ".played_times";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_ID = TABLE + ".album_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ARTIST_ID = TABLE + ".artist_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_ARTIST_ID = TABLE + ".album_artist_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String COMPOSER_ID = TABLE + ".composer_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String YEAR = "year";

		/**
		 * Cue offset milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String OFFSET_MS = "offset_ms";

		/**
		 * If non-null - this is cue "source" (big uncut image) file with that given virtual folder id<br>
		 * NOTE: enforces 1-1 between source files and cues. No multiple cues per single image thus possible<br>
		 * INTEGER
		 */
		public static final @NonNull String CUE_FOLDER_ID = "cue_folder_id";

		/**
		 * First seen time<br>
		 * Seconds<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Wave scan data<br>
		 * byte[] blob, nullable
		 */
		public static final @NonNull String WAVE = "wave";
		
		/**
		 * TEXT
		 */
		public static final @NonNull String META = TABLE + ".meta";
		
		/**
		 * Last played position in milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String LAST_POS = "last_pos";

		/**
		 * INTEGER
		 */
		public static final @NonNull String SHUFFLE_ORDER = TABLE + ".shuffle_order";

		/**
		 * tag_status
		 */
		public static final int TAG_NOT_SCANNED = 0;

		/**
		 * tag_status
		 */
		public static final int TAG_SCANNED = 1;
	}

	/** Contains the single track entry when/if some path is requested to be played and that path is not in Poweramp Music Folders/Library */
	public interface RawFiles extends Files {
		public static final @NonNull String TABLE = "raw_files";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Short filename<br>
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";

		/**
		 * Title tag<br>
		 * TEXT
		 */
		public static final @NonNull String TITLE_TAG = TABLE + ".title_tag";

		/**
		 * Seconds<br>
		 * INTEGER
		 */
		public static final @NonNull String UPDATED_AT = TABLE + ".updated_at";

		/**
		 * INTEGER
		 */
		public static final @NonNull String PLAYED_AT = TABLE + ".played_at";

		/**
		 * Full path. Works only if the query is joined with the folders<br>
		 * TEXT
		 */
		public static final @NonNull String FULL_PATH = Folders.PATH + "||" + NAME;

		/**
		 * INTEGER
		 */
		public static final @NonNull String PLAYED_TIMES = TABLE + ".played_times";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_ID = TABLE + ".album_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ARTIST_ID = TABLE + ".artist_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_ARTIST_ID = TABLE + ".album_artist_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String COMPOSER_ID = TABLE + ".composer_id";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * String
		 */
		public static final @NonNull String META = TABLE + ".meta";

		/**
		 * String
		 */
		public static final @NonNull String ALBUM_TAG = TABLE + ".album_tag";

		/**
		 * String
		 */
		public static final @NonNull String ARTIST_TAG = TABLE + ".artist_tag";
	}

	/** All known Poweramp folders */
	public interface Folders {
		public static final @NonNull String TABLE = "folders";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Name of the folder. Can be long for roots (e.g. can include storage description)<br>
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";

		/**
		 * (Always) short name of the folder<br>
		 * Since 828<br>
		 * TEXT
		 */
		public static final @NonNull String SHORT_NAME = TABLE + ".short_name";

		/**
		 * Short path of the parent folder<br>
		 * TEXT
		 */
		public static final @NonNull String PARENT_NAME = TABLE + ".parent_name";

		/**
		 * Parent folder label (which can be much longer than just PARENT_NAME, e.g. include storage description) to display in the UI<br>
		 * TEXT
		 */
		public static final @NonNull String PARENT_LABEL = TABLE + ".parent_label";

		/**
		 * Full path of the folder<br>
		 * NOTE: avoid TABLE name here to allow using field in raw_files. "path" is (almost) unique column, also used in playlists<br>
		 * TEXT
		 */
		public static final @NonNull String PATH = "path";

		/**
		 * This is the same as path for usual folders, but for cue virtual folders, this is path + name<br>
		 * Used for proper folders/subfolders sorting<br>
		 * TEXT
		 */
		public static final @NonNull String SORT_PATH = "sort_path";

		/**
		 * Folder album art/thumb image (short name)<br>
		 * TEXT
		 */
		public static final @NonNull String THUMB = "thumb";

		/**
		 * INTEGER
		 */
		public static final @NonNull String DIR_MODIFIED_AT = TABLE + ".dir_modified_at";

		/**
		 * Seconds<br>
		 * INTEGER
		 */
		public static final @NonNull String UPDATED_AT = TABLE + ".updated_at";

		/**
		 * Id of the parent folder or 0 for "root" folders<br>
		 * INTEGER
		 */
		public static final @NonNull String PARENT_ID = TABLE + ".parent_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String IS_CUE = TABLE + ".is_cue";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Number of direct child subfolders<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_SUBFOLDERS = TABLE + ".num_subfolders";

		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Number of tracks in the whole folder hierarchy, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String HIER_NUM_FILES = TABLE + ".hier_num_files";

		/**
		 * Number of tracks in the whole folder hierarchy, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String HIER_NUM_ALL_FILES = TABLE + ".hier_num_all_files";

		/**
		 * Duration in milliseconds for the tracks inside this folder only, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds for the tracks inside this folder only, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration in milliseconds for the whole hierarchy inside this folder, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String HIER_DURATION = TABLE + ".hier_duration";
		
		/**
		 * Duration in milliseconds for the whole hierarchy inside this folder, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String HIER_DURATION_ALL = TABLE + ".hier_duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";

		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";

		/**
		 * Hierarchy duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String HIER_DUR_META = TABLE + ".hier_dur_meta";

		/**
		 * Hierarchy duration meta including cues<br> 
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String HIER_DUR_ALL_META = TABLE + ".hier_dur_all_meta";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";

		/**
		 * INTEGER (boolean)
		 */
		public static final @NonNull String KEEP_LIST_POS = TABLE + ".keep_list_pos"; // Sync with RestLibraryListMemorizable

		/**
		 * INTEGER (boolean)
		 */
		public static final @NonNull String KEEP_TRACK_POS = TABLE + ".keep_track_pos"; // Sync with RestLibraryListMemorizable
		
		/**
		 * Calculated subquery column which retrieves parent name<br>
		 * TEXT
		 */
		public static final @NonNull String PARENT_NAME_SUBQUERY = "(SELECT name FROM folders AS f2 WHERE f2._id=folders.parent_id) AS parent_name_subquery";
		
		/**
		 * Calculated subquery column which retrieves short parent name<br>
		 * TEXT 
		 */
		public static final @NonNull String PARENT_SHORT_NAME_SUBQUERY = "(SELECT short_name FROM folders AS f2 WHERE f2._id=folders.parent_id) AS parent_short_name_subquery";
	}


	public interface Albums {
		public static final @NonNull String TABLE = "albums";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * NOTE: important to have it w/o table for headers-enabled compound selects<br>
		 * TEXT
		 */
		public static final @NonNull String ALBUM = "album";

		/**
		 * NOTE: important to have it w/o table for headers-enabled compound selects<br>
		 * TEXT
		 */
		public static final @NonNull String ALBUM_SORT = "album_sort";

		/**
		 * NOTE: this is NULL for Unknown album, so not all joins are possible with just albums + album_artists (use folder_files for joins)<br>
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_ARTIST_ID = TABLE + ".album_artist_id";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTRGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";
		
		/**
		 * The guessed album year<br>
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_YEAR = "album_year";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";

		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
	}


	public interface Artists {
		public static final @NonNull String TABLE = "artists";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * TEXT
		 */
		public static final @NonNull String ARTIST = "artist";

		/**
		 * TEXT
		 */
		public static final @NonNull String ARTIST_SORT = "artist_sort";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";

		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
	}


	/** This is similar to Artists, but uses Album Artist tag, where available */
	public interface AlbumArtists {
		public static final @NonNull String TABLE = "album_artists";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * TEXT
		 */
		public static final @NonNull String ALBUM_ARTIST = "album_artist";

		/**
		 * TEXT
		 */
		public static final @NonNull String ALBUM_ARTIST_SORT = "album_artist_sort";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";

		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
	}


	/**
	 * Album => artist 1:1 binding table<br>
	 * Used for Albums by Artist category, where can be multiple same Album repeated per each Artist
	 */
	public interface AlbumsByArtist {
		public static final @NonNull String TABLE = "artist_albums";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ARTIST_ID = TABLE + ".artist_id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String ALBUM_ID = TABLE + ".album_id";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";
		
		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
	}


	public interface Composers {
		public static final @NonNull String TABLE = "composers";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * TEXT
		 */
		public static final @NonNull String COMPOSER = "composer";

		/**
		 * INTEGER
		 */
		public static final @NonNull String COMPOSER_SORT = "composer_sort";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";

		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
	}

	public interface Genres {
		public static final @NonNull String TABLE = "genres";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * TEXT
		 */
		public static final @NonNull String GENRE = "genre";

		/**
		 * First seen time<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Number of tracks in this category, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of tracks in this category, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";

		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 829<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";
	}


	public interface GenreEntries {
		public static final @NonNull String TABLE = "genre_entries";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Actual id of the file in folder_files table<br>
		 * INTEGER
		 */
		public static final @NonNull String FOLDER_FILE_ID = "folder_file_id";

		/**
		 * Genre id<br>
		 * INTEGER
		 */
		public static final @NonNull String GENRE_ID = "genre_id";
	}


	public interface PlaylistEntries {
		public static final @NonNull String TABLE = "playlist_entries";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Actual id of the file in folder_files table<br>
		 * INTEGER
		 */
		public static final @NonNull String FOLDER_FILE_ID = "folder_file_id";

		/**
		 * Folder Playlist id<br>
		 * INTEGER
		 */
		public static final @NonNull String PLAYLIST_ID = "playlist_id";

		/**
		 * Sort order<br>
		 * INTEGER
		 */
		public static final @NonNull String SORT = "sort";
		
		/**
		 * Filename or the stream uri<br>
		 * Since 842<br>
		 * TEXT
		 */
		public static final @NonNull String FILE_NAME = "file_name";
		
		/**
		 * Parent folder path or NULL for streams<br>
		 * Since 842<br>
		 * TEXT
		 */
		public static final @NonNull String FOLDER_PATH = "folder_path";
		
		/**
		 * Cue offset for .cue tracks<br>
		 * Since 842<br>
		 * INTEGER
		 */
		public static final @NonNull String CUE_OFFSET_MS = TABLE + ".cue_offset_ms";

		/**
		 * INTEGER
		 */
		public static final @NonNull String PLAYED_AT = TABLE + ".played_at";
	}


	public interface Playlists {
		public static final @NonNull String TABLE = "playlists";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Name of the playlist<br>
		 * TEXT
		 */
		public static final @NonNull String PLAYLIST = TABLE + ".playlist";

		/**
		 * Updated to match file based playlist. Also updated on entry insert/reorder/deletion - for all playlists<br>
		 * INTEGER
		 */
		public static final @NonNull String MTIME = TABLE + ".mtime";

		/**
		 * INTEGER
		 */
		public static final @NonNull String PATH = TABLE + ".playlist_path";

		/**
		 * Seconds<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * Seconds<br>
		 * INTEGER
		 */
		public static final @NonNull String UPDATED_AT = TABLE + ".updated_at";

		/**
		 * Number of playlist entries<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Number of playlist entries. Since 824 - same as num_files<br>
		 * Poweramp can insert CUE images into playlists if appropriate option enabled, or it skips them completely. In anycase, playlist should show # of all possible entries in it,
		 * without filtering for CUE images<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 796<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";
		
		/**
		 * INTEGER (boolean)
		 */
		public static final @NonNull String KEEP_LIST_POS = TABLE + ".keep_list_pos"; // Sync with RestLibraryListMemorizable

		/**
		 * INTEGER (boolean)
		 */
		public static final @NonNull String KEEP_TRACK_POS = TABLE + ".keep_track_pos"; // Sync with RestLibraryListMemorizable
		
		/**
		 * Duration in milliseconds<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 826<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * Since 826<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";

		/**
		 * Calculated column
		 * INTEGER (boolean)
		 */
		public static final @NonNull String IS_FILE = TABLE + ".playlist_path IS NOT NULL AS _is_file";
	}



	public class Queue {
		public static final @NonNull String TABLE = "queue";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Folder file id<br>
		 * INTEGER
		 */
		public static final @NonNull String FOLDER_FILE_ID = TABLE + ".folder_file_id";

		/**
		 * Milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";

		/**
		 * INTEGER
		 */
		public static final @NonNull String SORT = TABLE + ".sort";
		
		public static final @NonNull String CALC_PLAYED = "folder_files.played_at >= queue.created_at"; // If played at is the same as queue entry time, consider it played already 
		public static final @NonNull String CALC_UNPLAYED = "folder_files.played_at < queue.created_at";
	}

	public class ShuffleSessionIds {
		public static final @NonNull String TABLE = "shuffle_session_ids";

		public static final @NonNull String _ID = TABLE + "._id";
	}


	public class EqPresets {
		public static final @NonNull String TABLE = "eq_presets";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Predefined preset number (see res/values/arrays/eq_preset_labels) or NULL for custom preset<br>
		 * INTEGER
		 */
		public static final @NonNull String PRESET = "preset";

		/**
		 * Eq preset string<br>
		 * TEXT 
		 */
		public static final @NonNull String _DATA = TABLE + "._data";

		/**
		 * Custom preset name or null for predefined preset<br>
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";

		/**
		 * 1 if preset is bound to speaker, 0 otherwise<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_SPEAKER = "bind_to_speaker";

		/**
		 * 1 if preset is bound to wired headset, 0 otherwise<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_WIRED = "bind_to_wired";

		/**
		 * 1 if preset is bound to bluetooth audio output, 0 otherwise<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_BT = "bind_to_bt";

		/**
		 * 1 if preset is bound to USB audio output, 0 otherwise<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_USB = "bind_to_usb";

		/**
		 * 1 if preset is bound to other audio outputs, 0 otherwise<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_OTHER = "bind_to_other";

		/**
		 * 1 if preset is bound to chromecast output, 0 otherwise<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_CHROMECAST = "bind_to_cc";
	}

	public static final class EqPresetSongs implements BaseColumns {
		public static final @NonNull String TABLE = "eq_preset_songs";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Either folder_file_id<br>
		 * INTEGER 
		 */
		public static final @NonNull String FILE_ID = TABLE + ".file_id";

		/**
		 * Eq preset id<br>
		 * INTEGER
		 */
		public static final @NonNull String PRESET_ID = "preset_id";
	}

	public class ReverbPresets {
		public static final @NonNull String TABLE = "reverb_presets";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * TEXT
		 */
		public static final @NonNull String _DATA = TABLE + "._data";

		/**
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";
	}

	/** Since 841 */
	public class PrefSearch {
		public static final @NonNull String TABLE = "pref_search";
		public static final @NonNull String _ID = TABLE + "._id";
		public static final @NonNull String BREADCRUMB = "breadcrumb";
		public static final @NonNull String PREF_URI = "pref_uri";
		public static final @NonNull String PREF_KEY = "pref_key";
		public static final @NonNull String ICON = "icon";
	}

	/** Since 841 */
	public class PrefSearchFts {
		public static final @NonNull String TABLE = "pref_search_fts";
		public static final @NonNull String DOCID = "docid";
		public static final @NonNull String TITLE = "title";
		public static final @NonNull String SUMMARY = "summary";
	}
}
