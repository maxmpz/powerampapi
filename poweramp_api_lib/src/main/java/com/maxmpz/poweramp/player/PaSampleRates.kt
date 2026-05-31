/*
Copyright (C) 2011-2026 Maksim Petrov

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
package com.maxmpz.poweramp.player

import kotlin.math.absoluteValue


// Sync with output-internal.h
const val INTERNAL_OUTPUT_FLAG_FIRST: Int = 0x10
const val INTERNAL_OUTPUT_FLAG_FIRST_PCM: Int = 0x10

const val INTERNAL_OUTPUT_FLAG_SR_44K: Int   = 0x10
const val INTERNAL_OUTPUT_FLAG_SR_48K: Int   = 0x20
const val INTERNAL_OUTPUT_FLAG_SR_88K: Int   = 0x40
const val INTERNAL_OUTPUT_FLAG_SR_96K: Int   = 0x80
const val INTERNAL_OUTPUT_FLAG_SR_176K: Int  = 0x100
const val INTERNAL_OUTPUT_FLAG_SR_192K: Int  = 0x200
const val INTERNAL_OUTPUT_FLAG_SR_352K: Int  = 0x400
const val INTERNAL_OUTPUT_FLAG_SR_384K: Int  = 0x800
const val INTERNAL_OUTPUT_FLAG_SR_705K: Int  = 0x1000
const val INTERNAL_OUTPUT_FLAG_SR_768K: Int  = 0x2000
// = 10

/** The last _supported_ PCM */
const val INTERNAL_OUTPUT_FLAG_LAST_PCM: Int  = 0x2000

// Non supported rates
const val INTERNAL_OUTPUT_FLAG_SR_1411K: Int = 0x4000 // 1411200 Hz
const val INTERNAL_OUTPUT_FLAG_SR_1536K: Int = 0x8000 // 1536000 Hz
const val INTERNAL_OUTPUT_FLAG_SR_2822K: Int = 0x10000 // 2822400 Hz
const val INTERNAL_OUTPUT_FLAG_SR_3072K: Int = 0x20000 // 3072000 Hz
// = 14
// 0x40000
// 0x80000

const val INTERNAL_OUTPUT_FLAG_FIRST_DSD: Int = 0x100000

const val INTERNAL_OUTPUT_FLAG_DSD64: Int      = 0x100000
const val INTERNAL_OUTPUT_FLAG_DSD64x48: Int   = 0x200000
const val INTERNAL_OUTPUT_FLAG_DSD128: Int     = 0x400000
const val INTERNAL_OUTPUT_FLAG_DSD128x48: Int  = 0x800000
const val INTERNAL_OUTPUT_FLAG_DSD256: Int     = 0x1000000
const val INTERNAL_OUTPUT_FLAG_DSD256x48: Int  = 0x2000000
const val INTERNAL_OUTPUT_FLAG_DSD512: Int     = 0x4000000
const val INTERNAL_OUTPUT_FLAG_DSD512x48: Int  = 0x8000000
const val INTERNAL_OUTPUT_FLAG_DSD1024: Int    = 0x10000000
const val INTERNAL_OUTPUT_FLAG_DSD1024x48: Int = 0x20000000

/** Last _supported_ DSD */
const val INTERNAL_OUTPUT_FLAG_LAST_DSD: Int = 0x1000000
const val INTERNAL_OUTPUT_FLAG_LAST: Int     = 0x1000000 // Special, update when bits updated

// Non supported rates
const val INTERNAL_OUTPUT_FLAG_DSD2048: Int    = 0x40000000
const val INTERNAL_OUTPUT_FLAG_DSD2048x48: Int = 0x80000000.toInt()
// = 12

const val PA_48K_BASED_PCM_SAMPLE_RATES_MASK: Int =
    INTERNAL_OUTPUT_FLAG_SR_44K or
    INTERNAL_OUTPUT_FLAG_SR_88K or
    INTERNAL_OUTPUT_FLAG_SR_176K or
    INTERNAL_OUTPUT_FLAG_SR_352K or
    INTERNAL_OUTPUT_FLAG_SR_705K

/** Supported PCM rates */
const val PA_44K_BASED_PCM_SAMPLE_RATES_MASK: Int =
    INTERNAL_OUTPUT_FLAG_SR_48K or
    INTERNAL_OUTPUT_FLAG_SR_96K or
    INTERNAL_OUTPUT_FLAG_SR_192K or
    INTERNAL_OUTPUT_FLAG_SR_384K or
    INTERNAL_OUTPUT_FLAG_SR_768K

