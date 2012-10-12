package io.yippie.iupb.app;

import io.yippie.iupb.lib.RestaurantManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Toast;

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
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				//TODO: Add R-String
				Toast.makeText(getApplicationContext(), "Leider ist ein Fehler aufgetreten. Vielleicht kein Internet?", Toast.LENGTH_LONG).show();
			}

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
        
        mainWebView.loadUrl(generateURL("restaurants"));
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        if(!hasConnection()){
            CharSequence text = "Keine Internet-Verbindung. Probier es nochmal, wenn du Internet hast!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }
    }
    
    public boolean hasConnection() { 
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo netInfo = cm.getActiveNetworkInfo(); 
        if (netInfo != null && netInfo.isConnectedOrConnecting()){ 
            return true;
        }
        else{
            return false;
        } 
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
    	return "http://" + getString(R.string.iupb_base_url) + "/" + target  + "?canvas=true&os=android&version=2beta";
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		mainWebView.clearHistory();
		switch(item.getItemId()) {
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
