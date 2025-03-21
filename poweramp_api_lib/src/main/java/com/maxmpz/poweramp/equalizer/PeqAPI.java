package com.maxmpz.poweramp.equalizer;

import com.maxmpz.poweramp.player.PowerampAPI;

public final class PeqAPI {
	public static final String ACTION_ASK_FOR_DATA_PERMISSION = PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION;
	public static final String ACTION_RELOAD_DATA = PowerampAPI.ACTION_RELOAD_DATA;
	
	public static final String ACTION_API_COMMAND = "com.maxmpz.equalizer.API_COMMAND";
	public static final String EXTRA_PACKAGE = PowerampAPI.EXTRA_PACKAGE;
	public static final String EXTRA_SOURCE = PowerampAPI.EXTRA_SOURCE;
	public static final String EXTRA_COMMAND = PowerampAPI.EXTRA_COMMAND;
	

	/**
	 * API broadcast receiver name
	 */
	public static final String API_RECEIVER_NAME = "com.maxmpz.equalizer.PeqAPIReceiver";

	/**
	 * Action which can be sent to PeqAPI activity to import set of presets.<br>
	 * NOTE: only PeqAPI activity accepts this action, as appropriate import UI should be shown, which is not always possible for broadcast receiver<br> 
	 * Extras:<br>
	 * - {@link #EXTRA_NAMES} - array of preset names (>0). Length of this array should match {@link #EXTRA_PRESETS}<br>
	 * - {@link #EXTRA_PRESETS} - array of preset data (>0). Length of this array should match {@link #EXTRA_NAMES}<br>
	 * - {@link #EXTRA_PACKAGE} - package name of your app<br>
	 * 
	 */
	public static final String ACTION_IMPORT_PRESETS = "com.maxmpz.equalizer.IMPORT_PRESETS";

	/**
	 * String array
	 */
	public static final String EXTRA_NAMES = "names";
	
	/**
	 * String array
	 */
	public static final String EXTRA_PRESETS = "presets";
	
	public static final class Commands {
		public static final int STOP_SERVICE = PowerampAPI.Commands.STOP_SERVICE;

		public static final int SET_EQU_PRESET = PowerampAPI.Commands.SET_EQU_PRESET;
	}

	/** @see PowerampAPI#CALL_SET_PREFERENCE */
	public static final String CALL_SET_PREFERENCE = PowerampAPI.CALL_SET_PREFERENCE;

	public final static class MilkScanner extends PowerampAPI.MilkScanner {}
	
	
	/**
	 * Settings related actions
	 */
	public static class Settings extends PowerampAPI.Settings {
		/**
		 * Settings activity
		 */
		public static final String ACTIVITY_SETTINGS = "com.maxmpz.equalizer.preference.SettingsActivity";
	}
}
