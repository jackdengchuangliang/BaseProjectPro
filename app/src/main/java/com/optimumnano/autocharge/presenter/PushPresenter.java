package com.optimumnano.autocharge.presenter;

import com.lgm.baseframe.common.http.HttpCallbackListener;
import com.lgm.baseframe.common.http.RequestUtil;
import com.optimumnano.autocharge.common.Constant;
import com.optimumnano.autocharge.view.ISubmitPushIdView;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：刘广茂 on 2016/11/22 16:00
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class PushPresenter extends BasePresenter<ISubmitPushIdView>{

    public PushPresenter(ISubmitPushIdView mView) {
        super(mView);
    }

    public void submitPushId(PushChannel channel, String pushId){
        Map<String, Object> params = new HashMap<>();
        params.put("platform",channel.getChannel());
        params.put("registerId",pushId);
        RequestUtil.url(Constant.URL_SUBMIT_PUSH_ID)
                .params(getRequestParams(params))
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {


                    }

                    @Override
                    public void onHttpStateError(String result, int statusCode) {

                    }

                    @Override
                    public void onConnectionFailed(Exception ex) {
                        super.onConnectionFailed(ex);
                    }
                })
                .post();

    }

    enum PushChannel{
        HUAWEI("huawei"),JPUSH("jpush"),GETUI("getui"),MI("mi"),XINGE("xinge");


        PushChannel(String channel) {
            this.channel = channel;
        }

        public String getChannel() {
            return channel;
        }

        private String channel;

    }

}
