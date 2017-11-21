package com.mndream.easyweather.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Administrator on 2017/11/21.
 * 天气数据库
 */
@Database(name = WeatherDatabase.NAME, version = WeatherDatabase.VERSION)
public class WeatherDatabase {

    public static final String NAME = "WeatherDatabase";    //数据库名

    public static final int VERSION = 1;                    //版本号
}
