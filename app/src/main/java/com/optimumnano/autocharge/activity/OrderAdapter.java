package com.optimumnano.autocharge.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lgm.baseframe.base.BaseActivity;
import com.lgm.baseframe.base.MBaseAdapter;
import com.optimumnano.autocharge.R;
import com.optimumnano.autocharge.models.Order;
import com.optimumnano.autocharge.presenter.OrderManagePresenter;
import com.optimumnano.autocharge.view.IOrderView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 作者：刘广茂 on 2016/11/19 11:03
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class OrderAdapter extends MBaseAdapter<Order> implements IOrderView {

    private BaseActivity mContext;

    private OrderManagePresenter presenter;


    public OrderAdapter(BaseActivity context) {
        mContext = context;
        presenter = new OrderManagePresenter(this);
    }


    @Override
    public void showLoading() {
        mContext.showLoading();
    }

    @Override
    public void hideLoading() {
        mContext.hideLoading();
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        mContext.onError(errorCode, errorMsg);
    }

    @Override
    public void onHttpStateError(String result, int statusCode) {

    }

    @Override
    public void onConnectionFailed(Exception ex) {

    }

    @Override
    public void onOrderCanceled(Order order) {

    }

    @Override
    public void onOrderStateChanged(Order order, int oldState, int newState) {

    }

    @Override
    public void onGetOrders(List<Order> orders) {

    }

    @Override
    protected View getExView(int position, View convertView, ViewGroup parent) {
        OrderHolder holder;
        if(convertView==null){
            convertView = View.inflate(mContext, R.layout.item_order_list, null);
            holder = new OrderHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (OrderHolder) convertView.getTag();
        }
        final Order order = getList().get(position);
        holder.address.setText(order.address);
        holder.ownerName.setText(order.owner);
        holder.ownerPhone.setText(order.ownerMobile);
        holder.curBattery.setText(order.curbattery );
        holder.plateNumberView.setText(order.plateNumber);
        holder.orderIdView.setText(order.orderId);


//        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                presenter.changeOrderState(1, order.orderId);
//            }
//        });
//        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                presenter.cancelOrder(order.orderId, "不乐意去", -1);
//            }
//        });
//        holder.doneButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                presenter.changeOrderState(5, order.orderId);
//            }
//        });

        return convertView;
    }

    class OrderHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.plate_number_view)
        TextView plateNumberView;
        @Bind(R.id.owner_name)
        TextView ownerName;
        @Bind(R.id.owner_phone)
        TextView ownerPhone;
        @Bind(R.id.address)
        TextView address;
        @Bind(R.id.cur_battery)
        TextView curBattery;
        @Bind(R.id.order_id_view)
        TextView orderIdView;
//        @Bind(R.id.cancel)
//        Button cancelButton;
//        @Bind(R.id.confirm)
//        Button confirmButton;
//        @Bind(R.id.done)
//        Button doneButton;

        public OrderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
