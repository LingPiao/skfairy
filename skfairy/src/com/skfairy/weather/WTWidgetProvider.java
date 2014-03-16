package com.skfairy.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.skfairy.R;
import com.skfairy.SkLog;
import com.skfairy.Switch;

public class WTWidgetProvider extends AppWidgetProvider {

	private static final String WT_WIDGET_ACTION_CLICK = "android.sk.widget.wt.action.click";
	private static final String WT_WIDGET_ACTION_OPERATOR_KEY = "WT_WIDGET_ACTION_OPERATOR_KEY";
	private RemoteViews remoteViews = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Nothing to do
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
		SkLog.d("==============WTWidgetProvider.onEnabled");
		if (remoteViews == null) {
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.wt_widget);
		}
	}

	private boolean isInternetConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}
		return true;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String act = intent.getAction();
		SkLog.d("==============WTWidgetProvider.onReceive,action=" + act);
		if (act.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) || act.equals(WT_WIDGET_ACTION_CLICK)) {
			if (remoteViews == null) {
				remoteViews = new RemoteViews(context.getPackageName(), R.layout.wt_widget);
			}
			updateStatus(context);
		}
	}

	private void weatherWidgetClick(Context context) {
		SkLog.d("==============WTWidgetProvider.weatherWidgetClick");
		Intent wtIntent = new Intent(WT_WIDGET_ACTION_CLICK);
		wtIntent.putExtra(WT_WIDGET_ACTION_OPERATOR_KEY, Switch.WEATHER.getValue());
		PendingIntent wtPi = PendingIntent.getBroadcast(context, Switch.WEATHER.getValue(), wtIntent, 9);

		Toast.makeText(context, context.getString(R.string.widget_weather_loading), Toast.LENGTH_SHORT).show();
		if (isInternetConnected(context)) {
			new WTDataLoader(remoteViews, this, context).execute();
		} else {
			Toast.makeText(context, context.getString(R.string.widget_weather_no_inet), Toast.LENGTH_SHORT).show();
		}
		remoteViews.setOnClickPendingIntent(R.id.wtWidgetCtn, wtPi);
	}

	private void updateStatus(Context context) {
		SkLog.d("==============WTWidgetProvider.updateStatus");
		weatherWidgetClick(context);
		updateWidget(context);
	}

	public void updateWidget(Context context) {
		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
		appWidgetManger.updateAppWidget(new ComponentName(context, WTWidgetProvider.class), remoteViews);
	}

}
