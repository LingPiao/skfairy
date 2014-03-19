package com.skfairy.weather;

import java.util.ArrayList;
import java.util.List;

public class CityWeather {
	private String city;
	private List<WeatherInfo> weatherInfos = new ArrayList<WeatherInfo>();

	public CityWeather(String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public List<WeatherInfo> getWeatherInfos() {
		return weatherInfos;
	}

	public void addWeatherInfo(WeatherInfo weatherInfo) {
		weatherInfos.add(weatherInfo);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(city);
		sb.append(":");
		for (WeatherInfo wi : weatherInfos) {
			sb.append(wi.toString());
		}
		return sb.toString();
	}

}
