package com.hd.app;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.Poi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.hd.app.adapter.PoiHistoryAdapter;
import com.hd.app.adapter.PoiSuggestionAdapter;
import com.hd.app.adapter.RecyclerViewDivider;
import com.hd.app.base.BaseActivity;
import com.hd.app.util.LocationManager;
import com.hd.app.util.Utils;

import java.util.List;

//import static com.hd.app.util.NavUtil.activityList;

/**
 * Created by gaolei on 17/3/29.
 */

public class NavigationActivity extends BaseActivity implements
        OnGetSuggestionResultListener, PoiSuggestionAdapter.OnItemClickListener
        , PoiHistoryAdapter.OnHistoryItemClickListener {
    private static final String TAG = "NaigationActivity";
    LinearLayout placeSearchLayout;
    RelativeLayout title_content_layout;
    EditText placeEdit;
    TextView start_place_edit, destination_edit;
    RecyclerView recyclerviewPoi, recyclerviewPoiHistory;
    private List<SuggestionResult.SuggestionInfo> suggestionInfoList;
    private SuggestionSearch mSuggestionSearch = null;
    PoiSuggestionAdapter sugAdapter;
    boolean firstSetAdapter = true, isStartPoi = true;
    private PoiResult mPoiResult;
    String currentAddress, start_place, destination;
    LatLng startLL, endLL, tempLL;
    PoiHistoryAdapter poiHistoryAdapter;
    PoiSearch poiSearch;

    List<PoiInfo> poiInfo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setStatusBar();
        currentAddress = LocationManager.getInstance().getAddress();
        placeSearchLayout = findViewById(R.id.place_search_layout);
        title_content_layout = (RelativeLayout) findViewById(R.id.title_content_layout);
        start_place_edit = (TextView) findViewById(R.id.start_place_edit);
        destination_edit = (TextView) findViewById(R.id.destination_edit);
        placeEdit = (EditText) findViewById(R.id.place_edit);
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
        initPoiListener();

        /**;
         * 当输入关键字变化时，动态更新建议列表
         */
        placeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                Log.d("gaolei", "afterTextChanged--------------");

            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

                Log.d("gaolei", "beforeTextChanged--------------");
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                                      int arg3) {
                Log.d("gaolei", "onTextChanged--------------");

                if (cs.length() <= 0) {
                    return;
                }
                String adds = placeEdit.getText().toString().trim();
                Log.d("placeEdit",adds);
                poiSearch.searchInCity(new PoiCitySearchOption().city("福州").keyword(adds));
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch.requestSuggestion(
                        new SuggestionSearchOption()
                                .keyword(cs.toString()).city(adds)

                );
            }
        });
        placeEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Log.i("ABC","搜索操作执行:"+ placeEdit.getText());
                if (isStartPoi) {
                    start_place_edit.setText(placeEdit.getText());

                } else {
                    destination_edit.setText(placeEdit.getText());

                }
                backFromSearchPlace(placeSearchLayout);
                //providerUtil.addData(info.key,info.district, info.pt.latitude + "", info.pt.longitude + "");

            }
            return false;
        });
    }
    public void switchPoi(View view) {
        tempLL = startLL;
        startLL = endLL;
        endLL = tempLL;
        start_place = start_place_edit.getText().toString();
        destination = destination_edit.getText().toString();
        destination_edit.setText(start_place);
        start_place_edit.setText(destination);
        if (start_place_edit.getText().toString().equals(getString(R.string.input_destination))){
            start_place_edit.setText(getString(R.string.input_start_place));
        }

    }

    public void showInputStart(View view) {
        placeEdit.requestFocus();
        new Utils(this).showIMM();
        setStatusBarLayout();
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
        if (sugAdapter != null)
        {
            sugAdapter.changeData(null);
        }
        title_content_layout.setVisibility(View.VISIBLE);
        placeSearchLayout.setVisibility(View.GONE);
    }

    /**
     * 后退箭头的监听
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
        try {
            recyclerviewPoiHistory.setVisibility(View.VISIBLE);
            //在history里展示各种列表吧
            poiSearch.searchInCity(new PoiCitySearchOption().city("福州").keyword("福州"));
            Log.d(TAG, "showHistoryPOI: "+poiInfo.get(0).getName());
            List<PoiInfo> poiItems =poiInfo;
             poiHistoryAdapter = new PoiHistoryAdapter(NavigationActivity.this, poiItems);
            recyclerviewPoiHistory.setAdapter(poiHistoryAdapter);
            poiHistoryAdapter.setOnClickListener(this);
        } catch (Exception e) {
            Log.d("gaolei", e.getMessage());

        }
    }

    /**
     * 点击开始导航
     * @param view
     */

    public void startNavigation(View view) {
        if (start_place_edit.getText().toString().equals(getString(R.string.my_position))){
            startLL = LocationManager.getInstance().getCurrentLL();
        }

        if (startLL == null) {
            Toast.makeText(this, getString(R.string.please_input_start_place), Toast.LENGTH_SHORT).show();
            return;
        }
        if (endLL == null) {
            Toast.makeText(this, getString(R.string.please_input_destination), Toast.LENGTH_SHORT).show();
            return;
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
        //providerUtil.addData(info.key,info.district, info.pt.latitude + "", info.pt.longitude + "");
    }

    /**
     * 设置历史记录点击项
     * @param v
     * @param position   某项位置
     * @param poiInfo   数据类型
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
    private void initPoiListener(){
        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        poiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                poiInfo =poiResult.getAllPoi();

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
    }





    public void onBackPressed(View view) {
        Intent intent = new Intent(NavigationActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}