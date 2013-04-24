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

import com.maxmpz.poweramp.widgetpackcommon.WidgetUpdateData;
import com.maxmpz.powerampapi.simplewidgetpack.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

// NOTE: keep in mind that this provider gets instantiated and destroyed for just any update or other calls.
public class Widget4x2Provider extends Widget4x4Provider {
	public static final String PREF_AA = "aa";
	
	protected boolean mAAEnabled;
	
	public Widget4x2Provider() {
		super();
		mWidgetLayout = R.layout.appwidget_4x2;
		mGlueAlbum = false;
	}
	
	protected void onWidgetDeleted(Editor edit, int id) {
		edit.remove(id + PREF_AA);
	}
	
	@Override
	public RemoteViews update(Context context, WidgetUpdateData data, SharedPreferences prefs, int id) {
		mAAEnabled = prefs.getBoolean(id + PREF_AA, true);
		return super.update(context, data, prefs, id);
	}
	
	protected int setShadow(RemoteViews views, int aaColor, int shadow) {
		int flipperFrameId;
		switch(shadow) {
			case SHADOW_ALBUM_ART:
				views.setViewVisibility(R.id.shadow, View.INVISIBLE);
				flipperFrameId = R.id.flipper_frame_alt;
				break;
			case SHADOW_BOTH_DOWN:
				flipperFrameId = R.id.flipper_frame;
				views.setViewVisibility(R.id.flipper_frame_alt, View.GONE);
				if(aaColor == 0) {
					views.setViewVisibility(R.id.shadow, View.INVISIBLE);
				}
				
				break;
			case SHADOW_BOTH_UP:
			default:
				flipperFrameId = R.id.flipper_frame_alt;
				if(aaColor == 0) {
					views.setViewVisibility(R.id.shadow, View.INVISIBLE);
				}
				break;
		}
		return flipperFrameId;
	}
	
	protected void setTheme(SharedPreferences prefs, int id, RemoteViews views) {
	}
	
	protected void bindNavigation(Context context, RemoteViews views, WidgetUpdateData data) {
		if(mAAEnabled) {
			bindGoToMainUI(context, views, R.id.aa_cont_layout);
			bindGoToPlUI(context, views, R.id.playing_now, data.apiVersion);
		} else {
			bindGoToMainUI(context, views, R.id.playing_now);
		}
	}
	
	@Override
	protected void updateAlbumArt(Context context, SharedPreferences prefs, int id, WidgetUpdateData data, RemoteViews views, boolean altScale, int flipperFrameId, WidgetContext widgetCtx) {
		//final boolean aa = prefs.getBoolean(id + PREF_AA, true);
		if(mAAEnabled) {
			views.setInt(R.id.album_artist, "setGravity", Gravity.TOP | Gravity.LEFT);
			super.updateAlbumArt(context, prefs, id, data, views, altScale, flipperFrameId, widgetCtx);
		} else {
			views.setInt(R.id.album_artist, "setGravity", Gravity.TOP | Gravity.CENTER_HORIZONTAL);
			views.setViewVisibility(R.id.aa_cont_layout, View.GONE);
		}
	}
	
	protected void setBG(RemoteViews views, int color, int shadow) {
		int alpha = Color.alpha(color);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		views.setInt(R.id.deck_bg, "setColorFilter", Color.rgb(r, g, b));
		views.setInt(R.id.deck_bg, "setAlpha", alpha);
		views.setViewVisibility(R.id.deck_bg, View.VISIBLE);
		views.setViewVisibility(R.id.shadow, shadow != Widget4x4Provider.SHADOW_ALBUM_ART && alpha > 0 ? View.VISIBLE : View.INVISIBLE);
	}
	
}
