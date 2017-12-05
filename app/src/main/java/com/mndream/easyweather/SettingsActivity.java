package com.mndream.easyweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.util.List;

/**
*   @app    易天气
*   @author mndream
*   @date   2017/12/5
**/

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 0;//定义请求码常量

    private List<Uri> mSelected;

    private LinearLayout mSettingsIsDIY;
    private TextView mIsDIYDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();

    }

    private void init() {
        mIsDIYDetails = findViewById(R.id.settings_is_diy_details);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isDIY = prefs.getBoolean("bg_pic_is_diy",false);//是否自定义背景
        if(isDIY){
            mIsDIYDetails.setText(R.string.settings_is_diy_details_diy);
        }else{
            mIsDIYDetails.setText(R.string.settings_is_diy_details);
        }
        //返回按钮
        Button backBtn = findViewById(R.id.settings_toolbar_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //自定义背景图片
        mSettingsIsDIY = findViewById(R.id.settings_is_diy);
        mSettingsIsDIY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit();
            editor.putString("bg_pic_diy", mSelected.get(0).toString());
            editor.putBoolean("bg_pic_is_diy", true);
            editor.apply();
            finish();
        }
    }
}
