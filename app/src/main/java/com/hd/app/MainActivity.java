package com.hd.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.hd.app.R;
import com.bm.library.PhotoView;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.List;

import apiTools.MyLocationListener;
import apiTools.MyOrientationListener;
import adapter.FloorListAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import module.Building;
import module.Spot;
import module.User;

import static apiTools.MyLocationListener.nlocation;


public class MainActivity extends AppCompatActivity {
    public static MapView mMapView = null;
    public static BaiduMap mBaiduMap;

    private Button routeOpen;
    private Button personList;
    private EditText searchContent;
    private Button search;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private  Button locationButton;
    private Button follow_icon;
    private Button normal_icon;
    private CheckBox spotOpenCheck;
    /**
     * 下面三个元素是室内图功能的按键
     */
    private CardView cardView;
    private Button closeFloorCard;
    private TextView buildingNameText;
    public static PhotoView floorImg;
    //


    private ProgressBar markerInitProgress;
    public LocationClient mLocationClient;
    private List<String> permissionList = new ArrayList<>();//申请的静态权限表
    public static MyOrientationListener myOrientationListener;
    public static int mXDirection;

    public static double lastX = 0.0;
    private double mCurrentLantitude;
    private double mCurrentLongitude;
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    /**
     * 当前的精度
     */
    private float mCurrentAccracy;
    private int mCurrentDirection = 0;

    public static BDLocation nlocation = null;

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

    List<Building> buildingList = new ArrayList<>();



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


    /**
     * 初始化控件
     */
    private void init() {

        //获取地图控件引用
        markerInitProgress = (ProgressBar)findViewById(R.id.marker_init_progress);
        BaiduMapOptions options = new BaiduMapOptions();
        //初始化控件
        routeOpen = (Button)findViewById(R.id.route_icon);
        normal_icon = (Button)findViewById(R.id.icon_normal);
        follow_icon = (Button)findViewById(R.id.icon_follow);
        personList = (Button) findViewById(R.id.person_icon);
        searchContent = (EditText) findViewById(R.id.search_text);
        search = (Button) findViewById(R.id.search_icon);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mMapView = (MapView) findViewById(R.id.bmapView);
//        myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS,true,);
        locationButton = (Button)findViewById(R.id.locotion_icon);
        spotOpenCheck = (CheckBox)findViewById(R.id.spot_open_icon);

        cardView = (CardView)findViewById(R.id.floor_card);
        closeFloorCard = (Button)findViewById(R.id.close_floorView);
        buildingNameText = (TextView)findViewById(R.id.building_name);
        floorImg = (PhotoView)findViewById(R.id.floor_img);
        floorImg.enable();


        LatLng p = new LatLng(1.0,1.0);
        Integer[] temp = {R.drawable.library_f5,R.drawable.library_f4,R.drawable.library_f3,R.drawable.library_f2,R.drawable.library_f1,
        };



        Building b = new Building("图书馆",p,0,5,temp);
        buildingList.add(b);

//        PhotoView ph = (PhotoView)findViewById(R.id.floor_img);
//        ph.enable();
//        ph.setImageResource(R.drawable.library_f1);


    }


    /**
     * 初始化地图
     */

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
        mBaiduMap.setMyLocationEnabled(true); //定位初始化
        //mBaiduMap.setIndoorEnable(true);//打开室内图
        myLocationConfiguration = new MyLocationConfiguration(mCurrentMode,true,null);
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);
        mLocationClient = new LocationClient(getApplicationContext());
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//定位的时间间隔//毫秒
        option.setNeedDeviceDirect(true);
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

        routeOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RoutePlanActivity.class);
                startActivity(intent);
            }
        });



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
        myOrientationListener.setmOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mXDirection =(int) x;
            }
        });

            }
        });
        spotOpenCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    spotOpenCheck.setChecked(true);
                    markerInitProgress.setVisibility(View.VISIBLE);
                    initMapMarker();
                    markerInitProgress.setVisibility(View.GONE);

                }
                else
                {
                    spotOpenCheck.setChecked(false);
                    markerInitProgress.setVisibility(View.VISIBLE);
                    removeMarker();
                    markerInitProgress.setVisibility(View.GONE);

                }
            }
        });


        closeFloorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setVisibility(View.GONE);
            }
        });

//设置地图单击事件监听
        mBaiduMap.setOnMapClickListener(mapListener);

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
    BaiduMap.OnMapClickListener mapListener = new BaiduMap.OnMapClickListener() {
        /**
         * 地图单击事件回调函数
         *
         * @param point 点击的地理坐标
         */
        @Override
        public void onMapClick(LatLng point) {

            if(cardView.getVisibility()==View.VISIBLE)
            {
                cardView.setVisibility(View.GONE);
            }

        }
        /**
         * 地图内 Poi 单击事件回调函数
         *
         * @param mapPoi 点击的 poi 信息
         */
        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {
            String poiName =mapPoi.getName(); //名称
            LatLng point = mapPoi.getPosition(); //坐标
            RecyclerView recyclerView = findViewById(R.id.floor_list_icon);
            LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
            recyclerView.setLayoutManager(layoutManager);
            boolean flag = false;
            for(int i = 0 ;i<buildingList.size();i++)
            {
                Building b = buildingList.get(i);

                if(b.getBuildingName().equals(poiName)) {
                    floorImg.setImageResource(R.drawable.select_floor_back);
                    Log.d("地理位置", b.getBuildingName());
                    FloorListAdapter adapter = new FloorListAdapter(b.getFloorList());
                    recyclerView.setAdapter(adapter);
                    buildingNameText.setText(poiName);
                    cardView.setVisibility(View.VISIBLE);
                    flag = true;
                    break;
                }
            }
            if(!flag)
            {
                Toast.makeText(MainActivity.this,"当前地点尚未加入室内图",Toast.LENGTH_SHORT).show();
            }
            return false;
        }



    };




    /**
     * 删除marker
     */
    private void removeMarker() {
        int i = 0;
        for(i=0;i<markerList.size();i++)
        {
            Marker m = markerList.get(i);
            m.remove();
        }
    }

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




    /**
     * 定位监听器类
     */
    public class MyLocationListener extends BDAbstractLocationListener {

        private boolean isFirstLocate = true;
        //定位模式
        //是否是第一次定位
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null){
                return;
            }
            //mapView 销毁后不在处理新接收的位置
            mCurrentAccracy = location.getRadius();
            mCurrentLantitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();
            nlocation = location;
            if (location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
                return;
            }

        }
        private void navigateTo(BDLocation location)
        {
            if(isFirstLocate)
            {
                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(18f);
                mBaiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }

        }
    }


}
