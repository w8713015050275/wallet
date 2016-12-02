package com.letv.walletbiz.base.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letv.shared.widget.LeLoadingDialog;
import com.letv.wallet.common.fragment.AccountBaseFragment;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderRequestBean;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.view.OrderListViewAdapter;
import com.letv.walletbiz.mobile.beans.OrderBean;

import org.xutils.common.Callback;
import org.xutils.xmain;

import java.lang.reflect.Type;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import timehop.stickyheader.RecyclerItemClickListener;
import timehop.stickyheader.StickyRecyclerHeadersDecoration;
import timehop.stickyheader.StickyRecyclerHeadersTouchListener;

/**
 * Created by changjiajie on 16-1-26.
 */
public abstract class BaseOrderListFragment extends AccountBaseFragment implements OrderListViewAdapter.BaseOrderListCallBack {

    public static final String TAG = BaseOrderListFragment.class.getSimpleName();
    public static final int ORDER_LIST_RETURN = 100;
    public static final int ORDER_LIST_FAIL_RETURN = 102;
    private static String ISGOLOGIN = "isGoLogin";
    // pull refresh precedence
    private boolean mLoading = false;
    private boolean isGoLogin = false;
    private long mLoadLastId = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
    // page contain item count
    private int mPageItemCount = 0;
    // list last visible position
    private int mLastVisibleItem = 0;
    private long mClickLastId = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
    private int mClickPosition = -1;
    private boolean isClickItem = false;
    private static int mLimits = 20;

    private View mView;
    private OrderListViewAdapter mAdapter;
    private PtrClassicFrameLayout mPtrFrameLayout;
    private RecyclerView mOrderListView;
    private LinearLayoutManager mLayoutManager;
    private LeLoadingDialog mProgressDialog;
    private StickyRecyclerHeadersDecoration mDecoration;
    AccountHelper accountHelper = AccountHelper.getInstance();

    private Callback.Cancelable mCancelable;
    private Thread mLoadDataThd;

    private String promptNoRecordStr;

