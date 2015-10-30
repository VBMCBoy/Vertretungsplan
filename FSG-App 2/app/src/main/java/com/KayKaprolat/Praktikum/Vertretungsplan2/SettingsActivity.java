package com.KayKaprolat.Praktikum.Vertretungsplan2;

import com.KayKaprolat.Praktikum.Vertretungsplan2.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String BN = settings.getString("BN", "");
		String PW = settings.getString("PW", "");
		String Kl = settings.getString("Kl", "");
		EditText PWtxt = (EditText) findViewById(R.id.txt_PW);
		EditText BNtxt = (EditText) findViewById(R.id.txt_BN);
		EditText Kltxt = (EditText) findViewById(R.id.txt_klasse);
		Kltxt.setText(Kl);
		BNtxt.setText(BN);
		PWtxt.setText(PW);

	}

	public void onButtonClick(View view) {

		if (view.getId() == R.id.btn_kontakt) {

			new AlertDialog.Builder(this)
					.setTitle("Kontakt")
					.setMessage(
							"Sie können mich unter kakaoh6@gmail.com erreichen."
									+ "\n"
									+ "Die App kann leider nur den exakten Ausruck finden, gibt es z.B. Vertretung für 4e wird Vertretung für 4de ignoriert und nicht angezeigt!")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// Tut erstmal nichts weiter
								}
							})

					.show();

		} else if (view.getId() == R.id.button1) {

			EditText Klasse = (EditText) findViewById(R.id.txt_klasse);
			String wert_klasse = Klasse.getText().toString();
			SettingsActivity.geteilt_klasse = wert_klasse;
			// Text aus den Textfeldern holen
			EditText Name = (EditText) findViewById(R.id.txt_BN);
			String wert_name = Name.getText().toString();


			EditText Passwort = (EditText) findViewById(R.id.txt_PW);
			String wert_PW = Passwort.getText().toString();

			Intent i1 = new Intent(this, ViewerActivity.class);
			i1.putExtra("PW", wert_PW);
			i1.putExtra("BN", wert_name);
			i1.putExtra("Kl", wert_klasse);
			startActivity(i1);

		} else {
		}

	}

	public static String geteilt_klasse = null;

	@Override
	protected void onStop() {
		EditText sBN = (EditText) findViewById(R.id.txt_BN);
		EditText sPW = (EditText) findViewById(R.id.txt_PW);
		EditText sKl = (EditText) findViewById(R.id.txt_klasse);
		String ssKl = sKl.getText().toString();
		String ssBN = sBN.getText().toString();
		String ssPW = sPW.getText().toString();
		String saveKl = ssKl;
		String saveBN = ssBN;
		String savePW = ssPW;
		super.onStop();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("PW", savePW);
		editor.putString("BN", saveBN);
		editor.putString("Kl", saveKl);
		editor.commit();
	}

}
