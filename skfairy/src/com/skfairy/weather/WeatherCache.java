package com.skfairy.weather;

import java.util.HashMap;
import java.util.Map;

import com.skfairy.SkLog;

public class WeatherCache {

	private int currentCityIndex = 0;

	private long lastLoaded = 0;
	// 1minute
	private final static long PERIOD = 40000;
	private boolean isLoading = false;
	private boolean isLastLoadingSuccessful = false;
	private boolean isCityChanged = false;

	private static WeatherCache instance = null;
	private Map<String, CityWeather> cache = new HashMap<String, CityWeather>();

	private WeatherCache() {

	}

	public static WeatherCache getInstance() {
		if (instance == null) {
			instance = new WeatherCache();
		}
		return instance;
	}

	public Map<String, CityWeather> getCachedWeatherInfos() {
		return cache;
	}

	public void addCache(String cityName, CityWeather cw) {
		cache.put(cityName, cw);
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
		cache.clear();
	}

}
