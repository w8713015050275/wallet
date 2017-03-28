package com.letv.walletbiz.movie.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieScheduleActivity;
import com.letv.walletbiz.movie.activity.MovieTicketActivity;
import com.letv.walletbiz.movie.beans.CinemaFilterBean;
import com.letv.walletbiz.movie.beans.CinemaList;
import com.letv.walletbiz.movie.ui.CinemaFilterPopupWindow;
import com.letv.walletbiz.movie.ui.MovieTabFlowLayout;
import com.letv.walletbiz.movie.utils.CinemaListHelper;
import com.letv.walletbiz.movie.utils.CinemaListTask;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MovieSearchHelper;
import com.letv.walletbiz.movie.utils.MovieTicketHelper;

import java.util.Arrays;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 15-12-30.
 */
public class CinemaListFragment extends BaseFragment implements MovieCommonCallback<CinemaList>,AccountHelper.OnAccountChangedListener {

    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_MOVIE_ID = "movie_id";

    public static final int PERMISSIONS_REQUEST_CODE = 1;
    public static final int PERMISSIONS_REQUEST_CODE_FORCE = 2;

    private View mCinemaFilterContainer;
    private TextView mCinemaFilterTextView;
    private TextView mCinemaFilterCancelButton;

    private PtrClassicFrameLayout mPtrFrameLayout;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private CinemaListAdapter mAdapter;

    private TextView mLocationDesc;
    private LinearLayout mLocationContainer;
    private CinemaFilterPopupWindow mFilterWindow;

    private int mCurrentCityId = -1;
    private long mMovieId = -1;
    private String mDate = null;

    private CinemaListTask mTask;

    private CinemaList mCinemaList;
    private CinemaList.Cinema[] mCurrentCinemaArray;
    private int mCurrentCategory = -1;
    private String mCurrentSecCategoryName;
    private CinemaFilterBean mFilterBean;

    private boolean isLocationPermissionRequested = false;
    private LocationHelper mLocationHelper;
    private Address mAddress;
    private int mPermissionState = PermissionCheckHelper.PERMISSION_REFUSED;
    private String mCurrentCityName = null;
    private int cityId = -1;
    private String cityName = null;
    private boolean isDataExpire = false;
    private MenuItem menuItem;

    private LocationHelper.LocationCallback mLocationCallback = new LocationHelper.LocationCallback() {

        @Override
        public void onLocationUpdateFinished(Address address, int responseCode) {
            if (address != null) {
                mAddress = address;
                updateLocationDescView(true);
                updateDiatanceAndSortAsyn();
            } else {
                updateLocationDescView(false);
            }
        }
    };

