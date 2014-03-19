package com.skfairy;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.TelephonyManager;

import com.skfairy.ProximityDetector.ProximityListener;
import com.skfairy.ShakeDetector.ShakeListener;

public class SkService extends Service {

	private ShakeDetector mShakeDetector;
	private ProximityDetector mProximityDetector;

	private boolean shake4call = false;
	private boolean isReadForAnswer = false;

	private Vibrator vibrator = null;

	// BroadcastReceiver for handling Phone call.
	public BroadcastReceiver callReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SkLog.d("onReceive intent action:" + intent.getAction());
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String state = extras.getString(TelephonyManager.EXTRA_STATE);
				SkLog.d("onReceive: state=" + state);
				if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
					shake4call = true;
					mProximityDetector.registerListener();
					mShakeDetector.registerListener();
					String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
					// ringMode = CallHelper.getRingMode(SkService.this);
					SkLog.d("onReceive: phoneNumber=" + phoneNumber + ", isReadForAnswer=" + isReadForAnswer);
				}
				if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
					SkLog.d("onReceive Phone IDLE");
					mShakeDetector.unRegisterListener();
					mProximityDetector.unRegisterListener();
				}
			}
		}
	};

	// BroadcastReceiver for handling ACTION_SCREEN_OFF.
	public BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SkLog.d("onReceive intent action:" + intent.getAction());
		}
	};

	@Override
	public void onCreate() {
		vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		mShakeDetector = new ShakeDetector(SkService.this);
		mShakeDetector.setOnShakeListener(new ShakeListener() {
			@Override
			public void onShake(String value) {
				// mShakeDetector.unRegisterListener();
				if (shake4call && isReadForAnswer) {
					boolean answered = CallHelper.answerCall(SkService.this);
					if (answered) {
						// Short vibrating and only once
						vibrator.vibrate(new long[] { 100, 300}, -1);
					}
				} else {
					SkLog.d("Get onShake event with value:" + value);
					notifyActivity(value);
				}
			}
		});

		mProximityDetector = new ProximityDetector(SkService.this);
		mProximityDetector.setOnProximityChanged(new ProximityListener() {
			@Override
			public void onChanged(float x, float maxRange) {
				SkLog.d("Proximity changed: value=" + x + ", maxRange=" + maxRange);
				if (x > 1) {
					isReadForAnswer = true;
				} else {
					isReadForAnswer = false;
				}
				notifyActivity("Proximity changed: value=" + x + ", maxRange=" + maxRange + "\n");
			}
		});

		SkLog.d("Register Intent receiver.");
		registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

		SkLog.d("Register Phone call receiver.");
		registerReceiver(callReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
	}

	private void notifyActivity(String value) {
		SkLog.d("Send a broadcast");
		Intent it = new Intent();
		it.setAction(Constants.SHAKE_ACTION);
		it.putExtra(Constants.SHAKE_EXTRA_VALUE, value);
		sendBroadcast(it);
	}

	@Override
	public void onDestroy() {
		SkLog.d("SkService on Destroy");
		mShakeDetector.unRegisterListener();
		mProximityDetector.unRegisterListener();
		unregisterReceiver(screenOffReceiver);
		unregisterReceiver(callReceiver);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
