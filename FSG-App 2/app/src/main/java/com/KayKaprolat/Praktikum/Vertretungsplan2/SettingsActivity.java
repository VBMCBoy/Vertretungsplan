package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingsActivity extends Activity {

  SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
      SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
          // your stuff here

        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.getFragmentManager().beginTransaction()
        .replace(R.id.content, new SettingsFragment())
        .commit();


  }


}
