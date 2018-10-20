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

public interface RouterConsts {
	// Sync with output-internal.h
	public static final int DEVICE_HEADSET = 0;
	public static final int DEVICE_SPEAKER = 1;
	public static final int DEVICE_BT      = 2;
	public static final int DEVICE_USB     = 3;
	public static final int DEVICE_OTHER   = 4;
	// 5

	public static final int DEVICE_UNKNOWN = 0xFF;

	public static final int DEVICE_MASK    = 0xFF; // 256 devices

	public static final int DEVICE_COUNT   = 5;
	public static final int DEVICE_SAFE_DEFAULT = DEVICE_HEADSET;

	public static final String DEVICE_NAME_HEADSET = "headset";
	public static final String DEVICE_NAME_SPEAKER = "speaker";
	public static final String DEVICE_NAME_BT = "bt";
	public static final String DEVICE_NAME_USB = "usb";
	public static final String DEVICE_NAME_OTHER = "other";
}
