package com.longshine.electriccars.view.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.MyLocationData;
import com.longshine.data.net.TokenAuthenticator;
import com.longshine.electriccars.AppApplication;
import com.longshine.electriccars.R;
import com.longshine.electriccars.app.AppManager;
import com.longshine.electriccars.baidu.service.LocationService;
import com.longshine.electriccars.internal.di.components.ApplicationComponent;
import com.longshine.electriccars.internal.di.modules.ActivityModule;
import com.longshine.electriccars.utils.Logger;
import com.longshine.electriccars.view.widget.LoadingDialog;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * 作者：凌章 on 16/8/31 17:50
 * 邮箱：lilingzhang@longshine.com
 */

public abstract class BaseActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Toolbar.OnMenuItemClickListener onMenuItemClickListener;
    private int menuRes = INVALID_MENU;
    private static final int INVALID_MENU = -1;
    private TextView mToolbarRightTv;
    private TextView mToolbarLeftTv;
    private TextView mToolbarTitleTv;
    private CoordinatorLayout rootLayout;
    private LocationService locationService;
    private boolean isLocation = true;
    private boolean isActive;//后台
    @Inject
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getApplicationComponent().inject(this);
        setContentView(R.layout.layout_base);
        AppManager.getAppManager().addActivity(this);
        initLoading();
