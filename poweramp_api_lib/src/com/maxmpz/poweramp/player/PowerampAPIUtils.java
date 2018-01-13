/*
Copyright (C) 2011-2018 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with PowerAMP application on Android platform.

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

public class PowerampAPIUtils {
	private static final int PACKED_PLAYING_MODE_MASK          = 0x000F;
	private static final int PACKED_PLAYING_MODE_SHUFFLE_SHIFT = 4;
	
	public static String getReadable(String title, String unknown) { // REVISIT: is this used?
		if(title != null && title.length() > 0) {
			return title;
		}
		return unknown;
	}
	
	/**
	 * Unpacks shuffle mode from packed repeat and shuffle integer
	 * @param packedRepeatShuffle
	 * @return
	 */
	public static int unpackShuffle(int packedRepeatShuffle) {
		return packedRepeatShuffle & PACKED_PLAYING_MODE_MASK;
	}
	
	/**
	 * Unpacks repeat mode from packed repeat and shuffle integer
	 * @param packedRepeatShuffle
	 * @return
	 */
	public static int unpackRepeat(int packedRepeatShuffle) {
		return (packedRepeatShuffle >> PACKED_PLAYING_MODE_SHUFFLE_SHIFT) & PACKED_PLAYING_MODE_MASK;
	}
	
	/**
	 * Packs both repeat and shuffle into one integer
	 * @param repeat
	 * @param shuffle
	 * @return
	 */
	public static int packRepeatShuffle(int repeat, int shuffle) {
		return (repeat & PACKED_PLAYING_MODE_MASK) | (shuffle & PACKED_PLAYING_MODE_MASK) << PACKED_PLAYING_MODE_SHUFFLE_SHIFT;
	}
}
