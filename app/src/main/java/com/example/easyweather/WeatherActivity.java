package com.example.easyweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.easyweather.gson.Forecast;
import com.example.easyweather.gson.Weather;
import com.example.easyweather.util.HttpUtil;
import com.example.easyweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private final String WEATHER_KEY = "f9b22264e8b040b4ad2bade41c6b53d0";

    private ImageView weatherBgPic;
    private  ScrollView weatherLayout;
    private LinearLayout forecastLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //沉浸状态栏
        if(Build.VERSION.SDK_INT >= 21){
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

       //初始化控件
        weatherBgPic = findViewById(R.id.weather_bg_pic);
        weatherLayout = findViewById(R.id.weather_layout);
        forecastLayout = findViewById(R.id.forecast_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.car_wash_text);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //有缓存则直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            //无缓存则去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        //设置天气背景图
        String bgPic = prefs.getString("bg_pic",null);
        if(bgPic != null){
            Glide.with(this).load(bgPic).into(weatherBgPic);
        }else{
            loadBgPic();
        }
    }

    /**
     * 加载天气背景图片
     */
    private void loadBgPic() {
        String requestBgPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBgPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bgPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bg_pic", null);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bgPic).into(weatherBgPic);
                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    private void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=" + WEATHER_KEY;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        "获取天气信息失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            //将天气数据缓存
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            //展示天气数据
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,
                                    "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBgPic();
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;   //城市名
        titleCity.setText(cityName);

        String updateTime = weather.basic.update.updateTime.split(" ")[1];  //更新时间
        titleUpdateTime.setText(updateTime);

        String degree = weather.now.temperature + "℃";  //温度
        degreeText.setText(degree);

        String weatherInfo = weather.now.more.info;     //天气情况
        weatherInfoText.setText(weatherInfo);

        String comfort = "舒适度：" + weather.suggestion.comfort.info;   //舒适度
        comfortText.setText(comfort);

        String carWash = "洗车指数：" + weather.suggestion.carWash.info;   //洗车指数
        comfortText.setText(carWash);

        String sport = "运动建议：" + weather.suggestion.sport.info;   //运动建议
        sportText.setText(sport);

        //设置预报部分
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            //每个预报的天气项
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.data_text);
            dateText.setText(forecast.date);
            TextView infoText = view.findViewById(R.id.info_text);
            infoText.setText(forecast.more.info);
            TextView maxText = view.findViewById(R.id.max_text);
            maxText.setText(forecast.temperatrue.max);
            TextView minText = view.findViewById(R.id.min_text);
            minText.setText(forecast.temperatrue.min);
            forecastLayout.addView(view);
        }

        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        weatherLayout.setVisibility(View.VISIBLE);
    }
}
