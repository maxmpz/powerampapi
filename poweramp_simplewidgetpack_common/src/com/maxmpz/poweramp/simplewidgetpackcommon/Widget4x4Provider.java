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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.widgetpackcommon.WidgetUpdateData;
import com.maxmpz.powerampapi.simplewidgetpack.R;

public class Widget4x4Provider extends SimpleBaseWidgetProvider {
	private static final String TAG = "Widget4x4AAProvider";
	private static final boolean LOG = false;
	private static final boolean LOG_AA = false;
	
	protected static final int DEFAULT_BG = 0x55000000; //0x55000000;	
	
	protected static final SparseArray<WidgetContext> sWidgetContexts = new SparseArray<WidgetContext>(2);	

	// NOTE: each such preference requires string contact (id + pref).
	
	public static final String PREF_THEME = "theme";
	public static final String PREF_ALT_SCALE = "alt_scale";
	public static final String PREF_SHADOW = "shadow";
	public static final String PREF_COLOR = "color";
	
	public static final int SHADOW_BOTH_UP = 0;
	public static final int SHADOW_BOTH_DOWN = 1;
	public static final int SHADOW_ALBUM_ART = 2;
	
	public static final int STYLE_1 = 0;
	public static final int STYLE_2 = 1;
	public static final int STYLE_3 = 2;
	
	// non-direct files (bitmaps) produces pops and clicks on SGS (probably due to slow parcel save to flash).
	// direct files can cause problems if the original file is too large.
	// also it can kill launcher pro if something bad is going with the memory in it.
	private static final boolean USE_DIRECT_FILES = true;
	
	protected int mWidgetLayout;
	protected boolean mGlueAlbum;
	
	public Widget4x4Provider() {
		super();
		mWidgetLayout = R.layout.appwidget_4x4;
		mGlueAlbum = true;
	}
	
	public static void clearContexts() {
		sWidgetContexts.clear();
	}
	

	@Override
	public RemoteViews update(Context context, WidgetUpdateData data, SharedPreferences prefs, int id) {
		if(LOG) Log.e(TAG, "update id=" + id + " me=" + this);
		
		if(sIconsHelper == null) {
			sIconsHelper = new IconsHelper(context.getApplicationContext());
		}
		
		RemoteViews views = new RemoteViews(context.getPackageName(), mWidgetLayout);
		
		bindButtons(context, views);
		
		views.setOnClickPendingIntent(R.id.shuffle_icon, PendingIntent.getService(context, 6, 
				new Intent(PowerampAPI.ACTION_API_COMMAND)
														.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SHUFFLE), 0));
		
