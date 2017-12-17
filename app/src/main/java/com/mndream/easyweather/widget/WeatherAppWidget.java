package com.mndream.easyweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mndream.easyweather.MyApplication;
import com.mndream.easyweather.R;
import com.mndream.easyweather.WeatherPagerActivity;
import com.mndream.easyweather.db.SelectedCounty;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherAppWidgetConfigureActivity WeatherAppWidgetConfigureActivity}
 */
public class WeatherAppWidget extends AppWidgetProvider {
    //更新widget的广播对应的action
    private final String ACTION_UPDATE_WIDGET = "android.appwidget.action.APPWIDGET_UPDATE";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        //获取对应天气id
        String weatherId = WeatherAppWidgetConfigureActivity.loadWeatherIdPref(context,appWidgetId);
        //获取城市对象
        List<SelectedCounty> selectedCountyList = DataSupport
                .where("weatherId = ?", weatherId)
                .find(SelectedCounty.class);
        //获取对应数据
        String countyName = "--";
        String temp = "--℃";
        int iconResId = 0;
        if(selectedCountyList.size() > 0){
            countyName = selectedCountyList.get(0).getCountyName();
            temp = selectedCountyList.get(0).getTemp_curr();
            iconResId = selectedCountyList.get(0).getIconResId();
        }
        // Construct the RemoteViews object
        //更新视图对象
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_app_widget);
        views.setTextViewText(R.id.widget_county_txt,countyName);
        views.setTextViewText(R.id.widget_temp_txt,temp + "℃");
        views.setImageViewResource(R.id.widget_cond_img,iconResId);
        //设置点击打开应用
        Intent intent = new Intent(context,WeatherPagerActivity.class);
        intent.putExtra("WeatherAppWidget",weatherId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    /**
     *  当小部件被添加时或者每次小部件更新时都会调用一次该方法，
     *  配置文件中配置小部件的更新周期 updatePeriodMillis，每次更新都会调用。
     *  对应广播 Action 为：ACTION_APPWIDGET_UPDATE 和 ACTION_APPWIDGET_RESTORED 。
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * 每删除一个小部件就调用一次。
     * 对应的广播的 Action 为： ACTION_APPWIDGET_DELETED 。
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WeatherAppWidgetConfigureActivity.deleteWeatherIdPref(context, appWidgetId);
        }
    }

    /**
     * 当小部件第一次被添加到桌面时回调该方法，可添加多次，但只在第一次调用。
     * 对应广播的 Action 为 ACTION_APPWIDGET_ENABLE。
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 当最后一个该类型的小部件从桌面移除时调用，对应的广播的 Action 为 ACTION_APPWIDGET_DISABLED。
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_UPDATE_WIDGET.equals(action)) {
            updateWidgetView(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    private void updateWidgetView(Context context) {
        AppWidgetManager am = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = am.getAppWidgetIds(new ComponentName(context, WeatherAppWidget.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, am, appWidgetId);
        }
    }

}

