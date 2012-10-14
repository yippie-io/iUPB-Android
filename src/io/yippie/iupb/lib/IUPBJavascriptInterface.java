package io.yippie.iupb.lib;

import android.content.Context;
import android.widget.Toast;

public class IUPBJavascriptInterface {
	Context mContext;

	/** Instantiate the interface and set the context */
	public IUPBJavascriptInterface(Context c) {
		mContext = c;
	}

	/** Show a toast from the web page */
	public void showToast(String toast) {
		Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
	}
}