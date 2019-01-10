package com.poweramp.v3.sampleskin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class SkinInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_info);
    }

    public void startWithSampleSkin(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.StartupActivity");
        intent.putExtra("theme_pak", getPackageName());
        intent.putExtra("theme_id", R.style.SampleSkin);
        startActivity(intent);
        //Toast.makeText(this, R.string.skin_applied_msg, Toast.LENGTH_LONG).show(); // Enable toast if needed
        finish();
    }

    public void startWithSampleAAASkin(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.StartupActivity");
        intent.putExtra("theme_pak", getPackageName());
        intent.putExtra("theme_id", R.style.SampleSkinAAA);
        startActivity(intent);
        //Toast.makeText(this, R.string.skin_applied_msg, Toast.LENGTH_LONG).show(); // Enable toast if needed
        finish();
    }

    public void openPowerampThemeSettings(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setClassName("com.maxmpz.audioplayer", "com.maxmpz.audioplayer.SettingsActivity");
        intent.putExtra("open", "theme");
        intent.putExtra("theme_pak", getPackageName()); // If theme_pak/theme_id specified for open/theme, will scroll to/opens this skins settings
        intent.putExtra("theme_id", R.style.SampleSkin);
        startActivity(intent);
        finish();
    }

}
