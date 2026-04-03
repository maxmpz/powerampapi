/*
Copyright (C) 2011-2022 Maksim Petrov

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


object PipelineConsts {
    /** The format used internally in DSP kernels  */
    const val DSP_FORMAT: Int = PaSampleFormat.PA_SAMPLE_FMT_DBL

    /** The format used between pipeline plugins  */
    const val PIPELINE_FORMAT: Int = PaSampleFormat.PA_SAMPLE_FMT_FLT

    const val SAMPLE_RATE_AUTO: Int = 0
    /** Used on java side only, never sent to the native side. Native receives 0 */
    const val SAMPLE_RATE_FOLLOW_SR = -2
    /** Used on java side only, never sent to the native side. Native receives either 0 or the available codec SR */
    const val SAMPLE_RATE_CODEC: Int = -3

    /** Used on java side only, never sent to the native side. Native receives either NONE or the available codec fmt */
    const val FORMAT_CODEC: Int = -3

    // For queueMsg()
    const val PLUGIN_ID_SUBSYSTEM_PIPELINE: Int = 0
    const val PLUGIN_ID_SUBSYSTEM_DECODER_TH: Int = 1 // NOTE: sync with NativePluginManager.PLUGIN_ID_SUBSYSTEM_LAST
    const val PLUGIN_ID_SUBSYSTEM_DSP_TH: Int = 2

    // NOTE: these should be matching the subsystem vals
    // REVISIT: may use single subsystem consts namespace, e.g. PLUGIN_ID_SUBSYSTEM_* for params as well
    // For getPipelineParamInt()
    const val PARAM_SUBSYSTEM_PIPELINE: Int = PLUGIN_ID_SUBSYSTEM_PIPELINE // 0
    const val PARAM_SUBSYSTEM_DECODER_TH: Int = PLUGIN_ID_SUBSYSTEM_DECODER_TH // 1. Unused ATM
    const val PARAM_SUBSYSTEM_DSP_TH: Int = PLUGIN_ID_SUBSYSTEM_DSP_TH
    const val PARAM_SUBSYSTEM_OUTPUT: Int = 3


    /** NOTE: raw native plugin value, not resolved vs Android audio subsystem */
    const val PA_OUTPUT_PARAM_SAMPLE_RATE: Int = 1

    /** NOTE: raw native plugin value, not resolved vs Android audio subsystem. REVISIT: unused, drop? */
    const val PA_OUTPUT_PARAM_SAMPLE_BITS: Int = 2
    const val PA_OUTPUT_PARAM_ANDROID_SESSION_ID: Int = 3
    const val PA_OUTPUT_PARAM_RESTART_LATENCY_MS: Int = 4

    @Deprecated("")
    const val PA_OUTPUT_PARAM_ANDROID_AUDIO_STREAM: Int = 5

    /** NOTE: not used, use DSP_TH_OUTPUT_FORMAT  */
    const val PA_OUTPUT_PARAM_SAMPLE_FORMAT: Int = 6
    const val PA_OUTPUT_PARAM_DEFAULT_MASTER_VOLUME_LEVELS: Int = 20

    const val PA_OUTPUT_PARAM_FIRST_CUSTOM: Int = 0x1000

    // Internal
    const val PA_OUTPUT_PARAM_AUDIO_IO_HANDLE: Int = -0x7fffffff

    // getPipelineParamInt() SUBSYSTEM_PIPELINE
    const val PARAM_LAST_DECODER_IX: Int = 1 // Never used ATM
    const val PARAM_LAST_DECODER_ID: Int = 2
    const val PARAM_FATAL_ERROR: Int = 3

    // getPipelineParamInt() SUBSYSTEM_DSP_TH
    // NOTE: sync with dsp_threads.h
    const val DSP_TH_OUTPUT_ID: Int = 1
    const val DSP_TH_PIPELINE_LATENCY: Int = 2
    const val DSP_TH_OUTPUT_LATENCY: Int = 3
    const val DSP_TH_OUTPUT_CAPS: Int = 4

    /**
     * This is active DSP and output sample rate, always corresponds to the negotiated sample rate, but not the device sample rate.<br></br>
     * NOTE: we ALWAYS run DSP and output on the same sample rate (though format still can differ, as we always use float32 for DSP)
     */
    const val DSP_TH_AND_OUTPUT_SAMPLE_RATE: Int = 5

    /** This is active output format, always corresponds to the negotiated format, but it is not the end device format  */
    const val DSP_TH_OUTPUT_FORMAT: Int = 6
    const val DSP_TH_BUFFERS: Int = 7
    const val DSP_TH_BUFFER_FRAMES: Int = 8
    const val DSP_TH_OUTPUT_PRESENTATION_LATENCY: Int = 9

    // NOTE: sync with plugininterface-output.h
    const val PA_OUTPUT_CAP_INITED: Int = 0x0001
    const val PA_OUTPUT_CAP_PTS_UI: Int = 0x0002
    const val PA_OUTPUT_CAP_NO_MUSIC_STREAM_VOL: Int = 0x0004
    const val PA_OUTPUT_CAP_RAW: Int = 0x0008
    const val PA_OUTPUT_CAP_ALWAYS_UNITY_GAIN: Int = 0x0010
    const val PA_OUTPUT_CAP_NO_HEADROOM_GAIN: Int = 0x0020
    const val PA_OUTPUT_CAP_NO_EQU: Int = 0x0040
    const val PA_OUTPUT_CAP_FLT_EXTENDED_DYN_RANGE: Int = 0x0080
    const val PA_OUTPUT_CAP_PAUSE_FAST_VOLUME: Int = 0x0100
    const val PA_OUTPUT_CAP_SEEK_FAST_VOLUME: Int = 0x0200
    const val PA_OUTPUT_CAP_TRACK_CHANGE_FAST_VOLUME: Int = 0x0400
    const val PA_OUTPUT_CAP_NEEDS_VOLUME_PROVIDER: Int = 0x0800
    const val PA_OUTPUT_CAP_CUSTOM_MASTER_VOLUME: Int = 0x1000
    const val PA_OUTPUT_CAP_NO_DUCK: Int = 0x2000
    const val PA_OUTPUT_CAP_TRACK_PLAYBACK: Int = 0x4000
    const val PA_OUTPUT_CAP_NEEDS_VOL_UI: Int = 0x8000
    const val PA_OUTPUT_CAP_NO_AUDIO_FOCUS: Int = 0x100000
    const val PA_OUTPUT_CAP_USE_STREAM3: Int = 0x200000
    const val PA_OUTPUT_CAP_DELAYED_FORMAT: Int = 0x400000
    const val PA_OUTPUT_CAP_FOLLOW_SR: Int = 0x800000
    const val PA_OUTPUT_CAP_DSD: Int = 0x1000000
    const val PA_OUTPUT_CAP_PIPELINE64: Int = 0x2000000
    const val PA_OUTPUT_CAP_DSD_REMASTER: Int = 0x4000000
    const val PA_OUTPUT_CAP_PERFECTBITPERFECT: Int = 0x8000000
    const val PA_OUTPUT_CAP_FOLLOW_BT_CODEC: Int = 0x10000000

    // NOTE: plugininterface-internal.h
    // NOTE: used for get_options() only
    const val PA_OUTPUT_CAP_FORCED_UNITY_GAIN: Int = 0x20000

    /** Basically gives extra flag in settings called OEM Variant. Used for Lenovo only  */
    const val PA_OUTPUT_CAP_OEM_VARIANT: Int = 0x40000 // Used for caps as well

    /**
     * This is added to OutputOpts.uiFlags IF we know we requested DVC, but disabled it due to absvol
     * This is used not just by UI, but also by PSPipelineManager to detect requested but missing DVC
     * */
    const val OPTS_RESOLVE_FLAG_NO_DVC_DUE_TO_BT_ABSVOL: Int = 0x0001

    /** Volume update broadcast  */
    const val PA_BROADCAST_VOLUME: Int = 1

    const val FADE_NO_FADE: Int = 0
    const val FADE_IN_OUT: Int = 1
    const val FADE_CROSSFADE: Int = 2

    const val CROSSFADE_NO_FADE: Int = 0
    const val CROSSFADE_NO_GAPLESS: Int = 1
    const val CROSSFADE_ALL: Int = 2
    const val CROSSFADE_SHUFFLE: Int = 3
    const val CROSSFADE_MAX: Int = 3

    /** Used for cmd_player_reload_pipeline arg1  */
    const val ARG1_FLAG_RECONFIGURE: Int = 0x1000
}
