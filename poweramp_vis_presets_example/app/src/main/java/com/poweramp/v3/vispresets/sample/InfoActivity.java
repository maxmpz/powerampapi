package com.poweramp.v3.vispresets.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class InfoActivity extends Activity {
	private static final String TAG = "InfoActivity";
	private static final boolean LOG = true;

	private static final int REQUEST_ACCESS_MILK_PRESETS = 1;

	private ArrayList<String> mPushedFiles = new ArrayList<String>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

		EditText edit = findViewById(R.id.edit);

		// Inject some preset as text
		try(InputStreamReader isr = new InputStreamReader(getResources().getAssets().open("milk_presets/Example - example-bars-vertical.milk"), StandardCharsets.UTF_8)) {
			final StringBuilder out = new StringBuilder();
			int charsRead;
			final int bufferSize = 1024;
			final char[] buffer = new char[bufferSize];
			while((charsRead = isr.read(buffer, 0, buffer.length)) > 0) {
				out.append(buffer, 0, charsRead);
			}
			edit.setText(out);

		} catch(IOException e) {
			Log.e(TAG, "", e);
		}
	}

	/**
	 * This opens Poweramp and rescans appropriate preset APK - as specified in {@link PowerampAPI.Settings#EXTRA_VIS_PRESETS_PAK} extra
	 */
	public void startWithVisPresets(View view) {
		Intent intent = new Intent(Intent.ACTION_MAIN)
				.setClassName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.ACTIVITY_STARTUP)
				.putExtra(PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK, getPackageName());
		startActivity(intent);
		finish();
	}

	/**
	 * This opens Poweramp settings and scrolls to the appropriate preset APK - as specified in {@link PowerampAPI.Settings#EXTRA_VIS_PRESETS_PAK} extra,
	 * but this doesn't rescan presets
	 */
	public void openPowerampVisSettings(View view) {
		Intent intent = new Intent(Intent.ACTION_MAIN)
				.setClassName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.Settings.ACTIVITY_SETTINGS)
				.putExtra(PowerampAPI.Settings.EXTRA_OPEN, PowerampAPI.Settings.OPEN_VIS)
				.putExtra(PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK, getPackageName()) // If vis_presets_pak specified for open/theme, will scroll presets list to this apk entry
				;
		startActivity(intent);
		finish();
	}

	/**
	 * This asks Poweramp to rescan all presets. At this moment (build 867) all presets are rescanned, not just the given package.<br>
	 * This works only if current application is foreground, meaning this can't be used from the background service (Android 8+), as
	 * Android will block background service execution.
	 */
	public void rescanVisPresets(View view) {
		Intent intent = new Intent(PowerampAPI.MilkScanner.ACTION_SCAN)
				.setComponent(PowerampAPIHelper.getMilkScannerServiceComponentName(this))
				.putExtra(PowerampAPI.MilkScanner.EXTRA_CAUSE, "Manual rescan")
				.putExtra(PowerampAPI.MilkScanner.EXTRA_PACKAGE, getPackageName());
		startService(intent);
	}

	public void sendPreset(View view) {
		// Issue SET_VIS_PRESET

		Intent intent = new Intent(PowerampAPI.ACTION_API_COMMAND)
				.setComponent(PowerampAPIHelper.getApiActivityComponentName(this))
				.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.SET_VIS_PRESET)
				.putExtra(PowerampAPI.EXTRA_NAME, "VisPresetsExample - Test Preset.milk")
				.putExtra(PowerampAPI.EXTRA_DATA, ((EditText) findViewById(R.id.edit)).getText().toString()); // NOTE: strictly String type is expected
		;
		startActivity(intent);

		// NOTE: we can't immediately do same action/component startActivity immediately, so delay it a bit via post
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				Intent intent;
				// Let's force  vis mode to VIS + UI

				// NOTE: Poweramp won't change interface in reflection to just changing this preference. But as we're currently inside other activity,
				// re-opening Poweramp UI will re-read this preference. Though, this may fail for e.g. split screen when both activities are always on top
				Bundle request = new Bundle();
				request.putInt("vis_mode", PowerampAPI.Settings.PreferencesConsts.VIS_MODE_VIS_W_UI); // See PowerampAPI.Settings.Preferences
				getContentResolver().call(PowerampAPI.ROOT_URI, PowerampAPI.CALL_SET_PREFERENCE, null, request);

				// Now open Poweramp UI main screen

				intent = new Intent(PowerampAPI.ACTION_OPEN_MAIN)
						.setClassName(PowerampAPIHelper.getPowerampPackageName(InfoActivity.this), PowerampAPI.ACTIVITY_STARTUP)
				;
				startActivity(intent);

				// And command it to play something

				intent = new Intent(PowerampAPI.ACTION_API_COMMAND)
						.setComponent(PowerampAPIHelper.getApiActivityComponentName(InfoActivity.this))
						.putExtra(PowerampAPI.EXTRA_COMMAND, PowerampAPI.Commands.RESUME)
				;
				startActivity(intent);
			}
		});
	}

	public void listMilkPresets(View view) {
		if(Build.VERSION.SDK_INT < 23) {
			// We shouldn't do this for Androids below 10. Instead directly access files as needed
			// Poweramp still supports this functionality for lower Androids, but due to the permission access used, we may ask it directly
			// here for Android 5
			listMilkPresetsImpl();
			return;
		}

		if(checkSelfPermission(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS) != PackageManager.PERMISSION_GRANTED) {
			if(LOG) Log.w(TAG, "listMilkPresets requesting permission");
			requestPermissions(new String[] { PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS }, REQUEST_ACCESS_MILK_PRESETS);
		} else {
			listMilkPresetsImpl();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(requestCode == REQUEST_ACCESS_MILK_PRESETS && grantResults != null && grantResults.length > 0
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED
		) {
			if(LOG) Log.w(TAG, "onRequestPermissionsResult PERMISSION_GRANTED");
			listMilkPresetsImpl();

		} else if(LOG) Log.e(TAG, "onRequestPermissionsResult !PERMISSION_GRANTED requestCode=" + requestCode +
								" permissions=" + Arrays.toString(permissions) + " grantResults=" + Arrays.toString(grantResults));
	}

	private String getExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if(lastDot == -1 || lastDot == filename.length() - 1) {
			return "";
		}
		return filename.substring(lastDot + 1);
	}

	public void listMilkPresetsImpl() {
		if(LOG) Log.w(TAG, "listMilkPresetsImpl");
		final ArrayList<String> presets = new ArrayList<>();
		final ArrayList<String> textures = new ArrayList<>();
		final ArrayList<String> zips = new ArrayList<>();

		// The path parameter also accepts glob patterns, e.g. content://com.maxmpz.audioplayer.milk_presets/*.milk,
		// To quickly query for the single file, you can also use uris like: content://com.maxmpz.audioplayer.milk_presets/single_file
		// NOTE: cols are always ignored, the following colums are always retuned:
		//   Document.COLUMN_DISPLAY_NAME, Document.COLUMN_LAST_MODIFIED, Document.COLUMN_SIZE,
		// NOTE: ? symbol requires escaping (%3F)

		Uri uri = PowerampAPI.MILK_PRESETS_URI;
		//Uri uri = Uri.parse("content://com.maxmpz.audioplayer.milk_presets");
		//Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("*.milk").build();
		//Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("*.{jpg|jpeg|png|tga|bmp}").build();
		//Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("file.???").build();
		//Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath("*.???").build();

		try(Cursor c = getContentResolver().query(uri, null, null, null, null)) {
			while(c != null && c.moveToNext()) {
				String fileName = c.getString(0);
				long lastModified = c.getLong(1);
				long size = c.getLong(2);

				if(LOG) Log.w(TAG, "listMilkPresetsImpl fileName=" + fileName + " lastModified=" + lastModified + " size=" + size);

				String ext = getExtension(fileName).toLowerCase(Locale.ROOT); // NOTE: Poweramp is not case sensitive for presets
				switch(ext) {
					case "zip":
						Log.w(TAG, "listMilkPresetsImpl FOUND zip=" + fileName);
						zips.add(fileName);
						break;
					case "prjm":
					case "milk":
						Log.w(TAG, "listMilkPresetsImpl FOUND milk preset=" + fileName);
						presets.add(fileName);
						break;
					case "jpeg":
					case "jpg":
					case "png":
					case "tga":
					case "bmp":
						Log.w(TAG, "listMilkPresetsImpl FOUND texture=" + fileName);
						textures.add(fileName);
						break;
				}
			}

			new AlertDialog.Builder(this)
					.setTitle("listMilkPresets")
					.setMessage(
							"Presets: " + presets.size() + "\n" +
							"Zips: " + zips.size() + "\n" +
							"Textures: " + textures.size() + "\n"
					)
					.setPositiveButton(android.R.string.ok, null)
					.show();

		} catch(Throwable th) {
			Log.e(TAG, "", th);
		}
	}


	public void pushFile(View view) {
		// Assume we asked for permission earlier in list milk presets
		if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "pushFile !permission");
			return;
		}

		String newFile = "TestPreset - " + System.currentTimeMillis() + ".milk";

		Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath(newFile).build();
		try(ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "wt")) {
			if(pfd != null) {
				try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pfd.getFileDescriptor()))) {
					EditText edit = findViewById(R.id.edit);
					writer.write(edit.getText().toString());
				}
			}

			// Ask Poweramp to rescan
			rescanVisPresets(null);

			mPushedFiles.add(newFile);

			new AlertDialog.Builder(this)
					.setTitle("pushFile")
					.setMessage("Pushed file=" + newFile)
					.setPositiveButton(android.R.string.ok, null)
					.show();

		} catch(Throwable th) {
			Log.e(TAG, "", th);
		}
	}
	
	public void deleteFile(View view) {
		// Assume we asked for permission earlier in list milk presets
		if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(PowerampAPI.PERMISSION_ACCESS_MILK_PRESETS) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "deleteFile !permission");
			return;
		}

		if(mPushedFiles.size() == 0) { // We need some files previously pushed in this app session
			new AlertDialog.Builder(this)
					.setTitle("deleteFile")
					.setMessage("No pushed files to delete yet")
					.setPositiveButton(android.R.string.ok, null)
					.show();
			return;
		}

		String fileToDelete = mPushedFiles.get(mPushedFiles.size() - 1); // Delete last file pushed
		mPushedFiles.remove(mPushedFiles.size() - 1);

		Uri uri = PowerampAPI.MILK_PRESETS_URI.buildUpon().appendPath(fileToDelete).build();
		try{
			int deleted = getContentResolver().delete(uri, null, null);
			if(deleted != 0) {
				// Ask Poweramp to rescan
				rescanVisPresets(null);
			}
			new AlertDialog.Builder(this)
					.setTitle("deleteFile")
					.setMessage(deleted != 0 ? "Deleted file=" + fileToDelete : "No files deleted")
					.setPositiveButton(android.R.string.ok, null)
					.show();

		} catch(Throwable th) {
			Log.e(TAG, "", th);
		}
	}

}
