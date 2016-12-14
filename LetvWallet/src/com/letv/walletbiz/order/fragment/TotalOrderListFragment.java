package com.letv.walletbiz.order.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.PayWebViewActivity;
import com.letv.walletbiz.base.fragment.BaseOrderListFragment;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderRequestBean;
import com.letv.walletbiz.base.view.OrderListViewAdapter;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.order.TotalOrderConstant;
import com.letv.walletbiz.order.bean.TotalOrderBean;

import org.xutils.xmain;

import java.lang.reflect.Type;
import java.util.Date;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by changjiajie on 16-3-25.
 */
public class TotalOrderListFragment extends BaseOrderListFragment {

    private Toast mToast;
    private Toast mNoAppToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mToast = Toast.makeText(getContext(), getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT);
        mNoAppToast = Toast.makeText(getContext(), getString(R.string.total_order_detail_fail_prom), Toast.LENGTH_SHORT);
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
        OrderRequestBean requestBean = new OrderRequestBean(TotalOrderConstant.URL.TOTAL_ORDER_LIST_PATH);
        requestBean.addQueryStringParameter(MobileConstant.PARAM.TOKEN, uToken);
        return requestBean;
    }

    protected boolean isSupportHeader() {
        return true;
    }

    @Override
    protected Type getResponseType() {
        TypeToken typeToken = new TypeToken<BaseResponse<OrderListBaseBean<TotalOrderBean>>>() {
        };
        return typeToken.getType();
    }

    @Override
    public OrderListViewAdapter.BaseOrderViewHolder getViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.total_orderlist_item_v, parent, false);
        return new AllOrderListViewHolder(v);
    }

    @Override
    public OrderListViewAdapter.BaseHeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.total_orderlist_hearder, parent, false);
        return new AllOrderListHeaderViewHolder(v);
    }

    @Override
    protected RecyclerItemClickListener getRecycleritemClickListener() {
        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == RecyclerView.NO_POSITION)
                    return;
                if (view != null && view.isEnabled()) {
                    if (!NetworkHelper.isNetworkAvailable()) {
                        mToast.show();
                        return;
                    }
                    OrderListViewAdapter adapater = getOrderListAdapter();
                    if (adapater == null) {
                        return;
                    }
                    TotalOrderBean orderBean = (TotalOrderBean) adapater.getOrderItem(position);
                    if (orderBean == null) {
                        return;
                    }
                    if (TextUtils.isEmpty(orderBean.jump_param)) {
                        LogHelper.d("[%S] jump_param == null", TAG);
                        return;
                    }
                    if (orderBean.jump_param.startsWith("http")) {
                        goToWebView(orderBean);
                    } else {
                        goToThirdPartyApp(orderBean);
                    }
                }
            }
        });
        return itemClickListener;
    }

    private void goToWebView(TotalOrderBean orderBean) {
        Intent intent = new Intent(TotalOrderListFragment.this.getContext(), PayWebViewActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, orderBean.jump_param);
        startActivity(intent);
    }

    private void goToThirdPartyApp(TotalOrderBean orderBean) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(orderBean.jump_param));
            startActivity(intent);
        } catch (Exception e) {
            mNoAppToast.show();
        }
    }

    public class AllOrderListHeaderViewHolder extends OrderListViewAdapter.BaseHeaderViewHolder {

        TextView mHearderTv;

        public AllOrderListHeaderViewHolder(View v) {
            super(v);
            initV();
        }

        private void initV() {
            mHearderTv = (TextView) itemView.findViewById(R.id.orderlist_hearder_tv);
        }

        @Override
        protected void setData(OrderBaseBean orderBaseBean, int position) {
            if (orderBaseBean == null) return;
            Date data = new Date(orderBaseBean.getOrderCTime());
            if (DateUtils.isThisYear(data)) {
                if (DateUtils.isThisMonth(data)) {
                    mHearderTv.setText(R.string.total_order_list_hearder_this_month);
                } else {
                    mHearderTv.setText(DateUtils.convertPatternForDate(orderBaseBean.getOrderCTime(),
                            getContext().getString(R.string.total_order_list_hearder_this_year_month)));
                }
            } else {
                mHearderTv.setText(DateUtils.convertPatternForDate(orderBaseBean.getOrderCTime(),
                        getContext().getString(R.string.total_order_list_hearder_other_month)));
            }
        }
    }

    public class AllOrderListViewHolder extends OrderListViewAdapter.BaseOrderViewHolder {

        private TotalOrderBean mOrderBean;
        ImageView mIvIcon;
        TextView mVWeek;
        TextView mVTime;
        TextView mVProductName;
        TextView mVOrderNo;
        TextView mVPrice;
        TextView mVOrderStatus;

        public AllOrderListViewHolder(View v) {
            super(v);
            initV();
        }

        private void initV() {
            mIvIcon = (ImageView) itemView.findViewById(R.id.icon);
            mVWeek = (TextView) itemView.findViewById(R.id.tv_week);
            mVTime = (TextView) itemView.findViewById(R.id.tv_time);
            mVProductName = (TextView) itemView.findViewById(R.id.tv_product_name);
            mVOrderNo = (TextView) itemView.findViewById(R.id.tv_order_no);
            mVPrice = (TextView) itemView.findViewById(R.id.tv_price);
            mVOrderStatus = (TextView) itemView.findViewById(R.id.tv_order_status);
        }

        @Override
        protected void setData(OrderBaseBean orderBaseBean, int position) {
            this.mOrderBean = (TotalOrderBean) orderBaseBean;
            xmain.image().bind(mIvIcon, mOrderBean.icon);
            mVWeek.setText(mOrderBean.getWeek(getContext()));
            mVTime.setText(DateUtils.getDayMMDDStr(mOrderBean.getOrderCTime()));
            mVProductName.setText(mOrderBean.goods_title);
            mVOrderNo.setText(mOrderBean.getOrderNO(getContext()));
            mVPrice.setText(mOrderBean.getPrice());
            if (mOrderBean.getOrderStatus() == TotalOrderBean.STATUS_UNPAY) {
                mVOrderStatus.setText(mOrderBean.getOrderStatus(getContext()));
                mVOrderStatus.setVisibility(View.VISIBLE);
            } else {
                mVOrderStatus.setVisibility(View.GONE);
            }
        }
    }

}