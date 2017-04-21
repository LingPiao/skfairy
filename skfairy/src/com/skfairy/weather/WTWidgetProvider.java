package com.skfairy.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.skfairy.R;
import com.skfairy.SkLog;
import com.skfairy.Switch;
import com.skfairy.Util;

import java.util.Map;

public class WTWidgetProvider extends AppWidgetProvider {

    private static final String WT_WIDGET_ACTION_CLICK = "android.sk.widget.wt.action.click";
    private static final String WT_WIDGET_ACTION_OPERATOR_KEY = "WT_WIDGET_ACTION_OPERATOR_KEY";
    private RemoteViews remoteViews = null;
    private WTCNDataLoader dataLoader = null;
    private long updateTime = System.currentTimeMillis();
    private static final long WEATHER_UPDATE_INTERVAL = 3600000; //An hour

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Nothing to do
        SkLog.d("==============WTWidgetProvider.onUpdate");
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
        if (dataLoader == null) {
            dataLoader = new WTCNDataLoader(this, context);
        }
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.wt_widget);
        }
        int operator = 0;
        if (act.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) || act.equals(WT_WIDGET_ACTION_CLICK)) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                operator = bundle.getInt(Switch.SWTICH_CITY.name(), 0);
            } else {
                SkLog.d("==============bundle is null");
            }
            SkLog.d("==============WTWidgetProvider.onReceive,operator=" + operator);
            if (operator == Switch.SWTICH_CITY.getValue()) {
                switchCity(context);
            } else {
                updateStatus(context);
            }
        } else if (act.equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {
            SkLog.d("==============WTWidgetProvider.WIFI_STATE_CHANGED");
            long current = System.currentTimeMillis();
            if (isUpdateRequired(updateTime, current)) {
                if (isNetEnable(context) && isInternetConnected(context)) {
                    SkLog.d("==============WIFI_STATE_CHANGED. Loading weather info");
                    dataLoader.execute();
                    updateTime = current;
                    updateStatus(context);
                }
            }
        }
    }

    private boolean isUpdateRequired(long updateTime, long current) {
        if ((current - updateTime) >= WEATHER_UPDATE_INTERVAL) {
            return true;
        }
        //Coming a new day
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(new java.util.Date(updateTime));
        int d1 = c.get(java.util.Calendar.DAY_OF_MONTH);
        c.setTime(new java.util.Date(current));
        int d2 = c.get(java.util.Calendar.DAY_OF_MONTH);
        return (d2 - d1) >= 1;
    }

    private boolean isNetEnable(Context context) {
        android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (wifiEnabled) {
            return true;
        }
        ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNet = conman.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mNet != null) {
            if (mNet.isAvailable() && mNet.isConnected()) {
                return true;
            }
        }
        return false;
    }

    private void weatherWidgetClick(Context context) {
        SkLog.d("==============WTWidgetProvider.weatherWidgetClick");
        if (WeatherCache.getInstance(context).isLoading()) {
            SkLog.d("==============WTWidgetProvider is loading weather data...");
            return;
        }

        if (WeatherCache.getInstance(context).isUpdateRequired()) {
            SkLog.d("==============WTWidgetProvider.weatherWidgetClick.isUpdateRequired");
            if (isInternetConnected(context)) {
                Util.msgBox(context, R.string.widget_weather_loading);
                dataLoader.execute();
            } else {
                if (WeatherCache.getInstance(context).getCachedWeatherInfos().size() > 0) {
                    // Update weather info from cache file if exists
                    updateWeatherInfo(context);
                } else {
                    SkLog.d("==============WTWidgetProvider.weatherWidgetClick.isUpdateRequired.noInternet");
                    Util.msgBox(context, R.string.widget_weather_no_inet);
                }
            }
        } else {
            Util.msgBox(context, R.string.widget_weather_msg_latest);
        }
        SkLog.d("==============WTWidgetProvider.weatherWidgetClick.setOnClick");
        Intent wtIntent = new Intent(WT_WIDGET_ACTION_CLICK);
        wtIntent.putExtra(WT_WIDGET_ACTION_OPERATOR_KEY, Switch.WEATHER.getValue());
        PendingIntent wtPi = PendingIntent.getBroadcast(context, Switch.WEATHER.getValue(), wtIntent, 9);

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

    public void updateWeatherInfo(Context context) {
        Map<String, CityWeather> wis = WeatherCache.getInstance(context).getCachedWeatherInfos();
        CityWeather cw = wis.get(dataLoader.getCities()[WeatherCache.getInstance(context).getCurrentCityIndex()]);
        updateWeatherInfo(context, cw);
    }

    private void updateWeatherInfo(Context context, CityWeather cw) {
        if (cw != null) {
            remoteViews.setTextViewText(R.id.city, cw.getCity());

            WeatherInfo today = cw.getWeatherInfos().get(0);

            remoteViews.setTextViewText(R.id.date, cw.getDate() + Util.SLASH + today.getDate());
            remoteViews.setTextViewText(R.id.weather, today.getWeather() + Util.BLANK_STRING + today.getWind());
            remoteViews.setTextViewText(R.id.temperature, today.getTemperature());
            remoteViews.setImageViewResource(R.id.weatherIcon, Util.getTodayIconId(today.getWeather()));

            // Next1
            WeatherInfo d1 = cw.getWeatherInfos().get(1);
            remoteViews.setTextViewText(R.id.day1, d1.getDate());
            remoteViews.setTextViewText(R.id.day1Temperature, d1.getTemperature());
            remoteViews.setImageViewResource(R.id.day1Icon, Util.getDayIconId(d1.getWeather()));

            // Next2
            WeatherInfo d2 = cw.getWeatherInfos().get(2);
            remoteViews.setTextViewText(R.id.day2, d2.getDate());
            remoteViews.setTextViewText(R.id.day2Temperature, d2.getTemperature());
            remoteViews.setImageViewResource(R.id.day2Icon, Util.getDayIconId(d2.getWeather()));

            // Next3
            WeatherInfo d3 = cw.getWeatherInfos().get(3);
            remoteViews.setTextViewText(R.id.day3, d3.getDate());
            remoteViews.setTextViewText(R.id.day3Temperature, d3.getTemperature());
            remoteViews.setImageViewResource(R.id.day3Icon, Util.getDayIconId(d3.getWeather()));
        }
        setOnClick(context);
        updateWidget(context);
    }

    private void setOnClick(Context context) {
        Intent wtIntent = new Intent(WT_WIDGET_ACTION_CLICK);
        wtIntent.putExtra(Switch.SWTICH_CITY.name(), Switch.SWTICH_CITY.getValue());
        PendingIntent swCityPi = PendingIntent.getBroadcast(context, Switch.SWTICH_CITY.getValue(), wtIntent, 9);
        remoteViews.setOnClickPendingIntent(R.id.city, swCityPi);
        remoteViews.setOnClickPendingIntent(R.id.weatherIcon, swCityPi);
    }

    private void switchCity(Context context) {
        int currentCityIndex = WeatherCache.getInstance(context).getCurrentCityIndex();
        int max = WeatherCache.getInstance(context).getCachedWeatherInfos().size() - 1;
        SkLog.d("==============WTWidgetProvider.switchCity(),max=" + max);
        if (max < 1)
            return;
        if (++currentCityIndex > max) {
            currentCityIndex = 0;
        }
        WeatherCache.getInstance(context).setCurrentCityIndex(currentCityIndex);
        updateWeatherInfo(context);
    }

}
