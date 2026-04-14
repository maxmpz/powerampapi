package com.maxmpz.poweramp.player

// Sync with output-internal.h
const val INTERNAL_OUTPUT_FLAG_FIRST: Int = 0x10
const val INTERNAL_OUTPUT_FLAG_FIRST_PCM: Int = 0x10

const val INTERNAL_OUTPUT_FLAG_SR_44K: Int = 0x10
const val INTERNAL_OUTPUT_FLAG_SR_48K: Int = 0x20
const val INTERNAL_OUTPUT_FLAG_SR_88K: Int = 0x40
const val INTERNAL_OUTPUT_FLAG_SR_96K: Int = 0x80
const val INTERNAL_OUTPUT_FLAG_SR_176K: Int = 0x100
const val INTERNAL_OUTPUT_FLAG_SR_192K: Int = 0x200
const val INTERNAL_OUTPUT_FLAG_SR_352K: Int = 0x400
const val INTERNAL_OUTPUT_FLAG_SR_384K: Int = 0x800
const val INTERNAL_OUTPUT_FLAG_SR_705K: Int = 0x1000
const val INTERNAL_OUTPUT_FLAG_SR_768K: Int = 0x2000

const val INTERNAL_OUTPUT_FLAG_LAST_PCM: Int  = 0x2000
const val INTERNAL_OUTPUT_FLAG_FIRST_DSD: Int = 0x100000

const val INTERNAL_OUTPUT_FLAG_DSD64: Int = 0x100000
const val INTERNAL_OUTPUT_FLAG_DSD128: Int = 0x200000
const val INTERNAL_OUTPUT_FLAG_DSD256: Int = 0x400000
const val INTERNAL_OUTPUT_FLAG_DSD512: Int = 0x800000
const val INTERNAL_OUTPUT_FLAG_DSD1024: Int = 0x1000000

const val INTERNAL_OUTPUT_FLAG_LAST_DSD: Int = 0x1000000
const val INTERNAL_OUTPUT_FLAG_LAST: Int = 0x1000000 // Special, update when bits updated


const val PA_ALL_PCM_SAMPLE_RATES_MASK: Int =
    INTERNAL_OUTPUT_FLAG_SR_44K or INTERNAL_OUTPUT_FLAG_SR_48K or
    INTERNAL_OUTPUT_FLAG_SR_88K or INTERNAL_OUTPUT_FLAG_SR_96K or
    INTERNAL_OUTPUT_FLAG_SR_176K or INTERNAL_OUTPUT_FLAG_SR_192K or
    INTERNAL_OUTPUT_FLAG_SR_352K or INTERNAL_OUTPUT_FLAG_SR_384K or
    INTERNAL_OUTPUT_FLAG_SR_705K or INTERNAL_OUTPUT_FLAG_SR_768K

const val PA_ALL_DSD_SAMPLE_RATES_MASK: Int =
    INTERNAL_OUTPUT_FLAG_DSD64 or
    INTERNAL_OUTPUT_FLAG_DSD128 or
    INTERNAL_OUTPUT_FLAG_DSD256 or
    INTERNAL_OUTPUT_FLAG_DSD512 or
    INTERNAL_OUTPUT_FLAG_DSD1024

const val PA_ALL_SAMPLE_RATES_MASK: Int = PA_ALL_PCM_SAMPLE_RATES_MASK or PA_ALL_DSD_SAMPLE_RATES_MASK


/** @return index of the first sample rate bit or -1 if none */
fun getFirstSampleRateBit(srFlags: Int): Int {
    val masked = srFlags and PA_ALL_SAMPLE_RATES_MASK
    if(masked == 0) return -1 else return srFlags.countTrailingZeroBits()
}

/** @return index of the last sample rate bit or -1 if none */
fun getLastSampleRateBit(srFlags: Int): Int {
    val masked = srFlags and PA_ALL_SAMPLE_RATES_MASK
    if(masked == 0) return -1 else return 31 - srFlags.countLeadingZeroBits()
}

fun sampleRateBitToFlag(bit: Int): Int {
    return (1 shl bit) and PA_ALL_SAMPLE_RATES_MASK
}

