# Poweramp Track Provider API
============================================

Poweramp v3 (build 862+) supports externally provided tracks which are shown along other tracks in Poweramp Library categories, including Folders and Folders Hierarchy.

This API enables scenarios like providing cloud-based tracks, streamed/cached tracks, file system, http hosted tracks, or other virtual track hierarchies into Poweramp Library.
Poweramp treats such tracks as usual tracks within Poweramp Library categories, they appear the same way the file system tracks appear
(Poweramp just adds the provider app name label to such tracks).

Please note that file-backed tracks have the best support: if track file is on the file system somewhere on the device, or in other words has seekable file descriptor,
then features like wave-seekbar, seeking in general, local tag reading are enabled.

It's also possible to have URLs to http/https - that can be almost any supported file format on http(s) or or hls stream.

Please create github issue if other streaming formats are needed,
or URL is not enought to access a track and custom headers and/or cookies are required as well.

Your provider still can provide m3u8/pls playlists and those will be appropriately processed, providing http stream tracks in the Poweramp Streams category and in the Playlists.

The API is not completely custom and based on combination of the standard Android APIs:
* [Storage Access Framework/SAF](https://developer.android.com/guide/topics/providers/document-provider)
* [DocumentsContract](https://developer.android.com/reference/android/provider/DocumentsContract)

Due to the standard APIs used the resulting provider plugin can be potentially used by other apps, provided they implement SAF/DocumentsContract APIs.

## Implementing Poweramp Track Provider Plugin

### Basics

Please be sure to review the
[Storage Access Framework/SAF](https://developer.android.com/guide/topics/providers/document-provider)
API basics that defines *Document Provider* (the plugin to be implemented), and *Client app* (Poweramp), and [this Document Provider guide](https://developer.android.com/guide/topics/providers/create-document-provider).
Document Provider provides information about tracks hierarchy and the metainformation (album art, track tags, such as title, album, artist, etc.)

Track Provider is added to Poweramp via Poweramp *Music Folders* dialog with the plus (+) button. The + button opens standard Android *Picker* dialog which allows user to select the Tracks Provider Plugin *Root* and
optionally sub-folder.

### Scanning

Poweramp scans Track Provider Plugin tracks in two phases:
* hierarchy scan
  * establishes folder and tracks hierarchy
  * adds new tracks, removes deleted tracks to/from Poweramp Library
  * updates last modified timestaps
* metadata scan
  * metadata retrieved for the new or modified tracks (based on last modified timestamp)

**Important:** each scan is executed in multithreaded manner, thus all related API Provider code should be thread safe  
(Android SAF Provider architecture already implies that and requires thread safe implementation).

### Icon

Poweramp shows Provider app icon where appropriate (e.g. Music Folders dialog, Info/Tags dialog, etc.)

### Provider Tracks In The Poweramp Library

Poweramp categorizes provider tracks the same way it does that for the usual file system tracks - tracks are available in All Songs, Folders, Folders Hierarchy, and other categories, according
to the tags/track metadata, playlists and playlist stream entries are available in the Playlist and Streams categories.

### URL Tracks
Poweramp supports static and dynamic URL tracks, which can be streams or just remote or proxied track files.

Poweramp looks for non-standard `TrackProviderConsts.COLUMN_URL` column in the provider returned cursor. If some url exists, the track is assumed to be remote file or stream.
Either it's processed as stream (no duration, non seekable) or remote file (duration and seekable), depends on `MediaStore.MediaColumns.DURATION` for given track:
`DURATION` <= 0 defines track as a stream.

Static URL track has URL defined once when provider is scanned by Poweramp for tracks. URL then is used as is to open the track.
You still need to provide `TrackProviderConsts.COLUMN_URL` with special value
Poweramp doesn't do openDocument in this case.

Dynamic URL tracks force Poweramp to query actual URL to use when given track is started in Poweramp. Poweramp does additional call with method `TrackProviderConsts.CALL_GET_URL`.
See [ExampleProvider.java around line 511](app/src/main/java/com/maxmpz/powerampproviderexample/ExampleProvider.java#L551) for the example method implementation.

For URL Tracks with the duration, Poweramp will try to pre-scan it for seek-wave. As this can increase traffic and server load, you can disable this with `TrackProviderConsts.COLUMN_TRACK_WAVE`
set to float array with zero size: `new float[0]`.

Note, at this moment streamed URL tracks (without duration) are shown by Poweramp in appropriate Folders Hierarchy in your provider folders. This is different vs Poweramp scanned
m3u8 playlists, where streams only visible in Streams, Playlists (incl. smart playlists), and Queue categories.
Provider stream tracks also shown in appropriate Album/Artist/Genre/etc. categories


### Metadata And Album Art
Poweramp supports 2 approaches to provider track metadata (tags) and album art:
* metadata and album art image is provided by the Provider
  * this is the only option for URL-based tracks
* metadata and album art is retrieved from track file (file descriptor) by Poweramp

#### Metadata And Album Art Provided By Provider

See `DEFAULT_TRACK_AND_METADATA_PROJECTION` in [ExampleProvider.java around line 74](app/src/main/java/com/maxmpz/powerampproviderexample/ExampleProvider.java#L74).

If `MediaStore.MediaColumns.TITLE` or `MediaStore.MediaColumns.DURATION` columns exist in the resulting cursor, Poweramp assumes metadata is provided by the Provider and track is not scanned
for the tags or the album art.

In this case album art image is retrieved if `Document.COLUMN_FLAGS` column has `Document.FLAG_SUPPORTS_THUMBNAIL` flag.

Poweramp then requests album art via `openDocumentThumbnail`. See [ExampleProvider.java around line 258](app/src/main/java/com/maxmpz/powerampproviderexample/ExampleProvider.java#L258)

Lyrics can be also provided via `TrackProviderConsts.COLUMN_TRACK_LYRICS`.

#### Metadata And Album Art From Track File

If `MediaStore.MediaColumns.TITLE` or/and `MediaStore.MediaColumns.DURATION` are missing from the resulting cursor, Poweramp assumes no metadata is provided and tries to open track file and
scan tags / retrieve embedded album art.


### Track Wave

Poweramp uses up to 100 float track amplitude values (-1.0 .. 1.0 range) to show "seek wave" - rough approximation of the track volume over the track duration.
The wave values can be passed by Provider along other metainformation in case the metainformation is provided by Provider.

Send 100 float values in range -1.0 .. 1.0 as float[] or byte[] array in `TrackProviderConsts.COLUMN_TRACK_WAVE` column.
If array size is 0, Poweramp assumes default wave (same as used for Streams) is required.
If array size is not 100, Poweramp will resample that to 100 values internally.

You can send either float[] array (easier to manipulate/generate) or byte[] array (float array represented as bytes, easier to store/retrieve as BLOB in/from the database).  
See [TrackProviderHelper.java](../poweramp_api_lib/src/com/maxmpz/poweramp/player/TrackProviderHelper.java#L13) for `bytesToFloats`, `floatsToBytes` methods if you need to convert
from one format to the another.


### Roots

For your Provider to be selectable in the Android system picker, the roots should have `Root.FLAG_SUPPORTS_IS_CHILD` flag set. Also, `isChildDocument` method should be overridden and, at least,
it should return true or do a full documentId hierarchy check as needed.

See [ExampleProvider.java around line 126](app/src/main/java/com/maxmpz/powerampproviderexample/ExampleProvider.java#L126).

### Data Refresh

Poweramp handles this automatically based on the `Document.COLUMN_LAST_MODIFIED` column for the folders and files. Data is refreshed on app first start, or when appropriate event or intent received  
(see [Scanner intents in PowerampAPI.java](../poweramp_api_lib/src/com/maxmpz/poweramp/player/PowerampAPI.java#L1454)), etc.

This is configurable in the Poweramp settings: for example, only manual rescan may work depending on settings.

Please note that the Scanner intents sent to Poweramp will be only processed if your app is on the foreground, or Poweramp is a foreground process (either Poweramp UI is visible or Poweramp is playing). If not,
scan service is a subject to the Android 8+ background services policy and intent will be ignored.

Use [EXTRA_PROVIDER from PowerampAPI.java](../poweramp_api_lib/src/com/maxmpz/poweramp/player/PowerampAPI.java#L1615) for fine-grained scan just for your provider. If not specified,
Poweramp will do all known folders and providers scan.

### Deletion
Poweramp will issue standard delete call when user tries to delete one or multiple files. Either delete the track or throw appropriate "not supported" exception to ignore deletion.

### Provider Crashes

Android closes client apps "connected" to your Provider if your Provider process crashes. That means even minor exception in the Provider may cause complete unexpected Poweramp shutdown.  
To avoid that, wrap your Provider public methods with `try/catch(Throwable)` with the appropriate logging.
Note that you still need to throw checked exceptions, such as `FileNotFoundException` from `openDocumentThumbnail`.
