package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/12/1.
 * 实时天气
 */

public class WeatherToday {

    public String success;

    public Today result;

    public class Today{
        public String weaid;

        public String citynm;     //城市名

        @SerializedName("days")
        public String date;     //预报日期

        public String week;     //星期数

        public String temp_curr;      //当前温度

        public String humidity;         //湿度

        @SerializedName("weather_curr")
        public String condition; //天气状况

        public String wind;         //风向

        public String winp;         //风力

        public String temp_high;          //最低气温

        public String temp_low;          //最低气温

    }

}
