package com.example.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/11/15.
 * 数据库映射对象，县
 */

public class County extends DataSupport {

    private int id;

    private String name;   //名称

    private String weatherId;  //天气代号

    private int cityId;    //对应所属城市代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
