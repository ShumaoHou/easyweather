package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2017/11/16.
 * 天气预报 未来5-7天
 */

public class WeatherFuture {

    public String success;               //请求结果

    @SerializedName("result")
    public List<Forecast> forecastList; //未来几天天气状况

}
