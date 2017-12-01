package com.mndream.easyweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mndream.easyweather.db.City;
import com.mndream.easyweather.db.City_Table;
import com.mndream.easyweather.db.County;
import com.mndream.easyweather.db.County_Table;
import com.mndream.easyweather.db.Province;
import com.mndream.easyweather.util.Utility;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 * 遍历省市县三级的Fragment
 */

public class ChooseAreaFragment extends Fragment {
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        mTitleText =  view.findViewById(R.id.title_text);
        mBackButton =  view.findViewById(R.id.back_button);
        mListView =  view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                    String weatherId = mCountyList.get(i).getName();
                    if(getActivity() instanceof MainActivity){
                        //当前活动为MainActivity，即从未选择过城市
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if(getActivity() instanceof WeatherActivity) {
                        //当前活动为WeatherActivity，即已经选择过城市，侧边滑出选择
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        WeatherFragment fragment= (WeatherFragment) activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        fragment.drawerLayout.closeDrawers();
                        fragment.swipeRefreshLayout.setRefreshing(true);
                        fragment.requestWeather(weatherId);
                    }

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
            InputStream is = getActivity().getAssets().open("list_weather.txt");
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
        mTitleText.setText("中国");
        mBackButton.setVisibility(View.GONE);
//        mProvinceList = DataSupport.findAll(Province.class);    //从数据库查询省份数据
        mProvinceList = SQLite.select().from(Province.class).queryList();
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
//        mCityList = DataSupport
//                .where("province = ?",String.valueOf(mSelectedProvince.getName()))
//                .find(City.class);      //数据库查询对应省份下的城市
        mCityList= SQLite.select().from(City.class)
                .where(City_Table.province.eq(String.valueOf(mSelectedProvince.getName())))
                .queryList();
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
//        mCountyList = DataSupport
//                .where("city = ?",String.valueOf(mSelectedCity.getName()))
//                .find(County.class);
        mCountyList = SQLite.select().from(County.class)
                .where(County_Table.city.eq(String.valueOf(mSelectedCity.getName())))
                .queryList();
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
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeDialog();
                    Toast.makeText(getActivity(),"查找城市失败",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    /**
     * 显示进度对话框
     */
    private void showDialog() {
        if(mDialog == null){
            mDialog = new ProgressDialog(getActivity());
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
