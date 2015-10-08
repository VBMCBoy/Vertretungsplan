package com.KayKaprolat.Praktikum.Vertretungsplan2;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.KayKaprolat.Praktikum.Vertretungsplan2.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.TextView;

@SuppressLint("JavascriptInterface")
public class ViewerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		final String wert_PW = intent.getExtras().getString("PW");
		final String wert_name = intent.getExtras().getString("BN");
		final String wert_klasse = intent.getExtras().getString("Kl");

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
					if (day == 1) {
						// Sonntag
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
					} else if (day == 2) {
						// Montag
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
					} else if (day == 3) {
						// Dienstag
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Dienstag.htm");
					} else if (day == 4) {
						// Mittwoch
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Mittwoch.htm");
					} else if (day == 5) {
						// Donnerstag
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Donnerstag.htm");
					} else if (day == 6) {
						// Freitag
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Freitag.htm");
					} else if (day == 7) {
						// Samstag
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");
					} else
						url = new URL(
								"http://www.sachsen.schule/~gym-grossroehrsdorf/docs/vt/Montag.htm");

					urlConnection = (HttpURLConnection) url.openConnection();
					InputStream in = new BufferedInputStream(
							urlConnection.getInputStream());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					for (int count; (count = in.read(buffer)) != -1;) {
						baos.write(buffer, 0, count);
					}

					final String Plan = new String(baos.toByteArray(),
							"windows-1252");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Document doc2 = Jsoup.parse(Plan, "windows-1252");

							Elements TEST = doc2.select("tr:contains(" // matches()
									// macht
									// keinen
									// Unterschied
									+ wert_klasse + ")");
							TEST.attr("bgcolor", "FFF007");

							webView.getSettings().setBuiltInZoomControls(true);
							webView.getSettings().setDisplayZoomControls(false);
							webView.loadData(doc2.html(), "text/html; charset=UTF-8",
									null);

						}
					});

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (urlConnection != null) {
						urlConnection.disconnect();
					}
				}
			}
		}.start();

	}
}