package com.maxmpz.poweramp.player;

/**
 * @see TableDefs.EqPresets#TYPE
 * @noinspection InterfaceNeverImplemented
 */
public interface EqPresetConsts {
	/** Preset type not set */
	public static final int TYPE_UNKNOWN = -1;

	/** User created or default user preset */
	public static final int TYPE_USER = 0;

	/** Built-in preset */
	public static final int TYPE_BUILT_IN = 10;

	/** AutoEq preset */
	public static final int TYPE_AUTO_EQ = 20;
}
