package com.maxmpz.poweramp.player;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.eclipse.jdt.annotation.NonNull;

public class TrackProviderHelper {
	private static final String TAG = "TrackProviderHelper";
	private static final boolean LOG = false;

	/** Converts bytes to float array */
	public static @NonNull float[] bytesToFloats(@NonNull byte[] waveBytes) {
		if(waveBytes.length == 0) {
			return new float[0];
		}
		int floatSize = waveBytes.length / Float.BYTES;

		float[] wave = new float[floatSize]; // Alloc
		ByteBuffer bb = ByteBuffer.wrap(waveBytes); // Allocs
		bb.asFloatBuffer().get(wave); // Alloc

		return wave;
	}

	/** Converts float array to bytes */
	@SuppressWarnings("null")
	public static @NonNull byte[] floatsToBytes(@NonNull float[] wave) {
		if(wave.length == 0) {
			return new byte[0];
		}
		ByteBuffer bb = ByteBuffer.allocate(wave.length * Float.BYTES);
		FloatBuffer fb = bb.asFloatBuffer(); // Allocs
		fb.put(wave);
		return bb.array();
	}
}
