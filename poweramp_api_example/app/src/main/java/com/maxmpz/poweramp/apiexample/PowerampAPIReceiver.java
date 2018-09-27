package com.maxmpz.poweramp.apiexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

public class PowerampAPIReceiver extends BroadcastReceiver {
	private static final String TAG = "PowerampAPIReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action != null) {
			switch(action) {
				case PowerampAPI.ACTION_STATUS_CHANGED_EXPLICIT:
					MainActivity.debugDumpIntent(TAG, "ACTION_STATUS_CHANGED_EXPLICIT", intent);
					break;

				case PowerampAPI.ACTION_TRACK_CHANGED_EXPLICIT:
					MainActivity.debugDumpIntent(TAG, "ACTION_TRACK_CHANGED_EXPLICIT", intent);
					break;

				default:
					MainActivity.debugDumpIntent(TAG, "UNKNOWN", intent);
					break;
			}
		}
	}
}
