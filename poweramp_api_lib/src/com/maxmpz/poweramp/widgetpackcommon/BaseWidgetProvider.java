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

import java.util.Arrays;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.maxmpz.poweramp.player.PowerampAPI;

/**
 * Base widget provider for PowerampAPI based app widgets.
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider implements IWidgetUpdater {
	private static final String TAG = "BaseWidgetProvider";
	private static final boolean LOG = false;
	
	public static final int WIDGET_PACK_PREFS_VERSION = 209;
	
	public static final int API_VERSION_200 = 200;
	
	public static boolean IS_HTC_SENSE; // NOTE: this should be set by Application class.
	private static boolean sPingedPowerampService;
	
	public static class WidgetContext {
		public long lastAATimeStamp;
		public int id;
	}
	
	public static final class ShuffleModeV140 {
		public static final int SHUFFLE_NONE = 0;
		public static final int SHUFFLE_ALL = 1;
		public static final int SHUFFLE_IN_CAT = 2;
		public static final int SHUFFLE_HIER = 3;
	}
	
	public static final class RepeatModeV140 {
		public static final int REPEAT_NONE = 0;
		public static final int REPEAT_ALL = 1;
		public static final int REPEAT_SONG = 2;
		public static final int REPEAT_CAT = 3;
	}

	protected static final int MIN_WIDTH = 100;
	protected static final int MIN_HEIGHT = 100;
	protected static ComponentName sServiceName;
	
	protected abstract ComponentName getWidgetUpdaterServiceName(Context context);
	
	public abstract RemoteViews update(Context context, WidgetUpdateData data, SharedPreferences prefs, int id);
	
	
	// Data should be always the same for any type of widgets as data is reused by other widgets, thus method is final.
	public final WidgetUpdateData generateUpdateData(Context context, boolean mediaRemoved, boolean trackChanged, boolean updateByOs) {
		if(LOG) Log.e(TAG, "generateUpdateData me=" + this);
		
		WidgetUpdateData data = new WidgetUpdateData();
		
		data.updateByOs = updateByOs;
		data.trackChanged = trackChanged;

		Intent trackIntent = context.registerReceiver(null, BaseWidgetUpdaterService.sTrackFilter);
		if(trackIntent != null) {
			data.track = trackIntent.getParcelableExtra(PowerampAPI.TRACK);
			if(LOG) Log.e(TAG, "received trackIntent");
		} else if(!sPingedPowerampService) {
			if(LOG) Log.e(TAG, "no trackIntent, pinging Poweramp service");
			context.startService(PowerampAPI.newAPIIntent());
			sPingedPowerampService = true;
		}

		Intent aaIntent = context.registerReceiver(null, BaseWidgetUpdaterService.sAAFilter);
		if(aaIntent != null) {
			try {
				data.albumArtBitmap = aaIntent.getParcelableExtra(PowerampAPI.ALBUM_ART_BITMAP);
				data.albumArtPath = aaIntent.getStringExtra(PowerampAPI.ALBUM_ART_PATH);
				data.albumArtTimestamp = aaIntent.getLongExtra(PowerampAPI.TIMESTAMP, 0);
				if(LOG) Log.e(TAG, "received AA TIMESTAMP=" + data.albumArtTimestamp);
			} catch(OutOfMemoryError oom) {
				Log.e(TAG, "", oom);
			}
		}

		if(mediaRemoved) {
			data.playing = false;
			if(LOG)  Log.e(TAG, "generateUpdateData mediaRemoved");
		} else {
			Intent statusIntent = context.registerReceiver(null, BaseWidgetUpdaterService.sStatusFilter);
			if(statusIntent != null) {
			
				int status = statusIntent.getIntExtra(PowerampAPI.STATUS, 0);
				boolean paused = statusIntent.getBooleanExtra(PowerampAPI.PAUSED, true);
				data.playing = (status == PowerampAPI.Status.TRACK_PLAYING) && !paused;
				
				data.apiVersion = statusIntent.getIntExtra(PowerampAPI.API_VERSION, 0);
				
				if(LOG) Log.e(TAG, "generateUpdateData statusIntent=" + statusIntent + " paused=" + paused + " playing=" + data.playing + " status=" + status);				
			} else if(LOG)  Log.e(TAG, "generateUpdateData statusIntent==null");
		}
		
		Intent modeIntent = context.registerReceiver(null, BaseWidgetUpdaterService.sModeFilter);
		if(modeIntent != null) {
			data.shuffle = modeIntent.getIntExtra(PowerampAPI.SHUFFLE, PowerampAPI.ShuffleMode.SHUFFLE_NONE);
			data.repeat = modeIntent.getIntExtra(PowerampAPI.REPEAT, PowerampAPI.RepeatMode.REPEAT_NONE);
			if(LOG) Log.e(TAG, "repeat=" + data.repeat + " shuffle=" + data.shuffle);
		}
		return data;
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if(appWidgetIds.length == 0) {
			if(LOG) Log.e(TAG, "no widget ids");
			return;
		}
		
		if(LOG) Log.e(TAG, "onUpdate ids=" + Arrays.toString(appWidgetIds));
		if(sServiceName == null) {
			sServiceName = getWidgetUpdaterServiceName(context);
		}
		
		Intent intent = new Intent().setComponent(sServiceName)
							.putExtra(BaseWidgetUpdaterService.EXTRA_UPDATE_BY_OS, true);
		context.startService(intent);
	}

	
	public WidgetUpdateData pushUpdate(Context context, SharedPreferences prefs, int[] ids, boolean mediaRemoved, boolean trackChanged, boolean updateByOs, WidgetUpdateData data) {
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		if(ids == null) {
			try { // java.lang.RuntimeException: system server dead?  at android.appwidget.AppWidgetManager.getAppWidgetIds(AppWidgetManager.java:492) at com.maxmpz.audioplayer.widgetpackcommon.BaseWidgetProvider (":139)
				ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
			} catch(Exception ex) {
				Log.e(TAG, "", ex);
			}
		}
		
		if(ids == null || ids.length == 0) {
			return null;
		}
		
		if(LOG) Log.w(TAG, "pushUpdate() ids to update: " + Arrays.toString(ids));
		
		if(data == null) {
			data = generateUpdateData(context, mediaRemoved, trackChanged, updateByOs);
		}
		
		int tid = Process.myTid();
		int priority = Process.getThreadPriority(tid);
		Process.setThreadPriority(tid, Process.THREAD_PRIORITY_LOWEST);
		try {
			for(int id : ids) {
				RemoteViews rv = update(context, data, prefs, id); // java.lang.RuntimeException: Could not write bitmap to parcel blob.
				appWidgetManager.updateAppWidget(id, rv);
			}
			
		} catch(Exception ex) {
			Log.e(TAG, "", ex);
			
		} finally {
			Process.setThreadPriority(tid, priority);
		}
		return data;
	}


	protected boolean getAANoAnimState(WidgetUpdateData data, WidgetContext widgetCtx) {
		if(IS_HTC_SENSE && Build.VERSION.SDK_INT < 15
				|| data.albumArtNoAnim
				|| widgetCtx.lastAATimeStamp == data.albumArtTimestamp 
				|| data.track != null && (data.track.getInt(PowerampAPI.Track.FLAGS, 0) & PowerampAPI.Track.Flags.FLAG_FIRST_IN_PLAYER_SESSION) != 0) {
			
			if(LOG) Log.e(TAG, "same AA, noAnim=>true, same ts=" + widgetCtx.lastAATimeStamp + 
					" or FLAG_FIRST_IN_PLAYER_SESSION=" + (data.track.getInt(PowerampAPI.Track.FLAGS, 0) & PowerampAPI.Track.Flags.FLAG_FIRST_IN_PLAYER_SESSION) + " bitmap=" + data.albumArtBitmap);
			
			return true;
		}
		return false;
	}
	
	
	public static String getReadable(String title, String unknown) {
		if(title != null && title.length() > 0) {
			return title;
		}
		return unknown;
	}
}