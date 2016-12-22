package com.letv.walletbiz.member.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.PriorityExecutorHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.adapters.MemberAdapter;
import com.letv.walletbiz.member.beans.BannerListBean;
import com.letv.walletbiz.member.beans.MemberTypeListBean;
import com.letv.walletbiz.member.beans.ProductListBean;
import com.letv.walletbiz.member.task.MemberBannerListTask;
import com.letv.walletbiz.member.task.MemberProductListTask;
import com.letv.walletbiz.member.util.MemberCommonCallback;
import com.letv.walletbiz.member.activity.MemberAgreementActivity;
import com.letv.walletbiz.member.widget.BannerPtrFrameLayout;

import org.xutils.common.task.PriorityExecutor;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by zhanghuancheng on 16-11-16.
 */

public class MemberFragment extends BaseFragment {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MemberAdapter mMemberAdapter;
    private BannerPtrFrameLayout mPtrFrameLayout;
    private MemberBannerListTask mBannerListTask;
    private MemberProductListTask mProductListTask;
    private PriorityExecutor mExecutor;

    private boolean mBannerLoadDone = false;
    private boolean mProductLoadDone = false;
    private boolean mBannerRefreshDone = false;
    private boolean mProductRefreshDone = false;
    private boolean mProductNoNet = false;
    private boolean mProductErNet = false;
    private boolean mBannerNoNet = false;
    private boolean mBannerErNet = false;
    private boolean mNeedUpdateBanner = true;

