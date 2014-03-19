package com.skfairy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
			SkLog.d("==========:AIRPLANE_MODE_CHANGED_ACTION");
		}
	}

}
