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

import com.skfairy.R;
import com.skfairy.SkLog;
import com.skfairy.Switch;
import com.skfairy.Util;

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

		if (isInternetConnected(context)) {
			Util.msgBox(context, R.string.widget_weather_loading);
			new WTDataLoader(this, context).execute();
		} else {
			Util.msgBox(context, R.string.widget_weather_no_inet);
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

	public void updateWidget(Context context, CityWeather cw) {

		remoteViews.setTextViewText(R.id.city, cw.getCity() + ">");

		WeatherInfo today = cw.getWeatherInfos().get(0);

		// Only get mm-dd
		remoteViews.setTextViewText(R.id.date, cw.getDate().substring(5).replace("-", "/") + "/" + today.getDate().substring(0, today.getDate().indexOf("(")));
		remoteViews.setTextViewText(R.id.weather, today.getWeather());
		remoteViews.setTextViewText(R.id.temperature, today.getTemperature());
		remoteViews.setImageViewResource(R.id.weatherIcon, Util.getTodayIconId(today.getDayIcon()));

		// Next1
		WeatherInfo d1 = cw.getWeatherInfos().get(1);
		remoteViews.setTextViewText(R.id.day1, d1.getDate());
		remoteViews.setTextViewText(R.id.day1Temperature, d1.getTemperature());
		remoteViews.setImageViewResource(R.id.day1Icon, Util.getDayIconId(d1.getDayIcon()));

		// Next2
		WeatherInfo d2 = cw.getWeatherInfos().get(2);
		remoteViews.setTextViewText(R.id.day2, d2.getDate());
		remoteViews.setTextViewText(R.id.day2Temperature, d2.getTemperature());
		remoteViews.setImageViewResource(R.id.day2Icon, Util.getDayIconId(d2.getDayIcon()));

		// Next3
		WeatherInfo d3 = cw.getWeatherInfos().get(3);
		remoteViews.setTextViewText(R.id.day3, d3.getDate());
		remoteViews.setTextViewText(R.id.day3Temperature, d3.getTemperature());
		remoteViews.setImageViewResource(R.id.day3Icon, Util.getDayIconId(d3.getDayIcon()));

		updateWidget(context);
	}

}
