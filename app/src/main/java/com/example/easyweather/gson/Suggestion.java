package com.example.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/16.
 * 天气-建议
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("sport")
    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;     //舒适信息
    }

    public class CarWash{
        @SerializedName("txt")
        public String info;     //洗车信息
    }

    public class Sport{
        @SerializedName("txt")
        public String info;     //运动信息
    }
}
