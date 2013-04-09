package com.skfairy;

import static com.skfairy.Constants.TAG;
import android.util.Log;

public class SkLog {

	private static boolean logEnabled = true;

	public static void d(String logMe) {
		if (logEnabled) {
			Log.d(TAG, logMe);
		}
	}

	public static void i(String logMe) {
		if (logEnabled) {
			Log.i(TAG, logMe);
		}
	}

	public static void v(String logMe) {
		if (logEnabled) {
			Log.v(TAG, logMe);
		}
	}

	public static void w(String logMe) {
		if (logEnabled) {
			Log.w(TAG, logMe);
		}
	}
}
