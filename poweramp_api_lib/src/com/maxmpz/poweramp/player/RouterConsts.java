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
