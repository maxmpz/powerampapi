package com.maxmpz.poweramplyricspluginexample

import java.util.*

/** NOTE: this is not thread safe */
object DebugLines {
    private const val MAX_LINES = 20
    
    private val lines = ArrayDeque<String>()
    private var _serial: Int = 1

    val serial: Int
        get() {
            return _serial
        }

    fun addDebugLine(line: String) {
        lines.add("- $line")
        if(lines.size == MAX_LINES) {
            lines.removeFirst()
        }
        _serial++
    }

    fun getLog() = if(lines.size > 0) lines.joinToString("\n") else "-empty-"
}