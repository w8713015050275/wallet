package com.letv.walletbiz.movie.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieDetailActivity;
import com.letv.walletbiz.movie.adapter.MovieListAdapter;
import com.letv.walletbiz.movie.adapter.MovieSoonListAdapter;
import com.letv.walletbiz.movie.beans.Movie;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MovieListLoadTask;

import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import timehop.stickyheader.RecyclerItemClickListener;
import timehop.stickyheader.StickyRecyclerHeadersDecoration;
import timehop.stickyheader.StickyRecyclerHeadersTouchListener;

/**
 * Created by liuliang on 15-12-30.
 */
public class MovieListFragment extends BaseFragment implements MovieCommonCallback<List<Movie>>,RadioGroup.OnCheckedChangeListener {
    private static final String TAG = MovieListFragment.class.getSimpleName();

    private PtrClassicFrameLayout mPtrFrameLayout;

    private RecyclerView mRecyclerView;

    private MovieListAdapter mShownAdapter;
    private MovieSoonListAdapter mSoonAdapter;
    private RecyclerView.Adapter adapter;
    private Movie movie;
    private int mCurrentCityID = -1;
    private MovieListLoadTask mLoadTask;
    private List<Movie> mData;
    private boolean isWill = false; // false:正在上映;  true:即将上映
    private RadioGroup radioGroupTimer;
    private StickyRecyclerHeadersDecoration decoration;

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            loadData(false);
        }

    };

    public static MovieListFragment newInstance(boolean isWill, int mCurrentCityID) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(MovieTicketConstant.EXTRA_MOVIE_LIST_FRAGMENT_TYPE, isWill);
        bundle.putInt(MovieTicketConstant.EXTRA_MOVIE_LIST_FRAGMENT_CITY_ID, mCurrentCityID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isWill = getArguments().getBoolean(MovieTicketConstant.EXTRA_MOVIE_LIST_FRAGMENT_TYPE);
            mCurrentCityID = getArguments().getInt(MovieTicketConstant.EXTRA_MOVIE_LIST_FRAGMENT_CITY_ID);
        }
        SharedPreferencesHelper.getSharePreferences().registerOnSharedPreferenceChangeListener(onCityPreferenceChangeListener);
        registerNetWorkReceiver();
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_list, container, false);
        LinearLayout ll_sort = (LinearLayout) view.findViewById(R.id.ll_sort);
        ll_sort.setVisibility(isWill? View.VISIBLE : View.GONE);
        radioGroupTimer = (RadioGroup) view.findViewById(R.id.radio_group);
        RadioGroup rg_filter = (RadioGroup) view.findViewById(R.id.rg_filter);
        rg_filter.setOnCheckedChangeListener(this);
        mPtrFrameLayout = (PtrClassicFrameLayout) view.findViewById(R.id.refresh_header_view);
        mPtrFrameLayout.setLastUpdateTimeRelateObject(this);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                loadData(true);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), getResources().getColor(R.color.colorDividerLineBg),
                DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(getAdapter(isWill));
        if (mCurrentCityID!= -1) {
            loadData(false);
        }
        return view;
    }

    RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

        @Override
        public void onItemClick(View view, int position) {
            adapter = getAdapter(isWill);
            movie = null;
            if (adapter instanceof MovieListAdapter) {
                movie = ((MovieListAdapter) adapter).getItem(position);
            } else if (adapter instanceof MovieSoonListAdapter) {
                movie = ((MovieSoonListAdapter) adapter).getItem(position);
            }
            if (movie != null) {
                Action.uploadClick(Action.MOVIE_DETAIL, String.valueOf(movie.id));
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, movie.id);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_NAME, movie.name);
                intent.putExtra(MovieTicketConstant.EXTRA_CITY_ID, mCurrentCityID);
                startActivity(intent);
            }
        }
    });

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if ((mPtrFrameLayout != null && mPtrFrameLayout.isRefreshing())
                    || (mData == null && mCurrentCityID != -1)){
                hideBlankPage();
                loadData(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() != null) {
            getActivity().getMenuInflater().inflate(R.menu.menu_movie_list, menu);
        }
    }

    @Override
    public void onLoadFinished(List<Movie> result, int errorCode) {
        if (getActivity() == null || getActivity().isFinishing()) return;
        hideLoadingView();
        if (errorCode == MovieCommonCallback.NO_ERROR) {
            mData = result;
            if (result == null || result.size() == 0) {
                BlankPage blankPage = showBlankPage();
                int resID = isWill ? R.string.movie_error_no_movie_will:R.string.movie_error_no_movie;
                blankPage.setCustomPage(getString(resID), BlankPage.Icon.NO_ACCESS);
            } else {
                hideBlankPage();
                adapter = getAdapter(isWill);
                if (adapter instanceof MovieListAdapter) {
                    ((MovieListAdapter) adapter).setData(result);
                } else if (adapter instanceof MovieSoonListAdapter) {
                    ((MovieSoonListAdapter) adapter).setData(result);
                }
            }
        } else if (errorCode == MovieCommonCallback.ERROR_NETWORK) {
            showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
        } else if (errorCode == MovieCommonCallback.ERROR_NO_NETWORK) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
        mLoadTask = null;
        if (mPtrFrameLayout.isRefreshing()) {
            mPtrFrameLayout.refreshComplete();
        }
    }

    private void loadData(boolean isLoadFromNet) {
        if (!isNetworkAvailable() && isLoadFromNet && mData != null) {
            Toast.makeText(getContext(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
            if (mPtrFrameLayout != null) {
                mPtrFrameLayout.refreshComplete();
            }
            return;
        }
        if (mLoadTask == null && mCurrentCityID != -1) {
            if (mData == null || mData.size() == 0) {
                showLoadingView();
            }
            mLoadTask = new MovieListLoadTask(getActivity(), this);
            mLoadTask.setParams(mCurrentCityID, isWill, isLoadFromNet);
            ExecutorHelper.getExecutor().runnableExecutor(mLoadTask);
        }
    }

    private RecyclerView.Adapter getAdapter(boolean isWill){
        return isWill ? getSoonAdapter() : getShownAdapter();
    }

    private RecyclerView.Adapter getSoonAdapter(){
        if (mSoonAdapter == null) {
            mSoonAdapter = new MovieSoonListAdapter(getContext(), radioGroupTimer, mCurrentCityID);
            decoration = new StickyRecyclerHeadersDecoration(mSoonAdapter);
            mRecyclerView.addItemDecoration(decoration);
            // Add touch listeners
            StickyRecyclerHeadersTouchListener touchListener =
                    new StickyRecyclerHeadersTouchListener(mRecyclerView, decoration);
            touchListener.setOnHeaderClickListener(
                    new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
                        @Override
                        public void onHeaderClick(View header, int position, long headerId) {
                        }
                    });
            mRecyclerView.addOnItemTouchListener(touchListener);
            mRecyclerView.addOnItemTouchListener(itemClickListener);
            mSoonAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    decoration.invalidateHeaders();
                }
            });
        }
        return mSoonAdapter;
    }

    private RecyclerView.Adapter getShownAdapter(){
        if(mShownAdapter == null){
            mShownAdapter = new MovieListAdapter(getContext(), mCurrentCityID);
            mRecyclerView.addOnItemTouchListener(itemClickListener);
        }
        return mShownAdapter ;
    }

    @Override
    public void onDestroy() {
        SharedPreferencesHelper.getSharePreferences().unregisterOnSharedPreferenceChangeListener(onCityPreferenceChangeListener);
        Bundle bundle = getArguments();
        bundle.putBoolean(MovieTicketConstant.EXTRA_MOVIE_LIST_FRAGMENT_TYPE, isWill);
        bundle.putInt(MovieTicketConstant.EXTRA_MOVIE_LIST_FRAGMENT_CITY_ID, mCurrentCityID);
        super.onDestroy();
    }

    SharedPreferences.OnSharedPreferenceChangeListener onCityPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID.equals(key)) {
                mCurrentCityID = sharedPreferences.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID,-1);
            }
            if (mCurrentCityID != -1 ) {
                mData = null;
                loadData(false);
            }
        }
    };

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (mSoonAdapter == null) {
            return;
        }
        switch (checkedId) {
            case R.id.time:
                mRecyclerView.addItemDecoration(decoration);
                mSoonAdapter.updateSortMode(MovieSoonListAdapter.SORT_TIME);
                mRecyclerView.scrollToPosition(0);
                radioGroupTimer.setVisibility(View.VISIBLE);
                break;

            case R.id.hot:
                mRecyclerView.removeItemDecoration(decoration);
                mSoonAdapter.updateSortMode(MovieSoonListAdapter.SORT_WANT_COUNT);
                mRecyclerView.scrollToPosition(0);
                radioGroupTimer.setVisibility(View.GONE);
                break;
        }
    }

}
