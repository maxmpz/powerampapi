/*
Copyright (C) 2011-2023 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for the widgets, plugins, applications and other software
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
import org.eclipse.jdt.annotation.NonNull;


/**
 * Poweramp Intent based API.
 * <br><br>
 * 
 * Most API intents defined here can be sent to various targets:
 * <ul>
 * <li>broadcast receiver via Context.sendBroadcast to {@link #API_RECEIVER_NAME}
 * <li>redirecting activity via Context.startActivity to {@link #API_ACTIVITY_NAME}<br>
 *     This activity immediately closes itself and it's useful if activity target is required or Poweramp app should be brought to foreground
 * <li>Player service via Context.startService to {@link #PLAYER_SERVICE_NAME}<br>
 *     Not recommended as services are subject to background execution limitation and your code may fail with "app is in background" exception<br>
 *     NOTE: Poweramp doesn't use Context.startForegroundService and if you call Poweramp service with startForegroundService app will crash
 * <li>ScanDispatcher service via Context.startService with {@link Scanner#ACTION_SCAN_DIRS}, {@link Scanner#ACTION_SCAN_TAGS} actions<br>
 *     Not recommended as services are subject to background execution limitation and your code may fail with "app is in background" exception<br>
 * </ul>
 * <br>
 * NOTE: in addition to ACTION_* intent actions defined by PowerampAPI, Poweramp also supports standard intents (these should be sent to {@link #ACTIVITY_STARTUP}):
 * <ul>
 * <li>android.content.Intent.ACTION_VIEW (android.intent.action.VIEW")
 * <li>android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH (android.media.action.MEDIA_PLAY_FROM_SEARCH)
 * <li>android.content.Intent.ACTION_SEARCH (android.intent.action.SEARCH)
 * <li>android.intent.action.MEDIA_SEARCH
 * </ul>
 * 
 * Starting from build 853 Poweramp also supports android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH (android.media.action.MEDIA_PLAY_FROM_SEARCH) as 
 * broadcast intent for {@link #API_RECEIVER_NAME} (recommended), or PlayerService intent {@link #PLAYER_SERVICE_NAME},
 * so this can be sent directly to service without activity / Poweramp UI starting.<br>
 * NOTE: this is supported for {@link #ACTIVITY_STARTUP} from around build 800<br><br>
 * 2
 * INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH supports:<br>
 * - <b>simple freeform query via SearchManager.QUERY (query) extra</b><br>
 *   - Poweramp will attempt to play track matching query<br>
 *   - Poweramp also looks to keywords such as playlist, album, artist, genre in the query in the local language and if found, Poweramp will attempt to play the found category<br><br>
 *   
 * - <b>focused query via MediaStore.EXTRA_MEDIA_FOCUS query (https://developer.android.com/reference/android/provider/MediaStore#EXTRA_MEDIA_FOCUS):</b><br>
 *   - when EXTRA_MEDIA_FOCUS == MediaStore.Audio.GenresGenres.ENTRY_CONTENT_TYPE (vnd.android.cursor.item/genre):<br>
 *   &nbsp;&nbsp;-> Poweramp plays genre indicated by MediaStore.EXTRA_MEDIA_GENRE (android.intent.extra.genre) extra<br>
 *   - when EXTRA_MEDIA_FOCUS == MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE (vnd.android.cursor.item/artist):<br>
 *   &nbsp;&nbsp;-> Poweramp plays artist indicated by MediaStore.EXTRA_MEDIA_ARTIST (android.intent.extra.artist) extra <br>
 *   - when EXTRA_MEDIA_FOCUS == MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE (vnd.android.cursor.item/album):<br>
 *   &nbsp;&nbsp;-> Poweramp plays album indicated by MediaStore.EXTRA_MEDIA_ALBUM (android.intent.extra.album) and MediaStore.EXTRA_MEDIA_ARTIST (android.intent.extra.artist) extras<br>
 *   - when EXTRA_MEDIA_FOCUS == MediaStore.Audio.Media.ENTRY_CONTENT_TYPE (vnd.android.cursor.item/audio):<br>
 *   &nbsp;&nbsp;-> Poweramp plays song indicated by MediaStore.EXTRA_MEDIA_TITLE (android.intent.extra.title), MediaStore.EXTRA_MEDIA_ALBUM (android.intent.extra.album), and MediaStore.EXTRA_MEDIA_ARTIST (android.intent.extra.artist) extras<br>
 *   - when EXTRA_MEDIA_FOCUS == MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE (vnd.android.cursor.item/playlist):<br>
 *   &nbsp;&nbsp;-> Poweramp plays playlist indicated by MediaStore.EXTRA_MEDIA_PLAYLIST (android.intent.extra.playlist) extra<br>
 *   - when EXTRA_MEDIA_FOCUS is anything else:<br>
 *   &nbsp;&nbsp;-> Poweramp tries to search for SearchManager.QUERY or MediaStore.EXTRA_MEDIA_TITLE in tracks, genres, and playlists and play the found result<br><br>
 *   
 * Actions supported (name -> target components):
 * <ul>
 * <li>{@link #ACTION_API_COMMAND} -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}, {@link #PLAYER_SERVICE_NAME}
 * <li>{@link #ACTION_ASK_FOR_DATA_PERMISSION} -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}
 * <li>{@link #ACTION_NATIVE_PLUGIN_COMMAND} -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}, {@link #PLAYER_SERVICE_NAME}
 * <li>{@link #ACTION_RELOAD_DATA} -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}, {@link #PLAYER_SERVICE_NAME}
 * <li>{@link #ACTION_OPEN_EQ} -> {@link #ACTIVITY_STARTUP}
 * <li>{@link #ACTION_OPEN_LIBRARY} -> {@link #ACTIVITY_STARTUP}
 * <li>{@link #ACTION_OPEN_MAIN} -> {@link #ACTIVITY_STARTUP}
 * <li>{@link #ACTION_OPEN_SEARCH} -> {@link #ACTIVITY_STARTUP}
 * <li>{@link #ACTION_SHOW_CURRENT} -> {@link #ACTIVITY_STARTUP}
 * <li>{@link #ACTION_SHOW_LIST} -> {@link #ACTIVITY_STARTUP}
 * <li>{@link #INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH} -> {@link #ACTIVITY_STARTUP}, {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}, {@link #PLAYER_SERVICE_NAME}
 * <li>{@link Scanner#ACTION_SCAN_DIRS} -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}
 * <li>{@link Scanner#ACTION_SCAN_TAGS} -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}
 * <li>{@link MilkScanner#ACTION_SCAN} -> {@link PowerampAPIHelper#getMilkScannerServiceComponentName}, since 868 -> {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}
 * </ul>
 */
@SuppressWarnings({"WeakerAccess", "Unused"})
public final class PowerampAPI {
	/**
	 * Defines PowerampAPI version
	 */
	public static final int VERSION = 855;

	/**
	 * No id value (for id-related fields, for example, {@link PowerampAPI.Track#ID})
	 */
	public static final long NO_ID = 0L;

	/**
	 * Special {@link PowerampAPI.Track#ID} value indicating raw file - file opened from some file manager, which can't be matched against Poweramp database
	 */
	public static final long RAW_TRACK_ID = -2L;

	/**
	 * Special {@link PowerampAPI.Track#ID} value indicating missing file - for example playlist entry which can't be found
	 */
	public static final long MISSING_TRACK_ID = -3L;
	
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
	 * Uri query parameter - shuffle mode
	 */
	public static final String PARAM_SHUFFLE = "shf";

	/**
	 * Sent by your app to Poweramp.<br>
	 * Poweramp Control action.<br>
	 * Can be sent to {@link #API_RECEIVER_NAME}, {@link #API_ACTIVITY_NAME}, {@link #PLAYER_SERVICE_NAME}<br>
	 * Starting from Poweramp build-855 this is now also a broadcast intent (which should be the primary target of this action).<br>
	 * Previously this was executed directly by service and though this is still supported it's deprecated.<br>
	 * The issue with sending intents to service is foreground processing, which on current Androids 8-10 can't be 100% reliable processed and may cause unexpected ANR errors<br><br>
	 * 
	 * Extras:<br>
	 * {@link #EXTRA_COMMAND} - command to execute. Since build 867 can be a string matching {@link Commands} field name, e.g. "TOGGLE_PLAY_PAUSE" (case insensitive)<br>
	 * {@link #EXTRA_PACKAGE} - optional - the command issuing plugin/app package name - for the debugging purposes. Poweramp will log appropriate command details if specified<br>
	 * {@link #EXTRA_SOURCE} - optional - the source of command, e.g. "widget", "UI", etc. - for the debugging purposes<br>
	 */
	public static final String ACTION_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
	

	/**
	 * Poweramp service name<br>
	 * See also {@link PowerampAPIHelper#getPlayerServiceComponentName} for a way of getting PlayerService component resolved according actual Poweramp package name<br>
	 * NOTE: Poweramp PlayerService does not implement MediaBrowser API. For this, separate service is used. See {@link PowerampAPIHelper#getBrowserServiceComponentName}
	 */
	public static final String PLAYER_SERVICE_NAME = "com.maxmpz.audioplayer.player.PlayerService";

	/**
	 * Poweramp API broadcast receiver name. This is now a preferable target of all command intents as it's free from background service limitation issues
	 * @since 855
	 */
	public static final String API_RECEIVER_NAME = "com.maxmpz.audioplayer.player.PowerampAPIReceiver";

	/**
	 * Poweramp API activity name. Can be used for intents which can't be sent as broadcast, where activity target is required.<br>
	 * Supports {@link #ACTION_API_COMMAND}, {@link Scanner#ACTION_SCAN_DIRS}, {@link Scanner#ACTION_SCAN_TAGS}  
	 * @since 855
	 */
	public static final String API_ACTIVITY_NAME = "com.maxmpz.audioplayer.PowerampAPIActivity";

