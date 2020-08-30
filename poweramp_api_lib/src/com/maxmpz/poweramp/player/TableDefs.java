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

import org.eclipse.jdt.annotation.NonNull;

public interface TableDefs {
	/**
	 * Alias used for category. Useful when query is actually a multi table join
	 */
	public static final @NonNull String CATEGORY_ALIAS = "cat";

	/** 
	 * Id used for all "unknown" categories. Also see {@link PowerampAPI#NO_ID}
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
		/** 
		 * Special value for {@link #TRACK_NUMBER} - means no valid track number exists for given track 
		 */
		public static final int INVALID_TRACK_NUMBER = 10000;
		
		public static final @NonNull String TABLE = "folder_files";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Short filename<br>
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";

		/**
		 * Track number extracted from tag or filename. May include disc number (if >= 2) as thousands (2001, 2002, etc.). Can be {@link #INVALID_TRACK_NUMBER}.<br>
		 * Used for sorting<br>
		 * INTEGER
		 */
		public static final @NonNull String TRACK_NUMBER = "track_number";

		/**
		 * Track number for display purposes (since 858), prior 858 just track number from track tags. 0 or NULL if no track tag exists.<br> 
		 * Never includes disc number (since 858)<br>
		 * INTEGER
		 */
		public static final @NonNull String TRACK_TAG = "track_tag";
		
		/**
		 * Track disc or 0 if no such tag exists<br>
		 * INTEGER 
		 * @since 859<br>
		 */
		public static final @NonNull String DISC = "disc";

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
		public static final @NonNull String YEAR = TABLE + ".year";

		/**
		 * Cue offset milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String OFFSET_MS = TABLE + ".offset_ms";

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
		 * 1 if this item (most probably stream track) was manually added and shouldn't be removed by rescans<br>
		 * INTEGER (boolean)
		 * @since 857<br>
		 */
		public static final @NonNull String USER_ADDED = TABLE + ".user_added";
		
		/**
		 * Optional http(s) URL pointing to the target track. If exists, this will be preferred over path (== folder.path + file_name.name)<br>
		 * Can be {@link TrackProviderConsts#DYNAMIC_URL}<br> 
		 * TEXT
		 */
		public static final @NonNull String URL = TABLE + ".url";
		
		/**
		 * Optional full path to the file. This file_path always overrides parent folder path + filename. Used for the cases when actual file path
		 * can't be derived from the parent folder due to non-hierarchical or opaque paths, e.g. for tracks providers.<br>
		 * Path is in Poweramp internal path format<br>
		 * TEXT
		 * @since 862
		 */
		public static final @NonNull String FILE_PATH = TABLE + ".file_path";
		
		/**
		 * Optional bitrate of the file. Currently set only for the provider tracks if provided in the appropiate metadata<br>
		 * INTEGER
		 * @since 862
		 */
		public static final @NonNull String BIT_RATE = TABLE + ".bit_rate";

		/**
		 * Full path. Works only if the query is joined with the folders, otherwise this may fail<br>
		 * TEXT
		 */
		public static final @NonNull String FULL_PATH = "COALESCE(" + FILE_PATH + ","+ Folders.PATH + "||" + NAME + "," + NAME + ")";
		
		/**
		 * Alternative track number. Currently applied only in Folders/Folders Hierarchy files for track number sorting. May differ for provider tracks, equals track_number for
		 * all other tracks<br>
		 * INTEGER
		 */
		public static final @NonNull String TRACK_NUMBER_ALT = "track_number_alt";


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
		 * Display (label) name of the folder. Can be long for roots (e.g. can include storage description).<br>
		 * May or may not match actual filesystem folder name<br> 
		 * TEXT
		 */
		public static final @NonNull String NAME = TABLE + ".name";

		/**
		 * (Always) short name of the folder. Always matches actual filesystem folder name<br>
		 * TEXT
		 * @since 828
		 */
		public static final @NonNull String SHORT_NAME = TABLE + ".short_name";

		/**
		 * Short path of the parent folder. Always matches parent short_name which is parent actual filesystem folder name<br>
		 * TEXT
		 */
		public static final @NonNull String PARENT_NAME = TABLE + ".parent_name";

		/**
		 * Parent folder display label (which can be much longer than just PARENT_NAME, e.g. include storage description) to display in the UI.<br>
		 * Corresponds to parent name<br>
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
		 * This is the same as path for usual folders, but for the cue virtual folders, this is path + name<br>
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
		 * INTEGER
		 * @since 829
		 */
		public static final @NonNull String HIER_NUM_FILES = TABLE + ".hier_num_files";

