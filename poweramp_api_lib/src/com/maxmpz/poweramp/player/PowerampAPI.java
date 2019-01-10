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

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;


/**
 * Poweramp Intent based API.
 */
public final class PowerampAPI {
	/**
	 * Defines PowerampAPI version
	 */
	public static final int VERSION = 800;

	/**
	 * No id value (for id-related fields, for example, {@link PowerampAPI.Track.ID})
	 */
	public static final long NO_ID = 0L;

	/**
	 * Special {@link PowerampAPI.Track.ID} value indicating raw file - file opened from some file manager, which can't be matched against Poweramp database
	 */
	public static final long RAW_TRACK_ID = -2L;

	/**
	 * Authority used for data provider
	 */
	public static final String AUTHORITY = "com.maxmpz.audioplayer.data";

	/**
	 * Root data provider uri
	 */
	public static final Uri ROOT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY).build();

	/**
	 * Authority used for album art provider
	 */
	public static final String AA_AUTHORITY = "com.maxmpz.audioplayer.aa";
	/**
	 * Root album art provider uri
	 */
	public static final Uri AA_ROOT_URI = new Uri.Builder().scheme("content").authority(AA_AUTHORITY).build();

	/**
	 * AA_AUTHORITY accepted parameter - get HD image. Default is true.
	 */
	public static final String PARAM_AA_HD = "hd";

	/**
	 * AA_AUTHORITY accepted parameter - try to download image. Default is false.
	 */
	public static final String PARAM_AA_DOWNLOAD = "dl";
	
	/**
	 * Uri query parameter - filter. Currently used only for search uri
	 */
	public static final String PARAM_FILTER = "flt";

	/**
	 * Uri query parameter - shuffle mode
	 */
	public static final String PARAM_SHUFFLE = "shf";

	/**
	 * Poweramp Control action.
	 * Extras:<br>
	 * {@code int cmd} - command to execute. See {@link #COMMAND}, 
	 * {@link #PACKAGE} - optional - the command issuing plugin/app package name - for debugging purposes
	 * {@link #SOURCE} - optional - the source of command, e.g. "widget", "UI", etc. - for debugging purposes
	 */
	public static final String ACTION_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";

	/**
	 * Poweramp package name
	 */
	public static final String PACKAGE_NAME = "com.maxmpz.audioplayer";

	/**
	 * Poweramp service name
	 */
	public static final String PLAYER_SERVICE_NAME = "com.maxmpz.audioplayer.player.PlayerService";

	/**
	 * Poweramp service ComponentName
	 */
	public static final ComponentName PLAYER_SERVICE_COMPONENT_NAME = new ComponentName(PACKAGE_NAME, PLAYER_SERVICE_NAME);

	/**
	 * @return ready to use Intent for Poweramp service
	 */
	public static Intent newAPIIntent() {
		return new Intent(ACTION_API_COMMAND).setComponent(PLAYER_SERVICE_COMPONENT_NAME);
	}

	/**
	 * ACTION_API_COMMAND extra
	 * <br>
	 * {@code int}
	 */
	public static final String COMMAND = "cmd";


	/**
	 * Common extras:
	 * <br>
	 *
	 */
	@SuppressWarnings("hiding")
	public static final class Commands {
		/**
		 * Extras:<br>
		 * {@code boolean keepService} - (optional) if true, Poweramp won't unload player service. Notification will be appropriately updated<br>
		 * {@code boolean beep} - (optional) if true, Poweramp will beep on playback command
		 */
		public static final int TOGGLE_PLAY_PAUSE = 1;
		/**
		 * Extras:<br>
		 * {@code boolean keepService} - (optional) if true, Poweramp won't unload player service. Notification will be appropriately updated<br>
		 * {@code boolean beep} - (optional) if true, Poweramp will beep on playback command
		 */
		public static final int PAUSE = 2;
		/**
		 * Extras:<br>
		 * {@code int shuffle} - (optional) if set, shuffle mode to set (even if Poweramp is already playing)
		 */
		public static final int RESUME = 3;
		/**
		 * NOTE: subject to 200ms throttling
		 */
		public static final int NEXT = 4;
		/**
		 * NOTE: subject to 200ms throttling
		 */
		public static final int PREVIOUS = 5;
		/**
		 * NOTE: subject to 200ms throttling
		 */
		public static final int NEXT_IN_CAT = 6;
		/**
		 * NOTE: subject to 200ms throttling
		 */
		public static final int PREVIOUS_IN_CAT = 7;
		/**
		 * Extras:<br>
		 * {@code boolean showToast} - (optional) if false, no toast will be shown. Applied for cycle only<br>
		 * {@code int repeat} - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled
		 * @see PowerampAPI.RepeatMode
		 */
		public static final int REPEAT = 8;
		/**
		 * Extras:<br>
		 * {@code boolean showToast} - (optional) if false, no toast will be shown. Applied for cycle only<br>
		 * {@code int shuffle} - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled
		 * @see PowerampAPI.ShuffleMode
		 */
		public static final int SHUFFLE = 9;
		/**
		 * Poweramp starts constantly seeking forward until {@link #END_FAST_FORWARD} received
		 */
		public static final int BEGIN_FAST_FORWARD = 10;
		/**
		 * Stops #BEGIN_FAST_FORWARD
		 */
		public static final int END_FAST_FORWARD = 11;
		/**
		 * Poweramp starts constantly seeking backward until {@link #END_REWIND} received
		 */
		public static final int BEGIN_REWIND = 12;
		/**
		 * Stops {@link #BEGIN_REWIND}
		 */
		public static final int END_REWIND = 13;
		public static final int STOP = 14;
		/**
		 * Extras:<br>
		 * {@code int pos} - seek position in seconds
		 */
		public static final int SEEK = 15;
		/**
		 * Request for Poweramp current track position. In response, {@link #ACTION_TRACK_POS_SYNC} is sent
		 */
		public static final int POS_SYNC = 16;

		/**
		 * Data:<br>
		 * - uri, following URIs are recognized:<br>
		 * 	- file://path<br>
		 * 	- content://com.maxmpz.audioplayer.data/... (see below)<br><br>
		 *
		 * # means some numeric id (track id for queries ending with /files, otherwise - appropriate category id).<br>
		 * If track id (in place of #) is not specified, Poweramp plays whole list starting from the specified track,<br>
		 * or from first one, or from random one in shuffle mode.<br><br>
		 *
		 * All queries support following params (added as URL encoded params, e.g. content://com.maxmpz.audioplayer.data/files?lim=10&flt=foo):<br>
		 * {@code int lim} - SQL LIMIT, which limits number of rows returned<br>
		 * {@code String flt} - filter substring. Poweramp will return only matching rows (the same way as returned in Poweramp lists UI when filter is used).<br>
		 * {@code int shf} - shuffle mode (see ShuffleMode class)<br>
		 * {@code int shs} - 1 if this is shuffle session (for internal use)<br><br>
		 <pre>
		 - All tracks:
		 content://com.maxmpz.audioplayer.data/files
		 content://com.maxmpz.audioplayer.data/files/#

		 - Most Played
		 content://com.maxmpz.audioplayer.data/most_played
		 content://com.maxmpz.audioplayer.data/most_played/#

		 - Top Rated
		 content://com.maxmpz.audioplayer.data/top_rated
		 content://com.maxmpz.audioplayer.data/top_rated/#

		 - Recently Added
		 content://com.maxmpz.audioplayer.data/recently_added
		 content://com.maxmpz.audioplayer.data/recently_added/#

		 - Recently Played
		 content://com.maxmpz.audioplayer.data/recently_played
		 content://com.maxmpz.audioplayer.data/recently_played/#

		 - Long
		 content://com.maxmpz.audioplayer.data/long
		 content://com.maxmpz.audioplayer.data/long/#

		 - Plain folders view (just files in plain folders list)
		 content://com.maxmpz.audioplayer.data/folders
		 content://com.maxmpz.audioplayer.data/folders/#
		 content://com.maxmpz.audioplayer.data/folders/#/files
		 content://com.maxmpz.audioplayer.data/folders/#/files/#

		 - Hierarchy folders view (files and folders intermixed in one cursor)
		 content://com.maxmpz.audioplayer.data/folders/#/folders_and_files
		 content://com.maxmpz.audioplayer.data/folders/#/folders_and_files/#
		 content://com.maxmpz.audioplayer.data/folders/files // All folder files, sorted as folders_files sort (for mass ops).

		 - Genres
		 content://com.maxmpz.audioplayer.data/genres
		 content://com.maxmpz.audioplayer.data/genres/#/files
		 content://com.maxmpz.audioplayer.data/genres/#/files/#
		 content://com.maxmpz.audioplayer.data/genres/files

		 - Artists
		 content://com.maxmpz.audioplayer.data/artists
		 content://com.maxmpz.audioplayer.data/artists/#
		 content://com.maxmpz.audioplayer.data/artists/#/files
		 content://com.maxmpz.audioplayer.data/artists/#/files/#
		 content://com.maxmpz.audioplayer.data/artists/files

		 - Composers
		 content://com.maxmpz.audioplayer.data/composers
		 content://com.maxmpz.audioplayer.data/composers/#
		 content://com.maxmpz.audioplayer.data/composers/#/files
		 content://com.maxmpz.audioplayer.data/composers/#/files/#
		 content://com.maxmpz.audioplayer.data/composers/files

		 - Albums
		 content://com.maxmpz.audioplayer.data/albums
		 content://com.maxmpz.audioplayer.data/albums/#/files
		 content://com.maxmpz.audioplayer.data/albums/#/files/#
		 content://com.maxmpz.audioplayer.data/albums/files

		 - Album Artists
		 content://com.maxmpz.audioplayer.data/album_artists
		 content://com.maxmpz.audioplayer.data/album_artists/#/files
		 content://com.maxmpz.audioplayer.data/album_artists/#/files/#
		 content://com.maxmpz.audioplayer.data/album_artists/files

		 - Albums by Genres
		 content://com.maxmpz.audioplayer.data/genres/#/albums
		 content://com.maxmpz.audioplayer.data/genres/#/albums/#/files
		 content://com.maxmpz.audioplayer.data/genres/#/albums/#/files/#
		 content://com.maxmpz.audioplayer.data/genres/#/albums/files
		 content://com.maxmpz.audioplayer.data/genres/albums

		 - Albums by Artists
		 content://com.maxmpz.audioplayer.data/artists/#/albums
		 content://com.maxmpz.audioplayer.data/artists/#/albums/#/files
		 content://com.maxmpz.audioplayer.data/artists/#/albums/#/files/#
		 content://com.maxmpz.audioplayer.data/artists/#/albums/files
		 content://com.maxmpz.audioplayer.data/artists/albums

		 - Albums by Composers
		 content://com.maxmpz.audioplayer.data/composers/#/albums
		 content://com.maxmpz.audioplayer.data/composers/#/albums/#/files
		 content://com.maxmpz.audioplayer.data/composers/#/albums/#/files/#
		 content://com.maxmpz.audioplayer.data/composers/#/albums/files
		 content://com.maxmpz.audioplayer.data/composers/albums

		 - Albums by Artist
		 content://com.maxmpz.audioplayer.data/artists_albums
		 content://com.maxmpz.audioplayer.data/artists_albums/#/files
		 content://com.maxmpz.audioplayer.data/artists_albums/#/files/#
		 content://com.maxmpz.audioplayer.data/artists_albums/files

		 - Playlists
		 content://com.maxmpz.audioplayer.data/playlists
		 content://com.maxmpz.audioplayer.data/playlists/#
		 content://com.maxmpz.audioplayer.data/playlists/#/files
		 content://com.maxmpz.audioplayer.data/playlists/#/files/#
		 content://com.maxmpz.audioplayer.data/playlists/files

		 - Search
		 content://com.maxmpz.audioplayer.data/search?flt=search string

		 - Equalizer Presets
		 content://com.maxmpz.audioplayer.data/eq_presets
		 content://com.maxmpz.audioplayer.data/eq_presets/#

		 - Reverb Presets
		 content://com.maxmpz.audioplayer.data/reverb_presets
		 content://com.maxmpz.audioplayer.data/reverb_presets/#

		 - Queue
		 content://com.maxmpz.audioplayer.data/queue
		 content://com.maxmpz.audioplayer.data/queue/#
		 </pre><br>

		 * Extras<br>
		 * {@code boolean paused} - (optional) default false. OPEN_TO_PLAY command starts playing the file immediately, unless "paused" extra is true<br>
		 * {@code int pos}- (optional) seek to this position in track before playing
		 * @see PowerampAPI.Track.POSITION
		 * @see PowerampAPI.PAUSED
		 */
		public static final int OPEN_TO_PLAY = 20;

		/**
		 * Extras:<br>
		 * {@code long id} - preset ID
		 */
		public static final int SET_EQU_PRESET = 50;

		/**
		 * Extras:<br>
		 * {@code String value} - equalizer values,
		 * @see PowerampAPI.ACTION_EQU_CHANGED
		 */
		public static final int SET_EQU_STRING = 51;

		/**
		 * Extras:<br>
		 * {@code String name} - equalizer band (bass/treble/preamp/31/62../8K/16K) name
		 * {@code float value} - equalizer band value (bass/treble/, 31/62../8K/16K => -1.0...1.0, preamp => 0..2.0)
		 */
		public static final int SET_EQU_BAND = 52;

		/**
		 * Extras:<br>
		 * {@code boolean equ}- if exists and true, equalizer is enabled
		 * {@code boolean tone} - if exists and true, tone is enabled
		 */
		public static final int SET_EQU_ENABLED = 53;

		/**
		 * Used by Notification controls to stop pending/paused service/playback and unload/remove notification
		 */
		public static final int STOP_SERVICE = 100;
		
		
		public static String cmdToString(int cmd) {
			switch(cmd) {
				case TOGGLE_PLAY_PAUSE:
					return "TOGGLE_PLAY_PAUSE";
				case PAUSE:
					return "PAUSE";
				case RESUME:
					return "RESUME";
				case NEXT:
					return "NEXT";
				case PREVIOUS:
					return "PREVIOUS";
				case NEXT_IN_CAT:
					return "NEXT_IN_CAT";
				case PREVIOUS_IN_CAT:
					return "PREVIOUS_IN_CAT";
				case REPEAT:
					return "REPEAT";
				case SHUFFLE:
					return "SHUFFLE";
				case BEGIN_FAST_FORWARD:
					return "BEGIN_FAST_FORWARD";
				case END_FAST_FORWARD:
					return "END_FAST_FORWARD";
				case BEGIN_REWIND:
					return "BEGIN_REWIND";
				case END_REWIND:
					return "END_REWIND";
				case STOP:
					return "STOP";
				case SEEK:
					return "SEEK";
				case POS_SYNC:
					return "POS_SYNC";
				case OPEN_TO_PLAY:
					return "OPEN_TO_PLAY";
				case SET_EQU_PRESET:
					return "SET_EQU_PRESET";
				case SET_EQU_STRING:
					return "SET_EQU_STRING";
				case SET_EQU_BAND:
					return "SET_EQU_BAND";
				case SET_EQU_ENABLED:
					return "SET_EQU_ENABLED";
				case STOP_SERVICE:
					return "STOP_SERVICE";
				default:
					return "unknown cmd=" + cmd;
			}
		}
	}

	/**
	 * Minimum allowed time between seek commands
	 */
	public static int MIN_TIME_BETWEEN_SEEKS_MS = 200;

	/**
	 * Extra<br>
	 * {@code Mixed}
	 */
	public static final String API_VERSION = "api";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 * @Deprecated not used now
	 */
	@Deprecated
	public static final String CONTENT = "content";

	/**
	 * Extra<br>
	 * {@code String}
	 */
	public static final String PACKAGE = "pak";

	/**
	 * ACTION_API_COMMAND extra
	 * <br>
	 * {@code int}
	 */
	public static final String SOURCE = "src";

	/**
	 * Extra<br>
	 * {@code String}
	 * @Deprecated not used now
	 */
	@Deprecated
	public static final String LABEL = "label";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @Deprecated not used now
	 */
	@Deprecated
	public static final String AUTO_HIDE = "autoHide";

	/**
	 * Extra<br>
	 * {@code Bitmap}
	 * @Deprecated not used now
	 */
	@Deprecated
	public static final String ICON = "icon";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @Deprecated not used now
	 */
	@Deprecated
	public static final String MATCH_FILE = "matchFile";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String SHOW_TOAST = "showToast";

	/**
	 * Extra<br>
	 * {@code String}
	 */
	public static final String NAME = "name";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 */
	public static final String VALUE = "value";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String EQU = "equ";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String TONE = "tone";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String KEEP_SERVICE = "keepService";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String BEEP = "beep";


	/**
	 * Extra<br>
	 * {@code String}
	 * @since 795
	 */
	public static final String TABLE = "table";

	/**
	 * Poweramp track changed.<br>
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br>
	 * Use *_EXPLICIT version to receive this action in background app.<br><br>
	 * Extras:<br>
	 * {@code Bundle track} - Track bundle<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 * @see PowerampAPI.Track
	 */
	public static final String ACTION_TRACK_CHANGED = "com.maxmpz.audioplayer.TRACK_CHANGED";

	/**
	 * This is explicit intent sent to your app to ensure it receives it on Android 8+ (with background execution limitations).<br>
	 * Differs from ACTION_TRACK_CHANGED which is sticky intent which won't be received by your app in the background.<br><br>
	 *
	 * <b>NOTE: Poweramp caches app list for this intent. Cache is updated when Poweramp is started or playback resumed.</b><br>
	 * This means if your app just installed and Poweramp is playing, your app won't receive this action until next Poweramp pause/resume cycle or Poweramp service restart.<br><br>
	 *
	 * Extras:<br>
	 * {@code Bundle track} - Track bundle<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 * @see PowerampAPI.Track
	 * @since 798
	 */
	public static final String ACTION_TRACK_CHANGED_EXPLICIT = "com.maxmpz.audioplayer.TRACK_CHANGED_EXPLICIT";

	/**
	 * Album art was changed. Album art can be the same for whole album/folder, thus usually it will be updated less frequently comparing to TRACK_CHANGE.
	 * If both aaPath and aaBitmap extras are missing that means no album art exists for the current track(s).<br>
	 * Note that there is no direct Album Art to track relation, i.e. both track and album art can change independently from each other -
	 * for example - when new album art asynchronously downloaded from internet or selected by user.<br><br>
	 *
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br><br>
	 * Extras:<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 */
	public static final String ACTION_AA_CHANGED = "com.maxmpz.audioplayer.AA_CHANGED";

	/**
	 * Poweramp playing state changed (paused/resumed/ended).<br>
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br>
	 * Use *_EXPLICIT version to receive this action in background app.<br><br>
	 *
	 * Extras:<br>
	 * {@code int state} - one of the STATE_* values (700+)<br>
	 * {@code boolean paused} - true if track paused/stopped, false if track is playing<br>
	 * {@code int pos} - (optional) current in-track position in seconds<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())<br>
	 * {@code int status} - one of the STATUS_* values (deprecated)<br><br>
	 * (deprecated since 790) additional extras - not sent anymore
	 */
	public static final String ACTION_STATUS_CHANGED = "com.maxmpz.audioplayer.STATUS_CHANGED";


	/**
	 * This is explicit intent sent to your app to ensure it receives it on Android 8+ (with background execution limitations).
	 * Differs from ACTION_STATUS_CHANGED which is sticky intent which won't be received by your app in the background.<br><br>
	 *
	 * <b>NOTE: Poweramp caches app list for this intent. Cache is updated when Poweramp is started or playback resumed.</b><br>
	 * This means if your app just installed and Poweramp is playing, your app won't receive this action until next Poweramp pause/resume cycle or Poweramp service restart.<br><br>
	 *
	 * Extras:<br>
	 * {@code int state} - one of the STATE_* values<br>
	 * {@code boolean paused} - true if track paused/stopped, false if track is playing<br>
	 * {@code int pos} - (optional) current in-track position in seconds<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 * @since 798
	 */
	public static final String ACTION_STATUS_CHANGED_EXPLICIT = "com.maxmpz.audioplayer.STATUS_CHANGED_EXPLICIT";

	/**
	 * NON sticky intent<br>
	 * Extras:<br>
	 * {@code int pos} - current in-track position in seconds
	 */
	public static final String ACTION_TRACK_POS_SYNC = "com.maxmpz.audioplayer.TPOS_SYNC";

	/**
	 * Poweramp repeat or shuffle mode changed.<br>
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br><br>
	 *
	 * Extras:<br>
	 * {@code int repeat} - new repeat mode<br>
	 * {@code int shuffle} - new shuffle mode<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 * @see PowerampAPI.RepeatMode, PowerampAPI.ShuffleMode
	 */
	public static final String ACTION_PLAYING_MODE_CHANGED = "com.maxmpz.audioplayer.PLAYING_MODE_CHANGED";

	/**
	 * Poweramp equalizer settings changed.<br>
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br><br>
	 *
	 * Extras:<br>
	 * {@code String name} - preset name. If no name extra exists, it's not a preset<br>
	 * {@code long id} - preset id. If no id extra exists, it's not a preset<br>
	 * {@code String value} - equalizer and tone values in format:<br>
	 * <blockquote><code>bass=pos_float|treble=pos_float|31=float|62=float|....|16K=float|preamp=0.0 ... 2.0</code><br>
	 * where float = -1.0 ... 1.0, pos_float = 0.0 ... 1.0</blockquote>
	 * {@code boolean equ} - true if equalizer bands are enabled<br>
	 * {@code boolean tone} - truel if tone bands are enabled<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 */
	public static final String ACTION_EQU_CHANGED = "com.maxmpz.audioplayer.EQU_CHANGED";


	/**
	 * Opens a category list for the current track, or library - if no track is loaded
	 */
	public static final String ACTION_SHOW_CURRENT = "com.maxmpz.audioplayer.ACTION_SHOW_CURRENT";

	/**
	 * Opens library
	 * @deprecated. Use ACTION_OPEN_LIBRARY
	 */
	@Deprecated
	public static final String ACTION_SHOW_LIST = "com.maxmpz.audioplayer.ACTION_SHOW_LIST";

	/**
	 * Opens library
	 */
	public static final String ACTION_OPEN_LIBRARY = "com.maxmpz.audioplayer.ACTION_OPEN_LIBRARY";

	/**
	 * Opens search
	 */
	public static final String ACTION_OPEN_SEARCH = "com.maxmpz.audioplayer.ACTION_OPEN_SEARCH";

	/**
	 * Opens equalizer screen
	 * @see #EXTRA_EQ_TAB
	 */
	public static final String ACTION_OPEN_EQ = "com.maxmpz.audioplayer.ACTION_OPEN_EQ";

	/**
	 * Opens main screen
	 */
	public static final String ACTION_OPEN_MAIN = "com.maxmpz.audioplayer.ACTION_OPEN_MAIN";


	/**
	 * Grants sender a permission for content provider data access<br>
	 * Extras:<br>
	 * {@code String package} - the package name of app trying to get access<br>
	 * @since 797
	 */
	public static final String ACTION_ASK_FOR_DATA_PERMISSION = "com.maxmpz.audioplayer.ACTION_ASK_FOR_DATA_PERMISSION";

	/**
	 * Forces Poweramp UI and PlayerService to reload data from DB<br>
	 * Extras:<br>
	 * {@code String package} - the package name of app asking for the reload
	 * {@code String table} - the changed table, e.g. "playlists", "queue". Gives Poweramp a hint regarding data reloading, e.g. when queue is changed, queue UI stats should be probably updated
	 * @since 797
	 */
	public static final String ACTION_RELOAD_DATA = "com.maxmpz.audioplayer.ACTION_RELOAD_DATA";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 * @since 700
	 */
	public static final String DATA = "data";

	/**
	 * Poweramp native plugin command<br>
	 * Extras:<br>
	 * {@code String pak} - plugin package (see PACKAGE)<br>
	 * {@code int cmd} - some dsp unique command. cmd should be >= 0 (see COMMAND)<br>
	 * {@code byte[] data} - the command data serialized as byte array (see CONTENT)
	 * @since 700
	 * @see PowerampAPI.PACKAGE
	 * @see PowerampAPI.COMMAND
	 * @see PowerampAPI.CONTENT
	 */
	public static final String ACTION_NATIVE_PLUGIN_COMMAND = "com.maxmpz.audioplayer.NATIVE_PLUGIN_COMMAND";


	/**
	 * Poweramp initiated broadcast. Sent by Poweramp when it loads/reloads its audio kernel and loads plugin.<br>
	 * In response, plugin apps should send NATIVE_PLUGIN_COMMAND to Poweramp with the initial or restored plugin parameters.<br>
	 * Extras:<br>
	 * {@code int api} - Poweramp API version
	 * @since 700
	 * @see PowerampAPI.API_VERSION
	 */
	public static final String ACTION_NATIVE_PLUGIN_INIT = "com.maxmpz.audioplayer.NATIVE_PLUGIN_INIT";

	/**
	 * @deprecated there is no PlayerUIActivity anymore
	 */
	@Deprecated
	public static final String ACTIVITY_PLAYER_UI = "com.maxmpz.audioplayer.PlayerUIActivity";

	/**
	 * @deprecated there is no EqActivity anymore
	 */
	@Deprecated
	public static final String ACTIVITY_EQ = "com.maxmpz.audioplayer.EqActivity";

	/**
	 * @deprecated there is no PlayListActivity anymore
	 */
	@Deprecated
	public static final String ACTIVITY_PLAYLIST = "com.maxmpz.audioplayer.PlayListActivity";


	/**
	 * Poweramp settings activity
	 */
	public static final String ACTIVITY_SETTINGS = "com.maxmpz.audioplayer.preference.SettingsActivity";

	/**
	 * Poweramp startup activity. This activity always tries to pass incoming intent to main activity. Calling StartupActivity instead of main activity is preferable due
	 * to activity animation and possible first-run setup
	 */
	public static final String ACTIVITY_STARTUP = "com.maxmpz.audioplayer.StartupActivity";

	/**
	 * Extra for ACTION_API_COMMAND and RESUME command<Br>
	 * {@code int}
	 * @see PowerampAPI.ShuffleMode
	 * @since 797
	 */
	public static final String EXTRA_SHUFFLE = "shuffle";

	/**
	 * Extra
	 * Int
	 * @see PowerampAPI.ACTION_OPEN_EQ
	 * @see PowerampAPI.EQ_TAB_DEFAULT, PowerampAPI.EQ_TAB_EQUALIZER, PowerampAPI.EQ_TAB_VOLUME, PowerampAPI.EQ_TAB_REVERB
	 */
	public static final String EXTRA_EQ_TAB = "eqTab";

	/**
	 * Open last user opened eq tab 
	 * @see PowerampAPI.EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_DEFAULT = -1;
	/**
	 * Open equalizer tab 
	 * @see PowerampAPI.EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_EQUALIZER = 0;
	/**
	 * Open volume tab 
	 * @see PowerampAPI.EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_VOLUME = 1;
	/**
	 * Open reverb tab 
	 * @see PowerampAPI.EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_REVERB = 2;


	/**
	 * Extra<br>
	 * {@code String}
	 * @deprecated not used anymore
	 */
	@Deprecated
	public static final String ALBUM_ART_PATH = "aaPath";

	/**
	 * Extra<br>
	 * {@code Bitmap}
	 * @deprecated not used anymore
	 */
	@Deprecated
	public static final String ALBUM_ART_BITMAP = "aaBitmap";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated not used anymore
	 */
	@Deprecated
	public static final String DELAYED = "delayed";


	/**
	 * Extra<br>
	 * {@code long}
	 */
	public static final String TIMESTAMP = "ts";

	/**
	 * Extra<br>
	 * {@code int}
	 * @since 700
	 * @see PowerampAPI.STATUS_CHANGED
	 */
	public static final String STATE = "state";

	/**
	 * Poweramp is probably not fully loaded, state is unknown
	 * @since 705
	 * @see PowerampAPI.STATUS_CHANGED
	 */
	public static final int STATE_NO_STATE = -1;

	/**
	 * Poweramp is in stopped state - finished playing some list and stopped, or explicitly stopped by user
	 * @since 700
	 * @see PowerampAPI.STATUS_CHANGED
	 */
	public static final int STATE_STOPPED = 0;
	/**
	 * Poweramp is playing
	 * @since 700
	 * @see PowerampAPI.STATUS_CHANGED
	 */
	public static final int STATE_PLAYING = 1;
	/**
	 * Poweramp is paused
	 * @since 700
	 * @see PowerampAPI.STATUS_CHANGED
	 */
	public static final int STATE_PAUSED = 2;


	/**
	 * STATUS_CHANGED extra<br>
	 * {@code int}
	 * @deprecated use ACTION_STATUS_CHANGED
	 */
	@Deprecated
	public static final String STATUS = "status";

	/**
	 * STATUS extra values
	 * @deprecated use ACTION_STATUS_CHANGED
	 */
	@Deprecated
	public static final class Status {
		/**
		 * STATUS_CHANGED status value - track has been started to play or has been paused.<br>
		 * Note that Poweramp will start track immediately into this state when it's just loaded to avoid STARTED => PAUSED transition.<br>
		 * Additional extras - deprecated since 790 - not sent anymore:<br>
		 * 	(deprecated) {@code Bundle track} - bundle - track info<br>
		 * 	{@code boolean paused} - true if track paused, false if track resumed
		 */
		@Deprecated
		public static final int TRACK_PLAYING = 1;

		/**
		 * STATUS_CHANGED status value - track has been ended. Note, this intent will NOT be sent for just finished track IF Poweramp advances to the next track.<br>
		 * Additional extras:<br>
		 * 	(deprecated) {@code Bundle track} - track info<br>
		 *  (deprecated) {@code boolean failed} - true if track failed to play
		 */
		@Deprecated
		public static final int TRACK_ENDED = 2;

		/**
		 * STATUS_CHANGED status value - Poweramp finished playing some list and stopped
		 */
		@Deprecated
		public static final int PLAYING_ENDED = 3;
	}


	/**
	 * STATUS_CHANGED trackEnded extra<br>
	 * {@code boolean} - true if track failed to play
	 * @Deprecated (since 790) not sent anymore
	 */
	@Deprecated
	public static final String FAILED = "failed";

	/**
	 * STATUS_CHANGED extra<br>
	 * {@code boolean} - true if track is paused
	 * @see #ACTION_STATUS_CHANGED
	 */
	public static final String PAUSED = "paused";

	/**
	 * PLAYING_MODE_CHANGED extra<br>
	 * {@code integer}
	 * @see PowerampAPI.ShuffleMode
	 */
	public static final String SHUFFLE = "shuffle";

	/**
	 * PLAYING_MODE_CHANGED extra<br>
	 * {@code integer}
	 * @see PowerampAPI.RepeatMode
	 */
	public static final String REPEAT = "repeat";


	/**
	 * Extra<br>
	 * {@code long}
	 */
	public static final String ID = "id";

	/**
	 * STATUS_CHANGED track extra<br>
	 * {@code Bundle}
	 */
	public static final String TRACK = "track";

	/**
	 * Shuffle extras values
	 */
	public static final class ShuffleMode {
		/**
		 * No any shuffle selected
		 */
		public static final int SHUFFLE_NONE		   = 0;
		/**
		 * All songs global category shuffle
		 */
		public static final int SHUFFLE_ALL			   = 1;
		/**
		 * Just songs from current category shuffled
		 */
		public static final int SHUFFLE_SONGS		   = 2;
		/**
		 * Categories shuffled, songs in order
		 */
		public static final int SHUFFLE_CATS		   = 3;
		/**
		 * Songs shuffled, categories in order
		 */
		public static final int SHUFFLE_SONGS_AND_CATS = 4;
		/**
		 * Max possible shuffle value
		 */
		public static final int MAX_SHUFFLE			   = 4;
		
		/**
		 * Pseudo mode just for UI, not used as mode directly (SHUFFLE_SONGS is used)
		 */
		public static final int SHUFFLE_SONGS_HIER     = 5;
	}

	/**
	 * Repeat extras values
	 */
	public static final class RepeatMode {
		/**
		 * Repeat is disabled
		 */
		public static final int REPEAT_NONE	   = 0;
		/**
		 * Current selected category repeated
		 */
		public static final int REPEAT_ON	   = 1;
		/**
		 * Category will be advanced to next one after the last song played
		 */
		public static final int REPEAT_ADVANCE = 2;
		/**
		 * Current song is repeated
		 */
		public static final int REPEAT_SONG    = 3;
		/**
		 * Max possible repeat value
		 */
		public static final int MAX_REPEAT     = 3;
	}

	/**
	 * Vis extras values
	 */
	public static final class VisMode {
		/**
		 * Visualization is disabled
		 */
		public static final int VIS_NONE        = 0;
		/**
		 * Visualization with UI visible
		 */
		public static final int VIS_W_UI        = 1;
		/**
		 * Full screen visualization
		 */
		public static final int VIS_FULL_SCREEN = 2;
	}


	/**
	 * STATUS_CHANGED track extra fields
	 */
	@SuppressWarnings("hiding")
	public static final class Track {
		/**
		 * Id of the current track.
		 * Can be a playlist entry id<br>
		 * {@code long}
		 */
		public static final String ID = "id";

		/**
		 * "Real" id. In case of playlist entry, this is always resolved to Poweramp folder_files table row ID or System Library MediaStorage.Audio._ID<nt>
		 * {@code long}
		 */
		public static final String REAL_ID = "realId";

		/**
		 * Category type<br>
		 * {@code int}
		 * @see PowerampAPI.Track.Type
		 */
		public static final String TYPE = "type";

		/**
		 * Category URI match<br>
		 * {@code int}
		 */
		public static final String CAT = "cat";

		/**
		 * {@code boolean}
		 */
		public static final String IS_CUE = "isCue";

		/**
		 * Category URI<br>
		 * {@code Uri}
		 */
		public static final String CAT_URI = "catUri";

		/**
		 * True if category navigation (<<< >>>) is possible<br>
		 * {@code boolean}
		 */
		public static final String SUPPORTS_CAT_NAV = "supportsCatNav";

		/**
		 * File type<br>
		 * {@code integer}
		 * @see PowerampAPI.Track.FileType
		 */
		public static final String FILE_TYPE = "fileType";

		/**
		 * Track file path<br>
		 * {@code String}
		 */
		public static final String PATH = "path";

		/**
		 * Track title<br>
		 * {@code String}
		 */
		public static final String TITLE = "title";

		/**
		 * Track album<br>
		 * {@code String}
		 */
		public static final String ALBUM = "album";

		/**
		 * Track artist<br>
		 * {@code String}
		 */
		public static final String ARTIST = "artist";

		/**
		 * Track duration in seconds<br>
		 * {@code int}
		 */
		public static final String DURATION = "dur";

		/**
		 * Position in track in seconds<br>
		 * {@code int}
		 */
		public static final String POSITION = "pos";

		/**
		 * Position in a list<br>
		 * {@code int}
		 */
		public static final String POS_IN_LIST = "posInList";

		/**
		 * List size<br>
		 * {@code int}
		 */
		public static final String LIST_SIZE = "listSize";

		/**
		 * Track sample rate<br>
		 * {@code int}
		 */
		public static final String SAMPLE_RATE = "sampleRate";

		/**
		 * Track number of channels<br>
		 * {@code int}
		 */
		public static final String CHANNELS = "channels";

		/**
		 * Track average bitrate<br>
		 * {@code int}
		 */
		public static final String BITRATE = "bitRate";

		/**
		 * Resolved codec name for the track<br>
		 * {@code String}
		 */
		public static final String CODEC = "codec";

		/**
		 * Track bits per sample<br>
		 * {@code int}
		 */
		public static final String BITS_PER_SAMPLE = "bitsPerSample";

		/**
		 * Track flags<br>
		 * {@code int}
		 */
		public static final String FLAGS = "flags";

		/**
		 * {@link PowerampAPI.Track} {@link #FILE_TYPE} values
		 */
		public static class FileType {
			public static final int TYPE_UNKNOWN = -1;
			public static final int TYPE_MP3 = 0;
			public static final int TYPE_FLAC = 1;
			public static final int TYPE_M4A = 2;
			public static final int TYPE_MP4 = 3;
			public static final int TYPE_OGG = 4;
			public static final int TYPE_WMA = 5;
			public static final int TYPE_WAV = 6;
			public static final int TYPE_TTA = 7;
			public static final int TYPE_APE = 8;
			public static final int TYPE_WV = 9;
			public static final int TYPE_AAC = 10;
			public static final int TYPE_MPGA = 11;
			public static final int TYPE_AMR = 12;

			public static final int TYPE_3GP = 13;
			public static final int TYPE_MPC = 14;
			public static final int TYPE_AIFF = 15;
			public static final int TYPE_AIF = 16;
			public static final int TYPE_FLV = 17;
			public static final int TYPE_OPUS = 18;
			public static final int TYPE_DFF = 19;
			public static final int TYPE_DSF = 20;
			public static final int TYPE_MKA = 21;
			public static final int TYPE_TAK = 22;
			public static final int LAST_TYPE = 22;

			@Deprecated
			public static final int mp3 = 0;
			@Deprecated
			public static final int flac = 1;
			@Deprecated
			public static final int m4a = 2;
			@Deprecated
			public static final int mp4 = 3;
			@Deprecated
			public static final int ogg = 4;
			@Deprecated
			public static final int wma = 5;
			@Deprecated
			public static final int wav = 6;
			@Deprecated
			public static final int tta = 7;
			@Deprecated
			public static final int ape = 8;
			@Deprecated
			public static final int wv = 9;
			@Deprecated
			public static final int aac = 10;
			@Deprecated
			public static final int mpga = 11;
			@Deprecated
			public static final int amr = 12;
			@Deprecated
			public static final int _3gp = 13;
			@Deprecated
			public static final int mpc = 14;
			@Deprecated
			public static final int aiff = 15;
			@Deprecated
			public static final int aif = 16;
		}

		/**
		 * {@link PowerampAPI.Track} {@link #FLAGS} bitset values. First 3 bits = FLAG_ADVANCE_*
		 */
		public static final class Flags {
			/** Track wasn't advanced */
			public static final int FLAG_ADVANCE_NONE            = 0;
			/** Track was advanced forward */
			public static final int FLAG_ADVANCE_FORWARD         = 1;
			/** Track was advanced backward */
			public static final int FLAG_ADVANCE_BACKWARD        = 2;
			/** Track category was advanced forward */
			public static final int FLAG_ADVANCE_FORWARD_CAT     = 3;
			/** Track category was advanced backward */
			public static final int FLAG_ADVANCE_BACKWARD_CAT    = 4;
			/** Mask for FLAG_ADVANCE_* values */
			public static final int FLAG_ADVANCE_MASK            = 0x7; // 111
			/** Track was advanced from notification */
			public static final int FLAG_NOTIFICATION_UI         = 0x20;
			/** Used to indicate that track is first in Poweramp service session */
			public static final int FLAG_FIRST_IN_PLAYER_SESSION = 0x40;
		}
	}

	/**
	 * {@link PowerampAPI.Track} {@link PowerampAPI.Track.CAT} categories
	 */
	public static final class Cats {
		public static final int ROOT = 0;
		public static final int FOLDERS = 10;
		public static final int MOST_PLAYED = 43;
		public static final int TOP_RATED = 48;
		public static final int LOW_RATED = 50;
		public static final int RECENTLY_ADDED = 53;
		public static final int RECENTLY_PLAYED = 58;
		public static final int LONG = 55;
		public static final int PLAYLISTS = 100;
		public static final int ALBUMS = 200;
		public static final int GENRES_ID_ALBUMS = 210;
		public static final int ARTISTS_ID_ALBUMS = 220;
		public static final int ARTISTS__ALBUMS = 250;
		public static final int COMPOSERS_ID_ALBUMS = 230;
		public static final int GENRES = 320;
		public static final int ARTISTS = 500;
		public static final int COMPOSERS = 600;
		public static final int QUEUE = 800;
	}

	/**
	 * Describes Poweramp scanner related actions
	 */
	public static final class Scanner {

		/**
		 * Poweramp Scanner action.<br><br>
		 *
		 * Poweramp Scanner scanning process is 2 step:<br>
		 * 1. Folders scan.<br>
		 *	Checks filesystem and updates DB with folders/files structure.<br>
		 * 2. Tags scan.<br>
		 *	Iterates over files in DB with TAG_STATUS == TAG_NOT_SCANNED and scans them with tag scanner.<br><br>
		 *
		 * Poweramp Scanner is a IntentService, this means multiple scan requests at the same time (or during another scans) are queued.<br>
		 * ACTION_SCAN_DIRS actions are prioritized and executed before ACTION_SCAN_TAGS.<br><br>
		 *
		 * Poweramp main scan action scans the set of folders either incrementally or from scratch, the folders are configured by user in Poweramp Settings.<br>
		 * NOTE: Poweramp will always do ACTION_SCAN_TAGS automatically after ACTION_SCAN_DIRS is finished and some changes are required to song tags in DB.<br>
		 * Unless, fullRescan specified, Poweramp will not remove songs if they are missing from filesystem due to unmounted storages.<br>
		 * Normal menu => Rescan calls ACTION_SCAN_DIRS without extras<br><br>
		 *
		 * Poweramp Scanner sends appropriate broadcast intents:<br>
		 * {@link #ACTION_DIRS_SCAN_STARTED} (sticky),
		 * {@link #ACTION_DIRS_SCAN_FINISHED},
		 * {@link #ACTION_TAGS_SCAN_STARTED} (sticky),
		 * {@link #ACTION_TAGS_SCAN_PROGRESS},
		 * {@link #ACTION_TAGS_SCAN_FINISHED}, or
		 * {@link #ACTION_FAST_TAGS_SCAN_FINISHED}<br><br>
		 *
		 * Extras:<br>
		 * {@code boolean fastScan} - Poweramp will not check folders and scan files which hasn't been modified from previous scan. Based on files last modified timestamp<br>
		 * {@code boolean eraseTags} - Poweramp will clean all tags from exisiting songs. This causes each song to be re-scanned for tags.
		 *			   Warning: as a side effect, cleans CUE tracks from user created playlists. 
		 *			   This is because scanner can't incrementaly re-scan CUE sheets, so they are deleted from DB, causing their
		 *			   deletion from user playlists as well<br>
		 * {@code boolean fullRescan} - Poweramp will also check for folders/files from missing/unmounted storages and will remove them from DB.
		 *				Warning: removed songs also disappear from user created playlists.
		 *				Used in Poweramp only when user specificaly goes to Settings and does Full Rescan (after e.g. SD card change)<br>
		 *
		 */
		public static final String ACTION_SCAN_DIRS = "com.maxmpz.audioplayer.ACTION_SCAN_DIRS";

		/**
		 * Poweramp Scanner action.<br>
		 * Secondary action, only checks songs with TAG_STATUS set to TAG_NOT_SCANNED. Useful for rescanning just songs (which are already in Poweramp DB) with editied file tag info.<br><br>
		 *
		 * Extras:<br>
		 * {@code boolean fastScan} - If true, scanner doesn't send ACTION_TAGS_SCAN_STARTED/ACTION_TAGS_SCAN_PROGRESS/ACTION_TAGS_SCAN_FINISHED intents,
		 *			   just sends ACTION_FAST_TAGS_SCAN_FINISHED when done.
		 *			   It doesn't modify scanning logic otherwise.
		 */
		public static final String ACTION_SCAN_TAGS = "com.maxmpz.audioplayer.ACTION_SCAN_TAGS";


		/**
		 * Broadcast<br>
		 * Poweramp Scanner started folders scan<br>
		 * Sticky intent (can be queried for permanently stored data)<br>
		 */
		public static final String ACTION_DIRS_SCAN_STARTED = "com.maxmpz.audioplayer.ACTION_DIRS_SCAN_STARTED";
		/**
		 * Broadcast<br>
		 * Poweramp Scanner finished folders scan<br>
		 */
		public static final String ACTION_DIRS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_DIRS_SCAN_FINISHED";
		/**
		 * Broadcast<br>
		 * Poweramp Scanner started tag scan<br>
		 * Sticky intent (can be queried for permanently stored data)
		 */
		public static final String ACTION_TAGS_SCAN_STARTED = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_STARTED";
		/**
		 * Broadcast<br>
		 * @Deprecated not used anymore
		 */
		@Deprecated
		public static final String ACTION_TAGS_SCAN_PROGRESS = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_PROGRESS";
		/**
		 * Broadcast<br>
		 * Poweramp Scanner finished tag scan<br>
		 * Extras:<br>
		 * {@code boolean track_content_changed} - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date)
		 */
		public static final String ACTION_TAGS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_FINISHED";
		/**
		 * Broadcast<br>
		 * Poweramp Scanner finished fast tag scan. Only fired when ACTION_SCAN_TAGS is called with extra fastScan = true<br>
		 * Extras:<br>
		 * {@code boolean trackContentChanged} - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date)
		 */
		public static final String ACTION_FAST_TAGS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_FAST_TAGS_SCAN_FINISHED";

		/**
		 * Extra<br>
		 * {@code boolean}
		 */
		public static final String EXTRA_FAST_SCAN = "fastScan";
		/**
		 * Extra<br>
		 * {@code int}
		 */
		public static final String EXTRA_PROGRESS = "progress";
		/**
		 * Extra<br>
		 * {@code boolean} - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date)
		 */
		public static final String EXTRA_TRACK_CONTENT_CHANGED = "trackContentChanged";

		/**
		 * Extra<br>
		 * {@code boolean}
		 */
		public static final String EXTRA_ERASE_TAGS = "eraseTags";

		/**
		 * Extra<br>
		 * {@code boolean}
		 */
		public static final String EXTRA_FULL_RESCAN = "fullRescan";

		/**
		 * Extra<br>
		 * {@code String} - cause of the scan (e.g. user request, auto scan, etc.). Useful for debugging, visible in logcat
		 */
		public static final String EXTRA_CAUSE = "cause";
	}

	/**
	 * Settings related actions
	 */
	public static final class Settings {
		/**
		 * Exports Poweramp settings
		 */
		public static final String ACTION_EXPORT_SETTINGS = "com.maxmpz.audioplayer.ACTION_EXPORT_SETTINGS";
		/**
		 * Imports Poweramp settings
		 */
		public static final String ACTION_IMPORT_SETTINGS = "com.maxmpz.audioplayer.ACTION_IMPORT_SETTINGS";

		/**
		 * Extra<br/>
		 * {@code boolean} if true, UI may be shown for import / export errors, otherwise import / export may fail silently
		 */
		public static final String EXTRA_UI = "ui";

		/**
		 * Value for EXTRA_OPEN - opens skins list
		 * @see PowerampAPI.ACTIVITY_SETTINGS
		 * @since 700
		 */
		public static final String OPEN_THEME = "theme";

		/**
		 * Extra for ACTIVITY_STARTUP and ACTIVITY_SETTINGS<br>
		 * If this is specified, Poweramp will attempt to enable and scan vis presets in this package<br>
		 * Can be also specified for com.maxmpz.audioplayer.SettingsActivity (with EXTRA_OPEN=vis) to scroll to that apk in presets list<br>
		 * {@code String} - vis presets APK package name
		 * @since 795
		 */
		public static final String EXTRA_VIS_PRESETS_PAK = "vis_presets_pak";

		/**
		 * Extra for ACTIVITY_SETTINGS<br>
		 * {@code String}
		 * @see PowerampAPI.ACTIVITY_SETTINGS
		 * @since 700
		 */
		public static final String EXTRA_OPEN = "open";


		/**
		 * Value for EXTRA_OPEN - opens vis presets list
		 * @see PowerampAPI.ACTIVITY_SETTINGS
		 * @since 700
		 */
		public static final String OPEN_VIS = "vis";

		/**
		 * Extra for ACTIVITY_STARTUP and ACTIVITY_SETTINGS<Br>
		 * If this is specified with EXTRA_SKIN_STYLE_ID, Poweramp will attempt to change skin as commanded, but on any failure, default skin is activated<br>
		 * Can be also specified for com.maxmpz.audioplayer.SettingsActivity (with EXTRA_OPEN=theme) to scroll to that skin in skins list<br><br>
		 *
		 * {@code String} - Skin APK package name
		 * @see PowerampAPI.ACTIVITY_STARTUP, PowerampAPI.ACTIVITY_SETTINGS
		 * @since 795
		 */
		public static final String EXTRA_SKIN_PACKAGE = "theme_pak";

		/**
		 * Can be specified for com.maxmpz.audioplayer.SettingsActivity and open/theme extras top scroll to given skin in skins list<Br>
		 * {@code integer} - theme resource id
		 * @since 795
		 */
		public static final String EXTRA_SKIN_STYLE_ID = "theme_id";
	}

}
