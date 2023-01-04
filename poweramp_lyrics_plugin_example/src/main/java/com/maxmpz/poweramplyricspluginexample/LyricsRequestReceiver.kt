package com.maxmpz.poweramplyricspluginexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPI.NO_ID
import com.maxmpz.poweramp.player.PowerampAPIHelper

class LyricsRequestReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "LyricsRequestReceiver"
        private const val LOG = true
        private const val DEBUG_DELAY_RESPONSE_MS = 1000L // Just a debugging delay
    }

    private val guiHandler = Handler(Looper.getMainLooper())

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == PowerampAPI.Lyrics.ACTION_NEED_LYRICS) {

            // Extract few extras, such as real track id, album, artist, title, duration, so we're able to do
            // a "search".
            // RealId is required for the response.
            // Title is generally usually required as well
            val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, NO_ID)
            val title = intent.getStringExtra(PowerampAPI.Track.TITLE)

            if(realId == NO_ID || title.isNullOrEmpty()) {
                Log.e(TAG, "FAIL readId=$realId title=$title")
                return
            }

            // NOTE: album/artist can be "" (empty string) for the unknown album/artist

            val album = intent.getStringExtra(PowerampAPI.Track.ALBUM)
            val artist = intent.getStringExtra(PowerampAPI.Track.ARTIST)
            val durationS = intent.getIntExtra(PowerampAPI.Track.DURATION, 0)
            // We can extract other PowerampAPI.Track fields from extras here if needed

            val debugLine = "ACTION_NEED_LYRICS realId=$realId title=$title album=$album artist=$artist durationS=$durationS"
            DebugLines.addDebugLine(debugLine)
            if(LOG) Log.w(TAG, debugLine)

            // Real plugin will initiate some background http request here for lyrics,
            // we just emulate the response with some delay

            guiHandler.postDelayed({

                val lyrics = generateFakeLyrics(title, album, artist, durationS)

                sendLyricsResponse(context, realId, lyrics)

            }, DEBUG_DELAY_RESPONSE_MS)
        }
    }

    private fun sendLyricsResponse(context: Context, realId: Long, lyrics: String?) {
        val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS)
        intent.putExtra(PowerampAPI.EXTRA_ID, realId)
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics) // Can be null
        try {
            PowerampAPIHelper.sendPAIntent(context, intent)
            val debugLine = "sendLyricsResponse realId=$realId"
            DebugLines.addDebugLine(debugLine)
            if(LOG) Log.w(TAG, debugLine)
        } catch(th: Throwable) {
            Log.e(TAG, "Failed to send lyrics response", th)
        }
    }

    private fun generateFakeLyrics(title: String, album: String?, artist: String?, durationS: Int): String? {
        // For the tracks starting from "A" or "O", return no lyrics - just for the testing purposes
        if(title.startsWith("A", ignoreCase = true) || title.startsWith("O", ignoreCase = true)) {
            if(LOG) Log.w(TAG, "generateFakeLyrics return NO LYRICS for title=$title")
            return null
        }

        // Generate random number of LRC lines
        val sb = StringBuilder()
        var timeMs = 0
        val msInMin = 1000 * 60
        val durationMs = durationS * 1000
        var count = 0
        while(true) {
            timeMs += (250..5000).random() // Random line time
            if(timeMs >= durationMs) break

            val min = timeMs / msInMin
            val s = (timeMs - min * msInMin) / 1000
            val ms = timeMs % 1000
            val sPadded = s.toString().padStart(2, '0')
            val msPadded = ms.toString().padStart(3, '0')

            val time = "$min:$sPadded.$msPadded"
            sb.append("[$time]")
            when(count) {
                0 -> sb.append(title)
                1 -> sb.append(album)
                2 -> sb.append(artist)
                else -> sb.append("Fake line #$count $time")
            }
            sb.append("\n")
            count++
        }

        return sb.toString()
    }
}