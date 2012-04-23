package org.dyndns.sven_ola.debian_kit;
 
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
 
public class DownloadActivity extends Activity {
 
	private WebView webView;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repo);
 
		webView = (WebView)findViewById(R.id.web_repo);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://sven-ola.dyndns.org/repo/");
 
	}
 
}