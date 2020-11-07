package com.maxmpz.poweramp.equalizer;

import com.maxmpz.poweramp.player.PowerampAPI;

public final class PeqAPI {
	public static final String ACTION_ASK_FOR_DATA_PERMISSION = PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION;
	public static final String ACTION_RELOAD_DATA = PowerampAPI.ACTION_RELOAD_DATA;
	
	public static final String ACTION_API_COMMAND = PowerampAPI.ACTION_API_COMMAND;
	public static final String EXTRA_PACKAGE = PowerampAPI.EXTRA_PACKAGE;
	public static final String EXTRA_SOURCE = PowerampAPI.EXTRA_SOURCE;
	public static final String EXTRA_COMMAND = PowerampAPI.EXTRA_COMMAND;
	
	public static final class Commands {
		public static final int STOP_SERVICE = PowerampAPI.Commands.STOP_SERVICE;
	}

	public final static class MilkScanner extends PowerampAPI.MilkScanner {}
}
