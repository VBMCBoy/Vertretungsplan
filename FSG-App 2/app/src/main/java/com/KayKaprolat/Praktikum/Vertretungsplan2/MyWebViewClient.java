package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.content.Context;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {

	MyWebViewClient(Context ctx) {
	}

	public MyWebViewClient() {
		// TODO Auto-generated constructor stub
	}

	public void onReceivedHttpAuthRequest(WebView view,
			HttpAuthHandler handler, String host, String realm) {
		String[] up = view.getHttpAuthUsernamePassword(host, realm);
		if (up != null && up.length == 2) {
			handler.proceed(up[0], up[1]);
		} else {
			Log.d("WebAuth", "Could not find user/pass for domain :" + host
					+ " with realm = " + realm);
		}

	}

	/*@Override
	public void onPageFinished(WebView view, String url) {
		view.loadUrl("javascript:HtmlViewer.showHTML"
				+ "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
		// view.loadData("<html></html>", "text/html", null);
		view.findAllAsync("\n" + SettingsActivity.geteilt_klasse + "\n");
	} */

}