		/**
		 * Number of tracks in the whole folder hierarchy, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829
		 * @deprecated since 864. {@link #HIER_NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String HIER_NUM_ALL_FILES = TABLE + ".hier_num_all_files";

		/**
		 * Duration in milliseconds for the tracks inside this folder only, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds for the tracks inside this folder only, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864. {@link #DURATION} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration in milliseconds for the whole hierarchy inside this folder, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String HIER_DURATION = TABLE + ".hier_duration";
		
		/**
		 * Duration in milliseconds for the whole hierarchy inside this folder, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String HIER_DURATION_ALL = TABLE + ".hier_duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";

		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864. {@link #DUR_META} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";

		/**
		 * Hierarchy duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String HIER_DUR_META = TABLE + ".hier_dur_meta";

		/**
		 * Hierarchy duration meta including cues<br> 
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String HIER_DUR_ALL_META = TABLE + ".hier_dur_all_meta";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";

		/**
		 * If 1 (true), this folder restores last played track<br>
		 * INTEGER (boolean)
		 * @since 821<br>
		 */
		public static final @NonNull String KEEP_LIST_POS = TABLE + ".keep_list_pos"; // Sync with RestLibraryListMemorizable

		/**
		 * If 1 (true), this folder restores last played track position<br>
		 * INTEGER (boolean)
		 * @since 821<br>
		 */
		public static final @NonNull String KEEP_TRACK_POS = TABLE + ".keep_track_pos"; // Sync with RestLibraryListMemorizable
		
		/**
		 * Non-null for provider folders, where provider wants to control default sorting order in Folders Hierarchy<br>
		 * INTEGER<br>
		 * @since 869
		 */
		public static final @NonNull String SORT_ORDER = "sort_order";
		
		/**
		 * Calculated subquery column which retrieves parent name<br>
		 * TEXT
		 * @deprecated use {@link #PARENT_LABEL}
		 */
		@Deprecated
		public static final @NonNull String PARENT_NAME_SUBQUERY = "(SELECT name FROM folders AS f2 WHERE f2._id=folders.parent_id) AS parent_name_subquery";
		
		/**
		 * Calculated subquery column which retrieves short parent name<br>
		 * TEXT
		 * @deprecated use {@link #PARENT_NAME} 
		 */
		@Deprecated
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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
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
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";

		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";
		
		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";

		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @since 829<br>
		 * @deprecated since 864
		 */
		@Deprecated
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

	
	/** 
	 * @since 856
	 */
	public interface Years {
		public static final @NonNull String TABLE = "years";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * INTEGER
		 */
		public static final @NonNull String YEAR = TABLE + ".year";

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
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
		public static final @NonNull String NUM_ALL_FILES = TABLE + ".num_all_files";

		/**
		 * Duration in milliseconds, excluding cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
		
		/**
		 * Duration meta including cues<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DUR_ALL_META = TABLE + ".dur_all_meta";
		
		/**
		 * Duration in milliseconds, including cue source images<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 * @deprecated since 864
		 */
		@Deprecated
		public static final @NonNull String DURATION_ALL = TABLE + ".duration_all";

		/**
		 * Bitwise flag<br>
		 * INTEGER
		 */
		public static final @NonNull String AA_STATUS = TABLE + ".aa_status";
	}


	/** 
	 * Used for specific dynamic subcategories, such as Artist Albums, where data subset is dynamically generated, thus, no table for stats otherwise exist<br>
	 * TYPE + REF_ID is an unique index
	 * @since 863
	 */
	public interface CatStats {
		public static final @NonNull String TABLE = "cat_stats";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Matches category numeric type ({@link PowerampAPI.Cats})<br>
		 * INTEGER
		 */
		public static final @NonNull String TYPE = TABLE + ".type";

		/**
		 * First referenced target id, e.g. genre _id<br>
		 * INTEGER
		 */
		public static final @NonNull String REF_ID = TABLE + ".ref_id";

		/**
		 * Second referenced target id, e.g. album _id<br>
		 * INTEGER
		 */
		public static final @NonNull String REF_ID2 = TABLE + ".ref_id2";

		/**
		 * Number of tracks in this category<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String NUM_FILES = TABLE + ".num_files";

		/**
		 * Duration in milliseconds<br>
		 * Dynamically recalculated on rescans<br>
		 * INTEGER
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 */
		public static final @NonNull String DUR_META = TABLE + ".dur_meta";
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
		 * TEXT
		 * @since 842<br>
		 */
		public static final @NonNull String FILE_NAME = "file_name";
		
		/**
		 * Parent folder path or NULL for streams<br>
		 * TEXT
		 * @since 842<br>
		 */
		public static final @NonNull String FOLDER_PATH = "folder_path";
		
