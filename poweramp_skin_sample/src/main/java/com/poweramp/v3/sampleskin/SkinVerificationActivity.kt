package com.poweramp.v3.sampleskin

import android.app.Activity
import android.app.PendingIntent
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
import com.maxmpz.poweramp.player.PowerampAPI.*
import com.poweramp.v3.sampleskin.SkinInfoActivity.Companion.skinVerifiedOnce

private const val TAG = "SkinVerificationActivity"
private const val LOG = true


class SkinVerificationActivity : Activity() {
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_verification)

        if(LOG) Log.w(TAG, "onCreate intent.action=${intent.action}")
        mayBeInitiateVerification()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(LOG) Log.w(TAG, "onNewIntent intent.action=${intent.action}")
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
        if(LOG) Log.w(TAG, "yourSkinVerification")
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

        if(ok) {
            // Store this on successful verification to avoid useless reverification (at least while this app is alive)
            skinVerifiedOnce = true

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
}
