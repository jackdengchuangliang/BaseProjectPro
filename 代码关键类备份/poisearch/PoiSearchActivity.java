package com.optimumnano.autocharge.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
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

import static com.optimumnano.autocharge.R.id.address;


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
     * 搜索关键字输入窗口
     */
    private EditText editCity = null;
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int loadIndex = 0;


    int radius = 1000;
    LatLng southwest = new LatLng( 39.92235, 116.380338 );
    LatLng northeast = new LatLng( 39.947246, 116.414977);
    LatLngBounds searchbound = new LatLngBounds.Builder().include(southwest).include(northeast).build();

    int searchType = 0;  // 搜索的类型，在显示时区分
    private WTMBaiduLocation mLocation;
    private LatLng mCurLocation;
    private LatLng center ;
    private boolean isOpenBoundSearch=false;
    private BaiduNavigation mBaiduNavigation;
    private View infoView;
    private TextView infoTitle;
    private PoiOverlay mOverlay;
    private GeoCoder mSearch;
    private String markTitle;
    private LatLng markLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisearch);
        setTitle("搜索");
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
                editCity.setText(bdLocation.getCity());
                mCurLocation=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                showMap(bdLocation,mBaiduMap);
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
        Button boundSearch = (Button) findViewById(R.id.searchBound);
        boundSearch.setText("定位");

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


    private void initGeoNai() {
        mSearch = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    showShortToast("无结果");
                }
                LatLng location1 = result.getLocation();
                LogUtil.i(TAG, "Latitude===" + location1.latitude + "   Longitude====" + location1.longitude);
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    showShortToast("反地理查询无结果");
                }

                markTitle = result.getAddress();

                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                OverlayOptions option=new MarkerOptions().position(markLocation)
                        .icon(bitmap).title(markTitle);
                mBaiduMap.clear();
                mBaiduMap.addOverlay(option);
                mBaiduMap.setOnMarkerClickListener(mOverlay);
                LogUtil.i(TAG, "address=" + address);
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
     * 响应城市内搜索按钮点击事件
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        //mLocation.start();
        hideInput();
        setProgressTitle("正在查询...");
        searchType = 1;
        String citystr = editCity.getText().toString();
        if (TextUtils.isEmpty(citystr)){
            showShortToast("请输入要查询的城市");
            return;
        }
        String keystr = keyWorldsView.getText().toString();
        if (TextUtils.isEmpty(keystr)){
            showShortToast("请输入要查询的内容");
            return;
        }
        showLoading();
        try {
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city(citystr).keyword(keystr).pageNum(loadIndex));
        } catch (Exception e) {
            showShortToast("搜索失败,请先定位或换其他搜索关键字");
            e.printStackTrace();
        }
    }

    /**
     * 响应周边搜索按钮点击事件
     *
     * @param v
     */
    public void  searchNearbyProcess(View v) {
        //mLocation.start();
        setProgressTitle("正在查询...");

        searchType = 2;
        if (TextUtils.isEmpty(keyWorldsView.getText().toString())){
            showShortToast("请输入要查询的内容");
            return;
        }
        if (mCurLocation!=null){
            center= mCurLocation;
        }else {
            showShortToast("请先按定位按钮");
            return;
        }
        showLoading();
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.keyword(keyWorldsView.getText()
                .toString()).sortType(PoiSortType.distance_from_near_to_far).location(center)
                .radius(radius).pageNum(loadIndex);
        try {
            mPoiSearch.searchNearby(nearbySearchOption);
        } catch (Exception e) {
            showShortToast("搜索失败,请先定位或换其他搜索关键字");
            e.printStackTrace();
        }

    }

    /**
     * 响应区域搜索按钮点击事件
     *
     * @param v
     */
    public void searchBoundProcess(View v) {

        if (!isOpenBoundSearch){
//            showShortToast("暂不开通该功能,请联系客服");
            mBaiduMap.clear();
            mBaiduMap.setMyLocationEnabled(true);
            mLocation.start();
            return;
        }

        searchType = 3;
        mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(searchbound)
                .keyword(keyWorldsView.getText().toString()));

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
            mOverlay.addToMap();
            mOverlay.zoomToSpan();

            switch( searchType ) {
                case 2:
                    //TODO:显示周边搜索的范围
                    //showNearbyArea(center, radius);
                    break;
                case 3:
                    //TODO:显示区域搜索的范围
                    //showBound(searchbound);
                    break;
                default:
                    break;
            }

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
     * 对周边检索的范围进行绘制
     * @param center
     * @param radius
     */
    public void showNearbyArea(LatLng center, int radius) {
        mBaiduMap.setMyLocationEnabled(false);
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor( 0x33000000 )
                .center(center).stroke(new Stroke(5, 0x550000FF ))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    /**
     * 对区域检索的范围进行绘制
     * @param bounds
     */
    public void showBound( LatLngBounds bounds) {
        BitmapDescriptor bdGround = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_geo);

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);

        bdGround.recycle();
    }

}
