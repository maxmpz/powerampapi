package com.poweramp.sampleskin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SkinInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_info);
    }

    public void startPoweramp(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.StartupActivity");
        intent.putExtra("theme_pak", getPackageName());
        intent.putExtra("theme_id", R.style.SampleSkin);
        startActivity(intent);
        finish();
    }

    public void openPowerampThemeSettings(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.SettingsActivity");
        intent.putExtra("open", "theme");
        startActivity(intent);
        finish();
    }
}
