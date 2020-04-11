package com.poweramp.v3.vispresets.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.maxmpz.poweramp.player.PowerampAPI;
import com.maxmpz.poweramp.player.PowerampAPIHelper;

import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class InfoActivity extends Activity {
    private static final String TAG = "InfoActivity";

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

    public void startWithVisPresets(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClassName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.Settings.ACTIVITY_SETTINGS)
                .putExtra(PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK, getPackageName())
                ;
        startActivity(intent);
        finish();
    }

    public void openPowerampVisSettings(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setClassName(PowerampAPIHelper.getPowerampPackageName(this), PowerampAPI.Settings.ACTIVITY_SETTINGS)
                .putExtra(PowerampAPI.Settings.EXTRA_OPEN, PowerampAPI.Settings.OPEN_VIS)
                .putExtra(PowerampAPI.Settings.EXTRA_VIS_PRESETS_PAK, getPackageName()) // If vis_presets_pak specified for open/theme, will scroll presets list to this apk entry
                ;
        startActivity(intent);
        finish();
    }

    public void sendPreset(View view) {
        // Issue SET_VIS_PRESET
        
        Intent intent = new Intent(PowerampAPI.ACTION_API_COMMAND)
                        .setComponent(PowerampAPIHelper.getApiActivityComponentName(this))
                        .putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.SET_VIS_PRESET)
                        .putExtra(PowerampAPI.EXTRA_NAME, "VisPresetsExample - Test Preset.milk")
                        .putExtra(PowerampAPI.EXTRA_DATA, ((EditText)findViewById(R.id.edit)).getText().toString()); // NOTE: strictly String type is expected
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
                        .putExtra(PowerampAPI.COMMAND, PowerampAPI.Commands.RESUME)
                ;
                startActivity(intent);
            }
        });
    }
}
