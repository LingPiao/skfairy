package com.skfairy.weather;

import org.json.JSONObject;

import com.skfairy.SkLog;
import com.skfairy.Util;

import java.io.Serializable;

public class WeatherInfo implements Serializable {

	public static final String DATE_NAME = "date";
	public static final String RESULTS = "results";
	public static final String CURRENT_CITY = "currentCity";
	public static final String WEATHER_DATA = "weather_data";
	public static final String ERROR = "error";

	private static final String WIND = "wind";
	private static final String NIGHT_PICTURE_URL = "nightPictureUrl";
	private static final String DAY_PICTURE_URL = "dayPictureUrl";
	private static final String TEMPERATURE = "temperature";
	private static final String WEATHER = "weather";

	private static final String LEFT_PARENTHESIS = "(";
	private static final String BLANK = " ";
	private static final String TEMP_SEPARATOR = " ~ ";

	private String date;
	private String weather;
	private String temperature;
	private String wind;
	private String dayIcon;
	private String nightIcon;
	private boolean loaded = false;
	private String error;

	public static WeatherInfo build(JSONObject d) {
		WeatherInfo wi = new WeatherInfo();
		try {
			wi.setDate(getWeekDay(Util.toUTF8(d.getString(DATE_NAME))));
			wi.setWeather(truncateWeatherInfo(Util.toUTF8(d.getString(WEATHER))));
			wi.setWind(Util.toUTF8(d.getString(WIND)));
			wi.setTemperature(trimTemperature(Util.toUTF8(d.getString(TEMPERATURE))));
			wi.setDayIcon(Util.extactPictureFromUrl(d.getString(DAY_PICTURE_URL)));
			wi.setNightIcon(Util.extactPictureFromUrl(d.getString(NIGHT_PICTURE_URL)));
		} catch (Exception e) {
			SkLog.v("Get weather info error:" + e.getLocalizedMessage());
			wi.setLoaded(false);
			return wi;
		}
		wi.setLoaded(true);
		return wi;
	}

	private static String truncateWeatherInfo(String weatherInfo) {
		if (weatherInfo == null) {
			return null;
		}
		int i = weatherInfo.indexOf("转");
		if (i > 0)
			return weatherInfo.substring(0, i);
		return weatherInfo;
	}

	private static String trimTemperature(String tmp) {
		return tmp.replace(TEMP_SEPARATOR, Util.SLASH);
	}

	private static String getWeekDay(String dateStr) {
		if (dateStr == null)
			return null;
		int i = dateStr.indexOf(LEFT_PARENTHESIS);
		if (i > 0) {
			String r = dateStr.substring(0, i);
			i = r.indexOf(BLANK);
			if (i > 0) {
				return r.substring(0, i);
			}
			return r;
		}

		// 有时会有:  "date": "周日 05月10日",
		i = dateStr.indexOf(BLANK);
		if (i > 0) {
			return dateStr.substring(0, i);
		}

		return dateStr;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getWind() {
		return wind;
	}

	public void setWind(String wind) {
		this.wind = wind;
	}

	public String getDayIcon() {
		return dayIcon;
	}

	public void setDayIcon(String dayIcon) {
		this.dayIcon = dayIcon;
	}

	public String getNightIcon() {
		return nightIcon;
	}

	public void setNightIcon(String nightIcon) {
		this.nightIcon = nightIcon;
	}

	@Override
	public String toString() {
		return date + "," + weather + "," + temperature + "\n";
	}

}
