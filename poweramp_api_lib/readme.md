# Poweramp Intent APIs
============================================

Poweramp v2 and v3 Intent based APIs are focused on simplicity: you can just throw a simple Intent to Poweramp to play something, command Poweramp to do something,
or get some Poweramp state from the published Intents.
While being simple Intent APIs provide almost complete control over Poweramp.

See [src/com/maxmpz/poweramp/player/PowerampAPI.java](src/com/maxmpz/poweramp/player/PowerampAPI.java) comments for reference.

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
  (*ParcelFileDescriptor* eitger to the cached image file or to the embedded album art)
  * Poweramp v3 (**build 817+**) also provides album art, artist, genres, etc. images for any "entity" (track, album, genre, artist, etc.) in their original form via REST like URIs.
    (These uris are used for MediaBrowserCompat resulting lists via *MediaBrowserCompat.MediaItem.getDescription().getIconUri()*)

