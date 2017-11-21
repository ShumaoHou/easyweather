package com.mndream.easyweather;

import android.app.Application;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;

import org.litepal.LitePal;

/**
 * Created by Administrator on 2017/11/15.
 */

public class MyApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        sContext = getApplicationContext();
        FlowManager.init(this);
    }

    public static Context getContext(){
        return sContext;
    }
}
