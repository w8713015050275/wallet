package com.letv.walletbiz.coupon.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.activity.CouponDetailActivity;
import com.letv.walletbiz.coupon.adapter.CouponExpiredAdapter;
import com.letv.walletbiz.coupon.adapter.CouponListAdapter;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.coupon.beans.CouponExpiredListResponseResult;
import com.letv.walletbiz.coupon.utils.CardCouponListTask;
import com.letv.walletbiz.coupon.utils.CouponCommonCallback;
import com.letv.walletbiz.coupon.utils.CouponListLoadTask;
import com.letv.walletbiz.coupon.utils.CouponUtils;
import com.letv.walletbiz.coupon.utils.RecyclerScroller;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.beans.MovieOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by lijunying on 16-4-19.
 */
public class CouponExpiredFragment extends BaseFragment implements AccountHelper.OnAccountChangedListener {
    private RecyclerView mRecyclerView;
    private static final String TAG = CouponExpiredFragment.class.getSimpleName();
    private int viewType;
    private View mRootView;
    private CouponExpiredAdapter mAdapter;
    private CouponListLoadTask mCouponLoadTask;
    private CardCouponListTask mCardLoadTask;
    private long last_id_card = -1;
    private long last_id_coupon ;
    private List<BaseCoupon> mCouponExpiredList;
    private List<MovieOrder> mCardExpiredList;
    private Intent intent;

    private RecyclerScroller recyclerScroller;
    private boolean isDataExpire = false;

