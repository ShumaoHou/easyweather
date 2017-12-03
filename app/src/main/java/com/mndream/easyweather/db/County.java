package com.mndream.easyweather.db;

import org.litepal.crud.DataSupport;


/**
 * Created by Administrator on 2017/11/15.
 * 数据库映射对象，县
 */

public class County extends DataSupport {

    private int id;

    private String name;   //名称

    private String city;    //对应所属城市

    private String weaid;    //对应天气代码

    public long getId() {
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWeaid() {
        return weaid;
    }

    public void setWeaid(String weaid) {
        this.weaid = weaid;
    }

}
