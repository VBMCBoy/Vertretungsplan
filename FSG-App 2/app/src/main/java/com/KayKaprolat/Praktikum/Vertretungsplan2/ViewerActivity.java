package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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

@SuppressLint("JavascriptInterface")
public class ViewerActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Vertretungsplan");


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefedit = prefs.edit();
        // use shared preferences instead
        final String wert_PW = prefs.getString("PW", "");
        final String wert_name = prefs.getString("BN", "");
        final String wert_klasse = prefs.getString("KL", "");

        //prüfen ob leer

        if (wert_PW == "" || wert_name == "" || wert_klasse == "") {
            // Toast
            Toast.makeText(getApplicationContext(), "Bitte stellen Sie Klasse / Lehrer, Benutzername und Passwort ein.", Toast.LENGTH_LONG).show();
            // Einstellungen öffnen
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {

            setContentView(R.layout.viewer); //Layout starten
            final TextView textView = (TextView) findViewById(R.id.Viewertxt); //Variablen festlegen
            final WebView webView = (WebView) findViewById(R.id.webView1);

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
                        DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
                        final Date date = new Date();
                        switch (day) {
                            case 1: // Sonntag
                                url = new URL(
                                        "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
                                break;
                            case 2:  // Montag
                                url = new URL(
                                        "http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
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


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Document doc2 = Jsoup.parse(Plan, "windows-1252");
                                if (wert_klasse.matches(".*\\d+.*")) { //true = ist kein Lehrer
                                    Elements TEST = doc2.select("tr:has(td:eq(1):contains(" + wert_klasse + "))");
                                    TEST.attr("bgcolor", "FFF007");
                                } else {
                                    Elements TEST = doc2.select("tr:has(td:eq(3):contains(" + wert_klasse + "))");
                                    TEST.attr("bgcolor", "FFF007");
                                }
                                webView.getSettings().setBuiltInZoomControls(true);
                                webView.getSettings().setDisplayZoomControls(false);
                                webView.loadData(doc2.html(), "text/html; charset=UTF-8",
                                        null);

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.", Toast.LENGTH_LONG).show();
                            }
                        });

                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }

                    }

                }

            }.start();


        }
    }


    public void BtnHeuteClick(View view) {
        // Heute
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // use shared preferences instead
        final String wert_PW = prefs.getString("PW", " ");
        final String wert_name = prefs.getString("BN", " ");
        final String wert_klasse = prefs.getString("KL", " ");
        Boolean LEHRER = false;

        setContentView(R.layout.viewer);
        final TextView textView = (TextView) findViewById(R.id.Viewertxt);
        final WebView webView = (WebView) findViewById(R.id.webView1);
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
                    DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
                    final Date date = new Date();

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
                        final String Plan = new String(baos.toByteArray(),
                                "windows-1252");


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Document doc2 = Jsoup.parse(Plan, "windows-1252");

                                if (wert_klasse.matches(".*\\d+.*")) { //true = ist kein Lehrer
                                    Elements TEST = doc2.select("tr:has(td:eq(1):contains(" + wert_klasse + "))");
                                    TEST.attr("bgcolor", "FFF007");
                                } else {
                                    Elements TEST = doc2.select("tr:has(td:eq(3):contains(" + wert_klasse + "))");
                                    TEST.attr("bgcolor", "FFF007");
                                }

                                webView.getSettings().setBuiltInZoomControls(true);
                                webView.getSettings().setDisplayZoomControls(false);
                                webView.loadData(doc2.html(), "text/html; charset=UTF-8",
                                        null);

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();


                    }
                }


            }
        }.start();
    }

    public void BtnMorgenClick(View view) {
        // Morgen
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String wert_PW = prefs.getString("PW", " ");
        final String wert_name = prefs.getString("BN", " ");
        final String wert_klasse = prefs.getString("KL", " ");
        final Boolean LEHRER = prefs.getBoolean("LE", false);

        setContentView(R.layout.viewer);
        final TextView textView = (TextView) findViewById(R.id.Viewertxt);
        final WebView webView = (WebView) findViewById(R.id.webView1);

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
                    DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
                    final Date date = new Date();
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Document doc2 = Jsoup.parse(Plan, "windows-1252");

                            if (wert_klasse.matches(".*\\d+.*")) { //true = ist kein Lehrer
                                Elements TEST = doc2.select("tr:has(td:eq(1):contains(" + wert_klasse + "))");
                                TEST.attr("bgcolor", "FFF007");
                            } else {
                                Elements TEST = doc2.select("tr:has(td:eq(3):contains(" + wert_klasse + "))");
                                TEST.attr("bgcolor", "FFF007");
                            }

                            webView.getSettings().setBuiltInZoomControls(true);
                            webView.getSettings().setDisplayZoomControls(false);
                            webView.loadData(doc2.html(), "text/html; charset=UTF-8",
                                    null);

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Ein Fehler ist aufgetreten.", Toast.LENGTH_LONG).show();
                        }
                    });

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            }
        }.start();
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


}