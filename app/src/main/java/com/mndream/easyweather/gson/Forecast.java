package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/16.
 * 天气-预报信息
 */

public class Forecast {

    public String date;     //预报日期

    @SerializedName("tmp_max")
    public String max;          //最高气温

    @SerializedName("tmp_min")
    public String min;          //最低气温

    @SerializedName("cond_txt_d")
    public String conditionDay; //白天天气状况

    @SerializedName("cond_txt_n")
    public String conditionNight; //晚间天气状况

}
