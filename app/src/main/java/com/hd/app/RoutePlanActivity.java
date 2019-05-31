package com.hd.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import apiTools.BikingRouteOverlay;
import apiTools.OverlayManager;
import apiTools.WalkingRouteOverlay;
import module.RouteColloctionItem;

import static android.view.View.GONE;
import static com.hd.app.MainActivity.lastX;

/**
 * 路径规划
 */

public class RoutePlanActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener {

    /**
     * title
     */
    private TextView titleName;
    private Button titleRightButton;
    /**
     * 界面元素
     */
    private TextView beginText;
    private TextView endText;
    private Button searchButton;
    private CheckBox routeCollectBox;
    private TextView costTimeText;
    private TextView distanceText;
    private CardView routeInformCard;
    private Button cardHideButton;
    private Button cardShowButton;
    private Button walkButton;
    private Button bikeButton;


    private TextView chooseRouteText;
    private Button chooseRouteLeft;
    private Button chooseRouteRight;
    //骑行步行选择按钮




    private double mCurrentLantitude;
    private double mCurrentLongitude;
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private MyLocationConfiguration myLocationConfiguration;
    public LocationClient mLocationClient;

    /**
     * 当前的精度
     */
    private float mCurrentAccracy;
    private int mCurrentDirection = 0;


    /**
     * 传感器
     */
    private SensorManager sensorManager;

    private Sensor accelerometerSensor;

    private Sensor magneticFieldSensor;

    private float[] accelerometerValues = new float[3];

    private float[] magneticValues = new float[3];

    //旋转矩阵,用来保存磁场和加速度的数据
    private float[] r = new float[9];

    //模拟方向传感器的数据(原始数据为弧度)
    private float[] values = new float[3];


    /**
     * 地图元素
     */
    private MapView mMapView =null;
    BaiduMap mBaiduMap = null;
    private RouteLine routeLine;
    RoutePlanSearch mSearch = null;
    MassTransitRouteLine massroute;

    private WalkingRouteResult nowResultwalk = null;
    private BikingRouteResult nowResultbike = null;
    OverlayManager routeOverlay = null;

    boolean hasShownDialogue = false;

    private String beginLocation;
    private String endLocation;


