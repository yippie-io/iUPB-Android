package io.yippie.iupb.app;

import io.yippie.iupb.lib.RestaurantManager;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

	private WebView mainWebView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Let's display the progress in the activity title bar, like the
        // browser app does.
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarVisibility(true);
        
        //progressbar
        //createProgressDialog();

        mainWebView = (WebView) findViewById(R.id.webViewIUPB);
        
        mainWebView.setWebViewClient(new WebViewClient() {
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (Uri.parse(url).getHost().contains(getString(R.string.iupb_base_url)) ||
        				Uri.parse(url).getHost().contains("facebook.com")  ||
        				Uri.parse(url).getHost().contains("google.com") ) {
        	        view.loadUrl(url);
        	        return false;	
        		}else {
        			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	        startActivity(intent);
        			return true;
        		}
            }
        	
        });
        

        mainWebView.getSettings().setJavaScriptEnabled(true);

        final Activity activity = this;
        mainWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            	// Activities and WebViews measure progress with different scales.
            	// The progress meter will automatically disappear when we reach 100%
            	activity.setProgress(progress * 100);
            	if(progress == 100) {
            		activity.setProgressBarVisibility(false);
                }
            }
        });
        
        
        // test
        //new RestaurantManager(this).getAllRestaurants();
        // this fails because Android Emulator does not allow a network request from code?
        
        mainWebView.loadUrl(generateURL("/"));
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }
    	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mainWebView.canGoBack() && !mainWebView.getUrl().equals(generateURL(""))) {
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
    	return "http://" + getString(R.string.iupb_base_url) + "/" + target  + "?canvas=true&os=android&version=2beta";
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
			case android.R.id.home:
				mainWebView.loadUrl(generateURL(""));
				return true;
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
			case R.id.itemTwitter:
				mainWebView.loadUrl(generateURL("twitter"));
				return true;
			default:
				return super.onOptionsItemSelected(item); 
		}
	}

}
