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
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;


public abstract class WidgetUpdater {
	private static final String TAG = "WidgetUpdater";
	private static final boolean LOG = false;

	/**
	 * If true, loadDefaultOrPersistantUpdateData call is used to generate widget update data in case of system calls to update widget.
	 * loadDefaultOrPersistantUpdateData should be able to retrieve all the data needed + album art
	 */
	private static final boolean ALWAYS_USE_PERSISTANT_DATA = true;
	/**
	 * NOTE: as of v3 betas, no album art event is sent anymore
	 */
	private static final boolean USE_AA_EVENT = false;

	private static boolean sUpdatedOnce;

	public static final IntentFilter sTrackFilter = new IntentFilter(PowerampAPI.ACTION_TRACK_CHANGED);
	public static final IntentFilter sAAFilter = USE_AA_EVENT ? new IntentFilter(PowerampAPI.ACTION_AA_CHANGED) : null;
	public static final IntentFilter sStatusFilter = new IntentFilter(PowerampAPI.ACTION_STATUS_CHANGED);
	public static final IntentFilter sModeFilter = new IntentFilter(PowerampAPI.ACTION_PLAYING_MODE_CHANGED);

	private final Context mContext;

	//private static boolean sMediaRemoved; // REVISIT: never true, remove
	private static @Nullable SharedPreferences sCachedPrefs;

	private final @NonNull PowerManager mPowerManager;

	protected final @NonNull Object mLock = new Object();
	protected final @NonNull List<IWidgetUpdater> mProviders = new ArrayList<>(4);

	/**
	 * Used by PS to push updates, usually all providers added in constructor of the derived class
	 */
	public WidgetUpdater(Context context) {
		long start;
		if(LOG) start = System.nanoTime();
		
		PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		if(powerManager == null) throw new AssertionError();
		mPowerManager = powerManager;

		mContext = context;
		
		if(LOG) Log.w(TAG, "ctor in=" + (System.nanoTime() - start) / 1000);
	} 

	/**
	 * Per-single provider ctor, used for cases when provider is called by system
	 */
	public WidgetUpdater(Context context, @NonNull BaseWidgetProvider prov) {
		this(context);

		synchronized(mLock) {
			mProviders.add(prov);
		}
	}

	/**
	 * Called during system onUpdate() call which requires remote views for widget due to some system event (boot, etc.)
	 * Called just for given provider 
	 */
	// THREADING: any
	public void updateSafe(@NonNull BaseWidgetProvider provider, boolean ignorePowerState, boolean updateByOs, int[] appWidgetIds) {
		if(LOG) Log.w(TAG, "updateSafe th=" + Thread.currentThread() + " provider=" + provider); 

		synchronized(mLock) {
			if(!ignorePowerState && !mPowerManager.isScreenOn() && sUpdatedOnce){
				if(LOG) Log.e(TAG, "skipping update, screen is off");
				return;
			}

//			int[] ids = appWidgetIds;
//			if(intent != null && updateByOs/*intent.getBooleanExtra(EXTRA_UPDATE_BY_OS, false)*/) {
//				// Check if media is removed. In this case PowerampAPI status can be in stale "playing" state (as Poweramp process could be killed by ripper).
//				sMediaRemoved = !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
//				// If update by OS, some Androids 2.x require new AA to be set again.
//			}

			WidgetUpdateData data = generateUpdateData(mContext/*, sMediaRemoved*/);

			if(LOG) Log.w(TAG, "========== updateSafe UPDATE data=" + data);

			pushUpdateCore(data, appWidgetIds);
		}

		if(LOG) Log.w(TAG, "update done ");
	}

	private void pushUpdateCore(@NonNull WidgetUpdateData data, int[] ids) {
		if(LOG) Log.w(TAG, "pushUpdateCore data=" + data + " ids=" + Arrays.toString(ids) + " mProviders.length=" + mProviders.size());

		SharedPreferences prefs = getCachedSharedPreferences(mContext);

		for(IWidgetUpdater prov : mProviders) {
			prov.pushUpdate(mContext, prefs, ids, false, data); // Media never removed, not changing signature for now
		}

		if(data.hasTrack && !sUpdatedOnce) {
			if(LOG) Log.w(TAG, "pushUpdateCore sUpdatedOnce=>true");
			sUpdatedOnce = true;
		}
	}

	/**
	 * Called by ExternalAPI
	 * @param data
	 * @param ignorePowerState
	 * @return true if update happened, false if power state doesn't allow update now
	 */
	public boolean updateDirectSafe(@NonNull WidgetUpdateData data, boolean ignorePowerState) {
		synchronized(mLock) {
			if(!ignorePowerState && !mPowerManager.isScreenOn() && sUpdatedOnce){
				if(LOG) Log.e(TAG, "updateDirectSafe skipping update, screen is off");
				return false;
			}

			if(LOG) Log.w(TAG, "updateDirectSafe data=" + data + " th=" + Thread.currentThread()); // + " extras=" + intent == null ? null : Arrays.toString(intent.getExtras().keySet().toArray(new String[]{})));

			pushUpdateCore(data, null);
		}

		if(LOG) Log.w(TAG, "update done ");

		return true;
	}

