package com.optimumnano.autocharge.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.base.BaseFragment;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.models.Order;
import com.optimumnano.autocharge.presenter.OrderManagePresenter;
import com.optimumnano.autocharge.view.IOrderView;
import com.optimumnano.pulltorefreshlistview.RefreshListView;

import java.util.List;

import butterknife.Bind;

/**
 * 作者：刘广茂 on 2016/11/19 12:01
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class OrderListFragment extends BaseFragment implements IOrderView {


    @Bind(R.id.order_list)
    RefreshListView orderList;

    OrderManagePresenter orderManagePresenter;
    private OrderAdapter adapter;

    private static final int PAGE_SIZE = 10;

    private  int pageIndex = 1;

    enum OrderState{
        UNDONE(-2,"未完成"),
        DONE(5,"已完成"),
        CANCELED(4,"已取消");

        public int getStatusValue() {
            return statusValue;
        }

        private int statusValue;

        public String getStatusStr() {
            return statusStr;
        }

        private String statusStr;
        OrderState(int i, String string) {
            statusValue = i;
            statusStr = string;
        }

        @Override
        public String toString() {
            return "["+getStatusValue()+""+getStatusStr()+"]";
        }
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }

    private OrderState orderState = OrderState.UNDONE;



    @Override
    protected int getLayoutId() {

        return R.layout.fragment_order_manage;
    }



    private boolean pull = false;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(orderManagePresenter==null){
            orderManagePresenter = new OrderManagePresenter(this);
            adapter = new OrderAdapter((BaseActivity) getActivity());
            orderList.setAdapter(adapter);
            orderManagePresenter.getOrderList(orderState.getStatusValue(),pageIndex, 20);

            orderList.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
                @Override
                public void onPullDownRefresh(ListView v) {
                    pageIndex=1;
                    pull = true;
                    orderManagePresenter.getOrderList(orderState.getStatusValue(),pageIndex, PAGE_SIZE);
                }

                @Override
                public void onRaiseRefresh(ListView v) {
                    pageIndex++;
                    pull = false;
                    orderManagePresenter.getOrderList(orderState.getStatusValue(),pageIndex, PAGE_SIZE);
                }
            });
            orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if(i==0){
                        return;
                    }
                    if(orderState!=OrderState.UNDONE){
                        return;
                    }
                    int position = i-1;
                    Order order = adapter.getList().get(position);
                showChooseDialog(order);


//
//


                }
            });
        }
    }

    private void showChooseDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(R.array.order_oprations_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }


    @Override
    public void onOrderCanceled(Order order) {

    }

    @Override
    public void onOrderStateChanged(Order order, int oldState, int newState) {

    }

    @Override
    public void onGetOrders(List<Order> orders) {
        if(pageIndex==1){
            adapter.clear();
        }
        adapter.appendToList(orders);
        if(orders==null||orders.size()<PAGE_SIZE&&!pull){
//            if(isVisible()){
//                showShortToast("后面没有了");
//            }
        }


    }


    @Override
    public void onConnectionFailed(Exception ex) {

    }

    @Override
    public void showLoading() {
        super.showLoading();
        orderList.pullRefreshComplete();
        orderList.raiseRefreshComplete();
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        orderList.pullRefreshComplete();
        orderList.raiseRefreshComplete();
    }

    @Override
    public void onHttpStateError(String result, int statusCode) {

    }
}
