package com.skfairy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;

public class SKWidgetProvider extends AppWidgetProvider {

	private static final String SK_WIDGET_ACTION_CLICK = "android.sk.widget.action.click";
	private static final String SK_WIDGET_ACTION_OPERATOR_KEY = "SK_WIDGET_ACTION_OPERATOR_KEY";
	private RemoteViews remoteViews = null;
	private static boolean isGPRSEnabled = false;
	private static boolean wifiEnabled = false;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// SkLog.d("==============onUpdate");
		// if (remoteViews == null)
		// remoteViews = new RemoteViews(context.getPackageName(),
		// R.layout.sk_widget);
		//
		// final int len = appWidgetIds.length;
		// int wid = R.id.wifi;
		// for (int i = 0; i < len; i++) {
		// int appWidgetId = appWidgetIds[i];
		// Intent intent = new Intent(SK_WIDGET_ACTION_CLICK);
		// intent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, i);
		// SkLog.d("==============putExtra:" + i);
		// PendingIntent pi = PendingIntent.getBroadcast(context, i, intent, 0);
		// if (i == Switch.WIFI.getValue()) {
		// wid = R.id.wifi;
		// } else if (i == Switch.AIRPLANE.getValue()) {
		// wid = R.id.airplaneMode;
		// } else if (i == Switch.GPRS.getValue()) {
		// wid = R.id.gprs;
		// } else if (i == Switch.LOCK.getValue()) {
		// wid = R.id.locker;
		// }
		// remoteViews.setOnClickPendingIntent(wid, pi);
		// appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		// }
	}

	@Override
	public void onDisabled(Context context) {
		SkLog.d("==============onDisabled");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		SkLog.d("==============onDeleted");
	}

	@Override
	public void onEnabled(Context context) {
		SkLog.d("==============SKWidgetProvider.onEnabled");

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		remoteViews = new RemoteViews(context.getPackageName(), R.layout.sk_widget);

		SkLog.d("==============onReceive,action=" + intent.getAction());

		String act = intent.getAction();
		if (act.equals(SK_WIDGET_ACTION_CLICK)) {
			DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName devAdminReceiver = new ComponentName(context, Darclass.class);
			boolean admin = mDPM.isAdminActive(devAdminReceiver);
			int operator = 0;

			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				operator = bundle.getInt(SK_WIDGET_ACTION_OPERATOR_KEY, 0);
			} else {
				SkLog.d("==============bundle is null");
			}
			SkLog.d("==============onReceive,operator=" + operator);
			if (operator == Switch.WIFI.getValue()) {
				SkLog.d("==============WIFI  ");
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				wifiEnabled = wifiManager.isWifiEnabled();
				wifiManager.setWifiEnabled(!wifiEnabled);
				wifiEnabled = !wifiEnabled;
			} else if (operator == Switch.LOCK.getValue()) {
				if (admin) {
					mDPM.lockNow();
					SkLog.d("==============Screen locked.");
				} else {
					SkLog.d("Not an admin");
				}
			} else if (operator == Switch.AIRPLANE.getValue()) {
				Intent i = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
				SkLog.d("==============AirplaneMode");
			} else if (operator == Switch.GPRS.getValue()) {
				try {
					setMobileDataEnabled(context, !isGPRSEnabled);
					isGPRSEnabled = !isGPRSEnabled;
					if (isGPRSEnabled) {
						SkLog.d("==============GPRS enabled");
						remoteViews.setImageViewResource(R.id.gprs, R.drawable.gprs_enabled);
					} else {
						SkLog.d("==============GPRS disabled");
						remoteViews.setImageViewResource(R.id.gprs, R.drawable.gprs);
					}
				} catch (Exception e) {

				}
				SkLog.d("==============Gprs");
			} else if (operator == Switch.MODEL.getValue()) {
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				int m = am.getRingerMode();
				if (m == AudioManager.RINGER_MODE_NORMAL) {
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				} else if (m == AudioManager.RINGER_MODE_SILENT) {
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				} else {
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				}
			}

		} else if (act.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			wifiEnabled = wifiManager.isWifiEnabled();
		} else if (act.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
			// nothing to do
		} else if (act.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNet = conman.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mNet != null) {
				if (mNet.isAvailable() && mNet.isConnected()) {
					isGPRSEnabled = true;
				} else {
					isGPRSEnabled = false;
				}
			} else {
				isGPRSEnabled = false;
				SkLog.d("==============mNet is null, GPRS disabled");
			}
		}

		updateStatus(context);
	}

	private void updateStatus(Context context) {
		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
		wifiClick(context);
		airplanClick(context);
		phoneModelClick(context);
		moblieNetClick(context);
		lockClick(context);
		appWidgetManger.updateAppWidget(new ComponentName(context, SKWidgetProvider.class), remoteViews);
	}

	private void wifiClick(Context context) {
		Intent wifiIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		wifiIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.WIFI.getValue());
		PendingIntent wifiPi = PendingIntent.getBroadcast(context, Switch.WIFI.getValue(), wifiIntent, 0);
		if (wifiEnabled) {
			remoteViews.setImageViewResource(R.id.wifi, R.drawable.wifi_enabled);
		} else {
			remoteViews.setImageViewResource(R.id.wifi, R.drawable.wifi);
		}
		remoteViews.setOnClickPendingIntent(R.id.wifi, wifiPi);
	}

	private void phoneModelClick(Context context) {
		Intent mInt = new Intent(SK_WIDGET_ACTION_CLICK);
		mInt.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.MODEL.getValue());
		PendingIntent mPi = PendingIntent.getBroadcast(context, Switch.MODEL.getValue(), mInt, 0);

		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int m = am.getRingerMode();
		if (m == AudioManager.RINGER_MODE_NORMAL) {
			remoteViews.setImageViewResource(R.id.model, R.drawable.ring_on);
		} else if (m == AudioManager.RINGER_MODE_SILENT) {
			remoteViews.setImageViewResource(R.id.model, R.drawable.silent);
		} else {
			remoteViews.setImageViewResource(R.id.model, R.drawable.vibrate_on);
		}

		remoteViews.setOnClickPendingIntent(R.id.model, mPi);
	}

	private void moblieNetClick(Context context) {
		Intent gprsIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		gprsIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.GPRS.getValue());
		PendingIntent gprsPi = PendingIntent.getBroadcast(context, Switch.GPRS.getValue(), gprsIntent, 0);

		if (isGPRSEnabled) {
			SkLog.d("==============GPRS enabled");
			remoteViews.setImageViewResource(R.id.gprs, R.drawable.gprs_enabled);
		} else {
			SkLog.d("==============GPRS disabled");
			remoteViews.setImageViewResource(R.id.gprs, R.drawable.gprs);
		}

		remoteViews.setOnClickPendingIntent(R.id.gprs, gprsPi);
	}

	private void airplanClick(Context context) {
		Intent airplaneModeIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		airplaneModeIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.AIRPLANE.getValue());
		PendingIntent airplanePi = PendingIntent.getBroadcast(context, Switch.AIRPLANE.getValue(), airplaneModeIntent, 0);

		boolean airplaneEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		if (airplaneEnabled) {
			SkLog.d("==============AirplaneMode enabled");
			remoteViews.setImageViewResource(R.id.airplaneMode, R.drawable.ic_lock_airplane_mode_enabled);
		} else {
			SkLog.d("==============AirplaneMode disabled");
			remoteViews.setImageViewResource(R.id.airplaneMode, R.drawable.ic_lock_airplane_mode);
		}
		remoteViews.setOnClickPendingIntent(R.id.airplaneMode, airplanePi);
	}

	private void lockClick(Context context) {

		Intent mylockIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		mylockIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.LOCK.getValue());
		PendingIntent mylockPi = PendingIntent.getBroadcast(context, Switch.LOCK.getValue(), mylockIntent, 0);

		remoteViews.setOnClickPendingIntent(R.id.locker, mylockPi);
	}

	private void setMobileDataEnabled(Context context, boolean enabled) throws Exception {
		SkLog.d("==============setMobileDataEnabled:" + enabled);
		ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class<?> conmanClass = Class.forName(conman.getClass().getName());
		Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		Object iConnectivityManager = iConnectivityManagerField.get(conman);
		Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);

		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	}

}
