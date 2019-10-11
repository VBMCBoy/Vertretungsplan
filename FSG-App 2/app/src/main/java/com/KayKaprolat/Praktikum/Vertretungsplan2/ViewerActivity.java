package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.evernote.android.job.JobManager;
import com.google.firebase.analytics.FirebaseAnalytics;

public class ViewerActivity extends AppCompatActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Create the NotificationChannel, but only on API 26+ because
      // the NotificationChannel class is new and not in the support library
      CharSequence name = "Vertretungsplan";
      String description = "Erinnert an den Vertretungsplan";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel("ID_Vertretungsplan", name, importance);
      channel.setDescription(description);
      // Register the channel with the system
      NotificationManager notificationManager = (NotificationManager) getSystemService(
          Context.NOTIFICATION_SERVICE);
      notificationManager.createNotificationChannel(channel);
    }



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
    Boolean datacollection = prefs.getBoolean("Datenschutz", false);

    FirebaseAnalytics.getInstance(getApplicationContext())
        .setAnalyticsCollectionEnabled(datacollection);

    if (syncable) {

      JobManager.create(this).addJobCreator(new MyJobCreator());
      MySyncJob.scheduleJob();



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

    if (syncable) {

      JobManager.create(this).addJobCreator(new MyJobCreator());


    }

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

  public void notification(String title, String text, Integer ID, Integer priority) {
    Context context = getApplicationContext();
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,
        "ID_Vertretungsplan")
        .setSmallIcon(R.drawable.ic_stat_name).setContentTitle(title).setContentText(text);
    Intent resultIntent = new Intent(context, ViewerActivity.class);
    PendingIntent resultPendingIntent = PendingIntent
        .getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    mBuilder.setAutoCancel(true);
    if (1 == priority) {
      // hohe Priorität
      mBuilder.setVibrate(new long[]{1000, 1000, 1000});
    }

    notificationManager.notify(ID, mBuilder.build());
  }


}