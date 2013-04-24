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

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.Dialog;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

public class Widget4x4Configure extends BaseWidgetConfigure implements OnItemSelectedListener, OnCheckedChangeListener, OnSeekBarChangeListener, OnAmbilWarnaListener {
	private static final String TAG = "Widget4x4Configure";
	private static final boolean LOG = false;
	
	
	private int mTheme = 0;
	
	protected int mBgColor = 0x55000000;
	protected int mShadow = Widget4x4Provider.SHADOW_BOTH_UP;
	private Handler mHandler = new Handler();
	private long mLastCreateResume;
		
	public Widget4x4Configure() {
		super();
		mWidgetProvider = new Widget4x4Provider();
		
		mConfigureContentLayout = R.layout.widget_configure_content_scroll;
		mConfigureOptionsLayout = R.layout.widget_4x4_configure;
	}
	
	@Override
	protected void setWidgetFrameSize() {
		mWidgetFrameHeight = getResources().getDimensionPixelSize(R.dimen.appwidget_4x4_height_config);
		mWidgetFrameWidth = LayoutParams.FILL_PARENT;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLastCreateResume = System.currentTimeMillis();

		if(savedInstanceState != null) {
			mBgColor = savedInstanceState.getInt("mBgColor");
		}
		
		findViewById(R.id.done_button).setOnClickListener(this);
		findViewById(R.id.color_button).setOnClickListener(this);
		
		ArrayAdapter<CharSequence> adapter;
		Spinner themeSpinner = (Spinner)findViewById(R.id.theme_spinner);
		if(themeSpinner != null) {
			adapter = ArrayAdapter.createFromResource(this, R.array.pref_theme_entries, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			themeSpinner.setAdapter(adapter);

			int themeToIndex = 0;
			
			themeSpinner.setSelection(themeToIndex);
			themeSpinner.setTag(new Object());
			themeSpinner.setOnItemSelectedListener(this);
		}
		
		final CheckBox altScale = (CheckBox)findViewById(R.id.alt_scale_cb);
		if(altScale != null) {
			altScale.setOnCheckedChangeListener(this);
		}
		
		final SeekBar opacity = (SeekBar)findViewById(R.id.alpha);
		opacity.setProgress(Color.alpha(mBgColor));
		opacity.setOnSeekBarChangeListener(this);
		
		Spinner shadowSpinner = (Spinner)findViewById(R.id.shadow_spinner);
		if(shadowSpinner != null) {
			adapter = ArrayAdapter.createFromResource(this, R.array.pref_widget_shadow_entries, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			shadowSpinner.setAdapter(adapter);
			shadowSpinner.setOnItemSelectedListener(this);
		}

		setDefaults();
		
		updateWidget(false);
		
		setBGColor(mBgColor);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mLastCreateResume = System.currentTimeMillis();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mBgColor", mBgColor);
	}
	
	protected void setDefaults() {
		mTheme = 0;
		
		Editor edit = mPrefs.edit();
		edit.remove(mAppWidgetId + Widget4x4Provider.PREF_SHADOW);
		edit.putBoolean(mAppWidgetId + Widget4x4Provider.PREF_ALT_SCALE, false);
		edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_THEME, mTheme);
		edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_COLOR, mBgColor);
		edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_SHADOW, Widget4x4Provider.SHADOW_BOTH_UP);
		
		edit.commit();
	}

	@Override
	public void onItemSelected(AdapterView<?> view, View arg1, int pos, long arg3) {
		if(view.getId() == R.id.theme_spinner) {
			if(mTheme != pos) {
				mTheme = pos;
				Editor edit = mPrefs.edit();
				//Log.w(TAG, "pos=" + pos);
				edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_THEME, pos);
				edit.commit();
				
				//updateWidget(true);
				updateWidgetDelayed();
			}
		} else if (view.getId() == R.id.shadow_spinner) {
			if(LOG) Log.e(TAG, "onItemSelected pos=" + pos + " view selected pos=" + view.getSelectedItemPosition());
			

			Editor edit = mPrefs.edit();
			edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_SHADOW, pos);
			
			if(pos == Widget4x4Provider.STYLE_3 && System.currentTimeMillis() - mLastCreateResume > 200) { // Avoid resitting alpha during init/restore step within first 200ms.
				((SeekBar)findViewById(R.id.alpha)).setProgress(0);
				setAlpha(0);
				edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_COLOR, mBgColor);
			}
			edit.commit();
			mShadow = pos;
			//updateWidget(true);
			updateWidgetDelayed();
		}
	}
	
	protected void updateWidgetDelayed() {
		mHandler.removeCallbacks(mUpdateWidgetDelayed);
		mHandler.postDelayed(mUpdateWidgetDelayed, 100);
	}
	
	private Runnable mUpdateWidgetDelayed = new Runnable() {
		public void run() {
			updateWidget(true);
		}
	};

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if (v.getId() == R.id.alt_scale_cb) {
			Editor edit = mPrefs.edit();
			edit.putBoolean(mAppWidgetId + Widget4x4Provider.PREF_ALT_SCALE, isChecked);
			edit.commit();
			updateWidget(false);
		} else if (v.getId() == R.id.alpha_sdk7) {
			if(LOG) Log.e(TAG, "alpha_sdk7=>" + (isChecked ? 0x55000000 : 0x00000000));
			Editor edit = mPrefs.edit();
			edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_COLOR, isChecked ? 0x55000000 : 0x00000000);
			edit.commit();
			updateWidget(true);
		}
	}
	
	private void setBGColor(int color) {
		mBgColor = color;
		View deck = mWidgetFrame.findViewById(R.id.deck_bg);

		setDeckBG(color, (ImageView)deck);
		
		int aaColor = Color.alpha(color);
		
		if(mShadow == Widget4x4Provider.SHADOW_ALBUM_ART) {
			View controlsShadow = mWidgetFrame.findViewById(R.id.controls_shadow);
			if(controlsShadow != null) {
				controlsShadow.setVisibility(aaColor == 0 ? View.GONE : View.VISIBLE);
			}
		}
		
		mHandler.removeCallbacks(mSaveRunnable);
		mHandler.postDelayed(mSaveRunnable, 500);
	}


	protected void setDeckBG(int color, ImageView deck) {
		deck.setBackgroundColor(mBgColor);
	}
	
	private Runnable mSaveRunnable = new Runnable() {
		public void run() {
			Editor edit = mPrefs.edit();
			if(LOG) Log.e(TAG, "saveRunnable bgColor=" + mBgColor);
			edit.putInt(mAppWidgetId + Widget4x4Provider.PREF_COLOR, mBgColor);
			edit.commit();
		}
	};
	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			setAlpha(progress);
		}			
	}


	private void setAlpha(int progress) {
		int r = Color.red(mBgColor);
		int g = Color.green(mBgColor);
		int b = Color.blue(mBgColor);
		
		setBGColor(Color.argb(progress, r, g, b));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
	@Override
	protected void onDone() {
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.color_button) {
			showDialog(DIALOG_COLOR);
		} else {
			super.onClick(v);
		}
	}
	
	
	private final int DIALOG_COLOR = 1;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_COLOR:
				return new AmbilWarnaDialog(this, mBgColor, this).getDialog();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onCancel(AmbilWarnaDialog dialog) {
	}

	@Override
	public void onOk(AmbilWarnaDialog dialog, int color) {
		int alpha = Color.alpha(mBgColor);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		setBGColor(Color.argb(alpha, r, g, b));
	}
}
