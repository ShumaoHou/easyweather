package com.example.easyweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by Administrator on 2017/11/15.
 */

public class MyApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        sContext = getApplicationContext();
        LitePal.initialize(sContext);
    }

    public static Context getContext(){
        return sContext;
    }
}
