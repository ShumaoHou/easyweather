package com.mndream.easyweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.mndream.easyweather.MyApplication;
import com.mndream.easyweather.WeatherActivity;
import com.mndream.easyweather.WeatherFragment;
import com.mndream.easyweather.gson.Weather;
import com.mndream.easyweather.gson.WeatherFuture;
import com.mndream.easyweather.gson.WeatherPM25;
import com.mndream.easyweather.gson.WeatherToday;
import com.mndream.easyweather.util.HttpUtil;
import com.mndream.easyweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private final String SIGN = "59e4d7bda830fa4c19bd86b58f3856ba";
    private final String APP_KEY = "30131";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (WeatherActivity.isNetworkConnected(MyApplication.getContext())){
            updateWeather();
            updateBgPic();
        }else{
            Toast.makeText(MyApplication.getContext(),
                    "自动更新不可用，请连接网络后重试",Toast.LENGTH_SHORT).show();
        }

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int cycleTime = 60 * 60 * 1000; //一周期一小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + cycleTime;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this ,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateBgPic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isDIY = prefs.getBoolean("bg_pic_is_diy",false);//是否自定义背景
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
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this)
                            .edit();
                    editor.putString("bg_pic", bgPic);
                    editor.apply();
                }
            });
        }
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherT = prefs.getString("weather1.2t",null);
        String weatherF = prefs.getString("weather1.2f",null);
        String weatherP = prefs.getString("weather1.2p",null);
        if(weatherT != null && weatherF != null && weatherP != null){
            //获取天气ID
            WeatherToday weatherToday = Utility.handleWeatherTodayResponse(weatherT);
            String weatherId = weatherToday.result.weaid;
            requestWeather(weatherId);
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
                    //将天气数据缓存
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this)
                            .edit();
                    editor.putString("weather1.2t",responseText);
                    editor.apply();
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
                    //将天气数据缓存
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this)
                            .edit();
                    editor.putString("weather1.2f",responseText);
                    editor.apply();
                    requestPM25(weatherId);
                }else{
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void requestPM25(String weatherId){
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
                    //将天气数据缓存
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this)
                            .edit();
                    editor.putString("weather1.2p",responseText);
                    editor.apply();

                }else{
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
