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

import android.content.Context

/** Generally means auto format  */
const val PA_SAMPLE_FMT_NONE: Int = -1

const val PA_SAMPLE_FMT_FIRST: Int = 0
/** unsigned 8 bits 0. We may consider this as non-set value and use it as AUTO */
const val PA_SAMPLE_FMT_U8: Int = 0
/** signed 16 bits */
const val PA_SAMPLE_FMT_S16: Int = 1
/** signed 32 bits */
const val PA_SAMPLE_FMT_S32: Int = 2
/** float (f32). Float32 */
const val PA_SAMPLE_FMT_FLT: Int = 3
/** double (f64). Float64 */
const val PA_SAMPLE_FMT_DBL: Int = 4
/** unsigned 8 bits, planar */
const val PA_SAMPLE_FMT_U8P: Int = 5
/** signed 16 bits, planar */
const val PA_SAMPLE_FMT_S16P: Int = 6
/** signed 32 bits, planar */
const val PA_SAMPLE_FMT_S32P: Int = 7
/** float, planar */
const val PA_SAMPLE_FMT_FLTP: Int = 8
/** double, planar */
const val PA_SAMPLE_FMT_DBLP: Int = 9
/** signed 64 bits (s64) */
const val PA_SAMPLE_FMT_S64: Int = 10
/** signed 64 bits, planar */
const val PA_SAMPLE_FMT_S64P: Int = 11

const val PA_SAMPLE_FMT_RESERVED_1: Int = 12
const val PA_SAMPLE_FMT_RESERVED_2: Int = 13
const val PA_SAMPLE_FMT_RESERVED_3: Int = 14
const val PA_SAMPLE_FMT_RESERVED_4: Int = 15
const val PA_SAMPLE_FMT_RESERVED_5: Int = 16
const val PA_SAMPLE_FMT_FIRST_RESERVED: Int = 12
const val PA_SAMPLE_FMT_LAST_RESERVED: Int = 16

/** S16 padded to S24 (subslot bytes==3). Left-justified (MSB-aligned). [16 bits audio][8 bits padding] */
const val PA_SAMPLE_FMT_S16_S24: Int = 17
/** S16 padded to S32 (subslot bytes==4). Left-justified (MSB-aligned). [16 bits audio][16 bits padding] */
const val PA_SAMPLE_FMT_S16_S32: Int = 18
/** S24 padded to S32 (subslot bytes==4). Left-justified (MSB-aligned). [24 bits audio][8 bits padding]. 1.0==0x7FFFFF00 */
const val PA_SAMPLE_FMT_S24_S32: Int = 19
/** packed 24bit. NOTE: we rely on S24 being > S32/S64, thus we'll select it last */
const val PA_SAMPLE_FMT_S24: Int = 20
/** Android Q8.23. Right-justified (LSB-aligned). [8 bits padding][24 bits audio] 1.0==0x007FFFFF */
const val PA_SAMPLE_FMT_S8_24: Int = 21

/** DOP DSD packed to 24 bits. Top 8 bits (MSB) alternate between 0x05 and 0xFA every sample, 16 LSB bits - actual data */
const val PA_SAMPLE_FMT_DSD_DOP_24: Int = 22
/** DOP DSD packed to 32 bits. Top 8 bits (MSB) alternate between 0x05 and 0xFA every sample, 16 LSB bits - actual data, 8 LSB 0-bits */
const val PA_SAMPLE_FMT_DSD_DOP_32: Int = 23
/** RAW DSD packed to 32 bits. BE, LSB */
const val PA_SAMPLE_FMT_DSD_RAW_32_LSB: Int = 24
/** RAW DSD packed to 32 bits. BE, MSB */
const val PA_SAMPLE_FMT_DSD_RAW_32_MSB: Int = 25
/** RAW DSD packed to 32 bits. LE, LSB */
const val PA_SAMPLE_FMT_DSD_RAW_32_LSB_LE: Int = 26
/** RAW DSD packed to 32 bits. LE, MSB */
const val PA_SAMPLE_FMT_DSD_RAW_32_MSB_LE: Int = 27
/** RAW DSD, 16-bit L/R interleave, 32-bit framing. BE, MSB == SNDRV_PCM_FORMAT_DSD_U16_BE */
const val PA_SAMPLE_FMT_DSD_RAW_16_32_MSB: Int = 28
/** RAW DSD, 16-bit L/R interleave, 32-bit framing. LE, MSB == SNDRV_PCM_FORMAT_DSD_U16_LE */
const val PA_SAMPLE_FMT_DSD_RAW_16_32_MSB_LE: Int = 29

