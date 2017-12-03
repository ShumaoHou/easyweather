package com.mndream.easyweather.gson;

/**
 * Created by Administrator on 2017/12/1.
 * PM2.5 AQI
 */

public class WeatherPM25 {
    public String success;

    public PM25 result;

    public class PM25{

        public String aqi;      //aqi值

        public String aqi_levnm;        //等级

        public String aqi_levid;        //等级

        public String aqi_remark;       //建议
    }
}
