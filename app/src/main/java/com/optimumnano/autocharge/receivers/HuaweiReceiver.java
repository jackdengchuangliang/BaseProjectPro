package com.optimumnano.autocharge.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;

import com.huawei.hms.support.api.push.PushReceiver;
import com.lgm.baseframe.common.LogUtil;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.activity.OrderManageActivity;
import com.optimumnano.autocharge.common.OrderNotificationObserver;

import java.util.Random;

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
            OrderNotificationObserver.getInstance().showNotification(context, "huaweipush" + new String(msg, "UTF-8"));
            //showPushMessage(PushMainActivity.RECEIVE_PUSH_MSG, content);
            OrderNotificationObserver.getInstance().notifyNewOrder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