		/**
		 * Cue offset for .cue tracks<br>
		 * INTEGER
		 * @since 842<br>
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
		 * INTEGER
		 * @since 796<br>
		 * @deprecated since 864. {@link #NUM_FILES} is now dynamically updated depending on "show cue images" preference
		 */
		@Deprecated
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
		 * INTEGER
		 * @since 826<br>
		 */
		public static final @NonNull String DURATION = TABLE + ".duration";
		
		/**
		 * Duration meta<br>
		 * Dynamically recalculated on rescans<br>
		 * TEXT
		 * @since 826<br>
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

	/**
	 * @since 877
	 */
	public class Bookmarks {
		public static final @NonNull String TABLE = "bookmarks";

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
		public static final @NonNull String OFFSET_MS = TABLE + ".offset_ms";

		/**
		 * INTEGER
		 */
		public static final @NonNull String SORT = TABLE + ".sort";

		/**
		 * TEXT
		 */
		public static final @NonNull String META = TABLE + ".meta";

		/**
		 * Milliseconds<br>
		 * INTEGER
		 */
		public static final @NonNull String CREATED_AT = TABLE + ".created_at";
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
		
		/**
		 * Virtual field, used for insert/update contentValues.<br>
		 * If set to 1 preset is bound to track specified by {@link #BOUND_TRACK_ID}, if set to 0, preset is unbound from that track<br>
		 * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * INTEGER (boolean)<br>
		 * @since 856<br>
		 */
		public static final @NonNull String BIND_TO_TRACK = "__bind_to_track";

		/**
		 * Virtual field, used for insert/update contentValues.<br>
		 * Should be set to track id which should be bound/unbound with {@link #BIND_TO_TRACK}
		 * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * @since 856<br>  
		 * INTEGER
		 */
		public static final @NonNull String BOUND_TRACK_ID = "__bound_track_id";

		/**
		 * Virtual field, used for insert/update contentValues.<br>
		 * If set to 1 preset is bound to all category tracks specified by {@link #BOUND_CAT_URI}, if set to 0, preset is unbound from these tracks<br>
		 * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * @since 856<br>
		 * INTEGER (boolean)
		 */
		public static final @NonNull String BIND_TO_CAT = "__bind_to_cat";

		/**
		 * Virtual field, used for insert/update contentValues.<br>
		 * Should be set to category uri which should be bound/unbound with {@link #BIND_TO_CAT}<br>
		 * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * @since 856<br>
		 * TEXT  
		 */
		public static final @NonNull String BOUND_CAT_URI = "__bound_cat_uri";

		/**
		 * Virtual field, used for insert/update contentValues. If this is set, no other track bind/unbind assignments will be executed and preset is unbound from all tracks<br>
		 * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * INTEGER (boolean) 
		 */
		public static final @NonNull String UNBIND_FROM_ALL_TRACK_IDS = "__unbind_from_all_track_ids";
		
		/**
		 * Virtual field, used for insert/update contentValues. If this is set, no other device bind/unbind assignments will be executed and preset is unbound from all devices<br>
		 * @since 856<br>
		 * INTEGER (boolean) 
		 */
		public static final @NonNull String UNBIND_FROM_ALL_DEVICES = "__unbind_from_all_devices";
	
		/**
		 * Virtual field, used for insert/update contentValues. <br>
		 * If this is set to value > 0, * content values named {@link #BIND_TO_DEVICE_PREFIX}#, {@link #DEVICE_PREFIX}#, {@link #DEVICE_ADDRESS_PREFIX}#, {@link #DEVICE_NAME_PREFIX}#,
		 * will be checked and if all 4 are set to appropriate valid non-empty values, preset will be bound/unbound to that device according to the {@link #BIND_TO_DEVICE_PREFIX}#.<br>
		 * # is number [0, NUM_BIND_DEVICES)<br>
		 * @since 856
		 * INTEGER 
		 */
		public static final @NonNull String NUM_BIND_DEVICES = "__num_bind_devices";

		/**
		 * Prefix for virtual fields, used for insert/update contentValues.<br>
		 * Content values named BIND_TO_DEVICE_PREFIX, {@link #DEVICE_PREFIX}#, {@link #DEVICE_ADDRESS_PREFIX}#, {@link #DEVICE_NAME_PREFIX}#,
		 * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
		 * # is number [0, NUM_BIND_DEVICES)<br>
		 * @since 856<br>
		 * INTEGER (boolean) 
		 */
		public static final @NonNull String BIND_TO_DEVICE_PREFIX = "__bind_to_device_";

