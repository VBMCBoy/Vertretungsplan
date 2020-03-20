package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    private String password;
    private String username;
    private String clazz;
    private final String BASE_URL = "https://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/";
    private WebView webView;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // use shared preferences instead
        password = prefs.getString("PW", "");
        username = prefs.getString("BN", "");
        clazz = prefs.getString("KL", "");

        if (!(password.isEmpty() || username.isEmpty() || clazz.isEmpty())) {
                download();

            //Variablen festlegen
            webView = view.findViewById(R.id.WebViewHeute); // TODO das irgendwie auf eine Webview zurückführen? -> einfach gleich nennen? geht wahrsch. nicht, weil wie ein enum...

        }
    }

    private String getUrlToDay(int day) {
        switch (day) {
            case 1: // Sonntag
                return BASE_URL + "Montag.htm";
            case 2:  // Montag
                return BASE_URL + "Montag.htm";
            case 3:// Dienstag
                return BASE_URL + "Dienstag.htm";
            case 4:// Mittwoch
                return BASE_URL + "Mittwoch.htm";
            case 5:  // Donnerstag
                return BASE_URL + "Donnerstag.htm";
            case 6: // Freitag
                return BASE_URL + "Freitag.htm";
            case 7: // Samstag
                return BASE_URL + "Montag.htm";
            default:
                return BASE_URL + "Montag.htm";
        }
    }

    abstract void download(); // TODO -> bei onResponse das in die WebView laden...

    private void toWebview(final WebView webView, final String Plan,
                           final Boolean heute, final Integer day) {

        final String Klassenkuerzel;
        final String Klassenstufe;

        if (Character.isLetter(clazz
                .charAt(clazz.length()
                        - 1))) {   // wenn das letzte Zeichen ein Buchstabe ist -- String wird bei Lehrer nicht benutzt, nur bei Schüler
            Klassenkuerzel = Character.toString(clazz.charAt(clazz.length() - 1));
            Klassenstufe = clazz.substring(0, clazz.length() - 1);
        } else {
            Klassenstufe = clazz;
            Klassenkuerzel = "";
        }

        final String regex = "" + Klassenstufe + ".*" + Klassenkuerzel + ".*";

        final Document doc2 = Jsoup.parse(Plan, "windows-1252");
        if (clazz.matches(".*\\d+.*")) { // true = ist kein Lehrer
            final Elements TEST = doc2.select("tr:has(td:eq(1):matches(" + regex + "))");
            TEST.attr("bgcolor", "FFF007");
        } else {
            final Elements TEST = doc2.select("tr:contains(" + clazz + ")");  // für Lehrer
            TEST.attr("bgcolor", "FFF007");
        }
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadData(doc2.html(), "text/html; charset=UTF-8",
                null);

        checkDateCorrect(Plan, day);    // macht einen Toast wenn das Datum nicht stimmt

    }


    private void checkDateCorrect(final String Plan, final Integer day) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM");

        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        c.setTime(date);

        if (day <= 5) { // zwischen Sonntag und Donnerstag -> Datum + 1
            c.add(Calendar.DATE, 1);
            date = c.getTime();
        } else if (day == 6) { // Freitag -> Datum + 3
            c.add(Calendar.DATE, 3);
            date = c.getTime();
        } else if (day == 7) {
            c.add(Calendar.DATE, 2); // Samstag -> Datum + 2
            date = c.getTime();
        } else {
            Toast.makeText(getContext(), "Ein Fehler ist aufgetreten.",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (!(Plan.contains(dateFormat.format(date)))) {
            Toast.makeText(getContext(),
                    "Der Vertretungsplan für morgen scheint falsch zu sein.",
                    Toast.LENGTH_LONG).show();
        }
    }

}
