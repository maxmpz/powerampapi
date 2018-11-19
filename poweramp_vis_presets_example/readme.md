# Poweramp v3 Visualization Presets APK Example
============================================

This is an example visualization presets project source that demonstrates .milk presets loading into Poweramp via APK.
This project can be directly used to build APK which can be installed on the device and recognized by
Poweramp v3 (**build 796+** is required for the correct APK handling).

### Poweramp v3 visualization presets

Poweramp v3 supports Milkdrop v1 and v2 (with shaders) presets (.milk). In addition, Poweramp also supports spectrum bars rendering defined in .milk presets with the extended syntax.

Milkdrop presets are presets used in Milkdrop visualization plugin for Winamp.
Some documentation for Milkdrop presets can be found here: https://raw.githubusercontent.com/afedchin/visualization.milkdrop2/master/visualization.milkdrop2/resources/Milkdrop2/docs/milkdrop_preset_authoring.html

Poweramp understands .milk presets placed by user directly into the specific folder, or it can load presets from 3rd party APK.

### How Poweramp v3 visualization works?

Poweramp parses .milk file and translates .milk preset internal Eel language to Lua, which then executed in internal LuaJIT VM. Shader code, which is DirectX HLSL shader dialect, is
translated internally to Open GL ES GLSL shaders, preprocessed and loaded for the preset.

**Due to the non-trivial loading process and multiple translations, not all .milk presets can be loaded or properly rendered by Poweramp. Please test all presets in Poweramp v3 before
publishing the presets APK.**

### Poweramp v3 visualization presets APK

Poweramp skin is a pretty much standard Android app in APK which includes:
* meta entry in AndroidManifest.xml:
```xml
<meta-data android:name="com.maxmpz.PowerampVisPresets" android:value="true"/>
```
* .milk files in **[app/src/main/assets/milk_presets](app/src/main/assets/milk_presets)**
  * Poweramp parses preset file name using "name - author.milk" scheme, where name, author, and APK name are separate searchable labels in Poweramp presets list
* optional textures in **[app/src/main/assets/milk_textures](app/src/main/assets/milk_textures)**
  * jpg/png/bmp/tga formats are supported
* an Activity which can be started by user. This activity may include actions to open Poweramp visualization settings or directly start Poweramp with target preset APK loaded
    * the activity is also used for the development to force Poweramp to reload APK under development
    * the activity can be further customized as needed

### How to start own presets APK (based on this example project)
APK development is done directly from Android Studio (3.1.4 was used for this project).
* clone this repository, rename appropriately and change **[values/strings.xml](app/src/main/res/values/strings.xml)** labels
* change application package, preferable to something containing **".poweramp.v3.vispresets."** as this is the substring that will be used in Poweramp to search for preset APKs in Play
* build and run presets APK as a normal Android app
* when presets APK activity is started, "Start Poweramp With This Preset" button can be pressed to force Poweramp immediately reload the presets APK
  * if the last loaded visualization preset was selected from this APK (via presets panel and may be filtering), this preset will be immediately reloaded as well
* APK should appear in Poweramp Visualization settings page as well

### Poweramp .milk format extentensions

Poweramp supports extra rendering object - spectrum bars - which is defined via "bars_*" .milk file entries:
* **bars_mode**
  * 0 - disabled
  * 1 - long bars
  * 2 - rectangles
  * 3 - long bars centered
  * 4 - rectangles centered
  * 5 - top aligned bars
  * 6 - long bars reflected to left/right
  * 7 - rectangles reflected to left/right
* **bars_num_x**
  * number of bars in x axis, max=128 for long bars; 32 for rectangles
* **bars_num_y**
  * number of rectanges in y axis, max=24
* **bars_spacing_x**
  * spacing in 0..1 range between bars horizontally
* **bars_spacing_y**
  * spacing in 0..1 range between rectangles vertically
* **bars_rot**
  * rotation in degrees
* **bars_sx**
  * scale by x axis
* **bars_sy**
  * scale by y axis
* **bars_smooth**
  * bars motion smoothing factor 0..1
* **bars_x**
  * bars x offset -1..0..1
* **bars_y**
  * bars y offset -1..0..1
* **bars_sensitivity**
  * bars sensitivity related to frequency amplitude
* **bars_bass_sensitivity**
  * bars sensitivity related to amplitude in bass frequencies
* **bars_color_t**
  * top bars color in 0xAARRGGBB format
    * .milk syntax is hexadecimal 32 bit number 0xAARRGGBB, e.g. 0xFFFF0000 for red
    * .milk code eel syntax is $xAARRGGBB
* **bars_color_b**
  * bottom bars color in 0xAARRGGBB format
* **bars_color_l**
  * optional left bars color overlay in 0xAARRGGBB format
* **bars_color_r**
  * optional right bars color overlay in 0xAARRGGBB format
* **bars_thr**
   * for non reflected bars 0..1 position where threshold color starts
* **bars_thr_color_t**
   * for non reflected bars: top threshold color in 0xAARRGGBB format
   * for reflected bars: 2nd half top color in 0xAARRGGBB format
* **bars_thr_color_b**
   * for non reflected bars: bottom threshold color in 0xAARRGGBB format
   * for reflected bars: 2nd half bottom color in 0xAARRGGBB format
* **bars_thr_color_l**
   * for non reflected bars: optional left threshold color overlay in 0xAARRGGBB format
   * for reflected bars: 2nd half optional left color overlay in 0xAARRGGBB format
* **bars_thr_color_r**
   * for non reflected bars: optional right threshold color overlay in 0xAARRGGBB format
   * for reflected bars: 2nd half optional right color overlay in 0xAARRGGBB format

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



