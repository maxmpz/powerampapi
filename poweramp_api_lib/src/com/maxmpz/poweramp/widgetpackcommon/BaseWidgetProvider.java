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

import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.maxmpz.poweramp.player.PowerampAPI;

/**
 * Base widget provider for PowerampAPI based app widgets
 */
public abstract class BaseWidgetProvider extends AppWidgetProvider implements
		IWidgetUpdater
{
	private static final String TAG = "BaseWidgetProvider";
	private static final boolean LOG = false;

	public static final int WIDGET_PACK_PREFS_VERSION = 209;

	public static final int API_VERSION_200 = 200;

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

	/**
	 * Min. AA image size to show (otherwise logo shown)
	 */
	protected static final int MIN_SIZE = 32;
	
	private @Nullable ComponentName mComponentName; // This provider component name
	private @Nullable AppWidgetManager mAppWidgetManager;
	
	/**
	 * Creates and caches widgetupdater suitable for updating this provider. Called when provider is called by system or by widget configure. Implmentation should be thread safe
	 * @param context
	 * @return
	 */
	// REVISIT: threading - actually always called on gui thread 
	protected abstract @NonNull WidgetUpdater getWidgetUpdater(Context context);

	/**
	 * THREADING: any
	 * @param context
	 * @param data
	 * @param prefs
	 * @param id
	 * @return
	 */
	public abstract @NonNull RemoteViews update(Context context, @NonNull WidgetUpdateData data, @NonNull SharedPreferences prefs, int id);


	// NOTE: called by system
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if(appWidgetIds.length == 0) {
			if(LOG) Log.e(TAG, "no widget ids");
			return;
		}

		if(LOG) Log.w(TAG, "onUpdate ids=" + Arrays.toString(appWidgetIds));

		WidgetUpdater widgetUpdater = getWidgetUpdater(context);

		try {

			widgetUpdater.updateSafe(this, true, true, appWidgetIds); // Immediate update, ignores power state

		} catch(Throwable th) {
			Log.e(TAG, "", th);
		}
	}



	// THREADING: any
	@Override
	public WidgetUpdateData pushUpdate(Context context, @NonNull SharedPreferences prefs, @Nullable int[] ids, boolean mediaRemoved, @NonNull WidgetUpdateData data) {
		AppWidgetManager appWidgetManager = mAppWidgetManager;
		if(appWidgetManager == null) {
			appWidgetManager = mAppWidgetManager = AppWidgetManager.getInstance(context); 
		}
		
		if(ids == null) {
			try { // java.lang.RuntimeException: system server dead?  at android.appwidget.AppWidgetManager.getAppWidgetIds(AppWidgetManager.java:492) at com.maxmpz.audioplayer.widgetpackcommon.BaseWidgetProvider (":139)
				long start;
				if(LOG) start = System.nanoTime();
				
				if(mComponentName == null) {
					mComponentName = new ComponentName(context, this.getClass());
				}
				ids = appWidgetManager.getAppWidgetIds(mComponentName);

				if(LOG) Log.w(TAG, "pushUpdate getAppWidgetIds in=" + (System.nanoTime() - start) / 1000 + " =>ids=" + Arrays.toString(ids) + " me=" + this);
			} catch(Exception ex) {
				Log.e(TAG, "", ex);
			}
		}

		if(ids == null || ids.length == 0) {
			if(LOG) Log.w(TAG, "pushUpdate FAIL no ids me=" + this);
			return null;
		}

		if(LOG) Log.w(TAG, "pushUpdate ids to update: " + Arrays.toString(ids) + " data=" + data + " me=" + this);

		try {
			for(int id : ids) {
				if(id == 0) { // Skip possible zero ids
					if(LOG) Log.w(TAG, "pushUpdate SKIP as ids[0] me=" + this);
					break;
				}
				RemoteViews rv = update(context, data, prefs, id); // java.lang.RuntimeException: Could not write bitmap to parcel blob.
				appWidgetManager.updateAppWidget(id, rv);
			}

		} catch(Exception ex) {
			Log.e(TAG, "", ex);
		}
		return data;
	}


	@SuppressWarnings("static-method")
	protected boolean getAANoAnimState(WidgetUpdateData data, WidgetContext widgetCtx) {
		if(data.albumArtNoAnim
				|| widgetCtx.lastAATimeStamp == data.albumArtTimestamp
				|| data.hasTrack && (data.flags & PowerampAPI.Track.Flags.FLAG_FIRST_IN_PLAYER_SESSION) != 0) {

			if(LOG) Log.w(TAG, "getAANoAnimState =>true data.albumArtNoAnim=" + data.albumArtNoAnim + " same ts=" + (widgetCtx.lastAATimeStamp == data.albumArtTimestamp) +
					"  FLAG_FIRST_IN_PLAYER_SESSION=" + (data.flags & PowerampAPI.Track.Flags.FLAG_FIRST_IN_PLAYER_SESSION) + " bitmap=" + data.albumArtBitmap);

			return true;
		}
		return false;
	}


	public static String getReadable(String title, String unknown) {
		return getReadable(title, unknown, false);
	}
	
	public static String getReadable(String title, String unknown, boolean allowEmpty) {
		if(title != null && (allowEmpty || title.length() > 0)) {
			return title;
		}
		return unknown;
	}
}