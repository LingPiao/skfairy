package com.skfairy;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

public class Util {

	private static final String V411 = "4.1.1";

	public static String getAndroidVer() {
		return android.os.Build.VERSION.RELEASE;
	}

	public static void msgBox(Context context, int strId) {
		msgBox(context, context.getString(strId));
	}

	public static void msgBox(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static String getPhoneModel() {
		return android.os.Build.MODEL;
	}

	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	public static boolean isV411() {
		return V411.equals(getAndroidVer());
	}

	public static void printPhoneInfo() {
		SkLog.d("Phone info:");
		SkLog.d("DeviceName:" + getDeviceName());
		SkLog.d("Android version:" + getAndroidVer());
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

}