const val PA_SAMPLE_FMT_LAST: Int = PA_SAMPLE_FMT_DSD_RAW_16_32_MSB_LE
/** This is NOT exactly the number of formats, as it includes reserved formats */
const val PA_SAMPLE_FMT_NB: Int = 30


const val PA_FLAG_FMT_NONE: Int = 0 // 0x0
const val PA_FLAG_FMT_U8: Int = 1 shl PA_SAMPLE_FMT_U8 // 0x1
const val PA_FLAG_FMT_S16: Int = 1 shl PA_SAMPLE_FMT_S16 // 0x2
const val PA_FLAG_FMT_S32: Int = 1 shl PA_SAMPLE_FMT_S32 // 0x4
const val PA_FLAG_FMT_FLT: Int = 1 shl PA_SAMPLE_FMT_FLT // 0x8
const val PA_FLAG_FMT_DBL: Int = 1 shl PA_SAMPLE_FMT_DBL // 0x10
const val PA_FLAG_FMT_U8P: Int = 1 shl PA_SAMPLE_FMT_U8P // 0x20
const val PA_FLAG_FMT_S16P: Int = 1 shl PA_SAMPLE_FMT_S16P // 0x40
const val PA_FLAG_FMT_S32P: Int = 1 shl PA_SAMPLE_FMT_S32P // 0x80
const val PA_FLAG_FMT_FLTP: Int = 1 shl PA_SAMPLE_FMT_FLTP // 0x100
const val PA_FLAG_FMT_DBLP: Int = 1 shl PA_SAMPLE_FMT_DBLP // 0x200
const val PA_FLAG_FMT_S64: Int = 1 shl PA_SAMPLE_FMT_S64 // 0x400
const val PA_FLAG_FMT_S64P: Int = 1 shl PA_SAMPLE_FMT_S64P // 0x800

const val PA_FLAG_FMT_S16_S24: Int = 1 shl PA_SAMPLE_FMT_S16_S24 // 0x20000
const val PA_FLAG_FMT_S16_S32: Int = 1 shl PA_SAMPLE_FMT_S16_S32 // 0x40000
const val PA_FLAG_FMT_S24_S32: Int = 1 shl PA_SAMPLE_FMT_S24_S32 // 0x80000

const val PA_FLAG_FMT_S24: Int = 1 shl PA_SAMPLE_FMT_S24 // 0x100000
const val PA_FLAG_FMT_S8_24: Int = 1 shl PA_SAMPLE_FMT_S8_24 // 0x200000
const val PA_FLAG_FMT_DSD_DOP_24: Int = 1 shl PA_SAMPLE_FMT_DSD_DOP_24 // 0x400000
const val PA_FLAG_FMT_DSD_DOP_32: Int = 1 shl PA_SAMPLE_FMT_DSD_DOP_32 // 0x800000
const val PA_FLAG_FMT_DSD_RAW_32_LSB: Int = 1 shl PA_SAMPLE_FMT_DSD_RAW_32_LSB // 0x1000000
const val PA_FLAG_FMT_DSD_RAW_32_MSB: Int = 1 shl PA_SAMPLE_FMT_DSD_RAW_32_MSB // 0x2000000
const val PA_FLAG_FMT_DSD_RAW_32_LSB_LE: Int = 1 shl PA_SAMPLE_FMT_DSD_RAW_32_LSB_LE // 0x4000000
const val PA_FLAG_FMT_DSD_RAW_32_MSB_LE: Int = 1 shl PA_SAMPLE_FMT_DSD_RAW_32_MSB_LE // 0x8000000
const val PA_FLAG_FMT_DSD_RAW_16_32_MSB: Int = 1 shl PA_SAMPLE_FMT_DSD_RAW_16_32_MSB // 0x10000000
const val PA_FLAG_FMT_DSD_RAW_16_32_MSB_LE: Int = 1 shl PA_SAMPLE_FMT_DSD_RAW_16_32_MSB_LE // 0x20000000