    public void setLimits(int limits) {
        mLimits = limits;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_order_list, null);
        initV();
        if (AccountHelper.getInstance().isLogin(BaseOrderListFragment.this.getActivity())) {
            if (isNetworkAvailable()) {
                showLoadingView();
                loadData(ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID, ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE);
            } else {
                showErrorBlankPage(BlankPage.STATE_NO_NETWORK);
            }
        } else {
            isGoLogin = true;
            if (mPtrFrameLayout != null && mPtrFrameLayout.getVisibility() == View.VISIBLE) {
                mPtrFrameLayout.setVisibility(View.GONE);
            }
            if (savedInstanceState == null || !savedInstanceState.getBoolean(ISGOLOGIN)) {
                AccountHelper.getInstance().loginLetvAccountIfNot(BaseOrderListFragment.this.getActivity(), null);
            }
        }
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!AccountHelper.getInstance().isLogin(BaseOrderListFragment.this.getActivity())) {
            isGoLogin = true;
            showErrorBlankPage(BlankPage.STATE_NO_LOGIN);
        } else {
            if (isGoLogin) {
                isGoLogin = false;
                mLoading = false;
                resetClickParams();
                if (mPtrFrameLayout != null) {
                    mPtrFrameLayout.setVisibility(View.GONE);
                }
                mAdapter.notifyDataSetChanged();
                hideBlankPage();
                if (isNetworkAvailable()) {
                    showLoadingView();
                    loadData(ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID, ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE);
                } else {
                    showErrorBlankPage(BlankPage.STATE_NO_NETWORK);
                }
                return;
            }
            updateData(mClickLastId, 1, mClickPosition);
        }
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (!isNetworkAvailable || !AccountHelper.getInstance().isLogin(BaseOrderListFragment.this.getActivity()) || mOrderListView == null)
            return;
        OrderListViewAdapter adapater = (OrderListViewAdapter) mOrderListView.getAdapter();
        if (adapater != null) {
            if (adapater.getItemCount() <= ActivityConstant.NUMBER.ZERO) {
                resetClickParams();
                mLoading = false;
                hideBlankPage();
                loadData(ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID, ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE);
            } else {
                if (mLoadLastId != ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID) {
                    loadData(mLoadLastId, ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE);
                }
            }
            updateData(mClickLastId, 1, mClickPosition);
        }
    }

    private void showErrorBlankPage(int failState) {
        hideLoadingView();
        switch (failState) {
            case ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD:
                hidePtrFrameLayout();
                showBlankPage().setCustomPage(promptNoRecordStr, BlankPage.Icon.NO_HISTORY);
                break;
            case ActivityConstant.RETURN_STATUS.FAIL_STATE_TOAST_PROMPT:
                if (mPtrFrameLayout != null && mPtrFrameLayout.isRefreshing()) {
                    mPtrFrameLayout.refreshComplete();
                }
                promptNoNetWork();
                break;
            case BlankPage.STATE_NO_NETWORK:
                hidePtrFrameLayout();
                showBlankPage(BlankPage.STATE_NO_NETWORK);
                break;
            case BlankPage.STATE_NETWORK_ABNORMAL:
                hidePtrFrameLayout();
                showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO 重新加载
                        if (!isNetworkAvailable() || mOrderListView == null)
                            return;
                        OrderListViewAdapter adapater = (OrderListViewAdapter) mOrderListView.getAdapter();
                        if (adapater != null && adapater.getItemCount() <= ActivityConstant.NUMBER.ZERO) {
                            hideBlankPage();
                            showLoadingView();
                            loadData(ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID, ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE);
                        }
                    }
                });
                break;
            default: {
                hidePtrFrameLayout();
                showBlankPage(failState);
            }
            break;
        }
    }

    /**
     * TODO:加载数据
     */
    public void resetRefresh() {
        if (isNetworkAvailable()) {
            mLoading = false;
            loadData(ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID, ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE);
        } else {
            showErrorBlankPage(ActivityConstant.RETURN_STATUS.FAIL_STATE_TOAST_PROMPT);
        }
    }

    protected abstract OrderRequestBean getRequestBean();

    protected abstract Type getResponseType();

    protected abstract RecyclerItemClickListener getRecycleritemClickListener();

    public OrderListViewAdapter getOrderListAdapter() {
        return mAdapter;
    }

    protected boolean isSupportHeader() {
        return false;
    }

    private void initV() {
        initResource();
        initLoadingDialog();
        mLoading = false;
        mAdapter = new OrderListViewAdapter(getContext(), this, isSupportHeader());
        mPtrFrameLayout = (PtrClassicFrameLayout) mView.findViewById(R.id.refresh_header_view);
        mOrderListView = (RecyclerView) mView.findViewById(R.id.rv_order_list);
        if (isSupportHeader()) {
            mDecoration = new StickyRecyclerHeadersDecoration(mAdapter);
            mOrderListView.addItemDecoration(mDecoration);
            StickyRecyclerHeadersTouchListener touchListener = new StickyRecyclerHeadersTouchListener(mOrderListView, mDecoration);
            touchListener.setOnHeaderClickListener(new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
                @Override
                public void onHeaderClick(View header, int position, long headerId) {

                }
            });
            mOrderListView.addOnItemTouchListener(touchListener);
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mDecoration.invalidateHeaders();
                }
            });
       }
        mLayoutManager =
                new LinearLayoutManager(getContext());
        mOrderListView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), getResources().getColor(R.color.colorDividerLineBg),
                        DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
        mOrderListView.addItemDecoration(dividerItemDecoration);
        mOrderListView.setAdapter(mAdapter);
        RecyclerItemClickListener itemClickListener = getRecycleritemClickListener();
        if (itemClickListener != null) {
            mOrderListView.addOnItemTouchListener(itemClickListener);
        }
        mOrderListView.addOnItemTouchListener(mGetClickPosition);
        initListener();
    }

    RecyclerItemClickListener mGetClickPosition = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            mClickPosition = position;
            isClickItem = true;
            if (mClickPosition == 0) {
                mClickLastId = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
            } else {
                OrderBaseBean orderBean = mAdapter.getOrderItem(mClickPosition - ActivityConstant.ORDER.LIST_CONSTANT.POSTION_OFFSET);
                if (orderBean != null) {
                    mClickLastId = orderBean.getRankId();
                } else {
                    resetClickParams();
                }
            }

        }
    }
    );

    private void initResource() {
        promptNoRecordStr = getResources().getString(R.string.total_order_empty_no_record);
    }

    private void initListener() {
        mOrderListView.setOnScrollListener(new RecyclerViewScrollListener());
        mPtrFrameLayout.setLastUpdateTimeRelateObject(this);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                if (isNetworkAvailable()) {
                    return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                } else {
                    return false;
                }
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                //On a second pull loading after operation under refresh, remove load more news
                resetRefresh();
            }
        });
    }

    private void initLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new LeLoadingDialog(getContext(), 1, 48);
            mProgressDialog.setCancelable(false);
        }
    }

    protected Handler mHandler = new Handler() {
        // 在Handler中获取消息，重写handleMessage()方法
        @Override
        public void handleMessage(Message msg) {
            // 判断消息码是否为1
            switch (msg.what) {
                case ORDER_LIST_RETURN:
                    updataOrderListData((OrderListBaseBean<OrderBaseBean>) msg.obj, msg.arg2);
                    mLoading = false;
                    break;
                case ORDER_LIST_FAIL_RETURN:
                    showErrorBlankPage(msg.arg1);
                    mLoading = false;
                    break;
                default:
                    return;
            }
        }
    };

    protected void showProgressLoadingDialog() {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) mProgressDialog.show();
    }

    protected void hideProgressLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }

    /**
     * 加载完数据回调方法 state值
     * ActivityConstant.RETURN_STATUS.FAIL_STATE_NO_NET 没网状态
     * ActivityConstant.RETURN_STATUS.FAIL_STATE_NET_CONNECTION_FIAL网络链接错误
     * ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD 没有记录
     *
     * @param result
     */
    private void updataOrderListData(OrderListBaseBean<OrderBaseBean> result, int position) {
        hideLoadingView();
        showPtrFrameLayout();
        OrderListViewAdapter adapater = (OrderListViewAdapter) mOrderListView.getAdapter();
        if (result == null || result.list == null || result.list.length <= 0) {
            if (!isNetworkAvailable()) {
                showErrorBlankPage(BlankPage.STATE_NETWORK_ABNORMAL);
            } else {
                if (adapater.getItemCount() <= 0) {
                    showErrorBlankPage(ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD);
                }
            }
        } else {
            hideBlankPage();
            switch (result.model) {
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE:
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_REFRESH:
                    mLoadLastId = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE:
                    adapater.addData(result);
                    break;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_PART_UPDATE:
                    resetClickParams();
                    adapater.addData(result, position);
                    break;

            }
        }
    }

    private void resetClickParams() {
        isClickItem = false;
        mClickPosition = -1;
        mClickLastId = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_LASTID;
    }

    private void showPtrFrameLayout() {
        if (mPtrFrameLayout.getVisibility() == View.GONE) {
            mPtrFrameLayout.setVisibility(View.VISIBLE);
        }
        if (mPtrFrameLayout.isRefreshing()) {
            mPtrFrameLayout.refreshComplete();
        }
    }

    private void hidePtrFrameLayout() {
        if (mPtrFrameLayout.getVisibility() == View.VISIBLE) {
            mPtrFrameLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 加载更多时需要调用
     *
     * @param lastId
     */
    private void loadData(long lastId, int mode) {
        if (!mLoading) {
            mLoadDataThd =
                    new Thread(new queryOrderListThread(lastId, mode));
            mLoadDataThd.start();
        }
    }

    /**
     * 更新数据
     *
     * @param lastId
     */
    private void updateData(long lastId, int limits, int updatePosition) {
        if (isClickItem && mClickPosition != -1) {
            mLoadDataThd =
                    new Thread(new queryOrderListThread(lastId, ActivityConstant.ORDER.LIST_CONSTANT.MOD_PART_UPDATE, limits, updatePosition));
            mLoadDataThd.start();
        }
    }

    /**
     * 下拉刷新加载数据
     *
     * @param firstId
     */
    private void refreshData(long firstId) {
        if (!mLoading) {
            mLoadDataThd =
                    new Thread(new queryOrderListThread(firstId, ActivityConstant.ORDER.LIST_CONSTANT.MOD_REFRESH));
            mLoadDataThd.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean(ISGOLOGIN, !AccountHelper.getInstance().isLogin(BaseOrderListFragment.this.getActivity()));
        }
    }

    @Override
    public void onDestroy() {
        resetClickParams();
        if (mHandler != null) {
            mHandler.removeMessages(ORDER_LIST_RETURN);
            mHandler.removeMessages(ORDER_LIST_FAIL_RETURN);
        }
        if (mAdapter != null) {
            mAdapter.destory();
        }
        if (mProgressDialog != null) {
            mProgressDialog.onDismissDialog4DestroyContext();
        }
        super.onDestroy();
    }

    class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView,
                                         int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!mLoading && (newState == RecyclerView.SCROLL_STATE_IDLE)
                    && (mAdapter.isLastPage() ? false : (mLastVisibleItem + ActivityConstant.ORDER.LIST_CONSTANT.POSTION_OFFSET == mAdapter.getItemCount()))) {
                if (isNetworkAvailable()) {
                    loadData(mAdapter.getLastId(), ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE);
                } else {
                    mLoadLastId = mAdapter.getLastId();
                    showErrorBlankPage(ActivityConstant.RETURN_STATUS.FAIL_STATE_TOAST_PROMPT);
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            if (!mLoading && !mAdapter.isLastPage() && mAdapter.getItemCount() > 0) {
                mPageItemCount = mLastVisibleItem - mLayoutManager.findFirstVisibleItemPosition();
                // The last item is the last page of the first
                if (mLastVisibleItem >= (mAdapter.getItemCount() - mPageItemCount)) {
                    if (isNetworkAvailable()) {
                        loadData(mAdapter.getLastId(), ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE);
                    }
                }
            }
        }
    }

    private void queryOrders(long lastId, int mod, final int limits/* 1 more; -1 refresh*/, final int updatePosition) {
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        OrderRequestBean requestBean = getRequestBean();
        if (requestBean == null || requestBean.reqParams == null || requestBean.reqParams.getQueryStringParams().size() <= 0)
            return;
        BaseRequestParams reqParams = requestBean.reqParams;
        reqParams.addQueryStringParameter(ActivityConstant.ORDER.PARAM.ORDER_LASTID, String.valueOf(lastId));
        reqParams.addQueryStringParameter(ActivityConstant.ORDER.PARAM.ORDER_LIMIT, String.valueOf(limits));
        int reqMode = ActivityConstant.ORDER.LIST_CONSTANT.DEFAULT_MOD;
        switch (mod) {
            case ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE:
            case ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE:
            case ActivityConstant.ORDER.LIST_CONSTANT.MOD_PART_UPDATE:
                reqMode = ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE;
                break;
            case ActivityConstant.ORDER.LIST_CONSTANT.MOD_REFRESH:
                reqMode = ActivityConstant.ORDER.LIST_CONSTANT.MOD_REFRESH;
                break;
        }
        reqParams.addQueryStringParameter(ActivityConstant.ORDER.PARAM.ORDER_MODEL, String.valueOf(reqMode));
        try {
            BaseResponse response = xmain.http().getSync(reqParams, getResponseType());
            OrderListBaseBean<OrderBean> orders = null;
            if (response != null) {
                orders = (OrderListBaseBean<OrderBean>) response.data;
                orders.model = mod;
            }
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(ORDER_LIST_RETURN);
                msg.obj = orders;
                msg.arg2 = updatePosition;
                msg.what = ORDER_LIST_RETURN;
                // 发送这个消息到消息队列中
                mHandler.sendMessage(msg);
            }
        } catch (Throwable throwable) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(ORDER_LIST_FAIL_RETURN);
                msg.arg1 = BlankPage.STATE_NETWORK_ABNORMAL;
                if (mAdapter != null && mAdapter.getItemCount() > 0) {
                    msg.arg1 = ActivityConstant.RETURN_STATUS.FAIL_STATE_TOAST_PROMPT;
                }
                msg.what = ORDER_LIST_FAIL_RETURN;
                mHandler.sendMessage(msg);
            }
        }
    }

    private class queryOrderListThread implements Runnable {
        long localLastId;
        int localMod;
        int localLimits;
        int localposition;

        public queryOrderListThread(long lastId, int mod) {
            this(lastId, mod, mLimits);
        }

        public queryOrderListThread(long lastId, int mod, int limits) {
            this.localLastId = lastId;
            this.localMod = mod;
            this.localLimits = limits;
        }

        public queryOrderListThread(long lastId, int mod, int limits, int updatePosition) {
            this.localLastId = lastId;
            this.localMod = mod;
            this.localLimits = limits;
            this.localposition = updatePosition;
        }

        @Override
        public void run() {
            switch (localMod) {
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_MORE:
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_REFRESH:
                    mLoadLastId = localLastId;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_UPDATE:
                    mLoading = true;
                    break;
                case ActivityConstant.ORDER.LIST_CONSTANT.MOD_PART_UPDATE:
                    break;
            }
            queryOrders(this.localLastId, this.localMod, this.localLimits, this.localposition);
        }
    }

}
