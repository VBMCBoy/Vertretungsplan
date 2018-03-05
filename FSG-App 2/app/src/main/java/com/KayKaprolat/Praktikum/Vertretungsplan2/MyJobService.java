package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyJobService extends JobService {

  @Override
  public boolean onStartJob(JobParameters job) {

    // hier arbeiten

    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext());
    Boolean aktiv = prefs.getBoolean("Benachrichtigungan", false);
    final String wert_PW = prefs.getString("PW", "");
    final String wert_name = prefs.getString("BN", "");
    final String wert_klasse = prefs.getString("KL", "");
    final Boolean Lehrer = !(wert_klasse.matches(".*\\d+.*")); // true = Lehrer

    notification("Info", "Sync gestartet...", 1, 0); // wird in der Zwischenzeit angezeigt

    // nur für morgen
    if (aktiv) {  // nur wenn an
      if ((!("".equals(wert_PW))) || (!("".equals(wert_name))) || (!("".equals(wert_klasse)
      ))) { // nur wenn alles eingestellt

        // asynchron, weil Netzwerk

        new Thread() {
          @Override
          public void run() {
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

              // morgen

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
                if (!(b.equals(d))) { // ist der Plan neu?
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
                  // Plan alt: Benachrichtigung wenn nötig (Plan ist wichtig)

                  if (Lehrer) {
                    if (Plan.contains(wert_klasse)) {
                      // Benachrichtigung an Lehrer --> alt, aber wichtig
                      notification("Erinnerung", "Sie haben morgen Vertretung oder Aufsicht!",
                          1,
                          1);
                    } else {
                      // keine Benachrichtigung --> alt und unwichtig --> Sync-Benachrichtigung schließen
                      closeNotification(1);
                    }
                  } else {
                    if (m.find()) {
                      // Benachrichtigung an Schüler --> alt und wichtig
                      notification("Erinnerung", "Sie haben morgen Vertretung!", 1, 1);
                    } else {
                      // keine Benachrichtigung --> alt und unwichtig --> Sync-Benachrichtigung schließen
                      closeNotification(1);
                    }
                  }
                }
                speichern(Plan);
              } else {
                closeNotification(1);
              }

            } catch (Exception e) {
              e.printStackTrace();
              notification("Fehler", "Ein Fehler beim Abrufen des Planes ist aufgetreten.", 1,
                  1);

            } finally {
              if (urlConnection != null) {
                urlConnection.disconnect();

              }
            }
          }

        }.start();


      }


    }
    jobFinished(job, false);
    return false;
  }

  @Override
  public boolean onStopJob(JobParameters job) {
    return false; // Answers the question: "Should this job be retried?"
  }

  public void notification(String title, String text, Integer ID, Integer priority) {
    Context context = getApplicationContext();
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_stat_name).setContentTitle(title).setContentText(text);
    Intent resultIntent = new Intent(context, ViewerActivity.class);
    PendingIntent resultPendingIntent = PendingIntent
        .getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    if (priority == 1) {
      // hohe Priorität
      mBuilder.setVibrate(new long[]{1000, 1000, 1000});
    }

    notificationManager.notify(ID, mBuilder.build());
  }

  public void closeNotification(Integer ID) {
    Context context = getApplicationContext();
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
        .getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor editor = sharedPref.edit();

    editor.putString("cache_website_morgen", string);

    editor.commit();
  }

}
