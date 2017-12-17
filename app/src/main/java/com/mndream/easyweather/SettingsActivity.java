package com.mndream.easyweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kyleduo.switchbutton.SwitchButton;
import com.mndream.easyweather.db.SelectedCounty;
import com.mndream.easyweather.util.Utility;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
*   @app    易天气
*   @author mndream
*   @date   2017/12/5
**/

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 0;//定义请求码常量

    private List<Uri> mSelected;

    private LinearLayout mSettingsContainer;
    private LinearLayout mSettingsIsDIYSelect;
    private ImageView mSettingsIsDIYSelectImg;
    private TextView mIsDIYDetails;
    private SwitchButton mIsDIYBtn;

    private Boolean isDIY = false;
    private String bgPic = null;
    private String diy = null;
    private String diy_cache = null;
    private String mWeatherId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettingsContainer = findViewById(R.id.settings_container);
        mIsDIYDetails = findViewById(R.id.settings_is_diy_details);
        mSettingsIsDIYSelectImg = findViewById(R.id.settings_is_diy_select_img);

        mWeatherId = getIntent().getStringExtra("weather_id");
        SelectedCounty mCurrentCounty = null;
        List<SelectedCounty> selectedCountyList = DataSupport
                .where("weatherId = ?", mWeatherId)
                .find(SelectedCounty.class);
        if(selectedCountyList.size()>0){
            mCurrentCounty = selectedCountyList.get(0);
        }
        if(mCurrentCounty != null){
            isDIY = mCurrentCounty.getDIY();
            bgPic = mCurrentCounty.getPicAuto();
            diy = mCurrentCounty.getPicDIY();
            diy_cache = mCurrentCounty.getPicDIYCache();
        }//是否自定义背景

        //返回按钮
        Button backBtn = findViewById(R.id.settings_toolbar_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //自定义背景图功能开关
        mIsDIYBtn = findViewById(R.id.settings_is_diy_btn);
        mIsDIYBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mIsDIYDetails.setText(R.string.settings_is_diy_details_on);
                    SelectedCounty updateCounty = new SelectedCounty();
                    updateCounty.setDIY(true);
                    updateCounty.setPicDIYCache(null);
                    updateCounty.updateAll("weatherId = ?", mWeatherId);
                    if(!TextUtils.isEmpty(diy)){
                        final Uri bgPicUri = Uri.parse(diy);
                        Glide.with(SettingsActivity.this).load(bgPicUri).into(mSettingsIsDIYSelectImg);
                    }
                    if(mSettingsIsDIYSelect.getParent() == null) {
                        mSettingsContainer.addView(mSettingsIsDIYSelect, 1);
                    }
                }else{
                    mIsDIYDetails.setText(R.string.settings_is_diy_details_off);
                    SelectedCounty updateCounty = new SelectedCounty();
                    updateCounty.setDIY(false);
                    updateCounty.setPicDIYCache(null);
                    updateCounty.updateAll("weatherId = ?", mWeatherId);
                    mSettingsContainer.removeView(mSettingsIsDIYSelect);
                }
            }
        });
        final LinearLayout mSettingsIsDIY = findViewById(R.id.settings_is_diy);
        mSettingsIsDIY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsDIYBtn.setChecked(!mIsDIYBtn.isChecked());
            }
        });
        //选择自定义背景图片
        mSettingsIsDIYSelect = findViewById(R.id.settings_is_diy_select);
        mSettingsIsDIYSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String permissionName = "android.permission.WRITE_EXTERNAL_STORAGE";
                if(Utility.hasPermission(SettingsActivity.this,permissionName)){
                    Matisse
                            .from(SettingsActivity.this)
                            .choose(MimeType.allOf())//照片视频全部显示
                            .countable(true)//有序选择图片
                            .maxSelectable(1)//最大选择数量为9
                            .gridExpectedSize(360)//图片显示表格的大小getResources()
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//图像选择和预览活动所需的方向。
                            .thumbnailScale(0.85f)//缩放比例
                            .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                            .imageEngine(new GlideEngine())//加载方式
                            .forResult(REQUEST_CODE_CHOOSE);//请求码
                    SettingsActivity.this.overridePendingTransition(R.anim.in_from_top,0);
                }else{
                    Snackbar.make(mSettingsIsDIYSelect,"请开启本应用存储权限",Snackbar.LENGTH_LONG)
                            .setAction("点击前往设置", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent =  new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            }
        });
        /*
        自动更新天气周期设置
         */
        final TextView settingsAutoUpdateDetails = findViewById(R.id.settings_auto_update_details);
        LinearLayout settingsAutoUpdate = findViewById(R.id.settings_auto_update);
        final String[] rates = {"1小时","2小时","4小时","8小时"};
        settingsAutoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("自动更新天气周期");
                builder.setItems(rates, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int rate = 2;
                        switch (which){
                            case 0:
                                rate = 1;
                                break;
                            case 1:
                                rate = 2;
                                break;
                            case 2:
                                rate = 4;
                                break;
                            case 3:
                                rate = 8;
                                break;
                        }
                        SharedPreferences.Editor editor = getSharedPreferences("weather_auto_update",0).edit();
                        editor.putInt("weather_auto_update_rate",rate);
                        editor.apply();
                        String detailText = "当前周期为：" + rate + "小时";
                        settingsAutoUpdateDetails.setText(detailText);
                    }
                });
                builder.show();
            }
        });
        SharedPreferences prefs = getSharedPreferences("weather_auto_update",0);
        int rate = prefs.getInt("weather_auto_update_rate",2);
        String detailText = "当前周期为：" + rate + "小时";
        settingsAutoUpdateDetails.setText(detailText);

        //关于按钮
        LinearLayout settingsAbout = findViewById(R.id.settings_about);
        settingsAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,AboutActivity.class);
                startActivity(intent);
            }
        });
        //初始化界面
        if(isDIY){
            mIsDIYBtn.setChecked(true);
            mIsDIYDetails.setText(R.string.settings_is_diy_details_on);
            if(isDIY && !TextUtils.isEmpty(diy)) {
                final Uri bgPicUri = Uri.parse(diy);
                Glide.with(SettingsActivity.this).load(bgPicUri).into(mSettingsIsDIYSelectImg);
            }
            if(mSettingsIsDIYSelect.getParent() == null){
                mSettingsContainer.addView(mSettingsIsDIYSelect, 1);
            }
        }else{
            mIsDIYBtn.setChecked(false);
            mIsDIYDetails.setText(R.string.settings_is_diy_details_off);
            mSettingsContainer.removeView(mSettingsIsDIYSelect);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);
            SelectedCounty updateCounty = new SelectedCounty();
            updateCounty.setDIY(true);
            updateCounty.setPicDIY(mSelected.get(0).toString());
            updateCounty.updateAll("weatherId = ?", mWeatherId);
            finish();
            this.overridePendingTransition(0,R.anim.out_to_top);
        }
    }
}
