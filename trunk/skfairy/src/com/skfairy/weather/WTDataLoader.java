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

	private static final String BASE = "http://api.map.baidu.com/telematics/v3/weather?location=%E4%B8%8A%E6%B5%B7&output=json&ak=640f3985a6437dad8135dae98d775a09";

	private static final int TIME_OUT = 30000;

	private WTWidgetProvider wtWidget = null;
	private Context wtContext;

	public WTDataLoader(WTWidgetProvider wt, Context wtContext) {
		this.wtWidget = wt;
		this.wtContext = wtContext;
	}

	private void loadWeatherInfo() {

		HttpGet request = new HttpGet(BASE);
		// JSONObject param = new JSONObject();
		// param.put("name", "Tom");
		// StringEntity se = new StringEntity(param.toString());
		// request.setEntity(se);
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParameters, TIME_OUT);

		HttpClient client = new DefaultHttpClient(httpParameters);

		// We have to set a proxy manually for the test sometimes
		// HttpHost proxy = new HttpHost("web-proxy.jpn.hp.com", 8080);
		// client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);

		HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
		String info = "Nothing got";
		HttpResponse httpResponse;
		boolean loaded = false;
		CityWeather cw = null;
		try {
			httpResponse = client.execute(request);

			String retSrc = EntityUtils.toString(httpResponse.getEntity());
			// SkLog.d("==============Got weather info :" + retSrc);
			JSONObject response = new JSONObject(retSrc);
			int error = response.getInt("error");
			if (error != 0) {
				info = "Error";
			}
			info = retSrc;
			// results ->weather_data[]
			cw = new CityWeather(response.getString("date"));
			JSONArray results = response.getJSONArray("results");
			for (int k = 0; k < results.length();) {
				JSONObject r = results.getJSONObject(k);
				String city = r.getString("currentCity");
				cw.setCity(city + " > ");
				JSONArray data = r.getJSONArray("weather_data");
				for (int i = 0; i < data.length(); i++) {
					cw.addWeatherInfo(WeatherInfo.build(data.getJSONObject(i)));
				}
				// Only get the first result
				loaded = true;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = "Get weather info error:" + e.getLocalizedMessage();
		}

		if (loaded) {
			SkLog.d("==============loadedWeatherInfo:" + info);
			// remoteViews.setTextViewText(R.id.weatherInfo, cw.toString());
			wtWidget.updateWidget(wtContext, cw);
		} else {
			SkLog.d("==============Get weather info error:" + info);
			Util.msgBox(wtContext, "Get weather info error:" + info);
		}
	}

	@Override
	protected String doInBackground(String... arg0) {
		SkLog.d("==============WTDataLoader.doInBackground");
		loadWeatherInfo();
		return "";
	}

}
