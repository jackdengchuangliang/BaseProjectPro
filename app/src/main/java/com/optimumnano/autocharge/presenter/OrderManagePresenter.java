package com.optimumnano.autocharge.presenter;

import com.alibaba.fastjson.JSON;
import com.lgm.baseframe.common.http.HttpCallbackListener;
import com.lgm.baseframe.common.http.HttpUtil;
import com.lgm.baseframe.common.http.RequestUtil;
import com.optimumnano.autocharge.common.Constant;
import com.optimumnano.autocharge.models.Order;
import com.optimumnano.autocharge.view.IOrderView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：刘广茂 on 2016/11/18 16:20
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class OrderManagePresenter extends BasePresenter<IOrderView> {


    public OrderManagePresenter(IOrderView mView) {
        super(mView);
    }

    public void cancelOrder(String orderId, String reason, int code) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("reason", reason);
        params.put("reasonCode", code);
        RequestUtil.url(Constant.URL_CANCEL_ORDER)
                .injectView(getView())
                .params(params)
                .requestType(HttpUtil.RequestBodyType.JSON)
                .params(getRequestParams(params))
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {

                    }

                    @Override
                    public void onHttpStateError(String result, int statusCode) {

                    }
                })
                .post();

    }

    public void changeOrderState(int state, String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("status", state);
        RequestUtil.url(Constant.URL_CHANGE_ORDER_STATE)
                .injectView(getView())
                .requestType(HttpUtil.RequestBodyType.JSON)
                .params(getRequestParams(params))
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {

                    }

                    @Override
                    public void onHttpStateError(String result, int statusCode) {

                    }
                })
                .post();

    }


    public void getOrderList(int state, int pageIndex, int pageCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageIndex", pageIndex);
        params.put("pageCount", pageCount);
        if(state!=-1){
            params.put("orderState", state);
        }
        RequestUtil.url(Constant.URL_GET_ORDERS)
                .injectView(getView())
                .params(getRequestParams(params))
                .requestType(HttpUtil.RequestBodyType.JSON)
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {
                        List<Order> orders = JSON.parseArray(result, Order.class);
                        getView().onGetOrders(orders);
                    }

                    @Override
                    public void onHttpStateError(String result, int statusCode) {

                    }

                })
                .post();

    }


}
