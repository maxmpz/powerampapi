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

package com.maxmpz.poweramp.simplewidgetpackcommon;

import java.util.Arrays;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.widgetpackcommon.BaseWidgetUpdaterService;
import com.maxmpz.poweramp.widgetpackcommon.WidgetUpdateData;
import com.maxmpz.powerampapi.simplewidgetpack.R;

/**
 * Base widget provider for PowerampAPI based app widgets.
 */
public abstract class SimpleBaseWidgetProvider extends com.maxmpz.poweramp.widgetpackcommon.BaseWidgetProvider {
	private static final String TAG = "BaseWidgetProvider";
	private static final boolean LOG = false;
	
	public static final String PREF_NO_BG_WIDGETS = "no_bg_widgets";
	public static final String PREF_ALBUM_ART = "album_art";
	
	protected static IconsHelper sIconsHelper;

	private final static Intent PLAY_INTENT = new Intent(PowerampAPI.ACTION_API_COMMAND).putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.TOGGLE_PLAY_PAUSE);

	public static final String V140_LIST_TYPE = "list_type";
	
	public static final int V140_LIST_NOW_PLAYING = 1;
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		if(LOG) Log.e(TAG, "onDeleted=" + this);
		super.onDeleted(context, appWidgetIds);
		SharedPreferences prefs = BaseWidgetUpdaterService.getCachedSharedPreferences(context);
		Editor edit = prefs.edit();
		for(int id : appWidgetIds) {
			edit.remove(id + PREF_NO_BG_WIDGETS);
			edit.remove(id + PREF_ALBUM_ART);
			onWidgetDeleted(edit, id);
		}
		edit.commit();
	}
	
	protected void onWidgetDeleted(Editor edit, int id) {
	}
	
	@Override
	protected ComponentName getWidgetUpdaterServiceName(Context context) {
		return new ComponentName(context.getPackageName(), context.getPackageName() + ".WidgetUpdaterService");
	}
	
	protected void bindButtons(Context context, RemoteViews views) {
		// NOTE: various requests ids (1, 2, 3...) are required to get different pending intent instances (they can't be distinguished by extras).
		views.setOnClickPendingIntent(R.id.folder_next_button, 
				PendingIntent.getService(context, 1, new Intent(PowerampAPI.ACTION_API_COMMAND)
														.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT_IN_CAT), PendingIntent.FLAG_UPDATE_CURRENT));

		views.setOnClickPendingIntent(R.id.play_button, 
				PendingIntent.getService(context, 2, PLAY_INTENT, PendingIntent.FLAG_UPDATE_CURRENT));
		
		
		views.setOnClickPendingIntent(R.id.folder_prev_button, 
				PendingIntent.getService(context, 3, new Intent(PowerampAPI.ACTION_API_COMMAND)
														.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS_IN_CAT), PendingIntent.FLAG_UPDATE_CURRENT));
		
		views.setOnClickPendingIntent(R.id.ff_button, 
				PendingIntent.getService(context, 4, new Intent(PowerampAPI.ACTION_API_COMMAND)
														.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.NEXT), PendingIntent.FLAG_UPDATE_CURRENT));
		
		views.setOnClickPendingIntent(R.id.rw_button, 
				PendingIntent.getService(context, 5, new Intent(PowerampAPI.ACTION_API_COMMAND)
														.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	public static String getReadable(String title, String unknown) {
		if(title != null && title.length() > 0) {
			return title;
		}
		return unknown;
	}

	protected void bindBaseTextFields(Context context, WidgetUpdateData data, final RemoteViews views, boolean glueAlbum) {
		final Bundle track = data.track;
		
		if(track != null) {
			StringBuilder sb = new StringBuilder();
			
			views.setTextViewText(R.id.title, 
					getReadable(track.getString(PowerampAPI.Track.TITLE), context.getString(R.string.unknown)));
			
			if(glueAlbum) {
				sb.append(getReadable(track.getString(PowerampAPI.Track.ARTIST), context.getString(R.string.unknown_artist_name)));
				sb.append(" - ");
				sb.append(getReadable(track.getString(PowerampAPI.Track.ALBUM), context.getString(R.string.unknown_album_name)));
				views.setTextViewText(R.id.line2, sb.toString());
				sb.setLength(0);
			} else {
				views.setTextViewText(R.id.line2, getReadable(track.getString(PowerampAPI.Track.ARTIST), context.getString(R.string.unknown_artist_name)));
				views.setTextViewText(R.id.line3, getReadable(track.getString(PowerampAPI.Track.ALBUM), context.getString(R.string.unknown_album_name)));
			}
			if(LOG) Log.e(TAG, "bindBaseTextFields playing=" + data.playing);
			views.setImageViewResource(R.id.play_button, data.playing ? R.drawable.matte_pause_big_selector : R.drawable.matte_play_big_selector);
			
			if(data.shuffle == PowerampAPI.ShuffleMode.SHUFFLE_ALL) {
				views.setBoolean(R.id.folder_next_button, "setEnabled", false);
				views.setBoolean(R.id.folder_prev_button, "setEnabled", false);
			} else {
				views.setBoolean(R.id.folder_next_button, "setEnabled", true);
				views.setBoolean(R.id.folder_prev_button, "setEnabled", true);
			}
			
			if(data.apiVersion >= API_VERSION_200) {
				int posInList = track.getInt(PowerampAPI.Track.POS_IN_LIST, 0);
				int listSize = track.getInt(PowerampAPI.Track.LIST_SIZE, 0);
				
				if(posInList >= 0 && listSize > 0) { 
					views.setTextViewText(R.id.counter, (posInList + 1) + "/" + listSize);
					views.setViewVisibility(R.id.counter, View.VISIBLE);
				} else {
					views.setViewVisibility(R.id.counter, View.GONE);
				}
			} else {
				views.setViewVisibility(R.id.counter, View.GONE);
			}
			
			
		} else {
			views.setTextViewText(R.id.title, context.getString(R.string.widget_no_songs_selected));
			views.setImageViewResource(R.id.play_button, R.drawable.matte_play_big_selector);
		}
	}

	protected void updateRepeatShuffle(int repeat, int shuffle, RemoteViews views, WidgetUpdateData data) {
		if(LOG) Log.e(TAG, "repeat=" + repeat + " shuffle=" + shuffle);
		if(sIconsHelper != null) {
			if(data.apiVersion < API_VERSION_200) {
				switch(repeat) {
					case RepeatModeV140.REPEAT_ALL:
					case RepeatModeV140.REPEAT_CAT:
						repeat = PowerampAPI.RepeatMode.REPEAT_ON;
						break;
					case RepeatModeV140.REPEAT_SONG:						
						repeat = PowerampAPI.RepeatMode.REPEAT_SONG;
						break;
					default:
					case RepeatModeV140.REPEAT_NONE:						
						repeat = PowerampAPI.RepeatMode.REPEAT_NONE;
						break;
					
				}
				switch(shuffle) {
					case ShuffleModeV140.SHUFFLE_ALL:
						shuffle = PowerampAPI.ShuffleMode.SHUFFLE_ALL;
						break;
					case ShuffleModeV140.SHUFFLE_IN_CAT:
						shuffle = PowerampAPI.ShuffleMode.SHUFFLE_SONGS;
						break;
					case ShuffleModeV140.SHUFFLE_HIER:
						shuffle = PowerampAPI.ShuffleMode.SHUFFLE_SONGS_AND_CATS;
						break;
					default:
					case ShuffleModeV140.SHUFFLE_NONE:
						shuffle = PowerampAPI.ShuffleMode.SHUFFLE_NONE;
						break;
				}
			}
			views.setInt(R.id.repeat_icon, "setImageLevel", repeat);
			views.setInt(R.id.shuffle_icon, "setImageLevel", shuffle);
		} else {
			if(LOG) Log.e(TAG, "sIconsHelper == null");
		}
	}
	

	protected void bindGoToMainUI(Context context, final RemoteViews views, int id) {
		views.setOnClickPendingIntent(id, 
				PendingIntent.getActivity(context, 0, 
										  new Intent().setComponent(new ComponentName(PowerampAPI.PACKAGE_NAME, PowerampAPI.ACTIVITY_PLAYER_UI))
										  			  .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 
										  PendingIntent.FLAG_UPDATE_CURRENT));
	}
	

	protected void bindGoToPlUI(Context context, final RemoteViews views, int id, int apiVersion) {
		Intent intent;
		
		if(apiVersion < API_VERSION_200) {
			intent = new Intent(PowerampAPI.ACTION_SHOW_LIST)
				.putExtra(V140_LIST_TYPE, V140_LIST_NOW_PLAYING)
				.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		} else {
			intent = new Intent(PowerampAPI.ACTION_SHOW_CURRENT).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		}
		
		views.setOnClickPendingIntent(id, 
				PendingIntent.getActivity(context, 0, 
					intent, PendingIntent.FLAG_UPDATE_CURRENT));
	}
}
