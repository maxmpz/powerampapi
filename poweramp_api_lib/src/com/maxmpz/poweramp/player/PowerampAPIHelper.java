package com.maxmpz.poweramp.player;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class PowerampAPIHelper {
	public static void startPAService(Context context, Intent intent) {
		intent.setComponent(PowerampAPI.PLAYER_SERVICE_COMPONENT_NAME);
		if(Build.VERSION.SDK_INT >= 26) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}
}
