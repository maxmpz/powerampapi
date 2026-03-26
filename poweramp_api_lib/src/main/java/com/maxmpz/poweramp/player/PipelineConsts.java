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

package com.maxmpz.poweramp.player;


public interface PipelineConsts {
	/** The format used internally in DSP kernels */
	int DSP_FORMAT = PaSampleFormat.PA_SAMPLE_FMT_DBL;
	/** The format used between pipeline plugins */
	int PIPELINE_FORMAT = PaSampleFormat.PA_SAMPLE_FMT_FLT;

	// For queueMsg()
	int PLUGIN_ID_SUBSYSTEM_PIPELINE   = 0;
	int PLUGIN_ID_SUBSYSTEM_DECODER_TH = 1; // NOTE: sync with NativePluginManager.PLUGIN_ID_SUBSYSTEM_LAST
	int PLUGIN_ID_SUBSYSTEM_DSP_TH     = 2;

	// NOTE: these should be matching the subsystem vals
	// REVISIT: may use single subsystem consts namespace, e.g. PLUGIN_ID_SUBSYSTEM_* for params as well
	// For getPipelineParamInt()
	int PARAM_SUBSYSTEM_PIPELINE   = PLUGIN_ID_SUBSYSTEM_PIPELINE; // 0
	int PARAM_SUBSYSTEM_DECODER_TH = PLUGIN_ID_SUBSYSTEM_DECODER_TH; // 1. Unused ATM
	int PARAM_SUBSYSTEM_DSP_TH     = PLUGIN_ID_SUBSYSTEM_DSP_TH;
	int PARAM_SUBSYSTEM_OUTPUT     = 3;


	/*
	 * NOTE: raw native plugin value, not resolved vs Android audio subsystem
	 */
	int PA_OUTPUT_PARAM_SAMPLE_RATE          = 1;
	/*
	 * NOTE: raw native plugin value, not resolved vs Android audio subsystem. REVISIT: unused, drop?
	 */
	int PA_OUTPUT_PARAM_SAMPLE_BITS          = 2;
	int PA_OUTPUT_PARAM_ANDROID_SESSION_ID   = 3;
	int PA_OUTPUT_PARAM_RESTART_LATENCY_MS   = 4;
	@Deprecated
	int PA_OUTPUT_PARAM_ANDROID_AUDIO_STREAM = 5;
	/** NOTE: not used, use DSP_TH_OUTPUT_FORMAT */
	int PA_OUTPUT_PARAM_SAMPLE_FORMAT        = 6;
	int PA_OUTPUT_PARAM_DEFAULT_MASTER_VOLUME_LEVELS = 20;
	
	int PA_OUTPUT_PARAM_FIRST_CUSTOM         = 0x1000;

	// Internal
	int PA_OUTPUT_PARAM_AUDIO_IO_HANDLE     = 0x80000001;

	// getPipelineParamInt() SUBSYSTEM_PIPELINE
	int PARAM_LAST_DECODER_IX       = 1; // Never used ATM
	int PARAM_LAST_DECODER_ID       = 2;
	int PARAM_FATAL_ERROR           = 3;

	// getPipelineParamInt() SUBSYSTEM_DSP_TH
	// NOTE: sync with dsp_threads.h
	int DSP_TH_OUTPUT_ID              = 1;
	int DSP_TH_PIPELINE_LATENCY       = 2;
	int DSP_TH_OUTPUT_LATENCY         = 3;
	int DSP_TH_OUTPUT_CAPS            = 4;
	/**
	 * This is active DSP and output sample rate, always corresponds to the negotiated sample rate, but not the device sample rate.<br>
	 * NOTE: we ALWAYS run DSP and output on the same sample rate (though format still can differ, as we always use float32 for DSP)
	 * */
	int DSP_TH_AND_OUTPUT_SAMPLE_RATE = 5;
	/** This is active output format, always corresponds to the negotiated format, but it is not the end device format */
	int DSP_TH_OUTPUT_FORMAT          = 6;
	int DSP_TH_BUFFERS                = 7;
	int DSP_TH_BUFFER_FRAMES          = 8;
	int DSP_TH_OUTPUT_PRESENTATION_LATENCY = 9;

	// NOTE: sync with plugininterface-output.h
	int PA_OUTPUT_CAP_INITED                   = 0x0001;
	int PA_OUTPUT_CAP_PTS_UI                   = 0x0002;
	int PA_OUTPUT_CAP_NO_MUSIC_STREAM_VOL      = 0x0004;
	int PA_OUTPUT_CAP_RAW                      = 0x0008;
	int PA_OUTPUT_CAP_ALWAYS_UNITY_GAIN        = 0x0010;
	int PA_OUTPUT_CAP_NO_HEADROOM_GAIN         = 0x0020;
	int PA_OUTPUT_CAP_NO_EQU                   = 0x0040;
	int PA_OUTPUT_CAP_FLT_EXTENDED_DYN_RANGE   = 0x0080;
	int PA_OUTPUT_CAP_PAUSE_FAST_VOLUME        = 0x0100;
	int PA_OUTPUT_CAP_SEEK_FAST_VOLUME         = 0x0200;
	int PA_OUTPUT_CAP_TRACK_CHANGE_FAST_VOLUME = 0x0400;
	int PA_OUTPUT_CAP_NEEDS_VOLUME_PROVIDER    = 0x0800;
	int PA_OUTPUT_CAP_CUSTOM_MASTER_VOLUME     = 0x1000;
	int PA_OUTPUT_CAP_NO_DUCK                  = 0x2000;
	int PA_OUTPUT_CAP_TRACK_PLAYBACK           = 0x4000;
	int PA_OUTPUT_CAP_NEEDS_VOL_UI             = 0x8000;
	int PA_OUTPUT_CAP_NO_AUDIO_FOCUS           = 0x100000;
	int PA_OUTPUT_CAP_USE_STREAM3              = 0x200000;
	int PA_OUTPUT_CAP_DELAYED_FORMAT           = 0x400000;
	int PA_OUTPUT_CAP_FOLLOW_SR                = 0x800000;
	int PA_OUTPUT_CAP_DSD                      = 0x1000000;
	int PA_OUTPUT_CAP_PIPELINE64               = 0x2000000;
	int PA_OUTPUT_CAP_DSD_REMASTER             = 0x4000000;
	int PA_OUTPUT_CAP_PERFECTBITPERFECT        = 0x8000000;

	// NOTE: plugininterface-internal.h
	// NOTE: used for get_options() only
	int PA_OUTPUT_CAP_FORCED_UNITY_GAIN      = 0x20000;
	/** Basically gives extra flag in settings called OEM Variant. Used for Lenovo only */
	int PA_OUTPUT_CAP_OEM_VARIANT            = 0x40000; 	// Used for caps as well
	// 2

	int UI_FLAG_NO_DVC_DUE_TO_BT_ABSVOL      = 0x0001;
	
	/** Volume update broadcast */
	int PA_BROADCAST_VOLUME       = 1;

	int MSG_SET_NO_DVC_HEADROOM_MB   = 0x0009; // NOTE: immediately applies this. NOTE: public - can be used by java code

	int FADE_NO_FADE = 0;
	int FADE_IN_OUT = 1;
	int FADE_CROSSFADE = 2;

	int CROSSFADE_NO_FADE = 0;
	int CROSSFADE_NO_GAPLESS = 1;
	int CROSSFADE_ALL = 2;
	int CROSSFADE_SHUFFLE = 3;
	int CROSSFADE_MAX = 3;

	/** Used for cmd_player_reload_pipeline arg1 */
	int ARG1_FLAG_RECONFIGURE = 0x1000;
}
