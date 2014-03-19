package com.skfairy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	public static final String AUTO_START_KEY = "start_key";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Intent i = new Intent(context, SkService.class);
			// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			SharedPreferences preferences = context.getSharedPreferences(Config.APP_CONFIG_KEY, Context.MODE_PRIVATE);
			if (preferences.getBoolean(Config.CONFIG_AUTO_START, false)) {
				context.startService(i);
			}
		}
	}

}
