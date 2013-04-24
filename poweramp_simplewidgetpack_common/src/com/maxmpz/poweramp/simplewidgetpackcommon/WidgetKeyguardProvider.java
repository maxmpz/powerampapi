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

import com.maxmpz.powerampapi.simplewidgetpack.R;

import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetKeyguardProvider extends Widget4x4Provider {
	private static final String TAG = "WidgetKeyguardProvider";
	private static final boolean LOG = false;

	
	public WidgetKeyguardProvider() {
		super();
		mWidgetLayout = R.layout.appwidget_keyguard;
	}

	protected int setShadow(RemoteViews views, int aaColor, int shadow) {
		int flipperFrameId;
		
		flipperFrameId = R.id.flipper_frame;
		
		switch(shadow) {
			case STYLE_3: // 2
				views.setViewVisibility(R.id.deck_bg, View.GONE);
				views.setViewVisibility(R.id.deck_bg2, View.GONE);
				views.setViewVisibility(R.id.deck_bg3, View.VISIBLE);
				break;
			case STYLE_2: // 1
				views.setViewVisibility(R.id.deck_bg, View.GONE);
				views.setViewVisibility(R.id.deck_bg2, View.VISIBLE);
				views.setViewVisibility(R.id.deck_bg3, View.GONE);
				break;
			case STYLE_1: // 0
			default:
				views.setViewVisibility(R.id.deck_bg, View.VISIBLE);
				views.setViewVisibility(R.id.deck_bg2, View.GONE);
				views.setViewVisibility(R.id.deck_bg3, View.GONE);
				break;
		}
		return flipperFrameId;
	}
	
	@Override
	protected void setBG(RemoteViews views, int color, int shadow) {
		//Log.e(TAG, "setBG color=" + Integer.toHexString(color) + " shadow=" + shadow);
		
		int alpha = Color.alpha(color);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);

		int id;
		switch(shadow) {
			case STYLE_3:
				id = R.id.deck_bg3;
				break;
			case STYLE_2:
				id = R.id.deck_bg2;
				break;
			case STYLE_1:
			default:
				id = R.id.deck_bg;
				break;
		}
		
		views.setInt(id, "setColorFilter", Color.rgb(r, g, b));
		views.setInt(id, "setAlpha", alpha);
		views.setViewVisibility(id, View.VISIBLE);
	}
}
