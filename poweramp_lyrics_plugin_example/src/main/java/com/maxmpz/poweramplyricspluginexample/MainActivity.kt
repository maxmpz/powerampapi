package com.maxmpz.poweramplyricspluginexample

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.method.ScrollingMovementMethod
import android.widget.TextView

/**
 * This is a debug activity.
 * Real plugin may show anything it wants on plugin activities as activity is not needed for the core lyrics plugin
 * functionality
 */
class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var serial = 0
    private var logTv: TextView? = null
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logTv = findViewById<TextView>(R.id.log)
        logTv?.setMovementMethod(ScrollingMovementMethod())
    }

    override fun onStart() {
        super.onStart()
        poll()
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        super.onStop()
    }

    private fun poll() {
        val serial = DebugLines.serial
        val logTv = logTv
        if(serial != this.serial && logTv != null) {
            this.serial = serial
            logTv.text = DebugLines.getLog()
            handler.post(::scrollToEnd)
        }

        handler.postDelayed(::poll, 500)
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

    companion object {
        private const val TAG = "MainActivity"
        private const val LOG = true
    }
}