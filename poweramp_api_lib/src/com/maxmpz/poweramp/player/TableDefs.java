/*
Copyright (C) 2011-2014 Maksim Petrov

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

import android.provider.BaseColumns;

public interface TableDefs {
	/**
	 * Alias used for category. Useful when query is actually a multi table join.
	 */
	public static final String CATEGORY_ALIAS = "cat";
	
	public static final long UNKNOWN_ID = 1000L;
	
	public static final String NUM_FILES_ALL = "num_files AS num_files_total";
	public static final String NUM_FILES_NO_CUE = "(num_files - num_cue_files) AS num_files_total";
	
	
	/**
	 * Alias used for category aliased table _id.
	 */
	public static final String CATEGORY_ALIAS_ID = CATEGORY_ALIAS + "._id";
	
	
	public interface Files {
    	public static final String TABLE = "folder_files";
    	public static final String VIEW_MOST_PLAYED = "files_most_played";
    	public static final String VIEW_TOP_RATED = "files_most_played";
    	public static final String VIEW_RECENTLY_ADDED = "files_recently_added";
    	public static final String VIEW_RECENTLY_ADDED_FS = "files_recently_added_fs";
    	public static final String VIEW_RECENTLY_PLAYED = "files_recently_played";
    	
    	// Fields.
    	
    	public static final String _ID = TABLE + "._id";
    	
    	/*
    	 * Short filename.
    	 * String.
    	 */
    	public static final String NAME = TABLE + ".name";
    	
    	/*
    	 * Track number (extracted from filename).
    	 * Int.
    	 */
    	public static final String TRACK_NUMBER = "track_number";
    	
    	/*
    	 * Track name without number.
    	 * String.
    	 */
    	public static final String NAME_WITHOUT_NUMBER = "name_without_number";
    	
    	/*
    	 * One of the TAG_* constants.
    	 * Int.
    	 */
    	public static final String TAG_STATUS = "tag_status";
    	
    	/*
    	 * Track # tag.
    	 * Int.
    	 */
    	public static final String TRACK_TAG = "track_tag";
    	
    	/*
    	 * Parent folder id.
    	 * Int.
    	 */
    	public static final String FOLDER_ID = "folder_id";
    	
    	/*
    	 * Title tag.
    	 * String.
    	 */
    	public static final String TITLE_TAG = TABLE + ".title_tag";
    	
    	/*
    	 * Duration in miliseconds.
    	 * Int.
    	 */
    	public static final String DURATION = "duration";
    	
    	/*
    	 * Int.
    	 */
    	public static final String UPDATED_AT = TABLE + ".updated_at";
    	
    	/*
    	 * One of the file types - see PowerAMPiAPI.Track.FileType class.
    	 */
    	public static final String FILE_TYPE = "file_type";
    	
    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";

    	/*
    	 * Int.
    	 * This is file last modified time actually, for most filesystems
    	 */
    	public static final String FILE_CREATED_AT = "file_created_at";
    	
    	/*
    	 * Bitwise flag.
    	 * Int.
    	 */
    	public static final String AA_STATUS = "aa_status";
    	
    	/*
    	 * Full path. Works only if the query is joined with the folders.
    	 * String.
    	 */
    	public static final String FULL_PATH = Folders.PATH + "||" + NAME;
    	
    	/*
    	 * Int.
    	 */
    	public static final String RATING = "rating";

    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_TIMES = TABLE + ".played_times";

    	/*
    	 * Int.
    	 */
    	public static final String ALBUM_ID = TABLE + ".album_id";

    	/*
    	 * Int.
    	 */
    	public static final String ARTIST_ID = TABLE + ".artist_id";

    	/*
    	 * Int.
    	 */
    	public static final String COMPOSER_ID = TABLE + ".composer_id";
    	
    	/*
    	 * Int.
    	 */
    	public static final String YEAR = "year";

    	/*
    	 * Int.
    	 */
    	public static final String OFFSET_MS = "offset_ms";

    	/*
    	 * Int.
    	 */
    	public static final String CUE_FOLDER_ID = "cue_folder_id";

    	/*
    	 * First seen time.
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";
    	
    	
		/**
		 * tag_status
		 */
		public static final int TAG_NOT_SCANNED = 0;
		/**
		 * tag_status
		 */
		public static final int TAG_SCANNED = 1;
		
		/**
		 * aa_status
		 */
		public static final int AA_STATUS_NONE = 0;
		/**
		 * aa_status
		 */
		public static final int AA_STATUS_EMBED = 1;
	}
	
	
	
	public interface Folders {
		public static final String TABLE = "folders";

		// Fields.
		
		public static final String _ID = TABLE + "._id";

		/*
		 * volume_id (fatID) of the storage.
		 * Int.
		 */
		//public static final String VOLUME_ID = TABLE + ".volume_id";

		/*
		 * Short name of the folder.
		 * String.
		 */
		public static final String NAME = TABLE + ".name";

		/*
		 * Short path of the parent folder.
		 * String.
		 */
		public static final String PARENT_NAME = TABLE + ".parent_name";

		/*
		 * Full path of the folder.
		 * String.
		 */
		public static final String PATH = TABLE + ".path";

		/*
		 * Folder album art/thumb image (short name).
		 * String.
		 */
		public static final String THUMB = TABLE + ".thumb";

		/*
		 * One of the THUMB_* constants.
		 * Int.
		 */
		public static final String THUMB_STATUS = TABLE + ".thumb_status";

		/*
		 * Number of files in a folder.
		 * Int.
		 */
		public static final String NUM_FILES = TABLE + ".num_files";
		
		/*
		 * Int.
		 */
		public static final String DIR_MODIFIED_AT = TABLE + ".dir_modified_at";
		
		/*
		 * Int.
		 */
		public static final String UPDATED_AT = TABLE + ".updated_at";
		/*
		 * Id of the parent folder or 0 for "root" folders.
		 * Int.
		 */
		public static final String PARENT_ID = TABLE + ".parent_id";	
		/*
		 * Number of child subfolders.
		 * Int.
		 */
		public static final String NUM_SUBFOLDERS = TABLE + ".num_subfolders";

		/*
		 * Int.
		 */
		public static final String IS_CUE = TABLE + ".is_cue";

		/*
		 * Set for real (non-cue-virtual) folders, means number of cue source files inside this folder.
		 * Int.
		 */
		public static final String NUM_CUE_FILES = TABLE + ".num_cue_files";
		
    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";

    	/*
		 * 
		 * First seen time.
		 * Int.
		 */
		public static final String CREATED_AT = TABLE + ".created_at";
	}
	
	
	public interface Albums {
		public static final String TABLE = "albums";
		
		// Fields.
		
		public static final String _ID = TABLE + "._id";
		
		/*
    	 * String.
    	 */
    	public static final String ALBUM = "album";
    	
		/*
    	 * String.
    	 */
    	public static final String ALBUM_SORT = "album_sort";

    	/*
    	 * Int
    	 */
    	//public static final String NUM_FILES = TABLE + ".num_files";
    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";
    	/*
    	 * First seen time.
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";
    	
    	// Albums uses special where for cue sources, thus just count files is enough.
		public static final String COUNT_FILES = "count(folder_files._id)";
//		public static final String COUNT_FILES_RAW = "(SELECT COUNT(*) FROM folder_files WHERE album_id=albums._id)";
//		public static final String COUNT_FILES_NON_CUE_SOURCE = "(SELECT COUNT(*) FROM folder_files WHERE album_id=albums._id AND folder_files.cue_folder_id IS NULL)";
//		public static final String COUNT_FILES_NON_CUE_SOURCE = "count(folder_files._id)";
	}
	
	
	public interface Artists {
		public static final String TABLE = "artists";
		
		// Fields.
		
		public static final String _ID = TABLE + "._id";
		
    	/*
    	 * String.
    	 */
    	public static final String ARTIST = "artist";

    	/*
    	 * String.
    	 */
    	public static final String ARTIST_SORT = "artist_sort";

    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";
    	/*
    	 * First seen time.
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";

    	// Artists uses special where for cue sources, thus just count files is enough.
		public static final String COUNT_FILES = "count(folder_files._id)";
	}
	

	public interface ArtistAlbums {
		public static final String TABLE = "artist_albums";
		
		// Fields.
		
		public static final String _ID = TABLE + "._id";
		
    	/*
    	 * Int.
    	 */
    	public static final String ARTIST_ID = TABLE + ".artist_id";

    	/*
    	 * Int.
    	 */
    	public static final String ALBUM_ID = TABLE + ".album_id";

    	/*
    	 * Int
    	 */
    	//public static final String NUM_FILES = TABLE + ".num_files";
    	
    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";
    	/*
    	 * First seen time.
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";
	}
	
	
	public interface Composers {
		public static final String TABLE = "composers";
		
		// Fields.
		
		public static final String _ID = TABLE + "._id";

		/*
    	 * String.
    	 */
    	public static final String COMPOSER = "composer";
    	
    	public static final String COMPOSER_SORT = "composer_sort";

    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";
    	/*
    	 * First seen time.
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";
    	
    	// Composers uses special where for cue sources, thus just count files is enough.
		public static final String COUNT_FILES = "count(folder_files._id)";
	}
	
	public interface Genres {
		public static final String TABLE = "genres";
		
		// Fields.
		
		public static final String _ID = TABLE + "._id";

		/*
    	 * String.
    	 */
    	public static final String GENRE = "genre";
    	
    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";
    	/*
    	 * First seen time.
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";
    	
		public static final String COUNT_FILES = "count(folder_files._id)";    	
	}
	

	public interface GenreEntries {
		public static final String TABLE = "genre_entries";
		
		// Fields.
		
		public static final String _ID = TABLE + "._id";

		/*
		 * Actual id of the file in folder_files table.
		 * Long.
		 */
		public static final String FOLDER_FILE_ID = "folder_file_id";
		
		/*
		 * Gerne id.
		 * Long.
		 */
		public static final String GENRE_ID = "genre_id";
	}
	
	
	public interface PlaylistEntries {
		public static final String TABLE = "folder_playlist_entries";
		
    	// Fields.
    	
    	public static final String _ID = TABLE + "._id";

    	/*
		 * Actual id of the file in folder_files table.
		 * Int.
		 */
		public static final String FOLDER_FILE_ID = "folder_file_id";
		
		/*
		 * Folder Playlist id.
		 * Int.
		 */
		public static final String PLAYLIST_ID = "playlist_id";
		
		/*
		 * Sort order.
		 * Int.
		 */
		public static final String SORT = "sort";
	}

	
	public interface Playlists {
		public static final String TABLE = "folder_playlists";
		
    	// Fields.
    	
    	public static final String _ID = TABLE + "._id";
    	
    	/*
		 * Name of the playlist.
		 * String.
		 */
		public static final String NAME = TABLE + ".name";
		
    	/*
    	 * Int.
    	 */
    	public static final String MTIME = TABLE + ".mtime";

    	/*
    	 * Int.
    	 */
    	public static final String PATH = TABLE + ".path";

    	/*
    	 * Int.
    	 */
    	public static final String SSID = TABLE + ".ssid";
    	/*
    	 * Int.
    	 */
    	public static final String PLAYED_AT = TABLE + ".played_at";
    	/*
    	 * Int.
    	 */
    	public static final String CREATED_AT = TABLE + ".created_at";
    	/*
    	 * Int.
    	 */
    	public static final String UPDATED_AT = TABLE + ".updated_at";
    	
    	public static final String NUM_FILES_COUNT = "(SELECT COUNT(*) FROM " + PlaylistEntries.TABLE + " WHERE " + PlaylistEntries.PLAYLIST_ID + "=" + _ID + ") AS _track_count";
	}
	
	
	
	
	public class Queue {
		public static final String TABLE = "queue";
		
		public static final String _ID = TABLE + "._id";

		/*
		 * Folder file id.
		 * Int.
		 */
		public static final String FOLDER_FILE_ID = TABLE + ".folder_file_id";
		
		/*
		 * Int.
		 */
		public static final String CREATED_AT = TABLE + ".created_at";

		/*
		 * Int.
		 */
		public static final String SORT = TABLE + ".sort";

		public static final String CALC_PLAYED = "folder_files.played_at > queue.created_at";
		public static final String CALC_UNPLAYED = "folder_files.played_at <= queue.created_at";
	}
	
	public class ShuffleSessionIds {
		public static final String TABLE = "shuffle_session_ids";
		
		public static final String _ID = TABLE + "._id";
	}
	
	
	public class EqPresets {
		public static final String TABLE = "eq_presets";
		
		public static final String _ID = TABLE + "._id";
	
	    /*
	     * Predefined preset number (see res/values/arrays/eq_preset_labels) or NULL for custom preset.
	     * Int.
	     */
	    public static final String PRESET = "preset";
	    
	    /*
	     * Eq preset string.
	     * String. 
	     */
	    public static final String _DATA = TABLE + "._data";
	    
	    /*
	     * Custom preset name or null for predefined preset.
	     * String.
	     */
	    public static final String NAME = TABLE + ".name";
	    
	    /*
	     * 1 if preset is bound to speaker, 0 otherwise.
	     * Int.
	     */
	    public static final String BIND_TO_SPEAKER = "bind_to_speaker";
	    
	    /*
	     * 1 if preset is bound to wired headset, 0 otherwise.
	     * Int.
	     */
	    public static final String BIND_TO_WIRED = "bind_to_wired";
	    
	    /*
	     * 1 if preset is bound to bluetooth audio output, 0 otherwise.
	     * Int.
	     */
	    public static final String BIND_TO_BT = "bind_to_bt";
	}
	
	
	public static final class EqPresetSongs implements BaseColumns {
		public static final String TABLE = "eq_preset_songs";
		
		public static final String _ID = TABLE + "._id";
		
		/*
		 * Either folder_file_id.
		 * Int. 
		 */
		public static final String FILE_ID = TABLE + ".file_id";
		
		/*
		 * Eq preset id.
		 * Int.
		 */
		public static final String PRESET_ID = "preset_id";
	}
}
