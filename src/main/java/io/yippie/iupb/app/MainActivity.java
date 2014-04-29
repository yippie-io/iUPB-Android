package io.yippie.iupb.app;

import android.annotation.*;
import android.content.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.FragmentTransaction;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.*;

import java.util.*;

import io.yippie.iupb.app.webview.*;

/**
 * TODO doc
 */
@EActivity(R.layout.activity_main)
@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends SherlockActivity implements
        TabListener, OnNavigationListener, IUpbWebViewCallback {

    private static final String ASSETS_LOADING_HTML = "file:///android_asset/loading.html";
    private static final String ASSETS_OFFLINE_HTML = "file:///android_asset/offline.html";
    public final static int FILECHOOSER_REQUEST_CODE = 1;
    private final String TAG = getClass().getSimpleName();

    /**
     * the current url of the webView
     */
    private String mCurrentUrl;
    private boolean offlineMode = false;
    private ValueCallback<Uri> mUploadMessage;

    @StringArrayRes(R.array.menu_items_urls)
    String[] mMenuItemURLs;

    @ViewById(R.id.webViewIUPB)
    IUpbWebView mMainWebView;


    public void setProgressValue(int progress) {
        setSupportProgressBarVisibility(true);
        setSupportProgressBarIndeterminateVisibility(true);
        setSupportProgress(progress * 100);
        if (progress == 100) {
            setSupportProgressBarVisibility(false);
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public void setUploadMessage(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @AfterViews
    void afterViews() {
        mCurrentUrl = generateURL(getString(R.string.actionbar_restaurants_url));
        mMainWebView.setCallback(this);
        // ASSETS_LOADING_HTML is loaded to show that something's happening
        mMainWebView.loadUrl(ASSETS_LOADING_HTML);
        mMainWebView.loadUrl(mCurrentUrl);

        initActionBar();
        initNavigation();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        setSupportProgressBarVisibility(true);
        setSupportProgressBarIndeterminateVisibility(true);
    }

    private void initNavigation() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
                context, R.array.menu_items_labels,
                R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(list, this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILECHOOSER_REQUEST_CODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
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

    @Override
    public synchronized void displayOfflineNotice() {
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
        Log.i(TAG,
                "menu selected, item = " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                loadHomeScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
