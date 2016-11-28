package com.optimumnano.autocharge.receivers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.huawei.hms.support.api.push.PushReceiver;
import com.lgm.baseframe.common.LogUtil;

/**
 * 作者：刘广茂 on 2016/11/21 17:05
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class HuaweiReceiver extends PushReceiver {

    public void onToken(Context context, String token, Bundle extras) {
        String belongId = extras.getString("belongId");
        String content = "获取token和belongId成功，token = " + token +
                ",belongId = " + belongId;
        LogUtil.d("huaweipush", content); // TODO

        //
    }

    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            String content = "收到一条Push消息： " + new String(msg, "UTF-8");
            LogUtil.d("huaweipush", content); // TODO
            //showPushMessage(PushMainActivity.RECEIVE_PUSH_MSG, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
