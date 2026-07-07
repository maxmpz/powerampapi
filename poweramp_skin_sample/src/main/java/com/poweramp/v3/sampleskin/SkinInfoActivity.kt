package com.poweramp.v3.sampleskin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.maxmpz.poweramp.player.PowerampAPI.ACTION_SKIN_VERIFICATION
import com.maxmpz.poweramp.player.PowerampAPI.ACTION_START_SKIN_VERIFICATION
import com.maxmpz.poweramp.player.PowerampAPI.API_ACTIVITY_NAME
import com.maxmpz.poweramp.player.PowerampAPI.EXTRA_PACKAGE
import com.maxmpz.poweramp.player.PowerampAPIHelper.getPowerampPackageName

private const val TAG = "SkinInfoActivity"
private const val LOG = true


/** Helper function to get the resolved Poweramp package or show the error toast */
internal fun Context.getPaPak(): String? {
    val pak = getPowerampPackageName(this, false)
    if(pak == null) {
        Toast.makeText(this, R.string.skin_poweramp_not_installed, Toast.LENGTH_LONG).show()
    }
    return pak
}


class SkinInfoActivity : Activity() {
    companion object {
        var skinVerifiedOnce = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_info)
    }

    override fun onResume() {
        super.onResume()
        // Disable reverify button if we already successfully verified during this process lifetime
        findViewById<View>(R.id.reverify_button).isEnabled = !skinVerifiedOnce
    }


    /**
     * Optional start of the (optional) verification for this skin. Normally, Poweramp will redirect a user to this activity
     * when the skin is unverified and the user tries to open its settings, but skin also may initiate such verification by
     * itself. Skin doesn't know in advance if the skin is verified or unverified, so wording for the verification button
     * should allow it, e.g., Re-verification of the skin.
     * In response to [ACTION_START_SKIN_VERIFICATION] Poweramp will immediately redirect back to this app's
     * [SkinVerificationActivity] with [ACTION_SKIN_VERIFICATION].
     */
    fun reverifySkin(view: View?) {
        val pak = getPaPak() ?: return
        // NOTE: use Poweramp API activity + startActivity, not the API receiver + sendBroadcast: Poweramp needs an
        // activity launch path here because it opens this app's verification activity in response.

        findViewById<View>(R.id.reverify_button).isEnabled = false

        val intent = Intent(ACTION_START_SKIN_VERIFICATION)
            .setClassName(pak, API_ACTIVITY_NAME)
            .putExtra(EXTRA_PACKAGE, packageName)

        startActivity(intent)
    }

    fun startWithSampleSkin(view: View?) {
        val pak = getPaPak() ?: return

        val intent = Intent(Intent.ACTION_MAIN)
            .setClassName(pak, "com.maxmpz.audioplayer.StartupActivity")
            .putExtra("theme_pak", packageName)
            .putExtra("theme_id", R.style.SampleSkin)
        startActivity(intent)
        //Toast.makeText(this, R.string.skin_applied_msg, Toast.LENGTH_LONG).show(); // Enable toast if needed
        finish()
    }

    fun startWithSampleAAASkin(view: View?) {
        val pak = getPaPak() ?: return

        val intent = Intent(Intent.ACTION_MAIN)
            .setClassName(pak, "com.maxmpz.audioplayer.StartupActivity")
            .putExtra("theme_pak", packageName)
            .putExtra("theme_id", R.style.SampleSkinAAA)
        startActivity(intent)
        //Toast.makeText(this, R.string.skin_applied_msg, Toast.LENGTH_LONG).show(); // Enable toast if needed
        finish()
    }

    fun openPowerampThemeSettings(view: View?) {
        val pak = getPaPak() ?: return

        val intent = Intent(Intent.ACTION_MAIN)
            .setClassName(pak, "com.maxmpz.audioplayer.SettingsActivity")
            // If theme_pak/theme_id specified for open/theme, will scroll to/opens this skins settings
            .putExtra("open", "theme")
            .putExtra("theme_pak", packageName)
            .putExtra("theme_id", R.style.SampleSkin)
        startActivityForResult(intent, 1) // startActivityForResult is required for open=theme
        finish()
    }
}
