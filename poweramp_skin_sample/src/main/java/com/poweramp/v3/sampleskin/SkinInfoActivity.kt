package com.poweramp.v3.sampleskin

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.IntentCompat
import com.maxmpz.poweramp.player.PowerampAPI.ACTION_SKIN_VERIFICATION
import com.maxmpz.poweramp.player.PowerampAPI.EXTRA_TOKEN

private const val TAG = "SkinInfoActivity"


class SkinInfoActivity : Activity() {
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_info)

        mayBeInitiateVerification()
    }

    private fun mayBeInitiateVerification() {
        val intent = intent
        if(intent != null && intent.action == ACTION_SKIN_VERIFICATION) {
            val token = IntentCompat.getParcelableExtra(intent, EXTRA_TOKEN, PendingIntent::class.java)
            if(token != null) {
                yourSkinVerification(token)
            }
        }
    }

    // Your code to verify the skin synchronously or asynchronously. We use delay here just for the demonstration
    private fun yourSkinVerification(token: PendingIntent) {
        // Disable the action buttons + show progress while we verify. There is no time limit on the verification — the
        // skin may take arbitrarily long (a purchase prompt, Play Billing / license server calls, etc.).
        setControlsEnabled(false)
        findViewById<View>(R.id.progress)?.visibility = VISIBLE

        // --- Google Play purchase verification (sample skeleton; add the Play Billing library and uncomment to use) ---
        // Verify the user actually owns this skin BEFORE sending the token. Real apps run their own check here. Only call
        // verificationDone(true, token) once it passes; call verificationDone(false, token) otherwise.
        //
        // val billing = BillingClient.newBuilder(this).enablePendingPurchases().setListener { _, _ -> }.build()
        // billing.startConnection(object : BillingClientStateListener {
        //     override fun onBillingSetupFinished(result: BillingResult) {
        //         if(result.responseCode != BillingClient.BillingResponseCode.OK) { verificationDone(false, token); return }
        //         billing.queryPurchasesAsync(
        //             QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        //         ) { _, purchases ->
        //             val owned = purchases.any { p ->
        //                 p.purchaseState == Purchase.PurchaseState.PURCHASED /* && p.products.contains(YOUR_SKU) */
        //             }
        //             verificationDone(owned, token)
        //         }
        //     }
        //     override fun onBillingServiceDisconnected() { verificationDone(false, token) }
        // })
        // return

        // Demo: post a positive result after a delay. Real apps shouldn't use this delay — it only simulates async work.
        handler.postDelayed({ verificationDone(true, token) }, 1000)
    }

    private fun verificationDone(ok: Boolean, token: PendingIntent) {
        findViewById<View>(R.id.progress)?.visibility = GONE

        // Re-enable the action buttons
        setControlsEnabled(true)

        if(ok) {
            Toast.makeText(this, R.string.successfully_verified_excl, Toast.LENGTH_SHORT).show()
            handler.postDelayed({
                // Firing the token tells Poweramp the skin verified itself. The token is one-shot + immutable; Poweramp
                // accepts it only if this package is still in its allowlist under the same signer-key.
                token.send()
                finish()
            }, 1000)

        } else {
            Toast.makeText(this, R.string.failed_to_verify, Toast.LENGTH_LONG).show(); // Enable toast if needed
        }
    }

    /** Enable/disable the activity's action buttons while verification is in progress. */
    private fun setControlsEnabled(enabled: Boolean) {
        val buttons = findViewById<ViewGroup>(R.id.buttons) ?: return
        for(i in 0 until buttons.childCount) {
            buttons.getChildAt(i).isEnabled = enabled
        }
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

    private fun getPaPak(): String? {
        val pak = getPowerampPackageName(this)
        if(pak == null) {
            Toast.makeText(this, R.string.skin_poweramp_not_installed, Toast.LENGTH_LONG).show()
        }
        return pak
    }

    companion object {
        /**
         * @return resolved Poweramp package name or null if not installed
         * NOTE: can be called from any thread, though double initialization is possible, but it's OK
         */
        fun getPowerampPackageName(context: Context): String? {
            try {
                val info = context.packageManager.resolveService(Intent("com.maxmpz.audioplayer.API_COMMAND"), 0)
                if(info != null && info.serviceInfo != null) {
                    return info.serviceInfo.packageName
                }
            } catch(th: Throwable) {
                Log.e(TAG, "", th)
            }
            return null
        }
    }
}
