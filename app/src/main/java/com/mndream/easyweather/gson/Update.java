package com.mndream.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/21.
 * 天气-更新天气信息时间
 */

public class Update {

    @SerializedName("loc")
    public String updateTime;

}
