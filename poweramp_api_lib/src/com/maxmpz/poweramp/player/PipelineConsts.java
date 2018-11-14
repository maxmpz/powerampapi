/*
Copyright (C) 2011-2018 Maksim Petrov

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

	// For getPipelineParamInt()
	public static final int SUBSYSTEM_PIPELINE   = 0;
	public static final int SUBSYSTEM_DSP_TH     = 1;
	public static final int SUBSYSTEM_DECODER_TH = 2;
	public static final int SUBSYSTEM_OUTPUT     = 3; // probably change to pipeline?

	// For queueMsg()
	public static final int PLUGIN_ID_SUBSYSTEM_PIPELINE   = 0;
	public static final int PLUGIN_ID_SUBSYSTEM_DECODER_TH = 1; // NOTE: sync with NativePlguinManager.PLUGIN_ID_SUBSYSTEM_LAST
	public static final int PLUGIN_ID_SUBSYSTEM_DSP_TH     = 2;

	public static final int PA_OUTPUT_PARAM_SAMPLE_RATE         = 1; // NOTE: requires deviceId arg, otherwise returns for 0 == HEADSET
	public static final int PA_OUTPUT_PARAM_SAMPLE_BITS         = 2; // NOTE: requires deviceId arg, otherwise returns for 0 == HEADSET
	public static final int PA_OUTPUT_PARAM_ANDROID_SESSION_ID  = 3;
	public static final int PA_OUTPUT_PARAM_RESTART_LATENCY_MS  = 4;
	public static final int PA_OUTPUT_PARAM_ANDROID_AUDIO_STREAM = 5;

	// Internal
	public static final int PA_OUTPUT_PARAM_AUDIO_IO_HANDLE     = 0x80000001; // NOTE: supported by atoutput only

	// getPipelineParamInt() SUBSYSTEM_PIPELINE
	public static final int PARAM_LAST_DECODER_IX       = 1; // Never used ATM
	public static final int PARAM_LAST_DECODER_ID       = 2;

	// getPipelineParamInt() SUBSYSTEM_DSP_TH
	// NOTE: sync with dsp_threads.h
	public static final int DSP_TH_OUTPUT_ID            = 1;
	public static final int DSP_TH_PIPELINE_LATENCY     = 2;
	public static final int DSP_TH_OUTPUT_LATENCY       = 3;
	public static final int DSP_TH_OUTPUT_CAPS          = 4;
	public static final int DSP_TH_SAMPLE_RATE          = 5;
	public static final int DSP_TH_OUTPUT_FORMAT        = 6; // The output format from features (e.g. the target format, not actual output device format)
	public static final int DSP_TH_BUFFERS              = 7;
	public static final int DSP_TH_BUFFER_FRAMES        = 8;


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

	// NOTE: plugininterface-internal.h
	// NOTE: used for get_options() only
	public static final int PA_OUTPUT_CAP_FORCED_UNITY_GAIN      = 0x20000;
	public static final int PA_OUTPUT_CAP_OEM_VARIANT            = 0x40000; 	// Used for caps as well
}