	/**
	 * Permission allowing ContentProvider based access to milk preset files<br>
	 * On Android 6+ (SDK=23+) this should be explicitly requested via {@link android.app.Activity#requestPermissions}
	 * @since 885
	 */
	public static final String PERMISSION_ACCESS_MILK_PRESETS = "com.maxmpz.audioplayer.permission.ACCESS_MILK_PRESETS";

	/**
	 * ContentProvider based access to milk preset files<br><br>
	 * Supported actions:
	 * <ul>
	 * <li>query<br>
	 *     Poweramp looks into uri only and ignores possible project, selection, selectionArgs arguments to query call<br>
	 *     Poweramp always returns same columns: Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED, Document.COLUMN_SIZE<br>
	 *     Uri may be empty to query all files, uri may include GLOB pattern (*, ?, {a|b|c}, [abc]) or uri can be a file name
	 *     to query just one file
	 * <li>openFile<br>
	 *     The uri should include desired file to open, e.g. content://com.maxmpz.audioplayer.milk_presets/preset.milk
	 * <li>delete<br>
	 *     The uri should include desired file to delete, e.g. content://com.maxmpz.audioplayer.milk_presets/preset.milk
	 * </ul>
	 * @since 885
	 */
	public static final Uri MILK_PRESETS_URI = Uri.parse("content://com.maxmpz.audioplayer.milk_presets/");

	/**
	 * ACTION_API_COMMAND extra
	 * <br>
	 * {@code int} - see {@link Commands}<br>
	 * or {@code String} (since 867) with values like "TOGGLE_PLAY_PAUSE", "RESUME", "PAUSE", etc. matching field names from {@link Commands}
	 */
	public static final String EXTRA_COMMAND = "cmd";