	// NOTE: specifically not synchronized as Context.getSharedPreferences() is thread safe and synchronized, so if we get contested here, we just get same preferences
	// from context 2 times
	// THREADING: any
	@SuppressWarnings("null")
	public static @NonNull SharedPreferences getCachedSharedPreferences(Context context) {
		SharedPreferences cachedPrefs = sCachedPrefs;
		if(cachedPrefs == null) {
			// REVISIT: try to get preferences from Application itself
			Context app = context.getApplicationContext();
			cachedPrefs = sCachedPrefs = app.getSharedPreferences(getGlobalSharedPreferencesName(app), 0);
		}
		return cachedPrefs;
	}

	public static String getGlobalSharedPreferencesName(Context context) {
		return context.getPackageName() + "_appwidgets";
	}


	/**
	 * Called when generateUpdateData is not able to find any sticky intents (e.g. after reboot), so default or previously stored data should be retrieved
	 * @param context
	 * @param data
	 */
	protected abstract void loadDefaultOrPersistantUpdateData(Context context, @NonNull WidgetUpdateData data);

	/**
	 * Generates WidgetUpdateData from sticky intents
	 * @param context
	 * @param mediaRemoved
	 * @return
	 */
	// Data should be always the same for any type of widgets as data is reused by other widgets, thus method is final.
	public @NonNull WidgetUpdateData generateUpdateData(Context context/*, boolean mediaRemoved*/) {
		WidgetUpdateData data = new WidgetUpdateData();

		if(ALWAYS_USE_PERSISTANT_DATA) {
			// Still check for actual playing status, as persistent data is stored per track change, thus never reflects playing state
			// Do it before loadDefaultOrPersistantUpdateData
			getPlayingState(context, data);

			loadDefaultOrPersistantUpdateData(context, data);

			return data;
		}

		Bundle track = null;

		Intent trackIntent = context.registerReceiver(null, WidgetUpdater.sTrackFilter);

		if(LOG) Log.w(TAG, "generateUpdateData trackIntent=" + trackIntent);


		if(trackIntent != null) {
			track = trackIntent.getParcelableExtra(PowerampAPI.TRACK);

			if(track != null) {
				data.hasTrack = true;
				data.title = track.getString(PowerampAPI.Track.TITLE);
				data.album = track.getString(PowerampAPI.Track.ALBUM);
				data.artist = track.getString(PowerampAPI.Track.ARTIST);
				data.listSize = track.getInt(PowerampAPI.Track.LIST_SIZE);
				data.posInList = track.getInt(PowerampAPI.Track.POS_IN_LIST);
				data.supportsCatNav = track.getBoolean(PowerampAPI.Track.SUPPORTS_CAT_NAV);
				data.flags = track.getInt(PowerampAPI.Track.FLAGS);
				if(LOG) Log.w(TAG, "received trackIntent data=" + data);

			} else {
				loadDefaultOrPersistantUpdateData(context, data);
				return data;
			}
		} else {
			// No any intent stored, need to get some defaults or previously saved persistent data 
			loadDefaultOrPersistantUpdateData(context, data);
			return data;
		}

		// NOTE: as of v3 betas, no album art event is sent anymore
		if(USE_AA_EVENT) {
			Intent aaIntent = context.registerReceiver(null, WidgetUpdater.sAAFilter);
			if(aaIntent != null) {
				try {
					data.albumArtBitmap = PowerampAPIHelper.getAlbumArt(context, track, 512, 512);
					if(LOG) Log.w(TAG, "generateUpdateData got aa=" + data.albumArtBitmap);
					data.albumArtTimestamp = aaIntent.getLongExtra(PowerampAPI.TIMESTAMP, 0);
					if(LOG) Log.w(TAG, "received AA TIMESTAMP=" + data.albumArtTimestamp);
				} catch(OutOfMemoryError oom) {
					Log.e(TAG, "", oom);
				}
			}
		}

		getPlayingState(context, data);

		Intent modeIntent = context.registerReceiver(null, WidgetUpdater.sModeFilter);
		if(modeIntent != null) {
			data.shuffle = modeIntent.getIntExtra(PowerampAPI.SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_NONE);
			data.repeat = modeIntent.getIntExtra(PowerampAPI.REPEAT, PowerampAPI.RepeatMode.REPEAT_NONE);
			if(LOG) Log.w(TAG, "repeat=" + data.repeat + " shuffle=" + data.shuffle);
		}
		return data;
	}

	@SuppressWarnings("static-method")
	private void getPlayingState(Context context, @NonNull WidgetUpdateData data) {
		Intent statusIntent = context.registerReceiver(null, WidgetUpdater.sStatusFilter);
		if(statusIntent != null) {

			boolean paused = statusIntent.getBooleanExtra(PowerampAPI.PAUSED, true);
			data.playing = !paused;

			data.apiVersion = statusIntent.getIntExtra(PowerampAPI.API_VERSION, 0);

			if(LOG) Log.w(TAG, "getPlayingState statusIntent=" + statusIntent + " paused=" + paused + " playing=" + data.playing);
		} else if(LOG)  Log.e(TAG, "getPlayingState statusIntent==null");
	}
}
