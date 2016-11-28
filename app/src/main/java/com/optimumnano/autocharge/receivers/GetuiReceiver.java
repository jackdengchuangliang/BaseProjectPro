package com.optimumnano.autocharge.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.igexin.sdk.PushConsts;
import com.lgm.baseframe.common.LogUtil;
import com.lgm.baseframe.common.http.HttpUtil;
import com.lgm.baseframe.common.http.RequestUtil;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.activity.OrderManageActivity;
import com.optimumnano.autocharge.common.Constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GetuiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        LogUtil.e("action",intent.getAction());
        Bundle bundle = intent.getExtras();

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:

                String cid = bundle.getString("clientid");
                LogUtil.e("clientid",cid);
                break;
            case PushConsts.GET_MSG_DATA:
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String data = new String(payload);
                    // TODO:接收处理透传（payload）数据
                    showNotification(context,data);
                }
                break;
            default:
                break;
        }
    }



    public void showNotification(Context context, String data){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setContentTitle("新工单")//设置通知栏标题
                .setContentText(data) //设置通知栏显示内容
        .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL,context)) //设置通知栏点击意图
                //  .setNumber(number) //设置通知集合的数量
                .setTicker("新工单") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                //  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON

        mNotificationManager.notify(new Random().nextInt(), mBuilder.build());

    }

    public PendingIntent getDefalutIntent(int flags,Context context){

        Intent intent = new Intent(context,OrderManageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }
}
