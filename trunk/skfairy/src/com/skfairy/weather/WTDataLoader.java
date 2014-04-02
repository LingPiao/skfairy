package com.skfairy.weather;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.skfairy.SkLog;
import com.skfairy.Util;

public class WTDataLoader extends AsyncTask<String, String, String> {

	private static final String CITY_SEPARATOR = " > ";
	// ShangHai, LuoPing
	public static final String[] cities = new String[] { "%E4%B8%8A%E6%B5%B7", "%E7%BD%97%E5%B9%B3" };

	// LuoPing
	// private static final String BASE =
	// "http://api.map.baidu.com/telematics/v3/weather?location=%E7%BD%97%E5%B9%B3&output=json&ak=640f3985a6437dad8135dae98d775a09";
	// ShangHai
	private static final String BASE = "http://api.map.baidu.com/telematics/v3/weather?location=";
	private static final String KEYS = "&output=json&ak=640f3985a6437dad8135dae98d775a09";

	private static final int TIME_OUT = 30000;

	private WTWidgetProvider wtWidget = null;
	private Context wtContext;
	private HttpClient client = null;
	private String errorMsg = "";

	public WTDataLoader(WTWidgetProvider wt, Context wtContext) {
		this.wtWidget = wt;
		this.wtContext = wtContext;
		initClient();
	}

	private void initClient() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);

		client = new DefaultHttpClient(httpParameters);

		// We have to set a proxy manually for the test sometimes
		// HttpHost proxy = new HttpHost("web-proxy.jpn.hp.com", 8080);
		// client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);

		HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
	}

	private boolean loadWeatherInfo(String cityName) {

		HttpGet request = new HttpGet(BASE + cityName + KEYS);

		String info = "Nothing got";
		HttpResponse httpResponse;
		boolean loaded = false;
		CityWeather cw = null;
		try {
			httpResponse = client.execute(request);
			String retSrc = EntityUtils.toString(httpResponse.getEntity());
			// SkLog.d("==============Got weather info :" + retSrc);
			JSONObject response = new JSONObject(retSrc);
			int error = response.getInt(WeatherInfo.ERROR);
			if (error != 0) {
				info = WeatherInfo.ERROR;
			}
			info = retSrc;
			// results ->weather_data[]
			cw = new CityWeather(response.getString(WeatherInfo.DATE_NAME));
			JSONArray results = response.getJSONArray(WeatherInfo.RESULTS);
			for (int k = 0; k < results.length();) {
				JSONObject r = results.getJSONObject(k);
				String city = r.getString(WeatherInfo.CURRENT_CITY);
				cw.setCity(city + CITY_SEPARATOR);
				JSONArray data = r.getJSONArray(WeatherInfo.WEATHER_DATA);
				for (int i = 0; i < data.length(); i++) {
					cw.addWeatherInfo(WeatherInfo.build(data.getJSONObject(i)));
				}
				// Only get the first result
				loaded = true;
				break;
			}
		} catch (Exception e) {
			info = "Get weather info error:" + e.getLocalizedMessage();
		}

		if (loaded) {
			// SkLog.d("==============loadedWeatherInfo:" + info);
			WeatherCache.getInstance().addCache(cityName, cw);
		} else {
			SkLog.w("==============Get weather info error:" + info);
			errorMsg = info;
		}
		return loaded;
	}

	@Override
	protected String doInBackground(String... arg0) {
		// SkLog.d("==============WTDataLoader.doInBackground");
		boolean loaded = false;
		boolean l = false;
		WeatherCache.getInstance().setLoading(true);
		errorMsg = "";
		for (String c : cities) {
			l = loadWeatherInfo(c);
			loaded = loaded || l;
		}
		if (loaded) {
			wtWidget.updateWeatherInfo(wtContext);
			WeatherCache.getInstance().setLastLoaded();
		} else {
			if (errorMsg.length() > 1)
				Util.msgBox(wtContext, errorMsg);
		}
		WeatherCache.getInstance().setLoading(false);
		return null;
	}

}
