package io.yippie.iupb.app;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.FragmentTransaction;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.*;
import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import java.util.*;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends SherlockActivity implements
        ActionBar.TabListener, ActionBar.OnNavigationListener {

    private static final String ASSETS_LOADING_HTML = "file:///android_asset/loading.html";
    private static final String ASSETS_OFFLINE_HTML = "file:///android_asset/offline.html";
    private final static int FILECHOOSER_RESULTCODE = 1;
    private static final String DEBUG_TAG = "iupb";

    /**
     * the current url of the webView
     */
    private String mCurrentUrl;
    private String[] mMenuItemURLs;
    private WebView mMainWebView;
    private boolean offlineMode = false;
    private ValueCallback<Uri> mUploadMessage;

    /**
     * @return the currentUrl
     */
    private String getCurrentUrl() {
        return mCurrentUrl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Let's display the progress in the activity title bar, like the
        // browser app does.
        configureLayout();
        mMenuItemURLs = getResources().getStringArray(R.array.menu_items_urls);
        configureWebView();
        configureActionbar();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("currentURL", mCurrentUrl);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String url = savedInstanceState.getString("currentURL");
        loadWebView(url);
    }

    /**
     *
     */
    private void configureActionbar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        setSupportProgressBarVisibility(true);
        setSupportProgressBarIndeterminateVisibility(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
                context, R.array.menu_items_labels,
                R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(list, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }

    /**
     *
     */
    private void configureLayout() {
        setContentView(R.layout.activity_main);
    }

    /**
     *
     */
    private void configureWebView() {
        Log.i(DEBUG_TAG, "configuring webview");
        mMainWebView = (WebView) findViewById(R.id.webViewIUPB);
        mMainWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // TODO: Add R-String
                displayOfflineNotice();
                Log.i(DEBUG_TAG, "Received error, code = "
                        + errorCode);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                final Uri uri = Uri.parse(url);
                if (uri.getHost() == null)
                    return false;
                if (uri.getHost()
                        .contains(getString(R.string.iupb_base_url))
                        || uri.getHost().contains("facebook.")
                        || (uri.getHost().contains("google.") && !url
                        .toLowerCase().contains("/calendar"))) {
                    view.loadUrl(url);
                    return false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            }

        });

        // enable javascript
        mMainWebView.getSettings().setJavaScriptEnabled(true);
        mMainWebView.getSettings().setUserAgentString(
                mMainWebView.getSettings().getUserAgentString()
                        + " (iUPBAndroidNativeApp)");

        //the below tries to enable android browsers to upload stuff
        final Activity context = this;

        // configure the progress change
        mMainWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different
                // scales.
                // The progress meter will automatically disappear when we reach
                // 100%
                Log.i(DEBUG_TAG, "Loading progress:"
                        + (progress * 100));
                MainActivity.this.setSupportProgressBarVisibility(true);
                MainActivity.this
                        .setSupportProgressBarIndeterminateVisibility(true);
                MainActivity.this.setSupportProgress(progress * 100);
                if (progress == 100) {
                    MainActivity.this.setSupportProgressBarVisibility(false);
                    MainActivity.this
                            .setSupportProgressBarIndeterminateVisibility(false);
                }
            }

            // For Android 3.0 - 4.0 (enables file upload)
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                Log.i(DEBUG_TAG, "Upload started");
                triggerUploadIntent(context, uploadMsg);
            }

            private void triggerUploadIntent(final Activity context,
                                             ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                context.startActivityForResult(Intent.createChooser(i, "Image Browser"), MainActivity.FILECHOOSER_RESULTCODE);
            }

            // For Android 4.1+ (enables file upload)
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                triggerUploadIntent(context, uploadMsg);
            }

            // For Android < 3.0 (enables file upload)
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                Log.i(DEBUG_TAG, "Upload started");
                openFileChooser(uploadMsg, "");
            }
        });

        // load loading url
        mMainWebView.loadUrl(ASSETS_LOADING_HTML);

        // if no default url exists, set it
        if (mCurrentUrl == null)
            mCurrentUrl = generateURL(getString(R.string.actionbar_restaurants_url));
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
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            invalidateOptionsMenu();
    }

    protected synchronized void removeOfflineNotice() {
        if (offlineMode) {
            offlineMode = false;
            if (mMainWebView.canGoBack())
                mMainWebView.goBack();
            else
                loadWebView(generateURL(getString(R.string.actionbar_restaurants_url)));
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
        offlineMode = false;
        Log.i(DEBUG_TAG,
                "menu selected, item = " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                loadHomeScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     *
     */
    private void loadHomeScreen() {
        loadWebView(generateURL(getString(R.string.actionbar_restaurants_url)));
    }

    /**
     * loads a specific view for the current webview
     */
    private void loadWebView(String url) {
        mCurrentUrl = url;
        offlineMode = false;
        mMainWebView.loadUrl(mCurrentUrl);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        loadWebView(generateURL((String) tab.getTag()));

    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        loadWebView(generateURL(mMenuItemURLs[itemPosition]));
        return true;
    }

}
