package com.optimumnano.autocharge.models;

/**
 * 作者：刘广茂 on 2016/11/18 16:15
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class Order {
    public static final int STATE_NEW = 0;
    public static final int STATE_CONFIRMED = 1;
    public static final int STATE_REQUEST_CANCEL = 2;
    public static final int STATE_CANCEL_REFUSES = 3;
    public static final int STATE_CANCELED = 4;
    public static final int STATE_DONE = 5;

    public String orderId;//ID
    public int orderState;//工单状态
    public String plateNumber;//物流车车牌号
    public String owner;//物流车主姓名
    public String curbattery;//物流车当前电量
    public String ownerMobile;//物流车联系方式
    public String address;//物流车最后一次定位地址
    public double latitude;//物流车最后一次定位纬度
    public double longitude;//物流车最后一次定位经度

    @Override
    public boolean equals(Object obj) {
        if(obj==null){
            return false;
        }
        if(obj==this){
            return true;
        }
        if(obj instanceof Order){
            Order order = (Order) obj;
            return hashCode()==order.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return orderId == null ? 0 : orderId.hashCode() << 1;
    }
}
