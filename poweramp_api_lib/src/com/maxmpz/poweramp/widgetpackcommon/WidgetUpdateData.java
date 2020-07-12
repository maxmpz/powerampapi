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

package com.maxmpz.poweramp.widgetpackcommon;

import android.graphics.Bitmap;
import android.util.Log;
import com.maxmpz.poweramp.player.PowerampAPI;

/**
 * The data required for widget update
 */
public class WidgetUpdateData {
	private static final String TAG = "WidgetUpdateData";
	private static final boolean LOG = false;

	public int apiVersion;

	public boolean hasTrack;
	public String title;
	public String album; // null for hide_unknown_album + unknown album
	public String artist;
	public boolean supportsCatNav;
	public int posInList;
	public int listSize;
	public int flags;

	public Bitmap albumArtBitmap;
	public long albumArtTimestamp;
	public boolean albumArtResolved;

	public boolean playing;

	public int shuffle = PowerampAPI.ShuffleMode.SHUFFLE_NONE;
	public int repeat = PowerampAPI.RepeatMode.REPEAT_NONE;

	public boolean albumArtNoAnim; // Used by widget configurator

	@Override
	public String toString() {
		return super.toString() + " hasTrack=" + hasTrack + " title=" + title + " album=" + album + " artist=" + artist + " supportsCatNav=" + supportsCatNav +
				" posInList=" + posInList + " listSize=" + listSize + " flags=0x" + Integer.toHexString(flags) + " albumArtBitmap=" + albumArtBitmap +
				" albumArtTimestamp=" + albumArtTimestamp + " playing=" + playing + " shuffle=" + shuffle + " repeat=" + repeat;
	}

	/**
	 * Resets textual track information, but not album art, as album art is generally independent from track info (==can be shared between different tracks).
	 * Same for repeat/shuffle, playing state
	 */
	public void resetTrackData() {
		if(LOG) Log.w(TAG, "resetTrackData", new Exception());
		hasTrack = false;
		title = album = artist = null;
		supportsCatNav = false;
		posInList = 0;
		listSize = 0;
		// NOTE: not resetting album art, repeat/shuffle, nor playing
	}
}