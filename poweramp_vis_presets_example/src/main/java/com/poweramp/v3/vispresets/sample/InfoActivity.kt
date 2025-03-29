package com.poweramp.v3.vispresets.sample

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPIHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

private const val TAG = "InfoActivity"
private const val LOG = true

private const val REQUEST_ACCESS_MILK_PRESETS = 1


class InfoActivity : Activity() {
    private val pushedFiles = ArrayList<String?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val edit = findViewById<EditText>(R.id.edit)

        // Inject some preset as text
        try {
            InputStreamReader(
                resources.assets.open("milk_presets/Example - example-bars-vertical.milk"),
                StandardCharsets.UTF_8
            ).use { isr ->
                val out = StringBuilder()
                var charsRead: Int
                val bufferSize = 1024
                val buffer = CharArray(bufferSize)
                while((isr.read(buffer, 0, buffer.size).also { charsRead = it }) > 0) {
                    out.append(buffer, 0, charsRead)
                }
                edit.setText(out)
            }
        } catch(e: IOException) {
            Log.e(TAG, "", e)
        }
    }

    /**
     * This opens Poweramp and rescans appropriate preset APK - as specified in [PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK] extra
     */
    fun startWithVisPresets(view: View?) {
        val intent = Intent(Intent.ACTION_MAIN)
            .setClassName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.ACTIVITY_STARTUP)
            .putExtra(PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK, packageName)
        startActivity(intent)
        finish()
    }

    /**
     * This opens Poweramp settings and scrolls to the appropriate preset APK - as specified in [PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK] extra,
     * but this doesn't rescan presets
     */
    fun openPowerampVisSettings(view: View?) {
        val intent = Intent(Intent.ACTION_MAIN)
            .setClassName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.Settings.ACTIVITY_SETTINGS)
            .putExtra(PowerampAPI.Settings.EXTRA_OPEN, PowerampAPI.Settings.OPEN_VIS)
            .putExtra(PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK, packageName)
        // If vis_presets_pak specified for open/theme, will scroll presets list to this apk entry

        startActivity(intent)
        finish()
    }

    /**
     * This asks Poweramp to rescan all presets. At this moment (build 867) all presets are rescanned, not just the given package.<br></br>
     * This works only if current application is foreground, meaning this can't be used from the background service (Android 8+), as
     * Android will block background service execution.
     */
    fun rescanVisPresets(view: View?) {
        val intent = Intent(PowerampAPI.MilkScanner.ACTION_SCAN)
            .setComponent(PowerampAPIHelper.getMilkScannerServiceComponentName(this))
            .putExtra(PowerampAPI.MilkScanner.EXTRA_CAUSE, "Manual rescan from plugin")
            .putExtra(PowerampAPI.MilkScanner.EXTRA_PACKAGE, packageName)
        startService(intent)
    }

    fun sendPreset(view: View?) {
        // Issue SET_VIS_PRESET

        val intent = Intent(PowerampAPI.ACTION_API_COMMAND)
            .setComponent(PowerampAPIHelper.getApiActivityComponentName(this))
            .putExtra(PowerampAPI.EXTRA_PACKAGE, packageName)
            .putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_VIS_PRESET)
            .putExtra(PowerampAPI.EXTRA_NAME, "VisPresetsExample - Test Preset.milk")
            .putExtra(
                PowerampAPI.EXTRA_DATA,
                (findViewById<View?>(R.id.edit) as EditText).getText().toString()
            ) // NOTE: strictly String type is expected
        startActivity(intent)

        // NOTE: we can't immediately do same action/component startActivity immediately, so delay it a bit via post
        Handler(Looper.getMainLooper()).post(object : Runnable {
            override fun run() {

                // Let's force  vis mode to VIS + UI

                // NOTE: Poweramp won't change interface in reflection to just changing this preference. But as we're currently inside other activity,
                // re-opening Poweramp UI will re-read this preference. Though, this may fail for e.g. split screen when both activities are always on top
                val request = Bundle()
                request.putInt("vis_mode", PowerampAPI.Settings.PreferencesConsts.VIS_MODE_VIS_W_UI) // See PowerampAPI.Settings.Preferences
                contentResolver.call(PowerampAPI.ROOT_URI, PowerampAPI.CALL_SET_PREFERENCE, null, request)

                // Now open Poweramp UI main screen
                var intent = Intent(PowerampAPI.ACTION_OPEN_MAIN)
                    .setClassName(PowerampAPIHelper.getPowerampPackageName(this@InfoActivity), PowerampAPI.ACTIVITY_STARTUP)
                startActivity(intent)

                // And command it to play something
                intent = Intent(PowerampAPI.ACTION_API_COMMAND)
                    .setComponent(PowerampAPIHelper.getApiActivityComponentName(this@InfoActivity))
                    .putExtra(PowerampAPI.EXTRA_PACKAGE, packageName)
                    .putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.RESUME)
                startActivity(intent)
            }
        })
    }

    fun listMilkPresets(view: View?) {
        if(Build.VERSION.SDK_INT < 23) {
            // We shouldn't do this for Androids below 10. Instead directly access files as needed
            // Poweramp still supports this functionality for lower Androids, but due to the permission access used, we may ask it directly
            // here for Android 5
            listMilkPresetsImpl()
            return
        }

        if(checkSelfPermission(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS) != PackageManager.PERMISSION_GRANTED) {
            if(LOG) Log.w(TAG, "listMilkPresets requesting permission")
            requestPermissions(arrayOf<String>(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS), REQUEST_ACCESS_MILK_PRESETS)
        } else {
            listMilkPresetsImpl()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if(requestCode == REQUEST_ACCESS_MILK_PRESETS
           && grantResults != null
           && grantResults.isNotEmpty()
           && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if(LOG) Log.w(TAG, "onRequestPermissionsResult PERMISSION_GRANTED")
            listMilkPresetsImpl()
        } else if(LOG) Log.e(
            TAG, "onRequestPermissionsResult !PERMISSION_GRANTED requestCode=" + requestCode +
                 " permissions=" + permissions.contentToString() + " grantResults=" + grantResults.contentToString()
        )
    }

    private fun getExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        if(lastDot == -1 || lastDot == filename.length - 1) {
            return ""
        }
        return filename.substring(lastDot + 1)
    }

    /**
     * This demonstrates access to the preset files in Poweramp /milk_presets/ folder, which are not accessible otherwise on the
     * modern Androids.
     */
    fun listMilkPresetsImpl() {
        if(LOG) Log.w(TAG, "listMilkPresetsImpl")
        val presets = ArrayList<String?>()
        val textures = ArrayList<String?>()
        val zips = ArrayList<String?>()

        // The path parameter also accepts glob patterns, e.g. content://com.maxmpz.audioplayer.milk_presets/*.milk,
        // To quickly query for the single file, you can also use uris like: content://com.maxmpz.audioplayer.milk_presets/single_file
        // NOTE: cols are always ignored, the following columns are always returned:
        //   Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED, Document.COLUMN_SIZE,
        // NOTE: ? symbol requires escaping (%3F)
        val uri = PowerampAPI.MILK_PRESETS_URI

        //Uri uri = Uri.parse("content://com.maxmpz.audioplayer.milk_presets");
        //Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("*.milk").build();
        //Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("*.{jpg|jpeg|png|tga|bmp}").build();
        //Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("file.???").build();
        //Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("*.???").build();
        try {
            contentResolver.query(uri, null, null, null, null).use { c ->
                while(c != null && c.moveToNext()) {
                    val fileName = c.getString(0)
                    val lastModified = c.getLong(1)
                    val size = c.getLong(2)

                    if(LOG) Log.w(TAG, "listMilkPresetsImpl fileName=$fileName lastModified=$lastModified size=$size")

                    val ext = getExtension(fileName).lowercase() // NOTE: Poweramp is not case sensitive for presets
                    when(ext) {
                        "zip" -> {
                            Log.w(TAG, "listMilkPresetsImpl FOUND zip=$fileName")
                            zips.add(fileName)
                        }

                        "prjm", "milk" -> {
                            Log.w(TAG, "listMilkPresetsImpl FOUND milk preset=$fileName")
                            presets.add(fileName)
                        }

                        "jpeg", "jpg", "png", "tga", "bmp" -> {
                            Log.w(TAG, "listMilkPresetsImpl FOUND texture=$fileName")
                            textures.add(fileName)
                        }
                    }
                }
                AlertDialog.Builder(this)
                    .setTitle("listMilkPresets")
                    .setMessage(
                        "Presets: " + presets.size + "\n" +
                        "Zips: " + zips.size + "\n" +
                        "Textures: " + textures.size + "\n"
                    )
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        } catch(th: Throwable) {
            Log.e(TAG, "", th)
        }
    }


    fun pushFile(view: View?) {
        // Assume we asked for permission earlier in list milk presets
        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "pushFile !permission")
            return
        }

        val newFile = "TestPreset - " + System.currentTimeMillis() + ".milk"

        val uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath(newFile).build()
        try {
            contentResolver.openFileDescriptor(uri, "wt").use { pfd ->
                if(pfd != null) {
                    OutputStreamWriter(FileOutputStream(pfd.fileDescriptor)).use { writer ->
                        val edit = findViewById<EditText>(R.id.edit)
                        writer.write(edit.getText().toString())
                    }
                }
                // Ask Poweramp to rescan
                rescanVisPresets(null)

                pushedFiles.add(newFile)
                AlertDialog.Builder(this)
                    .setTitle("pushFile")
                    .setMessage("Pushed file=$newFile")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        } catch(th: Throwable) {
            Log.e(TAG, "", th)
        }
    }

    fun deleteFile(view: View?) {
        // Assume we asked for permission earlier in list milk presets
        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "deleteFile !permission")
            return
        }

        if(pushedFiles.isEmpty()) { // We need some files previously pushed in this app session
            AlertDialog.Builder(this)
                .setTitle("deleteFile")
                .setMessage("No pushed files to delete yet")
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val fileToDelete = pushedFiles[pushedFiles.size - 1] // Delete last file pushed
        pushedFiles.removeAt(pushedFiles.size - 1)

        val uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath(fileToDelete).build()
        try {
            val deleted = contentResolver.delete(uri, null, null)
            if(deleted != 0) {
                // Ask Poweramp to rescan
                rescanVisPresets(null)
            }
            AlertDialog.Builder(this)
                .setTitle("deleteFile")
                .setMessage(if(deleted != 0) "Deleted file=$fileToDelete" else "No files deleted")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } catch(th: Throwable) {
            Log.e(TAG, "", th)
        }
    }
}
