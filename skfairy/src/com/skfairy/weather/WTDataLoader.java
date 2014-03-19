package com.skfairy.weather;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.skfairy.R;
import com.skfairy.SkLog;

public class WTDataLoader extends AsyncTask<String, String, String> {
	private RemoteViews remoteViews = null;

	private static final String BASE = "http://api.map.baidu.com/telematics/v3/weather?location=%E4%B8%8A%E6%B5%B7&output=json&ak=640f3985a6437dad8135dae98d775a09";

	private WTWidgetProvider wtWidget = null;
	private Context wtContext;

	public WTDataLoader(RemoteViews remoteViews, WTWidgetProvider wt, Context wtContext) {
		this.remoteViews = remoteViews;
		this.wtWidget = wt;
		this.wtContext = wtContext;
	}

	private void loadWeatherInfo() {

		HttpGet request = new HttpGet(BASE);
		// JSONObject param = new JSONObject();
		// param.put("name", "Tom");
		// StringEntity se = new StringEntity(param.toString());
		// request.setEntity(se);
		HttpClient client = new DefaultHttpClient();
		HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
		String info = "Nothing get";
		HttpResponse httpResponse;
		boolean loaded = false;
		CityWeather cw = null;
		try {
			httpResponse = client.execute(request);

			String retSrc = EntityUtils.toString(httpResponse.getEntity());
			JSONObject response = new JSONObject(retSrc);
			int error = response.getInt("error");
			if (error != 0) {
				info = "Error";
			}
			// results ->weather_data[]

			JSONArray results = response.getJSONArray("results");
			for (int k = 0; k < results.length();) {
				JSONObject r = results.getJSONObject(k);
				String city = r.getString("currentCity");
				cw = new CityWeather(city);
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

		SkLog.d("==============loadWeatherInfo:" + info);
		if (loaded) {
			remoteViews.setTextViewText(R.id.weatherInfo, cw.toString());
			wtWidget.updateWidget(wtContext);
		} else {
			Toast.makeText(wtContext, "Get weather info error:" + info, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected String doInBackground(String... arg0) {
		SkLog.d("==============WTDataLoader.doInBackground");
		loadWeatherInfo();
		return "";
	}

}
