package io.yippie.iupb.app.webview;

import android.content.*;
import android.net.*;
import android.util.*;
import android.webkit.*;

import org.androidannotations.annotations.*;

import io.yippie.iupb.app.*;

/**
 * TODO doc
 */
@EBean
public class IUpbWebViewClient extends WebViewClient {
    @RootContext
    Context mContext;
    private IUpbWebClientCallback mCallback = sDUMMY_CALLBACK;

    private final String TAG = getClass().getSimpleName();

    public void setIUpbWebClientCallback(IUpbWebClientCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        // TODO: Add R-String
        mCallback.displayOfflineNotice();
        Log.i(TAG, "Received error, code = "
                + errorCode);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        final Uri uri = Uri.parse(url);
        if (uri.getHost() == null)
            return false;
        if (uri.getHost()
                .contains(mContext.getString(R.string.iupb_base_url))
                || uri.getHost().contains("facebook.")
                || (uri.getHost().contains("google.") && !url
                .toLowerCase().contains("/calendar"))) {
            view.loadUrl(url);
            return false;
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
            return true;
        }
    }

    private static final IUpbWebClientCallback sDUMMY_CALLBACK = new IUpbWebClientCallback() {
        @Override
        public void displayOfflineNotice() {
            // dummy
        }
    };

}