//        TokenAuthenticator.TOKEN = preferences.getString("token", "");
//        TokenAuthenticator.REFRESH_TOKEN = preferences.getString("refreshToken", "");
        rootLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mToolbarRightTv = (TextView) findViewById(R.id.toolbar_right);
        mToolbarLeftTv = (TextView) findViewById(R.id.toolbar_left);
        mToolbarTitleTv = (TextView) findViewById(R.id.toolbar_title);
        inject();
        processLogic(savedInstanceState);
        beforeSetActionBar();
        isGoBack(true);
        setActionBar();
        afterSettingActionBar();
        initLocationService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isActive) {
            initLocationService();
            isActive = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isAppOnForeground()) {
            isActive = false;
        }
        if (loadingDialog != null)
            closeLoading();
        if (locationService != null) {
            locationService.stop(); //停止定位服务
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationService != null)
            locationService.unregisterListener(mListener); //注销掉监听
        if (TokenAuthenticator.TOKEN != null && TokenAuthenticator.REFRESH_TOKEN != null && !TokenAuthenticator.TOKEN.equals("") && !TokenAuthenticator.REFRESH_TOKEN.equals("")) {
            preferences.edit().putString("token", TokenAuthenticator.TOKEN).apply();
            preferences.edit().putString("refreshToken", TokenAuthenticator.REFRESH_TOKEN).apply();
        }
    }

    /**
     * 标题栏配置
     */
    public abstract void setActionBar();

    /**
     * 补充操作
     */
    protected abstract void processLogic(Bundle savedInstanceState);

    public void beforeSetActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("");
        mToolbar.setEnabled(true);
    }

    private void afterSettingActionBar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            //隐藏标题栏
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationOnClickListener(view -> AppManager.getAppManager().finishActivity());
        mToolbar.setOnMenuItemClickListener(onMenuItemClickListener);
    }

    /**
     * 获取标题栏
     */
    public Toolbar toolbar() {
        return mToolbar;
    }

    /**
     * 是否开启返回按钮
     */
    public void isGoBack(boolean flag) {
        if (flag)
            mToolbar.setNavigationIcon(R.mipmap.btn_back);
    }

    public void setMenuId(@MenuRes int menuRes) {
        this.menuRes = menuRes;
    }

    public void setMenu(@MenuRes int menuRes, Toolbar.OnMenuItemClickListener onMenuItemClickListener) {
        this.menuRes = menuRes;
        setMenuClickListener(onMenuItemClickListener);
    }

    public void setMenuClickListener(Toolbar.OnMenuItemClickListener clickListener) {
        this.onMenuItemClickListener = clickListener;
    }

    public void setOnNavigationClickListener(View.OnClickListener onNavigationClickListener) {
        mToolbar.setNavigationOnClickListener(onNavigationClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menuRes != INVALID_MENU) {
            getMenuInflater().inflate(menuRes, menu);
        }
        return true;
    }

    public void setToolbarGone() {
        mToolbar.setVisibility(View.GONE);
    }

    public void setToolbarVisible() {
        mToolbar.setVisibility(View.VISIBLE);
    }

    /**
     * 右侧按钮
     */
    public TextView getRightTv() {
        return mToolbarRightTv;
    }

    public void setRight(@StringRes int textId) {
        mToolbarRightTv.setText(textId);
    }

    public void setRight(String textStr) {
        mToolbarRightTv.setText(textStr);
    }

    public void setRightImg(@DrawableRes int imgId) {
        mToolbarRightTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, imgId, 0);
    }

    public void setRightOnClickListener(View.OnClickListener listener) {
        mToolbarRightTv.setOnClickListener(listener);
    }

    /**
     * 左侧按钮
     */

    public TextView getLeftTv() {
        return mToolbarLeftTv;
    }

    public void setLeft(@StringRes int textId) {
        mToolbarLeftTv.setText(textId);
    }

    public void setLeft(String text) {
        mToolbarLeftTv.setText(text);
    }

    public void setLeftImg(@DrawableRes int imgId) {
        mToolbarLeftTv.setCompoundDrawablesWithIntrinsicBounds(imgId, 0, 0, 0);
    }

    public void setLeftOnClickListener(View.OnClickListener listener) {
        mToolbarLeftTv.setOnClickListener(listener);
    }

    /**
     * 设置标题
     */
    public TextView getActivityTitle() {
        return mToolbarTitleTv;
    }

    public void setActivityTitle(String text) {
        mToolbarTitleTv.setText(text);
    }

    public void setActivityTitleGone() {
        mToolbarTitleTv.setVisibility(View.GONE);
    }

    public void setActivityTitleVisibility() {
        mToolbarTitleTv.setVisibility(View.VISIBLE);
    }

    public void setActivityImg(@DrawableRes int imgId) {
        mToolbarTitleTv.setCompoundDrawablesWithIntrinsicBounds(imgId, 0, 0, 0);
    }

    public void setActivityTitle(@StringRes int textId) {
        mToolbarTitleTv.setText(textId);
    }

    /**
     * Adds a {@link Fragment} to this activity's layout.
     *
     * @param containerViewId The container view to where add the fragment.
     * @param fragment        The fragment to be added.
     */
    protected void addFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(containerViewId, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Adds a {@link Fragment} to this activity's layout.
     *
     * @param fragment The fragment to be added.
     */
    protected void addFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.appContentFLayout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return {@link ApplicationComponent}
     */
    protected ApplicationComponent getApplicationComponent() {
        return ((AppApplication) getApplication()).getApplicationComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     *
     * @return {@link ActivityModule}
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    /**
     * Set ButterKnife
     */
    private void inject() {
        ButterKnife.bind(this);
    }

    /**
     * finish Activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppManager.getAppManager().finishActivity();
    }

    /**
     * Snackbar
     */
    public void showSnack(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * SharedPreferences
     */
    public String getSPString(String key) {
        return preferences.getString(key, "");
    }

    public String getSPString(String key, String value) {
        return preferences.getString(key, value);
    }

    public float getSPFloat(String key) {
        return preferences.getFloat(key, 0f);
    }

    public int getSPInt(String key) {
        return preferences.getInt(key, 0);
    }

    public Set<String> getSPSetString(String key) {
        return preferences.getStringSet(key, null);
    }


    public void setSPString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public void setSPSetString(String key, Set<String> values) {
        preferences.edit().putStringSet(key, values).apply();
    }

    public void setSPFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    public void setSPInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * loadingDialog
     */
    private LoadingDialog loadingDialog;

    private void initLoading() {
        loadingDialog = new LoadingDialog(this);
    }

    public void showLoading() {
        loadingDialog.show();
    }

    public void showLoading(String msg) {
        loadingDialog.show(msg);
    }

    public void closeLoading() {
        loadingDialog.dismiss();
    }


    public void stopPoint() {
        this.isLocation = false;
        if (locationService != null && locationService.isStart()) {
            locationService.stop();
        }

    }

    private void initLocationService() {
        if (isLocation) {
            locationService = ((AppApplication) this.getApplicationContext()).locationService;
            locationService.registerListener(mListener);
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
            locationService.start();
        }
    }

    private BDLocationListener mListener = location -> {
        // TODO Auto-generated method stub
        if (null != location && location.getLocType() != BDLocation.TypeServerError) {
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            Logger.e("point:" + locData.latitude + "," + locData.longitude);
            Logger.e("city:" + location.getCity() + "," + "cityCode:" + location.getCityCode());
            preferences.edit().putString("lat", locData.latitude + "").apply();
            preferences.edit().putString("lon", locData.longitude + "").apply();
            locationService.stop();
        } else {
            Logger.e("error:" + BDLocation.TypeServerError + this.getString(R.string.error_location));
        }
    };

    //APP状态处理
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

}