package com.example.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/11/15.
 */

public class City extends DataSupport {

    private int id;

    private String mName;   //名称

    private int mCode;      //代号

    private int mProvinceId;    //对应所属省份代号

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

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public int getProvinceId() {
        return mProvinceId;
    }

    public void setProvinceId(int provinceId) {
        mProvinceId = provinceId;
    }
}