const val PA_ALL_PCM_SAMPLE_RATES_MASK: Int = PA_48K_BASED_PCM_SAMPLE_RATES_MASK or PA_44K_BASED_PCM_SAMPLE_RATES_MASK

const val PA_44K_BASED_DSD_SAMPLE_RATES_MASK: Int =
    INTERNAL_OUTPUT_FLAG_DSD64 or
    INTERNAL_OUTPUT_FLAG_DSD128 or
    INTERNAL_OUTPUT_FLAG_DSD256 or
    INTERNAL_OUTPUT_FLAG_DSD512 or
    INTERNAL_OUTPUT_FLAG_DSD1024

const val PA_48K_BASED_DSD_SAMPLE_RATES_MASK: Int =
    INTERNAL_OUTPUT_FLAG_DSD64x48 or
    INTERNAL_OUTPUT_FLAG_DSD128x48 or
    INTERNAL_OUTPUT_FLAG_DSD256x48 or
    INTERNAL_OUTPUT_FLAG_DSD512x48 or
    INTERNAL_OUTPUT_FLAG_DSD1024x48

/** Supported DSD rates */
const val PA_ALL_DSD_SAMPLE_RATES_MASK: Int = PA_44K_BASED_DSD_SAMPLE_RATES_MASK or PA_48K_BASED_DSD_SAMPLE_RATES_MASK

/** All supported rates */
const val PA_ALL_SAMPLE_RATES_MASK: Int = PA_ALL_PCM_SAMPLE_RATES_MASK or PA_ALL_DSD_SAMPLE_RATES_MASK

/** All defined sample rates, including unsupported. This should be used for the debugging only */
const val PA_DEBUG_SAMPLE_RATES_MASK: Int = PA_ALL_SAMPLE_RATES_MASK or
                                            INTERNAL_OUTPUT_FLAG_SR_1411K or
                                            INTERNAL_OUTPUT_FLAG_SR_1536K or
                                            INTERNAL_OUTPUT_FLAG_SR_2822K or
                                            INTERNAL_OUTPUT_FLAG_SR_3072K or
                                            INTERNAL_OUTPUT_FLAG_DSD2048 or
                                            INTERNAL_OUTPUT_FLAG_DSD2048x48


/** @return true if this rate seems to be a sample rate, but not necessary a supported sample rate */
fun isSaneSampleRate(sr: Int): Boolean  = sr != 0 && sr != -1 && sr != Int.MIN_VALUE && sr != Int.MAX_VALUE

/** @return index of the first sample rate bit or -1 if none */
fun paFirstSampleRateBit(srFlags: Int): Int {
    val masked = srFlags and PA_ALL_SAMPLE_RATES_MASK
    if(masked == 0) return -1 else return srFlags.countTrailingZeroBits()
}

/** @return index of the last sample rate bit or -1 if none */
fun paLastSampleRateBit(srFlags: Int): Int {
    val masked = srFlags and PA_ALL_SAMPLE_RATES_MASK
    if(masked == 0) return -1 else return 31 - srFlags.countLeadingZeroBits()
}

fun paSampleRateBitToFlag(bit: Int): Int {
    return (1 shl bit) and PA_ALL_SAMPLE_RATES_MASK
}

/** @return number of sample rates in this bitset */
inline fun Int.iterateSampleRateBits(block: (sr: Int, srFlag: Int) -> Unit): Int {
    var srCount = 0
    for(srBit in paFirstSampleRateBit(this)..paLastSampleRateBit(this)) {
        val srFlag = paSampleRateBitToFlag(srBit)
        if((this and srFlag) != 0) {
            val sr = paSampleRateFlagToValue(srFlag)
            if(sr != 0) {
                block(sr, srFlag)
                srCount++
            }
        } // Else this is, e.g., just padding 0
    }
    return srCount
}

/**
 * NOTE: this SKIPS unsupported sample rates
 * @return DSD returned as a negative value
 */
fun paSupportedSampleRateFlagToValue(flag: Int): Int = paSampleRateFlagToValue(PA_ALL_SAMPLE_RATES_MASK and flag)

/**
 * NOTE: this handles unsupported sample rates
 * @return DSD returned as a negative value
 */
