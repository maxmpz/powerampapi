package com.maxmpz.poweramp.player;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class MetaTrackInfo {
	
	public static final @NonNull MetaTrackInfo EMPTY = new MetaTrackInfo(null, null, null);

	public final @Nullable String nextCategory; // Simple name for next category (e.g. album name) or null
	public final @Nullable String prevCategory; // Simple name for next category (e.g. album name) or null
	public final @Nullable String nextTrackInfo;
	
	public MetaTrackInfo(String nextTrackInfo, String prevCategory, String nextCategory) {
		this.nextTrackInfo = nextTrackInfo;
		this.nextCategory = nextCategory;
		this.prevCategory = prevCategory;
	}

}
