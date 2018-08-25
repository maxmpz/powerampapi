[Poweramp v3 Skin Sample]
============================================

This is a sample skin source, which demonstrates two separate skins in one project.
This can be directly used to build APK which can be installed on the device and recognized by
Poweramp.

**NOTE: this sample skin is intended for demonstration purposes only. While you can build and publish this skin, the skin goal is Poweramp v3 skin features demonstration, which may look weird
and unpolished when all such features combined in one skin**


### Poweramp v3 skin

Poweramp skin is a pretty much standard Android app in APK which includes:
* meta entry in AndroidManifest.xml which points to **res/xml/skins.xml**:
```xml
<meta-data android:name="com.maxmpz.PowerampSkins" android:resource="@xml/skins"/>
```
* **res/xml/skins.xml** file which defines skins in the APK and additional per-skin options

* an Activity which can be started by user. This activity may include actions which
opens Poweramp skin settings or directly start Poweramp with target skin applied
    * the activity is also used for skin development to force Poweramp to reload skin under development
    * the activity can be further customized as needed

* one or multiple skins style definitions (see **values/sample_skin_styles.xml**, **values/sample_skin_aaa_styles.xml**)
* all the required skin drawables, extra layouts, dimens, and other resources
* skin development is done directly from Android Studio (3.1.4 was used for these skins development):
  * you run skin as a normal Android app
  * when skin activity is started, "Start Poweramp With * Skin" button can be pressed to force Poweramp immediately reload the skin

### How Poweramp v3 skins work

Poweramp loads skin APK resources and applies skin theme style directly to its main player activity (there is only one player activity in Poweramp v3, exluding dialogs and Settings).
To ensure future skin compatibility (and to reuse existing default skin styles), skin styles usually *extend* default Poweramp styles.

See **values/sample_skin_styles.xml**, **values/sample_skin_aaa_styles.xml** for commented skin style definitions

### Poweramp skin styles.xml

The main skin styles is defined via res/xml/skins.xml (in this example, SampleSkin):
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


In this sample skin, SampleSkin style is defined in **res/values/sample_skin_styles.xml**. This is standard Android theme style definition, with couple additional requirements:
* the skin style should have one of the Poweramp default skin styles as parent:
```xml
<style name="SampleSkin" parent="com.maxmpz.audioplayer:style/Base_ActivityTheme_Default">
    ...
</style>
```

* all the overridden styles should be derived from appropriate Poweramp styles, with com.maxmpz.audioplayer: prefix:
```xml
<style name="SampleSkin" parent="com.maxmpz.audioplayer:style/Base_ActivityTheme_Default">
    ...
    <item name="com.maxmpz.audioplayer:ItemTrackMenu">@style/ItemTrackMenu</item><!-- We override com.maxmpz.audioplayer:ItemTrackMenu with own @style/ItemTrackMenu -->
    ...
</style>

<!-- Our @style/ItemTrackMenu style, we derive it from com.maxmpz.audioplayer:ItemTrackMenu -->
<style name="ItemTrackMenu" parent="com.maxmpz.audioplayer:ItemTrackMenu">
   ...
</style>

```

* overriden styles **should always be derived** from Poweramp styles, otherwise almost any Poweramp update will break skin, due to possible base styles changes

### Poweramp v3 skin theme specifics
Poweramp v3 has concept of scene. View can be rendered in the target scene (e.g. scene_aa for main player UI large album art item) and can be animated from one scene to another
(e.g. item can be animated from scene_aa to scene_grid when transition happens from main screen to the library playlist).

This is why many attribute/styles are ending by "_scene.." suffix, as for almost each view, addition per-scene styles are required.

Scenes generally define initial and final view layout/position and some view parameters.

See appropriate reference resources xmls for the details in comments.


### Reference resources

For skin authoring, some Poweramp v3 resources (attributes definitions, styles, drawables, etc.) are provided for the reference - see **/reference_resources** directory

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



