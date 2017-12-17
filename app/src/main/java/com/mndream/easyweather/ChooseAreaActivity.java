package com.mndream.easyweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.mndream.easyweather.db.City;
import com.mndream.easyweather.db.County;
import com.mndream.easyweather.db.Province;
import com.mndream.easyweather.db.SelectedCounty;
import com.mndream.easyweather.util.Utility;
import org.litepal.crud.DataSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 * 遍历省市县三级的Fragment
 */

public class ChooseAreaActivity extends AppCompatActivity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY= 1;
    public static final int LEVEL_COUNTY= 2;
    public static String jsonBody = "";

    private TextView mTitleText;
    private Button mBackButton;
    private ListView mListView;
    private ProgressDialog mDialog;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();  //存放要显示数据的列表
    private List<Province> mProvinceList;   //省列表
    private List<City> mCityList;           //市列表
    private List<County> mCountyList;       //县列表
    private Province mSelectedProvince; //选中的省份
    private City mSelectedCity;         //选中的城市
    private int mCurrentLevel;          //当前选中的级别

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);

        List<SelectedCounty> selectedCountyList = DataSupport.findAll(SelectedCounty.class);
        String weatherT;
        String weatherF;
        String weatherP;
        final Boolean weatherAdd = getIntent().getBooleanExtra("weather_add",false);//是否为添加城市
        if(selectedCountyList.size()>0 && !weatherAdd){
            weatherT = selectedCountyList.get(0).getToday();
            weatherF = selectedCountyList.get(0).getFuture();
            weatherP = selectedCountyList.get(0).getPm25();
            if(weatherT != null && weatherF != null && weatherP != null){
                Intent intent = new Intent(this,WeatherPagerActivity.class);
                startActivity(intent);
                finish();   //结束当前活动
            }
        }

        mTitleText =  findViewById(R.id.title_text);
        mBackButton =  findViewById(R.id.back_button);
        mListView =  findViewById(R.id.choose_area_list_view);
        mAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mCurrentLevel == LEVEL_PROVINCE){
                    mSelectedProvince = mProvinceList.get(i);
                    queryCities();
                }else if(mCurrentLevel == LEVEL_CITY){
                    mSelectedCity = mCityList.get(i);
                    queryCounties();
                }else if(mCurrentLevel == LEVEL_COUNTY){
                    //存储所选择的城市天气id
                    String weatherId = mCountyList.get(i).getWeaid();
                    List<SelectedCounty> selectedCountyList = DataSupport
                            .where("weatherId = ?", weatherId)
                            .find(SelectedCounty.class);
                    if(selectedCountyList.size()<=0) {
                        SelectedCounty selectedCounty = new SelectedCounty();
                        selectedCounty.setWeatherId(weatherId);
                        selectedCounty.setCountyName(mCountyList.get(i).getName());
                        selectedCounty.save();
                    }
                    //保存所选当前天气ID，结束当前活动
                    SharedPreferences.Editor editor = getSharedPreferences("weather_current",0).edit();
                    editor.putString("weather_id",weatherId);
                    editor.apply();
                    if(!weatherAdd){    //如果不是从天气界面启动的，就是首次启动
                        Intent intent = new Intent(ChooseAreaActivity.this,WeatherPagerActivity.class);
                        startActivity(intent);
                    }
                    finish();

                }
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(mCurrentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        try {
            InputStream is = getAssets().open("list_weather2.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonBody = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        queryProvinces();
    }

    /**
     * 查询全国所有省份，优先从数据库中查询，如果没有查询到，再从服务器上查询
     */
    private void queryProvinces() {
        mTitleText.setText("选择地区");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);    //从数据库查询省份数据
        if(mProvinceList.size() > 0){   //首先查询本地数据库
            dataList.clear();       //列表数据源清空
            for(Province province: mProvinceList){
                dataList.add(province.getName());   //为列表数据源添加省份名
            }
            mAdapter.notifyDataSetChanged();    //通知列表显示项变化
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        }else{  //本地数据库无数据，则从服务器上查询
            queryFromSD("province");
        }
    }
    /**
     * 查询选中省份的所有城市，优先从数据库中查询，如果没有查询到，再从服务器上查询
     */
    private void queryCities() {
        mTitleText.setText(mSelectedProvince.getName());
        mBackButton.setVisibility(View.VISIBLE);        //显示返回按钮
        mCityList= DataSupport
                .where("province = ?",String.valueOf(mSelectedProvince.getName()))
                .find(City.class);      //数据库查询对应省份下的城市
        if(mCityList.size()>0){
            dataList.clear();
            for(City city : mCityList){
                dataList.add(city.getName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        }else{
            queryFromSD("city");
        }
    }

    /**
     * 查询选中城市的所有县，优先从数据库中查询，如果没有查询到，再从服务器上查询
     */
    private void queryCounties() {
        mTitleText.setText(mSelectedCity.getName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport
                .where("city = ?",String.valueOf(mSelectedCity.getName()))
                .find(County.class);
        if(mCountyList.size() > 0){
            dataList.clear();
            for(County county : mCountyList){
                dataList.add(county.getName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_COUNTY;
        }else{
            queryFromSD("county");
        }
    }


    private void queryFromSD(final String type) {
        showDialog();

        boolean result = false;
        if("province".equals(type)){
            result = Utility.handleProvince(jsonBody);
        }else if("city".equals(type)){
            result = Utility.handleCity(jsonBody, mSelectedProvince.getName());
        }else if("county".equals(type)){
            result = Utility.handleCounty(jsonBody, mSelectedCity.getName());
        }
        if(result){
            //成功解析，则从本地数据库读取数据
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeDialog();
                    if("province".equals(type)){
                        queryProvinces();
                    }else if("city".equals(type)){
                        queryCities();
                    }else if("county".equals(type)){
                        queryCounties();
                    }
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeDialog();
                    Toast.makeText(ChooseAreaActivity.this,"查找城市失败",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    /**
     * 显示进度对话框
     */
    private void showDialog() {
        if(mDialog == null){
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("正在加载...");
            mDialog.setCanceledOnTouchOutside(false);   //点击空白处不能取消
        }
        mDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeDialog() {
        if(mDialog != null){
            mDialog.dismiss();
        }
    }
}