    private List<RouteColloctionItem> routeList = new ArrayList<>();//路径链表
    private int listPoint = 0;
    private RouteLine route = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan);
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        init();
        initMap();
        getSensorManager();
        setListener();
    }





    private void init()
    {
        titleRightButton = (Button)findViewById(R.id.custom_icon);
        titleRightButton.setBackgroundColor(getResources().getColor(R.color.appBlue));
        titleName = (TextView)findViewById(R.id.title_name);
        titleName.setText("路径规划");

        beginText = (TextView)findViewById(R.id.begin_text);
        endText = (TextView)findViewById(R.id.end_text);
        searchButton = (Button)findViewById(R.id.search_route_icon);

        routeCollectBox = (CheckBox)findViewById(R.id.collect_route_icon);
        costTimeText = (TextView)findViewById(R.id.route_time_text);
        distanceText = (TextView)findViewById(R.id.route_length_text);
        routeInformCard =(CardView)findViewById(R.id.route_inform_card);
        cardHideButton = (Button)findViewById(R.id.hide_routecard_icon);
        cardShowButton = (Button)findViewById(R.id.show_routecard_icon);

        walkButton = (Button) findViewById(R.id.on_walk);
        bikeButton = (Button)findViewById(R.id.on_bike);

        mMapView = (MapView)findViewById(R.id.bmapRView);

        chooseRouteText = (TextView)findViewById(R.id.choose_route_text);
        chooseRouteLeft = (Button)findViewById(R.id.route_left);
        chooseRouteRight = (Button)findViewById(R.id.route_right);


    }


    private void initMap()
    {
        //可通过options设置地图状态
        BaiduMapOptions options = new BaiduMapOptions();
        MapView mapView = new MapView(this, options);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(16.0f);//定位初始精度200米
        mBaiduMap = mMapView.getMap();//获取地图实例
        // 地图点击事件处理
        mBaiduMap.setOnMapClickListener(RoutePlanActivity.this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(RoutePlanActivity.this);


        float zoom = mBaiduMap.getMapStatus().zoom;
        // mapView.showZoomControls(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//设置地图初始状态
        // 开启定位图层，一定不要少了这句，否则对在地图的设置、绘制定位点将无效
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
        RoutePlanActivity.MyLocationListener myLocationListener = new RoutePlanActivity.MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
        // 初始化传感器
    }

    private void setListener()
    {
        cardHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeInformCard.setVisibility(GONE);
                cardShowButton.setVisibility(View.VISIBLE);
            }
        });
        cardShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeInformCard.setVisibility(View.VISIBLE);
                cardShowButton.setVisibility(GONE);
            }
        });

       walkButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               walkButton.setBackgroundResource(R.drawable.route_mode_walk_select);
               walkButton.setTextColor(getResources().getColor(R.color.white));
               bikeButton.setBackgroundResource(R.drawable.route_mode_bike_notselect);
               bikeButton.setTextColor(getResources().getColor(R.color.appBlue));
               walkingRoutePlan(beginLocation,endLocation);
           }
       });

       bikeButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               bikeButton.setBackgroundResource(R.drawable.route_mode_bike_select);
               bikeButton.setTextColor(getResources().getColor(R.color.white));
               walkButton.setBackgroundResource(R.drawable.route_mode_walk_notselect);
               walkButton.setTextColor(getResources().getColor(R.color.appBlue));
               bikingRoutePlan(beginLocation,endLocation);

           }
       });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginLocation = beginText.getText().toString();
                endLocation = endText.getText().toString();
                if(beginLocation.isEmpty()||endLocation.isEmpty())
                {
                    Toast.makeText(RoutePlanActivity.this,"起点和终点不能为空",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    walkButton.setBackgroundResource(R.drawable.route_mode_walk_select);
                    walkButton.setTextColor(getResources().getColor(R.color.white));
                    bikeButton.setBackgroundResource(R.drawable.route_mode_bike_notselect);
                    bikeButton.setTextColor(getResources().getColor(R.color.appBlue));
                    walkingRoutePlan(beginLocation,endLocation);
                    routeInformCard.setVisibility(View.VISIBLE);
                }
            }
        });
        chooseRouteLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listPoint>0)
                {
                    listPoint--;
                    chooseRouteText.setText("路线"+String.valueOf(listPoint+1));
                    RouteColloctionItem rt = routeList.get(listPoint);
                    distanceText.setText(String.valueOf(rt.getDistance())+"米");
                    costTimeText.setText(String.valueOf(rt.getTakeTime()/60)+"分钟");
                }
                else
                {
                    return;
                }
            }
        });
        chooseRouteRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listPoint<routeList.size()-1)
                {
                    listPoint++;
                    chooseRouteText.setText("路线"+String.valueOf(listPoint+1));
                    RouteColloctionItem rt = routeList.get(listPoint);
                    distanceText.setText(String.valueOf(rt.getDistance())+"米");
                    costTimeText.setText(String.valueOf(rt.getTakeTime()/60)+"分钟");
                }
                else
                {
                    return;
                }
            }
        });
    }




    public void getSensorManager() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        /**
         * 传入的参数决定传感器的类型
         * Senor.TYPE_ACCELEROMETER: 加速度传感器
         * Senor.TYPE_LIGHT:光照传感器
         * Senor.TYPE_GRAVITY:重力传感器
         * SenorManager.getOrientation(); //方向传感器
         */
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



    }


    /**
     * 加速度传感器和磁场传感器调用（用来获取方向）
     */

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                //这里是对象,需要克隆一份,否则共用一份数据
                accelerometerValues = event.values.clone();
            }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                //这里是对象,需要克隆一份,否则共用一份数据
                magneticValues = event.values.clone();
            }
            /**
             * 填充旋转数组r
             * r:要填充的旋转数组
             * I:将磁场数据转换成实际的重力坐标中,一般可以设置为null
             * gravity:加速度传感器数据
             * geomagnetic:地磁传感器数据
             */
            SensorManager.getRotationMatrix(r,null,accelerometerValues,magneticValues);
            /**
             * R:旋转数组
             * values :模拟方向传感器的数据
             */
            sensorManager.getOrientation(r,values);
            double mXDirection =  Math.toDegrees(values[0]);
            if(Math.abs(mXDirection - lastX)>1.0) {
                mCurrentDirection = (int)mXDirection;
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction( mCurrentDirection ).latitude(mCurrentLantitude)
                        .longitude(mCurrentLongitude).build();

                mBaiduMap.setMyLocationData(locData);
            }
            lastX = mXDirection;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //注册的Sensor精度发生变化时,在此处处理
        }

    };






    private void walkingRoutePlan(String begin,String end){
        mBaiduMap.clear();
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("福州", begin);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("福州", end);
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode).to(enNode));

    }

    private void bikingRoutePlan(String begin,String end)
    {
        mBaiduMap.clear();
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("福州", begin);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("福州", end);
        mSearch.bikingSearch((new BikingRoutePlanOption())
                .from(stNode).to(enNode));
    }







    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    /**
     * 步行路线回调函数
     * @param result
     */
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {

        routeList.clear();

        if (null == result) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo();
            AlertDialog.Builder builder = new AlertDialog.Builder(RoutePlanActivity.this);
            builder.setTitle("提示");
            builder.setMessage("检索地址存在歧义");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }

        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            Log.d("aaaaaab",String.valueOf(result.getRouteLines().size()) );
            if (result.getRouteLines().size() > 1) {
                Toast.makeText(RoutePlanActivity.this,"找到合适步行路径"+String.valueOf(result.getRouteLines().size())+"条",Toast.LENGTH_SHORT).show();

                for(int i = 0;i<result.getRouteLines().size();i++)
                {
                    route = result.getRouteLines().get(i);
                    Date dt = new Date();
                    RouteColloctionItem routeItem = new RouteColloctionItem("骑行",dt.toString(),route.getStarting().getTitle(),route.getTerminal().getTitle(),route.getDuration() / 60,route.getDistance());
                    routeList.add(routeItem);
                    listPoint = 0;
                }
                route = result.getRouteLines().get(0);
                chooseRouteText.setText("路径1");
                costTimeText.setText(String.valueOf(route.getDuration()/60)+"分钟");
                distanceText.setText(String.valueOf(route.getDistance())+"米");
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示

                Toast.makeText(RoutePlanActivity.this,"找到合适步行路径1条",Toast.LENGTH_SHORT).show();
                route = result.getRouteLines().get(0);
                Date dt = new Date();
                RouteColloctionItem routeItem = new RouteColloctionItem("骑行",dt.toString(),route.getStarting().getTitle(),route.getTerminal().getTitle(),route.getDuration() / 60,route.getDistance());
                routeList.add(routeItem);
                chooseRouteText.setText("路径1");
                costTimeText.setText(String.valueOf(route.getDuration()/60)+"分钟");
                distanceText.setText(String.valueOf(route.getDistance())+"米");
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                Log.d("步行规划", String.valueOf(route.getDuration()/60));

            } else {
                Toast.makeText(RoutePlanActivity.this,"未找到合适步行路径",Toast.LENGTH_SHORT).show();
                Log.d("route result", "结果数<0");
            }

        }
    }


    /**
     * 骑行路线回调函数
     * @param
     */
    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        routeList.clear();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(RoutePlanActivity.this);
            builder.setTitle("提示");
            builder.setMessage("检索地址存在歧义");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//
            if (result.getRouteLines().size() > 1) {
                Toast.makeText(RoutePlanActivity.this,"找到合适骑行路径"+String.valueOf(result.getRouteLines().size())+"条",Toast.LENGTH_SHORT).show();

                for (int i = 0; i < result.getRouteLines().size(); i++) {

                    route = result.getRouteLines().get(i);
                    Date dt = new Date();
                    RouteColloctionItem routeItem = new RouteColloctionItem("骑行",dt.toString(),route.getStarting().getTitle(),route.getTerminal().getTitle(),route.getDuration() / 60,route.getDistance());
                    routeList.add(routeItem);
                    listPoint = 0;
                }
                route = result.getRouteLines().get(0);
                chooseRouteText.setText("路径1");
                costTimeText.setText(String.valueOf(route.getDuration() / 60) + "分钟");
                distanceText.setText(String.valueOf(route.getDistance()) + "米");
                BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else if (result.getRouteLines().size() == 1) {
                Toast.makeText(RoutePlanActivity.this,"找到合适骑行路径1条",Toast.LENGTH_SHORT).show();
                route = result.getRouteLines().get(0);
                Date dt = new Date();
                RouteColloctionItem routeItem = new RouteColloctionItem("骑行",dt.toString(),route.getStarting().getTitle(),route.getTerminal().getTitle(),route.getDuration() / 60,route.getDistance());
                routeList.add(routeItem);
                costTimeText.setText(String.valueOf(route.getDuration() / 60) + "分钟");
                distanceText.setText(String.valueOf(route.getDistance()) + "米");
                BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
                routeOverlay = overlay;
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
//                mBtnPre.setVisibility(View.VISIBLE);
//                mBtnNext.setVisibility(View.VISIBLE);
                Log.d("骑行规划",String.valueOf(route.getDuration()/60));
            } else {
                Toast.makeText(RoutePlanActivity.this,"未找到合适骑行路径",Toast.LENGTH_SHORT).show();
                return;
            }

        }

    }


    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }









    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();

            sensorManager.unregisterListener(listener,accelerometerSensor);
            sensorManager.unregisterListener(listener,magneticFieldSensor);


    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        sensorManager.registerListener(listener,accelerometerSensor,SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener,magneticFieldSensor,SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onDestroy() {
        if (mSearch != null) {
            mSearch.destroy();
        }
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }


    /**
     * 设置自定义的起点和终点图标
     */
//    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
//
//        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
//            super(baiduMap);
//        }
//
//        @Override
//        public BitmapDescriptor getStartMarker() {
//            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
//            }
//            return null;
//        }
//
//        @Override
//        public BitmapDescriptor getTerminalMarker() {
//            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
//            }
//            return null;
//        }
//    }
//
//    private class MyBikingRouteOverlay extends BikingRouteOverlay {
//        public MyBikingRouteOverlay(BaiduMap baiduMap) {
//            super(baiduMap);
//        }
//
//        @Override
//        public BitmapDescriptor getStartMarker() {
//            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
//            }
//            return null;
//        }
//
//        @Override
//        public BitmapDescriptor getTerminalMarker() {
//            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
//            }
//            return null;
//        }
//
//
//    }



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

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction((int)lastX ).latitude(mCurrentLantitude)
                    .longitude(mCurrentLongitude).build();

            mBaiduMap.setMyLocationData(locData);

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