		/**
		 * Prefix for virtual fields, used for insert/update contentValues.<br>
		 * Sets device type for device assignment. See {@link RouterConsts} DEVICE_* constants<br>
		 * Content values named {@link #BIND_TO_DEVICE_PREFIX}#, DEVICE_PREFIX, {@link #DEVICE_ADDRESS_PREFIX}#, {@link #DEVICE_NAME_PREFIX}#,
		 * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
		 * # is number [0, NUM_BIND_DEVICES)<br>
		 * @since 856<br>
		 * INTEGER 
		 */
		public static final @NonNull String DEVICE_PREFIX = "__device_";
		
		/**
		 * Prefix for virtual fields, used for insert/update contentValues. Sets device address for given device assignment. Device address may match device name,
		 * but for BT / Chromecast device this is usually mac address or some other unique identifier.<br>
		 * Content values named {@link #BIND_TO_DEVICE_PREFIX}#, {@link #DEVICE_PREFIX}#, DEVICE_ADDRESS_PREFIX, {@link #DEVICE_NAME_PREFIX}#,
		 * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
		 * # is number [0, NUM_BIND_DEVICES)<br>
		 * @since 856<br>
		 * TEXT 
		 */
		public static final @NonNull String DEVICE_ADDRESS_PREFIX = "__device_address_";
		
		/**
		 * Prefix for virtual fields, used for insert/update contentValues. Sets visible device name for given device assignment.<br> 
		 * Content values named {@link #BIND_TO_DEVICE_PREFIX}, {@link #DEVICE_PREFIX}, {@link #DEVICE_ADDRESS_PREFIX}, DEVICE_NAME_PREFIX,
		 * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
		 * # is number [0, NUM_BIND_DEVICES)<br>
		 * @since 856<br>
		 * TEXT 
		 */
		public static final @NonNull String DEVICE_NAME_PREFIX = "__device_name_";

		/**
		 * Virtual field, used for insert/update contentValues. If set, preset is bound to that track<br>
		 * NOTE: track related assigments requires preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * INTEGER
		 * @deprecated see {@link #BIND_TO_TRACK} 
		 */
		@Deprecated
		public static final @NonNull String BIND_TO_TRACK_ID = "__bind_to_track_id";
		
		/**
		 * Virtual field, used for insert/update contentValues. If set, preset is unbound from that track<br>
		 * NOTE: track related assignents requires preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * INTEGER
		 * @deprecated see {@link #BIND_TO_TRACK} 
		 */
		@Deprecated
		public static final @NonNull String UNBIND_FROM_TRACK_ID = "__unbind_to_track_id";
		
		/**
		 * Virtual field, used for insert/update contentValues. If set, appropriate category uri (string==getMassOpsItemsUri(false)) is bound for this track<br>
		 * NOTE: track related assigments requires preset id in uri, for example uri path should end with /eq_presets/123<br>
		 * INTEGER (boolean) 
		 * @deprecated see {@link #BIND_TO_CAT}
		 */
		@Deprecated
		public static final @NonNull String BIND_TO_CAT_URI = "__bind_to_cat_uri";
	}

	public static final class EqPresetSongs {
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
		public static final @NonNull String PRESET_ID = TABLE + ".preset_id";
	}

	public static final class EqPresetDevices {
		public static final @NonNull String TABLE = "eq_preset_devices";

		public static final @NonNull String _ID = TABLE + "._id";

		/**
		 * Eq preset id<br>
		 * INTEGER
		 */
		public static final @NonNull String PRESET_ID = TABLE + ".preset_id";

		/**
		 * Device type<br>
		 * INTEGER
		 */
		public static final @NonNull String DEVICE = "device";

		/**
		 * Device name<br>
		 * TEXT
		 */
		public static final @NonNull String DEVICE_NAME = "device_name";

		/**
		 * Device address<br>
		 * TEXT
		 */
		public static final @NonNull String DEVICE_ADDRESS = "device_address";
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

	/** @since 841 */
	public class PrefSearch {
		public static final @NonNull String TABLE = "pref_search";
		public static final @NonNull String _ID = TABLE + "._id";
		public static final @NonNull String BREADCRUMB = "breadcrumb";
		public static final @NonNull String PREF_URI = "pref_uri";
		public static final @NonNull String PREF_KEY = "pref_key";
		public static final @NonNull String ICON = "icon";
	}

	/** @since 841 */
	public class PrefSearchFts {
		public static final @NonNull String TABLE = "pref_search_fts";
		public static final @NonNull String DOCID = "docid";
		public static final @NonNull String TITLE = "title";
		public static final @NonNull String SUMMARY = "summary";
	}
}
