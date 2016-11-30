package com.optimumnano.autocharge;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiAddrInfo;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.common.LogUtil;
import com.optimumnano.autocharge.common.BaiduNavigation;
import com.optimumnano.autocharge.common.WTMBaiduLocation;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements OnGetSuggestionResultListener {

    private static final String TAG =MainActivity.class.getSimpleName() ;
    @Bind(R.id.j)
    TextView mJ;
    @Bind(R.id.w)
    TextView mW;
    private BaiduNavigation mBaiduNavigation;
    private LatLng sLatLng;
    private GeoCoder mSearch;
    private PoiSearch mPoiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        ButterKnife.bind(this);

        WTMBaiduLocation location=new WTMBaiduLocation(this);
        mBaiduNavigation = new BaiduNavigation(this, BNRoutePlanNode.CoordinateType.BD09LL, BNRoutePlanNode.CoordinateType.BD09LL);

        mBaiduNavigation.setOnRoutePlanDoneListener(new BaiduNavigation.OnRoutePlanDoneListener() {
            @Override
            public void onRoutePlanDone() {
                hideLoading();
            }
        });
        location.start();

        initGeoNai();
        initPoiSearch();


        location.setLocationListner(new WTMBaiduLocation.OnLocationReceivedListner() {
            @Override
            public void onLocationReceived(BDLocation bdLocation) {

                sLatLng=new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

                setRightCustomBtn("开始导航", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setProgressTitle("正规划划路线...");
                        showLoading();

                        //startLatLngNai();
                        startGeoNai();
                        //startPoiSearch();


                    }
                });
            }
        });


    }

    private void startPoiSearch() {
        String city = mJ.getText().toString();
        String keyword=mW.getText().toString();
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(city)
                .keyword(keyword)
                .pageNum(10));

    }

    private void initPoiSearch() {
        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){

            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                List<PoiAddrInfo> addrList = poiResult.getAllAddr();
                if (addrList!=null){
                    for (PoiAddrInfo poi: addrList) {
                        LogUtil.i(TAG,""+poi.name+poi.address+poi.location.latitude);
                    }
                    LogUtil.i(TAG,"size"+addrList.size());
                }

                List<CityInfo> suggestCityList = poiResult.getSuggestCityList();
                    if (suggestCityList!=null){
                    for (CityInfo poi: suggestCityList) {
                        LogUtil.i(TAG,""+poi.city);
                    }
                }
                LogUtil.i(TAG,"test1....");
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                LogUtil.i(TAG,"test2....");
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                LogUtil.i(TAG,"test3....");
            }

        };
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);



    }

    private void startGeoNai() {
        String cicy = mJ.getText().toString();
        String address=mW.getText().toString();
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(sLatLng));
       /* mSearch.geocode(new GeoCodeOption()
                .city(cicy)
                .address(address));*/
    }

    private void initGeoNai() {
        mSearch = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    showShortToast("无结果");
                }
                LatLng location1 = result.getLocation();
                LogUtil.i(TAG,"Latitude==="+ location1.latitude+"   Longitude===="+location1.longitude);


                mBaiduNavigation.start(sLatLng, location1);
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    showShortToast("反地理查询无结果");
                }
                String address = result.getAddress();
                LogUtil.i(TAG,"address="+ address);
            }
        };
        mSearch.setOnGetGeoCodeResultListener(listener);

    }

    private void startLatLngNai() {

        setProgressTitle("正在计划路线...");
        showLoading();

        double jd= 0;
        double wd= 0;
        try {
            String j = mJ.getText().toString();
            jd = Double.parseDouble(j);
            String w = mW.getText().toString();
            wd = Double.parseDouble(w);
            LatLng eLatLng = new LatLng(wd, jd);
            mBaiduNavigation.start(sLatLng, eLatLng);
        } catch (NumberFormatException e) {
            showShortToast("请输入终点经纬度");
            e.printStackTrace();
        }

    }


    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }

        ArrayList<String> suggest = new ArrayList<String>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
                LogUtil.i(TAG,"key="+info.key);
            }
        }
    }
}
