package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ViewerActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(
        new GooglePlayDriver(getApplicationContext()));

    //  this.getActionBar().setTitle("Vertretungsplan");

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
    } else {

      setContentView(R.layout.viewer); //Layout starten
      //Variablen festlegen
      WebView webView = findViewById(R.id.webView1);

      Laden(webView, true, wert_klasse, wert_name, wert_PW);


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

    setContentView(R.layout.viewer);
    WebView webView = findViewById(R.id.webView1);

    Laden(webView, true, wert_klasse, wert_name, wert_PW);

  }

  public void BtnMorgenClick(View view) {
    // Morgen
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String wert_PW = prefs.getString("PW", " ");
    String wert_name = prefs.getString("BN", " ");
    String wert_klasse = prefs.getString("KL", " ");

    setContentView(R.layout.viewer);
    WebView webView = findViewById(R.id.webView1);

    Laden(webView, false, wert_klasse, wert_name, wert_PW);
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

  private void Laden(final WebView webView, final Boolean heute, final String wert_klasse,
      final String wert_name, final String wert_PW) {

    Calendar calendar = Calendar.getInstance();
    final int day = calendar.get(Calendar.DAY_OF_WEEK);

    String url;
    if (heute) {
      // heute
      switch (day) {
        case 1: // Sonntag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
        case 2:  // Montag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
        case 3:// Dienstag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm";
          break;
        case 4:// Mittwoch
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm";
          break;
        case 5:  // Donnerstag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm";
          break;
        case 6: // Freitag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm";
          break;
        case 7: // Samstag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
        default:
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
      }

    } else {
      // morgen

      switch (day) {
        case 1: // Sonntag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
        case 2:  // Montag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm";
          break;
        case 3:// Dienstag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm";
          break;
        case 4:// Mittwoch
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm";
          break;
        case 5:  // Donnerstag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm";
          break;
        case 6: // Freitag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
        case 7: // Samstag
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
        default:
          url =
              "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm";
          break;
      }
    }

    RequestQueue queue = Volley.newRequestQueue(this);

    StringRequest request = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            //  toWebview(webView, response, wert_klasse, heute, day);
            String Plan = response;
            String a = cache(heute).replaceAll(" ", "");
            String b = a.replaceAll("\r", "");
            String c = Plan.replaceAll("\r", "");
            String d = c.replaceAll(" ", "");
            if (!(b.equals(d))) {   // wenn der aktuelle Plan anders als der Alte ist

              Toast.makeText(getApplicationContext(),
                  "Der Vertretungsplan ist neu.",
                  Toast.LENGTH_LONG).show();


            }

            speichern(Plan, heute);

            toWebview(webView, Plan, wert_klasse, heute,
                day); // WebView, HTML, Klasse bzw Lehrer, heute?, heute Freitag?

          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        if (error instanceof AuthFailureError) {
          Toast.makeText(ViewerActivity.this, "Nutzername oder Passwort ist falsch.",
              Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(ViewerActivity.this, "Verbindungsfehler", Toast.LENGTH_SHORT).show();
        }
      }
    }) {

      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        // add headers <key,value>
        String credentials = wert_name + ":" + wert_PW;
        String auth = "Basic "
            + Base64.encodeToString(credentials.getBytes(),
            Base64.NO_WRAP);
        headers.put("Authorization", auth);
        return headers;
      }
    };

    request.setRetryPolicy(new DefaultRetryPolicy(5000, 5, 1.5f));

    queue.add(request);


  }


  private void speichern(String string, Boolean heute) {
    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
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
    runOnUiThread(new Runnable() {
      @Override
      public void run() {

        String Klassenkuerzel;
        String Klassenstufe;

        if (Character.isLetter(wert_klasse
            .charAt(wert_klasse.length()
                - 1))) {   // wenn das letzte Zeichen ein Buchstabe ist -- String wird bei Lehrer nicht benutzt, nur bei Schüler
          Klassenkuerzel = Character.toString(wert_klasse.charAt(wert_klasse.length() - 1));
          Klassenstufe = wert_klasse.substring(0, wert_klasse.length() - 1);
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

        Datum_richtig(Plan, heute, day);    // macht einen Toast wenn das Datum nicht stimmt

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
            Toast.makeText(getApplicationContext(),
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
            Toast.makeText(getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 7: // Samstag -- Datum +2
          c.setTime(date);
          c.add(Calendar.DATE, 2);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        default: // Default
          Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.",
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
            Toast.makeText(getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 6: // Freitag -- Datum +3
          c.setTime(date);
          c.add(Calendar.DATE, 3);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        case 7: // Samstag -- Datum +2
          c.setTime(date);
          c.add(Calendar.DATE, 2);
          date = c.getTime();
          if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getApplicationContext(),
                "Der Vertretungsplan scheint falsch zu sein.",
                Toast.LENGTH_LONG).show();
          }
          break;
        default: // Default
          Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.",
              Toast.LENGTH_LONG)
              .show();
          break;
      }
    }


  }


}