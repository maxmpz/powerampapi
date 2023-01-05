package com.maxmpz.poweramplyricspluginexample

import android.content.Context
import android.content.Intent
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPIHelper

private const val TAG = "sendLyricsResponse"
private const val LOG = true


/**
 * Sends lyrics response for given [realId] track
 * @param lyrics in LRC or plain text format. If null, we indicate a failure to load
 * @return true if we sent it, false on failure
 */
fun sendLyricsResponse(context: Context, realId: Long, lyrics: String?): Boolean {
    val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS)
    intent.putExtra(PowerampAPI.EXTRA_ID, realId)
    intent.putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics) // Can be null
    try {
        PowerampAPIHelper.sendPAIntent(context, intent)
        val debugLine = "sendLyricsResponse realId=$realId th=${Thread.currentThread()}"
        DebugLines.addDebugLine(debugLine)
        if(LOG) Log.w(TAG, debugLine)
        return true
    } catch(th: Throwable) {
        Log.e(TAG, "Failed to send lyrics response", th)
    }
    return false
}
