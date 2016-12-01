package com.optimumnano.autocharge.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.activity.OrderManageActivity;
import com.optimumnano.autocharge.models.Order;
import com.optimumnano.autocharge.models.PushMessage;
import com.pgyersdk.crash.PgyCrashManager;

import java.util.Random;

/**
 * 作者：刘广茂 on 2016/11/26 16:07
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class OrderNotificationObserver {

    private static OrderNotificationObserver orderNotificationObserver;

    private OrderNotificationObserver() {
    }

    public static OrderNotificationObserver getInstance() {
        if (orderNotificationObserver == null) {
            orderNotificationObserver = new OrderNotificationObserver();
        }
        return orderNotificationObserver;
    }

    public OrderNotifacation getOrderNotifacation() {
        return orderNotifacation;
    }

    public void setOrderNotifacation(OrderNotifacation orderNotifacation) {
        this.orderNotifacation = orderNotifacation;
    }

    private OrderNotifacation orderNotifacation;


    public void notifyNewOrder() {
        if (orderNotifacation != null) {
            orderNotifacation.notifyNewOrder();
        }
    }

    public void showNotification(Context context, String data){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        PushMessage pushMessage;
        try {
             pushMessage = JSON.parseObject(data, PushMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if(pushMessage==null){
            return;
        }

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


    public PendingIntent getDefalutIntent(int flags, Context context){

        Intent intent = new Intent(context,OrderManageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }


    public interface OrderNotifacation {
        void notifyNewOrder();
        void doneOrder(Order order);
        void cancelOrder(Order order);
    }


    public void doneOrder (Order order){
       if(orderNotifacation!=null){
           orderNotifacation.doneOrder(order);
       }
    }

    public void cancelOrder (Order order){
        if(orderNotifacation!=null){
            orderNotifacation.cancelOrder(order);
        }
    }



}
