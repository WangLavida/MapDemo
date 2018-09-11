package com.demo.mapdemo;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.RoutePara;
import com.amap.api.maps2d.overlay.WalkRouteOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

//参考https://blog.csdn.net/qq_34536167/article/details/79338243
public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    //初始化地图控制器对象
    AMap aMap;
    //声明定位回调监听器
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    private WalkRouteResult mWalkRouteResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        initUiSettings();
        initLocation();
        initMarket();
        initRouteSearch();
    }

    /**
     * 显示定位蓝点
     */
    private void initLocation() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位
        myLocationStyle.interval(1000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.i("定位", location.toString());
            }
        });
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    /**
     * 控件交互
     */
    private void initUiSettings() {
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        //是否允许显示缩放按钮
        mUiSettings.setZoomControlsEnabled(true);
        Log.i("获取缩放按钮的位置", mUiSettings.getZoomPosition() + "");
        //显示指南者
        mUiSettings.setCompassEnabled(true);
        //定位按钮
//        aMap.setLocationSource(new LocationSource() {
//            @Override
//            public void activate(OnLocationChangedListener onLocationChangedListener) {
//            }
//
//            @Override
//            public void deactivate() {
//
//            }
//        });
        //通过aMap对象设置定位数据源的监听
        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置
        mUiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示

    }

    private void initMarket() {
        LatLng latLng = new LatLng(31.8664303300, 117.2733088500);
        final Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title("城隍庙").snippet("DefaultMarker"));
        LatLng latLng1 = new LatLng(31.8560687700, 117.2519138100);
        final Marker marker1 = aMap.addMarker(new MarkerOptions().position(latLng1).title("之心城").snippet("DefaultMarker"));
        LatLng latLng2 = new LatLng(31.8403333300, 117.1705767200);
        final Marker marker2 = aMap.addMarker(new MarkerOptions().position(latLng2).title("大蜀山").snippet("DefaultMarker"));
    }

    private void initRouteSearch() {
        RouteSearch routeSearch = new RouteSearch(MapActivity.this);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
                Log.i("onBusRouteSearched", "onBusRouteSearched");
            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
                Log.i("onDriveRouteSearched", "onDriveRouteSearched");

            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
                Log.i("onWalkRouteSearched", "onWalkRouteSearched");
                aMap.clear();// 清理地图上的所有覆盖物
                if (errorCode == 1000) {
                    WalkPath walkPath = result.getPaths().get(0);
                    setWalkRoute(walkPath, result.getStartPos(), result.getTargetPos());
                    float distance = walkPath.getDistance() / 1000;
                    long duration = walkPath.getDuration() / 60;
                    Log.i("规划路线", "\n距离/公里：" + distance + "\n时间/分：" + duration);
                } else {
                    Log.e("规划路线", "onWalkRouteSearched: 路线规划失败");
                }
            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
                Log.i("onRideRouteSearched", "onRideRouteSearched");
            }
        });
        LatLonPoint point = new LatLonPoint(31.8664303300, 117.2733088500);
        LatLonPoint point1 = new LatLonPoint(31.8403333300, 117.1705767200);
        RouteSearch.FromAndTo fat = new RouteSearch.FromAndTo(point, point1);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fat);
        routeSearch.calculateWalkRouteAsyn(query);
    }
    /**
     * 步行规划线路
     */
    private void setWalkRoute(WalkPath walkPath, LatLonPoint start, LatLonPoint end) {
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this, aMap, walkPath, start, end);
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}