	/**
	 * Command values for {@link #EXTRA_COMMAND} extra.<br>
	 * Alternatively, Commands field name can be used instead of integer value, e.g. "PAUSE", instead of 2
	 */
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
		 * Same as {@link #RESUME}
		 */
		public static final int PLAY = 3;

		/**
		 * NOTE: subject to 200ms throttling
		 */
		public static final int NEXT = 4;

		/**
		 * NOTE: subject to 200ms throttling
		 */
		public static final int PREVIOUS = 5;

		/**
		 * Same as {@link #PREVIOUS}
		 */
		public static final int PREV = 5;

		/**
		 * Next category<br>
		 * NOTE: subject to 200ms throttling
		 */
		public static final int NEXT_IN_CAT = 6;

		/**
		 * Previous category<br>
		 * NOTE: subject to 200ms throttling
		 */
		public static final int PREVIOUS_IN_CAT = 7;

		/**
		 * Set repeat mode<br>
		 * Extras:<br>
		 * {@code int repeat} {@link PowerampAPI#EXTRA_REPEAT} - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled
		 * @see PowerampAPI.RepeatMode
		 */
		public static final int REPEAT = 8;

		/**
		 * Set shuffle mode<br>
		 * Extras:<br>
		 * {@code int shuffle} {@link PowerampAPI#EXTRA_SHUFFLE}- (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled
		 * @see PowerampAPI.ShuffleMode
		 */
		public static final int SHUFFLE = 9;

		/**
		 * Poweramp starts constantly seeking forward until {@link #END_FAST_FORWARD} received
		 */
		public static final int BEGIN_FAST_FORWARD = 10;

		/**
		 * Stops {@link #BEGIN_FAST_FORWARD} or {@link #BEGIN_REWIND}
		 */
		public static final int END_FAST_FORWARD = 11;

		/**
		 * Poweramp starts constantly seeking backward until {@link #END_REWIND} received
		 */
		public static final int BEGIN_REWIND = 12;

		/**
		 * Stops {@link #BEGIN_REWIND} or {@link #BEGIN_FAST_FORWARD}
		 */
		public static final int END_REWIND = 13;

		/**
		 * Stops playback, resets progress, unloads track
		 */
		public static final int STOP = 14;

		/**
		 * Extras:<br>
		 * {@link #EXTRA_POSITION} - int - seek position in seconds<br>
		 * {@link #EXTRA_RELATIVE_POSITION} - int - (since 868) if exists, defines relative seek position in seconds and can be both a negative and a positive value.
		 * EXTRA_POSITION is ignored. Seeking past track end or prior track start will cause track change to the next/previous one<br>
		 * {@link #EXTRA_LOCK} - boolean - if true, seeking start of track with {@link #EXTRA_RELATIVE_POSITION} will just reset track position to 0, and seeking past end will do nothing 
		 * (track continues to play normally) 
		 */
		public static final int SEEK = 15;
		
		/**
		 * Request for Poweramp current track position. In response, {@link #ACTION_TRACK_POS_SYNC} is sent
		 */
		public static final int POS_SYNC = 16;

		/**
		 * Stop {@link #BEGIN_FAST_FORWARD} or {@link #BEGIN_REWIND}
		 */
		public static final int END_FF_OR_RW = 11;
		
		/**
		 * Sets or disables sleep timer<br>
		 * Extras:<br>
		 * {@link PowerampAPI#EXTRA_SECONDS} - if 0, disables sleep timer<br>
		 * {@link PowerampAPI#EXTRA_PLAY_TO_END} - if true, Poweramp will play last track to end prior pausing<br>
		 * @since 862
		 */
		public static final int SLEEP_TIMER = 17;

		/**
		 * Set/unset "like" rating for the current track
		 * @since 966
		 */
		public static final int LIKE = 18;

		/**
		 * Set/unset "unlike" rating for the current track
		 * @since 966
		 */
		public static final int UNLIKE = 19;

		/**
		 * Switches between like/unlike/no rating for the current track
		 * @since 966
		 */
		public static final int TOGGLE_RATING = 21;

		/**
		 * Seek for +10s
		 * @since 966
		 */
		public static final int SEEK_JUMP_FORWARD = 22;

		/**
		 * Seel for -10s
		 * @since 966
		 */
		public static final int SEEK_JUMP_BACKWARD = 23;

		 /**
		  * Data:<br>
		  * - uri, following URIs are recognized:<br>
		  * 	- file://path (NOTE: depending on Android version, uris like this can fail due to missing filesystem permissions)<br>
		  * 	- content://com.maxmpz.audioplayer.data/... (see below)<br><br>
		  *  - any other content:// uri compatible with ContentResolver.openFile<br>
		  *  - http/https url (stream or remote track file)<br><br>
		  *
		  * # means some numeric id (track id for queries ending with /files, otherwise - appropriate category id).<br>
		  * If track id (in place of #) is not specified, Poweramp plays whole list starting from the specified track,<br>
		  * or from first one, or from random one in shuffle mode.<br><br>
		  *
		  * NOTE: for Playlist and Queue tracks the item id differs from other file categories. As the same track be added into Playlist or Queue multiple times,
		  * folder_files._id can't be used as unique id there. Instead, playlist_entries._id and queue._id are used.
		  * Make sure to pass playlist_entries._id/queue._id as id to start track from, not the folder_files._id<br><br>
		  *
		  * All queries support following params (added as URL encoded params, e.g. content://com.maxmpz.audioplayer.data/files?lim=10):<br>
		  * {@code int lim} - SQL LIMIT, which limits number of rows returned<br>
		  * {@code int shf} - shuffle mode (see ShuffleMode class)<br>
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

		 - Hierarchy folders view
		 content://com.maxmpz.audioplayer.data/folders_hier
		 _id => 0 is the "root" folder, never contains files)
		 Root folders:          content://com.maxmpz.audioplayer.data/folders_hier/0/subfolders
		 Any other sub-folders: content://com.maxmpz.audioplayer.data/folders_hier/#/subfolders
		 Sub-folder files:      content://com.maxmpz.audioplayer.data/folders_hier/#/files

		 - Genres
		 content://com.maxmpz.audioplayer.data/genres
		 content://com.maxmpz.audioplayer.data/genres/#/files
		 content://com.maxmpz.audioplayer.data/genres/#/files/#
		 content://com.maxmpz.audioplayer.data/genres/files

		 - Years
		 content://com.maxmpz.audioplayer.data/years
		 content://com.maxmpz.audioplayer.data/years/#/files
		 content://com.maxmpz.audioplayer.data/years/#/files/#
		 content://com.maxmpz.audioplayer.data/years/files

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

		 - Albums by Years
		 content://com.maxmpz.audioplayer.data/genres/#/years
		 content://com.maxmpz.audioplayer.data/genres/#/years/#/files
		 content://com.maxmpz.audioplayer.data/genres/#/years/#/files/#
		 content://com.maxmpz.audioplayer.data/genres/#/years/files
		 content://com.maxmpz.audioplayer.data/genres/years

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
		 
		 - Streams
		 content://com.maxmpz.audioplayer.data/streams
		 content://com.maxmpz.audioplayer.data/streams/#

		 - Bookmarks
		 content://com.maxmpz.audioplayer.data/bookmarks
		 content://com.maxmpz.audioplayer.data/bookmarks/#
		 
		 </pre><br>

		 * Extras<br>
		 * {@code boolean paused} - (optional) default false. OPEN_TO_PLAY command starts playing the file immediately, unless "paused" extra is true<br>
		 * {@code int pos}- (optional) seek to this position in track before playing
		 * @see PowerampAPI.Track#POSITION
		 * @see PowerampAPI#EXTRA_PAUSED
		 */
		public static final int OPEN_TO_PLAY = 20;

		/**
		 * Same as @{link #OPEN_TO_PLAY}
		 */
		public static final int OPEN = 20;

		/**
		 * Extras:<br>
		 * {@link #EXTRA_ID} - long - preset ID
		 */
		public static final int SET_EQU_PRESET = 50;

		/**
		 * Extras:<br>
		 * {@link #EXTRA_VALUE} - String - equalizer values,
		 * @see PowerampAPI#ACTION_EQU_CHANGED
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
		 * {@code boolean equ}- if exists and true, equalizer is enabled<br>
		 * {@code boolean tone} - if exists and true, tone is enabled<br>
		 */
		public static final int SET_EQU_ENABLED = 53;

		/**
		 * Used by Notification controls to stop pending/paused service/playback and unload/remove notification
		 */
		public static final int STOP_SERVICE = 100;

		/**
		 * Set as active and/or import visualization preset<br><br>
		 * Depending on extras, preset will be:<br>
 		 * - EXTRA_ID - set the preset identified by ID as active (selected)<br>
 		 * - EXTRA_NAME - set the preset identified by NAME as active (selected)<br>
		 * - EXTRA_NAME (no such preset exists) and EXTRA_DATA - import EXTRA_DATA preset text into the newly created preset named EXTRA_NAME<br>
		 * - EXTRA_NAME (preset exists) and EXTRA_DATA - import EXTRA_DATA preset text into the preset identified by EXTRA_NAME<br><br>
		 * 
		 * Imported presets are stored as file in Poweramp app data directory<br><br> 
		 * Extras:<br>
		 * {@link #EXTRA_ID} - long - if exists, the preset identified by this id will be set as active and/or imported into. If this extra is missing, a new visualization preset will be added<br>
		 * {@link #EXTRA_NAME} - String - preset file name. Should follow "[artist name] - [preset name].milk" format. ".milk" extension is optional<br>
		 * {@link #EXTRA_DATA} - String - the preset data to import<br>
		 * @since 867
		 */
		public static final int SET_VIS_PRESET = 200;
		
		
		public static @NonNull String cmdToString(int cmd) {
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
				case SLEEP_TIMER:
					return "SLEEP_TIMER";
				case SET_VIS_PRESET:
					return "SET_VIS_PRESET";
				case LIKE:
					return "LIKE";
				case UNLIKE:
					return "UNLIKE";
				case TOGGLE_RATING:
					return "TOGGLE_RATING";
				case SEEK_JUMP_FORWARD:
					return "SEEK_JUMP_FORWARD";
				case SEEK_JUMP_BACKWARD:
					return "SEEK_JUMP_BACKWARD";

				default:
					return "unknown cmd=" + cmd;
			}
		}
	}

	/**
	 * Extra<br>
	 * {@code Mixed}
	 */
	public static final String EXTRA_API_VERSION = "api";

	/**
	 * Minimum allowed time between seek commands
	 */
	public static int MIN_TIME_BETWEEN_SEEKS_MS = 200;

	/**
	 * Get all, one, or multiple Poweramp preferences, available for manipulation by this API<br>
	 * contentResolver().call => CALL_PREFERENCE with optional extra bundle:<br>
	 * - null to get all the available preferences<br>
	 * - or non-null bundle with some keys - the appropriate preferences will be returned<br>
	 * @since 849
	 */
	public static final String CALL_PREFERENCE = "preference";

	/**
	 * Set one, or multiple preferences<br>
	 * contentResolver().call => CALL_SET_PREFERENCE with extra bundle, containing preference names and values to set<br>
	 * NOTE: Poweramp allows limited subset of preferences to be set this way<br>
	 * Depending on preference Poweramp may immediately apply it, apply for the next track, for the next activity restart, or for the next Poweramp process restart.<br>
	 * <b>Experimental: this part of API is currently under development and may change in the future</b><br>
	 * Returns bundle with the old values for the preferences which were set, or null on error<br>
	 * @since 862
	 */
	public static final String CALL_SET_PREFERENCE = "set_preference";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 */
	public static final String EXTRA_CONTENT = "content";

	/**
	 * Extra<br>
	 * {@code String}
	 */
	public static final String EXTRA_PACKAGE = "pak";

	/**
	 * ACTION_API_COMMAND extra
	 * <br>
	 * {@code int}
	 */
	public static final String EXTRA_SOURCE = "src";

	/**
	 * Extra<br>
	 * {@code String}
	 */
	public static final String EXTRA_LABEL = "label";

	/**
	 * Extra<br>
	 * {@code String}
	 */
	public static final String EXTRA_NAME = "name";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 */
	public static final String EXTRA_VALUE = "value";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String EXTRA_EQU = "equ";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String EXTRA_TONE = "tone";
	
	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String EXTRA_KEEP_SERVICE = "keepService";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String EXTRA_BEEP = "beep";

	/**
	 * Extra<br>
	 * {@code String}
	 * @since 795
	 */
	public static final String EXTRA_TABLE = "table";

	/**
	 * Sent by Poweramp to your app<br>
	 * Poweramp track changed intent<br>
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br>
	 * Use *_EXPLICIT version to receive this action in background app.<br><br>
	 *
	 * <ul>
	 * <li>sent when the user manually changes the track in any way
	 * <li>sent when the player finishes playing one track, and begins another
	 * <li>sent when the player starts again after being in the stopped state
	 * <ul><li>may be sent multiple times in this case</ul>
	 * </ul>

	 * Extras:<br>
	 * {@link #EXTRA_TRACK} - Bundle - Track bundle<br>
	 * {@link #EXTRA_TIMESTAMP} - long - timestamp of the event (System.currentTimeMillis())
	 * @see PowerampAPI.Track
	 */
	public static final String ACTION_TRACK_CHANGED = "com.maxmpz.audioplayer.TRACK_CHANGED";

	/**
	 * Sent by Poweramp to your app<br>
	 * This is explicit intent sent to your app to ensure it receives it on Android 8+ (with background execution limitations).<br>
	 * Differs from ACTION_TRACK_CHANGED which is sticky intent that won't be received by your app in the background in the recent Androids.<br><br>
	 *
	 * <b>NOTE: Poweramp caches app list for this intent. Cache is updated when Poweramp is started or playback resumed.</b><br>
	 * This means if your app just installed and Poweramp is playing, your app won't receive this action until next Poweramp pause/resume
	 * cycle or Poweramp service restart.<br>
	 *
	 * <ul>
	 * <li>sent when the user manually changes the track in any way
	 * <li>sent when the player finishes playing one track, and begins another
	 * <li>sent when the player starts again after being in the stopped state
	 * <ul><li>may be sent multiple times in this case</ul>
	 * </ul>
	 *
	 * Extras:<br>
	 * {@code Bundle track} - Track bundle<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 * @see PowerampAPI.Track
	 * @since 798
	 */
	public static final String ACTION_TRACK_CHANGED_EXPLICIT = "com.maxmpz.audioplayer.TRACK_CHANGED_EXPLICIT";

	/**
	 * NOTE: this intent is only sent when send_old_api_aa option (Send Album Art for old API) is enabled for compatibility with the old Poweramp APIs<br>
	 * Sent by Poweramp to your app<br>
	 * Album art was changed. Album art can be the same for whole album/folder, thus usually it will be updated less frequently comparing to TRACK_CHANGE.
	 * If both aaPath and aaBitmap extras are missing that means no album art exists for the current track(s).<br>
	 * Note that there is no direct Album Art to track relation, i.e. both track and album art can change independently from each other -
	 * for example - when new album art asynchronously downloaded from Internet or selected by the user.<br><br>
	 *
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br><br>
	 * Extras:<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 * @deprecated since 829
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public static final String ACTION_AA_CHANGED = "com.maxmpz.audioplayer.AA_CHANGED";

	/**
	 * Sent by Poweramp to your app<br>
	 * Poweramp playing state changed (paused/resumed/ended).<br>
	 * Sticky intent (can be queried for permanently stored data).<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br>
	 * Use *_EXPLICIT version to receive this action in background app.<br><br>
	 * <ul>
	 * <li>sent when the player changes status for any reason (user initiated or automatic, such as playing when headphones are connected)
	 * <li>sent when the user presses the X button in the notification
	 * <ul>
	 * <li>on this event, the player may send the paused state first
	 * <li>then shortly after, the stopped state
	 * </ul>
	 * <li>sent when the player starts back up after being in the stopped state
	 * <ul>
	 * <li>may be sent multiple times in this case
	 * </ul>
	 * <li>when the player sends the stopped state, you can unload PowerAmp related state from your app, since the next
	 * intents sent to your app will refresh it on everything it needs to know<br><br>
	 * </ul>
	 *
	 * Extras:<br>
	 * {@code int state} - one of the STATE_* values (700+)<br>
	 * {@code boolean paused} - true if track paused/stopped, false if track is playing<br>
	 * {@code int pos} - (optional) current in-track position in seconds<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())<br>
	 * {@code int status} - one of the STATUS_* values (deprecated)<br><br>
	 * @since 790 - additional extras - not sent anymore
	 */
	public static final String ACTION_STATUS_CHANGED = "com.maxmpz.audioplayer.STATUS_CHANGED";


	/**
	 * Sent by Poweramp to your app<br>
	 * This is explicit intent sent to your app to ensure it receives it on Android 8+ (with background execution limitations).
	 * Differs from ACTION_STATUS_CHANGED which is sticky intent which won't be received by your app in the background.<br><br>
	 * NOTE: this works only with the receivers registered via AndroidManifest.xml<br><br>
	 *
	 * <b>NOTE: Poweramp caches app list for this intent. Cache is updated when Poweramp is started or playback resumed.</b><br>
	 * It means if your app just installed and Poweramp is playing, your app won't receive this action until next Poweramp pause/resume cycle or Poweramp service restart.<br><br>
	 *
	 * <ul>
	 * <li>sent when the player changes status for any reason (user initiated or automatic, such as playing when headphones are connected)
	 * <li>sent when the user presses the X button in the notification
	 * <ul>
	 * <li>on this event, the player may send the paused state first
	 * <li>then shortly after, the stopped state
	 * </ul>
	 * <li>sent when the player starts back up after being in the stopped state
	 * <ul>
	 * <li>may be sent multiple times in this case
	 * </ul>
	 * <li>when the player sends the stopped state, you can unload PowerAmp related state from your app, since the next
	 * intents sent to your app will refresh it on everything it needs to know<br><br>
	 * </ul>
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
	 * Sent by Poweramp to your app<br>
	 * NON sticky intent<br>
	 * Sent by Poweramp to your app<br>
	 * Extras:<br>
	 * {@code int pos} - current in-track position in seconds
	 */
	public static final String ACTION_TRACK_POS_SYNC = "com.maxmpz.audioplayer.TPOS_SYNC";

	/**
	 * Sent by Poweramp to your app<br>
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
	 * Sent by Poweramp to your app<br>
	 * Poweramp equalizer settings changed.<br>
	 * v2: sticky intent (can be queried for permanently stored data).<br>
	 * v3: non-sticky intent<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started
	 * or some foreground service is active).</b><br><br>
	 *
	 * Extras:<br>
	 * {@code String name} - preset name. If no name extra exists, it's not a preset<br>
	 * {@code long id} - preset id. If no id extra exists, it's not a preset<br>
	 * {@code String value} - equalizer and tone values in format:<br>
	 * <blockquote><code>bass=pos_float|treble=pos_float|31=float|62=float|....|16K=float|preamp=0.0 ... 2.0</code><br>
	 * where float = -1.0 ... 1.0, pos_float = 0.0 ... 1.0</blockquote>
	 * {@code boolean equ} - true if equalizer bands are enabled<br>
	 * {@code boolean tone} - true if tone bands are enabled<br>
	 * {@code long ts} - timestamp of the event (System.currentTimeMillis())
	 */
	public static final String ACTION_EQU_CHANGED = "com.maxmpz.audioplayer.EQU_CHANGED";

	/**
	 * <b>Experimental!</b><br>
	 * Sent by Poweramp to your app<br>
	 * Poweramp just ignored media button press due to the Respond To Buttons option disabled.<br><br>
	 *
	 * <b>NOTE: on Android 8+, you'll receive this intent only if your app is on foreground (some activity started or some foreground service is active).</b><br><br>
	 *
	 * Extras:<br>
	 * - same extras as in {@link Intent#ACTION_MEDIA_BUTTON}
	 * - or just single boolean {@link #EXTRA_LONG_PRESS} extra, when Poweramp is set to receive long presses instead of voice search AND Poweramp is on background
	 * @since 875
	 * 
	 */
	public static final String ACTION_MEDIA_BUTTON_IGNORED = "com.maxmpz.audioplayer.MEDIA_BUTTON_IGNORED";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens a category list for the current track, or library - if no track is loaded<br>
	 * Should be sent with Context.startActivity
	 */
	public static final String ACTION_SHOW_CURRENT = "com.maxmpz.audioplayer.ACTION_SHOW_CURRENT";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens library<br>
	 * Should be sent with Context.startActivity
	 */
	public static final String ACTION_OPEN_LIBRARY = "com.maxmpz.audioplayer.ACTION_OPEN_LIBRARY";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens and plays given library uri (if provided)<br>
	 * Should be sent with Context.startActivity
	 * @since 978
	 */
	public static final String ACTION_OPEN_LIST_AND_PLAY = "com.maxmpz.audioplayer.ACTION_OPEN_LIST_AND_PLAY";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens search<br>
	 * Should be sent with Context.startActivity
	 */
	public static final String ACTION_OPEN_SEARCH = "com.maxmpz.audioplayer.ACTION_OPEN_SEARCH";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens equalizer screen<br>
	 * Should be sent with Context.startActivity
	 * @see #EXTRA_EQ_TAB
	 */
	public static final String ACTION_OPEN_EQ = "com.maxmpz.audioplayer.ACTION_OPEN_EQ";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens main screen<br>
	 * Should be sent with Context.startActivity
	 */
	public static final String ACTION_OPEN_MAIN = "com.maxmpz.audioplayer.ACTION_OPEN_MAIN";
	
	/**
	 * Sent by your app to Poweramp<br>
	 * Grants sender a permission for content provider data access<br>
	 * Should be sent to {@link #API_RECEIVER_NAME}, or {@link #API_ACTIVITY_NAME} with the appropriate methods (sendBroadcast, startActivity)
	 * Extras:<br>
	 * {@code String package} - the package name of app trying to get access<br>
	 * @since 797
	 */
	public static final String ACTION_ASK_FOR_DATA_PERMISSION = "com.maxmpz.audioplayer.ACTION_ASK_FOR_DATA_PERMISSION";

	/**
	 * Sent by your app to Poweramp<br>
	 * Forces Poweramp UI and PlayerService to reload data from DB<br>
	 * Should be sent to {@link #API_RECEIVER_NAME}, or {@link #API_ACTIVITY_NAME} with the appropriate methods (sendBroadcast, startActivity)
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
	public static final String EXTRA_DATA = "data";

	/**
	 * Sent by your app to Poweramp<br>
	 * Poweramp native plugin command<br>
	 * Should be sent to {@link #API_RECEIVER_NAME}, or {@link #API_ACTIVITY_NAME}, or {@link #PLAYER_SERVICE_NAME} with the appropriate methods (sendBroadcast, startActivity, startService)<br><br>
	 * Extras:<br>
	 * {@link #EXTRA_PACKAGE} - String - plugin package<br>
	 * {@link #EXTRA_COMMAND} - int - some dsp unique command. cmd should be >= 0<br>
	 * {@link #EXTRA_CONTENT} - byte[] - the command data serialized as byte array<br>
	 * @since 700
	 */
	public static final String ACTION_NATIVE_PLUGIN_COMMAND = "com.maxmpz.audioplayer.NATIVE_PLUGIN_COMMAND";
	
	/**
	 * Sent by your app to Poweramp<br>
	 * Generic search command<br>
	 * Should be sent to {@link #API_RECEIVER_NAME}, or {@link #API_ACTIVITY_NAME}, or {@link #ACTIVITY_STARTUP}, or {@link #PLAYER_SERVICE_NAME} with the appropriate methods (sendBroadcast, startActivity, startService)
	 * Extras:<br>
	 * @see <a href="https://developer.android.com/reference/android/provider/MediaStore#INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH">MediaStore#INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH</a>
	 * @since 853
	 */
	public static final String INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH = android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH;

	/**
	 * Sent by Poweramp to your app.<br>
	 * Sent when Poweramp loads/reloads its audio engine and loads the plugin.<br>
	 * In response, plugin apps should send NATIVE_PLUGIN_COMMAND to Poweramp with the initial or restored plugin parameters.<br>
	 * Extras:<br>
	 * {@link #EXTRA_API_VERSION} - int - Poweramp API version
	 * @since 700
	 */
	public static final String ACTION_NATIVE_PLUGIN_INIT = "com.maxmpz.audioplayer.NATIVE_PLUGIN_INIT";

	/**
	 * This action should be used by skin apps for their main activity intent-filter element in AndroidManifest.xml.<br>
	 * Helps future Poweramp builds (targeting Android 11/SDK=30) to find skins, as Android 11 requires some explicit action to be
	 * defined by the "plugin" apps, such as skins.
	 */
	public static final String ACTION_SKIN_MAIN = "com.maxmpz.audioplayer.SKIN_MAIN";

	/**
	 * Used internally
	 * @since 966
	 */
	public static final String ACTION_NOTIFICATION_DELETED = "com.maxmpz.audioplayer.ACTION_NOTIFICATION_DELETED";


	/**
	 * Poweramp settings activity
	 */
	public static final String ACTIVITY_SETTINGS = "com.maxmpz.audioplayer.preference.SettingsActivity";

	/**
	 * Poweramp startup activity. This activity always tries to pass incoming intent to the main activity.
	 * Always calling StartupActivity instead of main activity is preferred due to the activity animation and possible
	 * first time run setup. Also, main activity is not exposed in API by default and is a subject to change
	 */
	public static final String ACTIVITY_STARTUP = "com.maxmpz.audioplayer.StartupActivity";

	/**
	 * Extra for ACTION_API_COMMAND, RESUME command, ACTION_PLAYING_MODE_CHANGED message<br>
	 * {@code int}
	 * @see PowerampAPI.ShuffleMode
	 * @since 797
	 */
	public static final String EXTRA_SHUFFLE = "shuffle";

	/**
	 * Extra for ACTION_API_COMMAND, RESUME command. If true, Poweramp will try to resume even if it's in playing state<br>
	 * {@code boolean}
	 * @since 940
	 */
	public static final String EXTRA_IGNORE_PLAYING_STATE = "ips";

	/**
	 * Extra
	 * Int
	 * @see PowerampAPI#ACTION_OPEN_EQ
	 * @see PowerampAPI#EQ_TAB_DEFAULT
	 * @see PowerampAPI#EQ_TAB_EQUALIZER
	 * @see PowerampAPI#EQ_TAB_VOLUME
	 * @see PowerampAPI#EQ_TAB_REVERB
	 */
	public static final String EXTRA_EQ_TAB = "eqTab";

	/**
	 * Open last user opened eq tab 
	 * @see PowerampAPI#EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_DEFAULT = -1;

	/**
	 * Open equalizer tab 
	 * @see PowerampAPI#EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_EQUALIZER = 0;

	/**
	 * Open volume tab 
	 * @see PowerampAPI#EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_VOLUME = 1;

	/**
	 * Open reverb tab 
	 * @see PowerampAPI#EXTRA_EQ_TAB
	 */
	public static final int EQ_TAB_REVERB = 2;


	/**
	 * Extra<br>
	 * {@code long}
	 */
	public static final String EXTRA_TIMESTAMP = "ts";

	/**
	 * Extra<br>
	 * {@code int}
	 * @since 700
	 * @see PowerampAPI#ACTION_STATUS_CHANGED
	 * @see Lyrics#ACTION_UPDATE_LYRICS
	 */
	public static final String EXTRA_STATE = "state";

	/**
	 * Poweramp is probably not fully loaded, state is unknown
	 * @since 705
	 * @see PowerampAPI#ACTION_STATUS_CHANGED
	 */
	public static final int STATE_NO_STATE = -1;

	/**
	 * Poweramp is in stopped state - finished playing some list and stopped, or explicitly stopped by user
	 * @since 700
	 * @see PowerampAPI#ACTION_STATUS_CHANGED
	 */
	public static final int STATE_STOPPED = 0;
	/**
	 * Poweramp is playing
	 * @since 700
	 * @see PowerampAPI#ACTION_STATUS_CHANGED
	 */
	public static final int STATE_PLAYING = 1;
	/**
	 * Poweramp is paused
	 * @since 700
	 * @see PowerampAPI#ACTION_STATUS_CHANGED
	 */
	public static final int STATE_PAUSED = 2;

	/**
	 * STATUS_CHANGED extra<br>
	 * {@code boolean} - true if track is paused
	 * @see #ACTION_STATUS_CHANGED
	 * @see Commands#OPEN_TO_PLAY
	 * @see ACTION_VIEW
	 */
	public static final String EXTRA_PAUSED = "paused";

	/**
	 * ACTION_PLAYING_MODE_CHANGED extra<br>
	 * {@code int}
	 * @see PowerampAPI.RepeatMode
	 */
	public static final String EXTRA_REPEAT = "repeat";

	/**
	 * Extra
	 * {@code long}
	 */
	public static final String EXTRA_ID = "id";

	/**
	 * ACTION_TRACK_CHANGED track extra<br>
	 * {@code Bundle}
	 */
	public static final String EXTRA_TRACK = "track";

	/**
	 * Extra<br>
	 * {@code int}
	 */
	public static final String EXTRA_SECONDS = "seconds";

	/**
	 * Extra<br>
	 * {@code boolean}
	 */
	public static final String EXTRA_PLAY_TO_END = "play_to_end";

	/**
	 * Extra<br>
	 * {@code int}
	 */
	public static final String EXTRA_POSITION = "pos";

	/**
	 * Extra<br>
	 * {@code int}
	 * @since 868
	 */
	public static final String EXTRA_RELATIVE_POSITION = "rel_pos";
	
	/**
	 * Extra<br>
	 * {@code boolean}
	 * @since 868
	 */
	public static final String EXTRA_LOCK = "lock";
	
	/**
	 * Extra<br>
	 * {@code boolean}
	 * @since 887
	 */
	public static final String EXTRA_LONG_PRESS = "long_press";

	/**
	 * Shuffle extras values
	 */
	public static final class ShuffleMode {
		/**
		 * No any shuffle selected
		 */
		public static final int SHUFFLE_NONE		   = 0;
		/**
		 * "All Songs" global category shuffle
		 */
		public static final int SHUFFLE_ALL			   = 1;
		/**
		 * Just songs from current category shuffled, categories in order
		 */
		public static final int SHUFFLE_SONGS		   = 2;
		/**
		 * Categories shuffled, songs in order
		 */
		public static final int SHUFFLE_CATS		   = 3;
		/**
		 * Songs shuffled, categories are also shuffled
		 */
		public static final int SHUFFLE_SONGS_AND_CATS = 4;
		/**
		 * Max possible shuffle value
		 */
		public static final int MAX_SHUFFLE			   = 4;
		
		/**
		 * Pseudo mode just for the UI, not used as mode directly (SHUFFLE_SONGS is used internally)
		 */
		public static final int SHUFFLE_SONGS_HIER     = 5;
		
		public static boolean areSongsShuffled(int shuffle) {
			return shuffle == SHUFFLE_ALL || shuffle == SHUFFLE_SONGS || shuffle == SHUFFLE_SONGS_AND_CATS;
		}

		public static boolean areCatsShuffled(int shuffle) {
			return shuffle == SHUFFLE_CATS || shuffle == SHUFFLE_SONGS_AND_CATS;
		}
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
		 * Current song is played once, then player pauses on next song
		 */
		public static final int SINGLE_SONG    = 4;

		/**
		 * Max possible repeat value
		 */
		public static final int MAX_REPEAT     = 4;
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
	 * {@link #ACTION_TRACK_CHANGED} and {@link #ACTION_TRACK_CHANGED_EXPLICIT} extra field {@link #EXTRA_TRACK}.<br><br>
	 *
	 * Since build 948 all these fields are also exposed to {@link #ACTION_TRACK_CHANGED} and {@link #ACTION_TRACK_CHANGED_EXPLICIT}
	 * extras directly.
	 */
	public static final class Track {
		/**
		 * Max number to use for filename numbers, e.g. 1-track.mp3 is considered a track #1, but 100-track.mp3 is not
		 */
		public static final int MAX_FILE_NUMBER = 99;
		
		/**
		 * Max track tag number to use/show
		 */
		public static final int MAX_TRACK_NUMBER = 999;

		/**
		 * Rating value for the "unlike" icon
		 */
		public static final int RATING_UNLIKE = 1;

		/**
		 * Rating value for the "like" icon
		 */
		public static final int RATING_LIKE = 5;

		/**
		 * Rating value for the unset like-ness
		 */
		public static final int RATING_NOT_SET = 0;


		/**
		 * Id of the current track.
		 * Can be a playlist entry id<br>
		 * {@code long}
		 */
		public static final String ID = "id";

		/**
		 * "Real" id. In case of playlist entry, this is always resolved to Poweramp folder_files table row ID<nt>
		 * {@code long}
		 */
		public static final String REAL_ID = "realId";

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
		 * @see FileType
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
		 * Track duration in milliseconds<br>
		 * {@code int}
		 * @since 948
		 */
		public static final String DURATION_MS = "durMs";

		/**
		 * Position in track in seconds<br>
		 * NOTE: while this extra is defined within this Track class, it's always exposed as top level extra, not the
		 * track bundle extra.<br>
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
		 * One of the {@link LyricsState} values
		 * @since 941
		 */
		public static final String LYRICS_STATE = "lyricsState";


		/**
		 * {@link PowerampAPI.Track} {@link #FILE_TYPE} values
		 */
		public static class FileType {
			public static final int TYPE_UNKNOWN    = -1;
			public static final int TYPE_MP3        = 0;
			public static final int TYPE_FLAC       = 1;
			public static final int TYPE_M4A        = 2;
			public static final int TYPE_MP4        = 3;
			public static final int TYPE_OGG        = 4;
			public static final int TYPE_WMA        = 5;
			public static final int TYPE_WAV        = 6;
			public static final int TYPE_TTA        = 7;
			public static final int TYPE_APE        = 8;
			public static final int TYPE_WV         = 9;
			public static final int TYPE_AAC        = 10;
			public static final int TYPE_MPGA       = 11;
			@Deprecated
			public static final int TYPE_AMR        = 12;
			public static final int TYPE_3GP        = 13;
			public static final int TYPE_MPC        = 14;
			public static final int TYPE_AIFF       = 15;
			public static final int TYPE_AIF        = 16;
			public static final int TYPE_FLV        = 17;
			public static final int TYPE_OPUS       = 18;
			public static final int TYPE_DFF        = 19;
			public static final int TYPE_DSF        = 20;
			public static final int TYPE_MKA        = 21;
			public static final int TYPE_TAK        = 22;
			public static final int TYPE_STREAM     = 23;
			public static final int TYPE_MKV        = 24;
			public static final int TYPE_MOD        = 25;
			public static final int TYPE_XM         = 26;
			public static final int TYPE_S3M        = 27;
			public static final int TYPE_IT         = 28;
			public static final int TYPE_MPTM       = 29;
			public static final int LAST_TYPE       = 29;
		}

		/**
		 * {@link PowerampAPI.Track} {@link #FLAGS} bitset values. First 3 bits = FLAG_ADVANCE_*
		 */
		public interface Flags {
			/** Track wasn't advanced */
			public static final int FLAG_ADVANCE_NONE            = 0;

			/** Track was advanced forward */
			public static final int FLAG_ADVANCE_FORWARD         = 0x1;

			/** Track was advanced backward */
			public static final int FLAG_ADVANCE_BACKWARD        = 0x2;

			/** Track category was advanced forward */
			public static final int FLAG_ADVANCE_FORWARD_CAT     = 0x3;

			/** Track category was advanced backward */
			public static final int FLAG_ADVANCE_BACKWARD_CAT    = 0x4;

			/**
			 * Track is manually selected by user from the list
			 */
			public static final int FLAG_ADVANCE_BY_USER         = 0x5;

			/** Mask for FLAG_ADVANCE_* values */
			public static final int FLAG_ADVANCE_MASK            = 0x7;

			/**
			 * Track was advanced from the notification.<br>
			 * If set, event comes from the notification ui and we will animate aa update then
			 */
			public static final int FLAG_NOTIFICATION_UI         = 0x20;

			/** Indicates the track is the first in Poweramp service session */
			public static final int FLAG_FIRST_IN_PLAYER_SESSION = 0x40;

			/**
			 * The track failed to load for any reason
			 * @since 948
			 */
			public static final int FLAG_FAILED                  = 0x80;
		}

		public static final class TagStatus {
			public static final int TAG_NOT_SCANNED = 0;
			public static final int TAG_SCANNED = 1;

			/** Currently used only for LRC files */
			public static final int TAG_FAILED = 2;
		}

		/**
		 * Values for {@link #LYRICS_STATE}
		 * @since 948
		 */
		public static final class LyricsState {
			/**
			 * No lyrics detected or cached for the track
			 * @since 948
			 */
			public static final int LYRICS_STATE_NONE = 0;

			/**
			 * Some local or previously cached plugin lyrics are available
			 * @since 948
			 */
			public static final int LYRICS_STATE_HAS_DATA = 1;
		}

		// Deprecated

		/**
		 * @deprecated not used anymore
		 */
		@Deprecated
		public static final String TYPE = "type";
	}
	
	/**
	 * {@link PowerampAPI.Track} {@link PowerampAPI.Track#CAT} categories
	 */
	public static final class Cats {
		/** Root library category. Not used in Poweramp v3. */
		@Deprecated
		public static final int ROOT                    = 0;

		/** All Songs */
		public static final int FILES                   = 30;
		public static final int FOLDERS                 = 10;
		public static final int FOLDERS_HIER            = 20;
		public static final int ALBUMS                  = 200;
		public static final int ARTISTS                 = 500;

		/** Albums for given artist id */
		public static final int ARTISTS_ID_ALBUMS       = 220;
		public static final int ALBUM_ARTISTS           = 520;

		/** Albums for given album_artist id */
		public static final int ALBUM_ARTISTS_ID_ALBUMS = 256;

		/** Albums for given albums split by artists */
		public static final int ARTISTS__ALBUMS         = 250;
		public static final int GENRES                  = 320;
		public static final int YEARS                   = 330;

		/** Albums for given genre id */
		public static final int GENRES_ID_ALBUMS        = 210;

		/** Albums for given year id */
		public static final int YEARS_ID_ALBUMS         = 340;
		public static final int COMPOSERS               = 600;

		/** Albums for given composer id */
		public static final int COMPOSERS_ID_ALBUMS     = 230;
		public static final int PLAYLISTS               = 100;
		public static final int QUEUE                   = 800;

		/** @since 877 */
		public static final int BOOKMARKS               = 810;
		public static final int STREAM_FILES            = 60;
		public static final int MOST_PLAYED             = 43;
		public static final int TOP_RATED               = 48;
		public static final int LOW_RATED               = 50;
		public static final int RECENTLY_PLAYED         = 58;
		public static final int RECENTLY_ADDED          = 53;
		public static final int LONG                    = 55;

		private Cats() {}
	}

	/**
	 * Describes Poweramp scanner related actions.<br>
	 * Use {@link PowerampAPIHelper#getScannerServiceComponentName} to get component name for the intent. Intents should be send with Context#startService<br>
	 * Please note that service won't start/scan if your application is not foreground, or if Poweramp is not in the foreground, or if Poweramp is not actively playing. 
	 */
	public static final class Scanner {
		/**
		 * Sent by your app to Poweramp.<br>
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
		 * {@link #ACTION_TAGS_SCAN_FINISHED}, or
		 * {@link #ACTION_FAST_TAGS_SCAN_FINISHED}<br><br>
		 *
		 * Extras:<br>
		 * {@code boolean fastScan} - Poweramp will not check folders and scan files which hasn't been modified from previous scan. Based on files last modified timestamp<br><br>
		 * {@code boolean eraseTags} - Poweramp will clean all tags from existing songs. This causes each song to be re-scanned for tags.
		 *			   Warning: as a side effect, cleans CUE tracks from user created playlists. 
		 *			   This is because scanner can't incrementally re-scan CUE sheets, so they are deleted from DB, causing their
		 *			   deletion from user playlists as well<br><br>
		 * {@code boolean fullRescan} - Poweramp will also check for folders/files from missing/unmounted storages and will remove them from DB.
		 *				Warning: removed songs also disappear from user created playlists.
		 *				Used in Poweramp only when user specifically goes to Settings and does Full Rescan (after e.g. SD card change)<br><br>
		 * {@code boolean scanProviders} - if true, Poweramp scans 3rd party providers<br><br>
		 * {@code String provider} - if set, Poweramp will scan only folders matching this provider authority. scanProviders extra should be set to true  (or can be missing) in this case<br>
		 *              Warning: don't use this with fullRescan, as only the provider tracks will appear after rescan<br><br>
		 *
		 */
		public static final String ACTION_SCAN_DIRS = "com.maxmpz.audioplayer.ACTION_SCAN_DIRS";

		/**
		 * Sent by your app to Poweramp.<br>
		 * Poweramp Scanner action.<br>
		 * Secondary action, only checks songs with TAG_STATUS set to TAG_NOT_SCANNED. Useful for rescanning just songs
		 * (which are already in Poweramp DB) with edited file tag info.<br><br>
		 *
		 * Extras:<br>
		 * {@code boolean fastScan} - If true, scanner doesn't send ACTION_TAGS_SCAN_STARTED/ACTION_TAGS_SCAN_PROGRESS/ACTION_TAGS_SCAN_FINISHED
		 * intents, just sends ACTION_FAST_TAGS_SCAN_FINISHED when done.
		 *	It doesn't modify scanning logic otherwise.
		 */
		public static final String ACTION_SCAN_TAGS = "com.maxmpz.audioplayer.ACTION_SCAN_TAGS";


		/**
		 * Sent by Poweramp to your app.<br>
		 * Broadcast<br>
		 * Poweramp Scanner started folders scan<br>
		 * Sticky intent (can be queried for permanently stored data)<br>
		 */
		public static final String ACTION_DIRS_SCAN_STARTED = "com.maxmpz.audioplayer.ACTION_DIRS_SCAN_STARTED";

		/**
		 * Sent by Poweramp to your app.<br>
		 * Broadcast<br>
		 * Poweramp Scanner finished folders scan<br>
		 */
		public static final String ACTION_DIRS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_DIRS_SCAN_FINISHED";

		/**
		 * Sent by Poweramp to your app.<br>
		 * Broadcast<br>
		 * Poweramp Scanner started tag scan<br>
		 * Sticky intent (can be queried for permanently stored data)
		 */
		public static final String ACTION_TAGS_SCAN_STARTED = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_STARTED";

		/**
		 * Sent by Poweramp to your app.<br>
		 * Broadcast<br>
		 * Poweramp Scanner finished tag scan<br>
		 * Extras:<br>
		 * {@code boolean track_content_changed} - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date)
		 */
		public static final String ACTION_TAGS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_FINISHED";

		/**
		 * Sent by Poweramp to your app.<br>
		 * Broadcast<br>
		 * Poweramp Scanner finished fast tag scan. Only fired when ACTION_SCAN_TAGS is called with extra fastScan = true<br>
		 * Extras:<br>
		 * {@code boolean trackContentChanged} - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date)
		 */
		public static final String ACTION_FAST_TAGS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_FAST_TAGS_SCAN_FINISHED";

		/**
		 * If true, FolderScanner tries to skip unmodified folders/files. Either folder/file is scanned or not is based on
		 * last modified time for that folder or file.<br>
		 * If given folder is not modified, Poweramp won't further look into that folder (direct children) files.<br>
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
		 * {@code boolean} - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date)<br>
		 */
		public static final String EXTRA_TRACK_CONTENT_CHANGED = "trackContentChanged";

		/**
		 * If true, LibraryScanner will first clear all track metadata prior the scan, causing total tags rescanning.<br>
		 * That will also force re-parse standalone CUEs and file based playlists<br>
		 * Extra<br>
		 * {@code boolean}
		 */
		public static final String EXTRA_ERASE_TAGS = "eraseTags";

		/**
		 * If true, FolderScanner will scan unmounted storages - removing folders, playlists, tracks entries which were located on them, but now missing due to the missing storage<br>
		 * Extra<br>
		 * {@code boolean}
		 */
		public static final String EXTRA_FULL_RESCAN = "fullRescan";

		/**
		 * If true, force LibraryScanner to resolve playlists<br>
		 * Extra<br>
		 * {@code boolean}
		 */
		public static final String EXTRA_RESOLVE_PLAYLISTS = "resolvePlaylists";

		/**
		 * If true, force LibraryScanner to import system playlists<br>
		 * Extra<br>
		 * {@code boolean}
		 * @since 841
		 */
		public static final String EXTRA_IMPORT_SYSTEM_PLAYLISTS= "importSystemPlaylists";

		/**
		 * If true, force file based playlist re-parsing<br>
		 * Extra<br>
		 * {@code boolean}
		 * @since 842
		 */
		public static final String EXTRA_REPARSE_PLAYLISTS= "reparsePlaylists";
		
		/**
		 * If true, Poweramp scans 3rd party providers. Default is true (if the extra is missing)<br>
		 * Extra for {@link #ACTION_SCAN_DIRS}<br>
		 * {@code boolean}
		 * @since 862
		 */
		public static final String EXTRA_SCAN_PROVIDERS = "scanProviders";

		/**
		 * If true, force rescan tracks with possible synchronized lyrics tags, e.g. SYLT.<br>
		 * Extra for {@link #ACTION_SCAN_DIRS}, {@link #ACTION_SCAN_TAGS}<br>
		 * {@code boolean}
		 * @since 948
		 */
		public static final String EXTRA_RESCAN_LYRICS_TAGS = "rescanLyricsTags";

		/**
		 * If set, Poweramp will scan only folders matching this provider authority. scanProviders extra should be set to true (or missing)
		 * in this case<br>
		 * Warning: don't use this with {@link #EXTRA_FULL_RESCAN}, as only the provider tracks will appear after such rescan<br>
		 * Extra for {@link #ACTION_SCAN_DIRS}<br>
		 * {@code String}
		 * @since 862
		 */
		public static final String EXTRA_PROVIDER = "provider";

		/**
		 * If set, and {@link #EXTRA_PROVIDER} is specified, Poweramp will scan only sub-hierarchy of folders matching this folder
		 * path for the given provider.<br><br>
		 *
		 * The path format is {@code /opaque-treeId/opaque-documentId/}:<br>
		 * - {@code opaque-treeId} is Uri.encoded treeId as returned from tree uri by
		 *   Uri.encode({@link android.provider.DocumentsContract#getTreeDocumentId})<br>
		 * - {@code opaque-documentId} is Uri.encoded documentId as returned from documentId uri by
		 *   Uri.encode({@link android.provider.DocumentsContract#getDocumentId})<br><br>
		 *
		 * The path is case-sensitive.<br><br>
		 *
		 * For example, for the folder URI {@code content://provider/tree/root1/my%2Fpath}, EXTRA_PATH is "root1/my%2Fpath/".<br><br>
		 *
		 * Note, that EXTRA_PATH assumes provider paths have some sort of hierarchy in them as GLOB pattern matching is used to
		 * match subfolders.
		 * @since 869
		 */
		public static final String EXTRA_PATH = "path";

		/**
		 * Extra<br>
		 * {@code String} - cause of the scan (e.g. user request, auto scan, etc.). Useful for debugging, visible in logcat
		 */
		public static final String EXTRA_CAUSE = "cause";


		// Deprecated

		/**
		 * Sent by Poweramp to your app.<br>
		 * Broadcast<br>
		 * @deprecated not used anymore
		 */
		@Deprecated
		public static final String ACTION_TAGS_SCAN_PROGRESS = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_PROGRESS";
	}
	
	/**
	 * Describes Poweramp Milk (visualization) scanner related actions.<br>
	 * Use {@link PowerampAPIHelper#getScannerServiceComponentName} to get component name for the intent. Intents should be send with Context#startService<br>
	 * Please note that service won't start/scan if your application is not foreground, or if Poweramp is not in the foreground, or if Poweramp is not actively playing. 
	 */
	public static class MilkScanner {
		/**
		 * Sent by your app to Poweramp. Should be sent to {@link PowerampAPIHelper#getMilkScannerServiceComponentName} service via startService (deprecated),
		 * or (since 868) to {@link #API_ACTIVITY_NAME} via startActivity or {@link #API_RECEIVER_NAME} via sendBroadcast<br>
		 * Extras:<br>
		 * {@link #EXTRA_CAUSE}
		 */
		public static final String ACTION_SCAN = "com.maxmpz.milk.ACTION_SCAN";

		/**
		 * Extra<br>
		 * {@code String} - cause of the scan (e.g. user request, auto scan, etc.). Useful for debugging, visible in logcat
		 */
		public static final String EXTRA_CAUSE = "cause";

		/**
		 * Extra<br>
		 * {@code String} - your app package name
		 */
		public static final String EXTRA_PACKAGE = "pak";
	}

	/**
	 * Describes simple Poweramp Lyrics API for lyrics loading plugin.<br><br>
	 * Your plugin, usually in response to Poweramp {@link Lyrics#ACTION_NEED_LYRICS} or in response to any other event as your plugin seems
	 * fit, may send {@link Lyrics#ACTION_UPDATE_LYRICS} intent with the appropriate extras filled to:
	 * <ul>
	 * <li>optionallu indicate your plugin is loading lyrics for the given track
	 * <li>update loaded lyrics for the track or indicate a loading failure
	 * </ul>
	 * NOTE: Poweramp won't update lyrics if lyrics already exist for the given track (previously updated by some lyrics plugin)
	 * unless user specifically refreshes lyrics for the given track. Refreshing the lyrics action causes {@link Lyrics#ACTION_NEED_LYRICS}
	 * being resent again
	 *
	 * @since 948
	 */
	public static class Lyrics {
		/**
		 * Sent by Poweramp to your app when lyrics are required for the track.<br>
		 * Sent, for example, when track has no lyrics, but lyrics mode is enabled, or
		 * when user explicitly requesting the lyrics for a track.<br><br>
		 *
		 * In response, your app can send one or multiple {@link #ACTION_UPDATE_LYRICS} intents
		 * to update lyrics state and/or data for the track.<br><br>
		 *
		 * Poweramp shows lyrics loading progress for a few seconds, after that "no lyrics found" message is shown,
		 * but the lyrics plugin is free to take more time as needed (e.g. on slow internet connection).<br><br>
		 *
		 * If multiple plugins are installed, Poweramp will only request lyrics from one of them. No priority of
		 * such lyrics plugins is defined at this time.<br><br>
		 *
		 * This is an explicit intent - it's sent specifically to your plugin component.
		 * See {@link #ACTION_TRACK_CHANGED_EXPLICIT} for explanation how exactly this event is sent to the background (plugin) apps.<br><br>
		 *
		 * Extras: Poweramp fills all {@link Track} values as extras to this intent
		 */
		public static final String ACTION_NEED_LYRICS = "com.maxmpz.audioplayer.ACTION_NEED_LYRICS";

		/**
		 * Sent by your app to Poweramp.<br>
		 * Changes lyrics or lyrics state for the track.<br><br>
		 *
		 * You can send lyrics data immediately or you can do a some long request over internet.
		 * Poweramp will show appropriate progress for loading for few seconds and then will show "no lyrics found",
		 * but ACTION_UPDATE_LYRICS will be processed by Poweramp anyway, updating the lyrics for the track.<br><br>
		 *
		 * User may re-request lyrics for the same track even if previously no any lyrics were found or updated by plugin for
		 * that track.<br><br>
		 *
		 * <b>To update lyrics content for the track:</b>
		 * <ul><li>send ACTION_UPDATE_LYRICS:<br>
		 *          {@link #EXTRA_ID}: Long - Track.REAL_ID<br>
		 *          {@link #EXTRA_LYRICS}: String - lyrics content plain text or LRC</ul>
		 *
		 * <b>To indicate failed request and stop loading progress (optional):</b>
		 * <ul><li>send ACTION_UPDATE_LYRICS:<br>
		 *          {@link #EXTRA_ID}: Long - Track.REAL_ID<br>
		 *          {@link #EXTRA_LYRICS}: String - explicit null as extra value or no EXTRA_LYRICS extra at all</ul>
		 * 
		 *  NOTE:
		 *  <ul>
		 *  <li>if multiple plugins send ACTION_UPDATE_LYRICS, the last received intent with non-empty lyrics wins
		 *  <li>empty or very small EXTRA_LYRICS is ignored
		 *  <li>if the track already has some cached lyrics, lyrics may be updated by another valid ACTION_UPDATE_LYRICS intent coming later
		 *  <li>you can send ACTION_UPDATE_LYRICS anytime, even if Poweramp hasn't requested anything.
		 *      The lyrics for the given track will be updated if the lyrics data is valid
		 *  <li>Poweramp shows link to the plugin which previously updated the lyrics with the plugin icon and name. Clicking the link
		 *      opens ACTION_LYRICS_LINK activity in your plugin with EXTRA_TRACK describing the track, if the activity is provided
		 *      by the plugin.
		 *  </ul>
		 *
		 * Extras: {@link #EXTRA_ID}, {@link #EXTRA_LYRICS}, {@link #EXTRA_INFO_LINE}<br>
		 */
		public static final String ACTION_UPDATE_LYRICS = "com.maxmpz.audioplayer.ACTION_UPDATE_LYRICS";

		/**
		 * Used by Poweramp to open plugin activity when user clicks plugin link for the lyrics previously updated by your plugin.<br>
		 * Extras: {@link #EXTRA_TRACK}
		 */
		public static final String ACTION_LYRICS_LINK = "com.maxmpz.audioplayer.ACTION_LYRICS_LINK";

		/**
		 * Extra for {@link #ACTION_UPDATE_LYRICS}. Contains lyrics data as plain text or text in LRC format.<br>
		 * {@code String}
		 */
		public static final String EXTRA_LYRICS = "lyrics";

		/**
		 * Extra for {@link #ACTION_UPDATE_LYRICS}.
		 * 3rd party plugin info line text, shown as last line in Poweramp lyrics. Can be copyright or other similar
		 * additional short info text<br>
		 * {@code String}
		 */
		public static final String EXTRA_INFO_LINE = "info_line";
	}
	

	/**
	 * Settings related actions
	 */
	public static class Settings {
		/**
		 * Poweramp settings activity
		 */
		public static final String ACTIVITY_SETTINGS = PowerampAPI.ACTIVITY_SETTINGS;

		/**
		 * Sent by your app to Poweramp.<br>
		 * Exports Poweramp settings<br>
		 * Target URI for file should be set via {@link Intent#setData} with the appropriate permissions given to Poweramp process
		 * Extras: {@link #EXTRA_UI}
		 * @deprecated disabled starting from build-912
		 */
		@Deprecated
		public static final String ACTION_EXPORT_SETTINGS = "com.maxmpz.audioplayer.ACTION_EXPORT_SETTINGS";

		/**
		 * Sent by your app to Poweramp.<br>
		 * Imports Poweramp settings
		 * @deprecated disabled starting from build-912
		 */
		@Deprecated
		public static final String ACTION_IMPORT_SETTINGS = "com.maxmpz.audioplayer.ACTION_IMPORT_SETTINGS";

		/**
		 * Extra for {@link #ACTION_EXPORT_SETTINGS}, {@link #ACTION_IMPORT_SETTINGS}<br>
		 * {@code boolean} if true, UI may be shown for import / export errors, otherwise import / export may fail silently
		 */
		public static final String EXTRA_UI = "ui";

		/**
		 * Value for EXTRA_OPEN - opens skins list
		 * @see PowerampAPI#ACTIVITY_SETTINGS
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
		 * @see PowerampAPI#ACTIVITY_SETTINGS
		 * @since 700
		 */
		public static final String EXTRA_OPEN = "open";
		
		/**
		 * Extra for ACTIVITY_SETTINGS<br>
		 * {@code String}
		 * @see PowerampAPI#ACTIVITY_SETTINGS
		 * @since 820
		 */
		public static final String EXTRA_OPEN_PATH = "open_path";
		
		/**
		 * Extra for ACTIVITY_SETTINGS<br>
		 * {@code boolean} if true and EXTRA_OPEN_PATH was used, pressing back will return back to the activity it was started. Otherwise by default Poweramp "restores" appropriate
		 * parent settings page
		 * @since 842
		 */
		public static final String EXTRA_NO_BACKSTACK = "no_backstack";

		/**
		 * Value for EXTRA_OPEN - opens vis presets list
		 * @see PowerampAPI#ACTIVITY_SETTINGS
		 * @since 700
		 */
		public static final String OPEN_VIS = "vis";

		/**
		 * Extra for ACTIVITY_STARTUP and ACTIVITY_SETTINGS<Br>
		 * If this is specified with EXTRA_SKIN_STYLE_ID, Poweramp will attempt to change skin as commanded, but on any failure, default skin is activated<br>
		 * Can be also specified for com.maxmpz.audioplayer.SettingsActivity (with EXTRA_OPEN=theme) to scroll to that skin in skins list<br><br>
		 *
		 * {@code String} - Skin APK package name
		 * @see PowerampAPI#ACTIVITY_STARTUP, PowerampAPI.ACTIVITY_SETTINGS
		 * @since 795
		 */
		public static final String EXTRA_SKIN_PACKAGE = "theme_pak";

		/**
		 * Can be specified for com.maxmpz.audioplayer.SettingsActivity and open/theme extras to scroll to given skin in skins list<Br>
		 * {@code integer} - theme resource id
		 * @since 795
		 */
		public static final String EXTRA_SKIN_STYLE_ID = "theme_id";

		/**
		 * Can be specified for com.maxmpz.audioplayer.SettingsActivity and open/theme extras to open given page <br>
		 * {@code String} - page path. Each page on each level indexed starting from 0. For example, 0/2 means page #3 inside page #1
		 * @since 865
		 */
		public static final String EXTRA_SKIN_PAGE_PATH = "theme_page_path";

		/**
		 * Limited subset of preferences allowed to be set by {@link #CALL_SET_PREFERENCE}.<br>
		 * NOTE: preferences defined as static field with the preference name and type as a reference<br>
		 * This <b>can't</b> be used for actual preference reading/writing, i.e.:<br>
		 * {@code Poweramp.Preferences.dvc_enabled = true;}<br>
		 * will do nothing<br><br>
		 *
		 * <b>Experimental: this part of API is currently under development and may/will change in the future</b><br>
		 * This class entries/types/values may change in the next Poweramp builds without prior warning/deprecation<br>
		 * @since 862
		 * @deprecated as preferences change often between Poweramp releases, static definitions do not work.
		 *             Instead, retreive the available subset of Poweramp preferences via {@link #CALL_PREFERENCE}
		 */
		@Deprecated
		public static class Preferences {
		}
		
		public static class PreferencesConsts {
			/** 
			 * Don't show vis<br>
			 */
			public static final int VIS_MODE_VIS_NONE = 0; 

			/**
			 * Vis + UI<br>
			 */
			public static final int VIS_MODE_VIS_W_UI = 1; 

			/**
			 * Just Vis<br>
			 */
			public static final int VIS_MODE_VIS      = 2;

			public static final int VIS_MODE_MAX      = 2;
		}
	}


	// Deprecated =============================================

	/**
	 * Poweramp package name.<br>
	 * NOTE: some Poweramp editions may have different package name (e.g. com.maxmpz.audioplayer.huawei)
	 * @deprecated see PowerampAPIHelper.getPowerampPackageName
	 */
	@Deprecated
	public static final String PACKAGE_NAME = "com.maxmpz.audioplayer";

	/**
	 * Poweramp service ComponentName
	 * @deprecated see {@link PowerampAPIHelper#getPlayerServiceComponentName}
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	public static final ComponentName PLAYER_SERVICE_COMPONENT_NAME = new ComponentName(PACKAGE_NAME, PLAYER_SERVICE_NAME);

	/**
	 * @return ready to use Intent for Poweramp service
	 * @deprecated see {@link PowerampAPIHelper#getPlayerServiceComponentName}, {@link PowerampAPIHelper#newAPIIntent}
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	public static Intent newAPIIntent() {
		return new Intent(ACTION_API_COMMAND).setComponent(PLAYER_SERVICE_COMPONENT_NAME);
	}

	/**
	 * @deprecated use {@link #EXTRA_COMMAND}
	 */
	@Deprecated
	public static final String COMMAND = "cmd";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 * @deprecated use {@link #EXTRA_API_VERSION}
	 */
	@Deprecated
	public static final String API_VERSION = "api";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 * @deprecated use {@link #EXTRA_CONTENT}
	 */
	@Deprecated
	public static final String CONTENT = "content";

	/**
	 * Extra<br>
	 * {@code String}
	 * @deprecated use {@link #EXTRA_PACKAGE}
	 */
	@Deprecated
	public static final String PACKAGE = "pak";

	/**
	 * ACTION_API_COMMAND extra
	 * <br>
	 * {@code int}
	 * @deprecated use {@link #EXTRA_SOURCE}
	 */
	@Deprecated
	public static final String SOURCE = "src";

	/**
	 * Extra<br>
	 * {@code String}
	 * @deprecated use {@link #EXTRA_LABEL}
	 */
	@Deprecated
	public static final String LABEL = "label";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated not used now
	 */
	@Deprecated
	public static final String AUTO_HIDE = "autoHide";

	/**
	 * Extra<br>
	 * {@code Bitmap}
	 * @deprecated not used now
	 */
	@Deprecated
	public static final String ICON = "icon";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated not used now
	 */
	@Deprecated
	public static final String MATCH_FILE = "matchFile";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated not used anymore
	 */
	@Deprecated
	public static final String SHOW_TOAST = "showToast";

	/**
	 * Extra<br>
	 * {@code String}
	 * @deprecated use {@link #EXTRA_NAME}
	 */
	@Deprecated
	public static final String NAME = "name";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 * @deprecated use {@link #EXTRA_VALUE}
	 */
	@Deprecated
	public static final String VALUE = "value";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated use {@link #EXTRA_EQU}
	 */
	@Deprecated
	public static final String EQU = "equ";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated use {@link #EXTRA_TONE}
	 */
	@Deprecated
	public static final String TONE = "tone";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated use {@link #EXTRA_KEEP_SERVICE}
	 */
	@Deprecated
	public static final String KEEP_SERVICE = "keepService";

	/**
	 * Extra<br>
	 * {@code boolean}
	 * @deprecated use {@link #EXTRA_BEEP}
	 */
	@Deprecated
	public static final String BEEP = "beep";

	/**
	 * Extra<br>
	 * {@code String}
	 * @since 795
	 * @deprecated use {@link #EXTRA_TABLE}
	 */
	@Deprecated
	public static final String TABLE = "table";

	/**
	 * Uri query parameter - filter. Currently used only for the search uri
	 * @deprecated not used anymore
	 */
	@Deprecated
	public static final String PARAM_FILTER = "flt";

	/**
	 * Sent by your app to Poweramp<br>
	 * Opens library<br>
	 * Should be sent with Context.startActivity
	 * @deprecated Use {@link #ACTION_OPEN_LIBRARY}
	 */
	@Deprecated
	public static final String ACTION_SHOW_LIST = "com.maxmpz.audioplayer.ACTION_SHOW_LIST";

	/**
	 * Extra<br>
	 * {@code Mixed}
	 * @deprecated Use {@link #EXTRA_DATA}
	 */
	@Deprecated
	public static final String DATA = "data";

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
	 * Extra<br>
	 * {@code String}
	 * @deprecated not used anymore
	 */
	@Deprecated
	public static final String ALBUM_ART_PATH = "aaPath";

	/**
	 * Extra<br>
	 * {@code Bitmap}
	 * @deprecated not used anymore by default
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
	 * @deprecated use {@link #EXTRA_TIMESTAMP}
	 */
	@Deprecated
	public static final String TIMESTAMP = "ts";

	/**
	 * Extra<br>
	 * {@code int}
	 * @since 700
	 * @see PowerampAPI#ACTION_STATUS_CHANGED
	 * @deprecated use {@link #EXTRA_STATE}
	 */
	@Deprecated
	public static final String STATE = "state";

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
	 * @deprecated (since 790) not sent anymore
	 */
	@Deprecated
	public static final String FAILED = "failed";

	/**
	 * STATUS_CHANGED extra<br>
	 * {@code boolean} - true if track is paused
	 * @see #ACTION_STATUS_CHANGED
	 * @deprecated use {@link #EXTRA_PAUSED}
	 */
	@Deprecated
	public static final String PAUSED = "paused";

	/**
	 * ACTION_PLAYING_MODE_CHANGED extra<br>
	 * {@code int}
	 * @see PowerampAPI.ShuffleMode
	 * @deprecated use {@link #EXTRA_SHUFFLE}
	 */
	@Deprecated
	public static final String SHUFFLE = "shuffle";

	/**
	 * ACTION_PLAYING_MODE_CHANGED extra<br>
	 * {@code int}
	 * @see PowerampAPI.RepeatMode
	 * @deprecated use {@link #EXTRA_REPEAT}
	 */
	@Deprecated
	public static final String REPEAT = "repeat";

	/**
	 * Extra<br>
	 * {@code long}
	 * @deprecated Use {@link #EXTRA_ID}
	 */
	@Deprecated
	public static final String ID = "id";

	/**
	 * STATUS_CHANGED track extra<br>
	 * {@code Bundle}
	 * @deprecated use {@link #EXTRA_TRACK}
	 */
	@Deprecated
	public static final String TRACK = "track";
}
