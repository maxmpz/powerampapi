/*
Copyright (C) 2011-2013 Maksim Petrov

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

package com.maxmpz.poweramp.widgetpackcommon;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

@SuppressLint("HandlerLeak")
public abstract class BaseWidgetUpdaterService extends Service {
	private static final String TAG = "BaseWidgetUpdaterService";
	private static final boolean LOG = false;
	
	public static final String EXTRA_UPDATE_BY_OS = "updateByOs";

	private static boolean sNewTrackPending;
	private static boolean sUpdatedOnce;
	
	public static final IntentFilter sTrackFilter = new IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED);
	public static final IntentFilter sAAFilter = new IntentFilter(PowerampAPI.ACTION_AA_CHANGED);
	public static final IntentFilter sStatusFilter = new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED);
	public static final IntentFilter sModeFilter = new IntentFilter(PowerampAPI.ACTION_PLAYING_MODE_CHANGED);
	
	private static final int STOP_SELF_DELAY = 10000; // Quit after idle for this time. Should be large enough to avoid multiple service starts/exits when user skips many tracks.
	private static final int INITIAL_DELAY = 2100;
	public static final int THROTTLE_DELAY = 250; // 2013-03-05: should match ExternalAPI.NO_AA_DELAY

	private static final int MSG_STOP_SELF = 0;
	private static final int MSG_UPDATE = 1;

	private static boolean sMediaRemoved;
	private static SharedPreferences sCachedPrefs; // Access this only from gui thread.
	
	private LocalBinder mBinder = new LocalBinder(this);
	private boolean mIsDestroyed;
	private PowerManager mPowerManager;
	private boolean mBound;
	
	
	protected List<BaseWidgetProvider> mProviders = new ArrayList<BaseWidgetProvider>();
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if(!mIsDestroyed) {
				switch(m.what) {
					case MSG_STOP_SELF:
						if(LOG) Log.w(TAG, "MSG_STOP_SELF stopSelf()");
						stopSelf();
						break;
	
					case MSG_UPDATE:
						update((Intent)m.obj, false);
						break;
				}
			}
		};
	};

	
	public static class LocalBinder extends Binder {
		BaseWidgetUpdaterService mService;
		
		public LocalBinder(BaseWidgetUpdaterService service) {
			mService = service;
		}
		
		public BaseWidgetUpdaterService getService() {
			return mService; 
		}
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		if(LOG) Log.w(TAG, "onBind, service=" + this + " mBound=" + mBound);

		mBound = true;
		
		restartIdleTimer();
		
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		if(LOG) Log.w(TAG, "onUnbind mBound=" + mBound);
		mBound = false;
		restartIdleTimer();
		
		return false;
	}
	

	@Override
	public void onCreate() {
		super.onCreate();
		if(LOG) Log.w(TAG, "onCreate");
		mPowerManager = (PowerManager)getSystemService(POWER_SERVICE);
		
		restartIdleTimer();
	}
	
	/**
	 * Checks for incoming intent and either throttles PowerampAPI intent, or updates widget (in case of MEDIA_REMOVED).
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) { // Called on UI thread.
		if(LOG) Log.w(TAG, "onStartCommand intent=" + intent);
		mHandler.removeCallbacksAndMessages(null);
		
		if(intent != null) {
			mHandler.removeCallbacksAndMessages(null);
			sMediaRemoved = false;
			String action = intent.getAction();
			if(Intent.ACTION_MEDIA_REMOVED.equals(action)) {
				if(LOG) Log.e(TAG, "onStartCommand => ACTION_MEDIA_REMOVED");
				sMediaRemoved = true;
				update(intent, false);
			} else if(intent.getBooleanExtra(EXTRA_UPDATE_BY_OS, false)) { // 2013-03-04: immediately update if requested by OS
				if(LOG) Log.e(TAG, "onStartCommand => updateByOs=true");
				update(intent, false);
			} else if(action != null) {
				if(LOG) Log.e(TAG, "onStartCommand => updateThrottled");
				updateThrottled(intent);
			} else { // Delay for initial delay.
				if(LOG) Log.e(TAG, "onStartCommand => INITIAL_DELAY");
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE, intent), INITIAL_DELAY); // TODO: revisit. This happens when service is bound/started by PlayerService
			}
		}
		
		restartIdleTimer();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		mIsDestroyed = true;
		mHandler.removeCallbacksAndMessages(null);
		
		super.onDestroy();
		
		if(LOG) Log.w(TAG, "onDestroy done");
	}


	/**
	 * Starts/restarts service idle timer.
	 */
	private void restartIdleTimer() {
		mHandler.removeMessages(MSG_STOP_SELF);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STOP_SELF), STOP_SELF_DELAY);
	}
	
	public synchronized void updateThrottled(Intent intent) { // Also called by PlayerService/ExternalAPI on ExternalAPI thread. Sync'ed to avoid reentrance by onStartCommand and ExternalAPI.
		mHandler.removeCallbacksAndMessages(null);
		// Poweramp usually sends TRACK_CHANGE/STATUS_CHANGE - delay - AA_CHANGE, thus, don't throttle the AA_CHANGE.
		int delay;
		final String action = intent.getAction();
		if(PowerampAPI.ACTION_AA_CHANGED.equals(action) || PowerampAPI.ACTION_PLAYING_MODE_CHANGED.equals(action) || PowerampAPI.ACTION_STATUS_CHANGED.equals(action)) {
			delay = 0;
		} else {
			delay = THROTTLE_DELAY; // We actually wait just for AA_CHANGE here.
		}

		if(LOG) Log.e(TAG, ">>updateThrottled intent=" + intent + " delay=" + delay); 
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE, intent), delay);
	}
	
	/**
	 * Retrieves sticky intents and updates the widget.
	 */
	public void update(Intent intent, boolean ignorePowerState) { // Always called on GUI thread.
		if(LOG) Log.w(TAG, "update=" + intent); // + " extras=" + intent == null ? null : Arrays.toString(intent.getExtras().keySet().toArray(new String[]{})));
		
		if(mIsDestroyed) {
			if(LOG) Log.w(TAG, "mIsDestroyed!");
			return;
		}
		
		mHandler.removeMessages(MSG_UPDATE);

		if(!ignorePowerState && !mPowerManager.isScreenOn() && sUpdatedOnce){ 
			if(LOG) Log.e(TAG, "skipping update, screen is off");
			return;
		}
		
		int[] ids = null;
		boolean updateByOs = false;
		

		if(intent != null && intent.getBooleanExtra(EXTRA_UPDATE_BY_OS, false)) {
			// Check if media is removed. In this case PowerampAPI status can be in stale "playing" state (as Poweramp process could be killed by ripper).
			sMediaRemoved = !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
			// If update by OS, some Androids 2.x require new AA to be set again.
			updateByOs = true;
		}
		
		if(LOG) Log.e(TAG, "========== pushing UPDATE => " + intent + " sNewTrackPending=" + sNewTrackPending + " updateByOs=" + updateByOs);
		
		SharedPreferences prefs = getCachedSharedPreferences(this);
		
		WidgetUpdateData data = null;
		for(BaseWidgetProvider prov : mProviders) {
			prov.pushUpdate(this, prefs, ids, sMediaRemoved, sNewTrackPending, updateByOs, data);
		}
		
		if(data != null && data.track != null && !sUpdatedOnce) {
			if(LOG) Log.w(TAG, "sUpdatedOnce");
			sUpdatedOnce = true;
		}
		
		sNewTrackPending = false;
		
		if(LOG) Log.w(TAG, "update done ");
	}

	public synchronized static SharedPreferences getCachedSharedPreferences(Context context) {
		if(sCachedPrefs == null) {
			sCachedPrefs = getGlobalSharedPreferences(context.getApplicationContext());
		}
		return sCachedPrefs;
	}
	
	public static String getGlobalSharedPreferencesName(Context context) {
		return context.getPackageName() + "_appwidgets";
	}
	
	@SuppressLint({ "WorldWriteableFiles", "WorldReadableFiles" })
	private static SharedPreferences getGlobalSharedPreferences(Context context) {
        return context.getSharedPreferences(getGlobalSharedPreferencesName(context), Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
	}
}
