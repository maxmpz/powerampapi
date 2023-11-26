/*
Copyright (C) 2011-2020 Maksim Petrov

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

import android.annotation.SuppressLint;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Some useful track provider related helper methods
 */
public class TrackProviderHelper {
	private static final String TAG = "TrackProviderHelper";
	private static final boolean LOG = false;

	/** Converts bytes to float array */
	public static float @NonNull[] bytesToFloats(byte @NonNull[] waveBytes) {
		if(waveBytes.length == 0) {
			return new float[0];
		}
		@SuppressLint("InlinedApi")
		int floatSize = waveBytes.length / Float.BYTES;

		float[] wave = new float[floatSize]; // Alloc
		ByteBuffer bb = ByteBuffer.wrap(waveBytes); // Allocs
		bb.asFloatBuffer().get(wave); // Alloc

		return wave;
	}

	/** Converts float array to bytes */
	public static byte @NonNull[] floatsToBytes(float @NonNull[] wave) {
		if(wave.length == 0) {
			return new byte[0];
		}
		@SuppressLint("InlinedApi")
		ByteBuffer bb = ByteBuffer.allocate(wave.length * Float.BYTES);
		FloatBuffer fb = bb.asFloatBuffer(); // Allocs
		fb.put(wave);
		return bb.array();
	}
}
