package com.letv.walletbiz.base.util;

import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by changjiajie on 16-1-21.
 */
public class OrderListDataHelper<T> {


    /**
     * 默认Limit Size
     */
    private static int DEFAULT_LIMIT = 20;
    /**
     * 默认最新的一条记录的id
     */
    private long mLastId = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
    /**
     * 查询需要的条数
     */
    private int mLimit = DEFAULT_LIMIT;
    /**
     * 1：标示向后取数据，-1：标识向前取数据
     */
    private int mModel = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_MOD;
    /**
     * Orderlist Data
     */
    private List<OrderBaseBean> mData;
    /**
     * end page identification
     */
    private boolean mIsLastPage = false;

    public OrderListDataHelper() {
        mData = new ArrayList<OrderBaseBean>();
    }

    public OrderBaseBean getOrderBean(int position) {
        return mData != null && mData.size() > position ? mData.get(position) : null;

    }

    public List<OrderBaseBean> getData() {
        return mData;
    }

    public void resetData(List<OrderBaseBean> data) {
        this.mData = data;
    }

    public int getOrderListLength() {
        return mData != null ? mData.size() : ActivityConstant.NUMBER.ZERO;
    }

    /**
     * Set Data
     *
     * @param data
     */
    private void setData(OrderListBaseBean<T> data) {
        List<OrderBaseBean> list = (List<OrderBaseBean>) Arrays.asList(data.list);
        int listCount = list.size();
        if (listCount < mLimit) {
            mIsLastPage = true;
        } else {
            mIsLastPage = false;
        }
        mData.clear();
        mData.addAll(list);
        mLastId = getLastRankId();
        mModel = data.model;
        mLimit = data.limit;
    }

    /**
     * Add Data
     *
     * @param data
     */
    public void addData(OrderListBaseBean<T> data, int position) {
        if (data.list != null && data.list.length > 0 && mData != null) {
            switch (data.model) {
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE:
                    setData(data);
                    break;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE:
                    appendData(data);
                    break;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_REFRESH:
                    insertData(data);
                    break;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_PART_UPDATE:
                    updatePartData(position, data);
                    break;
            }
        }
    }

    /**
     * Add Data
     *
     * @param data
     */
    private void appendData(OrderListBaseBean<T> data) {
        int listCount = data.list.length;
        if (listCount < mLimit) {
            mIsLastPage = true;
        } else {
            mIsLastPage = false;
        }
        mData.addAll((List<OrderBaseBean>) Arrays.asList(data.list));
        mLastId = getLastRankId();
        mModel = data.model;
        mLimit = data.limit;
    }

    /**
     * Add Data
     *
     * @param data
     */
    private void insertData(OrderListBaseBean<T> data) {
        if (data.list.length <= 0)
            return;
        mData.addAll(0, (List<OrderBaseBean>) Arrays.asList(data.list));
    }

    /**
     * Add Data
     *
     * @param data
     */
    public void insertData(int position, OrderBaseBean data) {
        if (data == null)
            return;
        mData.set(position, data);
    }

    /**
     * update Data
     * Position is the first position of the replacement part.
     *
     * @param data
     */
    private void updatePartData(int position, OrderListBaseBean<T> data) {
        int size = mData.size();
        for (int i = 0; i < data.list.length; i++) {
            int updatePosition = position + i;
            if (size > updatePosition) {
                updatePartData(updatePosition, (OrderBaseBean) data.list[i]);
            }
        }
    }

    /**
     * update Data
     *
     * @param data
     */
    private void updatePartData(int position, OrderBaseBean data) {
        if (data == null)
            return;
        mData.set(position, data);
    }

    /**
     * Add Data
     *
     * @param
     */

    public long getFirstRankId() {
        if (getOrderListLength() > 0)
            return mData.get(0).getRankId();
        else
            return ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
    }

    /**
     * Add Data
     *
     * @param
     */
    private long getLastRankId() {
        if (getOrderListLength() > 0)
            return mData.get(mData.size() - 1).getRankId();
        else
            return ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
    }


    public void destory() {
        mData = null;
    }

    public int getLimit() {
        return mLimit;
    }

    public int getModel() {
        return mModel;
    }

    public long getLastId() {
        return mLastId;
    }

    public boolean isLastPage() {
        return mIsLastPage;
    }
}
