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
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;

public class SKWidgetProvider extends AppWidgetProvider {
	private static final String SK_WIDGET_ACTION_CLICK = "android.sk.widget.action.click";
	private static final String SK_WIDGET_ACTION_OPERATOR_KEY = "SK_WIDGET_ACTION_OPERATOR_KEY";
	private RemoteViews remoteViews = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		SkLog.d("==============onUpdate");
		if (remoteViews == null)
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.sk_widget);

		final int len = appWidgetIds.length;
		int wid = R.id.locker;
		for (int i = 0; i < len; i++) {
			int appWidgetId = appWidgetIds[i];
			Intent intent = new Intent(SK_WIDGET_ACTION_CLICK);
			intent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, i);
			SkLog.d("==============putExtra:" + i);
			PendingIntent pi = PendingIntent.getBroadcast(context, i, intent, 0);
			if (i == 0) {
				wid = R.id.locker;
			} else if (i == 1) {
				wid = R.id.airplaneMode;
			} else if (i == 2) {
				wid = R.id.gprs;
			}
			remoteViews.setOnClickPendingIntent(wid, pi);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
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
		if (remoteViews == null)
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.sk_widget);

		SkLog.d("==============SKWidgetIntentReceiver.onReceive(),action=" + intent.getAction());
		if (intent.getAction().equals(SK_WIDGET_ACTION_CLICK)) {
			DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			ComponentName devAdminReceiver = new ComponentName(context, Darclass.class);
			boolean admin = mDPM.isAdminActive(devAdminReceiver);
			if (admin) {

				int operator = 0;

				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					operator = bundle.getInt(SK_WIDGET_ACTION_OPERATOR_KEY, 0);
				}

				if (operator == 0) {
					// mDPM.lockNow();
					SkLog.d("==============Screen locked.");
				} else if (operator == 1) {
					Intent i = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(i);

					SkLog.d("==============AirplaneMode");
				} else if (operator == 2) {
					try {
						setMobileDataEnabled(context, false);
					} catch (Exception e) {

					}
					SkLog.d("==============Gprs");
				}

			} else {
				SkLog.d("Not an admin");
			}
		}

		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);

		Intent lockIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		lockIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, 0);
		PendingIntent lockPi = PendingIntent.getBroadcast(context, 0, lockIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.locker, lockPi);

		Intent airplaneModeIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		airplaneModeIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, 1);
		PendingIntent airplanePi = PendingIntent.getBroadcast(context, 1, airplaneModeIntent, 0);

		boolean airplaneEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		if (airplaneEnabled) {
			SkLog.d("==============AirplaneMode enabled");
			remoteViews.setImageViewResource(R.id.airplaneMode, R.drawable.ic_lock_airplane_mode_enabled);
		} else {
			SkLog.d("==============AirplaneMode disabled");
			remoteViews.setImageViewResource(R.id.airplaneMode, R.drawable.ic_lock_airplane_mode);
		}
		remoteViews.setOnClickPendingIntent(R.id.airplaneMode, airplanePi);

		Intent gprsIntent = new Intent(SK_WIDGET_ACTION_CLICK);
		gprsIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, 2);
		PendingIntent gprsPi = PendingIntent.getBroadcast(context, 2, gprsIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.gprs, gprsPi);

		appWidgetManger.updateAppWidget(new ComponentName(context, SKWidgetProvider.class), remoteViews);
	}

	private void setMobileDataEnabled(Context context, boolean enabled) throws Exception {
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