    private static final int MSG_LOAD_FINISHED = 1;
    private static final int MSG_STOP_REFRESH = 4;
    public static final int REQUEST_UPDATE_DATA = 10;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (isDetached() || !isAdded()) {
                return;
            }
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    int erroCode = msg.arg1;
                    hideLoadingView();
                    CinemaList cinemaList = (CinemaList) msg.obj;
                    if (erroCode == MovieCommonCallback.NO_ERROR) {
                        if (mCurrentCinemaArray != null && mCurrentCinemaArray.length > 0) {
                            hideBlankPage();
                            if (mAddress != null) {
                                updateDiatanceAndSortAsyn();
                            } else {
                                updateView();
                            }
                        } else {
                            BlankPage blankPage = showBlankPage();
                            if (blankPage != null) {
                                blankPage.setCustomPage(getString(R.string.movie_error_no_cinema), BlankPage.Icon.NO_ACCESS);
                            }
                        }
                    } else if (erroCode == MovieCommonCallback.ERROR_NETWORK) {
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
                    } else if (erroCode == MovieCommonCallback.ERROR_NO_NETWORK) {
                        showBlankPage(BlankPage.STATE_NO_NETWORK);
                    }
                    updateMenuIcon(mFilterBean == null ? R.drawable.ic_menu_filter_disabled : R.drawable.ic_menu_filter);
                    break;
                case MSG_STOP_REFRESH:
                    if (mPtrFrameLayout != null && mPtrFrameLayout.isRefreshing()) {
                        mPtrFrameLayout.refreshComplete();
                    }
                    break;
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.location_container:
                    location(true);
                    break;
                case R.id.filter_cancel:
                    cancelCinemaFilter();
                    mFilterWindow = null;
                    break;
            }
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID.equals(key)) {
                cityId = sharedPreferences.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
            } else if (MovieTicketConstant.PREFERENCES_CURRENT_CITY.equals(key)) {
                cityName = sharedPreferences.getString(MovieTicketConstant.PREFERENCES_CURRENT_CITY, null);
            }
            if (cityId != -1 && !TextUtils.isEmpty(cityName) && cityId != mCurrentCityId && cityName != mCurrentCityName) {
                cancelCinemaFilter();
                mFilterWindow = null;
                mCinemaList = null;
                mCurrentCinemaArray = null;
                updateCurrentCity();
                showCurrentCity();
                loadData(false);
                cityId = -1;
                cityName = null;
            }
        }
    };

    public void setLocationPermissionRequested(boolean requested) {
        isLocationPermissionRequested = requested;
    }

    public void showCurrentCity() {
        if (getActivity() instanceof MovieTicketActivity) {
            ((MovieTicketActivity)getActivity()).showCurrentCity(mCurrentCityName);
        }
    }

    private void updateCurrentCity() {
        mCurrentCityId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
        mCurrentCityName = SharedPreferencesHelper.getString(MovieTicketConstant.PREFERENCES_CURRENT_CITY, null);
    }

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loadData(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMovieId = bundle.getLong(EXTRA_MOVIE_ID, -1);
            mDate = bundle.getString(EXTRA_DATE, null);
            mCurrentCityId = bundle.getInt(MovieTicketConstant.EXTRA_CITY_ID, -1);
        }
        Activity activity = getActivity();
        if ((activity instanceof MovieTicketActivity) && mCurrentCityId == -1 && activity != null) {
            mCurrentCityId = ((MovieTicketActivity) activity).getCityId();
            mCurrentCityName = ((MovieTicketActivity) activity).getCityName();
            if (TextUtils.isEmpty(mCurrentCityName)) {
                mCurrentCityId = -1;
            }
        }
        if (mCurrentCityId == -1) {
            updateCurrentCity();
        }
        SharedPreferencesHelper.getSharePreferences().registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        mLocationHelper = LocationHelper.getInstance();
        mLocationHelper.addLocationCallback(mLocationCallback);
        setHasOptionsMenu(true);
        showCurrentCity();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        registerCinemaFavoritesChangeReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location(false);
                } else {
                    mPermissionState = PermissionCheckHelper.PERMISSION_REFUSED;
                    updateLocationDescView(false);
                }
                break;
            case PERMISSIONS_REQUEST_CODE_FORCE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location(true);
                } else {
                    mPermissionState = PermissionCheckHelper.PERMISSION_REFUSED;
                    updateLocationDescView(false);
                }
                break;
        }
    }

    private boolean location(boolean force) {
        int requestCode;
        if (!force && isLocationPermissionRequested) {
            requestCode = -1;
        } else {
            requestCode = force ? PERMISSIONS_REQUEST_CODE_FORCE : PERMISSIONS_REQUEST_CODE;
        }
        int result = PermissionCheckHelper.checkLocationPermission(getActivity(), requestCode);
        isLocationPermissionRequested = true;
        mPermissionState = result;
        if (result == PermissionCheckHelper.PERMISSION_ALLOWED) {
            if (isNetworkAvailable()) {
                mLocationDesc.setText(R.string.movie_city_locating);
                mLocationHelper.getAddress(force);
            } else {
                mLocationDesc.setText(R.string.movie_city_location_failure);
            }
            return true;
        } else if (result == PermissionCheckHelper.PERMISSION_REFUSED) {
            updateLocationDescView(false);
        }
        return false;
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_cinema_list, container, false);

        mCinemaFilterContainer = view.findViewById(R.id.cinema_filter_container);
        mCinemaFilterTextView = (TextView) view.findViewById(R.id.cinema_filter_text);
        mCinemaFilterCancelButton = (TextView) view.findViewById(R.id.filter_cancel);
        mCinemaFilterCancelButton.setOnClickListener(mOnClickListener);

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
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new CinemaListAdapter(getContext());
        if (mCurrentCinemaArray != null) {
            mAdapter.setData(mCurrentCinemaArray);
        }
        mRecyclerView.setAdapter(mAdapter);

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                CinemaList.Cinema cinema = mAdapter.getItem(position);
                if (cinema == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), MovieScheduleActivity.class);
                intent.putExtra(MovieTicketConstant.EXTRA_CINEMA_ID, cinema.id);
                intent.putExtra(MovieTicketConstant.EXTRA_CINEMA_NAME, cinema.name);
                intent.putExtra(MovieTicketConstant.EXTRA_CITY_ID, mCurrentCityId);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, mMovieId);
                intent.putExtra(MovieTicketConstant.EXTRA_DATE, mDate);
                intent.putExtra(MovieTicketConstant.MOVIE_PARAM_FAVORITES, cinema.is_favorite);
                startActivityForResult(intent, REQUEST_UPDATE_DATA);
                Action.uploadClick(Action.MOVIE_DETAIL_CINEMA, String.valueOf(cinema.id), String.valueOf(cinema.distance));
            }
        });
        mRecyclerView.addOnItemTouchListener(itemClickListener);

        mLocationContainer = (LinearLayout) view.findViewById(R.id.location_container);
        mLocationContainer.setOnClickListener(mOnClickListener);
        mLocationDesc = (TextView) view.findViewById(R.id.location_desc);
        mPermissionState = PermissionCheckHelper.checkLocationPermission(getActivity(), -1);
        if (mAddress != null) {
            updateLocationDescView(true);
        }
        updateFilterView();
        return view;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if ((mPtrFrameLayout != null && mPtrFrameLayout.isRefreshing()) || mCurrentCinemaArray == null) {
                hideBlankPage();
                loadData(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCinemaList == null) {
            loadData(false);
        } else if(isDataExpire){
            loadData(true);
        }
        if (mAddress == null) {
            location(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_cinema_list, menu);
        menuItem = menu.findItem(R.id.action_filter);
        updateMenuIcon(mFilterBean == null ? R.drawable.ic_menu_filter_disabled : R.drawable.ic_menu_filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            if (mCinemaList == null) {
                return super.onOptionsItemSelected(item);
            }
            menuItem = item;
            showFilterDialog();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            MovieSearchHelper.startSearch(getContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMenuIcon(int resId){
        if (menuItem != null && resId > 0) {
            menuItem.setIcon(resId);
        }
    }

    @Override
    public void onDestroy() {
        mAdapter = null;
        SharedPreferencesHelper.getSharePreferences().unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        mLocationHelper.removeLocationCallback(mLocationCallback);
        unRegisterCinemaFavoritesChangeReceiver();
        super.onDestroy();
    }

    @Override
    public void onLoadFinished(CinemaList result, int errorCode) {
        if (errorCode == NO_ERROR) {
            mCinemaList = result;
            updateCurrentCinemaArray();
            isDataExpire = false;
        }
        Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
        msg.arg1 = errorCode;
        msg.obj = result;
        msg.sendToTarget();
        mHandler.sendEmptyMessage(MSG_STOP_REFRESH);
        mTask = null;
    }

    private void updateCurrentCinemaArray() {
        if (mCinemaList == null) {
            mFilterBean = null;
            mCurrentCinemaArray = null;
            return;
        }
        mFilterBean = CinemaListHelper.getCinemaFilterData(getContext(), mCinemaList);
        if (mCurrentCategory != -1 && mFilterBean != null) {
            List<CinemaList.Cinema> list = mFilterBean.getCinemaList(mCurrentCategory, mCurrentSecCategoryName);
            if (list != null) {
                mCurrentCinemaArray = list.toArray(new CinemaList.Cinema[0]);
                Arrays.sort(mCurrentCinemaArray);
            } else {
                mCurrentCinemaArray = null;
            }
        } else {
            mCurrentCinemaArray = mCinemaList.cinema_list;
        }
    }

    private void loadData(boolean isLoadFromNet) {
        if (!isNetworkAvailable() && isLoadFromNet && mCinemaList != null) {
            Toast.makeText(getContext(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
            if (mPtrFrameLayout != null) {
                mPtrFrameLayout.refreshComplete();
            }
            return;
        }
        if (mTask == null) {
            if(mCinemaList == null){
                showLoadingView();
            }
            int queryType = mMovieId > 0 ? CinemaListHelper.TYPE_CINEMA_BY_MOVIE : CinemaListHelper.TYPE_CINEMA_BY_CITY;
            mTask = new CinemaListTask(getContext(), this);
            if (queryType == CinemaListHelper.TYPE_CINEMA_BY_MOVIE) {
                mTask.setParam(queryType, isLoadFromNet, mCurrentCityId, mMovieId, mDate);
            } else {
                mTask.setParam(queryType, isLoadFromNet, mCurrentCityId);
            }
            ExecutorHelper.getExecutor().runnableExecutor(mTask);
        }
    }

    private void updateDiatanceAndSortAsyn() {
        if (mCinemaList == null || mAddress == null || mCurrentCinemaArray == null) {
            return;
        }
        if (mPermissionState == PermissionCheckHelper.PERMISSION_REFUSED) {
            if (mAdapter != null) {
                mAdapter.setData(mCurrentCinemaArray);
            }
            return;
        }
        if (mAddress == null || mPermissionState == PermissionCheckHelper.PERMISSION_REQUESTING) {
            return;
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                CinemaListHelper.updateDistanceForCinemaList(mCinemaList.cinema_list, mAddress);
                Arrays.sort(mCurrentCinemaArray);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mAdapter != null) {
                    mAdapter.setData(mCurrentCinemaArray);
                }
            }
        };
        task.execute();
    }

    private void updateView() {
        if (mAdapter != null) {
            mAdapter.setData(mCurrentCinemaArray);
        }
    }

    private void updateLocationDescView(boolean success) {
        if (!success) {
            mLocationDesc.setText(R.string.movie_city_location_failure);
            return;
        }
        if (mAddress != null && mLocationDesc != null) {
            StringBuilder builder = new StringBuilder();
            int index = mAddress.getMaxAddressLineIndex();
            if (index < 0) {
                mLocationDesc.setText(R.string.movie_city_location_failure);
                return;
            }
            builder.append(mAddress.getAddressLine(0));
            if (index >= 1) {
                builder.append(mAddress.getAddressLine(1));
            }
            mLocationDesc.setText(builder);
        }
    }

    private void showFilterDialog() {
        if (mFilterWindow == null) {
            mFilterWindow = new CinemaFilterPopupWindow(getContext());
            mFilterWindow.setOnCategoryChangedListener(new CinemaFilterPopupWindow.OnCategoryChangedListener() {

                @Override
                public void onCategoryChanged(int category, String secCategoryName, CinemaList.Cinema[] cinemaArray) {
                    if (cinemaArray != null) {
                        mCurrentCategory = category;
                        mCurrentSecCategoryName = secCategoryName;
                        mCurrentCinemaArray = cinemaArray;
                        Arrays.sort(mCurrentCinemaArray);
                        updateFilterView();
                        mAdapter.setData(mCurrentCinemaArray);
                    }
                    if (getString(R.string.movie_cinema_filter_category_all).equals(secCategoryName)) {
                        cancelCinemaFilter();
                    }
                    if (mFilterWindow != null) {
                        mFilterWindow.dismiss();
                    }
                }
            });
            mFilterWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (menuItem != null) {
                        menuItem.setIcon(R.drawable.ic_menu_filter);
                    }
                }
            });
        }
        if (mFilterWindow.isShowing()) {
            return;
        }
        if (getActivity() != null && getActivity() instanceof BaseFragmentActivity) {
            BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
            Toolbar toolbar = activity.getToolbar();
            if (toolbar != null) {
                mFilterWindow.setData(mFilterBean);
                mFilterWindow.showAsDropDown(toolbar);
                if(mFilterWindow.isShowing()){
                    updateMenuIcon(R.drawable.ic_menu_filter_selected);
                }
            }
        }
    }

    private void updateFilterView() {
        if (!TextUtils.isEmpty(mCurrentSecCategoryName) && mCurrentCategory != -1) {
            mCinemaFilterContainer.setVisibility(View.VISIBLE);
            mCinemaFilterTextView.setText(getString(R.string.movie_cinema_filter_text_formatter, mCurrentSecCategoryName));
        } else {
            mCinemaFilterContainer.setVisibility(View.GONE);
        }
    }

    private void cancelCinemaFilter() {
        mCinemaFilterContainer.setVisibility(View.GONE);
        mCurrentCategory = -1;
        mCurrentSecCategoryName = null;
        if (mCinemaList != null) {
            mCurrentCinemaArray = mCinemaList.cinema_list;
        } else {
            mCurrentCinemaArray = null;
        }
        if (mAdapter != null) {
            mAdapter.setData(mCurrentCinemaArray);
        }
    }

    @Override
    public void onAccountLogin() {
        isDataExpire = true;
    }

    @Override
    public void onAccountLogout() {
        isDataExpire = true;
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public TextView minPriceView;
        public TextView addressView;
        public TextView distanceView;
        public MovieTabFlowLayout tagContainer;
        public ImageView imgFavorites;

        public ItemHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.cinema_name);
            minPriceView = (TextView) itemView.findViewById(R.id.min_price);
            addressView = (TextView) itemView.findViewById(R.id.cinema_address);
            distanceView = (TextView) itemView.findViewById(R.id.cinema_distance);
            tagContainer = (MovieTabFlowLayout) itemView.findViewById(R.id.cinema_tag_container);
            imgFavorites = (ImageView) itemView.findViewById(R.id.img_favorites);
        }
    }

    class CinemaListAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private CinemaList.Cinema[] mCinemaArray;

        public CinemaListAdapter(Context context) {
            mContext = context;
        }

        public void setData(CinemaList.Cinema[] data) {
            mCinemaArray = data;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_cinema_list_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            CinemaList.Cinema cinema = mCinemaArray[position];
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.nameView.setText(cinema.name);
            itemHolder.addressView.setText(cinema.addr);
            itemHolder.minPriceView.setText(MovieTicketHelper.getTicketPriceStr(cinema.ticket_min_price, getString(R.string.movie_min_price), 10));
            if (mPermissionState == PermissionCheckHelper.PERMISSION_ALLOWED && cinema.distance > 0) {
                itemHolder.distanceView.setVisibility(View.VISIBLE);
                itemHolder.distanceView.setText(getDistanceStr(cinema.distance));
            } else {
                itemHolder.distanceView.setVisibility(View.GONE);
            }
            LayoutInflater inflater = LayoutInflater.from(mContext);
            TextView child;
            itemHolder.tagContainer.removeAllViews();
            for (String temp : cinema.special) {
                child = (TextView) inflater.inflate(R.layout.movie_special_item, itemHolder.tagContainer, false);
                child.setText(temp);
                itemHolder.tagContainer.addView(child);
            }
            if (!TextUtils.isEmpty(cinema.discount_des)) {
                int padingLeft = getResources().getDimensionPixelSize(R.dimen.movie_cinema_list_item_special_padding_horizon);
                child = (TextView) inflater.inflate(R.layout.movie_special_item, itemHolder.tagContainer, false);
                child.setBackgroundResource(R.drawable.movie_special_orange_bg);
                child.setPadding(padingLeft, 0, padingLeft, 0);
                child.setText(cinema.discount_des);
                child.setTextColor(getResources().getColor(R.color.movie_cinema_special_discount_text_color, getContext().getTheme()));
                itemHolder.tagContainer.addView(child);
            }
            if (position == mCinemaArray.length - 1) {
                itemHolder.itemView.setPadding(
                        itemHolder.itemView.getPaddingLeft(), itemHolder.itemView.getPaddingTop(), itemHolder.itemView.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.movie_cinema_list_item_padding_bottom_extra));
            } else {
                itemHolder.itemView.setPadding(
                        itemHolder.itemView.getPaddingLeft(), itemHolder.itemView.getPaddingTop(), itemHolder.itemView.getPaddingRight(), 0);
            }
            if (AccountHelper.getInstance().isLogin(getActivity())) {
                int visibility = cinema.is_favorite == MovieTicketConstant.MOVIE_PARAM_FAVORITED ? View.VISIBLE : View.GONE;
                itemHolder.imgFavorites.setVisibility(visibility);
            } else {
                itemHolder.imgFavorites.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mCinemaArray == null ? 0 : mCinemaArray.length;
        }

        public CinemaList.Cinema getItem(int position) {
            if (mCinemaArray == null || position < 0 || position >= mCinemaArray.length) {
                return null;
            }
            return mCinemaArray[position];
        }

        private CharSequence getDistanceStr(double distance) {
            String result = null;
            String unit = "m";
            if (distance < 500) {
                result = String.format("%.0f", distance);
                unit = " m";
            } else if (distance < 10000) {
                result = String.format("%.1f", distance / 1000);
                unit = " km";
            } else {
                result = String.format("%.0f", distance / 1000);
                unit = " km";
            }
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append(result);
            stringBuilder.append(unit, new AbsoluteSizeSpan(9, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return stringBuilder;
        }
    }

    public static final String ACTION_CINEMA_FAVORITES_CHANGE = "com.letv.wallet.movie.CINEMA_FAVORITES_CHANGE";
    private boolean isRegisteredFavoritesChangeReceiver = false;

    private BroadcastReceiver mCinemaFavoritesChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_CINEMA_FAVORITES_CHANGE.equals(action)) {
                isDataExpire = true;
            }
        }
    };

    private void registerCinemaFavoritesChangeReceiver() {
        if (getContext() == null) {
            return ;
        }
        IntentFilter filter = new IntentFilter(ACTION_CINEMA_FAVORITES_CHANGE);
        getContext().registerReceiver(mCinemaFavoritesChangeReceiver, filter);
        isRegisteredFavoritesChangeReceiver = true;
    }

    private void unRegisterCinemaFavoritesChangeReceiver() {
        try {
            if (isRegisteredFavoritesChangeReceiver && getContext() != null) {
                getContext().unregisterReceiver(mCinemaFavoritesChangeReceiver);
            }
        } catch (Exception e) {
        }
        isRegisteredFavoritesChangeReceiver = false;
    }


}
