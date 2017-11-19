package com.mndream.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/11/15.
 * 数据库映射对象，市
 */

public class City extends DataSupport {

    private int id;

    private String name;   //名称

    private int code;      //代号

    private int provinceId;    //对应所属省份代号

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
