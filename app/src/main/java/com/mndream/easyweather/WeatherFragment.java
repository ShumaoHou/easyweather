package com.mndream.easyweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.mndream.easyweather.db.SelectedCounty;
import com.mndream.easyweather.gson.Forecast;
import com.mndream.easyweather.gson.Weather;
import com.mndream.easyweather.gson.WeatherFuture;
import com.mndream.easyweather.gson.WeatherPM25;
import com.mndream.easyweather.gson.WeatherToday;
import com.mndream.easyweather.service.AutoUpdateService;
import com.mndream.easyweather.util.HttpUtil;
import com.mndream.easyweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
*   @app    易天气
*   @author mndream
*   @date   2017/12/5
**/

public class WeatherFragment extends Fragment{

    private static final String ARG_WEATHER_ID = "weather_id";
    private final String SIGN = "59e4d7bda830fa4c19bd86b58f3856ba";
    private final String APP_KEY = "30131";
    private FragmentActivity mActivity;

    private String mWeatherId;
    private Weather weather = new Weather();
    private SelectedCounty mCurrentCounty;//当前选择的城市信息

    public SwipeRefreshLayout swipeRefreshLayout;
    private ImageView weatherBgPic,weatherInfoImg;
    private LinearLayout weatherLayout;
    private TextView nowDegreeText,nowConditionText,
            now1txt,now2txt,now3txt,now4txt,now5txt,now6txt;
    private LinearLayout nowAqi,forecastLayout;
    private LineChart mChart;
    private Boolean isDIY = false;
    private String bgPic = null;
    private String diy = null;
    private String diy_cache = null;

    private ArrayList<Entry> mMaxDegreeList;    //预测最大温度列表
    private ArrayList<Entry> mMinDegreeList;    //预测最低温度列表


