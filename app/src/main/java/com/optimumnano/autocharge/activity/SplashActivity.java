package com.optimumnano.autocharge.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.githang.statusbar.StatusBarCompat;
import com.igexin.sdk.PushManager;
import com.lgm.baseframe.common.http.PersistentCookieStore;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.common.Constant;
import com.optimumnano.autocharge.models.Order;
import com.optimumnano.autocharge.view.IOrderView;
import com.pgyersdk.update.PgyUpdateManager;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * 作者：刘广茂 on 2016/11/22 08:40
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class SplashActivity extends AppCompatActivity implements IOrderView {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hideTitle();
      //  StatusBarCompat.setStatusBarColor(this, R.color.main_bg_color, false);
        PushManager.getInstance().initialize(this.getApplicationContext());
        setContentView(R.layout.activity_splash);
        PgyUpdateManager.register(this);
        hideBottomUIMenu();
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                PersistentCookieStore persistentCookieStore = new PersistentCookieStore(SplashActivity.this);
                HttpUrl.Builder builder = new HttpUrl.Builder();
                builder.host(Constant.HOST);
                builder.scheme("http");
                List<Cookie> cookies = persistentCookieStore.get(builder.build());
                boolean bool = false;
                for (Cookie item : cookies) {
                    if ("SessionKey".equals(item.name()) && !TextUtils.isEmpty(item.value())) {
                        bool = true;
                        break;
                    }
                }
                if (bool) {
                    startActivity(new Intent(SplashActivity.this, OrderManageActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
                super.handleMessage(msg);
            }
        };
        handler.sendEmptyMessageDelayed(0, 2000);

//        orderManagePresenter orderManagePresenter = new orderManagePresenter(this);
//        orderManagePresenter.getOrderList(-1,0,20);


    }

    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }


    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onError(int errorCode, String errorMsg) {

    }

    @Override
    public void onHttpStateError(String result, int statusCode) {

    }

    @Override
    public void onConnectionFailed(Exception ex) {

    }

    @Override
    public void onOrderCanceled(Order order) {

    }

    @Override
    public void onOrderStateChanged(Order order, int oldState, int newState) {

    }

    @Override
    public void onGetOrders(List<Order> orders) {

    }
}
