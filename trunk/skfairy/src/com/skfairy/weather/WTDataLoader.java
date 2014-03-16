package com.skfairy.weather;

import java.io.UnsupportedEncodingException;

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
			for (int k = 0; k < results.length(); k++) {
				JSONObject r = results.getJSONObject(k);
				JSONArray data = r.getJSONArray("weather_data");
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < data.length(); i++) {
					JSONObject d = data.getJSONObject(i);
					String dt = toUTF8(d.getString("date"));
					String wt = toUTF8(d.getString("weather"));
					String wd = toUTF8(d.getString("wind"));
					String t = toUTF8(d.getString("temperature"));
					sb.append(dt).append(",");
					sb.append(wt).append(",");
					sb.append(wd).append(",");
					sb.append(t).append(",\n");
				}
				// Only get the first result
				info = sb.toString();
				loaded = true;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = "Get weather info error:" + e.getLocalizedMessage();
		}

		SkLog.d("==============loadWeatherInfo:" + info);
		if (loaded) {
			remoteViews.setTextViewText(R.id.weatherInfo, info);
			wtWidget.updateWidget(wtContext);
		} else {
			Toast.makeText(wtContext, "Get weather info error:" + info, Toast.LENGTH_SHORT).show();
		}
	}

	private String toUTF8(String str) throws UnsupportedEncodingException {
		// return new String(str.getBytes(), "GBK"); //to out put GBK in the
		// console of Eclipse
		return new String(str.getBytes(), "UTF-8");
	}

	@Override
	protected String doInBackground(String... arg0) {
		SkLog.d("==============WTDataLoader.doInBackground");
		loadWeatherInfo();
		return "";
	}

}
