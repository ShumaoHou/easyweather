package com.mndream.easyweather.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

/**
*   @app    易天气
*   @author mndream
*   @date   2017/12/9
 *   已选择的城市的信息
**/

public class SelectedCounty  extends DataSupport{

    private int id;

    private String weatherId;   //对应的天气id

    private String countyName;  //城市名

    private String temp_curr;   //当前温度

    private int iconResId;   //当前天气图标路径

    private String updateTime;  //更新时间

    private String today;   //实况天气Json

    private String future;  //预报天气Json

    private String pm25;    //pm25空气质量Json

    private Boolean isDIY;  //是否为自定义图片

    private String picAuto; //自动图片地址

    private String picDIY;  //自定义图片地址

    private String picDIYCache; //自定义图片地址缓存

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getTemp_curr() {
        return temp_curr;
    }

    public void setTemp_curr(String temp_curr) {
        this.temp_curr = temp_curr;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getFuture() {
        return future;
    }

    public void setFuture(String future) {
        this.future = future;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public Boolean getDIY() {
        return isDIY;
    }

    public void setDIY(Boolean DIY) {
        isDIY = DIY;
    }

    public String getPicAuto() {
        return picAuto;
    }

    public void setPicAuto(String picAuto) {
        this.picAuto = picAuto;
    }

    public String getPicDIY() {
        return picDIY;
    }

    public void setPicDIY(String picDIY) {
        this.picDIY = picDIY;
    }

    public String getPicDIYCache() {
        return picDIYCache;
    }

    public void setPicDIYCache(String picDIYCache) {
        this.picDIYCache = picDIYCache;
    }
}
