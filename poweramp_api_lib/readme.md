[TOC levels=3]:# "Poweramp Intent APIs"

# Poweramp Intent APIs
- [Android 11/targetSdkVersion 30 support](#android-11targetsdkversion-30-support)
- [Tasker and Similar Automation Apps Integration](#tasker-and-similar-automation-apps-integration)
  - [Sending Commands](#sending-commands)
  - [Receiving Status](#receiving-status)
  - [Track Extras](#track-extras)
  - [Example: parsing Poweramp track info and showing it in a toast popup](#example-parsing-poweramp-track-info-and-showing-it-in-a-toast-popup)
  - [Other Relevant Poweramp APIs](#other-relevant-poweramp-apis)

Poweramp Intent based APIs are focused on simplicity: you can just throw a simple Intent to Poweramp to play something, command Poweramp to do something,
or get some Poweramp state from the published Intents.
While being simple the Intent APIs provide almost complete control over Poweramp.
The intents can be sent programmatically or by the automation apps.

See [src/com/maxmpz/poweramp/player/PowerampAPI.java](src/main/java/com/maxmpz/poweramp/player/PowerampAPI.java) comments for reference.

### Android 11/targetSdkVersion 30 support
All sample projects and PowerampAPI is built with targetSdkVersion 30. Poweramp is built with targetSdkVersion 30
starting from the build 911.  
Due to the package visibility changes on Android 11 for the apps compiled with targetSdkVersion 30, ensure your
skin AndroidManifest.xml contains:
* queries tag:
```xml
<queries>
    <intent>
        <action android:name="com.maxmpz.audioplayer.API_COMMAND"/>
    </intent>
</queries>
```
Note that poweramp_api_lib AndroidManifest.xml contains the queries element and it should be automatically merged
to your app/plugin - please verify that by the reviewing Merged Manifest bottom tab for the project AndroidManifest.xml.

## Tasker and Similar Automation Apps Integration

### Sending Commands
You can send Poweramp commands as intents.
The main command action here is `com.maxmpz.audioplayer.API_COMMAND`, which can be send to service (com.maxmpz.audioplayer.player.PlayerService), 
activity (com.maxmpz.audioplayer.player.PowerampAPIActivity, build 855+), or broadcast receiver (com.maxmpz.audioplayer.player.PowerampAPIReceiver, build 855).

Example of simple tasker action:
- Task Edit / Add Action 
- System / Send Intent
- Action: com.maxmpz.audioplayer.API_COMMAND
- Extra: cmd:TOGGLE_PLAY_PAUSE
- Package: com.maxmpz.audioplayer
- Target: service

This will send TOGGLE_PLAY_PAUSE (1) command. See other commands here: [PowerampAPI.java around line 206](src/main/java/com/maxmpz/poweramp/player/PowerampAPI.java#L206).

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

The %variables are taken directly from receive intent. See here for other extras:
[PowerampAPI.java around line 730](src/main/java/com/maxmpz/poweramp/player/PowerampAPI.java#L730).

### Track Extras
Since build 948 all track extras are also exposed within STATUS_CHANGED extras, so all of them, such as title, album, artist, etc.,
are available as %title, %album, %artist, etc.
See here for that track info extras: [PowerampAPI.java around line 1251](src/main/java/com/maxmpz/poweramp/player/PowerampAPI.java#L1251).

Previously, before build 948, track info was exposed in "track" extra as 2nd level bundle, which may be hard to extract in Tasker
without using (rather complicated) Java/JavaScript actions.

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
* Poweramp supports querying its database via **content provider** with REST like URIs. Please see [src/com/maxmpz/poweramp/player/PowerampAPI.java](src/main/java/com/maxmpz/poweramp/player/PowerampAPI.java) for reference
  * inaccurate data updates may corrupt Poweramp database or break Library consistency
  * **Android 8+**: permission should be requested from Poweramp: see *PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION*
* Poweramp v3 exposes current playing track album art via **content://com.maxmpz.audioplayer.aa/** URIs. Album art is delivered in its original form "as is"
  (*ParcelFileDescriptor* either to the cached image file or to the embedded album art)
  * Poweramp v3 (**build 817+**) also provides album art, artist, genres, etc. images for any "entity" (track, album, genre, artist, etc.) in their original form via REST like URIs.
    (These uris are used for MediaBrowserCompat resulting lists via *MediaBrowserCompat.MediaItem.getDescription().getIconUri()*)

