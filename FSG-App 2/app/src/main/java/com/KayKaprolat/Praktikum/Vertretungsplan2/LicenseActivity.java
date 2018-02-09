package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.Activity;
import android.os.Bundle;

public class LicenseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      setContentView(R.layout.license_activity);
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
