package com.letv.walletbiz.main.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letv.tracker.enums.EventType;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.wallet.common.util.WalletExecutor;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.SpacesItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.main.recommend.RecommendTask;
import com.letv.walletbiz.main.recommend.bean.RecommendCardBean;
import com.letv.walletbiz.main.recommend.view.RecommendCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by liuliang on 16-12-29.
 */

public class RecommendFragment extends MainFragment {

    private RecyclerView mRecyclerView;
    private PtrClassicFrameLayout mPtrFrameLayout;
    private RecommendAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private RecommendTask mTask;
    private WalletExecutor mExecutor;

    private LocationHelper mLocationHelper;
    private Address mAddress;

    private List<RecommendCardBean> mRecommendCardList;

    private static final int MSG_LOAD_FINISHED = 1;
    private static final int MSG_REFRESH_COMPLETED = 2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    if (isAdded() && !isDetached()) {
                        hideLoadingView();
                        List<RecommendCardBean> result = (List<RecommendCardBean>) msg.obj;
                        if (msg.arg1 != CommonCallback.NO_ERROR) {
                            showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
                        } else {
                            if (result == null || result.size() <= 0) {
                                BlankPage blankPage = showBlankPage();
                                if (blankPage != null) {
                                    blankPage.setCustomPage(getString(R.string.main_recommend_no_data), BlankPage.Icon.NO_ACCESS);
                                }
                            } else {
                                mRecommendCardList = result;
                                mAdapter.setData(mRecommendCardList);
                            }
                        }
                    }
                    mTask = null;
                    break;
                case MSG_REFRESH_COMPLETED:
                    if (mPtrFrameLayout != null && mPtrFrameLayout.isRefreshing()) {
                        mPtrFrameLayout.refreshComplete();
                    }
                    break;
            }
        }
    };

    private CommonCallback<List<RecommendCardBean>> mCallback = new CommonCallback<List<RecommendCardBean>>() {

        @Override
        public void onLoadFinished(List<RecommendCardBean> result, int errorCode) {
            Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
            msg.obj = result;
            msg.arg1 = errorCode;
            mHandler.sendMessage(msg);
            mHandler.sendEmptyMessage(MSG_REFRESH_COMPLETED);
        }
    };

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            startLoadData();
        }
    };

    private LocationHelper.LocationCallback mLocationCallback = new LocationHelper.LocationCallback() {

        @Override
        public void onLocationUpdateFinished(Address address, int responseCode) {
            if (responseCode == LocationHelper.LOCATE_SUCCESS) {
                mAddress = address;
                if (mAdapter != null) {
                    mAdapter.setAddress(mAddress);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExecutor = ExecutorHelper.getExecutor();
        mLocationHelper = LocationHelper.getInstance();
        mLocationHelper.addLocationCallback(mLocationCallback);
        mLocationHelper.getAddress(false);
        Action.uploadCustom(EventType.Expose, Action.RECOMMEND_PAGE_EXPOSE);
    }

    @Override
    public void onStart() {
        super.onStart();
        String from;
        Map<String, Object> props = new HashMap<>();
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            from = intent.getStringExtra(WalletConstant.EXTRA_FROM);
            props.put(WalletConstant.EXTRA_FROM, from);
        }
        Action.uploadCustom(EventType.Expose, Action.RECOMMEND_PAGE_EXPOSE, props);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_recommend, container, false);

        mPtrFrameLayout = (PtrClassicFrameLayout) view.findViewById(R.id.refresh_header_view);
        mPtrFrameLayout.setLastUpdateTimeRelateObject(this);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                loadData(false);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        int space = (int) DensityUtils.dip2px(6);
        SpacesItemDecoration itemDecoration = new SpacesItemDecoration(space, space, space, 0);
        mRecyclerView.addItemDecoration(itemDecoration);

        mAdapter = new RecommendAdapter(getContext());
        if (mAddress != null) {
            mAdapter.setAddress(mAddress);
        }
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationHelper.removeLocationCallback(mLocationCallback);
    }

    @Override
    public void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && mRecommendCardList == null) {
            startLoadData();
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public boolean displayActionbar() {
        return true;
    }

    @Override
    public void gotoNext(int type) {

    }

    @Override
    public void startLoadData() {
        if (!isNetworkAvailable()) {
            if (mRecommendCardList == null) {
                showBlankPage(BlankPage.STATE_NO_NETWORK, mRetryClickListener);
            }
            return;
        }
        loadData(true);
    }

    private void loadData(boolean showLoading) {
        if (mTask == null) {
            mTask = new RecommendTask(getContext(), mCallback, mAddress);
            mExecutor.runnableExecutor(mTask);
            if (showLoading) {
                showLoadingView();
            }
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(View itemView) {
            super(itemView);
        }
    }

    static class RecommendAdapter extends RecyclerView.Adapter<ItemHolder> {

        private Context mContext;
        private List<RecommendCardBean> mCardList;
        private Address mAddress;

        public RecommendAdapter(Context context) {
            mContext = context;
        }

        public void setData(List<RecommendCardBean> cardList) {
            mCardList = cardList;
            notifyDataSetChanged();
        }

        public void setAddress(Address address) {
            mAddress = address;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecommendCardView cardView = new RecommendCardView(mContext);
            return new ItemHolder(cardView);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            RecommendCardBean cardBean = mCardList.get(position);
            ((RecommendCardView) holder.itemView).setCardBean(cardBean);
            holder.itemView.setTag(mAddress);
        }

        @Override
        public int getItemCount() {
            return mCardList == null ? 0 : mCardList.size();
        }
    }
}
