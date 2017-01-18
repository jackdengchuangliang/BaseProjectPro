package com.longshine.electriccars.baidu.service;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.longshine.electriccars.utils.NetUtils;
import com.longshine.electriccars.view.activity.BaseActivity;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType.BD09LL;

/**
 * Created by jack on 2016/12/22
 */

public class BaiduNavigation {


    public static List<Activity> activityList = new LinkedList<Activity>();

    private static final String APP_FOLDER_NAME = "mfwnyczc";
    private final BaseActivity mContext;
    private final RxPermissions mRxPermissions;
    private String mSDCardPath = null;
    private BNRoutePlanNode.CoordinateType mSCoordinateType;
    private BNRoutePlanNode.CoordinateType mECoordinateType;
    /**
     * 描述:默认经纬度坐标为国际经纬度坐标系
     * 可选参数:GCJ02,BD09_MC,WGS84,BD09LL;
     */
    public static final BNRoutePlanNode.CoordinateType DEFULT_ENDCOORDINATETYPE = BD09LL;
    public static final BNRoutePlanNode.CoordinateType DEFULT_STARTCOORDINATETYPE = BD09LL;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
    public static final String RESET_END_NODE = "resetEndNode";
    public static final String VOID_MODE = "voidMode";

    /**
     * 百度导航的类
     * @param context activity
     * @param sCoordinateType 起点定位的坐标系,可选参数:GCJ02,BD09_MC,WGS84,BD09LL
     */
    public BaiduNavigation(BaseActivity context, BNRoutePlanNode.CoordinateType sCoordinateType, BNRoutePlanNode.CoordinateType eCoordinateType) {
        this.mContext = context;
        mRxPermissions=new RxPermissions(context);
        mSCoordinateType = sCoordinateType;
        mECoordinateType = eCoordinateType;
        initDirs();
        if (initDirs()) {
            initNavi();
        }
        activityList.add(mContext);
        // 打开log开关
        BNOuterLogUtil.setLogSwitcher(false);
    }

    public BaiduNavigation(BaseActivity context) {
        this.mContext = context;
        mRxPermissions=new RxPermissions(context);
        mSCoordinateType = DEFULT_STARTCOORDINATETYPE;
        mECoordinateType = DEFULT_ENDCOORDINATETYPE;
        initDirs();
        if (initDirs()) {
            initNavi();
        }
        activityList.add(mContext);
        // 打开log开关
        BNOuterLogUtil.setLogSwitcher(false);
    }

    /**
     * 开始导航
     *
     * @param slongitude 起点经度
     * @param slatitude  起点纬度
     * @param elongitude 终点经度
     * @param elatitude  终点纬度
     */
    public void start(double slongitude, double slatitude, double elongitude, double elatitude,String sNodeName, String eNodeName) {
        if (!NetUtils.isConnected(mContext)){
            showToastMsg("网络连接失败,请检查网络");
            if(routePlanDoneListener!=null){
                routePlanDoneListener.onRoutePlanDone();
            }
            return;
        }

        mRxPermissions.request
                (Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(permission -> {
                    if (permission) {
                        if (BaiduNaviManager.isNaviInited()) {
                            routeplanToNavi(mSCoordinateType, mECoordinateType, slongitude, slatitude, elongitude, elatitude,sNodeName,eNodeName);
                        }else {
                            mContext.closeLoading();
                            showToastMsg("初始失败");
                        }
                    } else {
                        if(routePlanDoneListener!=null){
                            routePlanDoneListener.onRoutePlanDone();
                        }
                        showToastMsg("定位权限被拒绝了");
                    }
                });

    }
    public void start(double slongitude, double slatitude, double elongitude, double elatitude) {
        start(slongitude, slatitude,elongitude,elatitude,null,null);
    }
    /**
     * 开始导航
     *
     * @param sLatLng 起点经纬度
     * @param eLatLng 起点经纬度
     * @param sNodeName  起点节点名字
     * @param sNodeName  终点节点名字
     */
    public void start(LatLng sLatLng, LatLng eLatLng, String sNodeName, String eNodeName) {
        start(sLatLng.longitude, sLatLng.latitude, eLatLng.longitude, eLatLng.latitude,sNodeName,eNodeName);
    }
    public void start(LatLng sLatLng, LatLng eLatLng) {
        start(sLatLng.longitude, sLatLng.latitude, eLatLng.longitude, eLatLng.latitude,null,null);
    }
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
//                    showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
//                    showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
//            showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
//            showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    public void showToastMsg(final String msg) {
        mContext.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        BaiduNaviManager.getInstance().init(mContext, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                }
                else {
                    authinfo = "key校验失败";
                    showToastMsg(authinfo+"请稍后重试,或与客服联系");
                }


            }

            public void initSuccess() {
               // showToastMsg("百度导航引擎初始化成功");
                initSetting();
            }

            public void initStart() {
                //showToastMsg("百度导航引擎初始化开始");
            }

            public void initFailed() {

                showToastMsg("百度导航引擎初始化失败,请稍后重试,或与客服联系");
            }


        }, null, ttsHandler, ttsPlayStateListener);

    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType sCoType,BNRoutePlanNode.CoordinateType eCoType, double slongitude, double slatitude, double elongitude, double elatitude, String sNodeName, String eNodeName) {
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        sNode = new BNRoutePlanNode(slongitude, slatitude, sNodeName, null, sCoType);
        eNode = new BNRoutePlanNode(elongitude, elatitude, eNodeName, null, eCoType);

        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(mContext, list, 1, true, new DemoRoutePlanListener(sNode));
        }
    }

    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
			 * 设置途径点以及resetEndNode会回调该接口
			 */

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNGuideActivity")) {
                    com.longshine.electriccars.utils.Logger.i("不能从当前activity开启导航=" + mContext.getClass().getSimpleName());
                    return;
                }
            }
            Intent intent = new Intent(mContext, BNGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, mBNRoutePlanNode);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
            if(routePlanDoneListener!=null){
                routePlanDoneListener.onRoutePlanDone();
            }

        }

        @Override
        public void onRoutePlanFailed() {
            if(routePlanDoneListener!=null){
                routePlanDoneListener.onRoutePlanDone();
            }
            showToastMsg("算路失败,请重试");
        }
    }


    public void setOnRoutePlanDoneListener(OnRoutePlanDoneListener routePlanDoneListener) {
        this.routePlanDoneListener = routePlanDoneListener;
    }

    private OnRoutePlanDoneListener routePlanDoneListener;


    public interface OnRoutePlanDoneListener{
        void onRoutePlanDone();
    }

    private void initSetting() {
        // 设置是否双屏显示
        BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        // 设置导航播报模式
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // 是否开启路况
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
    }

}
