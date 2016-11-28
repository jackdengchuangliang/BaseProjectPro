package com.optimumnano.autocharge.presenter;

import com.alibaba.fastjson.JSON;
import com.lgm.baseframe.common.LogUtil;
import com.lgm.baseframe.common.http.HttpCallbackListener;
import com.lgm.baseframe.common.http.HttpUtil;
import com.lgm.baseframe.common.http.RequestUtil;
import com.optimumnano.autocharge.common.Constant;
import com.optimumnano.autocharge.models.UserInfo;
import com.optimumnano.autocharge.view.ILoginView;

import java.util.HashMap;
import java.util.Map;



/**
 * 作者：刘广茂 on 2016/11/18 14:24
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class LoginPresenter extends BasePresenter<ILoginView> {
    private final ILoginView mView;

    public LoginPresenter(ILoginView mView) {
        super(mView);
        this.mView=mView;
    }

    public void getVerificationCode() {


        Map<String,Object> params = new HashMap<>();
        params.put("mobileNumber",mView.getPhoneNumber());
        params.put("purpose","login");
        RequestUtil.url(Constant.URL_VERIFACATION_CODE)
                .injectView(getView())
                .params(getRequestParams(params))
                .requestType(HttpUtil.RequestBodyType.JSON)
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {
                            LogUtil.i("result","onRequestSuccess"+result);

                            String userState=JSON.parseObject(result).getString("userState");
                            int userResultCode= Integer.parseInt(userState);
                            if (userResultCode==0){
                              //  mView.hidePlateNumberView();
                                mView.getVerificationCodeSuccess(userResultCode);
                            }else {
                               // mView.showPlateNumberView();
                                mView.onUserBindError(userResultCode);
                            }

                    }

                    @Override
                    public boolean onRequestFailed(int resultCode, String resultMsg) {
                        mView.getVerificationCodeFailed(resultCode,resultMsg);
                        return true;
                    }
                }).post();
    }


    public void login(){
        Map<String,Object> params = new HashMap<>();
        params.put("mobileNumber", mView.getPhoneNumber());
      //  if (!mView.isBindPlate())
        params.put("plateNumber",mView.getPlateNumber());
        params.put("verificationCode",mView.getVerificationCode());
        //params.put("purpose", " login");
        //TODO 添加业务参数
        RequestUtil.url(Constant.URL_LOGIN)
                .injectView(getView())
                .params(getRequestParams(params))
                .requestType(HttpUtil.RequestBodyType.JSON)
                .callback(new HttpCallbackListener() {
                    /**
                     * 请求网络成功
                     * @param result      请求结果 status==0
                     * @param requestData 请求数据
                     */
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {
                        UserInfo userInfo = (UserInfo) JSON.parseObject(result,UserInfo.class);
                        mView.loginSuccess(userInfo);
                    }

                    /**
                     * 请求网络失败
                     * @param resultCode 错误码 status!=0
                     * @param resultMsg 错误消息
                     * @return
                     */
                    @Override
                    public boolean onRequestFailed(int resultCode, String resultMsg) {
                        mView.onRequestFailed(resultCode,resultMsg);
                        return true;
                    }
                }).post();

    }

}
