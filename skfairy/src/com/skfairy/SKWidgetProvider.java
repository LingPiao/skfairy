package com.skfairy;

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
import android.os.Handler;
import android.widget.RemoteViews;

public class SKWidgetProvider extends AppWidgetProvider {

    private static final String SK_WIDGET_ACTION_CLICK = "android.sk.widget.action.click";
    private static final String SK_WIDGET_ACTION_OPERATOR_KEY = "SK_WIDGET_ACTION_OPERATOR_KEY";
    private RemoteViews remoteViews = null;
    private static boolean isGPRSEnabled = false;
    private static boolean wifiEnabled = false;
    private static int powerClickCount = 0;

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
        SkLog.d("==============SKWidgetProvider.onEnabled");
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.sk_widget);
        }

        checkWifiStatus(context);
        checkGPRSStatus(context);

        updateStatus(context);
    }

    private void checkWifiStatus(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiEnabled = wifiManager.isWifiEnabled();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String act = intent.getAction();
        if (act.equals(Intent.ACTION_SCREEN_OFF) || act.equals(Intent.ACTION_SCREEN_OFF)) {
            return;
        }
        SkLog.d("==============onReceive,action=" + act);

        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.sk_widget);
        }

        checkWifiStatus(context);
        checkGPRSStatus(context);

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
                wifiManager.setWifiEnabled(!wifiEnabled);
                wifiEnabled = !wifiEnabled;
            } else if (operator == Switch.LOCK.getValue()) {
                if (admin) {
                    lockPhone(context, mDPM);
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
                    SkLog.w("Gprs.setMobileDataEnabled exception:" + e.getMessage());
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
            } else if (operator == Switch.REBOOT.getValue()) {
                SkLog.d("==============Reboot the device,cnt:" + powerClickCount);
                if (powerClickCount == 0) {
                    Util.msgBox(context, R.string.reboot_click);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            powerClickCount = 0;
                        }
                    }, 2000);
                } else if (powerClickCount == 1) {
                    Util.executeCommandViaSu("svc power reboot");
                    SkLog.d("==============Reboot the device");
                }
                powerClickCount++;
            }

        }
        // else if (act.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
        // checkWifiStatus(context);
        // } else if (act.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
        // // nothing to do
        // } else if (act.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        // checkGPRSStatus(context);
        // }

        updateStatus(context);
    }

    private void lockPhone(final Context context, final DevicePolicyManager mDPM) {
        if (Util.isV411()) {
            // KeyguardManager myKeyGuard = (KeyguardManager)
            // context.getSystemService(Context.KEYGUARD_SERVICE);
            // KeyguardLock myLock = myKeyGuard.newKeyguardLock("SFLock");
            // myLock.disableKeyguard();

            mDPM.lockNow();

            // SkLog.d("==============Using handler to lock");
            // Handler handlerUI = new Handler();
            // handlerUI.postDelayed(new Runnable() {
            // @Override
            // public void run() {
            // mDPM.lockNow();
            // SkLog.d("==============Using handler to lock:1");
            // }
            // }, 400);
            // Handler handlerUI2 = new Handler();
            // handlerUI2.postDelayed(new Runnable() {
            // @Override
            // public void run() {
            // mDPM.lockNow();
            // SkLog.d("==============Using handler to lock:2");
            // }
            // }, 800);
        } else {
            mDPM.lockNow();
        }
    }

    private void checkGPRSStatus(Context context) {
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

    private void updateStatus(Context context) {
        AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
        wifiClickListener(context);
        rebootClickListener(context);
        phoneModelClickListener(context);
        moblieNetClickListener(context);
        lockClickListener(context);
        appWidgetManger.updateAppWidget(new ComponentName(context, SKWidgetProvider.class), remoteViews);
    }

    private void wifiClickListener(Context context) {
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

    private void phoneModelClickListener(Context context) {
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

    private void moblieNetClickListener(Context context) {
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

//    private void airplanClick(Context context) {
//        Intent airplaneModeIntent = new Intent(SK_WIDGET_ACTION_CLICK);
//        airplaneModeIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.AIRPLANE.getValue());
//        PendingIntent airplanePi = PendingIntent.getBroadcast(context, Switch.AIRPLANE.getValue(), airplaneModeIntent, 0);
//
//        boolean airplaneEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
//        if (airplaneEnabled) {
//            SkLog.d("==============AirplaneMode enabled");
//            remoteViews.setImageViewResource(R.id.airplaneMode, R.drawable.ic_lock_airplane_mode_enabled);
//        } else {
//            SkLog.d("==============AirplaneMode disabled");
//            remoteViews.setImageViewResource(R.id.airplaneMode, R.drawable.ic_lock_airplane_mode);
//        }
//        remoteViews.setOnClickPendingIntent(R.id.airplaneMode, airplanePi);
//    }


    private void rebootClickListener(Context context) {
        Intent rebootIntent = new Intent(SK_WIDGET_ACTION_CLICK);
        rebootIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.REBOOT.getValue());
        PendingIntent rebootPi = PendingIntent.getBroadcast(context, Switch.REBOOT.getValue(), rebootIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.rebootMode, rebootPi);
    }

    private void lockClickListener(Context context) {
        Intent mylockIntent = new Intent(SK_WIDGET_ACTION_CLICK);
        mylockIntent.putExtra(SK_WIDGET_ACTION_OPERATOR_KEY, Switch.LOCK.getValue());
        PendingIntent mylockPi = PendingIntent.getBroadcast(context, Switch.LOCK.getValue(), mylockIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.locker, mylockPi);
    }


    private void setMobileDataEnabled(Context context, boolean enabled) throws Exception {
        SkLog.d("==============setMobileDataEnabled:" + enabled);
        //		ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //		Class<?> conmanClass = Class.forName(conman.getClass().getName());
        //		Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        //		iConnectivityManagerField.setAccessible(true);
        //		Object iConnectivityManager = iConnectivityManagerField.get(conman);
        //		Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
        //		Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        //		setMobileDataEnabledMethod.setAccessible(true);
        //
        //		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        String flag = "disable";
        if (enabled) {
            flag = "enable";
        }
        Util.executeCommandViaSu("svc data " + flag);
    }

}
