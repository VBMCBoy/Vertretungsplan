package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class FragmentHeute extends Fragment {


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragmentheute, container, false);

  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    // use shared preferences instead
    String wert_PW = prefs.getString("PW", "");
    String wert_name = prefs.getString("BN", "");
    String wert_klasse = prefs.getString("KL", "");
    Boolean syncable = prefs.getBoolean("Benachrichtigungan", false);
    Boolean datacollection = prefs.getBoolean("Datenschutz", true);

    if (!("".equals(wert_PW) || "".equals(wert_name) || "".equals(wert_klasse))) {

      //Variablen festlegen
      WebView webView = getView().findViewById(R.id.WebViewHeute);

      Laden(webView, wert_klasse, wert_name, wert_PW);

    }


  }

  private void Laden(final WebView webView, final String wert_klasse,
      final String wert_name, final String wert_PW) {

    Calendar calendar = Calendar.getInstance();
    final int day = calendar.get(Calendar.DAY_OF_WEEK);

    String url;
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

    RequestQueue queue = Volley.newRequestQueue(getContext());

    StringRequest request = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            String Plan = response;

            speichern(Plan, true);

            toWebview(webView, Plan, wert_klasse, true,
                day); // WebView, HTML, Klasse bzw Lehrer, heute?, heute Freitag?

          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
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
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    if (heute) {
      editor.putString("cache_website_heute", string);
    } else {
      editor.putString("cache_website_morgen", string);
    }

    editor.commit();


  }

  private String cache(Boolean heute) {
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    if (heute) {
      return sharedPref.getString("cache_website_heute", "");
    } else {
      return sharedPref.getString("cache_website_morgen", "");
    }

  }

  private void toWebview(WebView webView, String Plan, String wert_klasse,
      Boolean heute, Integer day) {

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

    // Datum_richtig bei heute nicht nötig

  }


}
