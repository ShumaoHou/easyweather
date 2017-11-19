package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2017/11/16.
 * 天气总类
 */

public class Weather {

    public String status;               //请求结果

    public Basic basic;                 //今日天气基本信息

    public AQI aqi;                     //今日空气质量指数

    public Now now;                     //今日天气信息

    public Suggestion suggestion;       //今日天气建议

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList; //未来几天天气状况

}
