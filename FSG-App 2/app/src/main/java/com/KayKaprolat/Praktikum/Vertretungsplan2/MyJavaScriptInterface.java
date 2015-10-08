package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.webkit.JavascriptInterface;

public class MyJavaScriptInterface {

	public ViewerActivity m_WebView;

	MyJavaScriptInterface(ViewerActivity webView) {
		this.m_WebView = webView;
	}

	@JavascriptInterface
	public void showHTML(String html) {
		// new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
		// .setPositiveButton(android.R.string.ok, null)
		// .setCancelable(false).create().show();

		// MyJavaScriptInterface.geteilt_html = html;
		// Document doc = Jsoup.parse(html, null);
		// Elements metaElems = doc.select("Klasse");
		// m_WebView.changeHTML();

	}

}