fun paSampleRateFlagToValue(flag: Int): Int = when(flag) {
    INTERNAL_OUTPUT_FLAG_SR_44K -> 44100
    INTERNAL_OUTPUT_FLAG_SR_48K -> 48000
    INTERNAL_OUTPUT_FLAG_SR_88K -> 88200
    INTERNAL_OUTPUT_FLAG_SR_96K -> 96000
    INTERNAL_OUTPUT_FLAG_SR_176K -> 176400
    INTERNAL_OUTPUT_FLAG_SR_192K -> 192000
    INTERNAL_OUTPUT_FLAG_SR_352K -> 352800
    INTERNAL_OUTPUT_FLAG_SR_384K -> 384000
    INTERNAL_OUTPUT_FLAG_SR_705K -> 705600
    INTERNAL_OUTPUT_FLAG_SR_768K -> 768000

    INTERNAL_OUTPUT_FLAG_SR_1411K -> 1411200
    INTERNAL_OUTPUT_FLAG_SR_1536K -> 1536000
    INTERNAL_OUTPUT_FLAG_SR_2822K -> 2822400
    INTERNAL_OUTPUT_FLAG_SR_3072K -> 3072000

    INTERNAL_OUTPUT_FLAG_DSD64      -> -2822400 // => ffmpeg 352800
    INTERNAL_OUTPUT_FLAG_DSD128     -> -5644800
    INTERNAL_OUTPUT_FLAG_DSD256     -> -11289600
    INTERNAL_OUTPUT_FLAG_DSD512     -> -22579200
    INTERNAL_OUTPUT_FLAG_DSD1024    -> -45158400 // => ffmpeg 5644800

    INTERNAL_OUTPUT_FLAG_DSD64x48   -> -3072000
    INTERNAL_OUTPUT_FLAG_DSD128x48  -> -6144000
    INTERNAL_OUTPUT_FLAG_DSD256x48  -> -12288000
    INTERNAL_OUTPUT_FLAG_DSD512x48  -> -24576000
    INTERNAL_OUTPUT_FLAG_DSD1024x48 -> -49152000

    INTERNAL_OUTPUT_FLAG_DSD2048    -> -90316800
    INTERNAL_OUTPUT_FLAG_DSD2048x48 -> -98304000
    else -> 0
}

/** @param sr DSD is expected as a negative value */
fun paSampleRateToFlag(sr: Int): Int = when(sr) {
    44100 -> INTERNAL_OUTPUT_FLAG_SR_44K
    48000 -> INTERNAL_OUTPUT_FLAG_SR_48K
    88200 -> INTERNAL_OUTPUT_FLAG_SR_88K
    96000 -> INTERNAL_OUTPUT_FLAG_SR_96K
    176400 -> INTERNAL_OUTPUT_FLAG_SR_176K
    192000 -> INTERNAL_OUTPUT_FLAG_SR_192K
    352800 -> INTERNAL_OUTPUT_FLAG_SR_352K
    384000 -> INTERNAL_OUTPUT_FLAG_SR_384K
    705600 -> INTERNAL_OUTPUT_FLAG_SR_705K
    768000 -> INTERNAL_OUTPUT_FLAG_SR_768K

    1411200 -> INTERNAL_OUTPUT_FLAG_SR_1411K
    1536000 -> INTERNAL_OUTPUT_FLAG_SR_1536K
    2822400 -> INTERNAL_OUTPUT_FLAG_SR_2822K
    3072000 -> INTERNAL_OUTPUT_FLAG_SR_3072K

    -2822400 -> INTERNAL_OUTPUT_FLAG_DSD64
    -5644800 -> INTERNAL_OUTPUT_FLAG_DSD128
    -11289600 -> INTERNAL_OUTPUT_FLAG_DSD256
    -22579200 -> INTERNAL_OUTPUT_FLAG_DSD512
    -45158400 -> INTERNAL_OUTPUT_FLAG_DSD1024

    -3072000 -> INTERNAL_OUTPUT_FLAG_DSD64x48
    -6144000 -> INTERNAL_OUTPUT_FLAG_DSD128x48
    -12288000 -> INTERNAL_OUTPUT_FLAG_DSD256x48
    -24576000 -> INTERNAL_OUTPUT_FLAG_DSD512x48
    -49152000 -> INTERNAL_OUTPUT_FLAG_DSD1024x48

    -90316800 -> INTERNAL_OUTPUT_FLAG_DSD2048
    -98304000 -> INTERNAL_OUTPUT_FLAG_DSD2048x48

    else -> 0
}

