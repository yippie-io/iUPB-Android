package io.yippie.iupb.app;

import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

	private ProgressDialog progressDialog;
	private WebView mainWebView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //progressbar
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.progressbar_pleasewait));
        progressDialog.setCancelable(true);
		
        mainWebView = (WebView) findViewById(R.id.webViewIUPB);
        mainWebView.setWebViewClient(new WebViewClient() {
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (Uri.parse(url).getHost().equals(getString(R.string.iupb_base_url))) {
        	        view.loadUrl(url);
        	        return false;	
        		}else {
        			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	        startActivity(intent);
        			return true;
        		}
            }

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				progressDialog.hide();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				progressDialog.show();
			}
        	
        });
        
        mainWebView.loadUrl(generateURL("/"));
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mainWebView.canGoBack()) {
        	mainWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private String generateURL(String target) {
    	return "http://" + getString(R.string.iupb_base_url) + "/" + target  + "?canvas=true&os=android";
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
			case R.id.itemHome:
				mainWebView.loadUrl(generateURL(""));
				return true;
			case R.id.itemRestaurants:
				mainWebView.loadUrl(generateURL("restaurants"));
				return true;
			case R.id.itemTransporation:
				mainWebView.loadUrl(generateURL("transportation"));
				return true;
			case R.id.itemAsta:
				mainWebView.loadUrl(generateURL("asta"));
				return true;
			case R.id.itemPaul:
				mainWebView.loadUrl(generateURL("courses"));
				return true;
			case R.id.itemTimetable:
				mainWebView.loadUrl(generateURL("timetable"));
				return true;
			case R.id.itemWeather:
				mainWebView.loadUrl(generateURL("weather"));
				return true;
			default:
				return super.onOptionsItemSelected(item); 
		}
	}

}
