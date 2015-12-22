package com.skfairy;

import android.util.Log;

import static com.skfairy.Constants.TAG;

public class SkLog {

    private static boolean logEnabled = true;
    private static final String ERROR_MESS = "ERROR";

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

    public static void e(String logMe, Throwable tr) {
        Log.e(TAG, logMe, tr);
    }

}
