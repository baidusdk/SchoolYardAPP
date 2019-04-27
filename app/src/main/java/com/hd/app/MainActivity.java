package com.hd.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;


import java.util.ArrayList;
import java.util.List;

import apiTools.MyLocationListener;
import apiTools.MyOrientationListener;

import static apiTools.MyLocationListener.nlocation;


public class MainActivity extends AppCompatActivity {
    public static MapView mMapView = null;
    public static BaiduMap mBaiduMap;
    private Button personList;
    private EditText searchContent;
    private Button search;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private  Button locationButton;
    public LocationClient mLocationClient;
    private List<String> permissionList = new ArrayList<>();//申请的静态权限表
    public static MyOrientationListener myOrientationListener;
    public static int mXDirection;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magneticFieldSensor;
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];
    //旋转矩阵,用来保存磁场和加速度的数据
    private float[] r = new float[9];
    //模拟方向传感器的数据(原始数据为弧度)
    private float[] values = new float[3];




//    MyLocationConfiguration(LocationMode mode,
//                            boolean enableDirection,
//                            BitmapDescriptor customMarker,
//                            int accuracyCircleFillColor,
//                            int accuracyCircleStrokeColor)
//    mCurrentMode = LocationMode.FOLLOWING;//定位跟随态
//    mCurrentMode = LocationMode.NORMAL;   //默认为 LocationMode.NORMAL 普通态
//    mCurrentMode = LocationMode.COMPASS;  //定位罗盘态
//    mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);//支持自定义定位图标样式，替换定位icon
//
//            accuracyCircleFillColor = 0xAAFFFF88;  //自定义精度圈填充颜色
//
//    accuracyCircleStrokeColor = 0xAA00FF00;//自定义精度圈边框颜色

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        requestAuthority();//静态申请手机权限
       // getSensorManager();
        init();
        initMap();//初始化地图
        setListener();
    }

    //静态申请手机权限

    private void requestAuthority()
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);//申请手机定位权限
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);//读取系统信息，包含系统版本等信息，用作统计
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);//读写内存
        }
        if(!permissionList.isEmpty())
        {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
    }
    //权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode,String [] permissions,int[]grantResults)
    {
        switch (requestCode)
        {
            case 1: {
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须要同意以上所有权限才能正常使用app", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                    }
                }
                else{
                    Toast.makeText(this,"未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
                default:
        }
    }


    private void initMap() {
        //可通过options设置地图状态
        BaiduMapOptions options = new BaiduMapOptions();
        MapView mapView = new MapView(this, options);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);//定位初始精度50米
        mBaiduMap = mMapView.getMap();//获取地图实例
       // mapView.showZoomControls(true);

        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//设置地图初始状态
        // 开启定位图层，一定不要少了这句，否则对在地图的设置、绘制定位点将无效
        mBaiduMap.setMyLocationEnabled(true);
        //定位初始化
        mLocationClient = new LocationClient(getApplicationContext());
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//定位的时间间隔//毫秒
       // option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//开启高精度定位
        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        Log.d("定位测试","执行过这里");
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

    private void setListener() {
        personList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        navigationView.setCheckedItem(R.id.scenery_icon);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng ll = new LatLng(nlocation.getLatitude(),nlocation.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(18f);
                mBaiduMap.animateMapStatus(update);
            }
        });
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mXDirection =(int) x;
            }
        });

    }

    private void init() {
        //获取地图控件引用
        BaiduMapOptions options = new BaiduMapOptions();
        //初始化控件
        personList = (Button) findViewById(R.id.person_icon);
        searchContent = (EditText) findViewById(R.id.search_text);
        search = (Button) findViewById(R.id.search_icon);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mMapView = (MapView) findViewById(R.id.bmapView);
//        myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS,true,);
       locationButton = (Button)findViewById(R.id.locotion_icon);

        myOrientationListener = new MyOrientationListener(
                getApplicationContext());
    }



    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
