package com.skfairy.weather;

import android.content.Context;

import com.skfairy.SkLog;
import com.skfairy.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class WeatherCache {

    private int currentCityIndex = 0;

    private long lastLoaded = 0;
    // 1minute
    private final static long PERIOD = 40000;
    private boolean isLoading = false;
    private boolean isLastLoadingSuccessful = false;
    private boolean isCityChanged = false;

    private Context context;

    private static WeatherCache instance = null;
    private Map<String, CityWeather> cache = new HashMap<String, CityWeather>();

    private WeatherCache(Context context) {
        this.context = context;
    }

    public static WeatherCache getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherCache(context);
        }
        return instance;
    }

    public Map<String, CityWeather> getCachedWeatherInfos() {
        if (cache.size() < 1) {
            rebuildCacheFromFiles();
        }
        return cache;
    }

    public void addCache(String cityName, CityWeather cw) {
        cache.put(cityName, cw);
        cacheInFile(cityName, cw);
    }

    private void cacheInFile(String cityName, CityWeather cw) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        File dir = Util.getFileDirs(context);
        String filename = dir.getPath() + "/" + cityName;
        SkLog.d("Caching weather info in file[" + filename + "]...");

        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(cw);
            out.close();
        } catch (Exception ex) {
            SkLog.w("Caching weather info in file[" + filename + "] exception,message:" + ex.getMessage());
        }
    }

    private void rebuildCacheFromFiles() {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        File dir = Util.getFileDirs(context);

        String[] cities = ConfUtil.getConfiguredCities(context);
        for (int i = 0; i < cities.length; i++) {
            FileInputStream fis = null;
            ObjectInputStream in = null;
            String filename = dir.getPath() + "/" + cities[i];
            SkLog.d("Rebuilding weather info from file[" + filename + "]...");
            try {
                fis = new FileInputStream(filename);
                in = new ObjectInputStream(fis);
                CityWeather cw = (CityWeather) in.readObject();
                cache.put(cities[i], cw);
                in.close();
            } catch (Exception ex) {
                SkLog.w("Rebuilding weather info from file[" + filename + "] exception,message:" + ex.getMessage());
            }
        }
    }

    public int getCurrentCityIndex() {
        return currentCityIndex;
    }

    public void setCurrentCityIndex(int currentCityIndex) {
        this.currentCityIndex = currentCityIndex;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public void setLastLoadingSuccessful(boolean isLastLoadingSuccessful) {
        this.isLastLoadingSuccessful = isLastLoadingSuccessful;
    }

    public boolean isUpdateRequired() {
        if (!isLastLoadingSuccessful) {
            return true;
        }
        long now = System.currentTimeMillis();
        long diff = now - lastLoaded;
        if (diff > PERIOD) {
            return true;
        }
        SkLog.d("WeatherCache is not required to update, now-lastLoaded=" + diff);
        return false;
    }

    public void setLastLoaded() {
        this.lastLoaded = System.currentTimeMillis();
    }

    public boolean isCityChanged() {
        return isCityChanged;
    }

    public void setCityChanged(boolean isCityChanged) {
        clearCache();
        this.isCityChanged = isCityChanged;
    }

    public void clearCache() {
        String[] cities = ConfUtil.getConfiguredCities(context);
        File dir = Util.getFileDirs(context);

        for (int i = 0; i < cities.length; i++) {
            String filename = dir.getPath() + "/" + cities[i];
            SkLog.d("Cleaning cached file[" + filename + "]...");
            try {
                File f = new File(filename);
                f.delete();
            } catch (Exception ex) {
                SkLog.w("Cleaning cached file[" + filename + "] exception,message:" + ex.getMessage());
            }
        }
        cache.clear();
    }

}
