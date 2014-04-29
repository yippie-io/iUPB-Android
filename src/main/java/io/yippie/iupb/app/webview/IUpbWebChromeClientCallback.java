package io.yippie.iupb.app.webview;

import android.net.*;
import android.webkit.*;

/**
 * TODO doc
 */
interface IUpbWebChromeClientCallback {
    void setProgressValue(int progress);

    void setUploadMessage(ValueCallback<Uri> uploadMsg);
}
