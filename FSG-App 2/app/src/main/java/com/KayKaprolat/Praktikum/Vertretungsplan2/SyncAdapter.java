package com.KayKaprolat.Praktikum.Vertretungsplan2;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Calendar;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  ContentResolver resolver;

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);

  }

  public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
    super(context, autoInitialize, allowParallelSyncs);
  }

  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {

    Log.i("Test", "TEEEEEESSSSSTTTT");

    // Transfer Zeugs
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    Boolean aktiv = prefs.getBoolean("Benachrichtigungan", false);
    final String wert_PW = prefs.getString("PW", "");
    final String wert_name = prefs.getString("BN", "");
    String wert_klasse = prefs.getString("KL", "");

    Log.i("TEst", wert_klasse + wert_name + wert_PW + aktiv);

    // nur für morgen
    if (aktiv) {  // nur wenn an
      if ((!(wert_PW == "")) | (!(wert_name == "")) | (!(wert_klasse
          == ""))) { // nur wenn alles eingestellt

        URL url;
        HttpURLConnection urlConnection = null;

        try {
          java.net.Authenticator.setDefault(new Authenticator() {
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

          String morgen_alt = prefs.getString("cache_website_morgen", "");
          String a = morgen_alt.replaceAll(" ", "");
          String b = a.replaceAll("\r", ""); // gereinigter alter String
          String c = Plan.replaceAll(" ", "");
          String d = c.replaceAll("\r", "");

          if (!(b.equals(d))) {
            // Benachrichtigung wenn nötig (Plan ist neu und wichtig)

            notification("Titel", "Text");

          } else {
            // Benachrichtigung wenn nötig (Plan ist wichtig)
          }

          notification("Tiiiiitel", "Hier steht ganz viel Text...");


        } catch (Exception e) {
          e.printStackTrace();

          Toast.makeText(getContext(), "Ein Fehler ist aufgetreten.",
              Toast.LENGTH_LONG).show();
        } finally {
          if (urlConnection != null) {
            urlConnection.disconnect();
          }
        }

      }

    }
  }

  public void notification(String title, String text) {
    Context context = getContext();
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_stat_name).setContentTitle(title).setContentText(text);
    Intent resultIntent = new Intent(context, ViewerActivity.class);
    PendingIntent resultPendingIntent = PendingIntent
        .getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    int ID = 1;
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(NOTIFICATION_SERVICE);
    notificationManager.notify(ID, mBuilder.build());
  }

}
