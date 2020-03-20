package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;

public class FragmentMorgen extends Fragment {

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragmentmorgen, container, false);
  }


  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    // use shared preferences instead
    final String wert_PW = prefs.getString("PW", "");
    final String wert_name = prefs.getString("BN", "");
    final String wert_klasse = prefs.getString("KL", "");
    final Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
    final Boolean datacollection = prefs.getBoolean("Datenschutz", true);

    if (!("".equals(wert_PW) || "".equals(wert_name) || "".equals(wert_klasse))) {

      //Variablen festlegen
      final WebView webView = getView().findViewById(R.id.WebViewMorgen);

      Laden(webView, wert_klasse, wert_name, wert_PW);

    }


  }

  private void Laden(WebView webView, String wert_klasse,
                     String wert_name, String wert_PW) {

    final Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_WEEK);

    final String url;
    // heute
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

    final RequestQueue queue = Volley.newRequestQueue(getContext());

    final StringRequest request = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(final String response) {
            final String Plan = response;
            final String a = cache(false).replaceAll(" ", "");
            final String b = a.replaceAll("\r", "");
            final String c = Plan.replaceAll("\r", "");
            final String d = c.replaceAll(" ", "");
            if (!(b.equals(d))) {   // wenn der aktuelle Plan anders als der Alte ist

              Toast.makeText(getContext(),
                  "Der Vertretungsplan ist neu.",
                  Toast.LENGTH_LONG).show();


            }

            speichern(Plan, false);

            toWebview(webView, Plan, wert_klasse, false,
                day); // WebView, HTML, Klasse bzw Lehrer, heute?, heute Freitag?

          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(final VolleyError error) {
        if (error instanceof AuthFailureError) {
          Toast.makeText(getContext(), "Nutzername oder Passwort ist falsch.",
              Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getContext(), "Verbindungsfehler " + error, Toast.LENGTH_SHORT)
              .show();
        }
      }
    }) {

      @Override
      public Map<String, String> getHeaders() {
        final Map<String, String> headers = new HashMap<>();
        // add headers <key,value>
        final String credentials = wert_name + ":" + wert_PW;
        final String auth = "Basic "
            + Base64.encodeToString(credentials.getBytes(),
            Base64.NO_WRAP);
        headers.put("Authorization", auth);
        return headers;
      }
    };

    request.setRetryPolicy(new DefaultRetryPolicy(5000, 5, 1.5f));

    queue.add(request);


  }

  private void speichern(final String string, final Boolean heute) {
    final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = sharedPref.edit();
    if (heute) {
      editor.putString("cache_website_heute", string);
    } else {
      editor.putString("cache_website_morgen", string);
    }

    editor.apply();


  }

  private String cache(final Boolean heute) {
    final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    if (heute) {
      return sharedPref.getString("cache_website_heute", "");
    } else {
      return sharedPref.getString("cache_website_morgen", "");
    }

  }

  private void toWebview(final WebView webView, final String Plan, final String wert_klasse,
                         final Boolean heute, final Integer day) {

    final String Klassenkuerzel;
    final String Klassenstufe;

    if (Character.isLetter(wert_klasse
        .charAt(wert_klasse.length()
            - 1))) {   // wenn das letzte Zeichen ein Buchstabe ist -- String wird bei Lehrer nicht benutzt, nur bei Schüler
      Klassenkuerzel = Character.toString(wert_klasse.charAt(wert_klasse.length() - 1));
      Klassenstufe = wert_klasse.substring(0, wert_klasse.length() - 1);
    } else {
      Klassenstufe = wert_klasse;
      Klassenkuerzel = "";
    }

    final String regex = "" + Klassenstufe + ".*" + Klassenkuerzel + ".*";

    final Document doc2 = Jsoup.parse(Plan, "windows-1252");
    if (wert_klasse.matches(".*\\d+.*")) { // true = ist kein Lehrer
      final Elements TEST = doc2.select("tr:has(td:eq(1):matches(" + regex + "))");
      TEST.attr("bgcolor", "FFF007");
    } else {
      final Elements TEST = doc2.select("tr:contains(" + wert_klasse + ")");  // für Lehrer
      TEST.attr("bgcolor", "FFF007");
    }
    webView.getSettings().setBuiltInZoomControls(true);
    webView.getSettings().setDisplayZoomControls(false);
    webView.loadData(doc2.html(), "text/html; charset=UTF-8",
        null);

    Datum_richtig(Plan, day);    // macht einen Toast wenn das Datum nicht stimmt

  }


  private void Datum_richtig(final String Plan, final Integer day) {
    final DateFormat dateFormat = new SimpleDateFormat("dd.MM");

    final Calendar c = Calendar.getInstance();
    Date date = c.getTime();

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
          Toast.makeText(getContext(),
              "Der Vertretungsplan für morgen scheint falsch zu sein.",
              Toast.LENGTH_LONG).show();
        }
        break;
      case 6: // Freitag -- Datum +3
        c.setTime(date);
        c.add(Calendar.DATE, 3);
        date = c.getTime();
        if (!(Plan.contains(dateFormat.format(date)))) {
          Toast.makeText(getContext(),
              "Der Vertretungsplan für morgen scheint falsch zu sein.",
              Toast.LENGTH_LONG).show();
        }
        break;
      case 7: // Samstag -- Datum +2
        c.setTime(date);
        c.add(Calendar.DATE, 2);
        date = c.getTime();
        if (!(Plan.contains(dateFormat.format(date)))) {
          Toast.makeText(getContext(),
              "Der Vertretungsplan für morgen scheint falsch zu sein.",
              Toast.LENGTH_LONG).show();
        }
        break;
      default: // Default
        Toast.makeText(getContext(), "Ein Fehler ist aufgetreten.",
            Toast.LENGTH_LONG)
            .show();
        break;
    }
  }


}

