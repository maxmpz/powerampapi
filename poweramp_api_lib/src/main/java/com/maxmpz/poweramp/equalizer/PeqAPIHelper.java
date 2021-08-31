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

package com.maxmpz.poweramp.equalizer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;


public class PeqAPIHelper {
	private static final String TAG = "PeqAPIHelper";
	private static final boolean LOG = false;
	
	private static String sPak;
	private static int sBuild;
	private static ComponentName sApiReceiverComponentName;
	private static ComponentName sApiActivityComponentName;
	private static ComponentName sMilkScanServiceComponentName;


	/**
	 * THREADING: can be called from any thread, though double initialization is possible, but it's OK
	 * @return resolved and cached package name or null if it's not installed<br>
	 */
	public static String getPackageName(Context context) {
		String pak = sPak;
		if(pak == null) {
			ComponentName activityName = getApiActivityComponentName(context);
			if(activityName != null) {
				pak = sPak = activityName.getPackageName();
			}
		}
		return pak;
	}

	/**
	 * NOTE: this is API activity - invisible activity to receive API commands. This one doesn't show any UI.
	 * THREADING: can be called from any thread, though double initialization is possible, but it's OK
	 * @return resolved and cached API activity component name, or null if not installed
	 */
	public static ComponentName getApiActivityComponentName(Context context) {
		ComponentName componentName = sApiActivityComponentName;
		if(componentName == null) {
			try {
				ResolveInfo info = context.getPackageManager().resolveActivity(new Intent(PeqAPI.ACTION_API_COMMAND), 0);
				if(info != null && info.activityInfo != null) {
					componentName = sApiActivityComponentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
				}
			} catch(Throwable th) {
				Log.e(TAG, "", th);
			}
		}
		return componentName;
	}

	/**
	 * THREADING: can be called from any thread, though double initialization is possible, but it's OK
	 * @return resolved and cached milk scanner component name, or null if not installed
	 */
	public static ComponentName getMilkScannerServiceComponentName(Context context) {
		ComponentName componentName = sMilkScanServiceComponentName;
		if(componentName == null) {
			try {
				ResolveInfo info = context.getPackageManager().resolveService(new Intent(PeqAPI.MilkScanner.ACTION_SCAN).setPackage(getPackageName(context)), 0);
				if(info != null && info.serviceInfo != null) {
					componentName = sMilkScanServiceComponentName = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
				}
			} catch(Throwable th) {
				Log.e(TAG, "", th);
			}
		}
		return componentName;
	}

	/**
	 * THREADING: can be called from any thread, though double initialization is possible, but it's OK
	 * @return resolved and cached API receiver component name, or null if not installed
	 */
	public static ComponentName getApiReceiverComponentName(Context context) {
		ComponentName componentName = sApiReceiverComponentName;
		if(componentName == null) {
			String pak = getPackageName(context);
			if(pak != null) {
				componentName = sApiReceiverComponentName = new ComponentName(pak, PeqAPI.API_RECEIVER_NAME);
			}
		}
		return componentName;
	}

	/**
	 * THREADING: can be called from any thread, though double initialization is possible, but it's OK
	 * @return resolved and cached build number<br>
	 */
	public static int getPowerampBuild(Context context) {
		if(sBuild == 0) {
			String pak = getPackageName(context);
			if(pak != null) {
				try {
					PackageInfo pi = context.getPackageManager().getPackageInfo(pak, 0);
					sBuild = pi.versionCode > 1000 ? pi.versionCode / 1000 : pi.versionCode;
				} catch(Throwable th) {
					Log.e(TAG, "", th);
				}
			}
		}
		return sBuild;
	}
	
	/**
	 * THREADING: can be called from any thread<br>
	 * @return intent to send with sendPAIntent
	 */
	public static Intent newAPIIntent(Context context) {
		return new Intent(PeqAPI.ACTION_API_COMMAND);
	}


	/**
	 * THREADING: can be called from any thread<br>
	 */
	public static void sendPAIntent(Context context, Intent intent) {
		sendPAIntent(context, intent, false);
	}

	/**
	 * THREADING: can be called from any thread
	 * @param sendToActivity if true, we're sending intent to the activity
	 */
	public static void sendPAIntent(Context context, Intent intent, boolean sendToActivity) {
		intent.putExtra(PeqAPI.EXTRA_PACKAGE, context.getPackageName());
		if(sendToActivity) {
			intent.setComponent(getApiActivityComponentName(context));
			if(!(context instanceof Activity)) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(intent);
		} else {
			intent.setComponent(getApiReceiverComponentName(context));
			context.sendBroadcast(intent);
		}
	}
}
