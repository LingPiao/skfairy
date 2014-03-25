package com.skfairy.weather;

import java.io.UnsupportedEncodingException;

import org.json.JSONObject;

import com.skfairy.Util;

public class WeatherInfo {
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
			wi.setDate(toUTF8(d.getString("date")));
			wi.setWeather(toUTF8(d.getString("weather")));
			wi.setWind(toUTF8(d.getString("wind")));
			wi.setTemperature(trimTemperature(toUTF8(d.getString("temperature"))));
			wi.setDayIcon(Util.extactPictureFromUrl(d.getString("dayPictureUrl")));
			wi.setNightIcon(Util.extactPictureFromUrl(d.getString("nightPictureUrl")));
		} catch (Exception e) {
			wi.setError("Get weather info error:" + e.getLocalizedMessage());
		}
		wi.setLoaded(true);
		return wi;
	}

	private static String trimTemperature(String tmp) {
		return tmp.replace(" ~ ", "/");
	}

	private static String toUTF8(String str) throws UnsupportedEncodingException {
		// return new String(str.getBytes(), "GBK"); //to out put GBK in the
		// console of Eclipse
		return new String(str.getBytes(), "UTF-8");
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
