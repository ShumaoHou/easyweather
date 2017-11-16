package com.example.easyweather.gson;

/**
 * Created by Administrator on 2017/11/16.
 * 天气-空气质量指数
 */

public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;      //空气质量指数

        public String pm25;     //pm2.5指数

    }
}
