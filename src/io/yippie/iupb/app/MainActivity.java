package io.yippie.iupb.app;

import java.util.Locale;

import io.yippie.iupb.lib.IUPBJavascriptInterface;
import io.yippie.iupb.lib.VersionHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

	private static final String ASSETS_LOADING_HTML = "file:///android_asset/assets/loading.html";
	private static final String ASSETS_OFFLINE_HTML = "file:///android_asset/assets/offline.html";
	private WebView mainWebView;
	private boolean offlineMode = false;
	private BroadcastReceiver broadcastReceiver;
	private boolean broadcastReceiverRegistered = false;
	
	/**
	 * the current url of the webView
	 */
	private String currentUrl;
	private boolean alreadyConfigured = false;
	
    /**
	 * @return the currentUrl
	 */
	private String getCurrentUrl() {
		return currentUrl;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getString(R.string.app_tag), "main activity started");
		requestWindowFeature(Window.FEATURE_PROGRESS);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        // Let's display the progress in the activity title bar, like the
        // browser app does.
        configureLayout();
        
        //progressbar
        //createProgressDialog();

        configureWebView();
    }

	/**
	 * 
	 */
	private void configureLayout() {
        setContentView(R.layout.activity_main);
        setProgressBarVisibility(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			setProgressBarIndeterminateVisibility(true);
	}

	/**
	 * 
	 */
	private synchronized void configureWebView() {
		if (alreadyConfigured)
			return;
        Log.i(getString(R.string.app_tag), "configuring webview");
		mainWebView = (WebView) findViewById(R.id.webViewIUPB);
        
        mainWebView.setWebViewClient(new WebViewClient() {
        	@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				//TODO: Add R-String
				displayOfflineNotice();
				Log.i(getString(R.string.app_tag), "Received error, code = " + errorCode);
				//Toast.makeText(getApplicationContext(), "Leider ist ein Fehler aufgetreten. Vielleicht kein Internet?", Toast.LENGTH_LONG).show();
			}

			@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		if (Uri.parse(url).getHost().contains(getString(R.string.iupb_base_url)) ||
        				Uri.parse(url).getHost().contains("facebook.")  ||
        				(Uri.parse(url).getHost().contains("google.") && !url.toLowerCase().contains("/calendar"))) {
        	        view.loadUrl(url);
        	        return false;	
        		}else {
        			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        	        startActivity(intent);
        			return true;
        		}
            }
        	
        });
        
        //enable javascript
        mainWebView.getSettings().setJavaScriptEnabled(true);
        //mainWebView.addJavascriptInterface(new IUPBJavascriptInterface(this), "Android");
        mainWebView.getSettings().setUserAgentString(mainWebView.getSettings().getUserAgentString() + " (iUPBAndroidNativeApp)");
        
        //configure the progress change
        final Activity activity = this;
        mainWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            	// Activities and WebViews measure progress with different scales.
            	// The progress meter will automatically disappear when we reach 100%
        		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        			setProgressBarIndeterminateVisibility(true);
            	activity.setProgress(progress * 100);
            	if(progress == 100) {
            		activity.setProgressBarVisibility(false);
            		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            			setProgressBarIndeterminateVisibility(false);
                }
            }
        });
        
        //register receiver for connection changes
        //registerBroadcastReceiver(activity);
        //registerOfflineHandling();
        alreadyConfigured = true;
    	
        //load loading url
        mainWebView.loadUrl(ASSETS_LOADING_HTML);
        
        //if no default url exists, set it
        if (getCurrentUrl() == null)
        	currentUrl = generateURL("restaurants");
        mainWebView.loadUrl(getCurrentUrl());
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub   
		super.onConfigurationChanged(newConfig);  
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		    VersionHelper.refreshActionBarMenu(this);
	}

	/**
	 * @param activity
	 */
	private void registerBroadcastReceiver(final Activity activity) {
		if (broadcastReceiver != null) { 	
	    	broadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context conext, Intent intent) {
					Log.i(getString(R.string.app_tag), "Received broadcast event");
					ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
				    NetworkInfo netInfo = cm.getActiveNetworkInfo();
				    if(netInfo != null && netInfo.isConnectedOrConnecting()) {
						removeOfflineNotice();
				    }   
				    else {
						displayOfflineNotice();   
				    }
				}
	    		
	    	};
        }
	}
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		//unregisterServices();
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        //registerOfflineHandling();
	}

	private synchronized void registerOfflineHandling() {
    	if (!broadcastReceiverRegistered && broadcastReceiver != null) {
    		registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    		broadcastReceiverRegistered = true;
    	}
    }
    
    private synchronized void unregisterServices() {
    	if (broadcastReceiverRegistered) {
    		unregisterReceiver(broadcastReceiver);
    		broadcastReceiverRegistered = false;
    	}
    }
    
    protected synchronized void removeOfflineNotice() {
		if(offlineMode) {
			offlineMode = false;
			if (mainWebView.canGoBack())
				mainWebView.goBack();
			else
		        this.loadWebView(generateURL("restaurants"));
			mainWebView.clearHistory();
		}
	}

	private synchronized void displayOfflineNotice() {
		if(!offlineMode) {
			offlineMode = true;
			mainWebView.loadUrl(ASSETS_OFFLINE_HTML);
			mainWebView.clearHistory();
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
    	//get locale
    	String locale = Locale.getDefault().getDisplayLanguage().equals("de") ? "de" : "en";
    	return "http://" + getString(R.string.iupb_base_url) + "/" + locale + "/" + target  + "?canvas=true&os=android&version=2beta";
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		//mainWebView.clearHistory();
		offlineMode = false;
		Log.i(getString(R.string.app_tag), "menu selected, item = " + item.getItemId());
		switch(item.getItemId()) {
			case R.id.itemRestaurants:
			case android.R.id.home:
				loadHomeScreen();
				return true;
			case R.id.itemTransporation:
				loadWebView(generateURL("transportation"));
				return true;
			case R.id.itemAsta:
				loadWebView(generateURL("asta"));
				return true;
			case R.id.itemPaul:
				loadWebView(generateURL("courses"));
				return true;
			case R.id.itemTimetable:
				loadWebView(generateURL("timetable"));
				return true;
			case R.id.itemParties:
				loadWebView(generateURL("events"));
				return true;
			case R.id.itemWeather:
				loadWebView(generateURL("weather"));
				return true;
			case R.id.itemTwitter:
				loadWebView(generateURL("twitter"));
				return true;
			default:
				return super.onOptionsItemSelected(item); 
		}
	}

	/**
	 * 
	 */
	private void loadHomeScreen() {
		loadWebView(generateURL("restaurants"));
	}

	/**
	 * loads a specific view for the current webview
	 */
	private void loadWebView(String url) {
		currentUrl = url;
		mainWebView.loadUrl(currentUrl);
	}

}
