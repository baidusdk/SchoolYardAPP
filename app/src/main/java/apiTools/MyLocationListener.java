package apiTools;

import android.util.Log;
import android.widget.Button;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.hd.app.R;


import static com.hd.app.MainActivity.mBaiduMap;
import static com.hd.app.MainActivity.mMapView;
import static com.hd.app.MainActivity.mXDirection;

//构造地图数据
//我们通过继承抽象类BDAbstractListener并重写其onReceieveLocation方法来获取定位数据，并将其传给MapView。
public class MyLocationListener extends BDAbstractLocationListener {
    public static BDLocation nlocation;
    private boolean isFirstLocate = true;
    //定位模式
    //是否是第一次定位
    private volatile boolean isFirstLocation = true;
    private MyLocationConfiguration myLocationConfiguration;



    @Override
    public void onReceiveLocation(BDLocation location) {


        //mapView 销毁后不在处理新接收的位置

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mXDirection).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        String s = String.valueOf(mXDirection);
        Log.d("测试定位",s);
        mBaiduMap.setMyLocationData(locData);
        nlocation = location;

        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.my_location);
        myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,mCurrentMarker);
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration);
        if (location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
            navigateTo(location);
            return;
        }
//        centerToLocation(location,250);

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

