package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.analytics.FirebaseAnalytics;

public class ViewerActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(
        new GooglePlayDriver(getApplicationContext()));

    // ActionBar bauen

    setContentView(R.layout.viewer);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    TabLayout tabLayout = findViewById(R.id.tab_layout);
    tabLayout.addTab(tabLayout.newTab().setText("Heute"));
    tabLayout.addTab(tabLayout.newTab().setText("Morgen"));
    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

    final ViewPager viewPager = findViewById(R.id.pager);
    PagerAdapter adapter = new PagerAdapter
        (getSupportFragmentManager(), tabLayout.getTabCount());
    viewPager.setAdapter(adapter);
    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {

      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {

      }
    });

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // use shared preferences instead
    String wert_PW = prefs.getString("PW", "");
    String wert_name = prefs.getString("BN", "");
    String wert_klasse = prefs.getString("KL", "");
    Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
    Boolean datacollection = prefs.getBoolean("Datenschutz", true);

    FirebaseAnalytics.getInstance(getApplicationContext())
        .setAnalyticsCollectionEnabled(datacollection);

    if (syncable) {

      Job myJob = dispatcher.newJobBuilder().setService(MyJobService.class).setTag("mytag")
          .setRecurring(true).setLifetime(
              Lifetime.FOREVER).setReplaceCurrent(true)
          .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR).setConstraints(
              Constraint.ON_ANY_NETWORK).setTrigger(
              Trigger.executionWindow(3600, 3600 + 1800)) // jede Stunde mit einem Fenster von 1/2 h
          .build();

      dispatcher.mustSchedule(myJob);


    } else {
      dispatcher.cancelAll();
    }

    //prüfen ob leer

    if ("".equals(wert_PW) || "".equals(wert_name) || "".equals(wert_klasse)) {
      // Toast
      Toast.makeText(getApplicationContext(),
          "Bitte stellen Sie Klasse / Lehrer, Benutzername und Passwort ein.", Toast.LENGTH_LONG)
          .show();
      // Einstellungen öffnen
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
    }


  }

  @Override
  protected void onResume() {
    super.onResume();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
    Boolean datacollection = prefs.getBoolean("Datenschutz", true);

    FirebaseAnalytics.getInstance(getApplicationContext())
        .setAnalyticsCollectionEnabled(datacollection);

    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(
        new GooglePlayDriver(getApplicationContext()));

    if (syncable) {

      Job myJob = dispatcher.newJobBuilder().setService(MyJobService.class).setTag("mytag")
          .setRecurring(true).setLifetime(
              Lifetime.FOREVER).setReplaceCurrent(true)
          .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR).setConstraints(
              Constraint.ON_ANY_NETWORK).setTrigger(
              Trigger.executionWindow(3600, 3600 + 1800)) // jede Stunde mit einem Fenster von 1/2 h
          .build();

      dispatcher.mustSchedule(myJob);


    } else {
      dispatcher.cancelAll();
    }

  }


  public void notification(String title, String text) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, "ID_Vertretungsplan")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(text);
    Intent resultIntent = new Intent(this, ViewerActivity.class);
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    mBuilder.setContentIntent(resultPendingIntent);
    int ID = 1;
    NotificationManager notificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);
    notificationManager.notify(ID, mBuilder.build());
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  public void Menu_Einstellungen(MenuItem item) {

    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);

  }


  public void Menu_Licenses(MenuItem item) {

    Intent intent = new Intent(this, LicenseActivity.class);
    startActivity(intent);
  }


  public void Menu_About(MenuItem item) {
    Intent intent = new Intent(this, AboutActivity.class);
    startActivity(intent);

  }


  public void Menu_Datenschutz(MenuItem item) {
    Intent intent = new Intent(this, DatenschutzActivity.class);
    startActivity(intent);

  }


}