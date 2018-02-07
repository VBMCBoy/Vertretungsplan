package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ViewerActivity extends Activity {

  public static final String ACCOUNT_TYPE = "sachsen.schule";

  public static final String ACCOUNT = "default_account";

  public static final String AUTHORITY = "com.KayKaprolat.Praktikum.Vertretungsplan2.provider";   // provider oder StubProvider?

  ContentResolver mResolver;

  Account account;

  public static Account CreateSyncAccount(Context context) {
    Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
    AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
    if (accountManager.addAccountExplicitly(newAccount, null, null)) {
      return newAccount;
    } else {
      // ein Fehler ist aufgetreten
      return newAccount;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    account = CreateSyncAccount(getApplicationContext());

    getActionBar().setTitle("Vertretungsplan");

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // use shared preferences instead
    final String wert_PW = prefs.getString("PW", "");
    final String wert_name = prefs.getString("BN", "");
    final String wert_klasse = prefs.getString("KL", "");
    final long intervall = Long.parseLong(prefs.getString("delay", "60"));
    final Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);

    mResolver = getContentResolver();

    if (syncable) {
      if (ContentResolver.getSyncAutomatically(account, AUTHORITY)) {
        if (!(ContentResolver.isSyncPending(account, AUTHORITY))) {
          ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, intervall * 60);
        }
      } else {
        ContentResolver.setMasterSyncAutomatically(true);
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
      }

    } else {
      ContentResolver.setMasterSyncAutomatically(false);
      ContentResolver.setSyncAutomatically(account, AUTHORITY, false);
      ContentResolver.cancelSync(account, AUTHORITY);
    }

    //prüfen ob leer

    if (wert_PW.equals("") || wert_name.equals("") || wert_klasse.equals("")) {
      // Toast
      Toast.makeText(getApplicationContext(),
          "Bitte stellen Sie Klasse / Lehrer, Benutzername und Passwort ein.", Toast.LENGTH_LONG)
          .show();
      // Einstellungen öffnen
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
    } else {

      setContentView(R.layout.viewer); //Layout starten
      //Variablen festlegen
      final WebView webView = (WebView) findViewById(R.id.webView1);

      Laden(webView, true, wert_klasse, wert_name, wert_PW, false);


    }


  }

  @Override
  protected void onResume() {
    super.onResume();
    account = CreateSyncAccount(this);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
    final long intervall = Long.parseLong(prefs.getString("delay", "60"));
    mResolver = getContentResolver();

    if (syncable) {
      if (ContentResolver.getSyncAutomatically(account, AUTHORITY)) {
        if (!(ContentResolver.isSyncPending(account, AUTHORITY))) {
          ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, intervall * 60);
        }
      } else {
        ContentResolver.setMasterSyncAutomatically(true);
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
      }

    } else {
      ContentResolver.setMasterSyncAutomatically(false);
      ContentResolver.setSyncAutomatically(account, AUTHORITY, false);
      ContentResolver.cancelSync(account, AUTHORITY);
    }


  }

  public void notification(String title, String text) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
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
        NOTIFICATION_SERVICE);
    notificationManager.notify(ID, mBuilder.build());
  }


  public void BtnHeuteClick(View view) {

    // Heute
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    // use shared preferences instead
    final String wert_PW = prefs.getString("PW", " ");
    final String wert_name = prefs.getString("BN", " ");
    final String wert_klasse = prefs.getString("KL", " ");

    setContentView(R.layout.viewer);
    final WebView webView = (WebView) findViewById(R.id.webView1);

    Laden(webView, true, wert_klasse, wert_name, wert_PW, false);

  }

  public void BtnMorgenClick(View view) {
    // Morgen
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final String wert_PW = prefs.getString("PW", " ");
    final String wert_name = prefs.getString("BN", " ");
    final String wert_klasse = prefs.getString("KL", " ");
    final Boolean Lehrer = prefs.getBoolean("Lehrer", false);

    setContentView(R.layout.viewer);
    final WebView webView = (WebView) findViewById(R.id.webView1);

    Laden(webView, false, wert_klasse, wert_name, wert_PW, false);
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


  private void Laden(final WebView webView, final Boolean heute, final String wert_klasse,
      final String wert_name, final String wert_PW, final Boolean headless) {

    if (heute) {
      new Thread() {

        @Override
        public void run() {
          URL url;
          HttpURLConnection urlConnection = null;

          try {
            Authenticator.setDefault(new Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(wert_name,
                    wert_PW.toCharArray());

              }
            });

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            // heute

            switch (day) {
              case 1: // Sonntag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 2:  // Montag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 3:// Dienstag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm");
                break;
              case 4:// Mittwoch
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
                break;
              case 5:  // Donnerstag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm");
                break;
              case 6: // Freitag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm");
                break;
              case 7: // Samstag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              default:
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
            }

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(
                urlConnection.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
              baos.write(buffer, 0, count);
            }

            final String Plan = new String(baos.toByteArray(),
                "windows-1252");
            // das ist grauenhaft
            String a = cache(true).replaceAll(" ", "");
            String b = a.replaceAll("\r", "");
            String c = Plan.replaceAll("\r", "");
            String d = c.replaceAll(" ", "");
            if (!(b.equals(d))) {   // wenn der aktuelle Plan anders als der Alte ist
              if (headless) {

              } else {
                ViewerActivity.this.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(getApplicationContext(), "Der Vertretungsplan ist neu.",
                        Toast.LENGTH_LONG).show();
                  }
                });
              }
            }
            speichern(Plan, true);

            if (!headless) {
              toWebview(webView, Plan, wert_klasse, true,
                  day); // WebView, HTML, Klasse bzw Lehrer, heute?, heute Freitag?
            }


          } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.",
                    Toast.LENGTH_LONG).show();
              }
            });

          } finally {
            if (urlConnection != null) {
              urlConnection.disconnect();
            }


          }

        }

      }.

          start();
    } else {
// morgen
      new Thread() {

        @Override
        public void run() {
          URL url;
          HttpURLConnection urlConnection = null;

          try {
            Authenticator.setDefault(new Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(wert_name,
                    wert_PW.toCharArray());

              }
            });

            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            switch (day) {
              case 1: // Sonntag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 2:  // Montag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm");
                break;
              case 3:// Dienstag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
                break;
              case 4:// Mittwoch
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm");
                break;
              case 5:  // Donnerstag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm");
                break;
              case 6: // Freitag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 7: // Samstag
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              default:
                url = new URL(
                    "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
            }

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(
                urlConnection.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
              baos.write(buffer, 0, count);
            }

            final String Plan = new String(baos.toByteArray(),
                "windows-1252");

            String a = cache(false).replaceAll(" ", "");
            String b = a.replaceAll("\r", "");
            String c = Plan.replaceAll("\r", "");
            String d = c.replaceAll(" ", "");
            if (!(b.equals(d))) {   // wenn der aktuelle Plan anders als der Alte ist
              if (headless) {

              } else {
                ViewerActivity.this.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(getApplicationContext(), "Der Vertretungsplan ist neu.",
                        Toast.LENGTH_LONG).show();
                  }
                });
              }
            }
            speichern(Plan, false);

            if (!headless) {
              toWebview(webView, Plan, wert_klasse, false, day);
            }


          } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.",
                    Toast.LENGTH_LONG).show();
              }
            });

          } finally {
            if (urlConnection != null) {
              urlConnection.disconnect();
            }

          }

        }

      }.

          start();


    }
  }


  private void speichern(String string, Boolean heute) {
    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    if (heute) {
      editor.putString("cache_website_heute", string);
    } else {
      editor.putString("cache_website_morgen", string);
    }

    editor.commit();


  }

  private String cache(Boolean heute) {
    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    if (heute) {
      String cache = sharedPref.getString("cache_website_heute", "");
      return cache;
    } else {
      String cache = sharedPref.getString("cache_website_morgen", "");
      return cache;
    }

  }

  private void toWebview(final WebView webView, final String Plan, final String wert_klasse,
      final Boolean heute, final Integer day) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Document doc2 = Jsoup.parse(Plan, "windows-1252");
        if (wert_klasse.matches(".*\\d+.*")) { // true = ist kein Lehrer
          Elements TEST = doc2.select("tr:has(td:eq(1):contains(" + wert_klasse + "))");
          TEST.attr("bgcolor", "FFF007");
        } else {
          Elements TEST = doc2.select("tr:contains(" + wert_klasse + ")");
          TEST.attr("bgcolor", "FFF007");
        }
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadData(doc2.html(), "text/html; charset=UTF-8",
            null);

        Datum_richtig(Plan, heute, day);    // macht einen Toast wenn das Datum nicht stimmt

      }
    });
  }

  private void Datum_richtig(String Plan, Boolean heute, Integer day) {
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

    Calendar c = Calendar.getInstance();
    Date date = c.getTime();

    if (heute) {
      switch (day) {
        case 1: // Sonntag -- Datum +1
          c.setTime(date);
          c.add(Calendar.DATE, 1);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(), "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 2: // Montag -- heute

        case 3: // Dienstag

        case 4: // Mittwoch

        case 5: // Donnerstag

        case 6: // Freitag -- heute
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(), "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 7: // Samstag -- Datum +2
          c.setTime(date);
          c.add(Calendar.DATE, 2);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(), "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        default: // Default
          Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.", Toast.LENGTH_LONG)
              .show();
          break;
      }
    } else {
      switch (day) {
        case 1: // Sonntag -- Datum +1

        case 2: // Montag -- Datum +1

        case 3: // Dienstag -- +1

        case 4: // Mittwoch -- +1

        case 5: // Donnerstag -- Datum +1
          c.setTime(date);
          c.add(Calendar.DATE, 1);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(), "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 6: // Freitag -- Datum +3
          c.setTime(date);
          c.add(Calendar.DATE, 3);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(), "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 7: // Samstag -- Datum +2
          c.setTime(date);
          c.add(Calendar.DATE, 2);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(), "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        default: // Default
          Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.", Toast.LENGTH_LONG)
              .show();
          break;
      }
    }


  }


}