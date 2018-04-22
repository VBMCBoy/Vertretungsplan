package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySyncJob extends Job {


  public static final String TAG = "Vertretungsplan";

  public static void scheduleJob() {
    new JobRequest.Builder(MySyncJob.TAG)
        .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
        .setPeriodic(TimeUnit.MINUTES.toMillis(60), TimeUnit.MINUTES.toMillis(15))
        .setRequirementsEnforced(true)
        .setUpdateCurrent(true)
        .build()
        .schedule();
  }

  @Override
  @NonNull
  protected Job.Result onRunJob(Job.Params params) {
    // run job here

    runjob(0);

    return Job.Result.SUCCESS;
  }

  public void notification(String title, String text, Integer ID, Integer priority) {
    Context context = getContext();
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,
        "ID_Vertretungsplan")
        .setSmallIcon(R.drawable.ic_stat_name).setContentTitle(title).setContentText(text);
    Intent resultIntent = new Intent(context, ViewerActivity.class);
    PendingIntent resultPendingIntent = PendingIntent
        .getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    mBuilder.setAutoCancel(true);
    if (1 == priority) {
      // hohe Priorität
      mBuilder.setVibrate(new long[]{1000, 1000, 1000});
    }

    notificationManager.notify(ID, mBuilder.build());
  }

  public void closeNotification(Integer ID) {
    Context context = getContext();
    NotificationManager notimanager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    notimanager.cancel(ID);

  }

  public Boolean PlanRichtig(Integer day, String Plan) {

    DateFormat dateFormat = new SimpleDateFormat("dd.MM");

    Calendar c = Calendar.getInstance();
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
        return Plan.contains(dateFormat.format(date));
      //break;
      case 6: // Freitag -- Datum +3
        c.setTime(date);
        c.add(Calendar.DATE, 3);
        date = c.getTime();
        return Plan.contains(dateFormat.format(date));
      // break;
      case 7: // Samstag -- Datum +2
        c.setTime(date);
        c.add(Calendar.DATE, 2);
        date = c.getTime();
        return Plan.contains(dateFormat.format(date));
      //   break;
      default: // Default
        return false;
      //   break;
    }
  }

  private void speichern(String string) {
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(getContext());
    SharedPreferences.Editor editor = sharedPref.edit();

    editor.putString("cache_website_morgen", string);

    editor.commit();
  }

  public void runjob(final int counter) {

    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(getContext());
    Boolean aktiv = prefs.getBoolean("Benachrichtigungan", false);
    final String wert_PW = prefs.getString("PW", "");
    final String wert_name = prefs.getString("BN", "");
    final String wert_klasse = prefs.getString("KL", "");
    final Boolean Lehrer = !(wert_klasse.matches(".*\\d+.*")); // true = Lehrer
    String url;

    Calendar calendar = Calendar.getInstance();
    final int day = calendar.get(Calendar.DAY_OF_WEEK);

    // nur morgen
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

    if (aktiv) {  // nur wenn an
      if ((!("".equals(wert_PW))) || (!("".equals(wert_name))) || (!("".equals(wert_klasse)
      ))) { // nur wenn alles eingestellt

        notification("Info", "Sync gestartet...", 1, 0); // wird in der Zwischenzeit angezeigt

        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {

                String Plan = response;

                String morgen_alt = prefs.getString("cache_website_morgen", "");
                String a = morgen_alt.replaceAll(" ", "");
                String b = a.replaceAll("\r", ""); // gereinigter alter String
                String c = Plan.replaceAll(" ", "");
                String d = c.replaceAll("\r", ""); // gereinigter neuer String
                String Klassenstufe;
                String Klassenkuerzel;
                if (Character.isLetter(wert_klasse
                    .charAt(wert_klasse.length()
                        - 1))) {   // wenn das letzte Zeichen ein Buchstabe ist -- String wird bei Lehrer nicht benutzt, nur bei Schüler
                  Klassenkuerzel = Character.toString(wert_klasse.charAt(wert_klasse.length() - 1));
                  Klassenstufe = wert_klasse.substring(0, wert_klasse.length() - 1);
                } else {
                  Klassenstufe = wert_klasse;
                  Klassenkuerzel = "";
                }

                String regex = ">" + Klassenstufe + ".*" + Klassenkuerzel + ".*<";

                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(Plan);

                if (PlanRichtig(day, Plan)) { // Stimmt das Datum?
                  if (!(b.equals(d))) { // ist der Plan neu? --> nur 1x benachrichtigen reicht
                    // Plan neu: Benachrichtigung wenn nötig (Plan ist wichtig)
                    if (Lehrer) { // Lehrer?
                      if (Plan
                          .contains(
                              wert_klasse)) { // Lehrer in Plan?  // Regex bei Lehrern nicht nötig
                        // Benachrichtigung an Lehrer --> neu und wichtig
                        notification("Achtung", "Sie haben morgen Vertretung oder Aufsicht!", 1,
                            1);
                      } else {
                        notification("Keine Vertretung",
                            "Sie haben morgen keine Vertretung oder Aufsicht.", 1, 0);
                      }
                    } else {
                      if (m.find()) {
                        // Benachrichtigung an Schüler --> neu und wichtig
                        notification("Achtung", "Sie haben morgen Vertretung!", 1, 1);
                      } else {
                        notification("Keine Vertretung", "Sie haben morgen keine Vertretung.", 1,
                            0);
                      }
                    }


                  } else {
                    notification("es hat funktioniert", "jaja", 2, 1);
                    closeNotification(1);
                  }
                  speichern(Plan);
                } else {
                  closeNotification(1);
                }

                // Response i.O.

              }
            }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            // Fehler
            notification("Fehler", "Ein Fehler beim Abrufen des Planes ist aufgetreten.", 1,
                1);
            // Job wird wegen Fehler erneut ausgeführt
            if (5 > counter) {
              SystemClock.sleep(1000);
              if ((error instanceof TimeoutError) || (error instanceof NoConnectionError)) {
                Crashlytics.logException(error.fillInStackTrace());
                runjob(counter + 1);
              }
              // funktioniert das?

            }
            Crashlytics.logException(error.fillInStackTrace());
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

      } else {
        notification("Fehler", "In den Einstellungen fehlt etwas", 1, 1);
      }
    }

  }

}
