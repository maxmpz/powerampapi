package com.maxmpz.poweramp.player

/** Generally means auto format  */
const val PA_SAMPLE_FMT_NONE: Int = -1

/** unsigned 8 bits. As it matches 0, it generally means Auto/none currently  */
const val PA_SAMPLE_FMT_U8: Int = 0
const val PA_SAMPLE_FMT_S16: Int = 1

/**< signed 16 bits */
const val PA_SAMPLE_FMT_S32: Int = 2

/**< signed 32 bits */
const val PA_SAMPLE_FMT_FLT: Int = 3

/**< float */
const val PA_SAMPLE_FMT_DBL: Int = 4

/**< double */
const val PA_SAMPLE_FMT_U8P: Int = 5

/**< unsigned 8 bits, planar */
const val PA_SAMPLE_FMT_S16P: Int = 6

/**< signed 16 bits, planar */
const val PA_SAMPLE_FMT_S32P: Int = 7

/**< signed 32 bits, planar */
const val PA_SAMPLE_FMT_FLTP: Int = 8

/**< float, planar */
const val PA_SAMPLE_FMT_DBLP: Int = 9

/**< double, planar */
const val PA_SAMPLE_FMT_S64: Int = 10

/**< signed 64 bits */
const val PA_SAMPLE_FMT_S64P: Int = 11

/**< signed 64 bits, planar */ // 12..19 are reserved, but still valid values
const val PA_SAMPLE_FMT_S24: Int = 20 // packed 24bit
const val PA_SAMPLE_FMT_S8_24: Int = 21 // Android Q8.23

// 14
/** NOTE: this indicates max sample format + 1, as we have reserved values  */
const val PA_SAMPLE_FMT_NB: Int = 22


// Sync with output-internal.h
// This requires separate output param
const val INTERNAL_OUTPUT_FLAG_FMT_U8: Int = 1 shl PA_SAMPLE_FMT_U8
const val INTERNAL_OUTPUT_FLAG_FMT_S16: Int = 1 shl PA_SAMPLE_FMT_S16
const val INTERNAL_OUTPUT_FLAG_FMT_S32: Int = 1 shl PA_SAMPLE_FMT_S32
const val INTERNAL_OUTPUT_FLAG_FMT_FLT: Int = 1 shl PA_SAMPLE_FMT_FLT
const val INTERNAL_OUTPUT_FLAG_FMT_DBL: Int = 1 shl PA_SAMPLE_FMT_DBL
const val INTERNAL_OUTPUT_FLAG_FMT_U8P: Int = 1 shl PA_SAMPLE_FMT_U8P
const val INTERNAL_OUTPUT_FLAG_FMT_S16P: Int = 1 shl PA_SAMPLE_FMT_S16P
const val INTERNAL_OUTPUT_FLAG_FMT_S32P: Int = 1 shl PA_SAMPLE_FMT_S32P
const val INTERNAL_OUTPUT_FLAG_FMT_FLTP: Int = 1 shl PA_SAMPLE_FMT_FLTP
const val INTERNAL_OUTPUT_FLAG_FMT_DBLP: Int = 1 shl PA_SAMPLE_FMT_DBLP
const val INTERNAL_OUTPUT_FLAG_FMT_S64: Int = 1 shl PA_SAMPLE_FMT_S64
const val INTERNAL_OUTPUT_FLAG_FMT_S64P: Int = 1 shl PA_SAMPLE_FMT_S64P
const val INTERNAL_OUTPUT_FLAG_FMT_S24: Int = 1 shl PA_SAMPLE_FMT_S24
const val INTERNAL_OUTPUT_FLAG_FMT_S8_24: Int = 1 shl PA_SAMPLE_FMT_S8_24


fun isValidFormat(format: Int, allowReserve: Boolean): Boolean {
    if(format < 0) return false
    if(format >= PA_SAMPLE_FMT_NB) return false
    if(!allowReserve && format > PA_SAMPLE_FMT_S64P && format < PA_SAMPLE_FMT_S24) return false
    return true
}

/** This is storage bits per given sample  */
fun getBitsPerSample(sampleFormat: Int): Int {
    when(sampleFormat) {
        PA_SAMPLE_FMT_U8 -> return 8
        PA_SAMPLE_FMT_S16 -> return 16
        PA_SAMPLE_FMT_S32 -> return 32
        PA_SAMPLE_FMT_FLT -> return 32
        PA_SAMPLE_FMT_DBL -> return 64
        PA_SAMPLE_FMT_U8P -> return 8
        PA_SAMPLE_FMT_S16P -> return 16
        PA_SAMPLE_FMT_S32P -> return 32
        PA_SAMPLE_FMT_FLTP -> return 32
        PA_SAMPLE_FMT_DBLP -> return 64
        PA_SAMPLE_FMT_S24 -> return 24
        PA_SAMPLE_FMT_S8_24 -> return 32
        PA_SAMPLE_FMT_S64, PA_SAMPLE_FMT_S64P -> return 64
        PA_SAMPLE_FMT_NONE -> return 0
        else -> return 0
    }
}

/** @return the most appropriate format for this bit width, or PA_SAMPLE_FMT_NONE otherwise */
fun getSampleFmtForBits(sampleBits: Int): Int {
    when(sampleBits) {
        8 -> return PA_SAMPLE_FMT_U8
        16 -> return PA_SAMPLE_FMT_S16
        24 -> return PA_SAMPLE_FMT_S24
        32 -> return PA_SAMPLE_FMT_S32
        64 -> return PA_SAMPLE_FMT_S64
        else -> return PA_SAMPLE_FMT_NONE
    }
}

/** This is storage bits per given sample  */
fun getBytesPerSample(sampleFormat: Int): Int {
    return getBitsPerSample(sampleFormat) / 8
}

/** This is significant range bits per given sample, i.e. 24 for Float32 or S8_24  */
fun getSignificantBitsPerSample(sampleFormat: Int): Int {
    when(sampleFormat) {
        PA_SAMPLE_FMT_NONE -> return 0
        PA_SAMPLE_FMT_U8 -> return 8
        PA_SAMPLE_FMT_S16 -> return 16
        PA_SAMPLE_FMT_S32 -> return 32
        PA_SAMPLE_FMT_FLT -> return 24
        PA_SAMPLE_FMT_FLTP -> return 24
        PA_SAMPLE_FMT_DBL -> return 53
        PA_SAMPLE_FMT_U8P -> return 8
        PA_SAMPLE_FMT_S16P -> return 16
        PA_SAMPLE_FMT_S32P -> return 32
        PA_SAMPLE_FMT_DBLP -> return 53
        PA_SAMPLE_FMT_S24 -> return 24
        PA_SAMPLE_FMT_S8_24 -> return 24 // NOTE: not sure about this, actually this is closer to normal float, thus this is 24 bit
        PA_SAMPLE_FMT_S64, PA_SAMPLE_FMT_S64P -> return 53
        else -> return 0
    }
}
