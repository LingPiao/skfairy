package com.skfairy;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class WindowLocker {

	public static void wakeup(Activity activity) {
		SkLog.d("WnidowLocker wake up the phone from sleep...");
		Window window = activity.getWindow();
		window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		SkLog.d("WnidowLocker wakeup finished");
	}

	public static void releaseWakeup(Activity activity) {
		SkLog.d("WnidowLocker clear flags from window...");
		Window window = activity.getWindow();
		window.clearFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.clearFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		SkLog.d("WnidowLocker clear flags finished");
	}
}
