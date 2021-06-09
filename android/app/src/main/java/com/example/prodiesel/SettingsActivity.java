package com.example.prodiesel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.server_url_file), Context.MODE_PRIVATE);
        ((EditText)findViewById(R.id.server_url)).setText(sharedPref.getString("url_server", ""));
        //((CheckBox)findViewById(R.id.full_screen)).setChecked(sharedPref.getBoolean("fullscreen", true));
    }

    public void saveServerUrl(View view){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.server_url_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("url_server", ((EditText)findViewById(R.id.server_url)).getText().toString());
        //editor.putBoolean("fullscreen", ((CheckBox)findViewById(R.id.full_screen)).isChecked());
        editor.apply();
    }

    public void closeSettings(View view) {
        finish();
    }

    public void exitApplication(View view) {
        finishAffinity();
        getPackageManager().clearPackagePreferredActivities(getPackageName());
        stopLockTask();
        System.exit(0);
    }
}