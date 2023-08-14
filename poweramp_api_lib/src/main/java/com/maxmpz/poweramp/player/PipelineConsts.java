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
	public static final int DSP_FORMAT = PaSampleFormat.PA_SAMPLE_FMT_DBL;
	/** The format used between pipeline plugins */
	public static final int PIPELINE_FORMAT = PaSampleFormat.PA_SAMPLE_FMT_FLT;

	// For getPipelineParamInt()
	public static final int SUBSYSTEM_PIPELINE   = 0;
	public static final int SUBSYSTEM_DSP_TH     = 1;
	public static final int SUBSYSTEM_DECODER_TH = 2;
	public static final int SUBSYSTEM_OUTPUT     = 3; // probably change to pipeline?

	// For queueMsg()
	public static final int PLUGIN_ID_SUBSYSTEM_PIPELINE   = 0;
	public static final int PLUGIN_ID_SUBSYSTEM_DECODER_TH = 1; // NOTE: sync with NativePluginManager.PLUGIN_ID_SUBSYSTEM_LAST
	public static final int PLUGIN_ID_SUBSYSTEM_DSP_TH     = 2;

	/*
	 * NOTE: raw native plugin value, not resolved vs Android audio subsystem
	 */
	public static final int PA_OUTPUT_PARAM_SAMPLE_RATE          = 1;  
	/*
	 * NOTE: raw native plugin value, not resolved vs Android audio subsystem
	 */
	public static final int PA_OUTPUT_PARAM_SAMPLE_BITS          = 2;
	public static final int PA_OUTPUT_PARAM_ANDROID_SESSION_ID   = 3;
	public static final int PA_OUTPUT_PARAM_RESTART_LATENCY_MS   = 4;
	public static final int PA_OUTPUT_PARAM_ANDROID_AUDIO_STREAM = 5;
	/** NOTE: not used, use DSP_TH_OUTPUT_FORMAT */
	public static final int PA_OUTPUT_PARAM_SAMPLE_FORMAT        = 6;
	public static final int PA_OUTPUT_PARAM_DEFAULT_MASTER_VOLUME_LEVELS = 20;
	
	public static final int PA_OUTPUT_PARAM_FIRST_CUSTOM         = 0x1000;

	// Internal
	public static final int PA_OUTPUT_PARAM_AUDIO_IO_HANDLE     = 0x80000001;

	// getPipelineParamInt() SUBSYSTEM_PIPELINE
	public static final int PARAM_LAST_DECODER_IX       = 1; // Never used ATM
	public static final int PARAM_LAST_DECODER_ID       = 2;

	// getPipelineParamInt() SUBSYSTEM_DSP_TH
	// NOTE: sync with dsp_threads.h
	public static final int DSP_TH_OUTPUT_ID              = 1;
	public static final int DSP_TH_PIPELINE_LATENCY       = 2;
	public static final int DSP_TH_OUTPUT_LATENCY         = 3;
	public static final int DSP_TH_OUTPUT_CAPS            = 4;
	/**
	 * This is active DSP and output sample rate, always corresponds to the negotiated sample rate, but not the device sample rate.<br>
	 * NOTE: we ALWAYS run DSP and output on the same sample rate (though format still can differ, as we always use float32 for DSP)
	 * */
	public static final int DSP_TH_AND_OUTPUT_SAMPLE_RATE = 5;
	/** This is active output format, always corresponds to the negotiated format, but it is not the end device format */
	public static final int DSP_TH_OUTPUT_FORMAT          = 6;
	public static final int DSP_TH_BUFFERS                = 7;
	public static final int DSP_TH_BUFFER_FRAMES          = 8;
	public static final int DSP_TH_OUTPUT_PRESENTATION_LATENCY = 9;

	// NOTE: sync with plugininterface-output.h
	public static final int PA_OUTPUT_CAP_ALWAYS_UNITY_GAIN        = 0x0010;
	public static final int PA_OUTPUT_CAP_NO_HEADROOM_GAIN         = 0x0020;
	public static final int PA_OUTPUT_CAP_NO_EQU                   = 0x0040;
	public static final int PA_OUTPUT_CAP_FLT_EXTENDED_DYN_RANGE   = 0x0080;
	public static final int PA_OUTPUT_CAP_PAUSE_FAST_VOLUME        = 0x0100;
	public static final int PA_OUTPUT_CAP_SEEK_FAST_VOLUME         = 0x0200;
	public static final int PA_OUTPUT_CAP_TRACK_CHANGE_FAST_VOLUME = 0x0400;
	public static final int PA_OUTPUT_CAP_NEEDS_VOLUME_PROVIDER    = 0x0800;
	public static final int PA_OUTPUT_CAP_CUSTOM_MASTER_VOLUME     = 0x1000;
	public static final int PA_OUTPUT_CAP_NO_DUCK                  = 0x2000;
	public static final int PA_OUTPUT_CAP_TRACK_PLAYBACK           = 0x4000;
	public static final int PA_OUTPUT_CAP_NEEDS_VOL_UI             = 0x8000;
	public static final int PA_OUTPUT_CAP_RAW                      = 0x0008;
	public static final int PA_OUTPUT_CAP_NO_MUSIC_STREAM_VOL      = 0x0004;
	public static final int PA_OUTPUT_CAP_PTS_UI                   = 0x0002;
	public static final int PA_OUTPUT_CAP_NO_AUDIO_FOCUS           = 0x100000;
	public static final int PA_OUTPUT_CAP_USE_STREAM3              = 0x200000;
	public static final int PA_OUTPUT_CAP_DELAYED_FORMAT           = 0x400000;
	// 18

	// NOTE: plugininterface-internal.h
	// NOTE: used for get_options() only
	public static final int PA_OUTPUT_CAP_FORCED_UNITY_GAIN      = 0x20000;
	public static final int PA_OUTPUT_CAP_OEM_VARIANT            = 0x40000; 	// Used for caps as well
	// 2
	
	public static final int UI_FLAG_NO_DVC_DUE_TO_BT_ABSVOL      = 0x0001;
	
	/** Volume update broadcast */
	public static int PA_BROADCAST_VOLUME       = 1;
	
	
	public static final int MSG_SET_NO_DVC_HEADROOM_MB   = 0x0009; // NOTE: immediately applies this. NOTE: public - can be used by java code


	public static final int FADE_NO_FADE = 0;
	public static final int FADE_IN_OUT = 1;
	public static final int FADE_CROSSFADE = 2;

	public static final int CROSSFADE_NO_FADE = 0;
	public static final int CROSSFADE_NO_GAPLESS = 1;
	public static final int CROSSFADE_ALL = 2;
	public static final int CROSSFADE_SHUFFLE = 3;
	public static final int CROSSFADE_MAX = 3;
}
