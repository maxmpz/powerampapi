/*
Copyright (C) 2011-2020 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

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
