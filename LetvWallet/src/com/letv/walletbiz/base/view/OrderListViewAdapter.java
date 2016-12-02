package com.letv.walletbiz.base.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import com.letv.wallet.common.util.DateUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.util.OrderListDataHelper;

import java.io.Serializable;
import java.util.List;

import timehop.stickyheader.StickyRecyclerHeadersAdapter;

/**
 * Created by changjiajie on 16-1-26.
 */
public class OrderListViewAdapter extends RecyclerView.Adapter<OrderListViewAdapter.BaseOrderViewHolder> implements StickyRecyclerHeadersAdapter, SectionIndexer {
    private static final String TAG = OrderListViewAdapter.class.getSimpleName();

    private Context mContext;

    /**
     * Show view's Type
     */
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    /**
     * Data Manager
     */
    private OrderListDataHelper mDataManager;

    private BaseOrderListCallBack mOrderListCallBack;

    /**
     * Provide a suitable constructor (depends on the kind of dataset)
     * Must implement BaseOrderListCallBack, used to obtain the ViewHolder
     *
     * @param context
     * @param orderListCallBack
     */
    public OrderListViewAdapter(Context context, BaseOrderListCallBack orderListCallBack, boolean isSupportHeader) {
        super();
        this.mContext = context;
        mDataManager = new OrderListDataHelper();
        mOrderListCallBack = orderListCallBack;
        setHasStableIds(isSupportHeader);
    }

    /**
     * Provide a suitable constructor (depends on the kind of dataset)
     * Must implement BaseOrderListCallBack, used to obtain the ViewHolder
     *
     * @param context
     * @param data
     * @param orderListCallBack
     */
    public OrderListViewAdapter(Context context, OrderListBaseBean<OrderBaseBean> data, BaseOrderListCallBack orderListCallBack, boolean isSupportHeader) {
        super();
        this.mContext = context;
        mDataManager = new OrderListDataHelper();
        mOrderListCallBack = orderListCallBack;
        addData(data);
        setHasStableIds(isSupportHeader);
    }

    /**
     * Create new views (invoked by the layout manager)
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @Override
    public BaseOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_FOOTER:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_list_footer_v, parent, false);
                BaseFooterViewHolder footerVh = new BaseFooterViewHolder(v);
                return footerVh;
            default: {
                return mOrderListCallBack.getViewHolder(parent);
            }
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(BaseOrderViewHolder holder, int position) {
        if (mDataManager.isLastPage() || position + ActivityConstant.ORDER.LIST_CONSTANT.POSTION_OFFSET != getItemCount()) {
            holder.setData(mDataManager.getOrderBean(position), position);
        }
    }

    /**
     * @param position position to query
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (!mDataManager.isLastPage() && position + ActivityConstant.ORDER.LIST_CONSTANT.POSTION_OFFSET == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public long getItemId(int position) {
        return mDataManager.getOrderBean(position) == null ? -1 : mDataManager.getOrderBean(position).getRankId();
    }

    @Override
    public long getHeaderId(int position) {
        OrderBaseBean baseBean = mDataManager.getOrderBean(position);
        if (baseBean == null) {
            return -1;
        }
        return Long.valueOf(DateUtils.convertPatternForDate(baseBean.getOrderCTime(),
                mContext.getString(R.string.total_order_list_hearder_id_month)));
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return mOrderListCallBack.getHeaderViewHolder(parent);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        OrderListViewAdapter.BaseHeaderViewHolder viewHolder = (OrderListViewAdapter.BaseHeaderViewHolder) holder;
        if (viewHolder == null) return;
        viewHolder.setData(mDataManager.getOrderBean(position), position);
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mDataManager.getData() == null || mDataManager.getData().size() == 0) {
            return -1;
        }
        try {
            for (int i = 0; i < mDataManager.getData().size(); i++) {
                if (Integer.valueOf(String.valueOf(mDataManager.getOrderBean(i).getOrderCTime())) == sectionIndex) {
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (mDataManager.getData() == null || mDataManager.getData().size() == 0) {
            return -1;
        }
        try {
            return Integer.valueOf(String.valueOf(mDataManager.getOrderBean(position).getOrderCTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * Return the size of your dataset (invoked by the layout manager)
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (mDataManager.getOrderListLength() > ActivityConstant.NUMBER.ZERO) {
            return mDataManager.isLastPage()
                    ? mDataManager.getOrderListLength() : mDataManager.getOrderListLength() + ActivityConstant.ORDER.LIST_CONSTANT.POSTION_OFFSET;
        }
        return ActivityConstant.NUMBER.ZERO;
    }

    /**
     * @param position
     * @return
     */
    public OrderBaseBean getOrderItem(int position) {
        return mDataManager.getOrderBean(position);
    }

    public long getLastId() {
        return mDataManager.getLastId();
    }

    public boolean isLastPage() {
        return mDataManager.isLastPage();
    }

    public long getFirstRankId() {
        return mDataManager.getFirstRankId();
    }

    public void destory() {
        mDataManager.destory();
    }

    public List<OrderBaseBean> getData() {
        return mDataManager.getData();
    }

    public void addData(OrderListBaseBean<OrderBaseBean> data, int position) {
        if (data != null) {
            mDataManager.addData(data, position);
            notifyDataSetChanged();
        }
    }

    public void addData(OrderListBaseBean<OrderBaseBean> data) {
        addData(data, 0);
    }

    public interface BaseOrderListCallBack {
        OrderListViewAdapter.BaseOrderViewHolder getViewHolder(ViewGroup parent);

        OrderListViewAdapter.BaseHeaderViewHolder getHeaderViewHolder(ViewGroup parent);

    }

    public static abstract class BaseHeaderViewHolder extends RecyclerView.ViewHolder implements Serializable {

        public BaseHeaderViewHolder(View v) {
            super(v);
        }

        protected abstract void setData(OrderBaseBean orderBaseBean, int position);

    }

    public static abstract class BaseOrderViewHolder extends RecyclerView.ViewHolder implements Serializable {

        public BaseOrderViewHolder(View v) {
            super(v);
        }

        protected abstract void setData(OrderBaseBean orderBaseBean, int position);

    }

    protected class BaseFooterViewHolder extends BaseOrderViewHolder {

        public BaseFooterViewHolder(View view) {
            super(view);
        }

        @Override
        protected void setData(OrderBaseBean orderBaseBean, int position) {
        }

    }
}
