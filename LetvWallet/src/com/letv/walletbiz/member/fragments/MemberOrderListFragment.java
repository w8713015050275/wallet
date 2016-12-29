package com.letv.walletbiz.member.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.PriorityExecutorHelper;
import com.letv.wallet.common.util.ViewUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.fragment.BaseOrderListFragment;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderRequestBean;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.view.OrderListViewAdapter;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.activity.MemberOrderDetailActivity;
import com.letv.walletbiz.member.beans.OrderInfoBean;
import com.letv.walletbiz.member.beans.OrderListBean;
import com.letv.walletbiz.member.util.MemberUtils;

import java.lang.reflect.Type;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by zhanghuancheng on 16-11-24.
 */
public class MemberOrderListFragment extends BaseOrderListFragment{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected OrderRequestBean getRequestBean() {
        String uToken = AccountHelper.getInstance().getToken(getContext());
        OrderRequestBean requestBean = new OrderRequestBean(MemberConstant.MEMBER_GET_ORDER_LIST); //全部订单
        requestBean.addQueryStringParameter(MemberConstant.MEMBER_TOKEN, uToken);
        return requestBean;
    }

    @Override
    protected Type getResponseType() {
        TypeToken typeToken = new TypeToken<BaseResponse<OrderListBaseBean<OrderListBean.OrderBean>>>() {
        };
        return typeToken.getType();
    }

    @Override
    protected RecyclerItemClickListener getRecycleritemClickListener() {
        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (!NetworkHelper.isNetworkAvailable()) {
                    Toast.makeText(getContext(), getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT).show();
                    return;
                }
                OrderListViewAdapter adapater = getOrderListAdapter();
                if (adapater == null) {
                    return;
                }
                OrderListBean.OrderBean orderBean = (OrderListBean.OrderBean) adapater.getOrderItem(position);
                if (orderBean == null) {
                    return;
                }
                goOrderDetail(orderBean);
            }
        });
        return itemClickListener;
    }

    private void goOrderDetail(OrderListBean.OrderBean orderBean) {
        Intent intent = new Intent(MemberOrderListFragment.this.getContext(), MemberOrderDetailActivity.class);
        intent.putExtra(Constants.INFO_PARAM.ORDER_NO, orderBean.getOrderId());
        startActivity(intent);
    }

    @Override
    public OrderListViewAdapter.BaseOrderViewHolder getViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.member_orderlist_item_v, parent, false);
        return new MemberOrderListViewHolder(v);
    }

    @Override
    public OrderListViewAdapter.BaseHeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    private class MemberOrderListViewHolder extends OrderListViewAdapter.BaseOrderViewHolder {

        OrderListBean.OrderBean mOrderBean;
        private LinearLayout mIdsDescV;
        private TextView mDescLine1Tv;
        private TextView mTimeTv;
        private TextView mSNTv;
        private TextView mStatusTv;
        private TextView mPriceTv;

        public MemberOrderListViewHolder(View v) {
            super(v);
            initView();
        }

        private void initView() {
            mIdsDescV = (LinearLayout) itemView.findViewById(R.id.ids_desc_v);
            mDescLine1Tv = (TextView) itemView.findViewById(R.id.tv_order_prodcut);
            mStatusTv = (TextView) itemView.findViewById(R.id.tv_order_status);
            mSNTv = (TextView) itemView.findViewById(R.id.tv_order_sn);
            mPriceTv = (TextView) itemView.findViewById(R.id.tv_price);
            mTimeTv = (TextView) itemView.findViewById(R.id.tv_time);
        }

        @Override
        protected void setData(OrderBaseBean orderBaseBean, int position) {
            mOrderBean = (OrderListBean.OrderBean) orderBaseBean;
            if (mOrderBean.snapshot != null) {
                mDescLine1Tv.setText(mOrderBean.snapshot.name);
            }
            mStatusTv.setText(MemberUtils.getOrderStatusStringByStatus(getContext(), Integer.valueOf(mOrderBean.getOrderStatus())));
            switch (Integer.parseInt(mOrderBean.getOrderStatus())) {
                case MemberConstant.ORDER_STATUS.CREATED:
                    mStatusTv.setTextColor(getResources().getColor(R.color.member_color_to_pay));
                    break;
                default:
                    mStatusTv.setTextColor(getResources().getColor(R.color.member_color_sub_text));
            }
            mSNTv.setText(mOrderBean.order_sn);
            mPriceTv.setText(mOrderBean.getReal_price());
            mTimeTv.setText(DateUtils.getDayStr(Long.valueOf(mOrderBean.order_ctime) * 1000));
        }
    }
}
