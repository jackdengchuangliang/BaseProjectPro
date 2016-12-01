package com.optimumnano.autocharge.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.common.LogUtil;
import com.lgm.baseframe.common.Utils;
import com.lgm.baseframe.common.http.PersistentCookieStore;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.common.StringUtils;
import com.optimumnano.autocharge.models.UserInfo;
import com.optimumnano.autocharge.presenter.LoginPresenter;
import com.optimumnano.autocharge.view.ILoginView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：邓传亮 on 2016/11/18 14:24
 * <p>
 * 邮箱：dengchuanliang@optimumchina.com
 */
public class LoginActivity extends BaseActivity implements ILoginView, AdapterView.OnItemClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int GOEXIT = 1;
    private static final int LOGIN = 2;
    private static final int POPINIT = 3;
    LoginPresenter loginPresenter;
    @Bind(R.id.et_act_login_phonenum)
    EditText mEtPhonenum;
    @Bind(R.id.tv_act_login_getcode)
    TextView mTvGetcode;
    @Bind(R.id.et_act_login_code)
    EditText mEtVerCode;
    @Bind(R.id.et_act_login_platenum)
    EditText mEtPlatenum;
    @Bind(R.id.bt_act_login_login)
    Button mBtLogin;
    @Bind(R.id.ll_act_login_platemodule)
    LinearLayout mLlPlatemodule;
