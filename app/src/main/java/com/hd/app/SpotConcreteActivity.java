package com.hd.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.panxw.android.imageindicator.AutoPlayManager;
import com.panxw.android.imageindicator.ImageIndicatorView;

import java.util.ArrayList;
import java.util.List;

import module.SpotInformation;

public class SpotConcreteActivity extends AppCompatActivity {

    public static final String SPOT_ID ="id";
    public static List<SpotInformation> spotInformationList = new ArrayList<>();
    private String spotid;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ActionBar actionBar;
    private TextView spotInformationText;
    private ImageIndicatorView imageIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_concrete);
        initSpotInformatin();
        initView();
    }

    private void initSpotInformatin() {
        String spotName = "东三教学楼";
        String spotId = "1";
        String spotText = "东3教学楼一共五层，每层标配两间厕所与两间开水房，理工科专业课和英语相关课程多排在东3，教室间有小隔间可供同学们自习。 ";
        Integer[] imgIDArray ={R.drawable.east_3_1,R.drawable.east_3_2};
        SpotInformation spotInformation = new SpotInformation(spotId,spotName,imgIDArray,spotText);
        spotInformationList.add(spotInformation);
    }

    private void initView()
    {
        Intent intent = getIntent();
        spotid = intent.getStringExtra(SPOT_ID);//获取主页面传过来的id

        String spotName = new String() ;
        String spotId = new String();
        String spotText = new String() ;
        Integer[] imgArray= null;

        for(SpotInformation spotInformation:spotInformationList)
        {

            Log.d("bbbbbb", spotInformation.getSpotID());
            if(spotid.equals(spotInformation.getSpotID()))
            {
                Log.d("aaaaaa", spotid);
                spotName = spotInformation.getSpotName();
                spotId = spotInformation.getSpotID();
                imgArray = spotInformation.getSpotImageID();
                spotText = spotInformation.getSpotInformation();
            }
        }

        imageIndicatorView = (ImageIndicatorView) findViewById(R.id.indicate_view);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        collapsingToolbar =(CollapsingToolbarLayout)findViewById(R.id.collapsing_toolBar);
        spotInformationText = (TextView)findViewById(R.id.spot_information_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        collapsingToolbar.setTitle(spotName);

        /**
         * 轮播图
         */
        // 把数组交给图片展播组件

        imageIndicatorView.setupLayoutByDrawable(imgArray);
        // 展播的风格
//        indicate_view.setIndicateStyle(ImageIndicatorView.INDICATE_ARROW_ROUND_STYLE);
        imageIndicatorView.setIndicateStyle(ImageIndicatorView.INDICATE_USERGUIDE_STYLE);
        // 显示组件
        imageIndicatorView.show();
        final AutoPlayManager autoBrocastManager = new AutoPlayManager(imageIndicatorView);
        //设置开启自动广播
        autoBrocastManager.setBroadcastEnable(true);
        //autoBrocastManager.setBroadCastTimes(5);//loop times
        //设置开始时间和间隔时间
        autoBrocastManager.setBroadcastTimeIntevel(3000, 3000);
        //设置循环播放
        autoBrocastManager.loop();

        spotInformationText.setText(spotText);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
           return super.onOptionsItemSelected(item);
    }




}
