package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/16.
 * 天气-预报信息
 */

public class Forecast {
    public String weaid;

    public String citynm;     //城市名

    @SerializedName("days")
    public String date;     //预报日期

    public String week;     //星期数

    public String temp_high;          //最低气温

    public String temp_low;          //最低气温

    @SerializedName("weather")
    public String condition; //天气状况

    public String wind;         //风向

    public String winp;         //风力

}
