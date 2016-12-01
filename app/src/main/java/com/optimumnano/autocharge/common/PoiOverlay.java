package com.optimumnano.autocharge.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.common.LogUtil;
import com.optimumnano.autocharge.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示poi的overly
 */
public class PoiOverlay extends OverlayManager implements View.OnClickListener, View.OnLongClickListener {

    private static final int MAX_POI_SIZE = 10;

    private PoiResult mPoiResult = null;

    public BaseActivity activity;
    private BaiduNavigation mBaiduNavigation;
    private LatLng mELatLng;
    private final WTMBaiduLocation mLocation;
    private LatLng mSLatLng;
    private View infoView;
    private TextView infoTitle;
    private PoiInfo mPoiInfo;
    private InfoWindow mInfoWindow;
    private String finalTitle;

    /**
     * 构造函数
     * 
     * @param baiduMap
     *            该 PoiOverlay 引用的 BaiduMap 对象
     */
    public PoiOverlay(BaiduMap baiduMap,BaseActivity activity,WTMBaiduLocation baiduLocation,BaiduNavigation baiduNavigation) {
        super(baiduMap);
        this.activity=activity;
        mLocation = baiduLocation;
        mBaiduNavigation = baiduNavigation;
        initInfoView();
    }

    /**
     * 设置POI数据
     * 
     * @param poiResult
     *            设置POI数据
     */
    public void setData(PoiResult poiResult) {
        this.mPoiResult = poiResult;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mPoiResult == null || mPoiResult.getAllPoi() == null) {
            return null;
        }
        List<OverlayOptions> markerList = new ArrayList<OverlayOptions>();
        int markerSize = 0;
        for (int i = 0; i < mPoiResult.getAllPoi().size()
                && markerSize < MAX_POI_SIZE; i++) {
            if (mPoiResult.getAllPoi().get(i).location == null) {
                continue;
            }
            markerSize++;
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            markerList.add(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_remote)).extraInfo(bundle)
                    .position(mPoiResult.getAllPoi().get(i).location));
            
        }
        return markerList;
    }

    /**
     * 获取该 PoiOverlay 的 poi数据
     * 
     * @return
     */
    public PoiResult getPoiResult() {
        return mPoiResult;
    }

    /**
     * 覆写此方法以改变默认点击行为
     * 
     * @param i
     *            被点击的poi在
     *            {@link com.baidu.mapapi.search.poi.PoiResult#getAllPoi()} 中的索引
     * @return
     */
    public boolean onPoiClick(int i) {
        List<PoiInfo> allPoi = mPoiResult.getAllPoi();
        if (allPoi != null && allPoi.get(i) != null) {
            mPoiInfo = allPoi.get(i);
            showInfoView(mPoiInfo.name);
            finalTitle=mPoiInfo.name;
        }
        return false;
    }

    private void showInfoView(String title) {
        mInfoWindow = new InfoWindow(infoView,mELatLng,-70);
        infoTitle.setText(title);
        mBaiduMap.showInfoWindow(mInfoWindow);
        activity.showShortToast("长按字条可以去这里噢");
    }

    private void initInfoView() {
        infoView = LayoutInflater.from(activity).inflate(R.layout.infowindow, null, false);
        infoView.setOnClickListener(this);
        infoView.setOnLongClickListener(this);
        infoTitle = (TextView) infoView.findViewById(R.id.infowindo_title);
    }
    @Override
    public final boolean onMarkerClick(Marker marker) {
        mELatLng = marker.getPosition();
        LogUtil.i("Marker","position="+mELatLng.latitude+"::"+mELatLng.longitude);
        if (!mOverlayList.contains(marker)) {
            String title = marker.getTitle();
            LogUtil.i("Marker","title="+title);
            finalTitle=title;
            showInfoView(title);
            return true;
        }
        if (marker.getExtraInfo() != null) {
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return true;
    }

    private void goCurrentMark(String name) {
        mLocation.start();

        AlertDialog.Builder dialog=new AlertDialog.Builder(activity);
        dialog.setTitle("是否前往:");
        dialog.setMessage(name);
        dialog.setPositiveButton("前往", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.showLoading();
                activity.setProgressTitle("正在规划路线...");
                try {
                    mBaiduNavigation.start(mSLatLng, mELatLng);
                } catch (Exception e) {
                    activity.showShortToast("导航失败,请重试");
                    activity.hideLoading();
                    e.printStackTrace();
                }
                mLocation.stopLocation();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.hideLoading();
                mLocation.stopLocation();
            }
        });

        dialog.show();

        mLocation.setLocationListner(new WTMBaiduLocation.OnLocationReceivedListner() {
            @Override
            public void onLocationReceived(BDLocation bdLocation) {
                mSLatLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

            }
        });

        mBaiduNavigation.setOnRoutePlanDoneListener(new BaiduNavigation.OnRoutePlanDoneListener() {
            @Override
            public void onRoutePlanDone() {
                activity.hideLoading();
            }
        });

    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }

    @Override
    public void onClick(View v) {

        mBaiduMap.hideInfoWindow();
    }

    @Override
    public boolean onLongClick(View v) {
        goCurrentMark(finalTitle);
        return true;
    }
}
