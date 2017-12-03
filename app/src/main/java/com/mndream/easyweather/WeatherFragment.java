package com.mndream.easyweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mndream.easyweather.gson.WeatherFuture;
import com.mndream.easyweather.gson.WeatherPM25;
import com.mndream.easyweather.gson.WeatherToday;
import com.mndream.easyweather.service.AutoUpdateService;
import com.mndream.easyweather.util.HttpUtil;
import com.mndream.easyweather.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/12/1.
 *
 */

public class WeatherFragment extends Fragment{
    private final String SIGN = "59e4d7bda830fa4c19bd86b58f3856ba";
    private final String APP_KEY = "30131";

    private String mWeatherId;
    private Weather weather = new Weather();

    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private ImageView weatherBgPic;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleDate;
    private TextView nowDegreeText;
    private TextView nowConditionText;
    private TextView now1txt;
    private TextView now2txt;
    private TextView now3txt;
    private TextView now4txt;
    private TextView now5txt;
    private TextView now6txt;
    private LinearLayout nowAqi;
    private LinearLayout forecastLayout;
    private Button titleCityBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸状态栏
        if(Build.VERSION.SDK_INT >= 21){
            //透明状态栏
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        //首次启动设置
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .edit();
        editor.putBoolean("first_start",false);
        editor.apply();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.activity_weather,container,false);
        //初始化控件
        drawerLayout = v.findViewById(R.id.drawer_layout);
        weatherBgPic = v.findViewById(R.id.weather_bg_pic);
        weatherLayout = v.findViewById(R.id.weather_layout);
        titleCity = v.findViewById(R.id.title_city);
        titleDate = v.findViewById(R.id.title_date);
        nowDegreeText = v.findViewById(R.id.degree_text);
        nowConditionText = v.findViewById(R.id.weather_info_text);
        now1txt = v.findViewById(R.id.now_1_txt);
        now2txt = v.findViewById(R.id.now_2_txt);
        now3txt = v.findViewById(R.id.now_3_txt);
        now4txt = v.findViewById(R.id.now_4_txt);
        now5txt = v.findViewById(R.id.now_5_txt);
        now6txt = v.findViewById(R.id.now_6_txt);
        nowAqi = v.findViewById(R.id.now_aqi);
        forecastLayout = v.findViewById(R.id.forecast_layout);
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        titleCityBtn = v.findViewById(R.id.title_city_btn);
        titleCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        titleCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String weatherT = prefs.getString("weather1.2t",null);
        String weatherF = prefs.getString("weather1.2f",null);
        String weatherP = prefs.getString("weather1.2p",null);
        if(weatherT != null && weatherF != null && weatherP != null){
            //有缓存则直接解析天气数据
            WeatherFuture weatherFuture = Utility.handleWeatherFutureResponse(weatherF);
            WeatherToday weatherToday = Utility.handleWeatherTodayResponse(weatherT);
            WeatherPM25 weatherPM25 = Utility.handleWeatherPM25Response(weatherP);
            weather.today = weatherToday;
            weather.pm25 = weatherPM25;
            weather.future = weatherFuture;
            mWeatherId = weatherToday.result.weaid;
            showWeatherInfo(weather);
        }else{
            //无缓存则去服务器查询天气
            mWeatherId = getActivity().getIntent().getStringExtra("weather_id");
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

        return v;
    }
    /**
     * 从服务器加载天气背景图片
     */
    private void loadBgPic() {
        if(WeatherActivity.isNetworkConnected(MyApplication.getContext())){
            String requestBgPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
            HttpUtil.sendOkHttpRequest(requestBgPic, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String bgPic = Utility.handleBgPic(response.body().string());
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(getActivity())
                            .edit();
                    editor.putString("bg_pic", bgPic);
                    editor.apply();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getActivity()).load(bgPic).into(weatherBgPic);
                        }
                    });
                }
            });
        }else{
            Toast.makeText(getActivity(),
                    "请连接网络后重试",Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        if(WeatherActivity.isNetworkConnected(MyApplication.getContext())){
            //请求实时天气
            String weatherUrl = "http://api.k780.com/?app=weather.today&weaid=" + weatherId +
                    "&appkey=" + APP_KEY +
                    "&sign=" + SIGN +
                    "&format=json";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                String errorText = "获取实时天气信息失败";
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final WeatherToday weatherToday = Utility.handleWeatherTodayResponse(responseText);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(weatherToday != null && "1".equals(weatherToday.success)){
                                mWeatherId = weatherId; //更新id
                                //将天气数据缓存
                                weather.today = weatherToday;
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit();
                                editor.putString("weather1.2t",responseText);
                                editor.remove("weather");   //除去V1.0数据
                                editor.remove("weather6");   //除去V1.1数据
                                editor.apply();
                                requestFuture(weatherId);
                            }else{
                                Toast.makeText(getActivity(),
                                        errorText,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
            loadBgPic();    //更新天气背景图
        }else{
            Toast.makeText(getActivity(),
                    "请连接网络后重试",Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }

    }
    //请求天气预报
    public void requestFuture(final String weatherId){

        String weatherUrl = "http://api.k780.com/?app=weather.future&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取天气预报信息失败";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherFuture weatherFuture = Utility.handleWeatherFutureResponse(responseText);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherFuture != null && "1".equals(weatherFuture.success)){
                            //将天气数据缓存
                            weather.future = weatherFuture;
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .edit();
                            editor.putString("weather1.2f",responseText);
                            editor.apply();
                            requestPM25(weatherId);
                        }else{
                            Toast.makeText(getActivity(),
                                    errorText,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //请求PM25数据
    public void requestPM25(final String weatherId){

        String weatherUrl = "http://api.k780.com/?app=weather.pm25&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取PM25信息失败";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherPM25 weatherPM25 = Utility.handleWeatherPM25Response(responseText);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherPM25 != null && "1".equals(weatherPM25.success)){
                            //将天气数据缓存
                            weather.pm25 = weatherPM25;
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .edit();
                            editor.putString("weather1.2p",responseText);
                            editor.apply();
                            //展示天气数据
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(getActivity(),
                                    errorText,Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }
    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.today.result.citynm;   //城市名
        titleCity.setText(cityName);

        String updateTime = weather.today.result.date + "\n" + weather.today.result.week;  //时间
        titleDate.setText(updateTime);

        String degree =  weather.today.result.temp_curr + "℃";  //温度
        nowDegreeText.setText(degree);

        String conditionInfo = weather.today.result.condition;     //天气情况
        nowConditionText.setText(conditionInfo);

        String hum = weather.today.result.humidity ;        //实时湿度
        now1txt.setText(hum);

        String nowMinMaxText =weather.today.result.temp_low + "°～" + weather.today.result.temp_high + "°";   //温度范围
        now2txt.setText(nowMinMaxText);

        String wind = weather.today.result.wind;   //风向
        now3txt.setText(wind);

        String winp =  weather.today.result.winp;     //风力
        now4txt.setText(winp);

        String aqi = "空气质量："  + weather.pm25.result.aqi + "    " + weather.pm25.result.aqi_levid +"级    " + weather.pm25.result.aqi_levnm;         //aqi
        now5txt.setText(aqi);

        String remark = "运动建议：" + weather.pm25.result.aqi_remark;
        now6txt.setText(remark);

        if("-".equals(weather.pm25.result.aqi_levnm)){
            nowAqi.setVisibility(View.GONE);
        }else{
            nowAqi.setVisibility(View.VISIBLE);
        }
        //设置预报部分
        forecastLayout.removeAllViews();
        List<Forecast> forecastList = weather.future.forecastList;
        for(int i = 1; i<forecastList.size();i++){
            Forecast forecast = forecastList.get(i);
            //每个预报的天气项
            View view = LayoutInflater.from(getActivity())
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            String dateString = forecast.date.split("-")[1] + "." + forecast.date.split("-")[2] + "\n" + forecast.week;
            dateText.setText(dateString);

            TextView maxText = view.findViewById(R.id.max_text);
            String maxString = forecast.temp_high + "℃";
            maxText.setText(maxString);

            TextView minText = view.findViewById(R.id.min_text);
            String minString = forecast.temp_low + "℃";
            minText.setText(minString);


            TextView condText = view.findViewById(R.id.cond_text);
            condText.setText(forecast.condition);

            TextView windText = view.findViewById(R.id.wind_text);
            String windString = forecast.winp + "\n"+ forecast.wind;
            windText.setText(windString);

            forecastLayout.addView(view);
        }

        weatherLayout.setVisibility(View.VISIBLE);
        //启动自动更新服务
        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
        getActivity().startService(intent);
    }
}
