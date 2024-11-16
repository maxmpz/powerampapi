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

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPI.NO_ID
import com.maxmpz.poweramp.player.TableDefs.*
import kotlinx.coroutines.*

/**
 * This is a debug activity.
 * Real plugin may show anything it wants on plugin activities as activity is not needed for the core lyrics plugin
 * functionality
 */
class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val LOG = true
    }

    private val handler = Handler(Looper.getMainLooper())
    private var serial = 0
    private var logTv: TextView? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logTv = findViewById<TextView>(R.id.log)
        logTv?.setMovementMethod(ScrollingMovementMethod())

        val intent = getIntent()
        if(intent != null && intent.action == PowerampAPI.Lyrics.ACTION_LYRICS_LINK
                && !intent.getBooleanExtra("__processed", false)
        ) {
            intent.putExtra("__processed", true)
            handleLyricsLinkIntent(intent)
        }
    }

    private fun handleLyricsLinkIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(LOG) Log.w(TAG, "onNewIntent")
        val msg: String
        val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, NO_ID)
        if(realId != NO_ID) {
            msg = """
            REAL_ID=$realId
            TITLE=${intent.getStringExtra(PowerampAPI.Track.TITLE)}
            ALBUM=${intent.getStringExtra(PowerampAPI.Track.ALBUM)}
            ARTIST=${intent.getStringExtra(PowerampAPI.Track.ARTIST)}
            TYPE=${intent.getStringExtra(PowerampAPI.Track.FILE_TYPE)}
            DURATION_MS=${intent.getIntExtra(PowerampAPI.Track.DURATION_MS, 0)}
            """.trimIndent()
        } else {
            msg = "No track info provided"
        }

        AlertDialog.Builder(this)
                .setTitle("ACTION_LYRICS_LINK")
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    /**
     * On button press we try to load track details from Poweramp and we'll send some fake lyrics directly for that track id
     */
    fun sendLyricsForTrack(v: View) {
        val trackId = findViewById<EditText>(R.id.trackId).text.toString().toLongOrNull(10)

        scope.launch {
            var error: String? = null
            if(trackId != null && trackId != 0L) {
                val uri = PowerampAPI.ROOT_URI.buildUpon().appendEncodedPath("files").appendEncodedPath(trackId.toString()).build()

                // NOTE: we do simple query for track details. This won't handle tracks which do not have such details, such as streams
                error = "no such track id=$trackId" // If we fail checks below, it's the error

                contentResolver.query(
                        uri,
                        arrayOf(Files.TITLE_TAG, Artists.ARTIST, Albums.ALBUM, Files.DURATION, Files.FILE_TYPE),
                        null,
                        null,
                        null
                )?.use { c ->
                    if(c.moveToNext()) {
                        val title = c.getString(0)
                        val artist = c.getString(1).orEmpty()
                        val album = c.getString(2).orEmpty()
                        var durationMs = c.getInt(3)
                        val fileType = c.getInt(4)
                        val lrc = trackId % 3 != 0L // Generate non-lrc for third of realIds

                        val isStream = fileType == PowerampAPI.Track.FileType.TYPE_STREAM
                        if(isStream && durationMs <= 0) durationMs = 60 * 1000; // Let's use some fake duration for our generateFakeLyrics

                        DebugLines.addDebugLine("generating lyrics for trackId=$trackId lrc=$lrc title=$title artist=$artist album=$album" +
                                " dur=$durationMs ")

                        val lyrics = generateFakeLyrics(lrc, "DIRECT:$title", artist, album, durationMs)

                        // Inject some info line for even ids
                        val infoLine: String? = "DIRECTLY updated lyrics by Poweramp Plugin Example (realId=$trackId)"

                        if(sendLyricsResponse(this@MainActivity, trackId, lyrics, infoLine)) {
                            error = null
                        } else {
                            error = "error sending response"
                        }
                    }
                }
            } else {
                error = "bad track id"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,
                        if(error == null) "Sent lyrics OK" else "Failed to send lyrics: $error",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        pollDebugLines()

        // Try to get current track id from Poweramp API
        val trackIntent =
            ContextCompat.registerReceiver(this, null, IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED), ContextCompat.RECEIVER_EXPORTED)
        if(trackIntent != null) {
            findViewById<EditText>(R.id.trackId).setText(trackIntent.getLongExtra(PowerampAPI.Track.REAL_ID, 0L).toString())
        }
    }

    override fun onStop() {
        stopPolling()
        super.onStop()
    }

    override fun onDestroy() {
        stopPolling()
        scope.cancel(null)
        super.onDestroy()
    }

    /** Simple polling to show the latest debug lines */
    private fun pollDebugLines() {
        val serial = DebugLines.serial
        val logTv = logTv
        if(serial != this.serial && logTv != null) {
            this.serial = serial
            logTv.text = DebugLines.getLog()
            handler.post(::scrollToEnd)
        }

        handler.postDelayed(::pollDebugLines, 500)
    }

    private fun scrollToEnd() {
        val logTv = logTv
        if(logTv != null) {
            val layout: Layout? = logTv.layout
            if(layout != null) {
                val scrollDelta: Int = (layout.getLineBottom(logTv.lineCount - 1) - logTv.scrollY - logTv.height)
                if(scrollDelta > 0) logTv.scrollBy(0, scrollDelta)
            }
        }
    }

    private fun stopPolling() {
        handler.removeCallbacksAndMessages(null)
    }
}