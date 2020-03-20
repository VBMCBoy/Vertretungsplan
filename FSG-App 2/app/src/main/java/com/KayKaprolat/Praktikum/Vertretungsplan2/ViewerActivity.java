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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

public class ViewerActivity extends AppCompatActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            final String name = "Vertretungsplan";
            final String description = "Erinnert an den Vertretungsplan";
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel("ID_Vertretungsplan", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            final NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // ActionBar bauen
        setContentView(R.layout.viewer);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Heute"));
        tabLayout.addTab(tabLayout.newTab().setText("Morgen"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {

            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // use shared preferences instead
        final String wert_PW = prefs.getString("PW", "");
        final String wert_name = prefs.getString("BN", "");
        final String wert_klasse = prefs.getString("KL", "");
        final boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
        final boolean datacollection = prefs.getBoolean("Datenschutz", false);

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
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
        final boolean datacollection = prefs.getBoolean("Datenschutz", true);

        FirebaseAnalytics.getInstance(getApplicationContext())
                .setAnalyticsCollectionEnabled(datacollection);

        if (syncable) {
            JobManager.create(this).addJobCreator(new MyJobCreator());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public void Menu_Einstellungen(final MenuItem item) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    public void Menu_Licenses(final MenuItem item) {
        final Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }


    public void Menu_About(final MenuItem item) {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }


    public void Menu_Datenschutz(final MenuItem item) {
        final Intent intent = new Intent(this, DatenschutzActivity.class);
        startActivity(intent);
    }

    public void notification(final String title, final String text, final Integer ID, final Integer priority) {
        final Context context = getApplicationContext();
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,
                "ID_Vertretungsplan")
                .setSmallIcon(R.drawable.ic_stat_name).setContentTitle(title).setContentText(text);
        final Intent resultIntent = new Intent(context, ViewerActivity.class);
        final PendingIntent resultPendingIntent = PendingIntent
                .getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        final NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setAutoCancel(true);
        if (1 == priority) {
            // hohe Priorität
            mBuilder.setVibrate(new long[]{1000, 1000, 1000});
        }

        notificationManager.notify(ID, mBuilder.build());
    }


}