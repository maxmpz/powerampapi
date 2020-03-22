package com.poweramp.v3.sampleskin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SkinInfoActivity extends Activity {
	private static final String TAG = "SkinInfoActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_skin_info);
	}

	/**
	 * @return resolved Poweramp package name or null if not installed
	 * NOTE: can be called from any thread, though double initialization is possible, but it's OK
	 */
	public static String getPowerampPackageName(Context context) {
		try {
			ResolveInfo info = context.getPackageManager().resolveService(new Intent("com.maxmpz.audioplayer.API_COMMAND"), 0);
			if(info != null && info.serviceInfo != null) {
				return info.serviceInfo.packageName;
			}
		} catch(Throwable th) {
			Log.e(TAG, "", th);
		}
		return null;
	}

	public void startWithSampleSkin(View view) {
		String pak = getPowerampPackageName(this);
		if(pak == null) {
			Toast.makeText(this, R.string.skin_poweramp_not_installed, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_MAIN)
			.setClassName(pak, "com.maxmpz.audioplayer.StartupActivity")
			.putExtra("theme_pak", getPackageName())
			.putExtra("theme_id", R.style.SampleSkin);
		startActivity(intent);
		//Toast.makeText(this, R.string.skin_applied_msg, Toast.LENGTH_LONG).show(); // Enable toast if needed
		finish();
	}

	public void startWithSampleAAASkin(View view) {
		String pak = getPowerampPackageName(this);
		if(pak == null) {
			Toast.makeText(this, R.string.skin_poweramp_not_installed, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_MAIN)
			.setClassName(pak, "com.maxmpz.audioplayer.StartupActivity")
			.putExtra("theme_pak", getPackageName())
			.putExtra("theme_id", R.style.SampleSkinAAA);
		startActivity(intent);
		//Toast.makeText(this, R.string.skin_applied_msg, Toast.LENGTH_LONG).show(); // Enable toast if needed
		finish();
	}

	public void openPowerampThemeSettings(View view) {
		String pak = getPowerampPackageName(this);
		if(pak == null) {
			Toast.makeText(this, R.string.skin_poweramp_not_installed, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_MAIN)
			.setClassName(pak, "com.maxmpz.audioplayer.SettingsActivity")
			.putExtra("open", "theme")
			.putExtra("theme_pak", getPackageName()) // If theme_pak/theme_id specified for open/theme, will scroll to/opens this skins settings
			.putExtra("theme_id", R.style.SampleSkin);
		startActivity(intent);
		finish();
	}

}
