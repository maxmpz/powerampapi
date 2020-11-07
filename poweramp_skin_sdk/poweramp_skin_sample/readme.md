[TOC levels=3]:# "#Poweramp v3 Skin Sample"

# Poweramp v3 Skin Sample
- [Introduction](#introduction)
- [Poweramp v3 Skin](#poweramp-v3-skin)
- [How to Start Own Skin (Based on Sample Skin)](#how-to-start-own-skin-based-on-sample-skin)
- [How Poweramp v3 Skins Work](#how-poweramp-v3-skins-work)
- [Poweramp Skin styles.xml](#poweramp-skin-stylesxml)
- [Automatic Dark/Light Modes](#automatic-darklight-modes)
- [Poweramp v3 Skin Theme Specifics](#poweramp-v3-skin-theme-specifics)
- [Poweramp v3 Skin Options](#poweramp-v3-skin-options)
- [Skin Options Persistence](#skin-options-persistence)
- [Custom/Loadable Fonts](#customloadable-fonts)
- [Errors](#errors)
- [Difference vs Poweramp v2 Skins](#difference-vs-poweramp-v2-skins)
- [Reference Resources](#reference-resources)
- [License](#license)

### Introduction
This is a sample skin source that demonstrates two separate skins in one project.
This project can be directly used to build APK which can be installed on the device and recognized by
Poweramp v3.

**NOTE: this sample skin is intended for demonstration purposes only. While you can build and publish this skin, the skin goal is Poweramp v3 skin features demonstration, which may look weird
and unpolished when all such features combined in one skin**


### Poweramp v3 Skin
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


### How to Start Own Skin (Based on Sample Skin)
Skin development is done directly from Android Studio (3.2 was used for these skins development).
* clone this repository, rename appropriately and change **[values/strings.xml](app/src/main/res/values/strings.xml)** labels and **[xml/skins.xml](app/src/main/res/xml/skins.xml)** entries
* change application package, preferable to something containing **".poweramp.v3.skins."** as this is the substring that will be used in Poweramp to search for skin APKs in Play
* edit app/build.gradle, replace ../../../audioplayer/bin/audioplayer.apk with path to your Poweramp v3 APK (**build 795** and above)
```
additionalParameters "--stable-ids", "stable-ids.txt", "--emit-ids", "stable-ids.txt", "--package-id", "0x80",  "-I", "path to your Poweramp v3 APK"
```
* your emulated device needs to have Poweramp v3 installed (e.g. locate your Android\Sdk\platform-tools folder and copy the poweramp.apk into it.
Open a shell and enter `./adb install poweramp.apk`)
* build and run skin as a normal Android app
* when skin activity is started, "Start Poweramp With * Skin" button can be pressed to force Poweramp immediately reload the skin
* skin should appear in Poweramp skin selection settings page as well
* **Android Studio "Apply Changes" feature most probably won't properly reload resources, so don't use it for skin development**
* optionally enable **Always Reload Skin** in Poweramp Settings / Misc. This forces Poweramp to recheck skin on each activity start  


### How Poweramp v3 Skins Work
Poweramp loads skin APK resources and applies skin theme style directly to its main player activity (there is only one player activity in Poweramp v3).
To ensure future skin compatibility (and to reuse existing default skin styles), skin styles **must extend** default Poweramp styles.

See [values/sample_skin_styles.xml](app/src/main/res/values/sample_skin_styles.xml), [values/sample_skin_aaa_styles.xml](app/src/main/res/values/sample_skin_aaa_styles.xml) for commented skin style definitions.


### Poweramp Skin styles.xml
The main skin styles is defined via [xml/skins.xml](app/src/main/res/xml/skins.xml) (in this example, SampleSkin):
```xml
<skins xmlns:android="http://schemas.android.com/apk/res/android">
    <skin
        name="[visible name of the skin, e.g. @string/skin_sample]"
        author="[visible author name, e.g. @string/skin_author]"
        description="[visible skin description, e.g. @string/skin_sample_description]"
        style="[the reference to the skin theme style, e.g. @style/SampleSkin]"
        lightMode="[since 869. Optional. The reference to the light version of this skin, e.g. @style/SampleSkinLight. Valid only if darkMode is not specified]"
        darkMode="[since 869. Optional. The reference to the dark version of this skin, e.g. @style/SampleSkinDark. Valid only if lightMode is not specified]"
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

* overridden styles **should always be derived** from Poweramp styles, otherwise almost any Poweramp update will break skin, due to possible base styles changes

* include tag (since build 866) allows inclusion of sub-sections from other res/xml files
```xml
...
<include file="@xml/skin_sample_subpage2"/>
...
```
Included file should be a valid well formed xml. Include tag is supported at any level inside <skins>.


### Automatic Dark/Light Modes
Supported since build 869. Poweramp will switch skin to the dark/light version if appropriate option is enabled in Poweramp settings and current skin supports dark/light mode.
The support is indicated by the lightMode and darkMode attributes.
If Poweramp needs to switch to the dark mode and current skin has darkMode attribute, skin defined by darkMode will be selected.
If Poweramp needs to switch to the light mode and current skin has lightMode attribute, skin defined by lightMode will be selected.

Note that both attributes on same skin will result in an undefined behavior. Also, switching light/dark mode is two way only, meaning once the skin switched to the dark one,
switching back to light mode will switch to the initial skin, not the skin possibly defined as lightMode attribute for the dark skin.


### Poweramp v3 Skin Theme Specifics
Poweramp v3 has concept of a scene. A view can be rendered in the target scene (e.g. scene_aa for main player UI large album art item) and can be animated from one scene to another
(e.g. item can be animated from scene_aa to scene_grid when transition happens from main screen to the library playlist).

This is why many attributes/styles are ending with "_scene.." suffix, as for almost each view addition per-scene styles are required.

Also, almost all Poweramp views are custom views, including layout (FastLayout) and text views (FastTextView). FastLayout is multi-paradigm layout, somewhat similar to ConstraintLayout,
but faster, strictly one-pass per layout, and optimized for animations; and FastText is a fast text rendering view optimized for transitions. See **[reference_resources/values-sw1dp/attrs-powerui.xml](/poweramp_skin_sdk/reference_resources/values-sw1dp/attrs-powerui.xml)** for the commented attributes definitions
for these views.


### Poweramp v3 Skin Options
Poweramp v3 supports unique feature allowing user selectable skin options to be defined by skin author. Option is an "overlap" style which is applied in addition to the main skin theme.
See sample skin **[xml/skins.xml](app/src/main/res/xml/skins.xml)** for reference.

Options include:
* simple option with some name and optional summary (rendered as switch):
```xml
<option
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary]"
    overlapStyle="[overlap style reference]"
    checkedByDefault="[true|false]"
    dependency="[since 866. Key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
/>
```
* set of a radio buttons (since build 810). Only one selected value from the set of options is applied:
```xml
<radio
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary]"
    defaultValue="[optional default overlap style reference]"
    dependency="[since 866. Key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
>
        <option
            name="[visible name]"
            summary="[optional summary]"
            overlapStyle="[overlap style reference, can be an empty string]"
        />
        ...
</radio>
```
* option with a popup option chooser list (since build 810). Only one selected value from the set of options is applied:
```xml
<popup
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary, can include %s pattern which is replaced by currently selected option name]"
    defaultValue="[optional default overlap style reference]"
    dependency="[since 866. Key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
>
        <option
            name="[visible name]"
            overlapStyle="[overlap style reference, can be an empty string]"
        />
        ...
</popup>
```

* option with a seekbar option chooser list (since build 866). Only one selected value from the set of options is applied:
```xml
<seekbarOptions
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary, can include %s pattern which is replaced by currently selected option name]"
    summary2="[string - same as summary. Allows independent string resource to be used. Works only if just summary exists]"
    defaultValue="[optional default overlap style reference]"
    leftLabel="[string - the label to the left]"
    centerLabel="[string - the label at the center]"
    rightLabel="[string - the label to the right]"
    dependency="[since 866. Key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
>
        <option
            name="[visible name]"
            overlapStyle="[overlap style reference, can be an empty string]"
        />
        ...
</popup>
```

Radio/popup/seekbarOptions options support limited set of html tags inside **name** and **summary** attributes. These allow e.g. specifying color:
```xml
<option
    ...
    name="@string/skin_text_color1"
    ...
/>

```
where skin_text_color1 is defined in strings.xml with html tags inside:
```xml
<string name="skin_text_color1"><![CDATA[Text color <span style=\"color: #ff0000;\">⬤</span>]]></string>
```
* seekbar option (since build 866). The seekbar preference always manipulates integer values (e.g. 1, 100, 1000), but still can store float value (e.g. 0.5, 1.5) if needed.  
The seekbar preference is ignored completely by builds prior 866.
```xml
<seekbar
    key="[preference unique key. Used to store/retrieve key name, for dependencies, etc.]"
    id="[unique id for the option. Referenced from skin styles]"
    name="[visible name]"
    floatScale="[if set, then float value = value / floatScale is persisted]"
    defaultValue="[integer default value. If floatScale is set, actual default value = defaultValue / floatScale]"
    max="[integer max possible value]"
    min="[integer min possible value]"
    step="[integer step between values]"
    scale="[float. If set, this scale (= value / scale) is applied to displayed value (e.g. in summary). The actual stored value is not changed]"
    snapTo="[integer value where thumb may stick to (if in a close proximity). NOTE: doesn't work well if step is large and ticks are used]"
    leftLabel="[string - the label to the left]"
    centerLabel="[string - the label at the center]"
    rightLabel="[string - the label to the right]"
    ticks="[boolean - if true tick marks are used (positioned according to the step attribute)]"
    summary="[string - summary may include %d (or if floatScale is used: %.1f or similar format values. Here %.1f means 1 digit after point, %.2f - 2 digits, etc.)]"
    summary2="[string - same as summary. Allows independent string resource to be used. Works only if just summary exists]"
    dependency="[since 866. Key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
/>
```
At this moment the seekbar preference can be used only for the subset of properties:
* text size multiplier  
  This is silently ignored prior build 866

  Set to appropriate views via textSizeMultiplierPref attribute.
  ```xml
  <item name="com.maxmpz.audioplayer:textSizeMultiplierPref">@+id/yourSeekbarId</item>
  ```
  The _float_ value taken from this seekbar is applied as an additional font size scale value.
  _float_ value means we need to set floatScale for the seekbar preference.
* AAImage corner radius  
  This will silently fail to work prior build 866 - corners will be == 0px and requires substyle + some message to user not to enable on older versions of Poweramp.

  Set via directly assigning seekbar *id* to the appropriate corner value, e.g.:
    ```xml
    <item name="com.maxmpz.audioplayer:corners_aa_scene_aa">@+id/yourSeekbarId</item>
    ```
  The _float_ value taken from this seekbar is applied as an additional font size scale value.  
  _float_ value means we need to set floatScale for the seekbar preference.

* listSubstyle aaMaxYRotation
* listSubstyle aaMaxZRotation
* listSubstyle aaMaxScale
* listSubstyle aaDenseFactor


* category (since build 866). The category is a subset of options displayed with a header.
The category tag is ignored by builds prior 866, all inner options are still shown.
```xml
    <category
        name="[category header text]"
    >
        [options. Other category tags can't be placed here]
        ...
    </category>
```

* page (since build 866). Page is a subset of options on their own separate page.
The page tag is ignored by builds prior 866, all inner options are still shown.
```xml
    <page
        name="[category name]"
        summary="[optional summary]"
        dependency="[since 866. Key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
        icon="[optional icon drawable resource]"
    >
        [options. You can put more pages inside parent pages, creating hierarchy of option pages]
        ...
    </page>
```


### Skin Options Persistence
Poweramp persists skin option to own preferences based on skin generated IDs for the styles/attributes. Re-generating such IDs (e.g. in case of Clean build) will reset some
or all options on user side as IDs will be changed.

To avoid this issue **[stable-ids.txt](stable-ids.txt)** file is used, which is automatically updated during skin build process with the new styles/attributes you add to your skin.
Be sure to keep this file around. 
The file initially contains the sample skin IDs (which won't generally interfere with your IDs).


### Custom/Loadable Fonts
Poweramp (since build 842) supports custom fonts from **res/fonts**. See https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml for details on Android custom fonts usage.
If you use font family .xml file, don't forget to add both android: and app: prefixed properties. The app: values are used on Androids prior 8.

Please note that font files (.ttf/.otf) should be named with lowercase latin, 0-9, and "_" characters only. For example OpenSans-Bold.ttf should be renamed to opensans_bold.ttf.
This is Android resources builder requirement.

Poweramp control/view styles have separate layer of TextAppearance styling (same as core Android widgets do), which can be modified independently of other styles.
See sample skin font option implementations: **[values/sample_skin_open_sans_font.xml](app/src/main/res/values/sample_skin_open_sans_font.xml)**, **[values/sample_skin_ubuntu_font.xml](app/src/main/res/values/sample_skin_ubuntu_font.xml)** and Poppin


### Errors
Poweramp logs as much error info as possible during initial skin loading. Also, errors/fails in skin xml or resources may crash/restart Poweramp resulting in some default skin restored.
Check Logcat for Poweramp errors.


### Difference vs Poweramp v2 Skins
* Poweramp v2 skins are not compatible with Poweramp v3, Poweramp v3 skins are not compatible with Poweramp v2
* Poweramp v2 skins relied on skin provided layout xmls, v3 skins rely on style redefinitions, layouts xmls can't be changed by skin (except for a few injected specific **merge_** layouts)
* less bitmap graphics in default skins, but this is open for skin author, there is no any limitation on bitmap images


### Reference Resources
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
Copyright (C) 2010-2020 Maksim Petrov

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



