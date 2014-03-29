package com.skfairy.weather;

import java.util.HashMap;
import java.util.Map;

public class WeatherCache {

	private int currentCityIndex = 0;

	private long lastLoaded = 0;
	// 2minutes
	private final static long PERIOD = 120000;

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

	public boolean isUpdateRequired() {
		long now = System.currentTimeMillis();
		if (now - lastLoaded > PERIOD) {
			return true;
		}
		return false;
	}

	public void setLastLoaded() {
		this.lastLoaded = System.currentTimeMillis();
	}
}
