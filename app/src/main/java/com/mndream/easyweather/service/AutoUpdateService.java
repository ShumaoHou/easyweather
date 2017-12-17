package com.mndream.easyweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.data.Entry;
import com.mndream.easyweather.MyApplication;
import com.mndream.easyweather.R;
import com.mndream.easyweather.WeatherPagerActivity;
import com.mndream.easyweather.db.SelectedCounty;
import com.mndream.easyweather.gson.Forecast;
import com.mndream.easyweather.gson.Weather;
import com.mndream.easyweather.gson.WeatherFuture;
import com.mndream.easyweather.gson.WeatherPM25;
import com.mndream.easyweather.gson.WeatherToday;
import com.mndream.easyweather.util.HttpUtil;
import com.mndream.easyweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private final String SIGN = "59e4d7bda830fa4c19bd86b58f3856ba";
    private final String APP_KEY = "30131";

    private List<SelectedCounty> mSelectedCountyList = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSelectedCountyList = DataSupport.findAll(SelectedCounty.class);
        if (WeatherPagerActivity.isNetworkConnected(MyApplication.getContext())){
            updateWeather();
            updateBgPic();
            //更新桌面小部件
            Intent updateIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
            sendBroadcast(updateIntent);
        }else{
            Toast.makeText(MyApplication.getContext(),
                    "自动更新不可用，请连接网络后重试",Toast.LENGTH_SHORT).show();
        }

        SharedPreferences prefs = getSharedPreferences("weather_auto_update",0);
        int rate = prefs.getInt("weather_auto_update_rate",2);

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int cycleTime = rate * 60 * 60 *1000; //一周期4小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + cycleTime;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this ,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateBgPic() {
        for (int i = 0; i < mSelectedCountyList.size(); i++){
            final SelectedCounty selectedCounty = mSelectedCountyList.get(i);
            Boolean isDIY = selectedCounty.getDIY();
            if(!isDIY){
                String requestBgPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
                HttpUtil.sendOkHttpRequest(requestBgPic, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String bgPic = Utility.handleBgPic(response.body().string());;
                        SelectedCounty updateCounty = new SelectedCounty();
                        updateCounty.setPicAuto(bgPic);
                        updateCounty.updateAll("weatherId = ?", selectedCounty.getWeatherId());
                    }
                });
            }
        }
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        for (int i = 0; i < mSelectedCountyList.size(); i++) {
            final SelectedCounty selectedCounty = mSelectedCountyList.get(i);
            requestWeather(selectedCounty.getWeatherId());
        }

    }

    private void requestWeather(final String weatherId){
        //请求实时天气
        String weatherUrl = "http://api.k780.com/?app=weather.today&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取实时天气信息失败_auto";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherToday weatherToday = Utility.handleWeatherTodayResponse(responseText);
                if(weatherToday != null && "1".equals(weatherToday.success)){
                    String countyName = weatherToday.result.citynm;   //城市名
                    String dateTime = weatherToday.result.date.split("-")[1]
                            + "."
                            + weatherToday.result.date.split("-")[2]
                            + "\n" + weatherToday.result.week;  //时间
                    //更新数据库
                    SelectedCounty updateCounty = new SelectedCounty();
                    updateCounty.setToday(responseText);
                    updateCounty.setTemp_curr(weatherToday.result.temp_curr);
                    updateCounty.setCountyName(countyName);
                    updateCounty.setUpdateTime(dateTime);
                    updateCounty.updateAll("weatherId = ?", weatherId);
                    requestFuture(weatherId);
                }else{
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void requestFuture(final String weatherId){
        //请求天气预报
        String weatherUrl = "http://api.k780.com/?app=weather.future&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取天气预报信息失败_auto";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherFuture weatherFuture = Utility.handleWeatherFutureResponse(responseText);

                if(weatherFuture != null && "1".equals(weatherFuture.success)){
                    List<Forecast> forecastList = weatherFuture.forecastList;
                    //第一预报的天气项
                    Forecast forecast = forecastList.get(0);
                    //天气图标
                    String iconFileName = forecast.weather_icon.split("/")[6];
                    String iconNum = iconFileName.substring(0,iconFileName.length()-4);
                    ApplicationInfo appInfo = getApplicationInfo();
                    int resID = getResources().getIdentifier("w"+iconNum, "drawable", appInfo.packageName);

                    //更新数据库
                    SelectedCounty updateCounty = new SelectedCounty();
                    updateCounty.setFuture(responseText);
                    updateCounty.setIconResId(resID);
                    updateCounty.updateAll("weatherId = ?", weatherId);
                    //更新桌面小部件
                    Intent updateIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
                    sendBroadcast(updateIntent);
                    requestPM25(weatherId);
                }else{
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void requestPM25(final String weatherId){
        //请求PM25数据
        String weatherUrl = "http://api.k780.com/?app=weather.pm25&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取PM25信息失败_auto";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherPM25 weatherPM25 = Utility.handleWeatherPM25Response(responseText);

                if(weatherPM25 != null && "1".equals(weatherPM25.success)){
                    //更新数据库
                    SelectedCounty updateCounty = new SelectedCounty();
                    updateCounty.setPm25(responseText);
                    updateCounty.updateAll("weatherId = ?", weatherId);
                }else{
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
