/*
Copyright (C) 2011-2023 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

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
*/

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
import kotlinx.coroutines.*


/**
 * Fake lyrics request receiver, which will generate simple fake lyrics in response.
 * Real plugin should use some background thread/job to initiate any network or IO processing.
 * This receiver is called from the foreground Poweramp process.
 */
class LyricsRequestReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "LyricsRequestReceiver"
        private const val LOG = true
        private const val DEBUG_DELAY_RESPONSE_MS = 1000L // Just a debugging delay
    }

    private val guiHandler = Handler(Looper.getMainLooper())

    
    @OptIn(DelicateCoroutinesApi::class)
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
            val durationMs = intent.getIntExtra(PowerampAPI.Track.DURATION_MS, 0)
            // We can extract other PowerampAPI.Track fields from extras here if needed

            val debugLine = "ACTION_NEED_LYRICS realId=$realId title=$title album=$album artist=$artist durationMs=$durationMs"
            DebugLines.addDebugLine(debugLine)
            if(LOG) Log.w(TAG, debugLine)

            // Inject some info line for even ids
            val infoLine: String? = if((realId and 0x1L) == 0L) "Lyrics by Poweramp Plugin Example (realId=$realId)" else null
            val lrc = realId % 3 != 0L // Generate non-lrc for third of realIds

            // Real plugin will initiate some background http request here for lyrics,
            // we just emulate the response with some delay
            GlobalScope.launch(Dispatchers.IO) {
                val lyrics = generateFakeLyrics(lrc, title, album, artist, durationMs)

                delay(DEBUG_DELAY_RESPONSE_MS)

                sendLyricsResponse(context, realId, lyrics, infoLine)
            }
        }
    }
}
