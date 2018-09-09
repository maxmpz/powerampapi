package com.poweramp.v3.vispresets.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    public void startWithVisPresets(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.StartupActivity");
        intent.putExtra("vis_presets_pak", getPackageName());
        startActivity(intent);
        finish();
    }

    public void openPowerampVisSettings(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.SettingsActivity");
        intent.putExtra("open", "vis");
        intent.putExtra("vis_presets_pak", getPackageName()); // If vis_presets_pak specified for open/theme, will scroll presets list to this apk entry
        startActivity(intent);
        finish();
    }

}
