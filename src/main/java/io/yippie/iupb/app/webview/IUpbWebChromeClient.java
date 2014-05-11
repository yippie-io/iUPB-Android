package io.yippie.iupb.app.webview;

import android.app.*;
import android.content.*;
import android.net.*;
import android.util.*;
import android.webkit.*;

import org.androidannotations.annotations.*;

import io.yippie.iupb.app.*;

/**
 * TODO doc
 * TODO why are there methods like openFileChooser? they are not used
 */
@EBean
public class IUpbWebChromeClient extends WebChromeClient {
    private IUpbWebChromeClientCallback mCallback = sDUMMY_CALLBACK;

    private final String TAG = getClass().getSimpleName();

    @RootContext
    Activity mActivity;

    public void  setCallback(IUpbWebChromeClientCallback callback) {
        this.mCallback = callback;
    }

    public void onProgressChanged(WebView view, int progress) {
        // Activities and WebViews measure progress with different scales.
        // The progress meter will automatically disappear when we reach 100%
        Log.v(TAG, "Loading progress: " + progress);
        mCallback.setProgressValue(progress);
    }

    // For Android 3.0 - 4.0 (enables file upload)
    void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        Log.i(TAG, "Upload started");
        triggerUploadIntent(uploadMsg);
    }

    private void triggerUploadIntent(
                                     ValueCallback<Uri> uploadMsg) {
        mCallback.setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        mActivity.startActivityForResult(Intent.createChooser(i, "Image Browser"), MainActivity.FILE_CHOOSER_REQUEST_CODE);
    }

    // For Android 4.1+ (enables file upload)
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        triggerUploadIntent(uploadMsg);
    }

    // For Android < 3.0 (enables file upload)
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        Log.i(TAG, "Upload started");
        openFileChooser(uploadMsg, "");
    }

    private static final IUpbWebChromeClientCallback sDUMMY_CALLBACK = new IUpbWebChromeClientCallback() {
        @Override
        public void setProgressValue(int progress) {
            // dummy
        }

        @Override
        public void setUploadMessage(ValueCallback<Uri> uploadMsg) {
            // dummy
        }
    };
}
