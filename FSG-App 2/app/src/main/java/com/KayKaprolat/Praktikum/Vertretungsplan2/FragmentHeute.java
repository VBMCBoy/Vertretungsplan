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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class FragmentHeute extends Fragment {


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmentheute, container, false);

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
            final WebView webView = getView().findViewById(R.id.WebViewHeute);

            Laden(webView, wert_klasse, wert_name, wert_PW);

        }


    }

    private void Laden(WebView webView, String wert_klasse,
                       String wert_name, String wert_PW) {

        final Calendar calendar = Calendar.getInstance();
        final int day = calendar.get(Calendar.DAY_OF_WEEK);

        final String url;
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

        final RequestQueue queue = Volley.newRequestQueue(getContext());

        final StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    speichern(response, true);
                    toWebview(webView, response, wert_klasse, true,
                            day); // WebView, HTML, Klasse bzw Lehrer, heute?, heute Freitag?

                }, error -> {
            if (error instanceof AuthFailureError) {
                Toast.makeText(getContext(), "Nutzername oder Passwort ist falsch.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Verbindungsfehler " + error, Toast.LENGTH_SHORT)
                        .show();
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
        final FragmentActivity activity = getActivity();
        if (null != activity) {
            final SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPref.edit();
            if (heute) {
                editor.putString("cache_website_heute", string);
            } else {
                editor.putString("cache_website_morgen", string);
            }

            editor.apply();

        }
    }

    private String cache(final Boolean heute) {
        final FragmentActivity activity = getActivity();

        if (null != activity) {
            final SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            if (heute) {
                return sharedPref.getString("cache_website_heute", "");
            } else {
                return sharedPref.getString("cache_website_morgen", "");
            }

        }

        return ""; // activity war null
    }

    private void toWebview(final WebView webView, final String Plan, final String wert_klasse,
                           final Boolean heute, final Integer day) {

        String klassenkuerzel = "";
        String klassenstufe = "";

        if (Character.isLetter(wert_klasse
                .charAt(wert_klasse.length()
                        - 1))) {   // wenn das letzte Zeichen ein Buchstabe ist -- String wird bei Lehrer nicht benutzt, nur bei Schüler
            klassenkuerzel = Character.toString(wert_klasse.charAt(wert_klasse.length() - 1));
            klassenstufe = wert_klasse.substring(0, wert_klasse.length() - 1);
        } else {
            klassenstufe = wert_klasse;
            klassenkuerzel = "";
        }

        final String regex = "" + klassenstufe + ".*" + klassenkuerzel + ".*";

        final Document doc2 = Jsoup.parse(Plan, "windows-1252");

        if (wert_klasse.matches(".*\\d+.*")) { // true = ist kein Lehrer
            final Elements elements = doc2.select("tr:has(td:eq(1):matches(" + regex + "))");
            elements.attr("bgcolor", "FFF007");
        } else {
            final Elements elements = doc2.select("tr:contains(" + wert_klasse + ")");  // für Lehrer
            elements.attr("bgcolor", "FFF007");
        }
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadData(doc2.html(), "text/html; charset=UTF-8",
                null);

        // Datum_richtig bei heute nicht nötig

    }


}
