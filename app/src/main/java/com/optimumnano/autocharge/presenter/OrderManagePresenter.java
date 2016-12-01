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

    public void cancelOrder(final Order order, String reason, int code) {
        if(order==null){
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.orderId);
        params.put("reason", reason);
        params.put("reasonCode", code);
        RequestUtil.url(Constant.URL_CANCEL_ORDER)
                .injectView(getView())
                .params(params)
                .requestType(HttpUtil.RequestBodyType.JSON)
                .params(getRequestParams(params))
                .showProgressDialog()
                .showErrorToast()
                .tag(getView().toString())
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {
                        order.orderState = 4;
                        getView().onOrderCanceled(order);
                    }

                    @Override
                    public void onHttpStateError(String result, int statusCode) {

                    }
                })
                .post();

    }

    public void changeOrderState(final int state, final Order order) {
        if(order==null){
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", order.orderId);
        params.put("status", state);
        RequestUtil.url(Constant.URL_CHANGE_ORDER_STATE)
                .injectView(getView())
                .requestType(HttpUtil.RequestBodyType.JSON)
                .params(getRequestParams(params))
                .showProgressDialog()
                .tag(getView().toString())
                .showErrorToast()
                .callback(new HttpCallbackListener() {
                    @Override
                    public void onRequestSuccess(String result, Object requestData) {
                        int oldState = order.orderState;
                        order.orderState = state;
                        if(order.orderState==5){
                            getView().onOrderStateChanged(order,oldState,state);
                        }


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
                .showProgressDialog()
                .showErrorToast()
                .tag(getView().toString())
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
