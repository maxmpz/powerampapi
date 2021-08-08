package com.maxmpz.powerampproviderexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.DocumentsContract;
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

	public void openPAMusicFolders(View view) {
		if(LOG) Log.w(TAG, "openPAMusicFolders");

		Intent intent = new Intent();
		intent.setComponent(new ComponentName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.ACTIVITY_SETTINGS));
		intent.putExtra(PowerampAPI.Settings.EXTRA_OPEN_PATH, "folders_library/music_folders_button");
		intent.putExtra(PowerampAPI.Settings.EXTRA_NO_BACKSTACK, true);
		intent.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName());
		startActivity(intent);
	}

	// NOTE: depending on the "background" state of this app and Poweramp some methods may work or not in Android 8+ due to the background limitation.
	// As ACTION_SCAN_* actions rely on service running in Poweramp, they won't be executed if Poweramp itself is in the background.
	// sendScanThisAct below calls Poweramp via intermediate activity and that will allow scanning even if Poweramp is in the background, but
	// starting such activity from background application may be not possible.
	// It's also possible to start scan via startService (not demonstrated here) - subject to the similar limitations - can't be started from the background app.

	public void sendScan(View view) {
		if(LOG) Log.w(TAG, "sendScan");
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS)
			.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " rescan")
			.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName());

		try {
			// This won't work if Poweramp is on the background (not playing or Poweramp UI is not visible)
			//intent.setComponent(PowerampAPIHelper.getApiReceiverComponentName(this))
			//sendBroadcast(intent);

			// This won't work if this app or Poweramp is on the background (may throw)
			//intent.setComponent(PowerampAPIHelper.getScannerServiceComponentName(this));
			//startService(intent);

			// This won't work if started from background service, but works fine for starting from the activity
			intent.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
			startActivity(intent);

		} catch(Throwable th) {
			Log.w(TAG, "", th);
		}
	}

	public void sendScanThis(View view) {
		if(LOG) Log.w(TAG, "sendScanThis");
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS)
			.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " rescan")
			.putExtra(PowerampAPI.Scanner.EXTRA_PROVIDER, "com.maxmpz.powerampproviderexample") // Our provider authority (matches pak name in this case)
			.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName())
			.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
		try {
			startActivity(intent);
		} catch(Throwable th) {
			Log.w(TAG, "", th);
		}
	}

	public void sendScanThisSubdir(View view) {
		if(LOG) Log.w(TAG, "sendScanThisSubdir");
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS)
				.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " rescan")
				.putExtra(PowerampAPI.Scanner.EXTRA_PROVIDER, "com.maxmpz.powerampproviderexample") // Our provider authority (matches pak name in this case)
				// This rescans everything under root1
				// The path format is {@code /opaque-treeId/opaque-documentId/}:
				// - opaque-treeId is Uri.encoded treeId as returned from tree uri by Uri.encode(DocumentsContract.getTreeDocumentId(...))
				// - opaque-documentId is Uri.encoded documentId as returned from documentId uri by Uri.encode(DocumentsContract.getDocumentId(...))
				// Poweramp uses GLOB and just adds "*" to the EXTRA_PATH, thus, in our case, Poweramp will search for root1/* folders/files.
				// Last slash is required here so we avoid matching e.g. root123/.
				.putExtra(PowerampAPI.Scanner.EXTRA_PATH, "root1/")
				.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName())
				//.putExtra(PowerampAPI.Scanner.EXTRA_FAST_SCAN, true) // If specified, Poweramp will only scan tracks if parent folder lastModified changed
				.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
		try {
			startActivity(intent);
		} catch(Throwable th) {
			Log.w(TAG, "", th);
		}
	}

	public void sendScanThisSubdir2(View view) {
		if(LOG) Log.w(TAG, "sendScanThisSubdir2");
		Intent intent = new Intent(PowerampAPI.Scanner.ACTION_SCAN_DIRS)
				.putExtra(PowerampAPI.Scanner.EXTRA_CAUSE, getPackageName() + " rescan")
				.putExtra(PowerampAPI.Scanner.EXTRA_PROVIDER, "com.maxmpz.powerampproviderexample") // Our provider authority (matches pak name in this case)
				// This rescans everything under root1/Folder2 subpath.
				// The path format is {@code /opaque-treeId/opaque-documentId/}:
				// - opaque-treeId is Uri.encoded treeId as returned from tree uri by Uri.encode(DocumentsContract.getTreeDocumentId(...))
				// - opaque-documentId is Uri.encoded documentId as returned from documentId uri by Uri.encode(DocumentsContract.getDocumentId(...))
				// Poweramp uses GLOB and just adds "*" to the EXTRA_PATH.
				// Last slash is not added here, as we want to match  root1/root1%2FFolder2*
				// e.g. for file: @com.maxmpz.powerampproviderexample/root1/root1%2FFolder2%2Fdubstep-8.mp3.
				// Again, the EXTRA_PATH depends on how THIS provider defines and exposes paths/documentIds
				.putExtra(PowerampAPI.Scanner.EXTRA_PATH, Uri.encode("root1") + "/" + Uri.encode("root1/Folder2"))
				.putExtra(PowerampAPI.EXTRA_PACKAGE, getPackageName())
				//.putExtra(PowerampAPI.Scanner.EXTRA_FAST_SCAN, true) // If specified, Poweramp will only scan tracks if parent folder lastModified changed
				.setComponent(PowerampAPIHelper.getApiActivityComponentName(this));
		try {
			startActivity(intent);
		} catch(Throwable th) {
			Log.w(TAG, "", th);
		}
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mScanEventsReceiver);
		} catch(Throwable th) {	}
		super.onDestroy();
	}
}
