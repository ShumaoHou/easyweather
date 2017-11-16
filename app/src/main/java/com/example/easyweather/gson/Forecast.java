package com.example.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/16.
 * 天气-未来某天
 */

public class Forecast {

    public String date;     //预测日期

    @SerializedName("tmp")
    public Temperatrue temperatrue;

    @SerializedName("cond")
    public More more;

    public class Temperatrue{

        public String max;      //最高气温

        public String min;      //最低气温

    }

    public class More{

        @SerializedName("txt_d")
        public String info;         //未来天气状况

    }

}