fun isValidFormat(format: Int, allowReserve: Boolean): Boolean {
    if (format < 0 || format >= PA_SAMPLE_FMT_NB) return false
    if (!allowReserve && format in PA_SAMPLE_FMT_FIRST_RESERVED..PA_SAMPLE_FMT_LAST_RESERVED) return false
    return true
}

/** This is storage bits per given sample  */
fun getBitsPerSample(sampleFormat: Int): Int {
    return getBytesPerSample(sampleFormat) * 8
}

/**
 * @return the most appropriate format for this bit width, or PA_SAMPLE_FMT_NONE otherwise.
 * Should be used for visual purposes only, as obviously we can't always guess the format from bits */
fun getSampleFmtForBits(sampleBits: Int): Int {
    return when(sampleBits) {
        1 -> PA_SAMPLE_FMT_DSD_RAW_32_MSB
        8 -> PA_SAMPLE_FMT_U8
        16 -> PA_SAMPLE_FMT_S16
        24 -> PA_SAMPLE_FMT_S24
        32 -> PA_SAMPLE_FMT_S32
        64 -> PA_SAMPLE_FMT_S64
        else -> PA_SAMPLE_FMT_NONE
    }
}

/** This is storage bits per given sample  */
fun getBytesPerSample(sampleFormat: Int): Int {
    return when (sampleFormat) {
        PA_SAMPLE_FMT_U8,
        PA_SAMPLE_FMT_U8P -> 1

        PA_SAMPLE_FMT_S16,
        PA_SAMPLE_FMT_S16P -> 2

        PA_SAMPLE_FMT_S24,
        PA_SAMPLE_FMT_DSD_DOP_24 -> 3

        PA_SAMPLE_FMT_S32,
        PA_SAMPLE_FMT_FLT,
        PA_SAMPLE_FMT_S32P,
        PA_SAMPLE_FMT_FLTP,
        PA_SAMPLE_FMT_S8_24,
        PA_SAMPLE_FMT_DSD_DOP_32,
        PA_SAMPLE_FMT_DSD_RAW_32_LSB,
        PA_SAMPLE_FMT_DSD_RAW_32_MSB,
        PA_SAMPLE_FMT_DSD_RAW_32_LSB_LE,
        PA_SAMPLE_FMT_DSD_RAW_32_MSB_LE,
        PA_SAMPLE_FMT_DSD_RAW_16_32_MSB,
        PA_SAMPLE_FMT_DSD_RAW_16_32_MSB_LE -> 4

        PA_SAMPLE_FMT_DBL,
        PA_SAMPLE_FMT_DBLP,
        PA_SAMPLE_FMT_S64,
        PA_SAMPLE_FMT_S64P -> 8

        else -> 0
    }
}