    public static CouponExpiredFragment newInstance(int viewType) {
        CouponExpiredFragment fragment = new CouponExpiredFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CouponConstant.EXTRA_COUPON_EXPIRED_FRAGMENT_TYPE, viewType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        if (getArguments() != null) {
            viewType = getArguments().getInt(CouponConstant.EXTRA_COUPON_EXPIRED_FRAGMENT_TYPE);
        }
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDataExpire) { loadData();}
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mRootView) {
            mRootView = inflater.inflate(R.layout.coupon_expired_fragment, container, false);
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mAdapter = new CouponExpiredAdapter(getContext(), viewType);
            mRecyclerView.setAdapter(mAdapter);
            recyclerScroller = new RecyclerScroller(layoutManager, new RecyclerScroller.OnScrollListener() {
                @Override
                public void onLoadMore() {
                    loadData();
                }
            });
            mRecyclerView.addOnScrollListener(recyclerScroller);
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (mAdapter != null) {
                        Object obj = mAdapter.getItem(position);
                        if (obj != null) {
                            if (obj instanceof BaseCoupon) {
                                intent = new Intent(getActivity(), CouponDetailActivity.class);
                                intent.putExtra(CouponConstant.EXTRA_COUPON_BEAN, (BaseCoupon) obj);

                            } else if (obj instanceof MovieOrder) {
                                intent = new Intent(getActivity(), MovieOrderDetailActivity.class);
                                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM, ((MovieOrder) obj).order_no);
                            }
                            if (getActivity() != null && intent != null) {
                                startActivity(intent);
                            }
                        }

                    }
                }
            }));

        } else if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeAllViews();
        }
        loadData();
        return mRootView;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable || isDataExpire) {
            loadData();
        }
    }

    private void loadData() {
        if (viewType == CouponListAdapter.VIEW_TYPE_COUPON_ITEM) { //优惠劵
            loadCouponExpiredList(last_id_coupon, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
        } else if (viewType == CouponListAdapter.VIEW_TYPE_CARD_ITEM) { //卡劵
            loadCardExpiredList(last_id_card, CouponConstant.COUPON_PARAM_LIMIT_DEFAULT);
        }
    }

    private void loadCouponExpiredList(long last_id , int limit) { //优惠劵
        if (isNetworkAvailable()) {
            if (mCouponLoadTask == null) {
                if (mCouponExpiredList == null || mCouponExpiredList.size() == 0) showLoadingView();
                mCouponLoadTask = new CouponListLoadTask(getActivity(), last_id, limit);
                mCouponLoadTask.setExpiredResponseCallback(mCouponExpiredListCallback);
                ExecutorHelper.getExecutor().runnableExecutor(mCouponLoadTask);
            }
        } else if (mCouponExpiredList != null && mCouponExpiredList.size() > 0) {
            CouponUtils.showToast(getActivity(), R.string.empty_no_network);
        } else {
            handleError(CouponCommonCallback.ERROR_NO_NETWORK);
        }

    }

    private void loadCardExpiredList(long last_id, int pageSize) { //卡劵
        if (isNetworkAvailable()) {
            if (mCardLoadTask == null) {
                if (mCardExpiredList == null || mCardExpiredList.size() == 0) {
                    showLoadingView();
                }
                mCardLoadTask = new CardCouponListTask(getActivity(), mCardExpiredListCallback);
                mCardLoadTask.setParams(MovieOrder.MOVIE_TICKET_PROGRESS_SHOWN, last_id, -1, pageSize);
                ExecutorHelper.getExecutor().runnableExecutor(mCardLoadTask);
            }
        } else if (mCardExpiredList != null && mCardExpiredList.size() > 0) {
            CouponUtils.showToast(getActivity(), R.string.empty_no_network);
        } else {
            handleError(CouponCommonCallback.ERROR_NO_NETWORK);
        }

    }

    @Override
    public void onDestroyView() {
        if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    private CouponCommonCallback<CouponExpiredListResponseResult> mCouponExpiredListCallback = new CouponCommonCallback<CouponExpiredListResponseResult>() {
        @Override
        public void onLoadFinished(CouponExpiredListResponseResult result, int errorCode) {
            if (getActivity() == null || getActivity().isFinishing()) return;
            if (mCouponExpiredList == null || mCouponExpiredList.size() == 0) hideLoadingView();

            if (errorCode == CouponCommonCallback.NO_ERROR) {
                if (getActivity() !=null) {
                    getActivity().setResult(Activity.RESULT_OK);
                }
                if (mRecyclerView.getVisibility() != View.VISIBLE) mRecyclerView.setVisibility(View.VISIBLE);
                if (result != null && result.list != null && result.list.length > 0) {
                    List<BaseCoupon> temp = new ArrayList<>(Arrays.asList(result.list));
                    if (mCouponExpiredList != null && mCouponExpiredList.size() > 0) {

                        mCouponExpiredList.addAll(temp);
                    } else {
                        mCouponExpiredList = (temp);
                    }
                    last_id_coupon = temp.get(temp.size()-1).getRank_id();
                    mAdapter.setData(mCouponExpiredList);

                } else if (mCouponExpiredList != null && mCouponExpiredList.size() > 0) {
                    recyclerScroller.setEnableLoadMore(false);
                } else {
                    handleError(CouponCommonCallback.ERROR_NO_DATA);
                }
                isDataExpire = false;
            } else if (errorCode == CouponCommonCallback.ERROR_NETWORK) {
                if (mCouponExpiredList != null && mCouponExpiredList.size() > 0) {
                    CouponUtils.showToast(getActivity(), R.string.empty_network_error);
                } else {
                    handleError(CouponCommonCallback.ERROR_NETWORK);
                }

            } else if (errorCode == CouponCommonCallback.ERROR_NO_NETWORK) {
                if (mCouponExpiredList != null && mCouponExpiredList.size() > 0) {
                    CouponUtils.showToast(getActivity(), R.string.empty_no_network);
                } else {
                    handleError(CouponCommonCallback.ERROR_NO_NETWORK);
                }
            }
            mCouponLoadTask = null;
        }
    };

    private CouponCommonCallback<CardCouponList> mCardExpiredListCallback = new CouponCommonCallback<CardCouponList>() {
        @Override
        public void onLoadFinished(CardCouponList result, int errorCode) {
            if (getActivity() == null || getActivity().isFinishing()) return;
            if (mCardExpiredList == null || mCardExpiredList.size() == 0) hideLoadingView();
            if (errorCode == CouponCommonCallback.NO_ERROR) {
                    if (mRecyclerView.getVisibility() != View.VISIBLE) mRecyclerView.setVisibility(View.VISIBLE);
                    if (result != null && result.list != null && result.list.length > 0) {
                    List<MovieOrder> temp = new ArrayList<>(Arrays.asList(result.list));
                    if (mCardExpiredList != null && mCardExpiredList.size() > 0) {

                        mCardExpiredList.addAll(temp);
                    } else {
                        mCardExpiredList = (temp);
                    }
                    last_id_card = temp.get(temp.size()-1).rank_id;
                    mAdapter.setData(mCardExpiredList);

                } else if (mCardExpiredList != null && mCardExpiredList.size() > 0) {
                    recyclerScroller.setEnableLoadMore(false);
                } else {
                    handleError(CouponCommonCallback.ERROR_NO_DATA);
                }
                isDataExpire = false;
            } else if (errorCode == CouponCommonCallback.ERROR_NETWORK) {
                if (mCardExpiredList != null && mCardExpiredList.size() > 0) {
                    CouponUtils.showToast(getActivity(), R.string.empty_network_error);
                } else {
                    handleError(CouponCommonCallback.ERROR_NETWORK);
                }

            } else if (errorCode == CouponCommonCallback.ERROR_NO_NETWORK) {
                if (mCardExpiredList != null && mCardExpiredList.size() > 0) {
                    CouponUtils.showToast(getActivity(), R.string.empty_no_network);
                } else {
                    handleError(CouponCommonCallback.ERROR_NO_NETWORK);
                }
            }
            mCardLoadTask = null;
        }
    };

    private void handleError(int errorCode) {
        mRecyclerView.setVisibility(View.GONE);
        if (errorCode == CouponCommonCallback.ERROR_NO_NETWORK) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);

        } else if (errorCode == CouponCommonCallback.ERROR_NETWORK) {
            showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 重新加载
                    loadData();
                }
            });
        } else if (errorCode == CouponCommonCallback.ERROR_NO_DATA) {
            String str = " ";
            if (viewType == CouponListAdapter.VIEW_TYPE_COUPON_ITEM) { //优惠劵
                str = getString(R.string.coupon_list_expired_no_data);
            } else if (viewType == CouponListAdapter.VIEW_TYPE_CARD_ITEM) { //卡劵
                str = getString(R.string.coupon_card_list_expired_no_data);
            }
            BlankPage blankPage = showBlankPage();
            if (blankPage != null) {
                blankPage.setCustomPage(str, BlankPage.Icon.NO_ACCESS);
            }
        }
    }


    @Override
    public void onAccountLogin() { isDataExpire = true;}

    @Override
    public void onAccountLogout() { isDataExpire = true;}
}
