package com.mndream.easyweather;

import android.content.Context;
import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * Created by Administrator on 2017/11/15.
 *
 */

public class MyApplication extends LitePalApplication{
    private static Context sContext;

    @Override
    public void onCreate() {
        sContext = getApplicationContext();
        //数据库初始化
        LitePal.initialize(sContext);
//        FlowManager.init(this);
    }

    public static Context getContext(){
        return sContext;
    }
}
