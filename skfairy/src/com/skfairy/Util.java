package com.skfairy;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.format.Time;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Util {

    public static final String BLANK_STRING = " ";
    public static final String EMPTY_STRING = "";
    public static final String SLASH = "/";
    public static final String DATE_SEPARATOR = "-";
    private static final String V411 = "4.1.1";
    private static Map<String, Integer> dayIcons = new HashMap<String, Integer>();
    private static Map<String, Integer> nightIcons = new HashMap<String, Integer>();
    private static final String UTF_8 = "UTF-8";

    static {

        dayIcons.put("xiaoyu.png", R.drawable.xiaoyu);
        dayIcons.put("dayu.png", R.drawable.dayu);
        dayIcons.put("baoyu.png", R.drawable.dayu);
        dayIcons.put("zhongyu.png", R.drawable.zhongyu);
        dayIcons.put("zhenyu.png", R.drawable.zhenyu);
        dayIcons.put("leizhenyu.png", R.drawable.zhenyu);

        dayIcons.put("xiaoxue.png", R.drawable.xiaoxue);
        dayIcons.put("daxue.png", R.drawable.daxue);
        dayIcons.put("zhongxue.png", R.drawable.zhongxue);
        dayIcons.put("yujiaxue.png", R.drawable.yujiaxue);

        dayIcons.put("duoyun.png", R.drawable.duoyun);
        dayIcons.put("leidian.png", R.drawable.leidian);
        // dayIcons.put("na.png", R.drawable.na);
        dayIcons.put("qing.png", R.drawable.qing);
        dayIcons.put("yin.png", R.drawable.yin);

        // ======================================================
        // all the night's pictures are prefixed with nt_
        nightIcons.put("dayu.png", R.drawable.nt_dayu);
        nightIcons.put("baoyu.png", R.drawable.nt_dayu);
        nightIcons.put("duoyun.png", R.drawable.nt_duoyun);
        nightIcons.put("leidian.png", R.drawable.nt_leidian);
        nightIcons.put("qing.png", R.drawable.nt_qing);
        nightIcons.put("xiaoyu.png", R.drawable.nt_xiaoyu);
        nightIcons.put("yin.png", R.drawable.nt_yin);

    }

    public static String getAndroidVer() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static String toUTF8(String str) throws UnsupportedEncodingException {
        // return new String(str.getBytes(), "GBK"); //to out put GBK in the
        // console of Eclipse
        return new String(str.getBytes(), UTF_8);
    }

    public static String encodeURLWithUTF8(String str) {
        try {
            return URLEncoder.encode(str, UTF_8);
        } catch (UnsupportedEncodingException e) {
            SkLog.w("Encoding city exception:" + e.getMessage());
        }
        return str;
    }

    public static void msgBox(Context context, int strId) {
        msgBox(context, context.getString(strId));
    }

    public static void msgBox(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void msgBoxLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + BLANK_STRING + model;
        }
    }

    public static boolean isV411() {
        return V411.equals(getAndroidVer());
    }

    public static void printPhoneInfo() {
        SkLog.d("Phone info:");
        SkLog.d("DeviceName:" + getDeviceName());
        SkLog.d("Android version:" + getAndroidVer());
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return EMPTY_STRING;
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String extactPictureFromUrl(String url) {
        if (url == null || url.trim() == EMPTY_STRING) {
            return null;
        }
        return url.substring(url.lastIndexOf(SLASH) + 1);
    }

    public static int getDayIconId(String weatherPic) {
        Integer p = dayIcons.get(weatherPic);
        if (p == null) {
            return R.drawable.na;
        }
        return p.intValue();
    }

    public static int getTodayIconId(String weatherPic) {
        Time now = new Time();
        now.setToNow();
        Integer p = null;
        if (now.hour >= 19 || now.hour < 4) {
            p = nightIcons.get(weatherPic);
        }
        if (p == null) {
            p = dayIcons.get(weatherPic);
        }
        if (p == null) {
            p = R.drawable.na;
        }
        return p;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getFileDirs(Context context) {
        if (isExternalStorageReadable()) {
            return context.getExternalFilesDir(null);
        } else {
            return context.getFilesDir();
        }
    }

    public static boolean executeCommandViaSu(String command) {
        boolean success = false;
        String su = "/system/xbin/su";
        try {
            SkLog.d("Execute the cmd[" + su + " -c " + command + "]...");
            // Execute command as "su".
            Runtime.getRuntime().exec(new String[]{su, "-c", command});
        } catch (IOException e) {
            success = false;
            SkLog.w("Oops! Cannot execute the cmd for some reason:" + e.getMessage());
        } finally {
            success = true;
            SkLog.d("Execute the cmd successfully.");
        }
        return success;
    }

    public static boolean executeCommand(String command) {
        boolean success = false;
        try {
            SkLog.d("Execute the cmd[" + command + "]...");
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            SkLog.w("Oops! Cannot execute the cmd for some reason:" + e.getMessage());
        } finally {
            success = true;
            SkLog.d("Execute the cmd successfully.");
        }
        return success;
    }

}
