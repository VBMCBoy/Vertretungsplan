package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String wert_PW = prefs.getString("PW", "");
        final String wert_name = prefs.getString("BN", "");
        final String wert_klasse = prefs.getString("KL", "");
        final Boolean LEHRER = prefs.getBoolean("LE", false);


    }


}
