package org.dyndns.sven_ola.debian_kit;
 
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
 
public class ReadmeActivity extends Activity {
 
	private WebView webView;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
 
		webView = (WebView)findViewById(R.id.web_about);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("file:///android_asset/debian-kit-en.html");
 
	}
 
}