		views.setOnClickPendingIntent(R.id.repeat_icon, PendingIntent.getService(context, 7, 
				new Intent(PowerampAPI.ACTION_API_COMMAND)
														.putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.REPEAT), 0));
		
		final Bundle track = data.track;
		if(track != null) {
			int catMatchUri = track.getInt(PowerampAPI.Track.CAT);
			boolean isCue = track.getBoolean(PowerampAPI.Track.IS_CUE);
			
			bindBaseTextFields(context, data, views, mGlueAlbum);
		
			int iconRes = data.apiVersion >= API_VERSION_200 ? sIconsHelper.getIcon(catMatchUri, isCue) : sIconsHelper.getIconV140(catMatchUri, isCue);

			//if(LOG) Log.e(TAG, "cat=" + catMatchUri + " iconRes=" + iconRes + " data.apiVersion=" + data.apiVersion);
			
			views.setImageViewResource(R.id.type_image, iconRes);
		
			updateRepeatShuffle(data.repeat, data.shuffle, views, data);
		} else {
			bindBaseTextFields(context, data, views, mGlueAlbum);
			views.setImageViewResource(R.id.type_image, 0);
		}

		boolean altScale = prefs.getBoolean(id + PREF_ALT_SCALE, false);
		int flipperFrameId; 
		int color = prefs.getInt(id + PREF_COLOR, DEFAULT_BG);
		int aaColor = Color.alpha(color);
		int shadow = 0;
		shadow = prefs.getInt(id + PREF_SHADOW, 0);
		
		flipperFrameId = setShadow(views, aaColor, shadow);

		setBG(views, color, shadow);

		setTheme(prefs, id, views);
		
		WidgetContext widgetCtx = sWidgetContexts.get(id);
		if(widgetCtx == null) {
			if(LOG) Log.e(TAG, "creating widgetCtx for id=" + id + " me=" + this);
			widgetCtx = new WidgetContext();
			sWidgetContexts.put(id, widgetCtx);
		}
		
		updateAlbumArt(context, prefs, id, data, views, altScale, flipperFrameId, widgetCtx);
		
		bindNavigation(context, views, data);		
		
		return views;
	}
	
	protected void bindNavigation(Context context, RemoteViews views, WidgetUpdateData data) {
		bindGoToMainUI(context, views, R.id.aa_cont_layout);
		bindGoToPlUI(context, views, R.id.playing_now, data.apiVersion);
	}

	protected void setBG(RemoteViews views, int color, int shadow) {
		views.setInt(R.id.deck_bg, "setBackgroundColor", color);
	}

	protected void setTheme(SharedPreferences prefs, int id, RemoteViews views) {
		int smallButtonRes;
		int mediumButtonRes;
		int bigButtonRes;

		final int theme = prefs.getInt(id + PREF_THEME, 0);

		switch(theme) {
		
			case 1:
				smallButtonRes = R.drawable.alu_small_round_button_selector;
				mediumButtonRes = R.drawable.alu_medium_round_button_selector;
				bigButtonRes = R.drawable.alu_big_round_button_selector;
				break;
			case 0:
			default:
				smallButtonRes = R.drawable.matte_small_round_button_selector;
				mediumButtonRes = R.drawable.matte_medium_round_button_selector;
				bigButtonRes = R.drawable.matte_big_round_button_selector;
				break;
		}
		
		
		views.setInt(R.id.folder_prev_button, "setBackgroundResource", smallButtonRes);
		views.setInt(R.id.rw_button, "setBackgroundResource", mediumButtonRes);
		views.setInt(R.id.play_button, "setBackgroundResource", bigButtonRes);
		views.setInt(R.id.ff_button, "setBackgroundResource", mediumButtonRes);
		views.setInt(R.id.folder_next_button, "setBackgroundResource", smallButtonRes);
	}

	protected int setShadow(RemoteViews views, int aaColor, int shadow) {
		int flipperFrameId;
		switch(shadow) {
			case SHADOW_ALBUM_ART:
				views.setViewVisibility(R.id.shadow, View.INVISIBLE);
				views.setViewVisibility(R.id.shadow_up, View.INVISIBLE);
				views.setViewVisibility(R.id.shadow_down, View.INVISIBLE);
				flipperFrameId = R.id.flipper_frame_alt;
				views.setViewVisibility(R.id.flipper_frame, View.GONE);
				if(aaColor > 0) {
					views.setViewVisibility(R.id.controls_shadow, View.VISIBLE);
				}
				break;
			case SHADOW_BOTH_DOWN:
				views.setViewVisibility(R.id.shadow_up, View.INVISIBLE);
				views.setViewVisibility(R.id.shadow_down, View.VISIBLE);
				flipperFrameId = R.id.flipper_frame;
				views.setViewVisibility(R.id.flipper_frame_alt, View.GONE);
				break;
			case SHADOW_BOTH_UP:
			default:
				views.setViewVisibility(R.id.shadow_up, View.VISIBLE);
				views.setViewVisibility(R.id.shadow_down, View.INVISIBLE);
				flipperFrameId = R.id.flipper_frame;
				views.setViewVisibility(R.id.flipper_frame_alt, View.GONE);
				break;
		}
		return flipperFrameId;
	}
	
	protected void updateAlbumArt(Context context, SharedPreferences prefs, int id, WidgetUpdateData data, final RemoteViews views, boolean altScale, int flipperFrameId, WidgetContext widgetCtx) {
		if(LOG || LOG_AA) Log.e(TAG, "updateAlbumArt() me=" + this);
		
		final boolean noAnim = getAANoAnimState(data, widgetCtx);
		
		widgetCtx.lastAATimeStamp = data.albumArtTimestamp;

		final int aaLayout = altScale ? R.layout.appwidget_4x4_aa_layout_alt : R.layout.appwidget_4x4_aa_layout;
		
		views.removeAllViews(flipperFrameId);
		
		Bitmap albumArtBitmap = data.albumArtBitmap;
		String path = data.albumArtPath;
		
		if(noAnim) {
			// Set album art without animation.
			if(albumArtBitmap != null && albumArtBitmap.getHeight() >= MIN_HEIGHT && albumArtBitmap.getWidth() >= MIN_WIDTH || !TextUtils.isEmpty(path)) {
				final RemoteViews aaView2 = new RemoteViews(context.getPackageName(), aaLayout);
				views.setViewVisibility(flipperFrameId, View.VISIBLE);
				
				try {
					applyBitmapOrFile(albumArtBitmap, path, aaView2);
					
					views.addView(flipperFrameId, aaView2);
					
					//if(altScale) {
						views.setViewVisibility(R.id.logo, View.INVISIBLE);
					//}
					if(LOG || LOG_AA) Log.w(TAG, "AA => updated, no anim");
					return;
				} catch(OutOfMemoryError er) {
					Log.e(TAG, "", er);
				}
			} 
			views.setViewVisibility(R.id.logo, View.VISIBLE);
			views.setViewVisibility(flipperFrameId, View.INVISIBLE);
			// Fall back for no image or oom.
			if(LOG || LOG_AA) Log.w(TAG, "updateAlbumArt() - no AA (fast)");
			return;
		}
		
		final RemoteViews flipper = new RemoteViews(context.getPackageName(), R.layout.appwidget_4x4_flipper);

		if(albumArtBitmap != null &&  albumArtBitmap.getHeight() >= MIN_HEIGHT && albumArtBitmap.getWidth() >= MIN_WIDTH  || !TextUtils.isEmpty(path)) {
			// Proceed with animation.
			final RemoteViews aaView2 = new RemoteViews(context.getPackageName(), aaLayout);
			flipper.addView(R.id.view_flipper, aaView2);
			views.setViewVisibility(flipperFrameId, View.VISIBLE);
			
			try {
				applyBitmapOrFile(albumArtBitmap, path, aaView2);
				
				views.addView(flipperFrameId, flipper);
				//if(altScale) {
					views.setViewVisibility(R.id.logo, View.INVISIBLE);
				//}
				
				if(LOG || LOG_AA) Log.w(TAG, "AA => updated, animated");
				return;
			} catch(OutOfMemoryError er) {
				Log.e(TAG, "", er);
			}
		} 
			
		// Fall back for no image or oom.
		if(LOG || LOG_AA) Log.w(TAG, "updateAlbumArt() - no AA");
		views.setViewVisibility(R.id.logo, View.VISIBLE);
		views.setViewVisibility(flipperFrameId, View.INVISIBLE);
		views.addView(flipperFrameId, flipper);
	}
	
	private void applyBitmapOrFile(Bitmap bitmapAlbumArt, String path, RemoteViews aaView2) {
		if(LOG || LOG_AA) Log.e(TAG, "applyBitmapOrFile() me=" + this);
		
		if(USE_DIRECT_FILES && path != null) {
			if(LOG || LOG_AA) Log.w(TAG, "AA => got jpeg on disk");
			aaView2.setImageViewUri(R.id.image, Uri.parse(path));
		} else {
			if(bitmapAlbumArt != null) {
				if(LOG || LOG_AA) Log.w(TAG, "got bitmap w=" + bitmapAlbumArt.getWidth() + " h=" + bitmapAlbumArt.getHeight() + " rowBytes=" + bitmapAlbumArt.getRowBytes() + " total bytes=" + bitmapAlbumArt.getRowBytes() * bitmapAlbumArt.getHeight());
			}
			if(bitmapAlbumArt != null && (bitmapAlbumArt.getWidth() <= 0 || bitmapAlbumArt.getHeight() <= 0)) { // Safeguard about occasional bad images.
				bitmapAlbumArt = null;
			}
			aaView2.setImageViewBitmap(R.id.image, bitmapAlbumArt);
		}
	}
}
