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

import java.lang.reflect.Method;
import java.util.Arrays;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.widgetpackcommon.BaseWidgetUpdaterService;
import com.maxmpz.poweramp.widgetpackcommon.WidgetUpdateData;
import com.maxmpz.powerampapi.simplewidgetpack.R;

public abstract class BaseWidgetConfigure extends Activity implements OnClickListener {
	private static final String TAG = "BaseWidgetConfigure";
	private static final boolean LOG = false;
	
	private static final boolean DEBUG = true;
	
	static {
		if(DEBUG) Log.e(TAG, "DEBUG enabled, disable for release build");
	}
	
	protected int mAppWidgetId;
	protected SimpleBaseWidgetProvider mWidgetProvider;
	protected SharedPreferences mPrefs;
	
	private int mStickyIntents;
	private int mLastLayoutId;
	
	protected ViewGroup mWidgetFrame;
	
	// Set by descendants.
	protected int mConfigureContentLayout;
	protected int mConfigureOptionsLayout;
	protected int mWidgetFrameHeight = -1;
	protected int mWidgetFrameWidth = -1;
	
	@Override
	public Resources getResources() {
		try {
			return getApplication().getResources();
		} catch(Throwable ex) {
			Log.e(TAG, "", ex);
			return super.getResources();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setWidgetFrameSize();
		setTitle(R.string.configure_widget);
		
		final Resources r = getResources();
		final DisplayMetrics dm = r.getDisplayMetrics();
		Configuration configuration = r.getConfiguration(); 
		final int size = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		
		if(!(Build.VERSION.SDK_INT >= 11 && 
				(size == Configuration.SCREENLAYOUT_SIZE_XLARGE
				|| size == Configuration.SCREENLAYOUT_SIZE_LARGE && Math.max(dm.widthPixels, dm.heightPixels) > 1000))) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		

		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();

		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		mPrefs = BaseWidgetUpdaterService.getCachedSharedPreferences(this);
		
		// NOTE: always return RESULT_OK. RESULT_CANCELED will make ghost widgets.
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		
		if(LOG) Log.e(TAG, "extras=" + Arrays.toString(intent.getExtras().keySet().toArray(new String[]{})));
	
		// If they gave us an intent without the widget id, just bail.
		if(mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			if(DEBUG) {
				mAppWidgetId = (int)Math.random() * Integer.MAX_VALUE;	
			} else {
				Log.e(TAG, "AppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID");
				finish();
			}
		}
		
		setContentView(R.layout.widget_configure);
		
		ViewGroup container = (ViewGroup)findViewById(R.id.container);
		
		ViewGroup optionsFrame = (ViewGroup)findViewById(R.id.options_frame);
		
		final LayoutInflater inflater = getLayoutInflater();

		//View contentLayout = inflater.inflate(mConfigureContentLayout, container, false);
		//container.addView(contentLayout, 0);
		
		inflater.inflate(mConfigureContentLayout, container, true);
		View contentLayout = container.getChildAt(container.getChildCount() - 1);
		container.removeView(contentLayout);
		container.addView(contentLayout, 0);
		
		inflater.inflate(mConfigureOptionsLayout, optionsFrame, true);
	}
	
	protected abstract void setWidgetFrameSize();
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(!isFinishing() && mStickyIntents++ >= 3) {
				updateWidget(false);
			}
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(PowerampAPI.ACTION_STATUS_CHANGED);
		filter.addAction(PowerampAPI.ACTION_AA_CHANGED);		
		filter.addAction(PowerampAPI.ACTION_PLAYING_MODE_CHANGED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		try {
			unregisterReceiver(mReceiver);
		} catch(Exception ex) {
		}
		super.onPause();
	}
	

	@Override
	public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
	}
	
    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
        return this;
    }

	protected void updateWidget(boolean full) {
		if(LOG) Log.e(TAG, "updateWidget full=" + full);
		
		mWidgetFrame = (ViewGroup)findViewById(R.id.widget_frame);
		
		MarginLayoutParams lp = (MarginLayoutParams)mWidgetFrame.getLayoutParams();
		lp.height = mWidgetFrameHeight;
		lp.width = mWidgetFrameWidth;
		mWidgetFrame.setLayoutParams(lp);
		
		// Check if media is removed. In this case PowerAMPiAPI status can be in stale "playing" state (as Poweramp process could be killed by ripper).
		boolean mediaRemoved = !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		
		WidgetUpdateData data = mWidgetProvider.generateUpdateData(this, mediaRemoved, false, false);
		data.albumArtTimestamp = System.currentTimeMillis();
		data.albumArtNoAnim = true;
		RemoteViews rv = mWidgetProvider.update(this, data, mPrefs, mAppWidgetId);
		
		int layoutId = rv.getLayoutId();
		
		if(full || layoutId != mLastLayoutId) {
			mWidgetFrame.removeAllViews();
		}
		try {
			if(mWidgetFrame.getChildCount() == 0 ) {
				mLastLayoutId = layoutId;			
				View widget = rv.apply(this, mWidgetFrame);
				mWidgetFrame.addView(widget);
			} else {
				rv.reapply(this, mWidgetFrame.getChildAt(0));
			}
		} catch(OutOfMemoryError oom) {
			Log.e(TAG, "oom", oom);
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.done_button) {
			onDone();
			Widget4x4Provider.clearContexts();
			boolean mediaRemoved = !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
			mWidgetProvider.pushUpdate(this, mPrefs, new int[]{ mAppWidgetId }, mediaRemoved, false, false, null);
			Widget4x4Provider.clearContexts();
			finish();
		}
	}
	
	protected void onDone() {
	}
}
