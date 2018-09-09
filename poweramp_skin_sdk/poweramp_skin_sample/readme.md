# Poweramp v3 Skin Sample
============================================

This is a sample skin source that demonstrates two separate skins in one project.
This project can be directly used to build APK which can be installed on the device and recognized by
Poweramp v3.

**NOTE: this sample skin is intended for demonstration purposes only. While you can build and publish this skin, the skin goal is Poweramp v3 skin features demonstration, which may look weird
and unpolished when all such features combined in one skin**


### Poweramp v3 skin

Poweramp skin is a pretty much standard Android app in APK which includes:
* meta entry in AndroidManifest.xml which points to **[xml/skins.xml](app/src/main/res/xml/skins.xml)**:
```xml
<meta-data android:name="com.maxmpz.PowerampSkins" android:resource="@xml/skins"/>
```
* **[xml/skins.xml](app/src/main/res/xml/skins.xml)** file which defines skins in the APK and additional per-skin options

* an Activity which can be started by user. This activity may include actions which
opens Poweramp skin settings or directly start Poweramp with target skin applied
    * the activity is also used for skin development to force Poweramp to reload skin under development
    * the activity can be further customized as needed

* one or multiple skins style definitions (see **[values/sample_skin_styles.xml](app/src/main/res/values/sample_skin_styles.xml)**, **[values/sample_skin_aaa_styles.xml](app/src/main/res/values/sample_skin_aaa_styles.xml)**)
* all the required skin drawables, extra layouts, dimens, and other resources

### How to start own skin (based on sample skin)

Skin development is done directly from Android Studio (3.1.4 was used for these skins development).
* clone this repository, rename appropriately and change **[values/strings.xml](app/src/main/res/values/strings.xml)** labels and **[xml/skins.xml](app/src/main/res/xml/skins.xml)** entries
* change application package, preferable to something containing **".poweramp.v3.skins."** as this is the substring that will be used in Poweramp to search for skin APKs in Play
* edit app/build.gradle, replace ../../../audioplayer/bin/audioplayer.apk with path to your Poweramp v3 APK (**build 795** and above)
```
additionalParameters "--shared-lib", "-I", "path to your Poweramp v3 APK"
```

* build and run skin as a normal Android app
* when skin activity is started, "Start Poweramp With * Skin" button can be pressed to force Poweramp immediately reload the skin
* skin should appear in Poweramp skin selection settings page as well


### How Poweramp v3 skins work

Poweramp loads skin APK resources and applies skin theme style directly to its main player activity (there is only one player activity in Poweramp v3).
To ensure future skin compatibility (and to reuse existing default skin styles), skin styles **must extend** default Poweramp styles.

See [values/sample_skin_styles.xml](app/src/main/res/values/sample_skin_styles.xml), [values/sample_skin_aaa_styles.xml](app/src/main/res/values/sample_skin_aaa_styles.xml) for commented skin style definitions.

### Poweramp skin styles.xml

The main skin styles is defined via [xml/skins.xml](app/src/main/res/xml/skins.xml) (in this example, SampleSkin):
```xml
<skins xmlns:android="http://schemas.android.com/apk/res/android">
    <skin
        name="@string/skin_sample"
        author="@string/skin_author"
        description="@string/skin_sample_description"
        style="@style/SampleSkin"
    >
        ...
    </skin>
    ...
</skins>
```


In this sample skin, SampleSkin style is defined in **[values/sample_skin_styles.xml](app/src/main/res/values/sample_skin_styles.xml)**.
This is standard Android theme style definition, with couple additional requirements:
* the skin style should have one of the Poweramp default skin styles as parent:
```xml
<style name="SampleSkin" parent="com.maxmpz.audioplayer:style/Base_ActivityTheme_Default">
    ...
</style>
```

* all the overridden styles should be derived from appropriate Poweramp styles, with **com.maxmpz.audioplayer:** prefix:
```xml
<style name="SampleSkin" parent="com.maxmpz.audioplayer:style/Base_ActivityTheme_Default">
    ...
    <item name="com.maxmpz.audioplayer:ItemTrackMenu">@style/ItemTrackMenu</item><!-- We override com.maxmpz.audioplayer:ItemTrackMenu with own @style/ItemTrackMenu -->
    ...
</style>

<!-- This is our @style/ItemTrackMenu style, we derive it from com.maxmpz.audioplayer:ItemTrackMenu -->
<style name="ItemTrackMenu" parent="com.maxmpz.audioplayer:ItemTrackMenu">
   ...
</style>

```

* overriden styles **should always be derived** from Poweramp styles, otherwise almost any Poweramp update will break skin, due to possible base styles changes

### Poweramp v3 skin theme specifics
Poweramp v3 has concept of a scene. A view can be rendered in the target scene (e.g. scene_aa for main player UI large album art item) and can be animated from one scene to another
(e.g. item can be animated from scene_aa to scene_grid when transition happens from main screen to the library playlist).

This is why many attributes/styles are ending with "_scene.." suffix, as for almost each view addition per-scene styles are required.

Scene generally defines initial and final view layout/position and some view parameters.

Also, almost all Poweramp views are custom views, including layout (FastLayout) and text views (FastTextView). FastLayout is multi-paradigm layout, somewhat similar to ConstraintLayout,
but faster, strictly one-pass per layout, and optimized for animations; and FastText is a fast text rendering view optimized for transitions. See **[reference_resources/values-sw1dp/attrs-powerui.xml](/poweramp_skin_sdk/reference_resources/values-sw1dp/attrs-powerui.xml)** for commented attributes definitions
for these views.

See appropriate reference resources xmls for the details in the comments.

### Difference vs Poweramp v2 skins
* Poweramp v2 skins are not compatible with Poweramp v3, Poweramp v3 skins are not compatible with Poweramp v2
* Poweramp v2 skins relied on skin provided layout xmls, v3 skins rely on style redefinitions, layouts xmls can't be changed by skin (except for few injected specific **merge_** layouts)
* much less raster graphics in default skins, but this is open for skin author, there is no limitation on raster images


### Reference resources

For skin authoring, some Poweramp v3 resources (attribute definitions, styles, drawables, etc.) are provided for the reference - see **[/reference_resources](/poweramp_skin_sdk/reference_resources)** directory.

The most important files are:
* res/layout-sw1dp/activity_list_fast.xml - the main Poweramp activity layout
* res/layout-sw1dp/merge_*.xml - various additional merged layouts
* res/layout-sw1dp/item_track.xml - the "track" item which is used in main player UI and lists for all the items with image
* res/layout-sw1dp/item_text.xml - same as item_track.xml, but for text-only items
* res/values-sq1dp/attrs.xml, attrs-powerui.xml, attrs-player.xml - attributes definitions for all the Poweramp custom views, scenes, etc.
* res/values/default-styles.xml - default skin style
* res/values-sw1db/styles-*.xml - various default skin styles, grouped by style name prefix (all of them combined into default style inside Poweramp)

### License

Copyright (C) 2010-2018 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for themes, skins, widgets, plugins, applications and other software
which communicate with Poweramp music player application on Android platform.

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



