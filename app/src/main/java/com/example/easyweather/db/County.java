package com.example.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/11/15.
 */

public class County extends DataSupport {

    private int id;

    private String mName;   //名称

    private int mWeatherId;  //天气代号

    private int mCityId;    //对应所属城市代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getWeatherId() {
        return mWeatherId;
    }

    public void setWeatherId(int weatherId) {
        mWeatherId = weatherId;
    }

    public int getCityId() {
        return mCityId;
    }

    public void setCityId(int cityId) {
        mCityId = cityId;
    }
}