/** @return number of sample rates in this bitset */
inline fun Int.iterateSampleRateBits(block: (sr: Int, srFlag: Int) -> Unit): Int {
    var srCount = 0
    for(srBit in getFirstSampleRateBit(this)..getLastSampleRateBit(this)) {
        val srFlag = sampleRateBitToFlag(srBit)
        if((this and srFlag) != 0) {
            val sr = getSampleRateFromFlag(srFlag)
            if(sr != 0) {
                block(sr, srFlag)
                srCount++
            }
        } // Else this is, e.g., just padding 0
    }
    return srCount
}


/**
 * @return DSD returned as negative value
 */
fun getSampleRateFromFlag(flag: Int): Int = when(flag) {
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
    INTERNAL_OUTPUT_FLAG_DSD64 -> -2822400 // => ffmpeg 352800
    INTERNAL_OUTPUT_FLAG_DSD128 -> -5644800
    INTERNAL_OUTPUT_FLAG_DSD256 -> -11289600
    INTERNAL_OUTPUT_FLAG_DSD512 -> -22579200
    INTERNAL_OUTPUT_FLAG_DSD1024 -> -45158400 // => ffmpeg 5644800
    else -> 0
}

fun getSampleRateFlag(sr: Int): Int = when(sr) {
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
    -2822400 -> INTERNAL_OUTPUT_FLAG_DSD64
    -5644800 -> INTERNAL_OUTPUT_FLAG_DSD128
    -11289600 -> INTERNAL_OUTPUT_FLAG_DSD256
    -22579200 -> INTERNAL_OUTPUT_FLAG_DSD512
    -45158400 -> INTERNAL_OUTPUT_FLAG_DSD1024
    else -> 0
}

/**
 * @return short name of the sample rate - xxK or DSDxx or "?" for unknown sample rate flag
 */
fun getShortSampleRateNameFromFlag(
    flag: Int,
    kHzLabel: String = "K",
    needSpaceBeforeKHz: Boolean = false,
    defaultString: String = "-"
): String
    = when(flag) {
    INTERNAL_OUTPUT_FLAG_SR_44K -> "44.1" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_48K -> "48" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_88K -> "88.2" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_96K -> "96" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_176K -> "176.4" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_192K -> "192" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_352K -> "352.8" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_384K -> "384" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_705K -> "705.6" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_SR_768K -> "768" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    INTERNAL_OUTPUT_FLAG_DSD64 -> "DSD64" // => ffmpeg 352800
    INTERNAL_OUTPUT_FLAG_DSD128 -> "DSD128"
    INTERNAL_OUTPUT_FLAG_DSD256 -> "DSD256"
    INTERNAL_OUTPUT_FLAG_DSD512 -> "DSD512"
    INTERNAL_OUTPUT_FLAG_DSD1024 -> "DSD1014" // => ffmpeg 5644800
    else -> defaultString
}

private fun getSampleRateInKhz(sr: Int, defaultLabel: String = "-"): String {
    if(sr <= 0) {
        return defaultLabel
    }
    val kHz = sr / 1000
    val fraction: Int = sr - kHz * 1000
    if(fraction < 100) {
        return kHz.toString()
    }
    return kHz.toString() + "." + fraction / 100
}

@JvmOverloads
fun getSampleRateAndLabel(
    sr: Int,
    kHzLabel: String = "K",
    needSpaceBeforeKHz: Boolean = false,
    defaultLabel: String = "-"
): String {
    val srFlag = getSampleRateFlag(sr)
    if(srFlag != 0) {
        // We're good - matched SR
        return getShortSampleRateNameFromFlag(srFlag, kHzLabel, needSpaceBeforeKHz, defaultLabel)
    }
    if(sr > 0) {
        return getSampleRateInKhz(sr) + "?" + if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel
    } else if(sr < 0) { // Unknown DSD
        return "DSD?(" + getSampleRateInKhz(-sr) + (if(needSpaceBeforeKHz) " $kHzLabel" else kHzLabel) + ")"
    } else {
        return defaultLabel
    }
}
