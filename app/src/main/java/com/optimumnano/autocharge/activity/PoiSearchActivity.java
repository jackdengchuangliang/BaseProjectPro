package com.optimumnano.autocharge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.common.LogUtil;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.common.BaiduNavigation;
import com.optimumnano.autocharge.common.PoiOverlay;
import com.optimumnano.autocharge.common.WTMBaiduLocation;

import java.util.ArrayList;
import java.util.List;


/**
 * poi搜索功能
 */
public class PoiSearchActivity extends BaseActivity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener, View.OnLongClickListener {

    private static final String TAG = PoiSearchActivity.class.getSimpleName();
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private BaiduMap mBaiduMap = null;
    private List<String> suggest;
    /**
     * 搜索城市,关键字输入窗口
     */
    private EditText editCity = null;
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int loadIndex = 0;

    private WTMBaiduLocation mLocation;
    private LatLng mCurLocation;
    private BaiduNavigation mBaiduNavigation;
    private PoiOverlay mOverlay;
    private GeoCoder mSearch;
    private String markTitle;
    private LatLng markLocation;
    private boolean locationFlag=true;
    private String mCurCity;
    private LatLng mResultLocation=null;
    private boolean isSearched;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisearch);
        setTitle("搜索");
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToOrderAct();
            }
        });

        initSearchModule();

        editCity = (EditText) findViewById(R.id.city);
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        keyWorldsView.setThreshold(2);
        mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager()
                                                   .findFragmentById(R.id.map))).getBaiduMap();
        mBaiduMap.setMyLocationEnabled(true);
        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(mTextWatcher);
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                markTitle="未知位置";
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                markLocation=latLng;
            }
        });
        initGeoNai();
        mOverlay = new MyPoiOverlay(mBaiduMap,mLocation,mBaiduNavigation);
        mBaiduMap.setOnMarkerClickListener(mOverlay);
    }

    private void goToOrderAct() {
        startActivity(new Intent(PoiSearchActivity.this,OrderManageActivity.class));
        overridePendingTransition(R.anim.next_exit_anim,R.anim.next_enter_anim);
        finish();
    }

    private void initSearchModule() {
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        //初始化定位模块
        mLocation = new WTMBaiduLocation(this);
        mLocation.start();
        mLocation.setLocationListner(new WTMBaiduLocation.OnLocationReceivedListner() {
            @Override
            public void onLocationReceived(BDLocation bdLocation) {
                mCurCity=bdLocation.getCity();
                editCity.setText(mCurCity);
                mCurLocation=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                if (locationFlag)
                showMap(bdLocation,mBaiduMap);
                locationFlag=false;
                mLocation.stopLocation();
            }
        });
        //初始化导航模块
        mBaiduNavigation=  new BaiduNavigation(PoiSearchActivity.this, BNRoutePlanNode.CoordinateType.BD09LL, BNRoutePlanNode.CoordinateType.BD09LL);
        mBaiduNavigation.setOnRoutePlanDoneListener(new BaiduNavigation.OnRoutePlanDoneListener() {
            @Override
            public void onRoutePlanDone() {
                hideLoading();
            }
        });
    }


    private void initGeoNai() {
        mSearch = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    showShortToast("抱歉,查询不到结果,请重试");
                    return;
                }
                /*LatLng location = result.getLocation();
                mBaiduNavigation.start(mCurLocation,location);*/
                LogUtil.i(TAG, "sLatitude=" + mCurLocation.latitude + "   mCurLocation=" + mCurLocation.longitude);
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    LogUtil.i(TAG, "反地理查询无结果");
                }
                if (result.getAddress()!=null)
                markTitle = result.getAddress();
                keyWorldsView.setText(markTitle);
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                OverlayOptions option=new MarkerOptions().position(markLocation)
                        .icon(bitmap).title(markTitle);
                mBaiduMap.clear();
                mBaiduMap.addOverlay(option);
                mBaiduMap.setOnMarkerClickListener(mOverlay);
                LogUtil.i(TAG, "address=" + markTitle);
            }
        };
        mSearch.setOnGetGeoCodeResultListener(listener);
    }

    protected void showMap(BDLocation bdLocation, BaiduMap baiduMap) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        baiduMap.setMyLocationData(locData);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        baiduMap.setMyLocationConfigeration(config);

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude()));
        builder.zoom(18);
        //移动当前位置到屏幕中心
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(builder.build());
        baiduMap.setMapStatus(mapStatusUpdate);


    }

    private TextWatcher mTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before, int count) {
            if (cs.length() <= 0) {
                return;
            }

            /**
             * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
             */
            mSuggestionSearch
                    .requestSuggestion((new SuggestionSearchOption())
                            .keyword(cs.toString()).city(editCity.getText().toString()));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        keyWorldsView.removeTextChangedListener(mTextWatcher);
        mSuggestionSearch.destroy();
        mLocation.onLocationDestroy();
        mLocation=null;
        super.onDestroy();
    }

    /**
     * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
     * @param result
     */
    public void onGetPoiResult(PoiResult result) {
        hideLoading();
        LogUtil.i(TAG,result.error.toString());
        if (result.error ==SearchResult.ERRORNO.NETWORK_ERROR||result.error== SearchResult.ERRORNO.NETWORK_TIME_OUT ) {
            showShortToast( "网络出错");
            return;
        }

        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            showShortToast( "抱歉，未找到结果");
            return;
        }

        if (result.error == SearchResult.ERRORNO.ST_EN_TOO_NEAR) {
            showShortToast( "起终点太近");
            return;
        }
        if (result.error == SearchResult.ERRORNO.PERMISSION_UNFINISHED) {
            showShortToast( "请求出错");
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            mOverlay.setData(result);
            mResultLocation = result.getAllPoi().get(0).location;
            mOverlay.addToMap();
            mOverlay.zoomToSpan();
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {//关键词有歧义

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            showShortToast(strInfo);
        }
    }

    /**
     * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
     * @param result
     */
    public void onGetPoiDetailResult(PoiDetailResult result) {
        hideLoading();
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            showShortToast( "抱歉，未找到结果");
        } else {
            //showShortToast( result.getName() + ": " + result.getAddress());
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     * @param res
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }    
        suggest = new ArrayList<String>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }
        sugAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, suggest);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }


    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap,WTMBaiduLocation baiduLocation,BaiduNavigation baiduNavigation) {
            super(baiduMap,PoiSearchActivity.this,baiduLocation,baiduNavigation);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);

            return true;
        }
    }

    /**
     * 一键导航

     */
    public void onekeyNavigation(View v) {
        mLocation.start();
        String keystr = keyWorldsView.getText().toString();
        if (TextUtils.isEmpty(keystr)){
            showShortToast("请输入要查询的地址");
            return;
        }
        if (mResultLocation==null||!isSearched){
            showShortToast("请先城市内搜索,再导航");
            return;
        }

        hideInput();
        showLoading();
        setProgressTitle("正在规划路线...");
        mBaiduNavigation.start(mCurLocation,mResultLocation);
        mLocation.stopLocation();
       /* mSearch.geocode(new GeoCodeOption()
                .city(mCurCity)
                .address(keystr));*/
    }


    /**
     * 响应城市内搜索按钮点击事件
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        isSearched=true;
        mLocation.start();
        hideInput();
        setProgressTitle("正在查询...");
        String citystr = editCity.getText().toString();
        if (TextUtils.isEmpty(citystr)){
            showShortToast("请输入要查询的城市");
            return;
        }
        String keystr = keyWorldsView.getText().toString();
        if (TextUtils.isEmpty(keystr)){
            showShortToast("请输入要查询的地址");
            return;
        }
        showLoading();
        try {
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city(citystr).keyword(keystr).pageNum(loadIndex));
            mLocation.stopLocation();
        } catch (Exception e) {
            showShortToast("搜索失败,请先定位或换其他搜索关键字");
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        goToOrderAct();
    }
}
