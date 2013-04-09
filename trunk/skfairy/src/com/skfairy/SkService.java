package com.skfairy;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.skfairy.ProximityDetector.ProximityListener;
import com.skfairy.ShakeDetector.ShakeListener;

public class SkService extends Service {

	private ShakeDetector mShakeDetector;
	private ProximityDetector mProximityDetector;

	private boolean shake4call = false;
	// private int ringMode = AudioManager.RINGER_MODE_NORMAL;
	// private boolean silenced = false;
	private boolean isReadForAnswer = false;

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
					// if (silenced) {
					// silenced = false;
					// SkLog.d("set the ringmode back");
					// CallHelper.setRingModeBack(SkService.this, ringMode);
					// }
				}
			}
		}
	};

	// BroadcastReceiver for handling ACTION_SCREEN_OFF.
	public BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SkLog.d("onReceive intent action:" + intent.getAction());
			// Check action just to be on the safe side.
			// if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// shake4call = false;
			// SkLog.d("Register ACCELEROMETER listener when screen off");
			// mShakeDetector.registerListener();

			// SkLog.d("Register PROXIMITY listener when screen off");
			// mProximityDetector.registerListener();
			// }
		}
	};

	@Override
	public void onCreate() {
		mShakeDetector = new ShakeDetector(SkService.this);
		mShakeDetector.setOnShakeListener(new ShakeListener() {
			@Override
			public void onShake(String value) {
				// mShakeDetector.unRegisterListener();
				if (shake4call && isReadForAnswer) {
					CallHelper.answerCall(SkService.this);
					// CallHelper.silenceCall(SkService.this);
					// silenced = true;
				} else {
					SkLog.d("Get onShake event with value:" + value);
					// Locker.acquireCpuWakeLock(SkService.this);
					// try {
					// SkLog.d("Wait 2 seconds to light the screen on then release the wake lock");
					// Thread.sleep(2000);
					// } catch (InterruptedException e) {
					// }
					// Locker.releaseWakeLock();
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
