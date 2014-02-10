package io.yippie.iupb.lib;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class IUPBJavascriptInterface {
	private static final String TAG = "iupb";
	Context mContext;

	/** Instantiate the interface and set the context */
	public IUPBJavascriptInterface(Context c) {
		mContext = c;
	}

	/** Show a toast from the web page */
	public void showToast(String toast) {
		Log.v(TAG, "javascript triggers toast");
		Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
	}
}