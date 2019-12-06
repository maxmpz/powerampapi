package com.maxmpz.poweramp.apiexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.maxmpz.poweramp.player.PowerampAPI;

/**
 * This receiver is registered in manifest and can be used to trigger some processing based on Poweramp broadcast intents.<br>
 * Here we just dump received intent. Out MainActivity class receives and processes those intents via own receiver which is registered and unregistered as needed.
 */
public class APIReceiver extends BroadcastReceiver {
	private static final String TAG = "APIReceiver";

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
