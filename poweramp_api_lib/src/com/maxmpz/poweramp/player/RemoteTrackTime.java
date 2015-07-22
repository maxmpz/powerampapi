/*
Copyright (C) 2011-2013 Maksim Petrov

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

/*
 * This class tracks Poweramp in-song position via as few sync intents as possible.
 * Syncing happens when:
 * - your app calls registerAndLoadStatus() (for example, in activity onResume).
 * - when Poweramp seeks the track (throttled to 500ms)
 * - when track is started/resumed/paused
 */
public class RemoteTrackTime {
	private static final String TAG = "RemoteTrackTime";
	private static final boolean LOG = false; // Make it false for production.

	private static int UPDATE_DELAY = 1000;
	
	private Context mContext;
	//private int mDuration;
	private int mPosition;
	
	private long mStartTimeMs;
	private int mStartPosition;
	private boolean mPlaying;
	
	private Handler mHandler = new Handler();
	
	
	public interface TrackTimeListener {
		public void onTrackDurationChanged(int duration);
		public void onTrackPositionChanged(int position);
	}
	
	private TrackTimeListener mTrackTimeListener;
	
	
	public RemoteTrackTime(Context context) {
		mContext = context;
	}
	
	public void registerAndLoadStatus() {
		IntentFilter filter = new IntentFilter(PowerampAPI.ACTION_TRACK_POS_SYNC);
		mContext.registerReceiver(mTrackPosSyncReceiver, filter);
		mContext.startService(PowerampAPI.newAPIIntent().putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.POS_SYNC));		
		if(mPlaying) {
			mHandler.removeCallbacks(mTickRunnable);
			mHandler.postDelayed(mTickRunnable, 0);
		}
	}
	
	public void unregister() {
		if(mTrackPosSyncReceiver != null) {
			try {
				mContext.unregisterReceiver(mTrackPosSyncReceiver);
			} catch(Exception ex){}
		}
		mHandler.removeCallbacks(mTickRunnable);
	}
	
	private BroadcastReceiver mTrackPosSyncReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int pos = intent.getIntExtra(PowerampAPI.Track.POSITION, 0);
			if(LOG) Log.w(TAG, "mTrackPosSyncReceiver sync=" + pos);
			updateTrackPosition(pos);
		}
		
	};
	
	public void setTrackTimeListener(TrackTimeListener l) {
		mTrackTimeListener = l;
	}

	public void updateTrackDuration(int duration) {
		//mDuration = duration;
		if(mTrackTimeListener != null) {
			mTrackTimeListener.onTrackDurationChanged(duration);
		}
	}

	public void updateTrackPosition(int position) {
		mPosition = position;
		if(mPlaying) {
			mStartTimeMs = System.currentTimeMillis();
			mStartPosition = mPosition;
		}
		if(mTrackTimeListener != null) {
			mTrackTimeListener.onTrackPositionChanged(position);
		}
	}
	
	private Runnable mTickRunnable = new Runnable() {
		public void run() {
			mPosition = (int)(System.currentTimeMillis() - mStartTimeMs + 500) / 1000 + mStartPosition; 
			if(LOG) Log.w(TAG, "mTickRunnable mPosition=" + mPosition);
			if(mTrackTimeListener != null) {
				mTrackTimeListener.onTrackPositionChanged(mPosition);
			}
			mHandler.removeCallbacks(mTickRunnable);
			mHandler.postDelayed(mTickRunnable, UPDATE_DELAY);
		}
	};
	
	public void startSongProgress() {
		if(!mPlaying) {
			mStartTimeMs = System.currentTimeMillis();
			mStartPosition = mPosition;
			mHandler.removeCallbacks(mTickRunnable);
			mHandler.postDelayed(mTickRunnable, UPDATE_DELAY);
			mPlaying = true;
		}
	}
	
	public void stopSongProgress() {
		if(mPlaying) {
			mHandler.removeCallbacks(mTickRunnable);
			mPlaying = false;
		}
	}
}
