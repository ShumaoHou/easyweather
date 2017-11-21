package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/16.
 * 天气-今日实况天气
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;  //温度

    @SerializedName("fl")
    public String feeling;  //体感温度

    @SerializedName("cond_txt")
    public String condition;    //天气状况

    @SerializedName("wind_dir")
    public String windDir;  //风向

    @SerializedName("wind_sc")
    public String windSc;  //风力

    @SerializedName("wind_spd")
    public String windSpd;  //风速，公里/小时

    public String hum;      //相对湿度

    public String pcpn;      //降水量

    public String pres;      //大气压强

    public String vis;      //能见度

    public String cloud;      //云量

}
