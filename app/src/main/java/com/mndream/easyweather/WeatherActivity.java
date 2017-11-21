package com.mndream.easyweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mndream.easyweather.gson.Forecast;
import com.mndream.easyweather.gson.Weather;
import com.mndream.easyweather.service.AutoUpdateService;
import com.mndream.easyweather.util.HttpUtil;
import com.mndream.easyweather.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private String mWeatherId;

    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private ImageView weatherBgPic;
    private  ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView nowDegreeText;
    private TextView nowConditionText;
    private TextView nowMinMax;
    private TextView nowFeelingText;
    private TextView now1txt;
    private TextView now2txt;
    private TextView now3txt;
    private TextView now4txt;
    private TextView now5txt;
    private TextView now6txt;
    private TextView now7txt;
    private TextView now8txt;
    private LinearLayout forecastLayout;
    private Button titleCityBtn;

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
        drawerLayout = findViewById(R.id.drawer_layout);
        weatherBgPic = findViewById(R.id.weather_bg_pic);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        nowDegreeText = findViewById(R.id.degree_text);
        nowConditionText = findViewById(R.id.weather_info_text);
        nowMinMax = findViewById(R.id.now_min_max);
        nowFeelingText = findViewById(R.id.now_feeling);
        now1txt = findViewById(R.id.now_1_txt);
        now2txt = findViewById(R.id.now_2_txt);
        now3txt = findViewById(R.id.now_3_txt);
        now4txt = findViewById(R.id.now_4_txt);
        now5txt = findViewById(R.id.now_5_txt);
        now6txt = findViewById(R.id.now_6_txt);
        now7txt = findViewById(R.id.now_7_txt);
        now8txt = findViewById(R.id.now_8_txt);
        forecastLayout = findViewById(R.id.forecast_layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        titleCityBtn = findViewById(R.id.title_city_btn);
        titleCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather6",null);
        if(weatherString != null){
            //有缓存则直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.cityName;
            showWeatherInfo(weather);
        }else{
            //无缓存则去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        //界面初始化时设置天气背景图
        String bgPic = prefs.getString("bg_pic",null);
        if(bgPic != null){
            Glide.with(this).load(bgPic).into(weatherBgPic);
        }else{
            loadBgPic();
        }
    }

    /**
     * 从服务器加载天气背景图片
     */
    private void loadBgPic() {
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
                        .getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bg_pic", bgPic);
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
    public void requestWeather(final String weatherId) {
        String WEATHER_KEY = "f9b22264e8b040b4ad2bade41c6b53d0";
        String weatherUrl = "https://free-api.heweather.com/s6/weather?key=" + WEATHER_KEY + "&location=" + weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        "获取天气信息失败",Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
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
                            editor.putString("weather6",responseText);
                            editor.remove("weather");   //除去V1.0数据
                            editor.apply();
                            mWeatherId = weather.basic.cityName;
                            //展示天气数据
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,
                                    "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBgPic();    //更新天气背景图
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;   //城市名
        titleCity.setText(cityName);

        String updateTime = "更新于:" + weather.update.updateTime.split(" ")[1];  //更新时间
        titleUpdateTime.setText(updateTime);

        String degree = weather.now.temperature + "℃";  //温度
        nowDegreeText.setText(degree);

        String conditionInfo = weather.now.condition;     //天气情况
        nowConditionText.setText(conditionInfo);

        String nowMinMaxText = weather.forecastList.get(0).min+ "°/" + weather.forecastList.get(0).max + "°";   //温度范围
        nowMinMax.setText(nowMinMaxText);

        String feeling = "体感" + weather.now.feeling + "°";   //体感温度
        nowFeelingText.setText(feeling);

        String hum = "相对湿度\n" + weather.now.hum + "%";           //相对湿度
        now1txt.setText(hum);

        String windDir = "风向\n" + weather.now.windDir;   //风向
        now2txt.setText(windDir);

        String windSc = "风力\n" + weather.now.windSc + "级";     //风力
        now3txt.setText(windSc);

        String windSpd = "风速\n" + weather.now.windSpd +"km/h";   //风速
        now4txt.setText(windSpd);

        String pres = "大气压强\n" + weather.now.pres + "hPa";         //大气压强
        now5txt.setText(pres);

        String pcpn = "降水量\n" + weather.now.pcpn + "mm";         //降水量
        now6txt.setText(pcpn);

        String vis = "能见度\n" + weather.now.vis + "km";           //能见度
        now7txt.setText(vis);

        String cloud = "云量\n" + weather.now.cloud;       //云量
        now8txt.setText(cloud);

        //设置预报部分
        String[] dateCall = new String[]{"今天","明天","后天","大后天"};
        forecastLayout.removeAllViews();
        List<Forecast> forecastList = weather.forecastList;
        for(int i = 1; i<forecastList.size();i++){
            Forecast forecast = forecastList.get(i);
            //每个预报的天气项
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.data_text);
            String dateString = forecast.date.split("-")[1] + "." + forecast.date.split("-")[2];
            if(i<=3){
                dateString += "\t\t" + dateCall[i];
            }
            dateText.setText(dateString);
//            TextView infoText = view.findViewById(R.id.info_text);
//            infoText.setText(forecast.conditionDay + " " + forecast.conditionNight);
            TextView maxText = view.findViewById(R.id.max_text);
            String maxString = forecast.max + "℃";
            maxText.setText(maxString);
            TextView minText = view.findViewById(R.id.min_text);
            String minString = forecast.min + "℃";
            minText.setText(minString);
            TextView condDText = view.findViewById(R.id.cond_d_text);
            String condDString = forecast.conditionDay;
            condDText.setText(condDString);
            TextView condNText = view.findViewById(R.id.cond_n_text);
            String condNString = forecast.conditionNight;
            condNText.setText(condNString);
            forecastLayout.addView(view);
        }

        weatherLayout.setVisibility(View.VISIBLE);
        //启动自动更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
