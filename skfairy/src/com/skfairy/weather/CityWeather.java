package com.skfairy.weather;

import com.skfairy.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CityWeather implements Serializable {
	private String city;
	private String date;
	private List<WeatherInfo> weatherInfos = new ArrayList<WeatherInfo>();

	public CityWeather(String date) {
		this.date = date;
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

	public String getDate() {
		if (date == null) {
			return "";
		}
		// yyyy-mm-dd, Only get mm/dd
		return date.substring(5).replace(Util.DATE_SEPARATOR, Util.SLASH);
	}

	public void setDate(String date) {
		this.date = date;
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
