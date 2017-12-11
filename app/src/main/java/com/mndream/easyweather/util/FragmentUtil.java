package com.mndream.easyweather.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mndream.easyweather.R;

/**
*   @app    易天气
*   @author mndream
*   @date   2017/12/8
 *   Fragment工具类
**/

public class FragmentUtil {
    /**
     * 显示Fragment，并将事务加入返回栈
     * @param containerActivity  托管Activity
     * @param fragment  要显示的Fragment
     */
    public static void replaceFragment(FragmentActivity containerActivity, Fragment fragment){
        FragmentManager fragmentManager = containerActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
