[TOC]: # "Poweramp v3 Skin Sample"

# Poweramp v3 Skin Sample
- [Recent Changes](#recent-changes)
- [Introduction](#introduction)
- [Poweramp v3 Skin](#poweramp-v3-skin)
- [How to Start Own Skin (Based on Sample Skin)](#how-to-start-own-skin-based-on-sample-skin)
- [How Poweramp v3 Skins Work](#how-poweramp-v3-skins-work)
- [Android 11/targetSdkVersion 30+ support](#android-11targetsdkversion-30-support)
- [Bundle support](#bundle-support)
- [Poweramp Skin styles.xml](#poweramp-skin-stylesxml)
- [Automatic Dark/Light Modes](#automatic-darklight-modes)
- [Poweramp v3 Skin Theme Specifics](#poweramp-v3-skin-theme-specifics)
- [Poweramp v3 Skin Options](#poweramp-v3-skin-options)
- [Skin Options Persistence](#skin-options-persistence)
- [Custom/Loadable Fonts](#customloadable-fonts)
- [Errors](#errors)
- [Difference vs Poweramp v2 Skins](#difference-vs-poweramp-v2-skins)
- [Poweramp Equalizer Support](#poweramp-equalizer-support)
- [Reference Resources](#reference-resources)
- [License](#license)

### Recent Changes
##### Build 991
###### iconTint for page and msg 
- [page](#page) now supports iconTint attribute
- [page](#page) tag supporting icon + iconTint attributes, now avoids any tinting by default if no iconTint is specified (original icon colors are shown) - as this seems to be the expected behavior in currently published 3rd party skins. The default tint (color which is similar to current settings theme text color) now applied with the iconTint="default" attribute value
- in contrast, [MultiSwitch](#multiswitch), [Radio](#radio), [msg](#msg) and other option tags apply default tint (==secondary text color) by the default

##### Build 978
###### icon and iconTint for multiSwitch, msg options
- [MultiSwitch](#multiswitch), [msg](#msg) tags now supports icon and iconTint attributes

##### Build 949
###### targetBuild
- The top ```<skins>``` xml tag now supports targetBuild attribute, which defines the Poweramp build this skin was build for.
If not set, skin is assumed to be built for Poweramp builds prior 948 and Poweramp will apply extra layer of compatibility
styles for such skins.

##### Builds 912-914
###### Plugin XML minSdk/maxSdk attributes
New minSdk="*sdk_num*" and maxSdk="*sdk_num*" attributes are supported for option tags. *sdk_num* is also known as the API Level
(see Tools/SDK Manager in Android Studio for the reference).
Both minSdk and maxSdk attributes are optional
  * minSdk defines the minimum SDK level for the option. The option is hidden for the SDK levels prior minSdk
  * maxSDK defines the maximum SDK level for the option. The option is hidden for the SDK levels above maxSDK

###### New corner radiuses
By default, Android 12-like corner radiuses are now applied with the appropriate changes to the paddings/margins.
New corner radius attributes added for the specific use cases, see *attrs-corners.xml*

###### FastTextViews
Track related FastTextViews (title, line2, meta) now use new *layout_alignToContent* and *eatFontPads* attributes
- *layout_alignToContent* forces alignment to the content ignoring the padding
- *eatFontPads* removes implicit vertical font paddings

  This simplifies pixel perfect margin and alignment adjustments for text inside TrackItem for
  varied fonts, font sizes, scenes, zoom levels, etc.

  Previously, pixel perfect alignment required manual margin/padding adjustments per scene, and
  that could quickly get out of control due to the large number of possible scenes multiplied by
  different fonts/font sizes (in turn, multiplied by number of the text views, multiplied by other conditions, etc.).

  Instead, by ignoring the inner font padding differences (*eatFontPads=true*) and paddings in general
  (*layout_alignToContent=true*) such adjustments could be (and actually, should be) done only once.
  In skins, preferable, no any adjustments should be done and derived styles should be used as they are.

  To use the new paddings, add *allowNewPadding="true"* to your *&lt;skin>* element.

  By default (when no *allowNewPadding* is specified) Poweramp will try to guess if new paddings can
  be applied by checking if skin has theme attribute *ItemTrackTitle* defined.
  Most skins do not have this redefined and default Poweramp item styles work just fine.
  If skin redefines the *ItemTrackTitle*, Poweramp applies
  *default-styles.xml/ActivityTheme_Legacy_oldPadding* style which disables the new paddings.

  If you happen to redefine *ItemTrackTitle* or any other *ItemTrack** **text** styles, ensure your margins and
  alignments are OK with Poweramp build-912+.

  You may get wrong alignment/margins if you override some margin attributes (e.g. marginTop), but do not override others
  (e.g. marginBottom/Left/Right) causing derived margins and other styles being partially applied.

  Either override all margins and other attributes which may affect view alignment (scale/padding/gravity), or
  remove such overrides and just use the derived default style.

  
### Introduction
This is a sample skin source that demonstrates two separate skins in one project.
This project can be directly used to build APK which can be installed on the device and recognized by
Poweramp v3.

**NOTE: this sample skin is intended for demonstration purposes only. While you can build and publish this skin, the skin goal is Poweramp v3 skin features demonstration, which may look weird
and unpolished when all such features combined in one skin**


### Poweramp v3 Skin
Poweramp skin is a pretty much standard Android app in APK which includes:
* meta entry in AndroidManifest.xml which points to **[xml/skins.xml](src/main/res/xml/skins.xml)**:

```xml
<meta-data android:name="com.maxmpz.PowerampSkins" android:resource="@xml/skins"/>
```
* **[xml/skins.xml](src/main/res/xml/skins.xml)** file which defines skins in the APK and additional per-skin options

* an Activity which can be started by user. This activity may include actions which
opens Poweramp skin settings or directly start Poweramp with target skin applied
    * the activity is also used for skin development to force Poweramp to reload skin under development
    * the activity can be further customized as needed
* one or multiple skins style definitions (see **[values/sample_skin_styles.xml](src/main/res/values/sample_skin_styles.xml)**, **[values/sample_skin_aaa_styles.xml](src/main/res/values/sample_skin_aaa_styles.xml)**)
* all the required skin drawables, extra layouts, dimens, and other resources


### How to Start Own Skin (Based on Sample Skin)
Skin development is done directly from Android Studio (2020.3.1+).
* clone this repository, rename appropriately and change **[values/strings.xml](src/main/res/values/strings.xml)** labels and **[xml/skins.xml](src/main/res/xml/skins.xml)** entries
* change application package, preferable to something containing **".poweramp.v3.skins."** as this is the substring that will be used in Poweramp to search for skin APKs in Play
* (optionally) edit local.properties, add path to your Poweramp v3 APK (by default, it points to the bundled resources-only Poweramp apk):

```
poweramp.apk=relative path to your poweramp.apk
```
* your emulator or device needs to have Poweramp v3 installed
* build and run skin as a normal Android app
* when skin activity is started, "Start Poweramp With * Skin" button can be pressed to force Poweramp immediately reload the skin
* skin should appear in Poweramp skin selection settings page as well
* **Android Studio "Apply Changes" feature most probably won't properly reload resources, so don't use it for skin development**
* optionally enable **Always Reload Skin** in Poweramp Settings / Misc. This forces Poweramp to recheck skin on each activity start  


### How Poweramp v3 Skins Work
Poweramp loads skin APK resources and applies skin theme style directly to its main player activity (there is only one player activity in Poweramp v3).
To ensure future skin compatibility (and to reuse existing default skin styles), skin styles **must extend** default Poweramp styles.

See [values/sample_skin_styles.xml](src/main/res/values/sample_skin_styles.xml), [values/sample_skin_aaa_styles.xml](src/main/res/values/sample_skin_aaa_styles.xml) for commented skin style definitions.

### Android 11/targetSdkVersion 30+ support
All sample projects and PowerampAPI is built with targetSdkVersion 30+. Poweramp is built with targetSdkVersion 31
starting from the build 912.

Due to the package visibility changes on Android 11 for the apps compiled with targetSdkVersion 30+, ensure your
skin AndroidManifest.xml contains:
* queries tag (so skin app is able to find Poweramp to set the skin on button press):

```xml
<queries>
    <intent>
        <action android:name="com.maxmpz.audioplayer.API_COMMAND"/>
    </intent>
</queries>
```
* com.maxmpz.audioplayer.SKIN_MAIN action for the main activity (so Poweramp is able to find the skin):

```xml
<intent-filter>
    <action android:name="com.maxmpz.audioplayer.SKIN_MAIN" />
</intent-filter>
```

### Bundle support
Bundles should work fine out of box for the skin projects without extra configuration.

### Poweramp Skin styles.xml
The main skin styles is defined via [xml/skins.xml](src/main/res/xml/skins.xml) (in this example, SampleSkin):

```xml
<skins xmlns:android="http://schemas.android.com/apk/res/android">
    <skin
        name="[visible name of the skin, e.g. @string/skin_sample]"
        author="[visible author name, e.g. @string/skin_author]"
        description="[visible skin description, e.g. @string/skin_sample_description]"
        style="[the reference to the skin theme style, e.g. @style/SampleSkin]"
        lightMode="[optional. The reference to the light version of this skin, e.g. @style/SampleSkinLight. Valid only if darkMode is not specified]"
        darkMode="[optional. The reference to the dark version of this skin, e.g. @style/SampleSkinDark. Valid only if lightMode is not specified]"
    >
        ...
    </skin>
    ...
</skins>
```

In this sample skin, SampleSkin style is defined in **[values/sample_skin_styles.xml](src/main/res/values/sample_skin_styles.xml)**.
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
```

```xml
<!-- This is our @style/ItemTrackMenu style, we derive it from com.maxmpz.audioplayer:ItemTrackMenu -->
<style name="ItemTrackMenu" parent="com.maxmpz.audioplayer:ItemTrackMenu">
   ...
</style>
```


* overridden styles **should always be derived** from Poweramp styles, otherwise almost any Poweramp update will break skin, due to possible base styles changes

* include tag allows inclusion of sub-sections from other res/xml files

```xml
<include file="@xml/skin_sample_subpage2"/>
```
Included file should be a valid well formed xml. Include tag is supported at any level inside &lt;skins&gt;.


### Automatic Dark/Light Modes
Poweramp will switch skin to the dark/light version if appropriate option is enabled in Poweramp settings and current skin supports dark/light mode.
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
but faster, strictly one-pass per layout, and optimized for animations; and FastText is a fast text rendering view optimized for transitions. See **[reference_resources/values/attrs-powerui.xml](/poweramp_skin_sdk/reference_resources/values/attrs-powerui.xml)** for the commented attributes definitions
for these views.


### Poweramp v3 Skin Options
Poweramp v3 supports unique feature allowing user selectable skin options to be defined by skin author.

#### overlapStyle and overlapAttr
Option is an *overlapStyle* - the extra style applied to the theme on top level which is applied in addition to the main skin theme.

If *overlapStyle* is not defined, but *overlapAttr* is defined, *overlapAttr* is resolved to style and that style is applied.

* *overlapStyle* applies that style values directly to the top level theme
* *overlapAttr* is an attribute which points to the style which is applied directly to the top level theme. This indirection
allows earlier option to modify options below by simply overriding that later option *overlapAttr* attribute to an alternative style

See sample skin **[xml/skins.xml](src/main/res/xml/skins.xml)** for reference.

#### Options
* simple option with some name and optional summary (rendered as switch):

```xml
<option
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary]"
    overlapStyle="[overlap style reference. Applied if overlapAttr is not defined]"
    overlapAttr="[optional overlap attr reference to style]"
    checkedByDefault="[true|false]"
    dependency="[key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
    minSdk="[optional integer. If device Android SDK level is below that, option is not shown]"
    maxSdk="[optional integer. If device Android SDK level is above that, option is not shown]"
/>
```
* set of a radio buttons. Only one selected value from the set of options is applied:

```xml
<radio
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary]"
    defaultValue="[default overlapStyle/overlapAttr value to use]"
    dependency="[key of some other option. If the referenced option is switched off (or value is 0), this option is disabled]"
    minSdk="[optional integer. If device Android SDK level is below that, option is not shown]"
    maxSdk="[optional integer. If device Android SDK level is above that, option is not shown]"
>
        <option
            name="[visible name]"
            summary="[optional summary]"
            overlapStyle="[overlap style reference. Applied if overlapAttr is not defined]"
            overlapAttr="[optional overlap attr reference to style]"
            icon="[optional @drawable/* icon for the radio button]"
            iconTint="[optional #color, @color/* to apply given tint color, 'default ' for the default text-color based color, or 'none' to avoid tint. Default is 'default']"
        />
        ...
</radio>
```
* option with a popup option chooser list. Only one selected value from the set of options is applied. &lt;popup&gt; option are automatically converted to &lt;multiSwitch&gt; unless allowMulti is set to false for the &lt;popup&gt;.
The automatic conversion happens if number of &lt;popup&gt; options is less than 5, options don't have html and are shorter than 24 characters.

```xml
<popup
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary, can include %s pattern which is replaced by currently selected option name]"
    defaultValue="[default overlapStyle/overlapAttr value to use]"
    dependency="[key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
    allowMulti="[boolean - true|false - optional, default is true. If set to false, disables conversion to the multiSwitch]"
    minSdk="[optional integer. If device Android SDK level is below that, option is not shown]"
    maxSdk="[optional integer. If device Android SDK level is above that, option is not shown]"
>
        <option
            name="[visible name]"
            overlapStyle="[overlap style reference. Applied if overlapAttr is not defined]"
            overlapAttr="[optional overlap attr reference to style]"
        />
        ...
</popup>
```

* option with a seekbar option chooser list. Only one selected value from the set of options is applied:

```xml
<seekbarOptions
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary, can include %s pattern which is replaced by currently selected option name]"
    summary2="[string - same as summary. Allows independent string resource to be used. Works only if just summary exists]"
    defaultValue="[default overlapStyle/overlapAttr value to use]"
    leftLabel="[string - the label to the left]"
    centerLabel="[string - the label at the center]"
    rightLabel="[string - the label to the right]"
    dependency="[key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
    minSdk="[optional integer. If device Android SDK level is below that, option is not shown]"
    maxSdk="[optional integer. If device Android SDK level is above that, option is not shown]"
>
        <option
            name="[visible name]"
            overlapStyle="[overlap style reference. Applied if overlapAttr is not defined]"
            overlapAttr="[optional overlap attr reference to style]"
        />
        ...
</popup>
```

* <a name="#multiswitch">MultiSwitch</a> - an option with the multiple switchable inline options. Only one selected value from the set of options is applied.
Converted option won't have summary, if the summary contains formatting symbols (%s).

```xml
<multiSwitch
    key="[preference unique key]"
    name="[visible name]"
    summary="[optional summary, can include %s pattern which is replaced by currently selected option name]"
    defaultValue="[default overlapStyle/overlapAttr value to use]"
    dependency="[key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
    minSdk="[optional integer. If device Android SDK level is below that, option is not shown]"
    maxSdk="[optional integer. If device Android SDK level is above that, option is not shown]"
>
        <option
            name="[visible name]"
            summary="[optional. summary shown when option selected]"
            overlapStyle="[overlap style reference. Applied if overlapAttr is not defined]"
            overlapAttr="[optional overlap attr reference to style]"
            icon="[optional @drawable/* icon for the option button]"
            iconTint="[optional #color, @color/* to apply given tint color, 'default ' for the default text-color based color, or 'none' to avoid tint. Default is 'default']"
        />
        ...
</multiSwitch>
```

Radio/popup/seekbarOptions (but not multiSwitch) tags support limited set of html tags inside **name** and **summary** attributes. These allow e.g. specifying color:

```xml
<option
    name="@string/skin_text_color1"
/>

```
where skin_text_color1 is defined in strings.xml with html tags inside:

```xml
<string name="skin_text_color1"><![CDATA[Text color <span style=\"color: #ff0000;\">⬤</span>]]></string>
```
* seekbar option. The seekbar preference always manipulates integer values (e.g. 1, 100, 1000), but still can store float value (e.g. 0.5, 1.5) if needed.  
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
    dependency="[key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
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


* category. The category is a subset of options displayed with a header.
The category tag is ignored by builds prior 866, all inner options are still shown.

```xml
    <category
        name="[category header text]"
    >
        [options. Other category tags can't be placed here]
        ...
    </category>
```

* <a name="page">page</a>. Page is a subset of options on their own separate page.
The page tag is ignored by builds prior 866, all inner options are still shown.

```xml
    <page
        name="[category name]"
        summary="[optional summary]"
        dependency="[key of some other option. If referenced option is switched off (or value is 0) this option is disabled]"
        icon="[optional icon drawable resource]"
        iconTint="[since 991. Optional #color, @color/* to apply given tint color, 'default ' for the default text-color based color, or 'none' to avoid tint. Default is 'none']"
    >
        [options. You can put more pages inside parent pages, creating hierarchy of option pages]
        ...
    </page>
```

* <a name="msg">msg</a> - non-clickable message/hint to show. The lineX/smallLineX attributes are all optional

```xml
    <msg
        icon="[optional icon drawable resource]"
        iconTint="[optional #color, @color/* to apply given tint color, 'default ' for the default text-color based color, or 'none' to avoid tint. Default is 'none']"
        iconAlign="[top - option. If specified, the icon will be top aligned]"
        align="[top|bottom - option. If specified, the message will be appropriately aligned]"
        line1="[normal size text line or @string reference]"
        smallLine1="[smaller size text line or @string reference]"
        line2="[normal size text line or @string reference]"
        smallLine2="[smaller size text line or @string reference]"
        line3="[normal size text line or @string reference]"
        smallLine3="[smaller size text line or @string reference]"
        line4="[normal size text line or @string reference]"
        smallLine5="[smaller size text line or @string reference]"
    />
```

* globalOption - additional style to apply for appropriate global option. Multiple unique globalOptions are possible, per
appropriate Poweramp global option. The style defined by the overlapAttr is applied after skin options, so skin may override
some styling depending on the global option, such as OptionRatingDisabled

```xml
    <globalOption
        option="[Poweramp global option attr - see styles-options.xml - e.g. @attr/OptionRatingDisabled]"
        overlapAttr="[skin style attribute to apply when option is in effect - e.g. @attr/overlap_OptionRatingDisabled]"
        />

```

### Skin Options Persistence
Poweramp persists skin option to own preferences based on skin generated IDs for the styles/attributes.
Re-generating such IDs (e.g. in case of Clean build) will reset some or all options on user side as IDs will be changed.

To avoid this issue **[stable-ids.txt](stable-ids.txt)** file is used, which is automatically updated during skin
build process with the new styles/attributes you add to your skin.
Be sure to keep this file around. 
The file initially contains the sample skin IDs (which won't generally interfere with your IDs).

Note that only few attributes should have stable ids - the skin style as used in your *skins*.xml
<skin> tag style attribute.
For example <skin style="@style/SampleSkin"..> means line like:

```
com.poweramp.v3.sampleskin:style/SampleSkin = 0x80070054
```
should exist in stable-ids.txt. Other lines/stable ids are not needed.



### Custom/Loadable Fonts
Poweramp supports custom fonts from **res/fonts**. See https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml for details on Android custom fonts usage.
If you use font family .xml file, don't forget to add both android: and app: prefixed properties. The app: values are used on Androids prior 8.

Please note that font files (.ttf/.otf) should be named with lowercase latin, 0-9, and "_" characters only. For example OpenSans-Bold.ttf should be renamed to opensans_bold.ttf.
This is Android resources builder requirement.

Poweramp control/view styles have separate layer of TextAppearance styling (same as core Android widgets do), which can be modified independently of other styles.
See sample skin font option implementations:
**[values/sample_skin_open_sans_font.xml](src/main/res/values/sample_skin_open_sans_font.xml)**,  
**[values/sample_skin_ubuntu_font.xml](src/main/res/values/sample_skin_ubuntu_font.xml)**


### Errors
Poweramp logs as much error info as possible during initial skin loading. Also, errors/fails in skin xml or resources may crash/restart Poweramp resulting in some default skin restored.
Check Logcat for Poweramp errors.


### Difference vs Poweramp v2 Skins
* Poweramp v2 skins are not compatible with Poweramp v3, Poweramp v3 skins are not compatible with Poweramp v2
* Poweramp v2 skins relied on skin provided layout xmls, v3 skins rely on style redefinitions, layouts xmls can't be changed by skin (except for a few injected specific **merge_** layouts)
* less bitmap graphics in default skins, but this is open for skin author, there is no any limitation on bitmap images


### Poweramp Equalizer Support
Poweramp Equalizer is able to load Poweramp skins. Additional styles/attributes/layouts are defined specifically
for Equalizer, those are available in reference resources as well, usually with *peq* suffix/prefix.
The Equalizer skin should be still built vs Poweramp APK as Equalizer APK has no *public* definitions.


### Reference Resources
For skin authoring core Poweramp v3 resources (attribute definitions, styles, drawables, etc.) are provided for the reference -  
see **[/reference_resources](/poweramp_skin_sdk/reference_resources)** directory.

The most important files are:
* res/layout-sw2dp/activity_main.xml - the main Poweramp activity layout
* res/layout-sw2dp/merge_*.xml - various additional merged layouts
* res/layout-sw2dp/item_track.xml - the "track" item which is used in main player UI and lists for all the items with image
* res/layout-sw2dp/item_text.xml - same as item_track.xml, but for text-only items
* res/values-sq2dp/attrs.xml, attrs-powerui.xml, attrs-player.xml - attributes definitions for all the Poweramp custom views, scenes, etc.
* res/values/default-styles.xml - default skin style
* res/values/styles-*.xml - various default skin styles, grouped by style name prefix (all of them combined into default style inside Poweramp)


### License
Copyright (C) 2010-2022 Maksim Petrov

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



**