    public static WeatherFragment newInstance(String weatherId){
        Bundle args = new Bundle();
        args.putString(ARG_WEATHER_ID,weatherId);
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
            getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        //获取当前城市天气ID
        mWeatherId = getArguments().getString(ARG_WEATHER_ID);
        //获取当前数据模型
        List<SelectedCounty> selectedCountyList = DataSupport
                .where("weatherId = ?", mWeatherId)
                .find(SelectedCounty.class);
        if(selectedCountyList.size()>0){
            mCurrentCounty = selectedCountyList.get(0);
        }
        mActivity = getActivity();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_weather,container,false);
        //初始化控件
        weatherBgPic = v.findViewById(R.id.weather_bg_pic);
        weatherLayout = v.findViewById(R.id.weather_layout);
        nowDegreeText = v.findViewById(R.id.degree_text);
        nowConditionText = v.findViewById(R.id.weather_info_text);
        weatherInfoImg = v.findViewById(R.id.weather_info_img);
        now1txt = v.findViewById(R.id.now_1_txt);
        now2txt = v.findViewById(R.id.now_2_txt);
        now3txt = v.findViewById(R.id.now_3_txt);
        now4txt = v.findViewById(R.id.now_4_txt);
        now5txt = v.findViewById(R.id.now_5_txt);
        now6txt = v.findViewById(R.id.now_6_txt);
        nowAqi = v.findViewById(R.id.now_aqi);
        forecastLayout = v.findViewById(R.id.forecast_layout);
        mChart = v.findViewById(R.id.forecast_line_chart);
        //刷新列表
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        //长按删除
        weatherLayout.setLongClickable(true);
        weatherLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if( DataSupport.findAll(SelectedCounty.class).size()>1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("删除当前城市");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(mCurrentCounty.isSaved()){
                                mCurrentCounty.delete();
                            }
                            ViewPager viewPager = getActivity().findViewById(R.id.activity_weather_pager_view_pager);
                            FragmentStatePagerAdapter adapter = (FragmentStatePagerAdapter) viewPager.getAdapter();
                            adapter.notifyDataSetChanged(); //通知更新界面
                        }
                    }).create().show();
                    return true;
                }else{
                    return false;
                }
            }
        });
        //初始化数据
        String weatherT = null;
        String weatherF = null;
        String weatherP = null;
        if(mCurrentCounty != null) {
            weatherT = mCurrentCounty.getToday();
            weatherF = mCurrentCounty.getFuture();
            weatherP = mCurrentCounty.getPm25();
        }
        if(weatherT != null && weatherF != null && weatherP != null){
            //有缓存则直接解析天气数据
            WeatherFuture weatherFuture = Utility.handleWeatherFutureResponse(weatherF);
            WeatherToday weatherToday = Utility.handleWeatherTodayResponse(weatherT);
            WeatherPM25 weatherPM25 = Utility.handleWeatherPM25Response(weatherP);
            weather.today = weatherToday;
            weather.pm25 = weatherPM25;
            weather.future = weatherFuture;
            showWeatherInfo(weather);
        }else{
            //无缓存则去服务器查询天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        //界面初始化时设置天气背景图
        if(mCurrentCounty != null){
            isDIY = mCurrentCounty.getDIY();
            bgPic = mCurrentCounty.getPicAuto();
            diy = mCurrentCounty.getPicDIY();
            diy_cache = mCurrentCounty.getPicDIYCache();
        }
        if(isDIY && diy!=null){
            Glide.with(getActivity()).load(Uri.parse(diy)).into(weatherBgPic);
        }else{
            if(bgPic != null){
                Glide.with(this).load(bgPic).into(weatherBgPic);
            }else{
                loadBgPic();
            }
        }
        return v;
    }
    /**
     * 加载天气背景图片
     * 刷新自定义图片 或 加载每日图片
     */
    private void loadBgPic() {
        if(isDIY && !TextUtils.isEmpty(diy)){
            if(!TextUtils.equals(diy,diy_cache)){//现有图片与缓存不等则刷新
                final Uri bgPicUri = Uri.parse(diy);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getActivity()).load(bgPicUri).into(weatherBgPic);
                    }
                });
                SelectedCounty updateCounty = new SelectedCounty();
                updateCounty.setPicDIYCache(diy);
                updateCounty.updateAll("weatherId = ?", mWeatherId);
            }
        }else{
            if(WeatherPagerActivity.isNetworkConnected(getActivity())){
                String requestBgPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
                HttpUtil.sendOkHttpRequest(requestBgPic, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String bgPic = Utility.handleBgPic(response.body().string());
                        SelectedCounty updateCounty = new SelectedCounty();
                        updateCounty.setPicAuto(bgPic);
                        updateCounty.updateAll("weatherId = ?", mWeatherId);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(mActivity).load(bgPic).into(weatherBgPic);
                            }
                        });
                    }
                });
            }else{
                Toast.makeText(mActivity,
                        "请连接网络后重试",Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId 天气id
     */
    public void requestWeather(final String weatherId) {
        if(WeatherPagerActivity.isNetworkConnected(MyApplication.getContext())){
            //请求实时天气
            String weatherUrl = "http://api.k780.com/?app=weather.today&weaid=" + weatherId +
                    "&appkey=" + APP_KEY +
                    "&sign=" + SIGN +
                    "&format=json";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                String errorText = "获取实时天气信息失败";
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(MyApplication.getContext(),
                            errorText,Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final WeatherToday weatherToday = Utility.handleWeatherTodayResponse(responseText);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(weatherToday != null && "1".equals(weatherToday.success)){
                                mWeatherId = weatherId; //更新id
                                //将天气数据缓存
                                weather.today = weatherToday;
                                //删除之前数据
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit();
                                editor.clear();
                                editor.apply();
                                //更新数据库
                                SelectedCounty updateCounty = new SelectedCounty();
                                updateCounty.setToday(responseText);
                                updateCounty.updateAll("weatherId = ?", mWeatherId);
                                requestFuture(weatherId);
                            }else{
                                Toast.makeText(getActivity(),
                                        errorText,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
            loadBgPic();    //更新天气背景图
        }else{
            Toast.makeText(getActivity(),
                    "请连接网络后重试",Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }

    }
    //请求天气预报
    public void requestFuture(final String weatherId){

        String weatherUrl = "http://api.k780.com/?app=weather.future&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取天气预报信息失败";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherFuture weatherFuture = Utility.handleWeatherFutureResponse(responseText);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherFuture != null && "1".equals(weatherFuture.success)){
                            //将天气数据缓存
                            weather.future = weatherFuture;
                            //更新数据库
                            SelectedCounty updateCounty = new SelectedCounty();
                            updateCounty.setFuture(responseText);
                            updateCounty.updateAll("weatherId = ?", mWeatherId);
                            requestPM25(weatherId);
                        }else{
                            Toast.makeText(getActivity(),
                                    errorText,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //请求PM25数据
    public void requestPM25(final String weatherId){

        String weatherUrl = "http://api.k780.com/?app=weather.pm25&weaid=" + weatherId +
                "&appkey=" + APP_KEY +
                "&sign=" + SIGN +
                "&format=json";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            String errorText = "获取PM25信息失败";
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyApplication.getContext(),
                        errorText,Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final WeatherPM25 weatherPM25 = Utility.handleWeatherPM25Response(responseText);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weatherPM25 != null && "1".equals(weatherPM25.success)){
                            //将天气数据缓存
                            weather.pm25 = weatherPM25;
                            //更新数据库
                            SelectedCounty updateCounty = new SelectedCounty();
                            updateCounty.setPm25(responseText);
                            updateCounty.updateAll("weatherId = ?", mWeatherId);
                            //展示天气数据
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(getActivity(),
                                    errorText,Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }
    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {

        ArrayList<String> dateList = new ArrayList<>();  //预测日期列表
        mMaxDegreeList = new ArrayList<>();
        mMinDegreeList = new ArrayList<>();

        String countyName = weather.today.result.citynm;   //城市名
        String dateTime = weather.today.result.date.split("-")[1]
                + "."
                + weather.today.result.date.split("-")[2]
                + "\n" + weather.today.result.week;  //时间
        SelectedCounty updateCounty = new SelectedCounty();
        updateCounty.setCountyName(countyName);
        updateCounty.setUpdateTime(dateTime);
        updateCounty.updateAll("weatherId = ?", mWeatherId);

        String degree =  weather.today.result.temp_curr + "℃";  //温度
        nowDegreeText.setText(degree);

        String conditionInfo = weather.today.result.condition;     //天气情况
        nowConditionText.setText(conditionInfo);

        String hum = weather.today.result.humidity ;        //实时湿度
        now1txt.setText(hum);

        String nowMinMaxText =weather.today.result.temp_low + "°～" + weather.today.result.temp_high + "°";   //温度范围
        now2txt.setText(nowMinMaxText);

        String wind = weather.today.result.wind;   //风向
        now3txt.setText(wind);

        String winp =  weather.today.result.winp;     //风力
        now4txt.setText(winp);

        String aqi = "空气质量："  + weather.pm25.result.aqi + "      " + weather.pm25.result.aqi_levid +"级      " + weather.pm25.result.aqi_levnm;         //aqi
        now5txt.setText(aqi);

        String remark = "运动建议：" + weather.pm25.result.aqi_remark;
        now6txt.setText(remark);

        if("-".equals(weather.pm25.result.aqi_levnm)){
            nowAqi.setVisibility(View.GONE);
        }else{
            nowAqi.setVisibility(View.VISIBLE);
        }
        //设置预报部分
        forecastLayout.removeAllViews();
        List<Forecast> forecastList = weather.future.forecastList;
        if(forecastList.size() == 7){
            for(int i = 0; i<forecastList.size();i++){
                //每个预报的天气项
                Forecast forecast = forecastList.get(i);
                View view = LayoutInflater.from(getActivity())
                        .inflate(R.layout.forecast_item, forecastLayout, false);
                //设置日期
                TextView dateText = view.findViewById(R.id.forecast_item_date_text);
                String dateString = forecast.date.split("-")[1] + "." + forecast.date.split("-")[2] ;
                //显示星期
                if(i ==0 ){
                    dateText.setText("今天");
                }else{
                    dateText.setText(forecast.week);
                }
                //存储日期信息
                dateList.add(dateString);
                //最高温度
                String maxString = forecast.temp_high;
                mMaxDegreeList.add(new Entry(i,Float.parseFloat(maxString)));
                //最低温度
                String minString = forecast.temp_low;
                mMinDegreeList.add(new Entry(i,Float.parseFloat(minString)));
                //天气情况
                TextView condText = view.findViewById(R.id.forecast_item_cond_text);
                condText.setText(forecast.condition);
                //天气图标
                ImageView icon = view.findViewById(R.id.forecast_item_icon);
                String iconFileName = forecast.weather_icon.split("/")[6];
                String iconNum = iconFileName.substring(0,iconFileName.length()-4);
                ApplicationInfo appInfo = getActivity().getApplicationInfo();
                int resID = getResources().getIdentifier("w"+iconNum, "drawable", appInfo.packageName);
                if(i==0){
                    Glide.with(getActivity()).load(resID).into(weatherInfoImg);
                }
                Glide.with(getActivity()).load(resID).into(icon);
                //设置子控件属性
                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                forecastLayout.addView(view);
            }
        }
        initForecastLineChart(dateList);

        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void initForecastLineChart( final ArrayList<String> dateList){
        //设置chart属性
        //描述不可用
        mChart.getDescription().setEnabled(false);
        //设置动画
        mChart.animateY(1000);
        mChart.setNoDataText("暂时没有天气数据");
        mChart.setTouchEnabled(false);
        //设置图例
        Legend legend = mChart.getLegend();
        legend.setEnabled(false);
        //设置Y轴
        YAxis yAxisL = mChart.getAxisLeft();
        YAxis yAxisR = mChart.getAxisRight();
        yAxisL.setEnabled(false);
        yAxisR.setEnabled(false);
        yAxisL.setMinWidth(10f);
        //设置X轴
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int i = (int)value;
                return dateList.get(i);
            }
        });
        //设置间隔线
        LimitLine xLimitLine1 = new LimitLine(0.5f,"");
        LimitLine xLimitLine2 = new LimitLine(1.5f,"");
        LimitLine xLimitLine3 = new LimitLine(2.5f,"");
        LimitLine xLimitLine4 = new LimitLine(3.5f,"");
        LimitLine xLimitLine5 = new LimitLine(4.5f,"");
        LimitLine xLimitLine6 = new LimitLine(5.5f,"");
        xLimitLine1.setLineColor(ColorUtils.setAlphaComponent(Color.GRAY,192));
        xLimitLine2.setLineColor(ColorUtils.setAlphaComponent(Color.GRAY,192));
        xLimitLine3.setLineColor(ColorUtils.setAlphaComponent(Color.GRAY,192));
        xLimitLine4.setLineColor(ColorUtils.setAlphaComponent(Color.GRAY,192));
        xLimitLine5.setLineColor(ColorUtils.setAlphaComponent(Color.GRAY,192));
        xLimitLine6.setLineColor(ColorUtils.setAlphaComponent(Color.GRAY,192));
        xAxis.addLimitLine(xLimitLine1);
        xAxis.addLimitLine(xLimitLine2);
        xAxis.addLimitLine(xLimitLine3);
        xAxis.addLimitLine(xLimitLine4);
        xAxis.addLimitLine(xLimitLine5);
        xAxis.addLimitLine(xLimitLine6);
        LineDataSet lineDataSetMax;
        LineDataSet lineDataSetMin;
        if(mChart.getData() != null
                && mChart.getData().getDataSetCount() > 0) {//判断图表原来是否有数据
            //获取数据集
            lineDataSetMax = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            lineDataSetMin = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            //设置数据
            lineDataSetMax.setValues(mMaxDegreeList);
            lineDataSetMin.setValues(mMinDegreeList);
            //刷新数据
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        }else{
            //设置数据集-最高温线
            lineDataSetMax= new LineDataSet(mMaxDegreeList, "最高温");
            lineDataSetMax.setColors(ColorUtils.setAlphaComponent(Color.rgb(255,102,0),192));//折线颜色
            lineDataSetMax.setLineWidth(2f);//折线宽度
            lineDataSetMax.setAxisDependency(YAxis.AxisDependency.LEFT);//对准基线
            lineDataSetMax.setValueTextColor(Color.WHITE);//标值字体颜色
            lineDataSetMax.setValueTextSize(12f);//标值字体大小
            lineDataSetMax.setCircleColor(Color.WHITE);//数据圆点颜色
            lineDataSetMax.setCircleRadius(2f);//数据圆点半径
            lineDataSetMax.setValueFormatter(new IValueFormatter() {    //设置折线上数值显示模式
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    int i = (int)value;
                    return i + "°";
                }
            });
            //设置数据集-最低温线
            lineDataSetMin = new LineDataSet(mMinDegreeList, "最低温");
            lineDataSetMin.setColors(ColorUtils.setAlphaComponent(Color.rgb(0,204,255),192));//折线颜色
            lineDataSetMin.setLineWidth(2f);//折线宽度
            lineDataSetMin.setAxisDependency(YAxis.AxisDependency.LEFT);//对准基线
            lineDataSetMin.setValueTextColor(Color.WHITE);//标值字体颜色
            lineDataSetMin.setValueTextSize(12f);//标值字体大小
            lineDataSetMin.setCircleColor(Color.WHITE);//数据圆点颜色
            lineDataSetMin.setCircleRadius(2f);//数据圆点半径
            lineDataSetMin.setValueFormatter(new IValueFormatter() {   //设置折线上数值显示模式
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    int i = (int)value;
                    return i + "°";
                }
            });
            //线的集合（可单条或多条线）
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSetMax);
            dataSets.add(lineDataSetMin);
            //把要画的所有线(线的集合)添加到LineData里
            LineData lineData = new LineData(dataSets);
            //设置显示数值
            lineData.setDrawValues(true);
            //添加数据到图表中
            mChart.setData(lineData);
        }
        mChart.invalidate();
    }

}
