package io.yippie.iupb.app;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends SherlockActivity {

	private static final String ASSETS_LOADING_HTML = "file:///android_asset/loading.html";
	private static final String ASSETS_OFFLINE_HTML = "file:///android_asset/offline.html";
	private WebView mMainWebView;
	private boolean offlineMode = false;

	/**
	 * the current url of the webView
	 */
	private String mCurrentUrl;

	/**
	 * @return the currentUrl
	 */
	private String getCurrentUrl() {
		return mCurrentUrl;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(getString(R.string.app_tag), "main activity started");
		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// Let's display the progress in the activity title bar, like the
		// browser app does.
		configureLayout();
		if (savedInstanceState == null)
			configureWebView();
	}

	/**
	 * 
	 */
	private void configureLayout() {
		getSupportActionBar().setHomeButtonEnabled(true);
		setSupportProgressBarVisibility(true);
		setSupportProgressBarIndeterminateVisibility(true);
		setContentView(R.layout.activity_main);
	}

	/**
	 * 
	 */
	private void configureWebView() {
		Log.i(getString(R.string.app_tag), "configuring webview");
		mMainWebView = (WebView) findViewById(R.id.webViewIUPB);

		mMainWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				// TODO: Add R-String
				displayOfflineNotice();
				Log.i(getString(R.string.app_tag), "Received error, code = "
						+ errorCode);
				// Toast.makeText(getApplicationContext(),
				// "Leider ist ein Fehler aufgetreten. Vielleicht kein Internet?",
				// Toast.LENGTH_LONG).show();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Uri.parse(url).getHost()
						.contains(getString(R.string.iupb_base_url))
						|| Uri.parse(url).getHost().contains("facebook.")
						|| (Uri.parse(url).getHost().contains("google.") && !url
								.toLowerCase().contains("/calendar"))) {
					view.loadUrl(url);
					return false;
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					startActivity(intent);
					return true;
				}
			}

		});

		// enable javascript
		mMainWebView.getSettings().setJavaScriptEnabled(true);
		// mMainWebView.addJavascriptInterface(new
		// IUPBJavascriptInterface(this),
		// "Android");
		mMainWebView.getSettings().setUserAgentString(
				mMainWebView.getSettings().getUserAgentString()
						+ " (iUPBAndroidNativeApp)");

		// configure the progress change
		mMainWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different
				// scales.
				// The progress meter will automatically disappear when we reach
				// 100%
				Log.i(getString(R.string.app_tag), "Loading progress:" + (progress * 100));
				MainActivity.this.setSupportProgressBarVisibility(true);
				MainActivity.this.setSupportProgressBarIndeterminateVisibility(true);
				MainActivity.this.setSupportProgress(progress * 100);
				if (progress == 100) {
					MainActivity.this.setSupportProgressBarVisibility(false);
					MainActivity.this.setSupportProgressBarIndeterminateVisibility(false);
				}
			}
		});

		// load loading url
		mMainWebView.loadUrl(ASSETS_LOADING_HTML);

		// if no default url exists, set it
		if (getCurrentUrl() == null)
			mCurrentUrl = generateURL("restaurants");
		mMainWebView.loadUrl(getCurrentUrl());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * )
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		this.invalidateOptionsMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// unregisterServices();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// registerOfflineHandling();
	}

	protected synchronized void removeOfflineNotice() {
		if (offlineMode) {
			offlineMode = false;
			if (mMainWebView.canGoBack())
				mMainWebView.goBack();
			else
				this.loadWebView(generateURL("restaurants"));
			mMainWebView.clearHistory();
		}
	}

	private synchronized void displayOfflineNotice() {
		if (!offlineMode) {
			offlineMode = true;
			mMainWebView.loadUrl(ASSETS_OFFLINE_HTML);
			mMainWebView.clearHistory();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mMainWebView.canGoBack()) {
			mMainWebView.goBack();
			return true;
		}
		// If it wasn't the Back key or there's no web page history, bubble up
		// to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private String generateURL(String target) {
		// get locale
		String locale = Locale.getDefault().getLanguage().contains("de") ? "de"
				: "en";
		return "http://" + getString(R.string.iupb_base_url) + "/" + locale
				+ "/" + target + "?canvas=true&os=android&version=2beta";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// mMainWebView.clearHistory();
		offlineMode = false;
		Log.i(getString(R.string.app_tag),
				"menu selected, item = " + item.getItemId());
		switch (item.getItemId()) {
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
		mCurrentUrl = url;
		mMainWebView.loadUrl(mCurrentUrl);
	}

}
