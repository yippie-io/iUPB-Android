package io.yippie.iupb.lib;

import android.app.Activity;

public class VersionHelper {
	public static void refreshActionBarMenu(Activity activity)
    {
        activity.invalidateOptionsMenu();
    }
}
