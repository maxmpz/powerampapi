/*
Copyright (C) 2011-2013 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with PowerAMP application on Android platform.

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

import android.net.Uri;


/**
 * Poweramp intent based API.
 */
public final class PowerampAPI {
	/**
	 * Defines PowerampAPI version, which could be also 200 and 210 for older Poweramps.
	 */
	public static final int VERSION = 526;
	
	/**
	 * No id flag.
	 */
	public static final int NO_ID = 0;
	
	public static final String AUTHORITY = "com.maxmpz.audioplayer.data";
	
	public static final Uri ROOT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY).build();
	
	/**
	 * Uri query parameter - filter.
	 */
	public static final String PARAM_FILTER = "flt";
	/**
	 * Uri query parameter - shuffle mode.
	 */
	public static final String PARAM_SHUFFLE = "shf";
	
	
	/**
	 * PowerAMP Control action.
	 * Should be sent with sendBroadcast().
	 * Extras:
	 * 	- cmd - int - command to execute.
	 */
	public static final String ACTION_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
	
	/**
	 * ACTION_API_COMMAND extra.
	 * Int.
	 */
	public static final String COMMAND = "cmd";
	
	
	public static final class Commands {
		/**
		 * Extras:
		 * - keepService - boolean - (optional) if true, Poweramp won't unload player service. Notification will be appropriately updated.
		 */
		public static final int TOGGLE_PLAY_PAUSE = 1;
		/**
		 * Extras:
		 * - keepService - boolean - (optional) if true, Poweramp won't unload player service. Notification will be appropriately updated.
		 */
		public static final int PAUSE = 2;
		public static final int RESUME = 3;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int NEXT = 4;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int PREVIOUS = 5;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int NEXT_IN_CAT = 6;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int PREVIOUS_IN_CAT = 7;
		/**
		 * Extras:
		 * - showToast - boolean - (optional) if false, no toast will be shown. Applied for cycle only.
		 * - repeat - int - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled, see Repeat class.
		 */
		public static final int REPEAT = 8;
		/**
		 * Extras:
		 * - showToast - boolean - (optional) if false, no toast will be shown. Applied for cycle only.
		 * - shuffle - int - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled, see Shuffle class.
		 */
		public static final int SHUFFLE = 9;
		public static final int BEGIN_FAST_FORWARD = 10;
		public static final int END_FAST_FORWARD = 11;
		public static final int BEGIN_REWIND = 12;
		public static final int END_REWIND = 13;
		public static final int STOP = 14;
		/**
		 * Extras:
		 * - pos - int - seek position in seconds.
		 */
		public static final int SEEK = 15;
		public static final int POS_SYNC = 16;
		
		/**
		 * Data:
		 * - uri, following URIs are recognized:
		 * 	- file://path
		 * 	- content://com.maxmpz.audioplayer/FOLDER/<folder_id>/<file_id>
		 * 	- content://com.maxmpz.audioplayer/FOLDER/<root_folder_id>/<folder_id>/<file_id>?hier=true   (hierarchy mode)
		 * 	- content://com.maxmpz.audioplayer/FOLDER_PLAYLIST/<folder_playlist_id>/<folder_playlist_entry_id>
		 * 	- content://com.maxmpz.audioplayer/ALL/<lib_media_id>
		 *  - content://com.maxmpz.audioplayer/ARTIST/<lib_artist_id>/<lib_media_id>
		 * 	- content://com.maxmpz.audioplayer/ALBUM/<lib_album_id>/<lib_media_id>
		 * 	- content://com.maxmpz.audioplayer/ARTIST_ALBUM/<lib_artist_id>/<lib_album_id>/<lib_media_id>
		 *  - content://com.maxmpz.audioplayer/GENRE_ALBUM/<lib_genre_id>/<lib_album_id>/<lib_media_id>
		 * 	- content://com.maxmpz.audioplayer/GENRE/<lib_genre_id>/<lib_media_id>
		 * 	- content://com.maxmpz.audioplayer/PLAYLIST/<lib_playlist_id>/<lib_playlist_entry_id>
		 * 	- content://com.maxmpz.audioplayer/QUEUE/<queue_entry_id>
		 * 
		 * PowerAMP plays whole list starting from the specified song (for SONG uri) or from first one or random one in shuffle mode (for LIST uri).
		 * If last Uri segment exists (file/song/entry id) - it's SONG uri. 
		 * If last song/entry id segment doesn't exist - it's a LIST uri. 
		 * 
		 * Example SONG uri:          content://com.maxmpz.audioplayer/FOLDER/12/3
		 * Example LIST (folder) uri: content://com.maxmpz.audioplayer/FOLDER/12
		 * Example LIST (hier folder, with root folder=55, folder to play=12, no file id) uri: 
		 * 							  content://com.maxmpz.audioplayer/FOLDER/55/12?hier=true
		 *  
		 * Extras:
		 * - shuffle - int - (optional) apply appropriate shuffle mode to the list. Correct shuffle mode should be selected:
		 *                    to shuffle just folder - Shuffle.SHUFFLE_FOLDER, to shuffle in folder hierarchy - Shuffle.SHUFFLE_HIER (only in hierarchy mode)
		 *                    to shuffle any other list - Shuffle.SHUFFLE_CAT
		 *                    Can't be applied to file:// or SONG uri.
		 * 
		 * - filter - String - (optional) option text filtering to apply to the list which is played.
		 * 					  Can't be applied to file:// or SONG uri.
		 * 
		 * - paused - boolean - (optional) default false. OPEN_TO_PLAY command starts playing the file immediately, unless "paused" extra is true.
		 *                    Can be applied only to file:// and SONG uri.
		 * 
		 * - pos - int - (optional) seek to this position in song before playing
		 * 					  Can be applied only to file:// and SONG uri.
		 * 
		 * - matchFile - boolean - (optional) default true. If true, PowerAMP will try to match file://path against Folder Files database, and if match found,
		 *                    PowerAMP will play that file based on matched folder/file ids. Otherwise just file is played (no list loaded, Category=NONE_RAW_FILE).
		 *                    Can be applied only to file:// uri. 
		 *  
		 */
		public static final int OPEN_TO_PLAY = 20;
		
		/**
		 * Extras:
		 * - id - long - preset ID
		 */
		public static final int SET_EQU_PRESET = 50;

		/**
		 * Extras:
		 * - value - string - equalizer values, see ACTION_EQU_CHANGED description.
		 */
		public static final int SET_EQU_STRING = 51;

		/**
		 * Extras:
		 * - name - string - equalizer band (bass/treble/preamp/31/62../8K/16K) name
		 * - value - float - equalizer band value (bass/treble/, 31/62../8K/16K => -1.0...1.0, preamp => 0..2.0)
		 */
		public static final int SET_EQU_BAND = 52;
		
		/**
		 * Extras:
		 * - equ - boolean - if exists and true, equalizer is enabled
		 * - tone - boolean - if exists and true, tone is enabled
		 */
		public static final int SET_EQU_ENABLED = 53;
		
		/**
		 * Used by Notification controls to stop pending/paused service/playback and unload/remove notification.
		 * Since 2.0.6
		 */
		public static final int STOP_SERVICE = 100;
	}
	
	/**
	 * Extra.
	 * Mixed.
	 */
	public static final String API_VERSION = "api";

	/**
	 * Extra.
	 * Mixed.
	 */
	public static final String CONTENT = "content";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String PACKAGE = "pak";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String LABEL = "label";
	
	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String AUTO_HIDE = "autoHide";
	
	/**
	 * Extra.
	 * Bitmap.
	 */
	public static final String ICON = "icon";

	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String MATCH_FILE = "matchFile";
	
	/**
	 * Extra.
	 * Boolean
	 */
	public static final String SHOW_TOAST = "showToast";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String NAME = "name";

	/**
	 * Extra.
	 * Mixed.
	 */
	public static final String VALUE = "value";
	
	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String EQU = "equ";

	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String TONE = "tone";
	
	/**
	 * Extra.
	 * Boolean.
	 * Since 2.0.6
	 */
	public static final String KEEP_SERVICE = "keepService"; 
	
	
	/**
	 * PowerAMP track changed.
	 * Sticky intent.
	 * Extras:
	 * - track - bundle - Track bundle, see Track class.
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 *  Note, that by default PowerAMP won't search/download album art when screen is OFF, but will do that on next screen ON event.  
	 */
	public static final String ACTION_TRACK_CHANGED = "com.maxmpz.audioplayer.TRACK_CHANGED";
	
	/**
	 * Album art was changed. Album art can be the same for whole album/folder, thus usually it will be updated less frequently comparing to TRACK_CHANGE.
	 * If both aaPath and aaBitmap extras are missing that means no album art exists for the current track(s).
	 * Note that there is no direct Album Art to track relation, i.e. both track and album art can change independently from each other -
	 * for example - when new album art asynchronously downloaded from internet or selected by user.
	 * Sticky intent.
	 * Extras:
	 * - aaPath - String - (optional) if exists, direct path to the cached album art is available.
	 * - aaBitmap - Bitmap - (optional)	if exists, some rescaled up to 500x500 px album art bitmap is available.
	 *              There will be aaBitmap if aaPath is available, but image is bigger than 600x600 px.
	 * - delayed - boolean - (optional) if true, this album art was downloaded or selected later by user.
              
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 */
	public static final String ACTION_AA_CHANGED = "com.maxmpz.audioplayer.AA_CHANGED";

	/**
	 * PowerAMP playing status changed (track started/paused/resumed/ended, playing ended).
	 * Sticky intent.
	 * Extras: 
	 * - status - string - one of the STATUS_* values
	 * - pos - int - (optional) current in-track position in seconds.
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 * - additional extras - depending on STATUS_ value (see STATUS_* description below).
	 */
	public static final String ACTION_STATUS_CHANGED = "com.maxmpz.audioplayer.STATUS_CHANGED";
	
	/**
	 * NON sticky intent.
	 * - pos - int - current in-track position in seconds. 
	 */
	public static final String ACTION_TRACK_POS_SYNC = "com.maxmpz.audioplayer.TPOS_SYNC";
	
	/**
	 * PowerAMP repeat or shuffle mode changed.
	 * Sticky intent.
	 * Extras:
	 * - repeat - int - new repeat mode. See RepeatMode class.
	 * - shuffle - int - new shuffle mode. See ShuffleMode class.
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).	 * 
	 */
	public static final String ACTION_PLAYING_MODE_CHANGED = "com.maxmpz.audioplayer.PLAYING_MODE_CHANGED";

	/**
	 * PowerAMP equalizer settings changed.
	 * Sticky intent.
	 * Extras:
	 * - name - string - preset name. If no name extra exists, it's not a preset.
	 * - id - long - preset id. If no id extra exists, it's not a preset.
	 * - value - string - equalizer and tone values in format:
	 *   	bass=pos_float|treble=pos_float|31=float|62=float|....|16K=float|preamp=0.0 ... 2.0
	 *      where float = -1.0 ... 1.0, pos_float = 0.0 ... 1.0
	 * - equ - boolean - true if equalizer bands are enabled
	 * - tone - boolean - truel if tone bands are enabled
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 */
	public static final String ACTION_EQU_CHANGED = "com.maxmpz.audioplayer.EQU_CHANGED";
	
	/**
	 * Special actions for com.maxmpz.audioplayer.PlayerUIActivity only.
	 */
	public static final String ACTION_SHOW_CURRENT = "com.maxmpz.audioplayer.ACTION_SHOW_CURRENT";
	public static final String ACTION_SHOW_LIST  = "com.maxmpz.audioplayer.ACTION_SHOW_LIST";
	

	public static final String PACKAGE_NAME = "com.maxmpz.audioplayer";
	
	public static final String ACTIVITY_PLAYER_UI = "com.maxmpz.audioplayer.PlayerUIActivity";
	public static final String ACTIVITY_EQ = "com.maxmpz.audioplayer.EqActivity";
	
	/**
	 * If com.maxmpz.audioplayer.ACTION_SHOW_LIST action is sent to this activity, it will react to some extras.
	 * Extras:
	 * Data:
	 * - uri - uri of the list to display. 
	 */
	public static final String ACTIVITY_PLAYLIST = "com.maxmpz.audioplayer.PlayListActivity";
	public static final String ACTIVITY_SETTINGS = "com.maxmpz.audioplayer.preference.SettingsActivity";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String ALBUM_ART_PATH = "aaPath";
	
	/**
	 * Extra.
	 * Bitmap. 
	 */
	public static final String ALBUM_ART_BITMAP = "aaBitmap";

	/**
	 * Extra.
	 * boolean. 
	 */
	public static final String DELAYED = "delayed";


	/**
	 * Extra.
	 * long.
	 */
	public static final String TIMESTAMP = "ts";
	
	/**
	 * STATUS_CHANGED extra. See Status class for values.
	 * Int.
	 */
	public static final String STATUS = "status";	

	/**
	 * STATUS extra values.
	 */
	public static final class Status {
		/**
		 * STATUS_CHANGED status value - track has been started to play or has been paused.
		 * Note that PowerAMP will start track immediately into this state when it's just loaded to avoid STARTED => PAUSED transition. 
		 * Additional extras:
		 * 	track - bundle - track info 
		 * 	paused - boolean - true if track paused, false if track resumed
		 */
		public static final int TRACK_PLAYING = 1;
		
		/**
		 * STATUS_CHANGED status value - track has been ended. Note, this intent will NOT be sent for just finished song IF PowerAMP advances to the next song.
		 * Additional extras:
		 * 	track - bundle - track info 
		 *  failed - boolean - true if track failed to play
		 */
		public static final int TRACK_ENDED = 2;

		/**
		 * STATUS_CHANGED status value - PowerAMP finished playing some list and stopped.
		 */
		public static final int PLAYING_ENDED = 3;
	}
	
	
	/**
	 * STATUS_CHANGED trackEnded extra. 
	 * Boolean. True if track failed to play.
	 */
	public static final String FAILED = "failed";

	/**
	 * STATUS_CHANGED trackStarted/trackPausedResumed extra.
	 * Boolean. True if track is paused.
	 */
	public static final String PAUSED = "paused";
	
	/**
	 * PLAYING_MODE_CHANGED extra. See ShuffleMode class.
	 * Integer.
	 */
	public static final String SHUFFLE = "shuffle";

	/**
	 * PLAYING_MODE_CHANGED extra. See RepeatMode class.
	 * Integer.
	 */
	public static final String REPEAT = "repeat";
	
	
	/**
	 * Extra.
	 * Long.
	 */
	public static final String ID = "id";

	/**
	 * STATUS_CHANGED track extra.
	 * Bundle.
	 */
	public static final String TRACK = "track";
	
	
	/**
	 * shuffle extras values.
	 */
	public static final class ShuffleMode {
		public static final int SHUFFLE_NONE = 0;
		public static final int SHUFFLE_ALL = 1;
		public static final int SHUFFLE_SONGS = 2;
		public static final int SHUFFLE_CATS = 3; // Songs in order.
		public static final int SHUFFLE_SONGS_AND_CATS = 4; // Songs shuffled.
	}
	
	/**
	 * repeat extras values.
	 */
	public static final class RepeatMode {
		public static final int REPEAT_NONE = 0;
		public static final int REPEAT_ON = 1;
		public static final int REPEAT_ADVANCE = 2;
		public static final int REPEAT_SONG = 3;	
	}
	
	
	/**
	 * STATUS_CHANGED track extra fields.
	 */
	public static final class Track {
		/**
		 * Id of the current track.
		 * Can be a playlist entry id.
		 * Long.
		 */
		public static final String ID = "id";
		
		/**
		 * "Real" id. In case of playlist entry, this is always resolved to PowerAMP folder_files table row ID or System Library MediaStorage.Audio._ID. 
		 * Long.
		 */
		public static final String REAL_ID = "realId";
		
		/**
		 * Category type.
		 * See Track.Type class.
		 * Int.
		 */
		public static final String TYPE = "type";
		
		/**
		 * Category URI match.
		 * Int.
		 */
		public static final String CAT = "cat";
		
		/**
		 * Boolean.
		 */
		public static final String IS_CUE = "isCue";
		
		/**
		 * Category URI.
		 * Uri.
		 */
		public static final String CAT_URI = "catUri";
		
		/**
		 * File type. See Track.FileType.
		 * Integer.
		 */
		public static final String FILE_TYPE = "fileType";
	
		/**
		 * Song file path.
		 * String
		 */
		public static final String PATH = "path";
		
		/**
		 * Song title.
		 * String
		 */
		public static final String TITLE = "title";
		
		/**
		 * Song album.
		 * String.
		 */
		public static final String ALBUM = "album";
		
		/**
		 * Song artist.
		 * String.
		 */
		public static final String ARTIST = "artist";
		
		/**
		 * Song duration in seconds.
		 * Int.
		 */
		public static final String DURATION = "dur";
		
		/**
		 * Position in song in seconds.
		 * Int.
		 */
		public static final String POSITION = "pos";
		
		/**
		 * Position in a list.
		 * Int.
		 */
		public static final String POS_IN_LIST = "posInList";

		/**
		 * List size.
		 * Int.
		 */
		public static final String LIST_SIZE = "listSize";

		/**
		 * Song sample rate.
		 * Int.
		 */
		public static final String SAMPLE_RATE = "sampleRate";
		
		/**
		 * Song number of channels.
		 * Int.
		 */
		public static final String CHANNELS = "channels";
		
		/**
		 * Song average bitrate.
		 * Int.
		 */
		public static final String BITRATE = "bitRate";
		
		/**
		 * Resolved codec name for the song.
		 * Int.
		 */
		public static final String CODEC = "codec";
		
		/**
		 * Track flags.
		 * Int.
		 */
		public static final String FLAGS = "flags";
		
		/**
		 * Track.fileType values.
		 */
		public static final class FileType {
			public static final int mp3 = 0; 
			public static final int flac = 1;
			public static final int m4a = 2;
			public static final int mp4 = 3;
			public static final int ogg = 4;
			public static final int wma = 5;
			public static final int wav = 6;
			public static final int tta = 7;
			public static final int ape = 8;
			public static final int wv = 9;
			public static final int aac = 10;
			public static final int mpga = 11;
			public static final int amr = 12;
			public static final int _3gp = 13;
			public static final int mpc = 14;
			public static final int aiff = 15;
			public static final int aif = 16;
		}
		
		/**
		 * Track.flags bitset values. First 3 bits = FLAG_ADVANCE_* 
		 */
		public static final class Flags {
			public static final int FLAG_ADVANCE_NONE = 0;
			public static final int FLAG_ADVANCE_FORWARD = 1;
			public static final int FLAG_ADVANCE_BACKWARD = 2;
			public static final int FLAG_ADVANCE_FORWARD_CAT = 3;
			public static final int FLAG_ADVANCE_BACKWARD_CAT = 4;
			
			public static final int FLAG_ADVANCE_MASK = 0x7; // 111
			
			public static final int FLAG_NOTIFICATION_UI = 0x20;
			public static final int FLAG_FIRST_IN_PLAYER_SESSION = 0x40; // Currently used just to indicate that track is first in playerservice session.			
		}
	}
	
	public static final class Cats {
		public static final int ROOT = 0;
		public static final int FOLDERS = 10;
		public static final int GENRES_ID_ALBUMS = 210;
		public static final int ALBUMS = 200;
		public static final int GENRES = 320;
		public static final int ARTISTS = 500;
		public static final int ARTISTS_ID_ALBUMS = 220;		
		public static final int ARTISTS__ALBUMS = 250;
		public static final int COMPOSERS = 600;
		public static final int COMPOSERS_ID_ALBUMS = 230;
		public static final int PLAYLISTS = 100;
		public static final int QUEUE = 800;
		public static final int MOST_PLAYED = 43;
		public static final int TOP_RATED = 48;
		public static final int RECENTLY_ADDED = 53;
		public static final int RECENTLY_PLAYED = 58;
	}
	
}
