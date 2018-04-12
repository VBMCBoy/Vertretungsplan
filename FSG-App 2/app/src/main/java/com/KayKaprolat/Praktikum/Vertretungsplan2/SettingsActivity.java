package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

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

    setContentView(com.KayKaprolat.Praktikum.Vertretungsplan2.R.layout.settings);
    Toolbar toolbar = findViewById(
        R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    getFragmentManager().beginTransaction()
        .replace(com.KayKaprolat.Praktikum.Vertretungsplan2.R.id.fragment_container,
            new SettingsFragment())
        .commit();
  }


}
