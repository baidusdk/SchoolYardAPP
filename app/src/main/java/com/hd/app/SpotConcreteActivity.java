package com.hd.app;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.ocnyang.pagetransformerhelp.BannerItemBean;
import com.ocnyang.pagetransformerhelp.BannerViewPager;
import com.ocnyang.pagetransformerhelp.ImageLoaderInterface;
import com.ocnyang.pagetransformerhelp.transformer.AccordionTransformer;

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
    private BannerViewPager soptPager;
    private Integer[] imgArray= null;
    private FloatingActionButton takeThereIcon;
    private double spotLatitude;
    private double spotLogitude;

    private String spotName = new String() ;
    private String spotId = new String();
    private String spotText = new String() ;


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
        SpotInformation spotInformation = new SpotInformation(spotId,spotName,imgIDArray,spotText,26.0646793692,119.2042646576);
        spotInformationList.add(spotInformation);
    }

    private void initView()
    {
        Intent intent = getIntent();
        spotid = intent.getStringExtra(SPOT_ID);//获取主页面传过来的id


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
                spotLatitude = spotInformation.getSpotLatitude();
                spotLogitude = spotInformation.getSpotLongitude();
            }
        }

        takeThereIcon = (FloatingActionButton)findViewById(R.id.take_there_icon);
        /**
         * 点击去那里按钮，跳转进入路线规划界面
         */
        takeThereIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotConcreteActivity.this, NavigationActivity.class);
                intent.putExtra("action","1");
                intent.putExtra("latitude",spotLatitude);
                intent.putExtra("logitude",spotLogitude);
                intent.putExtra("spotName",spotName);
                startActivity(intent);
            }
        });







        soptPager = (BannerViewPager) findViewById(R.id.spot_pager);
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

        soptPager.setData(getViewPagerDatas(),//设置数据
                new ImageLoaderInterface() {//设置图片加载器
                    @Override
                    public void displayImage(Context context, Object imgPath, ImageView imageView) {
                        Glide.with(context).load(imgPath).into(imageView);
                    }
                }).setPageTransformer(new AccordionTransformer())//设置切换效果
                .setAutoPlay(true)//设置是否自动播放
                .setOnBannerItemClickListener(new BannerViewPager.OnBannerItemClickListener() {//设置item的监听事件
                    @Override
                    public void OnClickLister(View view, int currentItem) {


                        LayoutInflater inflater = LayoutInflater.from(SpotConcreteActivity.this);

                        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null); // 加载自定义的布局文件

                        PhotoView img = (PhotoView) imgEntryView.findViewById(R.id.large_image);
                        img.enable();//图片缩放手势允许
                        img.setImageResource(imgArray[currentItem]); // 自己的图片设置方法

                        final MyDialog dialog = new MyDialog(SpotConcreteActivity.this, 0, 0, imgEntryView, R.style.DialogTheme);
                        dialog.setCancelable(true);
                        // 自定义dialog
                        Button close = (Button)imgEntryView.findViewById(R.id.dialog_close);

                        dialog.show();

// 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮

                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });

                    }
                })
                .setHaveTitle(false);//设置是否显示标题
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

    private List<BannerItemBean> getViewPagerDatas() {
        List<BannerItemBean> pagerItemBeanList = new ArrayList<>(imgArray.length);

        for (int i = 0; i < imgArray.length; i++) {
            pagerItemBeanList.add(new BannerItemBean(imgArray[i], ""));
        }
        return pagerItemBeanList;
    }

    public class MyDialog extends Dialog {
        //    style引用style样式
        public MyDialog(Context context, int width, int height, View layout, int style) {

            super(context, style);

            setContentView(layout);

            Window window = getWindow();

            WindowManager.LayoutParams params = window.getAttributes();

            params.gravity = Gravity.CENTER;

            window.setAttributes(params);
        }
    }



}
