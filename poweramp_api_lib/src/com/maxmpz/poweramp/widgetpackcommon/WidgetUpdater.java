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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import com.maxmpz.poweramp.player.PowerampAPI;


public class WidgetUpdater {
	private static final String TAG = "WidgetUpdater";
	private static final boolean LOG = false;
	
	private static boolean sUpdatedOnce;
	
	public static final IntentFilter sTrackFilter = new IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED);
	public static final IntentFilter sAAFilter = new IntentFilter(PowerampAPI.ACTION_AA_CHANGED);
	public static final IntentFilter sStatusFilter = new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED);
	public static final IntentFilter sModeFilter = new IntentFilter(PowerampAPI.ACTION_PLAYING_MODE_CHANGED);
	
	public static final int THROTTLE_DELAY = 250; // 2013-03-05: should match ExternalAPI.NO_AA_DELAY

	private static final int MSG_UPDATE = 1;

	private static boolean sMediaRemoved;
	private static @Nullable SharedPreferences sCachedPrefs; 
	
	protected boolean mIsDestroyed;
	private PowerManager mPowerManager;
	
	protected final @NonNull Object mLock = new Object();;
	
	protected List<IWidgetUpdater> mProviders = new ArrayList<>();
	
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message m) {
			switch(m.what) {
				case MSG_UPDATE:
					updateSafe((Intent)m.obj, false, m.arg1 == 1);
					break;
			}
		};
	};
	private Context mContext;

	public WidgetUpdater(Context context) {
		mPowerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		mContext = context;
	}
	
	public void destroy() {
		synchronized(mLock) {
			mIsDestroyed = true;
			mHandler.removeCallbacksAndMessages(null);
		}
		if(LOG) Log.w(TAG, "onDestroy done");
	}
	
	protected void updateThrottled(Intent intent, boolean updateByOs) { 
		mHandler.removeCallbacksAndMessages(null);
		// Poweramp usually sends TRACK_CHANGE/STATUS_CHANGE - delay - AA_CHANGE, thus, don't throttle the AA_CHANGE.
		int delay;
		final String action = intent.getAction();
		if(PowerampAPI.ACTION_AA_CHANGED.equals(action) || PowerampAPI.ACTION_PLAYING_MODE_CHANGED.equals(action) || PowerampAPI.ACTION_STATUS_CHANGED.equals(action)) {
			delay = 0;
		} else {
			delay = THROTTLE_DELAY; // We actually wait just for AA_CHANGE here.
		}

		if(LOG) Log.w(TAG, ">>updateThrottled intent=" + intent + " delay=" + delay); 
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE, updateByOs ? 1 : 0, 0, intent), delay);
	}
	
	/**
	 * Retrieves sticky intents and updates the widget.
	 */
	// Also called by PlayerService/ExternalAPI on ExternalAPI thread. Sync'ed to avoid reentrance by onStartCommand and ExternalAPI.
	// THREADING: any
	public void updateSafe(@Nullable Intent intent, boolean ignorePowerState, boolean updateByOs) {
		if(LOG) Log.w(TAG, "updateSafe=" + intent + " th=" + Thread.currentThread()); // + " extras=" + intent == null ? null : Arrays.toString(intent.getExtras().keySet().toArray(new String[]{})));
		
		synchronized(mLock) {
			
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
	
			if(intent != null && updateByOs/*intent.getBooleanExtra(EXTRA_UPDATE_BY_OS, false)*/) {
				// Check if media is removed. In this case PowerampAPI status can be in stale "playing" state (as Poweramp process could be killed by ripper).
				sMediaRemoved = !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
				// If update by OS, some Androids 2.x require new AA to be set again.
			}
			
			WidgetUpdateData data = BaseWidgetProvider.generateUpdateData(mContext, sMediaRemoved);

			if(LOG) Log.w(TAG, "========== updateSafe UPDATE => " + intent);
			
			pushUpdateCore(data, ids);
		}
		
		if(LOG) Log.w(TAG, "update done ");
	}

	private void pushUpdateCore(@NonNull WidgetUpdateData data, int[] ids) {
		SharedPreferences prefs = getCachedSharedPreferences(mContext);
		
		for(IWidgetUpdater prov : mProviders) {
			prov.pushUpdate(mContext, prefs, ids, sMediaRemoved, data);
		}
		
		if(data.hasTrack && !sUpdatedOnce) {
			if(LOG) Log.w(TAG, "pushUpdateCore sUpdatedOnce=>true");
			sUpdatedOnce = true;
		}
	}

	public void updateDirectSafe(@NonNull WidgetUpdateData data, boolean ignorePowerState) {
		if(LOG) Log.w(TAG, "updateDirectSafe data=" + data + " th=" + Thread.currentThread(), new Exception()); // + " extras=" + intent == null ? null : Arrays.toString(intent.getExtras().keySet().toArray(new String[]{})));
		
		synchronized(mLock) {
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
	
			if(LOG) Log.w(TAG, "========== updateDirectSafe UPDATE => " + data);
			
			pushUpdateCore(data, ids);
		}
		
		if(LOG) Log.w(TAG, "update done ");
	}

	// NOTE: specifically not synchronized as Context.getSharedPreferences() is thread safe and synchronized, so if we get contested here, we just get same preferences
	// from context 2 times
	// THREADING: any
	public static SharedPreferences getCachedSharedPreferences(Context context) {
		if(sCachedPrefs == null) {
			// REVISIT: try to get preferences from Application itself
			Context app = context.getApplicationContext();
			sCachedPrefs = app.getSharedPreferences(getGlobalSharedPreferencesName(app), 0);
		}
		return sCachedPrefs;
	}
	
	public static String getGlobalSharedPreferencesName(Context context) {
		return context.getPackageName() + "_appwidgets";
	}
}
