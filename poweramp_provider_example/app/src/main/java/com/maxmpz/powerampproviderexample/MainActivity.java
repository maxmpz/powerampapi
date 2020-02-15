package com.maxmpz.powerampproviderexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private static final boolean LOG = true;


	private BroadcastReceiver mScanEventsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			((TextView)findViewById(R.id.status)).setText(intent.getAction());
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PowerampAPI.Scanner.ACTION_DIRS_SCAN_STARTED);
		intentFilter.addAction(PowerampAPI.Scanner.ACTION_DIRS_SCAN_FINISHED);
		intentFilter.addAction(PowerampAPI.Scanner.ACTION_FAST_TAGS_SCAN_FINISHED);
		intentFilter.addAction(PowerampAPI.Scanner.ACTION_TAGS_SCAN_STARTED);
		intentFilter.addAction(PowerampAPI.Scanner.ACTION_TAGS_SCAN_FINISHED);
		registerReceiver(mScanEventsReceiver, intentFilter);
	}

	public void sendScan(View view) {
		if(LOG) Log.w(TAG, "sendScan");
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS);
		intent.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " rescan");
		intent.setComponent(PowerampAPIHelper.getScannerServiceComponentName(this));
		startService(intent);
	}

	public void sendScanThis(View view) {
		if(LOG) Log.w(TAG, "sendScanThis");
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS);
		intent.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " rescan");
		intent.putExtra(PowerampAPI.Scanner.EXTRA_PROVIDER, "com.maxmpz.powerampproviderexample"); // Our provider authority (matches pak name in this case)
		intent.setComponent(PowerampAPIHelper.getScannerServiceComponentName(this));
		startService(intent);
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mScanEventsReceiver);
		} catch(Throwable th) {	}
		super.onDestroy();
	}
}
