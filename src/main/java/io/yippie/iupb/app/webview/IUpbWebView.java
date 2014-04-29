package io.yippie.iupb.app.webview;

import android.content.*;
import android.net.*;
import android.util.*;
import android.webkit.*;

import org.androidannotations.annotations.*;

/**
 * TODO doc
 */
@EView
public class IUpbWebView extends WebView implements IUpbWebChromeClientCallback, IUpbWebClientCallback {
    @Bean
    IUpbWebViewClient webViewClient;
    @Bean
    IUpbWebChromeClient webChromeClient;
    IUpbWebViewCallback mCallback = sDUMMY_CALLBACK;

    public IUpbWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterInject
    void init() {
        webViewClient.setIUpbWebClientCallback(this);
        setWebViewClient(webViewClient);

        // enable javascript
        getSettings().setJavaScriptEnabled(true);
        getSettings().setUserAgentString(
                getSettings().getUserAgentString()
                        + " (iUPBAndroidNativeApp)"
        );

        webChromeClient.setCallback(this);
        setWebChromeClient(webChromeClient);
    }

    @Override
    public void setProgressValue(int progress) {
        mCallback.setProgressValue(progress);
    }

    @Override
    public void setUploadMessage(ValueCallback<Uri> uploadMsg) {
        mCallback.setUploadMessage(uploadMsg);
    }

    @Override
    public void displayOfflineNotice() {
        mCallback.displayOfflineNotice();
    }

    public void setCallback(IUpbWebViewCallback callback) {
        this.mCallback = callback;
    }

    public static final IUpbWebViewCallback sDUMMY_CALLBACK = new IUpbWebViewCallback() {
        @Override
        public void setProgressValue(int progress) {
            // dummy
        }

        @Override
        public void setUploadMessage(ValueCallback<Uri> uploadMsg) {
            // dummy
        }

        @Override
        public void displayOfflineNotice() {
            // dummy
        }
    };
}
