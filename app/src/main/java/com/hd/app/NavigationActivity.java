package com.hd.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBNaviStatusListener;
import com.baidu.mapapi.bikenavi.adapter.IBRouteGuidanceListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.adapter.IBTTSPlayer;
import com.baidu.mapapi.bikenavi.model.BikeRouteDetailInfo;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
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
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
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
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.walknavi.model.RouteGuideKind;
import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.hd.app.adapter.PoiHistoryAdapter;
import com.hd.app.adapter.PoiSuggestionAdapter;
import com.hd.app.adapter.RecyclerViewDivider;
import com.hd.app.base.BaseActivity;
import com.hd.app.control.InitConfig;
import com.hd.app.listener.UiMessageListener;
import com.hd.app.util.AutoCheck;
import com.hd.app.util.LocationManager;
import com.hd.app.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apiTools.BikingRouteOverlay;
import apiTools.OverlayManager;
import apiTools.WalkingRouteOverlay;
import module.RouteColloctionItem;

import static android.view.View.GONE;
import static com.hd.app.MainActivity.lastX;


/**
 * Created by Only_ZziTai on 17/3/29.
 */

public class NavigationActivity extends BaseActivity implements
        OnGetSuggestionResultListener, PoiSuggestionAdapter.OnItemClickListener
        , PoiHistoryAdapter.OnHistoryItemClickListener, BaiduMap.OnMapClickListener, OnGetRoutePlanResultListener {

    /**
     * title
     */
    private TextView titleName;
    private Button titleRightButton;
    /**
     * ===============UI初始================
     */
    private RelativeLayout routeMap;
    private Button locationButton;
    private BDLocation nlocation = null;
    private ImageView returnIocn;
    private TextView chooseRouteText;
    private Button chooseRouteLeft;
    private Button chooseRouteRight;
    private CheckBox routeCollectBox;
    private TextView costTimeText;
    private TextView distanceText;
    private LinearLayout routeInformCard;
    private Button cardHideButton;
    private Button cardShowButton;
    //骑行步行选择按钮
    private Button walkButton;
    private Button bikeButton;


    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "16454797";

    protected String appKey = "isBkX92ODa88x9CjSxFfPPkV";

    protected String secretKey = "dUEe9WR5vbvXnfxDQeS0IsQHcHGg4fmu";

    private static final String TEXT = "欢迎使用百度语音合成，请在代码中修改合成文本";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;

    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================
    //初始化经纬度
    private PlanNode stNode = null;
    private PlanNode enNode = null;
    private LatLng tempLL=null;
    private LatLng startLL=null;
    private LatLng endLL=null;
    protected SpeechSynthesizer mSpeechSynthesizer;
    protected Handler mainHandler;
    private double mCurrentLantitude;
    private double mCurrentLongitude;
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private MyLocationConfiguration myLocationConfiguration;
    public LocationClient mLocationClient;
    BikeNavigateHelper mNaviHelper;
    BikeNaviLaunchParam param;

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
    private MapView mMapView = null;

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


    /**
     * POI+SUGGESTION元素
     */


    private static final String TAG = "navigationActivity";

    LinearLayout placeSearchLayout;

    RelativeLayout title_content_layout;

    EditText placeEdit;

    TextView start_place_edit, destination_edit, searchButton;

    RecyclerView recyclerviewPoi, recyclerviewPoiHistory;

    private List<SuggestionResult.SuggestionInfo> suggestionInfoList;

    private SuggestionSearch mSuggestionSearch = null;

    PoiSuggestionAdapter sugAdapter;

    boolean firstSetAdapter = true, isStartPoi = true;

    String currentAddress, start_place, destination;


    PoiHistoryAdapter poiHistoryAdapter;

    PoiSearch poiSearch;

    private List<PoiInfo> poiInfo = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setStatusBar();
        initConfig();
        initPermission();
        intiNavEnigne();
        initTTs();
        initMap();
        getSensorManager();
        initPoiListener();
        setListener();
    }


    private void setListener() {
        returnIocn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                walkingRoutePlan(beginLocation, endLocation);
            }
        });

        bikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bikeButton.setBackgroundResource(R.drawable.route_mode_bike_select);
                bikeButton.setTextColor(getResources().getColor(R.color.white));
                walkButton.setBackgroundResource(R.drawable.route_mode_walk_notselect);
                walkButton.setTextColor(getResources().getColor(R.color.appBlue));
                bikingRoutePlan(beginLocation, endLocation);

            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginLocation = start_place_edit.getText().toString().trim();
                endLocation = destination_edit.getText().toString().trim();
                if (beginLocation.isEmpty() || endLocation.isEmpty()) {
                    Toast.makeText(NavigationActivity.this, "起点和终点不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    routeMap.setVisibility(View.VISIBLE);
                    walkButton.setBackgroundResource(R.drawable.route_mode_walk_select);
                    walkButton.setTextColor(getResources().getColor(R.color.white));
                    bikeButton.setBackgroundResource(R.drawable.route_mode_bike_notselect);
                    bikeButton.setTextColor(getResources().getColor(R.color.appBlue));
                    walkingRoutePlan(beginLocation, endLocation);
                    routeInformCard.setVisibility(View.VISIBLE);
                }
            }
        });
        chooseRouteLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPoint > 0) {
                    listPoint--;
                    chooseRouteText.setText("路线" + String.valueOf(listPoint + 1));
                    RouteColloctionItem rt = routeList.get(listPoint);
                    distanceText.setText(String.valueOf(rt.getDistance()) + "米");
                    costTimeText.setText(String.valueOf(rt.getTakeTime() / 60) + "分钟");
                } else {
                    return;
                }
            }
        });
        chooseRouteRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPoint < routeList.size() - 1) {
                    listPoint++;
                    chooseRouteText.setText("路线" + String.valueOf(listPoint + 1));
                    RouteColloctionItem rt = routeList.get(listPoint);
                    distanceText.setText(String.valueOf(rt.getDistance()) + "米");
                    costTimeText.setText(String.valueOf(rt.getTakeTime() / 60) + "分钟");
                } else {
                    return;
                }
            }
        });
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nlocation != null) {
                    if (nlocation != null) {
                        LatLng ll = new LatLng(nlocation.getLatitude(), nlocation.getLongitude());
                        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                        mBaiduMap.animateMapStatus(update);
                        update = MapStatusUpdateFactory.zoomTo(18f);
                        mBaiduMap.animateMapStatus(update);
                    }
                }
            }
        });
    }
    private void intiNavEnigne(){
        BikeNavigateHelper.getInstance().initNaviEngine(this, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                Log.d("View", "engineInitSuccess");
                if(stNode.toString()!=null&&enNode.toString()!=null)
                {
                    BikeRouteNodeInfo startBikeRouteNodeInfo = new BikeRouteNodeInfo();
                    BikeRouteNodeInfo endBikeRouteNodeInfo = new BikeRouteNodeInfo();
                    startBikeRouteNodeInfo.setLocation(startLL);
                    endBikeRouteNodeInfo.setLocation(endLL);
                    param = new BikeNaviLaunchParam().startNodeInfo(startBikeRouteNodeInfo).endNodeInfo(endBikeRouteNodeInfo).vehicle(0);
                }
            }
            @Override
            public void engineInitFail() {
                Log.d("View", "engineInitFail");
            }
        });
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(param, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d(TAG,"开始算路");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d(TAG,"算路成功");
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError bikeRoutePlanError) {
                Log.d(TAG,"算路失败");
            }
        });

        mNaviHelper.setRouteGuidanceListener(this, new IBRouteGuidanceListener() {
            @Override
            public void onRouteGuideIconUpdate(Drawable drawable) {

            }

            @Override
            public void onRouteGuideKind(RouteGuideKind routeGuideKind) {

            }

            @Override
            public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence charSequence1) {

            }

            @Override
            public void onRemainDistanceUpdate(CharSequence charSequence) {

            }

            @Override
            public void onRemainTimeUpdate(CharSequence charSequence) {

            }

            @Override
            public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {

            }

            @Override
            public void onReRouteComplete() {

            }

            @Override
            public void onArriveDest() {

            }

            @Override
            public void onVibrate() {

            }

            @Override
            public void onGetRouteDetailInfo(BikeRouteDetailInfo bikeRouteDetailInfo) {

            }
        });
        mNaviHelper.setTTsPlayer(new IBTTSPlayer() {
            /**
             * 获取导航过程中语音,进行播报
             * @param s 播报语音文本
             * @param b 是否抢占播报
             */
            @Override
            public int playTTSText(String s, boolean b) {
                //调用语音识别SDK的语音回调进行播报
                speak(s);
                return 0;
            }
        });
    }
    private void initConfig() {
        returnIocn = (ImageView) findViewById(R.id.menu_icon);
        routeMap = (RelativeLayout) findViewById(R.id.route_map);
        routeCollectBox = (CheckBox) findViewById(R.id.collect_route_icon);
        costTimeText = (TextView) findViewById(R.id.route_time_text);
        distanceText = (TextView) findViewById(R.id.route_length_text);
        routeInformCard = (LinearLayout) findViewById(R.id.route_inform_card);
        cardHideButton = (Button) findViewById(R.id.hide_routecard_icon);
        cardShowButton = (Button) findViewById(R.id.show_routecard_icon);
        walkButton = (Button) findViewById(R.id.on_walk);
        bikeButton = (Button) findViewById(R.id.on_bike);
        mMapView = (MapView) findViewById(R.id.bmapRView);
        locationButton = (Button) findViewById(R.id.locotion_icon2);
        chooseRouteText = (TextView) findViewById(R.id.choose_route_text);
        chooseRouteLeft = (Button) findViewById(R.id.route_left);
        chooseRouteRight = (Button) findViewById(R.id.route_right);
        //searchButton = (TextView)findViewById(R.id.book_bt);
        currentAddress = LocationManager.getInstance().getAddress();
        placeSearchLayout = findViewById(R.id.place_search_layout);
        title_content_layout = (RelativeLayout) findViewById(R.id.title_content_layout);
        start_place_edit = (TextView) findViewById(R.id.start_place_edit);
        destination_edit = (TextView) findViewById(R.id.destination_edit);
        searchButton = (TextView) findViewById(R.id.book_bt);
        placeEdit = (EditText) findViewById(R.id.place_edit);


        /**
         * recycle 初始化
         */


        recyclerviewPoi = findViewById(R.id.recyclerview_poi);
        recyclerviewPoi.setLayoutManager(new LinearLayoutManager(this));
        recyclerviewPoi.addItemDecoration(new RecyclerViewDivider(
                this, LinearLayoutManager.HORIZONTAL, 1,
                ContextCompat.getColor(this, R.color.color_c8cacc)));
        recyclerviewPoiHistory = (RecyclerView) findViewById(R.id.recyclerview_poi_history);
        recyclerviewPoiHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerviewPoiHistory.addItemDecoration(new RecyclerViewDivider(
                this, LinearLayoutManager.HORIZONTAL, 1,
                ContextCompat.getColor(this, R.color.color_c8cacc)));
        poiHistoryAdapter = new PoiHistoryAdapter(NavigationActivity.this, poiInfo);
        recyclerviewPoiHistory.setAdapter(poiHistoryAdapter);
        poiHistoryAdapter.setOnClickListener(this);
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.obj != null) {
                    Log.d(TAG, "handleMessage: " + msg.obj.toString());
                }
            }

        };
    }


    private void initPermission() {
        {
            String[] permissions = {
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
            };

            ArrayList<String> toApplyList = new ArrayList<String>();

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                    toApplyList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
            String[] tmpList = new String[toApplyList.size()];
            if (!toApplyList.isEmpty()) {
                ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须要同意以上所有权限才能正常使用app", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                    }
                } else {
                    Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:
        }
    }


    /**
     * 检查离线文件
     */

    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                Log.d(TAG, "[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                Log.d(TAG, "[ERROR] 初始化失败！！！");
                return false;
            }
        }
        return true;
    }

    /**
     * 初始化Tts引擎
     */


    private void initTTs() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;
        if (isMix) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                return;
            } else {
                Log.d(TAG, "离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }
        // 日志更新在UI中，可以换成MessageListener，在logcat中查看日志
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);

        // 2. 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(listener);

        // 3. 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        checkResult(result, "setApiKey");

        // 4. 支持离线的话，需要设置离线模型
        if (isMix) {
            // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
            isSuccess = checkAuth();
            if (!isSuccess) {
                return;
            }
            // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        // x. 额外 ： 自动so文件是否复制正确及上面设置的参数
        Map<String, String> params = new HashMap<>();
        // 复制下上面的 mSpeechSynthesizer.setParam参数
        // 上线时请删除AutoCheck的调用
        if (isMix) {
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            /**
             * 开新线程检查，成功后回调
             */
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });

        // 6. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        checkResult(result, "initTts");

    }


    /**
     * 检测权限
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.d(TAG,
                    "【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.d(TAG, "验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.d(TAG, "error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private void speak(String text) {
        /* 以下参数每次合成时都可以修改
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
         *  设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5"); 设置合成的音量，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5"); 设置合成的语速，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5"); 设置合成的语调，0-9 ，默认 5
         *
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
         *  MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         */

        if (mSpeechSynthesizer == null) {
            Log.d(TAG, "[ERROR], 初始化失败");
            return;
        }
        int result = mSpeechSynthesizer.speak(text);
        Log.d(TAG, "合成并播放 按钮已经点击");
        checkResult(result, "speak");
    }


    /**
     * 初始化地图
     */
    private void initMap() {
        //可通过options设置地图状态
        BaiduMapOptions options = new BaiduMapOptions();
        MapView mapView = new MapView(this, options);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(16.0f);//定位初始精度200米
        mBaiduMap = mMapView.getMap();//获取地图实例
        // 地图点击事件处理
        mBaiduMap.setOnMapClickListener(NavigationActivity.this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(NavigationActivity.this);
        float zoom = mBaiduMap.getMapStatus().zoom;
        // mapView.showZoomControls(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//设置地图初始状态
        // 开启定位图层，一定不要少了这句，否则对在地图的设置、绘制定位点将无效
        mBaiduMap.setMyLocationEnabled(true); //定位初始化
        //mBaiduMap.setIndoorEnable(true);//打开室内图
        myLocationConfiguration = new MyLocationConfiguration(mCurrentMode, true, null);
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
        Log.d("定位测试", "执行过这里");
        NavigationActivity.MyLocationListener myLocationListener = new NavigationActivity.MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
        // 初始化传感器
    }


    /**
     * 获得传感器
     */
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
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //这里是对象,需要克隆一份,否则共用一份数据
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
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
            SensorManager.getRotationMatrix(r, null, accelerometerValues, magneticValues);
            /**
             * R:旋转数组
             * values :模拟方向传感器的数据
             */
            sensorManager.getOrientation(r, values);
            double mXDirection = Math.toDegrees(values[0]);
            if (Math.abs(mXDirection - lastX) > 1.0) {
                mCurrentDirection = (int) mXDirection;
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mCurrentDirection).latitude(mCurrentLantitude)
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


    /**
     * 步行导航
     *
     * @param begin
     * @param end
     */

    private void walkingRoutePlan(String begin, String end) {
        mBaiduMap.clear();
        if (begin.equals("我的位置")) {
            startLL = new LatLng(nlocation.getLatitude(), nlocation.getLongitude());
            stNode = PlanNode.withLocation(startLL);
        } else {
            stNode = PlanNode.withCityNameAndPlaceName("福州", begin);
        }
        if (end.equals("我的位置")) {
            endLL = new LatLng(nlocation.getLatitude(), nlocation.getLongitude());
            enNode = PlanNode.withLocation(endLL);

        } else {
            enNode = PlanNode.withCityNameAndPlaceName("福州", end);
        }
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode).to(enNode));

    }


    /**
     * 骑行导航
     *
     * @param begin
     * @param end
     */
    private void bikingRoutePlan(String begin, String end) {

        mBaiduMap.clear();
        //获取BikeNavigateHelper示例
        // 获取诱导页面地图展示View
        mNaviHelper = BikeNavigateHelper.getInstance();
//        View view = mNaviHelper.onCreate(NavigationActivity.this);
//        if (view != null) {
//            setContentView(view);
//        }
        Log.d("骑行路线规划起点", begin);
        Log.d("骑行路线规划终点", end);
        if (begin.equals("我的位置")) {
            startLL= new LatLng(nlocation.getLatitude(), nlocation.getLongitude());
            stNode = PlanNode.withLocation(startLL);
        } else {
            stNode = PlanNode.withCityNameAndPlaceName("福州", begin);
        }
        if (end.equals("我的位置")) {
            endLL = new LatLng(nlocation.getLatitude(), nlocation.getLongitude());
            enNode = PlanNode.withLocation(endLL);
        } else {
            enNode = PlanNode.withCityNameAndPlaceName("福州", end);
        }
        mSearch.bikingSearch((new BikingRoutePlanOption().ridingType(0))
                .from(stNode).to(enNode));
        speak("百度导航为您服务");

       mNaviHelper.startBikeNavi(NavigationActivity.this);
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
     *
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
            AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
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
            Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            Log.d("aaaaaab", String.valueOf(result.getRouteLines().size()));
            if (result.getRouteLines().size() > 1) {
                Toast.makeText(NavigationActivity.this, "找到合适步行路径" + String.valueOf(result.getRouteLines().size()) + "条", Toast.LENGTH_SHORT).show();

                for (int i = 0; i < result.getRouteLines().size(); i++) {
                    route = result.getRouteLines().get(i);
                    Date dt = new Date();
                    RouteColloctionItem routeItem = new RouteColloctionItem("骑行", dt.toString(), route.getStarting().getTitle(), route.getTerminal().getTitle(), route.getDuration() / 60, route.getDistance());
                    routeList.add(routeItem);
                    listPoint = 0;
                }
                route = result.getRouteLines().get(0);
                chooseRouteText.setText("路径1");
                costTimeText.setText(String.valueOf(route.getDuration() / 60) + "分钟");
                distanceText.setText(String.valueOf(route.getDistance()) + "米");
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示

                Toast.makeText(NavigationActivity.this, "找到合适步行路径1条", Toast.LENGTH_SHORT).show();
                route = result.getRouteLines().get(0);
                Date dt = new Date();
                RouteColloctionItem routeItem = new RouteColloctionItem("骑行", dt.toString(), route.getStarting().getTitle(), route.getTerminal().getTitle(), route.getDuration() / 60, route.getDistance());
                routeList.add(routeItem);
                chooseRouteText.setText("路径1");
                costTimeText.setText(String.valueOf(route.getDuration() / 60) + "分钟");
                distanceText.setText(String.valueOf(route.getDistance()) + "米");
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                routeOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                Log.d("步行规划", String.valueOf(route.getDuration() / 60));

            } else {
                Toast.makeText(NavigationActivity.this, "未找到合适步行路径", Toast.LENGTH_SHORT).show();
                Log.d("route result", "结果数<0");
            }

        }
    }


    /**
     * 骑行路线回调函数
     *
     * @param
     */
    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {
        routeList.clear();
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
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
                Toast.makeText(NavigationActivity.this, "找到合适骑行路径" + String.valueOf(result.getRouteLines().size()) + "条", Toast.LENGTH_SHORT).show();

                for (int i = 0; i < result.getRouteLines().size(); i++) {

                    route = result.getRouteLines().get(i);
                    Date dt = new Date();
                    RouteColloctionItem routeItem = new RouteColloctionItem("骑行", dt.toString(), route.getStarting().getTitle(), route.getTerminal().getTitle(), route.getDuration() / 60, route.getDistance());
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
                Toast.makeText(NavigationActivity.this, "找到合适骑行路径1条", Toast.LENGTH_SHORT).show();
                route = result.getRouteLines().get(0);
                Date dt = new Date();
                RouteColloctionItem routeItem = new RouteColloctionItem("骑行", dt.toString(), route.getStarting().getTitle(), route.getTerminal().getTitle(), route.getDuration() / 60, route.getDistance());
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
                Log.d("骑行规划", String.valueOf(route.getDuration() / 60));
            } else {
                Toast.makeText(NavigationActivity.this, "未找到合适骑行路径", Toast.LENGTH_SHORT).show();
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


    public void switchPoi(View view) {
        tempLL = startLL;
        startLL = endLL;
        endLL = tempLL;
        start_place = start_place_edit.getText().toString();
        destination = destination_edit.getText().toString();
        destination_edit.setText(start_place);
        start_place_edit.setText(destination);
        if (start_place_edit.getText().toString().equals(getString(R.string.input_destination))) {
            start_place_edit.setText(getString(R.string.input_start_place));
        }

    }

    public void showInputStart(View view) {
        placeEdit.requestFocus();
        new Utils(this).showIMM();
        setStatusBarLayout();
        routeMap.setVisibility(GONE);
        title_content_layout.setVisibility(View.GONE);
        placeSearchLayout.setVisibility(View.VISIBLE);
        placeEdit.setHint(getString(R.string.input_start_place));
        isStartPoi = true;

        showHistoryPOI();
    }

    public void showInputDestination(View view) {
        placeEdit.requestFocus();
        new Utils(this).showIMM();
        setStatusBarLayout();
        routeMap.setVisibility(GONE);
        title_content_layout.setVisibility(View.GONE);
        placeSearchLayout.setVisibility(View.VISIBLE);
        placeEdit.setHint(getString(R.string.input_destination));
        isStartPoi = false;

        showHistoryPOI();
    }

    public void backFromSearchPlace(View view) {
        setStatusBar();
        new Utils(this).hideIMM();
        placeEdit.setText("");
        if (sugAdapter != null) {
            sugAdapter.changeData(null);
        }
        title_content_layout.setVisibility(View.VISIBLE);
        placeSearchLayout.setVisibility(View.GONE);
    }

    /**
     * 后退箭头的监听
     *
     * @param keyCode
     * @param event
     * @return
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (placeSearchLayout.getVisibility() == View.VISIBLE) {
                backFromSearchPlace(placeSearchLayout);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 建议结果
     *
     * @param res
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            Log.e(TAG, "fail to get suggestions, null");
            return;
        }

        Log.d(TAG, "onGetSuggestionResult" + res.getAllSuggestions());
        recyclerviewPoiHistory.setVisibility(View.GONE);
        suggestionInfoList = res.getAllSuggestions();
        if (firstSetAdapter) {
            String from = isStartPoi ? "start" : "detination";
            //这里适配 很关键。from标识  起点/终点
            sugAdapter = new PoiSuggestionAdapter(this, suggestionInfoList, from);
            recyclerviewPoi.setAdapter(sugAdapter);
            Log.d(TAG, "sugAdapter");
            sugAdapter.setOnClickListener(this);
            firstSetAdapter = false;
        } else {
            sugAdapter.changeData(suggestionInfoList);
        }
    }

    /**
     * 展示历史记录
     */
    private void showHistoryPOI() {
        poiSearch.searchNearby(new PoiNearbySearchOption()
                .location(new LatLng(26.055801, 119.197295)).radius(3000).keyword("福州大学教学楼"
                ));
        recyclerviewPoiHistory.setVisibility(View.VISIBLE);

        if (recyclerviewPoiHistory.getAdapter() == null) {
            Log.d(TAG, "no Adapter");
        } else {
            Log.d(TAG, "is Adapter" + poiInfo.size());
        }
    }


    @Override
    public void onItemClick(View v, int position, String flag, SuggestionResult.SuggestionInfo info) {
        if (isStartPoi) {
            start_place_edit.setText(info.key);
            //         startLL = info.;
        } else {
            destination_edit.setText(info.key);
            //        endLL = info.pt;
        }
        backFromSearchPlace(placeSearchLayout);
    }

    /**
     * 设置历史记录点击项
     *
     * @param v
     * @param position 某项位置
     * @param poiInfo  数据类型
     */
    @Override
    public void onHistoryItemClick(View v, int position, PoiInfo poiInfo) {

        if (isStartPoi) {
            // startLL = new LatLng(Double.parseDouble(poiObject.lattitude), Double.parseDouble(poiObject.longitude));
            start_place_edit.setText(poiInfo.getName());
        } else {
            // endLL = new LatLng(Double.parseDouble(poiObject.lattitude), Double.parseDouble(poiObject.longitude));
            destination_edit.setText(poiInfo.getName());
        }
        backFromSearchPlace(placeSearchLayout);
    }

    /**
     * 初始化POI监听
     */
    private void initPoiListener() {
        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        poiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                poiInfo = poiResult.getAllPoi();
                poiHistoryAdapter.changeData(poiInfo);
                Log.d(TAG, String.valueOf(poiInfo.size()));
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getApplication(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                } else {// 正常返回结果的时候，此处可以获得很多相关信息
                    Toast.makeText(getApplication(), poiDetailResult.getName() + ": "
                                    + poiDetailResult.getAddress(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            }
        };
        poiSearch.setOnGetPoiSearchResultListener(poiListener);

        /**;
         * 当输入关键字变化时，动态更新建议列表
         */
        placeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                Log.d("Only_ZziTai", "afterTextChanged--------------");

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                Log.d("Only_ZziTai", "beforeTextChanged--------------");
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                Log.d("Only_ZziTai", "onTextChanged--------------");


                if (cs.length() <= 0) {
                    return;
                }
                String adds = placeEdit.getText().toString().trim();
                adds = "福州大学" + adds;
                Log.d("placeEdit", adds);

                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch.requestSuggestion(
                        new SuggestionSearchOption()
                                .keyword("福州大学" + cs.toString()).city("福州市")
                                .citylimit(true)
                );
            }
        });
        placeEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.i("ABC", "搜索操作执行:" + placeEdit.getText());
                    if (isStartPoi) {
                        start_place_edit.setText(placeEdit.getText());

                    } else {
                        destination_edit.setText(placeEdit.getText());

                    }
                    NavigationActivity.this.backFromSearchPlace(placeSearchLayout);

                }
                return false;
            }
        });
    }


    public void onBackPressed(View view) {
        Intent intent = new Intent(NavigationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
        sensorManager.unregisterListener(listener, accelerometerSensor);
        sensorManager.unregisterListener(listener, magneticFieldSensor);
        //mNaviHelper.pause();

    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI);
        //mNaviHelper.resume();
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
      //  mNaviHelper.quit();
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
            if (location == null || mMapView == null) {
                return;
            }
            //mapView 销毁后不在处理新接收的位置
            mCurrentAccracy = location.getRadius();
            mCurrentLantitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction((int) lastX).latitude(mCurrentLantitude)
                    .longitude(mCurrentLongitude).build();
            nlocation = location;
            mBaiduMap.setMyLocationData(locData);

            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
                return;
            }

        }

        private void navigateTo(BDLocation location) {
            if (isFirstLocate) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(18f);
                mBaiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }

        }
    }


}