/*    @Bind(R.id.et_act_login_platetitle)
    EditText mEtPlatetitle;
    @Bind(R.id.iv_act_login_chose_platetitle)
    ImageView mIvChosePlatetitle;*/
    @Bind(R.id.bt_act_login_chose_platetitle)
    Button mbtChosePlatetitle;

    private PopupWindow mPw;
    private View mPopView;
    private View mRootview;
    private boolean isBindPlate = false;
    private TimerTask timerTask;
    private int mTimess;
    private Timer timer;
    private boolean isExit = false;
    private ListView mPopLV;
    private ArrayList cityList=new ArrayList();
    private String[] cityString = new String[]{"澳","川","鄂","赣","桂","贵","甘","港","沪","黑", "京","津","冀", "晋","吉", "鲁", "辽", "闽", "蒙","宁","青", "琼","陕","苏","台","皖","湘", "新", "豫","粤", "云","渝","浙","藏"};

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if(msg.what==GOEXIT)
                isExit = false;
            if (msg.what==LOGIN)
                loginPresenter.login();

            if (msg.what==POPINIT)
                mbtChosePlatetitle.setEnabled(true);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(mContext);
        persistentCookieStore.removeAll();
        mLlPlatemodule.setVisibility(View.VISIBLE);
        mEtPlatenum.setText("ATT002");
        hideTitle();
        leftView.setVisibility(View.GONE);
        loginPresenter = new LoginPresenter(this);
        mbtChosePlatetitle.setEnabled(false);
        initPopwindow();
    }

    private void initPopwindow() {
        mRootview = LayoutInflater.from(LoginActivity.this).inflate(R.layout.activity_login, null);
        mPopView = View.inflate(this, R.layout.popview, null);
        int height=0;
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        height = metric.heightPixels;   // 屏幕高度（像素）
        if(height==0){
            mPw = new PopupWindow(mPopView, LinearLayout.LayoutParams.MATCH_PARENT, 200);
        }
        mPw = new PopupWindow(mPopView, LinearLayout.LayoutParams.MATCH_PARENT, (int) (height*0.4));
        mPw.setBackgroundDrawable(new ColorDrawable());
        mPw.setFocusable(true);
        mPw.setOutsideTouchable(true);
        mPw.setAnimationStyle(R.style.PopupAnimation);

        mPopView.findViewById(R.id.popview_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPw.dismiss();
            }
        });
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < cityString.length; i++) {
                    cityList.add(cityString[i]);
                }
                if (cityList.size()>0)
                mHandler.sendEmptyMessage(POPINIT);
            }
        }.start();


        mPopLV = (ListView) mPopView.findViewById(R.id.popview_listview);
        mPopLV.setAdapter(new ArrayAdapter(this,R.layout.popview_city_item,cityList));
        mPopLV.setOnItemClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mPw.isShowing() && mPw != null) {
            mPw.dismiss();
        }
        else {
            exit();

            //finish();
        }
    }

    @Override
    public String getPhoneNumber() {
        String phoneNum = mEtPhonenum.getText().toString();
        if (TextUtils.isEmpty(phoneNum)) {
            showShortToast("电话号码为空");
            return null;
        }
        else if (!StringUtils.isPhoneNumber(phoneNum)) {
            showShortToast("电话号码格式错误");
            return null;
        }
        else {
            return phoneNum;
        }
    }

    @Override
    public String getVerificationCode() {
        String verCode = mEtVerCode.getText().toString();
        if (TextUtils.isEmpty(verCode) || verCode.length() != 6 || !StringUtils.isDigit(verCode)) {
            showShortToast("请输入正确的验证码");
            return null;
        }
        else {
            return verCode;
        }
    }

    @Override
    public String getPlateNumber() {
        String city = mbtChosePlatetitle.getText().toString();
        String plateNum = mEtPlatenum.getText().toString();
//        String regex = "^[\u4e00-\u9fa5]{1}[A-Z]{1}[A-Z_0-9]{5}$";
        String regex = "^[A-Z]{1}[A-Z_0-9]{5}$";
        if (TextUtils.isEmpty(plateNum)) {
            showShortToast("车牌号为空");
            return null;
        }
        else if (!StringUtils.isMatch(regex, plateNum)) {
            showShortToast("车牌号格式错误");
            return null;
        }
        else {
            return city+plateNum;
        }
    }

    @Override
    public void showPlateNumberView() {
        isBindPlate = false;
        mLlPlatemodule.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean hidePlateNumberView() {
        isBindPlate = true;
        mLlPlatemodule.setVisibility(View.INVISIBLE);
        return isBindPlate;
    }

    @Override
    public boolean isBindPlate() {
        return isBindPlate;
    }

    @Override
    public void clearInputs() {

    }

    @Override
    public void getVerificationCodeSuccess(int userStateCode) {
        //startTimer();
        Utils.showShortToast(this, "获取验证码成功");
        LogUtil.i(TAG, "userStateCode=" + userStateCode);
    }

    @Override
    public void getVerificationCodeFailed(int resultCode, String resultMsg) {
        stopTimer();
        if (resultCode == 10002) {
            Utils.showShortToast(this, "请求时间过短,2分钟后重试");
        }
        else {
            Utils.showShortToast(this, resultMsg);
        }
        Utils.showShortToast(this, resultMsg);
        LogUtil.i(TAG, "resultCode=" + resultCode);
    }

    @Override
    public void loginSuccess(Object object) {
        //hideLoading();  ->requestCreator已经实现了隐藏
        Utils.showShortToast(this, "登录成功");
        LogUtil.i(TAG, "result=" + ((UserInfo) object).userinfo.userName);
        startActivity(new Intent(this, OrderManageActivity.class));
        overridePendingTransition(R.anim.next_enter_anim,R.anim.next_exit_anim);
        finish();
    }

    @Override
    public void onUserBindError(int resultCode) {
        Utils.showShortToast(this, "用户未绑定车辆");
        LogUtil.i(TAG, "resultCode=" + resultCode);
    }

    /**
     * 请求业务失败,登录失败
     *
     * @param resultCode
     * @param resultMsg
     */
    @Override
    public void onRequestFailed(int resultCode, String resultMsg) {
        Utils.showShortToast(this, resultMsg);
        LogUtil.i("result", "onRequestFailed" + resultCode);
    }

    @OnClick({R.id.tv_act_login_getcode, R.id.bt_act_login_chose_platetitle, R.id.bt_act_login_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_act_login_getcode:
                if (getPhoneNumber() != null) {
                    setProgressTitle("正在获取验证码...");
                    startTimer();
                    loginPresenter.getVerificationCode();
                }
                break;
            case R.id.bt_act_login_chose_platetitle:
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mPw.showAtLocation(mRootview, Gravity.BOTTOM, 0, 0);
                }*/
                if (mPw.isShowing() && mPw != null) {
                    mPw.dismiss();
                }else {
                    mPw.showAtLocation(mRootview, Gravity.BOTTOM, 0, 0);
                    mPw.update();
                }
                break;
            case R.id.bt_act_login_login:
                startLogin();
                break;

        }
        hideInput();
    }

    private void startLogin() {
        //TODO:是否增加隐藏车牌功能
        if (isBindPlate) {
            if (getPhoneNumber() != null && getVerificationCode() != null) {
                hideLoading();
                setProgressTitle("正在登录...");
                showLoading();
                mHandler.sendEmptyMessageDelayed(LOGIN,1500);

            }
        } else {
            if (getPhoneNumber() != null && getVerificationCode() != null && getPlateNumber() != null) {
                hideLoading();
                setProgressTitle("正在登录...");
                showLoading();
                mHandler.sendEmptyMessageDelayed(LOGIN,1500);
            }
        }
    }


    private void startTimer() {
        mTimess = 120;
        mTvGetcode.setClickable(false);
        mTvGetcode.setText(mTimess + "s");
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTimess--;
                            if (mTimess <= 0) {
                                stopTimer();
                                return;
                            }
                            if (mTvGetcode != null)
                                mTvGetcode.setText(mTimess + "s");
                        }
                    });
                }
            };
        }
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(timerTask, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        mTvGetcode.setText("重新获取");
        mTvGetcode.setClickable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPw.isShowing() && mPw != null) {
            mPw.dismiss();
        }
        mPw = null;
        mPopView = null;
        mRootview = null;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        finish();
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            // 利用handler延迟发送更改状态信息
            Utils.showShortToast(this, "再按一次退出程序");
            mHandler.sendEmptyMessageDelayed(GOEXIT, 2000);
        }
        else {
            mPw = null;
            mPopView = null;
            mRootview = null;
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mbtChosePlatetitle.setText(cityString[position]);
        mPw.dismiss();
    }
}