    private MemberTypeListBean.MemberTypeBean mMemberTypeBean;

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loadData();
        }
    };

    private MemberCommonCallback<BannerListBean.BannerBean[]> mBannerListCallback = new MemberCommonCallback<BannerListBean.BannerBean[]>() {
        @Override
        public void onLoadFinished(BannerListBean.BannerBean[] result, int errorCode, boolean needUpdate) {
            mBannerNoNet = false;
            mBannerErNet = false;
            if (result != null && result.length > 0) {
                if (needUpdate) {
                    mMemberAdapter.setBannerList(result);
                }
                if (!isNetworkAvailable() || errorCode == MemberCommonCallback.ERROR_NO_NETWORK) {
                    mBannerNoNet = true;
                } else if (errorCode == MemberCommonCallback.ERROR_NETWORK) {
                    mBannerErNet = true;
                }
                showNetToast();
            } else if (result == null || result.length == 0 ) {
                mMemberAdapter.setBannerList(null);
                mNeedUpdateBanner = false;
                if (!isNetworkAvailable() || errorCode == MemberCommonCallback.ERROR_NO_NETWORK) {
                    mBannerNoNet = true;
                } else if (errorCode == MemberCommonCallback.ERROR_NETWORK) {
                    mBannerErNet = true;
                }
                showNetToast();
            } else {
                showBlankPages(errorCode);
            }
            mBannerLoadDone = true;
            if (checkLoadState()) {
                hideLoadingView();
            }
            mBannerRefreshDone = true;
            hideRefreshing();
        }
    };

    private MemberCommonCallback<ProductListBean.ProductBean[]> mProductListCallback = new MemberCommonCallback<ProductListBean.ProductBean[]>() {
        @Override
        public void onLoadFinished(ProductListBean.ProductBean[] result, int errorCode, boolean needUpdate) {
            mProductNoNet = false;
            mProductErNet = false;
            if (result != null && result.length > 0) {
                if (needUpdate) {
                    mMemberAdapter.setProductList(result);
                }
                if (!isNetworkAvailable() || errorCode == MemberCommonCallback.ERROR_NO_NETWORK) {
                    mProductNoNet = true;
                } else if (errorCode == MemberCommonCallback.ERROR_NETWORK) {
                    mProductErNet = true;
                }
                showNetToast();
            } else {
                showBlankPages(errorCode);
            }
            mProductLoadDone = true;
            if (checkLoadState()) {
                hideLoadingView();
            }
            mProductRefreshDone = true;
            hideRefreshing();
        }
    };

    private void showBlankPages(int errorCode) {
        if (errorCode == MemberCommonCallback.ERROR_NETWORK) {
            showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
        } else if (!isNetworkAvailable() || errorCode == MemberCommonCallback.ERROR_NO_NETWORK) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    private void hideRefreshing() {
        if (mPtrFrameLayout.isRefreshing() && checkRefreshState()) {
            resetRefreshState();
            mPtrFrameLayout.refreshComplete();
            mMemberAdapter.getmBannerVH().bannerPager.enableAutoSlide();
        }
    }

    private void showNetToast() {
        if (mBannerErNet && mProductErNet) {
            Toast.makeText(getContext(), getResources().getString(R.string.member_prompt_user_select_load_title), Toast.LENGTH_SHORT).show();
            mBannerErNet = false;
            mProductErNet = false;
        }
        if (mBannerNoNet && mProductNoNet) {
            Toast.makeText(getContext(), getResources().getString(R.string.member_prompt_net_connection_fail), Toast.LENGTH_SHORT).show();
            mProductNoNet = false;
            mBannerNoNet = false;
        }
    }

    @Override
    public void hideLoadingView() {
        mBannerLoadDone = false;
        mProductLoadDone = false;
        super.hideLoadingView();
    }

    private boolean checkLoadState() {
        return mBannerLoadDone && mProductLoadDone;
    }

    private boolean checkRefreshState() {
        return mBannerRefreshDone && mProductRefreshDone;
    }

    private void resetRefreshState() {
        mBannerRefreshDone = false;
        mProductRefreshDone = false;
    }

    public static MemberFragment newInstance(MemberTypeListBean.MemberTypeBean memberTypeBean) {
        MemberFragment fragment = new MemberFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MemberConstant.MEMBER_TYPE, memberTypeBean);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mMemberTypeBean = (MemberTypeListBean.MemberTypeBean) getArguments().getSerializable(MemberConstant.MEMBER_TYPE);
        }
        mExecutor = PriorityExecutorHelper.getPriorityExecutor();
        registerNetWorkReceiver();
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.member_fragment_layout, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == MemberAdapter.POSITION_AGREEMENT) {
                    Intent intent = new Intent(mContext, MemberAgreementActivity.class);
                    intent.putExtra(CommonConstants.EXTRA_URL, mMemberTypeBean.protocol_link);
                    intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, mMemberTypeBean.name + getString(R.string.label_agreement));
                    startActivity(intent);
                }
            }
        }));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mMemberAdapter = new MemberAdapter(mContext, mMemberTypeBean, (ViewGroup) mRecyclerView.getParent());
        mRecyclerView.setAdapter(mMemberAdapter);

        mPtrFrameLayout = (BannerPtrFrameLayout) view.findViewById(R.id.refresh_header_view);
        mMemberAdapter.setPtrFrameLayout(mPtrFrameLayout);
        mPtrFrameLayout.setLastUpdateTimeRelateObject(this);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mMemberAdapter.getmBannerVH().bannerPager.disableAutoSlide();
                loadData();
            }
        });
        loadData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if ((mPtrFrameLayout != null && mPtrFrameLayout.isRefreshing())
                    || (mMemberAdapter.getBannerList() != null && mMemberAdapter.getProductList() != null)) {
                hideBlankPage();
            }
            loadData();
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    private void loadData() {
        hideBlankPage();

        if (mNeedUpdateBanner) {
            if (mMemberAdapter.getBannerList() == null || mMemberAdapter.getProductList() == null) {
                showLoadingView();
            }
        } else {
            if (mMemberAdapter.getProductList() == null)
                showLoadingView();
        }
        if (mBannerListTask == null) {
            mBannerListTask = new MemberBannerListTask(mContext, mMemberTypeBean.type, mBannerListCallback);
        }
        mExecutor.execute(mBannerListTask);
        if (mProductListTask == null) {
            String uToken = AccountHelper.getInstance().getToken(mContext);
            mProductListTask = new MemberProductListTask(mContext, String.valueOf(mMemberTypeBean.id), uToken, mProductListCallback);
        }
        mExecutor.execute(mProductListTask);
    }
}
