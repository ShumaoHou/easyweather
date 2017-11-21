package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2017/11/16.
 * 天气总类
 */

public class Weather {

    public String status;               //请求结果

    public Basic basic;                 //地区名

    public Update update;               //更新时间

    public Now now;                     //实况天气

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList; //未来几天天气状况

}
