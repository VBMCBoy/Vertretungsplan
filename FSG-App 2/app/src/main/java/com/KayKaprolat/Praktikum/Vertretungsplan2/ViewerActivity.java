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
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import com.KayKaprolat.Praktikum.Vertretungsplan2.R.drawable;
import com.KayKaprolat.Praktikum.Vertretungsplan2.R.id;
import com.KayKaprolat.Praktikum.Vertretungsplan2.R.layout;
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

  private static final String ACCOUNT_TYPE = "sachsen.schule";

  private static final String ACCOUNT = "default_account";

  private static final String AUTHORITY = "com.KayKaprolat.Praktikum.Vertretungsplan2.provider";   // provider oder StubProvider?

  private ContentResolver mResolver;

  private Account account;

  private static Account CreateSyncAccount(Context context) {
    Account newAccount = new Account(ViewerActivity.ACCOUNT, ViewerActivity.ACCOUNT_TYPE);
    AccountManager accountManager = (AccountManager) context.getSystemService(
        Context.ACCOUNT_SERVICE);
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

    this.account = ViewerActivity.CreateSyncAccount(this.getApplicationContext());

    this.getActionBar().setTitle("Vertretungsplan");

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // use shared preferences instead
    String wert_PW = prefs.getString("PW", "");
    String wert_name = prefs.getString("BN", "");
    String wert_klasse = prefs.getString("KL", "");
    Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);

    this.mResolver = this.getContentResolver();

    if (syncable) {
      if (ContentResolver.getSyncAutomatically(this.account, ViewerActivity.AUTHORITY)) {
        if (!(ContentResolver.isSyncPending(this.account, ViewerActivity.AUTHORITY))) {
          ContentResolver
              .addPeriodicSync(this.account, ViewerActivity.AUTHORITY, Bundle.EMPTY, 60 * 60);
        }
      } else {
        ContentResolver.setMasterSyncAutomatically(true);
        ContentResolver.setSyncAutomatically(this.account, ViewerActivity.AUTHORITY, true);
      }

    } else {
      ContentResolver.setMasterSyncAutomatically(false);
      ContentResolver.setSyncAutomatically(this.account, ViewerActivity.AUTHORITY, false);
      ContentResolver.cancelSync(this.account, ViewerActivity.AUTHORITY);
    }

    //prüfen ob leer

    if ("".equals(wert_PW) || "".equals(wert_name) || "".equals(wert_klasse)) {
      // Toast
      Toast.makeText(this.getApplicationContext(),
          "Bitte stellen Sie Klasse / Lehrer, Benutzername und Passwort ein.", Toast.LENGTH_LONG)
          .show();
      // Einstellungen öffnen
      Intent intent = new Intent(this, SettingsActivity.class);
      this.startActivity(intent);
    } else {

      this.setContentView(layout.viewer); //Layout starten
      //Variablen festlegen
      WebView webView = (WebView) this.findViewById(id.webView1);

      this.Laden(webView, true, wert_klasse, wert_name, wert_PW);


    }


  }

  @Override
  protected void onResume() {
    super.onResume();
    this.account = ViewerActivity.CreateSyncAccount(this);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
    this.mResolver = this.getContentResolver();

    if (syncable) {
      if (ContentResolver.getSyncAutomatically(this.account, ViewerActivity.AUTHORITY)) {
        if (!(ContentResolver.isSyncPending(this.account, ViewerActivity.AUTHORITY))) {
          ContentResolver
              .addPeriodicSync(this.account, ViewerActivity.AUTHORITY, Bundle.EMPTY, 60 * 60);
        }
      } else {
        ContentResolver.setMasterSyncAutomatically(true);
        ContentResolver.setSyncAutomatically(this.account, ViewerActivity.AUTHORITY, true);
      }

    } else {
      ContentResolver.setMasterSyncAutomatically(false);
      ContentResolver.setSyncAutomatically(this.account, ViewerActivity.AUTHORITY, false);
      ContentResolver.cancelSync(this.account, ViewerActivity.AUTHORITY);
    }


  }

  public void notification(String title, String text) {
    Builder mBuilder =
        new Builder(this)
            .setSmallIcon(drawable.ic_stat_name)
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
    NotificationManager notificationManager = (NotificationManager) this.getSystemService(
        Context.NOTIFICATION_SERVICE);
    notificationManager.notify(ID, mBuilder.build());
  }


  public void BtnHeuteClick(View view) {

    // Heute
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    // use shared preferences instead
    String wert_PW = prefs.getString("PW", " ");
    String wert_name = prefs.getString("BN", " ");
    String wert_klasse = prefs.getString("KL", " ");

    this.setContentView(layout.viewer);
    WebView webView = (WebView) this.findViewById(id.webView1);

    this.Laden(webView, true, wert_klasse, wert_name, wert_PW);

  }

  public void BtnMorgenClick(View view) {
    // Morgen
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String wert_PW = prefs.getString("PW", " ");
    String wert_name = prefs.getString("BN", " ");
    String wert_klasse = prefs.getString("KL", " ");

    this.setContentView(layout.viewer);
    WebView webView = (WebView) this.findViewById(id.webView1);

    this.Laden(webView, false, wert_klasse, wert_name, wert_PW);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = this.getMenuInflater();
    inflater.inflate(menu.main, menu);
    return true;
  }

  public void Menu_Einstellungen(MenuItem item) {

    Intent intent = new Intent(this, SettingsActivity.class);
    this.startActivity(intent);

  }


  public void Menu_Licenses(MenuItem item) {

    Intent intent = new Intent(this, LicenseActivity.class);
    this.startActivity(intent);

  }


  public void Menu_About(MenuItem item) {
    Intent intent = new Intent(this, AboutActivity.class);
    this.startActivity(intent);

  }


  private void Laden(final WebView webView, Boolean heute, final String wert_klasse,
      final String wert_name, final String wert_PW) {

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
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 2:  // Montag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 3:// Dienstag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm");
                break;
              case 4:// Mittwoch
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
                break;
              case 5:  // Donnerstag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm");
                break;
              case 6: // Freitag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm");
                break;
              case 7: // Samstag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              default:
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
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

            String Plan = new String(baos.toByteArray(),
                "windows-1252");
            // das ist grauenhaft
            String a = ViewerActivity.this.cache(true).replaceAll(" ", "");
            String b = a.replaceAll("\r", "");
            String c = Plan.replaceAll("\r", "");
            String d = c.replaceAll(" ", "");
            if (!(b.equals(d))) {   // wenn der aktuelle Plan anders als der Alte ist

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(ViewerActivity.this.getApplicationContext(),
                      "Der Vertretungsplan ist neu.",
                      Toast.LENGTH_LONG).show();
                }
              });

            }
            ViewerActivity.this.speichern(Plan, true);

            ViewerActivity.this.toWebview(webView, Plan, wert_klasse, true,
                day); // WebView, HTML, Klasse bzw Lehrer, heute?, heute Freitag?


          } catch (Exception e) {
            e.printStackTrace();
            ViewerActivity.this.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(ViewerActivity.this.getApplicationContext(),
                    "Ein Fehler ist aufgetreten.",
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
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 2:  // Montag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm");
                break;
              case 3:// Dienstag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
                break;
              case 4:// Mittwoch
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm");
                break;
              case 5:  // Donnerstag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm");
                break;
              case 6: // Freitag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              case 7: // Samstag
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                break;
              default:
                url = new URL(
                    "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
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

            String Plan = new String(baos.toByteArray(),
                "windows-1252");

            String a = ViewerActivity.this.cache(false).replaceAll(" ", "");
            String b = a.replaceAll("\r", "");
            String c = Plan.replaceAll("\r", "");
            String d = c.replaceAll(" ", "");
            if (!(b.equals(d))) {   // wenn der aktuelle Plan anders als der Alte ist

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(ViewerActivity.this.getApplicationContext(),
                      "Der Vertretungsplan ist neu.",
                      Toast.LENGTH_LONG).show();
                }
              });

            }
            ViewerActivity.this.speichern(Plan, false);

            ViewerActivity.this.toWebview(webView, Plan, wert_klasse, false, day);


          } catch (Exception e) {
            e.printStackTrace();
            ViewerActivity.this.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(ViewerActivity.this.getApplicationContext(),
                    "Ein Fehler ist aufgetreten.",
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
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    Editor editor = sharedPref.edit();
    if (heute) {
      editor.putString("cache_website_heute", string);
    } else {
      editor.putString("cache_website_morgen", string);
    }

    editor.commit();


  }

  private String cache(Boolean heute) {
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    if (heute) {
      return sharedPref.getString("cache_website_heute", "");
    } else {
      return sharedPref.getString("cache_website_morgen", "");
    }

  }

  private void toWebview(final WebView webView, final String Plan, final String wert_klasse,
      final Boolean heute, final Integer day) {
    this.runOnUiThread(new Runnable() {
      @Override
      public void run() {

        String Klassenkuerzel;
        String Klassenstufe;

        if (Character.isLetter(wert_klasse
            .charAt(wert_klasse.length()
                - 1))) {   // wenn das letzte Zeichen ein Buchstabe ist -- String wird bei Lehrer nicht benutzt, nur bei Schüler
          Klassenkuerzel = Character.toString(wert_klasse.charAt(wert_klasse.length() - 1));
          Klassenstufe = wert_klasse.substring(0, wert_klasse.length() - 2);
        } else {
          Klassenstufe = wert_klasse;
          Klassenkuerzel = "";
        }

        String regex = "" + Klassenstufe + ".*" + Klassenkuerzel + ".*";

        Document doc2 = Jsoup.parse(Plan, "windows-1252");
        if (wert_klasse.matches(".*\\d+.*")) { // true = ist kein Lehrer
          Elements TEST = doc2.select("tr:has(td:eq(1):matches(" + regex + "))");
          TEST.attr("bgcolor", "FFF007");
        } else {
          Elements TEST = doc2.select("tr:contains(" + wert_klasse + ")");  // für Lehrer
          TEST.attr("bgcolor", "FFF007");
        }
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadData(doc2.html(), "text/html; charset=UTF-8",
            null);

        ViewerActivity.this
            .Datum_richtig(Plan, heute, day);    // macht einen Toast wenn das Datum nicht stimmt

      }
    });
  }

  private void Datum_richtig(String Plan, Boolean heute, Integer day) {
    DateFormat dateFormat = new SimpleDateFormat("dd.MM");

    Calendar c = Calendar.getInstance();
    Date date = c.getTime();

    if (heute) {
      switch (day) {
        case 1: // Sonntag -- Datum +1
          c.setTime(date);
          c.add(Calendar.DATE, 1);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(this.getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 2: // Montag -- heute

        case 3: // Dienstag

        case 4: // Mittwoch

        case 5: // Donnerstag

        case 6: // Freitag -- heute
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(this.getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 7: // Samstag -- Datum +2
          c.setTime(date);
          c.add(Calendar.DATE, 2);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(this.getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        default: // Default
          Toast.makeText(this.getApplicationContext(), "Ein Fehler ist aufgetreten.",
              Toast.LENGTH_LONG)
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
            Toast.makeText(this.getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 6: // Freitag -- Datum +3
          c.setTime(date);
          c.add(Calendar.DATE, 3);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(this.getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 7: // Samstag -- Datum +2
          c.setTime(date);
          c.add(Calendar.DATE, 2);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(this.getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        default: // Default
          Toast.makeText(this.getApplicationContext(), "Ein Fehler ist aufgetreten.",
              Toast.LENGTH_LONG)
              .show();
          break;
      }
    }


  }


}