fun getAudibleBitsPerSample(sampleFormat: Int): Int {
    return when (sampleFormat) {
        PA_SAMPLE_FMT_DSD_DOP_24,
        PA_SAMPLE_FMT_DSD_DOP_32,
        PA_SAMPLE_FMT_DSD_RAW_32_LSB,
        PA_SAMPLE_FMT_DSD_RAW_32_MSB,
        PA_SAMPLE_FMT_DSD_RAW_32_LSB_LE,
        PA_SAMPLE_FMT_DSD_RAW_32_MSB_LE,
        PA_SAMPLE_FMT_DSD_RAW_16_32_MSB,
        PA_SAMPLE_FMT_DSD_RAW_16_32_MSB_LE -> 1

        PA_SAMPLE_FMT_U8,
        PA_SAMPLE_FMT_U8P -> 8

        PA_SAMPLE_FMT_S16,
        PA_SAMPLE_FMT_S16P -> 16

        PA_SAMPLE_FMT_FLT,
        PA_SAMPLE_FMT_FLTP,
        PA_SAMPLE_FMT_S8_24,
        PA_SAMPLE_FMT_S24 -> 24

        PA_SAMPLE_FMT_S32,
        PA_SAMPLE_FMT_S32P -> 32

        PA_SAMPLE_FMT_DBL,
        PA_SAMPLE_FMT_DBLP -> 53

        PA_SAMPLE_FMT_S64,
        PA_SAMPLE_FMT_S64P -> 64

        else -> 0
    }
}

/** This is significant range bits per given sample, i.e. 24 for Float32 or S8_24  */
fun getSignificantBitsPerSample(sampleFormat: Int): Int {
    return getAudibleBitsPerSample(sampleFormat)
}

/**
 * THREADING: any
 * @param defaultValue value for PA_SAMPLE_FMT_NONE, "-" is used if null and defaultIsNull=false
 * @param allowUnknownFormats if true, any unknown format will be reported, if false we return default value or "-"
 * @return bits based sample format name with "bit" suffix
 */
fun getSampleFormatLabel(
    sampleFmt: Int,
    defaultValue: String?,
    allowUnknownFormats: Boolean,
    bitLabel: String,
    unknownString: String
): String {
    return when(sampleFmt) {
        PA_SAMPLE_FMT_NONE -> defaultValue ?: "-"
        PA_SAMPLE_FMT_U8 -> "8 $bitLabel"
        PA_SAMPLE_FMT_U8P -> "8P $bitLabel"
        PA_SAMPLE_FMT_S16 -> "16 $bitLabel"
        PA_SAMPLE_FMT_S32 -> "32 $bitLabel"
        PA_SAMPLE_FMT_FLT -> "Float32"
        PA_SAMPLE_FMT_DBL -> "Float64"
        PA_SAMPLE_FMT_S16P -> "16P $bitLabel"
        PA_SAMPLE_FMT_S32P -> "32P $bitLabel"
        PA_SAMPLE_FMT_FLTP -> "Float32P"
        PA_SAMPLE_FMT_DBLP -> "Float64P"
        PA_SAMPLE_FMT_S64 -> "64 $bitLabel"
        PA_SAMPLE_FMT_S64P -> "64P $bitLabel"

        PA_SAMPLE_FMT_S24 -> "24 $bitLabel"
        PA_SAMPLE_FMT_S8_24 -> "32 (8.24) $bitLabel"

        // Padded formats
        PA_SAMPLE_FMT_S16_S24 -> "16/24 $bitLabel"
        PA_SAMPLE_FMT_S16_S32 -> "16/32 $bitLabel"
        PA_SAMPLE_FMT_S24_S32 -> "24/32 $bitLabel"

        // DSD formats
        PA_SAMPLE_FMT_DSD_DOP_24 -> "DSD DoP24"
        PA_SAMPLE_FMT_DSD_DOP_32 -> "DSD DoP32"
        PA_SAMPLE_FMT_DSD_RAW_32_LSB -> "DSD Raw LSB"
        PA_SAMPLE_FMT_DSD_RAW_32_MSB -> "DSD Raw MSB"
        PA_SAMPLE_FMT_DSD_RAW_32_LSB_LE -> "DSD Raw LSB LE"
        PA_SAMPLE_FMT_DSD_RAW_32_MSB_LE -> "DSD Raw MSB LE"
        PA_SAMPLE_FMT_DSD_RAW_16_32_MSB -> "DSD Raw MSB 16/32"
        PA_SAMPLE_FMT_DSD_RAW_16_32_MSB_LE -> "DSD Raw MSB 16/32 LE"

        else -> {
            if(allowUnknownFormats) "$unknownString ($sampleFmt)" else defaultValue ?: "-"
        }
    }
}

