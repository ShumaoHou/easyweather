package com.mndream.easyweather.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mndream.easyweather.R;
import com.mndream.easyweather.db.SelectedCounty;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link WeatherAppWidget WeatherAppWidget} AppWidget.
 */
public class WeatherAppWidgetConfigureActivity extends Activity {

    private List<String> mCountyNameList = new ArrayList<>();  //存放要显示已选择的城市名称列表
    private List<String> mWeatherIdList = new ArrayList<>();     //存放已选择城市天气代码
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<SelectedCounty> mSelectedCountyList;

    private static final String PREFS_NAME = "com.mndream.easyweather.appwidget.WeatherAppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;   //将要配置的widgetId

    public WeatherAppWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveWeatherIdPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadWeatherIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteWeatherIdPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.weather_app_widget_configure);

        //初始化数据源
        mSelectedCountyList = DataSupport.findAll(SelectedCounty.class);
        if (mSelectedCountyList.size() > 0){
            mCountyNameList.clear();
            mWeatherIdList.clear();
            for(SelectedCounty selectedCounty : mSelectedCountyList){
                mCountyNameList.add(selectedCounty.getCountyName());
                mWeatherIdList.add(selectedCounty.getWeatherId());
            }
        }
        //设置城市列表
        mListView =  findViewById(R.id.widget_conf_list_view);
        mAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, mCountyNameList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Context context = WeatherAppWidgetConfigureActivity.this;
                //获取点击城市天气id，并储存
                String weatherId = mWeatherIdList.get(i);
                saveWeatherIdPref(context,mAppWidgetId,weatherId);
                // 首次添加不启用OnUpdate，所以需要手动更新
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherAppWidget.class));
                for (int appWidgetId : appWidgetIds) {
                    WeatherAppWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
                }
                // 传递appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        //设置返回按钮
        findViewById(R.id.widget_conf_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }
}

