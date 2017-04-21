package com.skfairy.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.skfairy.SkLog;
import com.skfairy.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grap weather data from weather.com.cn
 */
public class WTCNDataLoader extends AsyncTask<String, String, String> {

    private static final String CITY_APPENDER = " > ";
    // ShangHai
    public String[] cities = new String[]{"上海"};

    private static final String BASE = "http://www.weather.com.cn/weather/{city}.shtml";
    private static final String CITY_HOLDER = "{city}";
    private static final String CITY_URL = "http://toy1.weather.com.cn/search?cityname={city}&callback=&";
    private static final String DEFAULT_REFFER = "http://www.weather.com.cn/weather/101021200.shtml";
    private static final String WEATH_DATA_LIST_SELECTOR = "ul.t.clearfix";
    private static final String LI = "li"; //or class name: "li.sky.skyid.lv"
    private static final SimpleDateFormat DF = new SimpleDateFormat("MM/dd");

    private static Map<String, String> cityCodes = new HashMap<>();

    private static final int TIME_OUT = 30000;

    private WTWidgetProvider wtWidget = null;
    private Context wtContext;
    private String errorMsg = "";
    private SharedPreferences preferences = null;
    private static List<String> days = new java.util.ArrayList<>();

    static {
        days.add("周日");
        days.add("周一");
        days.add("周二");
        days.add("周三");
        days.add("周四");
        days.add("周五");
        days.add("周六");

        cityCodes.put("罗平", "101290407");
        cityCodes.put("上海", "101020100");
        cityCodes.put("太仓", "101190408");
    }


    public WTCNDataLoader(WTWidgetProvider wt, Context wtContext) {
        this.wtWidget = wt;
        this.wtContext = wtContext;
        setCities();
    }

    private void setCities() {
        cities = ConfUtil.getConfiguredCities(wtContext);
    }

    public String[] getCities() {
        return cities;
    }


    private boolean loadWeatherInfo(String cityName) {

        String info = "Nothing got";
        boolean loaded = false;
        CityWeather cw = null;
        try {

            String cd = cityCodes.get(cityName);
            if (cd == null) {
                Document cityDoc = Jsoup.connect(CITY_URL.replace(CITY_HOLDER, cityName) + System.currentTimeMillis()).referrer(DEFAULT_REFFER).get();
                String s = cityDoc.text(); //([{"ref":"10102010003A~
                cd = s.substring(s.indexOf(":") + 2, s.indexOf("~"));
                cityCodes.put(cityName, cd);
            }

            Document doc = Jsoup.connect(BASE.replace(CITY_HOLDER, cityName)).get();
            Elements ds = doc.select(WEATH_DATA_LIST_SELECTOR);
            Elements wth = ds.select(LI);
            cw = new CityWeather(getDaysOfToday());
            for (int i = 0; i < wth.size(); i++) {
                if (i > 3) { //Get 4 days
                    break;
                }
                //System.out.println(wth.get(i).text());
                cw.addWeatherInfo(WeatherInfo.build(wth.get(i).text()));
            }
        } catch (Exception e) {
            SkLog.e("Get weather info error:", e);
            info = "Get weather info error:" + e.getMessage();
        }

        if (loaded) {
            // SkLog.d("==============loadedWeatherInfo:" + info);
            WeatherCache.getInstance(wtContext).addCache(cityName, cw);
        } else {
            SkLog.w("==============Get weather info error:" + info);
            errorMsg = info;
        }
        return loaded;
    }

    private String getDaysOfToday() {
        Date td = new Date(System.currentTimeMillis());
        return DF.format(td) + "/" + days.get(td.getDay());
    }


    @Override
    protected String doInBackground(String... arg0) {
        // SkLog.d("==============WTCNDataLoader.doInBackground");
        boolean loaded = false;
        WeatherCache.getInstance(wtContext).setLoading(true);
        if (WeatherCache.getInstance(wtContext).isCityChanged()) {
            setCities();
            WeatherCache.getInstance(wtContext).setCityChanged(false);
        }
        errorMsg = "";
        for (String c : cities) {
            loaded = loadWeatherInfo(c);
            if (!loaded)
                break;
        }
        if (loaded) {
            wtWidget.updateWeatherInfo(wtContext);
            WeatherCache.getInstance(wtContext).setLastLoaded();
        } else {
            if (errorMsg.length() > 1) {
                SkLog.d("============== doInBackground: showing error message");
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Util.msgBoxLong(wtContext, errorMsg);
                    }
                });
            }
        }
        WeatherCache.getInstance(wtContext).setLastLoadingSuccessful(loaded);
        WeatherCache.getInstance(wtContext).setLoading(false);
        return null;
    }

}
