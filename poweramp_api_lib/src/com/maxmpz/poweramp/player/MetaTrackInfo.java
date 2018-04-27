package com.maxmpz.poweramp.player;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class MetaTrackInfo {
	
	//public static final @NonNull MetaTrackInfo EMPTY = new MetaTrackInfo(null, null, null); // NOTE: not used ATM

	public final @Nullable String nextTrackInfo;
	
	public MetaTrackInfo(String nextTrackInfo) {
		this.nextTrackInfo = nextTrackInfo;
	}

}
