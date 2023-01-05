package com.maxmpz.poweramplyricspluginexample

import android.util.Log

private const val TAG = "LyricsRequestReceiver"
private const val LOG = true


fun generateFakeLyrics(title: String, album: String?, artist: String?, durationMs: Int): String? {
    // For the tracks starting from "A" or "O", return no lyrics - just for the testing purposes
    if(title.startsWith("A", ignoreCase = true) || title.startsWith("O", ignoreCase = true)) {
        if(LOG) Log.w(TAG, "generateFakeLyrics return NO LYRICS for title=$title")
        return null
    }

    // Generate random number of LRC lines
    val sb = StringBuilder()
    var timeMs = 0
    val msInMin = 1000 * 60
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
            1 -> sb.append(if(album.isNullOrBlank()) "-" else album)
            2 -> sb.append(if(artist.isNullOrBlank()) "-" else artist)
            else -> sb.append("Fake line #$count $time")
        }
        sb.append("\n")
        count++
    }

    return sb.toString()
}
