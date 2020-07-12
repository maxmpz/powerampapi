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

import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

public class Widget4x2Configure extends Widget4x4Configure {
	private static final String TAG = "Widget4x2AAConfigure";
	
	public Widget4x2Configure() {
		super();
		mWidgetProvider = new Widget4x2Provider();
		mConfigureContentLayout = R.layout.widget_configure_content_centered;
		mConfigureOptionsLayout = R.layout.widget_small_configure;
	}
	
	protected void setDefaults() {
		super.setDefaults();
		
		Editor edit = mPrefs.edit();
		edit.putBoolean(mAppWidgetId + Widget4x2Provider.PREF_AA, true);
		edit.commit();
	}
	
	@Override
	protected void setWidgetFrameSize() {
		final Resources r = getResources();
		mWidgetFrameHeight = r.getDimensionPixelSize(R.dimen.appwidget_small_configure_height);
		mWidgetFrameWidth = r.getDimensionPixelSize(R.dimen.appwidget_small_configure_width);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CheckBox aaCheckBox = (CheckBox)findViewById(R.id.aa_cb);
		aaCheckBox.setChecked(true);
		aaCheckBox.setOnCheckedChangeListener(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if(v.getId() == R.id.aa_cb) {
			Editor edit = mPrefs.edit();
			edit.putBoolean(mAppWidgetId + Widget4x2Provider.PREF_AA, isChecked);
			edit.commit();
			updateWidgetDelayed();
		} else {
			super.onCheckedChanged(v, isChecked);
		}
	}
	
	protected void setDeckBG(int color, ImageView deck) {
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		deck.setColorFilter(Color.rgb(r, g, b));
		int alpha = Color.alpha(color);
		deck.setAlpha(alpha);
		
		findViewById(R.id.shadow).setVisibility(mShadow != Widget4x4Provider.SHADOW_ALBUM_ART && alpha > 0 ? View.VISIBLE : View.INVISIBLE);
	}
	
}
