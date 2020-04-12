# Poweramp Intent APIs
============================================

Poweramp v2 and v3 Intent based APIs are focused on simplicity: you can just throw a simple Intent to Poweramp to play something, command Poweramp to do something,
or get some Poweramp state from the published Intents.
While being simple Intent APIs provide almost complete control over Poweramp.

See [src/com/maxmpz/poweramp/player/PowerampAPI.java](src/com/maxmpz/poweramp/player/PowerampAPI.java) comments for reference.

## Tasker and Similar Apps Integration

### Sending Commands
You can send Poweramp commands as intents.
The main command action here is `com.maxmpz.audioplayer.API_COMMAND`, which can be send to service (com.maxmpz.audioplayer.player.PlayerService), 
activity (com.maxmpz.audioplayer.player.PowerampAPIActivity, build 855+), or broadcast receiver (com.maxmpz.audioplayer.player.PowerampAPIReceiver, build 855).

Example of simple tasker action:
- Task Edit / Add Action 
- System / Send Intent
- Action: com.maxmpz.audioplayer.API_COMMAND
- Extra: cmd:1
- Package: com.maxmpz.audioplayer
- Target: service

This will send TOGGLE_PLAY_PAUSE (1) command. See other commands here: [PowerampAPI.java around line 206](src/com/maxmpz/poweramp/player/PowerampAPI.java#L206). 

### Receiving Status

You can receive Poweramp status via broadcast intents.

Example of simple tasker intent receiver:
- Profile / Add Profile / Event
- System / Intent Received
- Action: com.maxmpz.audioplayer.STATUS_CHANGED
- New Task / enter e.g. "Show Popup"
- + / Alert / Popup 
- Title: Status Changed
- Text: State=%state Paused=%paused

The %variables are taken directly from receive intent. See here for other extras: [PowerampAPI.java around line 667](src/com/maxmpz/poweramp/player/PowerampAPI.java#L667).

### Example: parsing Poweramp track info and showing it in a toast popup
(Courtesy of Clever_man)

```
Profile: TrackChanged (16)
    Event: Intent Received [ Action:com.maxmpz.audioplayer.TRACK_CHANGED Cat:None Cat:None Scheme:* Mime Type:* ]
   	
    Enter: FlashInfo (19)
    	A1: JavaScriptlet [ Code:var regex = new RegExp(/([A-Za-z]*)=(.*?),/g);
    while (matcher = regex.exec(track)) {
      setLocal(matcher[1].toLowerCase(), matcher[2]);
    } Libraries: Auto Exit:On Timeout (Seconds):45 ] 
    	A2: Flash [ Text:%artist %title Long:Off ]
```


### Other Relevant Poweramp APIs

* Poweramp v3 (**build 817+**) implements **MediaSessionCompat, MediaBrowserServiceCompat** APIs:
  * you can get Poweramp mediasession reference via MediaBrowser/MediaBrowserCompat and control Poweramp via MediaSessionCompat/MediaSession
  * MediaSessionCompat:
    * Poweramp supports everything except *AddQueueItem, RemoveQueueItem* - as Poweramp now playing lists are usually non-modifiable + lack of apps using this functionality,
      *onSetCaptioningEnabled* - not relevant for a music player
  * MediaBrowserCompat:
    * Poweramp supports everything, but resulting list item number is limited (depending on the connected client, limit is up to 800 items) as MediaBrowserCompat API is not intended for
      very large lists, while Poweramp categories are unlimited
    * the resulting lists are modified to include relevant Shuffle, All Songs, etc. actions. For raw datasets, please see Poweramp content provider API
* old Poweramp v3 builds (from Alphas to build 816) partially implements **MediaSession**:
  * current played track info is exposed
  * commands are processed only via *onMediaButtonEvent*
* Poweramp supports querying its database via **content provider** with REST like URIs. Please see [src/com/maxmpz/poweramp/player/PowerampAPI.java](src/com/maxmpz/poweramp/player/PowerampAPI.java) for reference
  * inaccurate data updates may corrupt Poweramp database or break Library consistency
  * **Android 8+**: permission should be requested from Poweramp: see *PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION*
* Poweramp v3 exposes current playing track album art via **content://com.maxmpz.audioplayer.aa/** URIs. Album art is delivered in its original form "as is"
  (*ParcelFileDescriptor* either to the cached image file or to the embedded album art)
  * Poweramp v3 (**build 817+**) also provides album art, artist, genres, etc. images for any "entity" (track, album, genre, artist, etc.) in their original form via REST like URIs.
    (These uris are used for MediaBrowserCompat resulting lists via *MediaBrowserCompat.MediaItem.getDescription().getIconUri()*)

