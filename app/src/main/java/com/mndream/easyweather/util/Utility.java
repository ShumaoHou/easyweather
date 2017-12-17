package com.mndream.easyweather.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.mndream.easyweather.db.City;
import com.mndream.easyweather.db.County;
import com.mndream.easyweather.db.Province;
import com.mndream.easyweather.gson.WeatherFuture;
import com.google.gson.Gson;
import com.mndream.easyweather.gson.WeatherPM25;
import com.mndream.easyweather.gson.WeatherToday;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 * 工具类
 */

public class Utility {
    //判断是否拥有权限
    public static boolean hasPermission(Context context, String permission){
        int perm = context.checkCallingOrSelfPermission(permission);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 将返回的JSON数据解析成WeatherFuture实体类
     */
    public static WeatherFuture handleWeatherFutureResponse(String response){
        try {
            return new Gson().fromJson(response,WeatherFuture.class);       //使用Gson返回对应的对象
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 将返回的JSON数据解析成WeatherToday实体类
     */
    public static WeatherToday handleWeatherTodayResponse(String response){
        try {
            return new Gson().fromJson(response,WeatherToday.class);       //使用Gson返回对应的对象
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 将返回的JSON数据解析成WeatherPM25实体类
     */
    public static WeatherPM25 handleWeatherPM25Response(String response){
        try {
            return new Gson().fromJson(response,WeatherPM25.class);       //使用Gson返回对应的对象
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 处理必应返回数据
     */
    public static String handleBgPic(String response){
        try {
            JSONArray imagesArray = new JSONObject(response).getJSONArray("images");
            JSONObject jsonObject = imagesArray.getJSONObject(0);
            String bgPic = jsonObject.getString("url");  //获取图片后缀地址
            return "http://cn.bing.com"+bgPic;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询jsonBody所有省份名，保存数据库
     */
    public static boolean handleProvince(String jsonBody){
        if(!TextUtils.isEmpty(jsonBody)){
            try {
                List<String> dataList = new ArrayList<>();  //缓存省份名
                JSONArray jsonArray = new JSONArray(jsonBody);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject provinceObject = jsonArray.getJSONObject(i);
                    String provinceName = provinceObject.getString("province"); //逐个获取相应省份名
                    if(!dataList.contains(provinceName)){   //未存储的省份就进行存储
                        dataList.add(provinceName);
                        Province province = new Province();
                        province.setName(provinceName);
                        province.save();
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 查询jsonBody中所有provinceName下的城市名，保存数据库
     */
    public static boolean handleCity(String jsonBody,String provinceName){
        if(!TextUtils.isEmpty(jsonBody)){
            try {
                List<String> dataList = new ArrayList<>();  //缓存城市名
                JSONArray jsonArray = new JSONArray(jsonBody);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject cityObject = jsonArray.getJSONObject(i);
                    String cityName = cityObject.getString("city"); //逐个获取相应城市名
                    String province = cityObject.getString("province");
                    if(provinceName.equals(province) && !dataList.contains(cityName)){   //未存储的城市就进行存储
                        dataList.add(cityName);
                        City city = new City();
                        city.setName(cityName);
                        city.setProvince(provinceName);
                        city.save();
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 查询jsonBody中所有cityName下的县名，保存数据库
     */
    public static boolean handleCounty(String jsonBody,String cityName){
        if(!TextUtils.isEmpty(jsonBody)){
            try {
                JSONArray jsonArray = new JSONArray(jsonBody);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject countyObject = jsonArray.getJSONObject(i);
                    String countyName = countyObject.getString("county"); //逐个获取相应县名
                    String city = countyObject.getString("city");
                    String weaid = countyObject.getString("weaid");
                    if(cityName.equals(city)){
                        County county = new County();
                        county.setName(countyName);
                        county.setCity(cityName);
                        county.setWeaid(weaid);
                        county.save();
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
