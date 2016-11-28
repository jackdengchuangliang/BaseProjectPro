package com.optimumnano.autocharge.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;
import com.igexin.sdk.PushManager;
import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.common.Utils;
import com.lgm.baseframe.common.http.HttpUtil;
import com.lgm.baseframe.common.http.RequestUtil;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.common.Constant;
import com.optimumnano.autocharge.presenter.LogoutPresenter;
import com.optimumnano.autocharge.view.ILogoutView;
import com.optimumnano.autocharge.widget.CustomViewPager;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import me.amiee.nicetab.NiceTabLayout;



/**
 * 作者：刘广茂 on 2016/11/18 16:38
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class OrderManageActivity extends BaseActivity implements ILogoutView, HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener {


    @Bind(R.id.order_pager)
    CustomViewPager orderPager;
    @Bind(R.id.sliding_tabs)
    NiceTabLayout niceTabLayout;
    LogoutPresenter logoutPresenter;


    private OrderListFragment.OrderState[] mItems = new OrderListFragment.OrderState[]{
            OrderListFragment.OrderState.UNDONE,
            OrderListFragment.OrderState.DONE,
            OrderListFragment.OrderState.CANCELED};
    private boolean isExit = false;
    private HuaweiApiClient huaweiApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manage);
        setTitle("工单管理");
        leftView.setVisibility(View.GONE);
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
        orderPager.setAdapter(mPagerAdapter);
        niceTabLayout.setViewPager(orderPager);
        logoutPresenter = new LogoutPresenter(this);
        initHuaweiPush();
        String clientid = PushManager.getInstance().getClientid(this);
        if(!TextUtils.isEmpty(clientid)){
            submitPushID("getui",clientid);
        }


    }

    private void initHuaweiPush() {
        huaweiApiClient = new HuaweiApiClient.Builder(this).addApi(HuaweiPush.PUSH_API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        huaweiApiClient.connect();
    }



    private void submitPushID(String platform,String cid) {
        Map<String,Object> params = new HashMap<>();
        params.put("registerId",cid);
        params.put("platform",platform);
        Map<String,Object> rootParams = new HashMap<>();
        rootParams.put("params",params);
        RequestUtil.url(Constant.URL_SUBMIT_PUSH_ID)
                .params(rootParams)
                .requestType(HttpUtil.RequestBodyType.JSON)
                .post();
    }

    @OnClick(R.id.logout)
    public void onClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("退出登陆后将不会再接收到新的工单，确定退出？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutPresenter.logout();
                Intent intent = new Intent(mContext,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();
    }

    @Override
    public void onConnected() {
        getToken();


    }

    private void getToken() {
        if (!isConnected()) {
            return;
        }

        // 异步调用方式
        PendingResult<TokenResult> tokenResult = HuaweiPush.HuaweiPushApi.getToken(huaweiApiClient);
        tokenResult.setResultCallback(new ResultCallback<TokenResult>() {

            @Override
            public void onResult(TokenResult result) {
                String token = result.getTokenRes().getToken();
                if(!TextUtils.isEmpty(token)){
                    submitPushID("huawei", token);
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public  boolean isConnected() {
        if (huaweiApiClient != null && huaweiApiClient.isConnected()) {
            return true;
        } else {
            return false;
        }
    }



    private static class PagerAdapter extends FragmentStatePagerAdapter {

        OrderListFragment[] mFragments;
        OrderListFragment.OrderState[] mTabs;

        public PagerAdapter(FragmentManager fm, OrderListFragment.OrderState[] tabs) {
            super(fm);
            mTabs = tabs;
            if (tabs == null) {
                return;
            }
            mFragments = new OrderListFragment[mTabs.length];
            for (int i = 0; i < mTabs.length; i++) {
                mFragments[i] = new OrderListFragment();
                mFragments[i].setOrderState(tabs[i]);
            }

            for (int i = 0; i < mTabs.length; i++) {
                System.out.println(mFragments[i].getOrderState().toString());
            }
        }


        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs[position].getStatusStr();
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }


    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            // 利用handler延迟发送更改状态信息
            Utils.showShortToast(this, "再按一次退出程序");
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };


}
