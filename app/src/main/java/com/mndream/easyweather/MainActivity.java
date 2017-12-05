package com.mndream.easyweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return new ChooseAreaFragment();
    }


}