fun getShortSampleRateNameFromFlag(flag: Int, defaultString: String = "-"): String
    = when(flag) {
        INTERNAL_OUTPUT_FLAG_SR_44K -> "44.1"
        INTERNAL_OUTPUT_FLAG_SR_48K -> "48"
        INTERNAL_OUTPUT_FLAG_SR_88K -> "88.2"
        INTERNAL_OUTPUT_FLAG_SR_96K -> "96"
        INTERNAL_OUTPUT_FLAG_SR_176K -> "176.4"
        INTERNAL_OUTPUT_FLAG_SR_192K -> "192"
        INTERNAL_OUTPUT_FLAG_SR_352K -> "352.8"
        INTERNAL_OUTPUT_FLAG_SR_384K -> "384"
        INTERNAL_OUTPUT_FLAG_SR_705K -> "705.6"
        INTERNAL_OUTPUT_FLAG_SR_768K -> "768"
        INTERNAL_OUTPUT_FLAG_SR_1411K -> "1411.2"
        INTERNAL_OUTPUT_FLAG_SR_1536K -> "1536"
        INTERNAL_OUTPUT_FLAG_SR_2822K -> "2822.4"
        INTERNAL_OUTPUT_FLAG_SR_3072K -> "3072"

        INTERNAL_OUTPUT_FLAG_DSD64 -> "DSD64" // => ffmpeg 352800
        INTERNAL_OUTPUT_FLAG_DSD128 -> "DSD128"
        INTERNAL_OUTPUT_FLAG_DSD256 -> "DSD256"
        INTERNAL_OUTPUT_FLAG_DSD512 -> "DSD512"
        INTERNAL_OUTPUT_FLAG_DSD1024 -> "DSD1024" // => ffmpeg 5644800
        INTERNAL_OUTPUT_FLAG_DSD64x48   -> "DSD64x48"
        INTERNAL_OUTPUT_FLAG_DSD128x48  -> "DSD128x48"
        INTERNAL_OUTPUT_FLAG_DSD256x48  -> "DSD256x48"
        INTERNAL_OUTPUT_FLAG_DSD512x48  -> "DSD512x48"
        INTERNAL_OUTPUT_FLAG_DSD1024x48 -> "DSD1024x48"
        else -> defaultString
    }

/**
 * @return short name of the sample rate - xxK or DSDxx or defaultString ("-") for unknown sample rate flag
 */
fun getShortSampleRateNameFromFlag(
    flag: Int,
    kHzLabel: String? = "K",
    needSpaceBeforeHz: Boolean = false,
    defaultString: String = "-",
): String {
    if((flag and PA_ALL_SAMPLE_RATES_MASK) == 0) return defaultString
    // Generally can't be defaultString here, but let's use ? to indicate logic error
    val name = getShortSampleRateNameFromFlag(flag, "?")
    if((flag and PA_ALL_PCM_SAMPLE_RATES_MASK) != 0) {
        return if(kHzLabel.isNullOrEmpty()) name else name + if(needSpaceBeforeHz) " $kHzLabel" else kHzLabel
    }
    return name
}

private fun getSampleRateInKhz(sr: Int, defaultLabel: String = "-"): String {
    if(sr == 0) return defaultLabel
    val sr = sr.absoluteValue
    val kHz = sr / 1000
    val fraction: Int = sr - kHz * 1000
    if(fraction < 100) {
        return kHz.toString()
    }
    return kHz.toString() + "." + fraction / 100
}

private fun getSampleRateInMhz(sr: Int, defaultLabel: String = "-"): String {
    if(sr == 0) return defaultLabel
    val sr = sr.absoluteValue
    val mHz = sr / 1000000
    val fraction: Int = sr - mHz * 1000000
    if(fraction < 100) {
        return mHz.toString()
    }
    return mHz.toString() + "." + fraction / 100
}

@JvmOverloads
fun getSampleRateAndLabel(
    sr: Int,
    kHzLabel: String = "K",
    needSpaceBeforeHz: Boolean = false,
    defaultLabel: String = "-",
    expandDSDRate: Boolean = false,
    mHzLabel: String = "M"
): String {
    val srFlag = paSampleRateToFlag(sr)
    if(srFlag != 0) {
        // We're good - matched SR
        val srAndLabel = getShortSampleRateNameFromFlag(srFlag, kHzLabel, needSpaceBeforeHz, defaultLabel)
        if(expandDSDRate && (srFlag and PA_ALL_DSD_SAMPLE_RATES_MASK) != 0) {
            return srAndLabel + " " + getSampleRateInMhz(sr.absoluteValue) + (if(needSpaceBeforeHz) " $mHzLabel" else mHzLabel)
        }
        return srAndLabel
    }
    if(sr > 0) {
        // Allow non-standard PCM rates, e.g., for tracks
        return getSampleRateInKhz(sr) + if(needSpaceBeforeHz) " $kHzLabel" else kHzLabel
    } else if(sr < -1) { // Unknown DSD, keep ?. NOTE: we ignore possible -1
        return "DSD? " + getSampleRateInMhz(sr.absoluteValue) + (if(needSpaceBeforeHz) " $mHzLabel" else mHzLabel)
    } else {
        return defaultLabel
    }
}

