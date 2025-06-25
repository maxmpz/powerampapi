/*
Copyright (C) 2011-2025 Maksim Petrov

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
package com.maxmpz.poweramp.player

import com.maxmpz.poweramp.player.PowerampAPI.Track.FileType
import com.maxmpz.poweramp.player.PowerampAPI.Track.TagStatus


object TableDefs {
    /**
     * Tracks
     */
    object Files {
        /** Special value for [.TRACK_NUMBER] - means no valid track number exists for the given track  */
        const val INVALID_TRACK_NUMBER: Int = 10000

        const val TABLE: String = "folder_files"

        const val _ID: String = "$TABLE._id"

        /**
         * Short filename
         * TEXT
         */
        const val NAME: String = "$TABLE.name"

        /**
         * Track number extracted from tag or filename. May include disc number (if >= 2) as thousands (2001, 2002, etc.).
         * Can be [.INVALID_TRACK_NUMBER].
         * Used for sorting
         * INTEGER
         */
        const val TRACK_NUMBER: String = "track_number"

        /**
         * Track number for display purposes (since 858), prior 858 just track number from track tags. 0 or NULL if no track tag exists.
         * Never includes disc number (since 858)
         * INTEGER
         */
        const val TRACK_TAG: String = "track_tag"

        /**
         * Track disc or 0 if no such tag exists
         * INTEGER
         * @since 859
         */
        const val DISC: String = "disc"

        /**
         * Track name without number. For streams - this is name of stream, if available
         * TEXT
         */
        const val NAME_WITHOUT_NUMBER: String = "name_without_number"

        /**
         * One of the TAG_* constants
         * INTEGER
         * @see com.maxmpz.poweramp.player.PowerampAPI.Track.TagStatus
         */
        const val TAG_STATUS: String = "tag_status"

        /**
         * Parent folder id
         * INTEGER
         */
        const val FOLDER_ID: String = "folder_id"

        /**
         * Title tag
         * NOTE: important to have it w/o table name for the header-enabled compound selects
         * TEXT
         */
        const val TITLE_TAG: String = "title_tag"

        /**
         * NOTE: non-null for streams only
         * TEXT
         */
        const val ALBUM_TAG: String = "album_tag"

        /**
         * NOTE: non-null for streams only
         * TEXT
         */
        const val ARTIST_TAG: String = "artist_tag"

        /**
         * Duration in milliseconds
         * INTEGER
         */
        const val DURATION: String = "$TABLE.duration"

        /**
         * Time of update in epoch seconds
         * INTEGER
         */
        const val UPDATED_AT: String = "$TABLE.updated_at"

        /**
         * One of the file types - [PowerampAPI.Track.FileType]
         */
        const val FILE_TYPE: String = "file_type"

        /**
         * Milliseconds, updated when track started
         * INTEGER
         */
        const val PLAYED_AT: String = "$TABLE.played_at"

        /**
         * Milliseconds, updated with play start time (=played_at) when and only if track is counted as played
         * INTEGER
         * @since 900
         */
        const val PLAYED_FULLY_AT: String = "$TABLE.played_fully_at"

        /**
         * This is the file last modified time - mtime (for the most Android variants).
         * The naming "file_created_at" is a legacy.
         * Seconds
         * INTEGER
         */
        const val FILE_CREATED_AT: String = "file_created_at"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = "$TABLE.aa_status"

        /**
         * INTEGER
         */
        const val RATING: String = "rating"

        /**
         * Number of times the track was played, as counted for the internal logic. This is not incremented when we are playing any count
         * based category, or count-based sorting as changing this field at the moment of playing such categories/sorts will constantly
         * change position of the file in the list
         * INTEGER
         */
        const val PLAYED_TIMES: String = "$TABLE.played_times"

        /**
         * Number of total played times for the track, including playing from count based categories/times
         * INTEGER
         * @since 989
         */
        const val TOTAL_PLAYED_TIMES: String = "$TABLE.total_played_times"

        /**
         * INTEGER
         */
        const val ALBUM_ID: String = "$TABLE.album_id"

        /**
         * INTEGER
         */
        const val ARTIST_ID: String = "$TABLE.artist_id"

        /**
         * INTEGER
         */
        const val ALBUM_ARTIST_ID: String = "$TABLE.album_artist_id"

        /**
         * INTEGER
         */
        const val COMPOSER_ID: String = "$TABLE.composer_id"

        /**
         * INTEGER
         */
        const val YEAR: String = "$TABLE.year"

        /**
         * Cue offset milliseconds
         * INTEGER
         */
        const val OFFSET_MS: String = "$TABLE.offset_ms"

        /**
         * If non-null - this is cue "source" (big uncut image) file with that given virtual folder id
         * NOTE: enforces 1-1 between source files and cues. No multiple cues per single image thus possible
         * INTEGER
         */
        const val CUE_FOLDER_ID: String = "cue_folder_id"

        /**
         * First seen time
         * Seconds
         * INTEGER
         */
        const val CREATED_AT: String = "$TABLE.created_at"

        /**
         * Wave scan data
         * byte[] blob, nullable
         */
        const val WAVE: String = "wave"

        /**
         * TEXT
         */
        const val META: String = "$TABLE.meta"

        /**
         * Last played position in milliseconds
         * INTEGER
         */
        const val LAST_POS: String = "last_pos"

        /**
         * INTEGER
         */
        const val SHUFFLE_ORDER: String = "$TABLE.shuffle_order"

        /**
         * 1 if this item (most probably stream track) was manually added and shouldn't be removed by rescans
         * INTEGER (boolean)
         * @since 857
         */
        const val USER_ADDED: String = "$TABLE.user_added"

        /**
         * Optional http(s) URL pointing to the target track. If exists, this will be used
         * instead of the path (== folder.path + file_name.name)
         * Can be [TrackProviderConsts.DYNAMIC_URL]
         * TEXT
         */
        const val URL: String = "$TABLE.url"

        /**
         * Optional full path to the file. This file_path always overrides parent folder path + filename. Used for the cases
         * when actual file path can't be derived from the parent folder due to non-hierarchical or opaque paths,
         * e.g. for tracks providers.
         * Path is in Poweramp internal path format
         * TEXT
         * @since 862
         */
        const val FILE_PATH: String = "$TABLE.file_path"

        /**
         * Optional bitrate of the file. Currently set only for the provider tracks if provided in the appropiate metadata
         * INTEGER
         * @since 862
         */
        const val BIT_RATE: String = "$TABLE.bit_rate"

        /**
         * Full path. Works only if the query is joined with the folders, otherwise this may fail
         * TEXT
         */
        const val FULL_PATH: String = "COALESCE(" + FILE_PATH + "," + Folders.PATH + "||" + NAME + "," + NAME + ")"

        /**
         * Alternative track number. Currently applied only in Folders/Folders Hierarchy files for track number sorting.
         * May differ for the provider tracks, equals track_number for all other tracks
         * INTEGER
         */
        const val TRACK_NUMBER_ALT: String = "track_number_alt"


        /**
         * @see com.maxmpz.poweramp.player.PowerampAPI.Track.TagStatus.TAG_NOT_SCANNED
         */
        @JvmField
        @Deprecated(" ")
        val TAG_NOT_SCANNED: Int = TagStatus.TAG_NOT_SCANNED

        /**
         * @see com.maxmpz.poweramp.player.PowerampAPI.Track.TagStatus.TAG_SCANNED
         */
        @JvmField
        @Deprecated(" ")
        val TAG_SCANNED: Int = TagStatus.TAG_SCANNED


        /**
         * If non-NULL, references lrc_files entry in [LrcFiles]
         * INTEGER NULL
         * @since 948
         */
        const val LRC_FILES_ID: String = "lrc_files_id"

        /**
         * If non-NULL, defines priority for [.LRC_FILES_ID]. The higher value is the higher priority.
         * LRC priority is used to support multiple possible sources of the LRC file for the track and
         * reduce the scope of the LRC resolition against files.
         * INTEGER NOT NULL DEFAULT 0
         * @since 948
         */
        const val LRC_FILES_PRIO: String = "lrc_files_prio"

        /**
         * 1 if this track is known to have lyrics tag (not necessarily a synchronized lyrics)
         * INTEGER NOT NULL (boolean)
         * @since 948
         */
        const val HAS_LYRICS_TAG: String = "has_lyrics_tag"

        /**
         * 1 if this track is known to be in some playlist, 0 otherwise
         * INTEGER NOT NULL (boolean)
         * @since 994
         */
        const val IN_PLAYLIST: String = "in_playlist"

        /**
         * If non-NULL, references cached lyrics entry in [CachedLyrics]
         * INTEGER NULL
         * @since 948
         */
        const val CACHED_LYRICS_ID: String = "cached_lyrics_id"

        /**
         * Cached lyrics loading start timestamp
         * Milliseconds in System.currentTimeMillis timebase
         * NULL if loading is not started yet or loading is complete
         * INTEGER NULL
         * @since 948
         */
        const val CACHED_LYRICS_LOADING_STARTED_AT: String = "cached_lyrics_loading_started_at"

        /**
         * Calculated field
         * INTEGER (boolean)
         */
        const val HAS_LYRICS: String =  // NOTE: avoid matching cached_lyrics_id for stream as stream constantly changes metadata
            "(has_lyrics_tag OR lrc_files_id IS NOT NULL" +
            " OR cached_lyrics_id IS NOT NULL AND cached_lyrics_loading_started_at IS NULL AND file_type!=" + FileType.TYPE_STREAM +
            ") AS _has_lyrics"
    }


    /**
     * Contains the single track entry when/if some path is requested to be played and that path is not in Poweramp Music Folders/Library.
     * We use "AS folder_files" in SQL so we can use Files fields directly as is
     * @since 949 this is always a structural copy of folder_files table (with just the single _id=[PowerampAPI.RAW_TRACK_ID] (-2) entry)
     */
    object RawFiles {
        const val TABLE: String = "raw_files"
    }

    object SoFiles {
        const val TABLE: String = "so_files"
    }

    /** All folders known to Poweramp */
    object Folders {
        const val TABLE: String = "folders"

        const val _ID: String = "$TABLE._id"

        /**
         * Display (label) name of the folder. Can be long for roots (e.g. can include storage description).
         * May or may not match actual filesystem folder name
         * TEXT
         */
        const val NAME: String = "$TABLE.name"

        /**
         * (Always) short name of the folder. Always matches actual filesystem folder name
         * For CUE - name of the file (either CUE or the target track with the embedded CUE)
         * TEXT
         * @since 828
         */
        const val SHORT_NAME: String = "$TABLE.short_name"

        /**
         * Short path of the parent folder. Always matches parent short_name which is parent actual filesystem folder name
         * TEXT
         */
        const val PARENT_NAME: String = "$TABLE.parent_name"

        /**
         * Parent folder display label (which can be much longer than just PARENT_NAME, e.g. include storage description) to display in the UI.
         * Corresponds to parent name
         * TEXT
         */
        const val PARENT_LABEL: String = "$TABLE.parent_label"

        /**
         * Full path of the folder
         * NOTE: avoid TABLE name here to allow using field in raw_files. "path" is (almost) unique column, also used in playlists
         * The path has a trailing /
         * TEXT
         */
        const val PATH: String = "path"

        /**
         * This is the same as path for usual folders, but for the cue virtual folders, this is path + name
         * Used for proper folders/subfolders hiearachy sorting and it's ciritcal for correct hieararchy playing/reshuffle
         * TEXT
         */
        const val SORT_PATH: String = "sort_path"

        /**
         * Folder album art/thumb image (short name)
         * TEXT
         */
        const val THUMB: String = "thumb"

        /**
         * INTEGER
         */
        const val DIR_MODIFIED_AT: String = "$TABLE.dir_modified_at"

        /**
         * Seconds
         * INTEGER
         */
        const val UPDATED_AT: String = "$TABLE.updated_at"

        /**
         * Id of the parent folder or 0 for "root" folders
         * INTEGER
         */
        const val PARENT_ID: String = "$TABLE.parent_id"

        /**
         * INTEGER
         */
        const val IS_CUE: String = "$TABLE.is_cue"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = "$TABLE.created_at"

        /**
         * Number of direct child subfolders
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_SUBFOLDERS: String = "$TABLE.num_subfolders"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = "$TABLE.num_files"

        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_ALL_FILES: String = "$TABLE.num_all_files"

        /**
         * Number of tracks in the whole folder hierarchy, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val HIER_NUM_FILES: String = "$TABLE.hier_num_files"

        /**
         * Duration in milliseconds for the tracks inside this folder only, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = "$TABLE.duration"

        /**
         * Duration in milliseconds for the whole hierarchy inside this folder, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val HIER_DURATION: String = "$TABLE.hier_duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = "$TABLE.dur_meta"

        /**
         * Hierarchy duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val HIER_DUR_META: String = "$TABLE.hier_dur_meta"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = "$TABLE.aa_status"

        /**
         * If 1 (true), this folder restores last played track
         * INTEGER (boolean)
         * @since 821
         */
        const val KEEP_LIST_POS: String = "$TABLE.keep_list_pos" // Sync with RestLibraryListMemorizable

        /**
         * If 1 (true), this folder restores last played track position
         * INTEGER (boolean)
         * @since 821
         */
        const val KEEP_TRACK_POS: String = "$TABLE.keep_track_pos" // Sync with RestLibraryListMemorizable

        const val KEEP_LIST_AND_TRACK_POS_COMBINED: String = "($KEEP_TRACK_POS<<1)+$KEEP_LIST_POS"

        /**
         * Non-null for provider folders, where provider wants to control default sorting order in Folders Hierarchy
         * INTEGER
         * @since 869
         */
        const val SORT_ORDER: String = "sort_order"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = "$TABLE.played_at"

        // Deprecated
        /**
         * Number of tracks in the whole folder hierarchy, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val HIER_NUM_ALL_FILES: String = "$TABLE.hier_num_all_files"

        /**
         * Duration in milliseconds for the tracks inside this folder only, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = "$TABLE.duration_all"

        /**
         * Hierarchy duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val HIER_DUR_ALL_META: String = "$TABLE.hier_dur_all_meta"

        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = "$TABLE.dur_all_meta"

        /**
         * Duration in milliseconds for the whole hierarchy inside this folder, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val HIER_DURATION_ALL: String = "$TABLE.hier_duration_all"

        /**
         * Calculated subquery column which retrieves parent name
         * TEXT
         */
        @Deprecated("use {@link #PARENT_LABEL}")
        const val PARENT_NAME_SUBQUERY: String =
            "(SELECT name FROM folders AS f2 WHERE f2._id=folders.parent_id) AS parent_name_subquery"

        /**
         * Calculated subquery column which retrieves short parent name
         * TEXT
         */
        @Deprecated("use {@link #PARENT_NAME} ")
        const val PARENT_SHORT_NAME_SUBQUERY: String =
            "(SELECT short_name FROM folders AS f2 WHERE f2._id=folders.parent_id) AS parent_short_name_subquery"
    }


    object Albums {
        const val TABLE: String = "albums"

        const val _ID: String = "$TABLE._id"

        /**
         * NOTE: important to have it w/o table for headers-enabled compound selects
         * TEXT
         */
        const val ALBUM: String = "album"

        /**
         * NOTE: important to have it w/o table for headers-enabled compound selects
         * TEXT
         */
        const val ALBUM_SORT: String = "album_sort"

        /**
         * NOTE: this is NULL for Unknown album, so not all joins are possible with just albums + album_artists (use folder_files for joins)
         * INTEGER
         */
        const val ALBUM_ARTIST_ID: String = "$TABLE.album_artist_id"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = "$TABLE.created_at"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = "$TABLE.num_files"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = "$TABLE.aa_status"

        /**
         * The guessed album year
         * INTEGER
         */
        const val ALBUM_YEAR: String = "album_year"

        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = "$TABLE.duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = "$TABLE.dur_meta"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = "$TABLE.played_at"

        // Deprecated
        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = "$TABLE.num_all_files"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = "$TABLE.duration_all"

        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = "$TABLE.dur_all_meta"
    }


    object Artists {
        const val TABLE: String = "artists"

        const val _ID: String = "$TABLE._id"

        /**
         * TEXT
         */
        const val ARTIST: String = "artist"

        /**
         * TEXT
         */
        const val ARTIST_SORT: String = "artist_sort"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = "$TABLE.created_at"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = "$TABLE.aa_status"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = "$TABLE.num_files"


        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = "$TABLE.duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = "$TABLE.dur_meta"

        /**
         * If true (1) this is unsplit combined multi-artist
         * INTEGER (boolean)
         * @since 899
         */
        const val IS_UNSPLIT: String = "$TABLE.is_unsplit"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = "$TABLE.played_at"

        // Deprecated
        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864. NUM_FILES is now dynamically updated depending on show cue images preference")
        val NUM_ALL_FILES: String = "$TABLE.num_all_files"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = "$TABLE.duration_all"

        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = "$TABLE.dur_all_meta"
    }

    /**
     * One-to-many relation table for track artists
     * Always used. First entry may be the UNKNOWN_ID or an unsplit artist
     * @since 899
     */
    object MultiArtists {
        const val TABLE: String = "multi_artists"

        const val _ID: String = "$TABLE._id"
        const val ARTIST_ID: String = "$TABLE.artist_id"
        const val FILE_ID: String = "$TABLE.file_id"
    }

    /** This is similar to Artists, but uses Album Artist tag, where available  */
    object AlbumArtists {
        const val TABLE: String = "album_artists"

        const val _ID: String = TABLE + "._id"

        /**
         * TEXT
         */
        const val ALBUM_ARTIST: String = "album_artist"

        /**
         * TEXT
         */
        const val ALBUM_ARTIST_SORT: String = "album_artist_sort"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = TABLE + ".aa_status"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = TABLE + ".dur_meta"

        /**
         * If true (1) this is combined unsplit multi-artist
         * INTEGER (boolean)
         * @since 899
         */
        const val IS_UNSPLIT: String = TABLE + ".is_unsplit"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        // Deprecated fields
        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = TABLE + ".dur_all_meta"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = TABLE + ".duration_all"

        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = TABLE + ".num_all_files"
    }

    /**
     * One-to-many relation table for track album artists
     * Always used. First entry may be the UNKNOWN_ID or an unsplit album artist
     * @since 899
     */
    object MultiAlbumArtists {
        const val TABLE: String = "multi_album_artists"

        const val _ID: String = TABLE + "._id"
        const val ALBUM_ARTIST_ID: String = TABLE + ".album_artist_id"
        const val FILE_ID: String = TABLE + ".file_id"
    }

    /**
     * Album => artist 1:1 binding table
     * Used for Albums by Artist category, where can be multiple same Album repeated per each Artist
     */
    object AlbumsByArtist {
        const val TABLE: String = "artist_albums"

        const val _ID: String = TABLE + "._id"

        /**
         * INTEGER
         */
        const val ARTIST_ID: String = TABLE + ".artist_id"

        /**
         * INTEGER
         */
        const val ALBUM_ID: String = TABLE + ".album_id"

        /**
         * First seen time
         * Seconds unix time
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        // Deprecated
        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = TABLE + ".num_all_files"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = TABLE + ".duration_all"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = TABLE + ".dur_meta"

        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = TABLE + ".dur_all_meta"
    }


    object Composers {
        const val TABLE: String = "composers"

        const val _ID: String = TABLE + "._id"

        /**
         * TEXT
         */
        const val COMPOSER: String = "composer"

        /**
         * INTEGER
         */
        const val COMPOSER_SORT: String = "composer_sort"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = TABLE + ".aa_status"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = TABLE + ".dur_meta"

        /**
         * If true (1) this is combined unsplit multi-composer
         * INTEGER (boolean)
         * @since 899
         */
        const val IS_UNSPLIT: String = TABLE + ".is_unsplit"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        // Deprecated
        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = TABLE + ".num_all_files"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = TABLE + ".duration_all"

        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = TABLE + ".dur_all_meta"
    }


    /**
     * One-to-many relation table for track album artists
     * Always used. First entry may be the UNKNOWN_ID or an unsplit composer
     * @since 899
     */
    object MultiComposers {
        const val TABLE: String = "multi_composers"

        const val _ID: String = TABLE + "._id"
        const val COMPOSER_ID: String = TABLE + ".composer_id"
        const val FILE_ID: String = TABLE + ".file_id"
    }


    object Genres {
        const val TABLE: String = "genres"

        const val _ID: String = TABLE + "._id"

        /**
         * TEXT
         */
        const val GENRE: String = "genre"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        const val DUR_META: String = TABLE + ".dur_meta"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = TABLE + ".aa_status"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        // Deprecated
        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = TABLE + ".num_all_files"

        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         * @since 829
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = TABLE + ".dur_all_meta"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 829
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = TABLE + ".duration_all"
    }


    object GenreEntries {
        const val TABLE: String = "genre_entries"

        const val _ID: String = TABLE + "._id"

        /**
         * Actual id of the file in folder_files table
         * INTEGER
         */
        const val FOLDER_FILE_ID: String = "folder_file_id"

        /**
         * Genre id
         * INTEGER
         */
        const val GENRE_ID: String = "genre_id"
    }


    /**
     * @since 856
     */
    object Years {
        const val TABLE: String = "years"

        const val _ID: String = TABLE + "._id"

        /**
         * INTEGER
         */
        const val YEAR: String = TABLE + ".year"

        /**
         * First seen time
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Number of tracks in this category, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Duration in milliseconds, excluding cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         */
        const val DUR_META: String = TABLE + ".dur_meta"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = TABLE + ".aa_status"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        // Deprecated
        /**
         * Duration meta including cues
         * Dynamically recalculated on rescans
         * TEXT
         */
        @Deprecated("since 864")
        val DUR_ALL_META: String = TABLE + ".dur_all_meta"

        /**
         * Duration in milliseconds, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val DURATION_ALL: String = TABLE + ".duration_all"

        /**
         * Number of tracks in this category, including cue source images
         * Dynamically recalculated on rescans
         * INTEGER
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = TABLE + ".num_all_files"
    }


    /**
     * Used for specific dynamic subcategories, such as Artist Albums, where data subset is dynamically generated, thus, no table for stats otherwise exist
     * TYPE + REF_ID is an unique index
     * @since 863
     */
    object CatStats {
        const val TABLE: String = "cat_stats"

        const val _ID: String = TABLE + "._id"

        /**
         * Matches category numeric type ([PowerampAPI.Cats])
         * INTEGER
         */
        const val TYPE: String = TABLE + ".type"

        /**
         * First referenced target id, e.g. genre _id
         * INTEGER
         */
        const val REF_ID: String = TABLE + ".ref_id"

        /**
         * Second referenced target id, e.g. album _id
         * INTEGER
         */
        const val REF_ID2: String = TABLE + ".ref_id2"

        /**
         * Number of tracks in this category
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Duration in milliseconds
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         */
        const val DUR_META: String = TABLE + ".dur_meta"
    }


    object PlaylistEntries {
        const val TABLE: String = "playlist_entries"

        const val _ID: String = TABLE + "._id"

        /**
         * Actual id of the file in folder_files table
         * INTEGER
         */
        const val FOLDER_FILE_ID: String = "folder_file_id"

        /**
         * Folder Playlist id
         * INTEGER
         */
        const val PLAYLIST_ID: String = "playlist_id"

        /**
         * Sort order
         * INTEGER
         */
        const val SORT: String = "sort"

        /**
         * Filename, the full or final part of the stream URL
         * TEXT
         * @since 842
         */
        const val FILE_NAME: String = "file_name"

        /**
         * Parent folder path (matching [Folders.PATH], or base URL (or NULL) for streams
         * TEXT
         * @since 842
         */
        const val FOLDER_PATH: String = "folder_path"

        /**
         * Cue offset for .cue tracks
         * INTEGER
         * @since 842
         */
        const val CUE_OFFSET_MS: String = TABLE + ".cue_offset_ms"

        /**
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        /**
         * INTEGER
         */
        const val SHUFFLE_ORDER: String = TABLE + ".shuffle_order"
    }


    object Playlists {
        const val TABLE: String = "playlists"

        const val _ID: String = TABLE + "._id"

        /**
         * Name of the playlist
         * TEXT
         */
        const val PLAYLIST: String = TABLE + ".playlist"

        /**
         * Updated to match file based playlist. Also updated on entry insert/reorder/deletion - for all playlists
         * INTEGER
         */
        const val MTIME: String = TABLE + ".mtime"

        /**
         * TEXT
         */
        const val PATH: String = TABLE + ".playlist_path"

        /**
         * Seconds
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Seconds
         * INTEGER
         */
        const val UPDATED_AT: String = TABLE + ".updated_at"

        /**
         * Number of playlist entries
         * Dynamically recalculated on rescans
         * INTEGER
         */
        const val NUM_FILES: String = TABLE + ".num_files"

        /**
         * Bitwise flag
         * INTEGER
         */
        const val AA_STATUS: String = TABLE + ".aa_status"

        /**
         * INTEGER (boolean)
         */
        const val KEEP_LIST_POS: String = TABLE + ".keep_list_pos" // Sync with RestLibraryListMemorizable

        /**
         * INTEGER (boolean)
         */
        const val KEEP_TRACK_POS: String = TABLE + ".keep_track_pos" // Sync with RestLibraryListMemorizable

        const val KEEP_LIST_AND_TRACK_POS_COMBINED: String = "(" + KEEP_TRACK_POS + "<<1)+" + KEEP_LIST_POS

        /**
         * Duration in milliseconds
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 826
         */
        const val DURATION: String = TABLE + ".duration"

        /**
         * Duration meta
         * Dynamically recalculated on rescans
         * TEXT
         * @since 826
         */
        const val DUR_META: String = TABLE + ".dur_meta"

        /**
         * Calculated column
         * INTEGER (boolean)
         */
        const val IS_FILE: String = TABLE + ".playlist_path IS NOT NULL AS _is_file"

        /**
         * This is usually not updated, unless shuffled
         * INTEGER
         */
        const val PLAYED_AT: String = TABLE + ".played_at"

        // Deprecated
        /**
         * Number of playlist entries. Since 824 - same as num_files
         * Poweramp can insert CUE images into playlists if appropriate option enabled, or it skips them completely. In anycase, playlist should show # of all possible entries in it,
         * without filtering for CUE images
         * Dynamically recalculated on rescans
         * INTEGER
         * @since 796
         */
        @Deprecated("since 864")
        val NUM_ALL_FILES: String = TABLE + ".num_all_files"
    }


    object Queue {
        const val TABLE: String = "queue"

        const val _ID: String = TABLE + "._id"

        /**
         * Folder file id
         * INTEGER
         */
        const val FOLDER_FILE_ID: String = TABLE + ".folder_file_id"

        /**
         * Milliseconds
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * INTEGER
         */
        const val SORT: String = TABLE + ".sort"

        /**
         * INTEGER
         */
        const val SHUFFLE_ORDER: String = TABLE + ".shuffle_order"

        const val CALC_PLAYED: String =
            "folder_files.played_at >= queue.created_at" // If played at is the same as queue entry time, consider it played already 
        const val CALC_UNPLAYED: String = "folder_files.played_at < queue.created_at"
    }

    /**
     * @since 877
     */
    object Bookmarks {
        const val TABLE: String = "bookmarks"

        const val _ID: String = TABLE + "._id"

        /**
         * Folder file id
         * INTEGER
         */
        const val FOLDER_FILE_ID: String = TABLE + ".folder_file_id"

        /**
         * Milliseconds
         * INTEGER
         */
        const val OFFSET_MS: String = TABLE + ".offset_ms"

        /**
         * INTEGER
         */
        const val SORT: String = TABLE + ".sort"

        /**
         * TEXT
         */
        const val META: String = TABLE + ".meta"

        /**
         * Milliseconds
         * INTEGER
         */
        const val CREATED_AT: String = TABLE + ".created_at"
    }

    /** Never used, to be removed  */
    @Deprecated("")
    object ShuffleSessionIds {
        const val TABLE: String = "shuffle_session_ids"

        const val _ID: String = TABLE + "._id"
    }


    object EqPresets {
        const val TABLE: String = "eq_presets"

        const val _ID: String = TABLE + "._id"

        /**
         * If non-null - this is the predefined preset (see res/values/arrays/eq_preset_labels).
         * preset=0 defines the user default preset (the preset selected when no any named preset is selected),
         * which is tehcnically built-in (provided by the app)
         * INTEGER
         */
        const val PRESET: String = "preset"

        /**
         * Eq preset string. Either this or data_blob is used for the preset data
         * The text version of data is supported for the graphic mode only (since 906)
         * TEXT
         */
        const val _DATA: String = TABLE + "._data"

        /**
         * Eq preset data. Either this or _data is used for the preset data, app always saves to DATA_BLOB.
         * To read user/built-in/AutoEq preset data properly, use [.RESOLVED_BLOB], as DATA_BLOB
         * is NULL for built-in/AutoEq presets until user changes the preset.
         * The blob data format supports both graphic and parametric modes
         * BLOB
         * @since 906
         */
        const val DATA_BLOB: String = TABLE + ".data_blob"

        /**
         * Default eq preset data for built-in and AutoEq presets.
         * BLOB
         * @since 960
         */
        const val DEFAULT_BLOB: String = TABLE + ".default_blob"

        /**
         * Eq preset data resolved to either user changed data (if any) or the default preset data
         * BLOB
         * @since 960
         */
        const val RESOLVED_BLOB: String = "COALESCE(" + DATA_BLOB + "," + DEFAULT_BLOB + ")"

        /**
         * 1 if preset is parametric
         * INTEGER (boolean)
         * @since 906
         */
        const val PARAMETRIC: String = "parametric"

        /**
         * Preset name
         * TEXT
         */
        const val NAME: String = TABLE + ".name"

        /**
         * Additional meta information, such as AutoEq category
         * TEXT
         */
        const val META: String = TABLE + ".meta"

        /**
         * Updated automatically when name, _data, data_blob, or parametric fields are updated, other fields ignored
         * Seconds before 980, milliseconds for 980+
         * INTEGER
         * @since 906
         */
        const val UPDATED_AT: String = TABLE + ".updated_at"

        /**
         * BLOB
         * @since 906
         */
        const val SHARE_BLOB: String = "share_blob"

        /**
         * Seconds before 980, milliseconds for 980+
         * INTEGER
         * @since 906
         */
        const val SHARE_BLOB_UPDATED_AT: String = "share_blob_updated_at"

        /**
         * INTEGER
         * Defines type of preset: user, builtin, autoeq, etc.
         * NOTE: parametric/graphic behavior is defined by [.PARAMETRIC]
         * @see EqPresetConsts
         *
         * @since 960
         */
        const val TYPE: String = TABLE + ".type"

        /**
         * 1 if preset is bound to speaker, 0 otherwise
         * INTEGER (boolean)
         */
        const val BIND_TO_SPEAKER: String = "bind_to_speaker"

        /**
         * 1 if preset is bound to wired headset, 0 otherwise
         * INTEGER (boolean)
         */
        const val BIND_TO_WIRED: String = "bind_to_wired"

        /**
         * 1 if preset is bound to bluetooth audio output, 0 otherwise
         * INTEGER (boolean)
         */
        const val BIND_TO_BT: String = "bind_to_bt"

        /**
         * 1 if preset is bound to USB audio output, 0 otherwise
         * INTEGER (boolean)
         */
        const val BIND_TO_USB: String = "bind_to_usb"

        /**
         * 1 if preset is bound to other audio outputs, 0 otherwise
         * INTEGER (boolean)
         */
        const val BIND_TO_OTHER: String = "bind_to_other"

        /**
         * 1 if preset is bound to chromecast output, 0 otherwise
         * INTEGER (boolean)
         */
        const val BIND_TO_CHROMECAST: String = "bind_to_cc"

        /**
         * Baked up devices as ints string for the assigned named devices. Used as an optimization for the UI layer
         * TEXT
         * @since 963
         */
        const val META_BOUND_DEVICES: String = "meta_bound_devices"

        /**
         * Baked up first assigned device and device name (device-device_name). Used as an optimization for the UI layer
         * TEXT
         * @since 963
         */
        const val META_BOUND_DEVICE_NAME: String = "meta_bound_device_name"

        /**
         * Virtual field, used for insert/update contentValues.
         * If set to 1 preset is bound to track specified by [.BOUND_TRACK_ID], if set to 0, preset is unbound from that track
         * NOTE: track related assignments required preset id in uri, for example uri path should end with /eq_presets/123
         * INTEGER (boolean)
         * @since 856
         */
        const val BIND_TO_TRACK: String = "__bind_to_track"

        /**
         * Virtual field, used for insert/update contentValues.
         * Should be set to track id which should be bound/unbound with [.BIND_TO_TRACK]
         * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123
         * @since 856
         * INTEGER
         */
        const val BOUND_TRACK_ID: String = "__bound_track_id"

        /**
         * Virtual field, used for insert/update contentValues.
         * If set to 1 preset is bound to all category tracks specified by [.BOUND_CAT_URI], if set to 0, preset is unbound from these tracks
         * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123
         * @since 856
         * INTEGER (boolean)
         */
        const val BIND_TO_CAT: String = "__bind_to_cat"

        /**
         * Virtual field, used for insert/update contentValues.
         * Should be set to category uri which should be bound/unbound with [.BIND_TO_CAT]
         * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123
         * @since 856
         * TEXT
         */
        const val BOUND_CAT_URI: String = "__bound_cat_uri"

        /**
         * Virtual field, used for insert/update contentValues. If this is set, no other track bind/unbind assignments will be executed and preset is unbound from all tracks
         * NOTE: track related assignents required preset id in uri, for example uri path should end with /eq_presets/123
         * INTEGER (boolean)
         */
        const val UNBIND_FROM_ALL_TRACK_IDS: String = "__unbind_from_all_track_ids"

        /**
         * Virtual field, used for insert/update contentValues. If this is set, no other device bind/unbind assignments will be executed and preset is unbound from all devices
         * @since 856
         * INTEGER (boolean)
         */
        const val UNBIND_FROM_ALL_DEVICES: String = "__unbind_from_all_devices"

        /**
         * Virtual field, used for insert/update contentValues. 
         * If this is set to value > 0, * content values named [.BIND_TO_DEVICE_PREFIX]#, [.DEVICE_PREFIX]#, [.DEVICE_ADDRESS_PREFIX]#, [.DEVICE_NAME_PREFIX]#,
         * will be checked and if all 4 are set to appropriate valid non-empty values, preset will be bound/unbound to that device according to the [.BIND_TO_DEVICE_PREFIX]#.
         * # is number [0, NUM_BIND_DEVICES)
         * @since 856
         * INTEGER
         */
        const val NUM_BIND_DEVICES: String = "__num_bind_devices"

        /**
         * Prefix for virtual fields, used for insert/update contentValues.
         * Content values named BIND_TO_DEVICE_PREFIX#, [.DEVICE_PREFIX]#, [.DEVICE_ADDRESS_PREFIX]#, [.DEVICE_NAME_PREFIX]#,
         * will be checked and if all set to appropriate valid values, preset will
         * be bound (if this value is set to 1)/unbound (if this value is set to 0) to the device.
         * # is number [0, NUM_BIND_DEVICES)
         *
         * @since 856
         * INTEGER (boolean)
         */
        const val BIND_TO_DEVICE_PREFIX: String = "__bind_to_device_"

        /**
         * Prefix for virtual fields, used for insert/update contentValues.
         * Sets device type for device assignment. See [RouterConsts] DEVICE_* constants
         * Content values named [.BIND_TO_DEVICE_PREFIX]#, DEVICE_PREFIX, [.DEVICE_ADDRESS_PREFIX]#, [.DEVICE_NAME_PREFIX]#,
         * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
         * # is number [0, NUM_BIND_DEVICES)
         * @since 856
         * INTEGER
         */
        const val DEVICE_PREFIX: String = "__device_"

        /**
         * Prefix for virtual fields, used for insert/update contentValues. Sets device address for given device assignment. Device address may match device name,
         * but for BT / Chromecast device this is usually mac address or some other unique identifier.
         * Content values named [.BIND_TO_DEVICE_PREFIX]#, [.DEVICE_PREFIX]#, DEVICE_ADDRESS_PREFIX, [.DEVICE_NAME_PREFIX]#,
         * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
         * # is number [0, NUM_BIND_DEVICES)
         * @since 856
         * TEXT
         */
        const val DEVICE_ADDRESS_PREFIX: String = "__device_address_"

        /**
         * Prefix for virtual fields, used for insert/update contentValues. Sets visible device name for given device assignment.
         * Content values named [.BIND_TO_DEVICE_PREFIX], [.DEVICE_PREFIX], [.DEVICE_ADDRESS_PREFIX], DEVICE_NAME_PREFIX,
         * will be checked and if all set to appropriate valid values, preset will be bound/unmound to the device.
         * # is number [0, NUM_BIND_DEVICES)
         * @since 856
         * TEXT
         */
        const val DEVICE_NAME_PREFIX: String = "__device_name_"

        /**
         * Virtual field, used for insert/update contentValues. If set, preset is bound to that track
         * NOTE: track related assigments requires preset id in uri, for example uri path should end with /eq_presets/123
         * INTEGER
         */
        @Deprecated("see {@link #BIND_TO_TRACK} ")
        const val BIND_TO_TRACK_ID: String = "__bind_to_track_id"

        /**
         * Virtual field, used for insert/update contentValues. If set, preset is unbound from that track
         * NOTE: track related assignents requires preset id in uri, for example uri path should end with /eq_presets/123
         * INTEGER
         */
        @Deprecated("see {@link #BIND_TO_TRACK} ")
        const val UNBIND_FROM_TRACK_ID: String = "__unbind_to_track_id"

        /**
         * Virtual field, used for insert/update contentValues. If set, appropriate category uri (string==getMassOpsItemsUri(false)) is bound for this track
         * NOTE: track related assigments requires preset id in uri, for example uri path should end with /eq_presets/123
         * INTEGER (boolean)
         */
        @Deprecated("see {@link #BIND_TO_CAT}")
        const val BIND_TO_CAT_URI: String = "__bind_to_cat_uri"

        /**
         * Extra sort field
         * INTEGER
         * @since 906
         */
        @Deprecated("not used since 962")
        val SORT: String = TABLE + ".sort"
    }

    object EqPresetSongs {
        const val TABLE: String = "eq_preset_songs"

        const val _ID: String = TABLE + "._id"

        /**
         * Either folder_file_id
         * INTEGER
         */
        const val FILE_ID: String = TABLE + ".file_id"

        /**
         * Eq preset id
         * INTEGER
         */
        const val PRESET_ID: String = TABLE + ".preset_id"
    }

    object EqPresetDevices {
        const val TABLE: String = "eq_preset_devices"

        const val _ID: String = TABLE + "._id"

        /**
         * Eq preset id
         * INTEGER
         */
        const val PRESET_ID: String = TABLE + ".preset_id"

        /**
         * Device type
         * INTEGER
         */
        const val DEVICE: String = "device"

        /**
         * Device name
         * TEXT
         */
        const val DEVICE_NAME: String = "device_name"

        /**
         * Device address
         * TEXT
         */
        const val DEVICE_ADDRESS: String = "device_address"
    }

    /** @since 960
     */
    object KnownDevices {
        const val TABLE: String = "known_devices"

        const val _ID: String = TABLE + "._id"

        /**
         * Device name
         * TEXT
         */
        const val DEVICE_NAME: String = TABLE + ".device_name"
    }

    object ReverbPresets {
        const val TABLE: String = "reverb_presets"

        const val _ID: String = TABLE + "._id"

        /**
         * TEXT
         */
        const val _DATA: String = TABLE + "._data"

        /**
         * TEXT
         */
        const val NAME: String = TABLE + ".name"
    }


    /** @since 841
     */
    object PrefSearch {
        const val TABLE: String = "pref_search"
        const val _ID: String = TABLE + "._id"
        const val BREADCRUMB: String = "breadcrumb"
        const val PREF_URI: String = "pref_uri"
        const val PREF_KEY: String = "pref_key"
        const val ICON: String = "icon"
        const val TYPE: String = "type"
    }

    /** @since 841
     */
    object PrefSearchFts {
        const val TABLE: String = "pref_search_fts"
        const val DOCID: String = "docid"
        const val TITLE: String = "title"
        const val SUMMARY: String = "summary"
    }

    /**
     * Search history for the main (tracks) search
     * @since 907
     */
    // NOTE: avoid table name as these columns are shared
    open class SearchHistory {
        companion object {
            const val TABLE: String = "search_history"
            const val _ID: String = TABLE + "._id"
            const val TERM: String = "term"
            const val UPDATED_AT: String = "updated_at"
        }
    }

    /** @since 978
     */
    object SettingsSearchHistory : SearchHistory() {
        const val TABLE: String = "settings_search_history"
        const val _ID: String = TABLE + "._id"

        /** @since 979
         */
        const val URI: String = "uri"
    }

    /**
     * LRC files found during the file system/providers scan
     * @since 948
     */
    object LrcFiles {
        const val TABLE: String = "lrc_files"

        const val _ID: String = TABLE + "._id"

        /**
         * First seen time
         * Seconds
         * INTEGER NOT NULL
         */
        const val CREATED_AT: String = TABLE + ".created_at"

        /**
         * Time of update
         * Seconds
         * INTEGER NOT NULL
         */
        const val UPDATED_AT: String = TABLE + ".updated_at"

        /**
         * LRC file mtime
         * INTEGER NOT NULL
         */
        const val MTIME: String = TABLE + ".mtime"

        /**
         * Title extracted either from [ti:] tag or from the file name or NULL if none
         * TEXT NULL
         */
        const val TITLE: String = TABLE + ".title"

        /**
         * Artist extracted either from [ar:] tag or from the file name or NULL if none
         * TEXT NULL
         */
        const val ARTIST: String = TABLE + ".artist"

        /**
         * [al:] album tag contents or NULL if none
         * TEXT NULL
         */
        const val ALBUM: String = TABLE + ".album"

        /**
         * [length:] tag in milliseconds or NULL if none or 0
         * INTEGER NULL
         */
        const val LENGTH: String = TABLE + ".length"

        /**
         * Simple filename - the filename without path or extension
         * TEXT NON NULL
         */
        const val SIMPLE_FILENAME: String = TABLE + ".simple_filename"

        /**
         * File extension with the dot, e.g. ".lrc" or empty string if none
         * TEXT NON NULL
         */
        const val EXTENSION: String = TABLE + ".extension"

        /**
         * Parent folder path including the last /
         * TEXT NON NULL
         */
        const val FOLDER_PATH: String = TABLE + ".folder_path"

        /**
         * 1 if this file should be treated as utf8
         * BOOLEAN NON NULL
         */
        const val IS_UTF8: String = TABLE + ".is_utf8"

        /**
         * One of the TAG_* constants
         * INTEGER NOT NULL
         * @see com.maxmpz.poweramp.player.PowerampAPI.Track.TagStatus
         */
        const val TAG_STATUS: String = TABLE + ".tag_status"

        /**
         * Full path to the lrc file.
         * Calculated field
         */
        const val FULL_PATH: String = FOLDER_PATH + "||" + SIMPLE_FILENAME + "||" + EXTENSION
    }

    /**
     * The cached lyrics. Only lyrics from 3rd party plugins gets here.
     * LRC files and embedded/tag lyrics are always loaded from the respective LRC file or the track tag.
     * @since 948
     */
    object CachedLyrics {
        const val TABLE: String = "cached_lyrics"

        const val _ID: String = "$TABLE._id"

        /**
         * First seen time
         * Seconds
         * INTEGER NOT NULL
         */
        const val CREATED_AT: String = "$TABLE.created_at"

        /**
         * Time of update
         * Seconds
         * INTEGER NOT NULL
         */
        const val UPDATED_AT: String = "$TABLE.updated_at"

        /**
         * 3rd party plugin package, the source of the lyrics
         * TEXT NULL
         */
        const val CREATED_BY_PAK: String = "$TABLE.created_by_pak"

        /**
         * 3rd party plugin info line text, shown as last line in Poweramp lyrics. Can be copyright or other similar
         * additional short info text
         * TEXT NULL
         */
        const val INFO_LINE: String = "$TABLE.info_line"

        /**
         * Lyrics content or NULL if none.
         * NOTE: we may have NULL while lyrics is requested via the plugin, but we haven't received data from it yet
         * TEXT
         */
        const val CONTENT: String = "$TABLE.content"
    }

    /**
     * Alias used for category. Useful when query is actually a multi table join
     */
    const val CATEGORY_ALIAS: String = "cat"

    /**
     * Id used for all "unknown" categories. Also see [PowerampAPI.NO_ID]
     */
    const val UNKNOWN_ID: Long = 1000L

    /**
     * Alias used for category aliased table _id
     */
    const val CATEGORY_ALIAS_ID: String = "$CATEGORY_ALIAS._id"
}
