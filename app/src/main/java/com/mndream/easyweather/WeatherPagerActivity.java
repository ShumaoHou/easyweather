package com.mndream.easyweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mndream.easyweather.db.SelectedCounty;
import com.mndream.easyweather.service.AutoUpdateService;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/8.
 *
 */

public class WeatherPagerActivity extends FragmentActivity{
    private List<SelectedCounty> mSelectedCountys;  //选择的城市列表
//    private Map<Integer, WeatherFragment> mPageReferenceMap = new HashMap<>();

    private ViewPager mViewPager;
    private FragmentStatePagerAdapter adapter;
    private LinearLayout mWeatherDotLayout;
    private Button mPreSelectedBtn;
    private TextView mToolbarCity;
    private TextView mToolbarDate;

    private String mWeatherId;  //当前城市天气ID
    private SelectedCounty mCurrentCounty;  //当前城市

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_pager);

        mViewPager = findViewById(R.id.activity_weather_pager_view_pager);
        mWeatherDotLayout = findViewById(R.id.weather_dot_layout);
        mToolbarDate = findViewById(R.id.weather_toolbar_date_txt);

        //选择城市按钮
        Button toolbarCityBtn = findViewById(R.id.weather_toolbar_city_btn);
        toolbarCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherPagerActivity.this,ChooseAreaActivity.class);
                intent.putExtra("weather_add",true);//是否为添加城市
                startActivity(intent);
            }
        });
        mToolbarCity = findViewById(R.id.weather_toolbar_city_txt);
        mToolbarCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherPagerActivity.this,ChooseAreaActivity.class);
                intent.putExtra("weather_add",true);//是否为添加城市
                startActivity(intent);
            }
        });
        //设置弹出菜单
        Button toolbarMoreBtn = findViewById(R.id.weather_toolbar_more_btn);
        toolbarMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(WeatherPagerActivity.this,SettingsActivity.class);
                settingsIntent.putExtra("weather_id",mWeatherId);
                startActivity(settingsIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mSelectedCountys = DataSupport.findAll(SelectedCounty.class);
        FragmentManager fm = getSupportFragmentManager();
        adapter = new FragmentStatePagerAdapter(fm) {
            private int mChildCount = 0;
            private int mCurrentPosition = 0;
            @Override
            public Fragment getItem(int position) {
                String weatherId = mSelectedCountys.get(position).getWeatherId();
                WeatherFragment fragment = WeatherFragment.newInstance(weatherId);
//                mPageReferenceMap.put(position,fragment);
                return fragment;
            }

            @Override
            public int getCount() {
                return mSelectedCountys.size();
            }

            @Override
            public int getItemPosition(Object object) {
                if ( mChildCount > 0) {
                    // 这里利用判断执行若干次不缓存，刷新
                    mChildCount --;
                    // 返回这个是itemPOSITION_NONE
                    return POSITION_NONE;
                }
                // 这个则是POSITION_UNCHANGED
                return super.getItemPosition(object);
            }

            @Override
            public void notifyDataSetChanged() {
                // 重写这个方法，取到子Fragment的数量，用于下面的判断，以执行多少次刷新
                mSelectedCountys = DataSupport.findAll(SelectedCounty.class);
                mChildCount = getCount();
                super.notifyDataSetChanged();
                //设置城市、日期
                notifyCityDateChange(mCurrentPosition);
                initWeatherDot();
                Button currentBtn = (Button)mWeatherDotLayout.getChildAt(mCurrentPosition);
                currentBtn.setBackgroundResource(R.drawable.ic_dot_current);
                mPreSelectedBtn = currentBtn;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
//                mPageReferenceMap.remove(position);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                mCurrentPosition = position;  //将当前显示的Fragment位置缓存
                super.setPrimaryItem(container, position, object);
            }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                //设置城市、日期
                notifyCityDateChange(position);
                //设置当前指示点
                if(mPreSelectedBtn != null){
                    mPreSelectedBtn.setBackgroundResource(R.drawable.ic_dot_normal);
                }
                Button currentBtn = (Button)mWeatherDotLayout.getChildAt(position);
                currentBtn.setBackgroundResource(R.drawable.ic_dot_current);
                mPreSelectedBtn = currentBtn;

                SharedPreferences.Editor editor = getSharedPreferences("weather_current",0).edit();
                editor.putString("weather_id",mWeatherId);
                editor.apply();
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });
        //获取当前的WeatherID
        SharedPreferences prefs = getSharedPreferences("weather_current",0);
        mWeatherId = prefs.getString("weather_id",null);
        //初始化点数指示器
        initWeatherDot();

        //        启动自动更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 更新城市名、时间
     * @param position 当前Fragment的位置
     */
    private void notifyCityDateChange(int position){
        //将当前天气ID存储
        mWeatherId = mSelectedCountys.get(position).getWeatherId();
        //获取当前数据模型
        List<SelectedCounty> selectedCountyList = DataSupport
                .where("weatherId = ?", mWeatherId)
                .find(SelectedCounty.class);
        if(selectedCountyList.size()>0){
            mCurrentCounty = selectedCountyList.get(0);
        }
        if(mCurrentCounty!=null){
            mToolbarCity.setText(mCurrentCounty.getCountyName());
            mToolbarDate.setText(mCurrentCounty.getUpdateTime());
        }
    }

    /**
     * 设置下方页数指示点
     */
    public void initWeatherDot(){
        mWeatherDotLayout.removeAllViews();
        for (int i = 0; i < mSelectedCountys.size(); i++) {
            Button bt = new Button(this );
            bt.setLayoutParams( new ViewGroup.LayoutParams(20,20));
            bt.setBackgroundResource(R.drawable.ic_dot_normal );
            mWeatherDotLayout.addView(bt);
        }
        //设置当前页面
        String weatherId = mWeatherId;
        int currentPosition = 0;
        if(weatherId != null){
            for(int i = 0;i<mSelectedCountys.size();i++){
                if(weatherId.equals(mSelectedCountys.get(i).getWeatherId())){
                    mViewPager.setCurrentItem(i);
                    currentPosition = i;
                    Button currentBtn = (Button)mWeatherDotLayout.getChildAt(i);
                    currentBtn.setBackgroundResource(R.drawable.ic_dot_current);
                    mPreSelectedBtn = currentBtn;
                }
            }
        }
        notifyCityDateChange(currentPosition);
    }
    /**
     * 判断是否有网络连接
     * @param context 上下文
     * @return true 网络连接 false 网络不连接
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
