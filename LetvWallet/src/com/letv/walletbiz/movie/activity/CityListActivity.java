package com.letv.walletbiz.movie.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.AlphabetWavesView;
import com.letv.shared.widget.AlphabetWavesView.OnAlphabetListener;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CityList;
import com.letv.walletbiz.movie.ui.ChooseCityHeaderView;
import com.letv.walletbiz.movie.utils.CityListHelper;
import com.letv.walletbiz.movie.utils.CityListLoadTask;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;

import java.util.Arrays;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import timehop.stickyheader.RecyclerItemClickListener;
import timehop.stickyheader.StickyRecyclerHeadersAdapter;
import timehop.stickyheader.StickyRecyclerHeadersDecoration;
import timehop.stickyheader.StickyRecyclerHeadersTouchListener;

/**
 * Created by liuliang on 16-1-4.
 */
public class CityListActivity extends BaseWalletFragmentActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private LocationHelper mLocationHelper;

    public static String[] ALPHABET = {"#", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    private PtrClassicFrameLayout mPtrFrameLayout;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CityListAdapter mAdapter;
    private StickyRecyclerHeadersDecoration decoration;

    private ChooseCityHeaderView mHeaderView;
    private View.OnClickListener mOnHeaderItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int cityId = -1;
            String cityName = null;
            if (R.id.geocity == v.getId()) {
                cityId = mLocationCityId;
                cityName = mLocationCityname;
            } else {
                Object object = v.getTag();
                if (object != null && object instanceof CityList.City) {
                    CityList.City city = (CityList.City) object;
                    cityId = city.id;
                    cityName = city.name;
                }
            }

            if (cityId != -1 && !TextUtils.isEmpty(cityName)) {
                Action.uploadSet(Action.MOVIE_CITY_CLICK, cityName);
                saveCurrentCity(cityId, cityName);
                mSelectCityId = cityId;
                if (isFirst) {
                    goToMovieTicketMain();
                }
                finish();
            }
        }
    };

    private AlphabetWavesView mAlphabetWavesView;
    private OnAlphabetListener mAlphabetListener = new OnAlphabetListener() {

        @Override
        public void onAlphabetChanged(int alphabetPosition, String firstAlphabet) {
            int position = mAdapter.getPositionForSection(firstAlphabet.charAt(0));
            if (position != -1) {
                moveToPosition(position);
            }
        }
    };

    private AsyncTask<String, Void, CityList.City> mLocationTask;
    private String mLocationCityname;
    private int mLocationCityId = -1;
    private boolean isLocationPermissionRequest = false;

    private int mSelectCityId = -1;

    private CityListLoadTask mLoadTask;

    private boolean mScrolling = false;

    private boolean mRecyclerViewMoving = false;
    private int mScrollToPosition = -1;

    private CityList mCityList;

    private MovieCommonCallback<CityList> mCallback = new MovieCommonCallback<CityList>() {

        @Override
        public void onLoadFinished(CityList result, int errorCode) {
            if (isFinishing()) {
                return;
            }
            if (errorCode == MovieCommonCallback.NO_ERROR) {
                hideBlankPage();
                mAlphabetWavesView.setVisibility(View.VISIBLE);
                if (result != null && result.list != null) {
                    mCityList = result;
                    mAdapter.setData(result.list);
                    if (mHeaderView != null) {
                        mHeaderView.setHotCityArray(result.hot);
                    }
                }
            } else if (errorCode == MovieCommonCallback.ERROR_NETWORK) {
                showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(mRetryClickListener);
            } else if (errorCode == MovieCommonCallback.ERROR_NO_NETWORK) {
                showBlankPage(BlankPage.STATE_NO_NETWORK);
            }
            if (mPtrFrameLayout.isRefreshing()) {
                mPtrFrameLayout.refreshComplete();
            }
            mLoadTask = null;
            mHandler.sendEmptyMessage(MSG_LOAD_CITY_LIST_FINISHED);
        }
    };

    private LocationHelper.LocationCallback mLocationCallback = new LocationHelper.LocationCallback() {

        @Override
        public void onLocationUpdateFinished(Address address) {
            if (isFinishing()) {
                return;
            }
            Message msg = mHandler.obtainMessage(MSG_LOCATION_UPDATE);
            msg.obj = address == null ? null : address.getLocality();
            mHandler.sendMessage(msg);
        }
    };

    private static final int MSG_LOCATION_UPDATE = 1;
    private static final int MSG_OBTAIN_LOCATION_CITY_ID = 2;
    private static final int MSG_LOAD_CITY_LIST_FINISHED = 3;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_UPDATE:
                    removeMessages(MSG_LOCATION_UPDATE);
                    String cityName = (String) msg.obj;
                    if (cityName == null) {
                        mHeaderView.setGeoCity(R.string.movie_city_location_failure);
                    } else {
                        mLocationCityname = cityName;
                        if (mLoadTask == null) {
                            getLocationCityId(cityName);
                        }
                    }
                    break;
                case MSG_OBTAIN_LOCATION_CITY_ID:
                    CityList.City city = (CityList.City) msg.obj;
                    if (city == null) {
                        mHeaderView.setGeoCity(R.string.movie_city_location_failure);
                    } else {
                        mLocationCityname = city.name;
                        mLocationCityId = city.id;
                        mHeaderView.setGeoCity(mLocationCityname);
                    }
                    break;
                case MSG_LOAD_CITY_LIST_FINISHED:
                    if (!TextUtils.isEmpty(mLocationCityname) && mLocationCityId == -1) {
                        getLocationCityId(mLocationCityname);
                    }
                    break;
            }
        }
    };

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isNetworkAvailable()) {
                hideBlankPage();
                loadData(false);
                location();
            }
        }
    };

    private boolean isFirst = false;

    private int mTabId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.movie_city_list);
        Intent intent = getIntent();
        if (intent != null) {
            isFirst = intent.getBooleanExtra("first", false);
            mTabId = intent.getIntExtra(MovieTicketConstant.EXTRA_MOVIE_TICKET_TAB_ID, -1);
        }

        mSelectCityId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);

        mLocationHelper = LocationHelper.getInstance();
        mLocationHelper.addLocationCallback(mLocationCallback);

        mPtrFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_header_view);
        mPtrFrameLayout.setLastUpdateTimeRelateObject(this);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, mRecyclerView, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                loadData(true);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mHeaderView = new ChooseCityHeaderView(this);
        mHeaderView.setOnHeaderItemClickListener(mOnHeaderItemClickListener);

        mHeaderView.setGeoCity(R.string.movie_city_locating);

        mAdapter = new CityListAdapter(this);
        mAdapter.setHeaderView(mHeaderView);
        mRecyclerView.setAdapter(mAdapter);

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                CityList.City city = mAdapter.getItem(position);
                if (city != null) {
                    Action.uploadSet(Action.MOVIE_CITY_CLICK, city.name);
                    saveCurrentCity(city.id, city.name);
                    mSelectCityId = city.id;
                    if (isFirst) {
                        goToMovieTicketMain();
                    }
                    finish();
                }
            }
        });

        decoration = new StickyRecyclerHeadersDecoration(mAdapter);
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
        mRecyclerView.addItemDecoration(decoration);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, getResources().getColor(R.color.colorDividerLineBg),
                DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        RecyclerViewScrollListener listener = new RecyclerViewScrollListener();
        mRecyclerView.addOnScrollListener(listener);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                decoration.invalidateHeaders();
            }
        });

        mAlphabetWavesView = (AlphabetWavesView) findViewById(R.id.list_alphabet_index);
        mAlphabetWavesView.setOnAlphabetListener(mAlphabetListener);
        mAlphabetWavesView.setAlphabetList(Arrays.asList(ALPHABET));
        mAlphabetWavesView.setSelection(0);
        mAlphabetWavesView.setTextDefaultColor(getColor(R.color.movie_primary_tv_color));
        loadData(false);
        location();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && mCityList == null) {
            hideBlankPage();
            loadData(false);
            location();
        }
    }

    private void location() {
        int result = PermissionCheckHelper.checkLocationPermission(this, isLocationPermissionRequest ? -1 : PERMISSIONS_REQUEST_CODE);
        isLocationPermissionRequest = true;
        if (result == PermissionCheckHelper.PERMISSION_ALLOWED) {
            mLocationHelper.getAddress(false);
            mHandler.sendEmptyMessageDelayed(MSG_LOCATION_UPDATE, 15000);
        } else if (result == PermissionCheckHelper.PERMISSION_REFUSED) {
            mHandler.obtainMessage(MSG_LOCATION_UPDATE).sendToTarget();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location();
                } else {
                    mHandler.obtainMessage(MSG_LOCATION_UPDATE).sendToTarget();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelectCityId == -1 && mLocationCityId != -1 && !TextUtils.isEmpty(mLocationCityname)) {
            saveCurrentCity(mLocationCityId, mLocationCityname);
            if (isFirst) {
                goToMovieTicketMain();
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_LOCATION_UPDATE);
        }
        mLocationHelper.removeLocationCallback(mLocationCallback);
        super.onDestroy();
    }

    private void goToMovieTicketMain() {
        Intent intent = new Intent(this, MovieTicketActivity.class);
        if (mTabId != -1) {
            intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_TICKET_TAB_ID, mTabId);
        }
        startActivity(intent);
    }

    private void saveCurrentCity(int cityId, String cityName) {
        SharedPreferencesHelper.putInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, cityId);
        SharedPreferencesHelper.putString(MovieTicketConstant.PREFERENCES_CURRENT_CITY, cityName);
        setResult(RESULT_OK);
    }

    private void loadData(boolean isLoadFromNet) {
        if (isLoadFromNet && !isNetworkAvailable() && mCityList != null) {
            Toast.makeText(this, R.string.empty_no_network, Toast.LENGTH_SHORT).show();
            if (mPtrFrameLayout != null) {
                mPtrFrameLayout.refreshComplete();
            }
            return;
        }
        if (mLoadTask == null) {
            mLoadTask = new CityListLoadTask(this, mCallback);
            mLoadTask.execute(isLoadFromNet);
        }
    }

    private void getLocationCityId(String cityName) {
        if (mLocationCityId != -1) {
            return;
        }
        if (mLocationTask == null) {
            mLocationTask = new AsyncTask<String, Void, CityList.City>() {

                @Override
                protected CityList.City doInBackground(String... params) {
                    String name = params[0];
                    return CityListHelper.getLocationCityIdByCityName(CityListActivity.this, name);
                }

                @Override
                protected void onPostExecute(CityList.City result) {
                    if (isFinishing()) {
                        return;
                    }
                    Message msg = mHandler.obtainMessage(MSG_OBTAIN_LOCATION_CITY_ID);
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            };
            mLocationTask.execute(cityName);
        }
    }

    private void moveToPosition(int position) {
        int firstItem = mLayoutManager.findFirstVisibleItemPosition();
        int lastItem = mLayoutManager.findLastVisibleItemPosition();

        if (position <= firstItem) {
            mRecyclerView.scrollToPosition(position);
        } else if (position <= lastItem) {
            int top = mRecyclerView.getChildAt(position - firstItem).getTop();
            mRecyclerView.scrollBy(0, top);
        } else {
            mScrollToPosition = position;
            mRecyclerView.scrollToPosition(position);
            mRecyclerViewMoving = true;
        }
    }

    class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                mScrolling = false;
            } else {
                mScrolling = true;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mRecyclerViewMoving) {
                mRecyclerViewMoving = false;
                int position = mScrollToPosition - mLayoutManager.findFirstVisibleItemPosition();
                if (0 <= position && position < mRecyclerView.getChildCount()) {
                    int top = mRecyclerView.getChildAt(position).getTop();
                    mRecyclerView.scrollBy(0, top);
                }
            } else {
                int section = (char) mAdapter.getSectionForPosition(mLayoutManager.findFirstVisibleItemPosition());
                int index = 0;
                if (section != -1) {
                    index = Arrays.asList(ALPHABET).indexOf(String.valueOf((char) section));
                }
                try {
                    if (mAlphabetWavesView != null && mScrolling) {
                        mAlphabetWavesView.setSelection(index);
                    }
                } catch (Exception e) {
                }
            }

        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(View itemView) {
            super(itemView);
        }
    }

    static class SectionHolder extends RecyclerView.ViewHolder {

        public SectionHolder(View itemView) {
            super(itemView);
        }
    }

    class CityListAdapter extends RecyclerView.Adapter<ItemHolder>
            implements StickyRecyclerHeadersAdapter, SectionIndexer {

        private static final int TYPE_NORMAL = 1;
        private static final int TYPE_HEADER = 2;
        private static final int TYPE_FOOTER = 3;

        private View mHeaderView;
        private View mFooterView;

        private Context mContext;
        private CityList.City[] mCityArray;

        public CityListAdapter(Context context) {
            mContext = context;
            setHasStableIds(true);
        }

        public void setData(CityList.City[] cityArray) {
            mCityArray = cityArray;
            notifyDataSetChanged();
        }

        public void setHeaderView(View header) {
            mHeaderView = header;
        }

        private boolean hasHeaderView() {
            return mHeaderView != null;
        }

        public void setFooterView(View footer) {
            mFooterView = footer;
        }

        public boolean hasFooterView() {
            return mFooterView != null;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_NORMAL) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.movie_city_list_item, parent, false);
                return new ItemHolder(view);
            } else if (viewType == TYPE_HEADER) {
                return new ItemHolder(mHeaderView);
            } else if (viewType == TYPE_FOOTER) {
                return new ItemHolder(mFooterView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            int itemType = getItemViewType(position);
            if (itemType == TYPE_NORMAL) {
                TextView textView = (TextView) holder.itemView;
                textView.setText(mCityArray[getRealPositionForData(position)].name);
            } else if (itemType == TYPE_HEADER) {
                //
            }
        }

        @Override
        public long getHeaderId(int position) {
            if (mCityArray == null) {
                return -1;
            }
            if (hasHeaderView() && position == 0) {
                return -1;
            }
            return mCityArray[getRealPositionForData(position)].pinyin.toUpperCase().charAt(0);
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_city_list_section, parent, false);
            return new SectionHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            textView.setText(String.valueOf(mCityArray[getRealPositionForData(position)].pinyin.charAt(0)).toUpperCase());
        }

        private int getRealPositionForData(int position) {
            int realPos = position;
            if (hasHeaderView()) {
                realPos--;
            }
            return realPos;
        }

        @Override
        public int getItemCount() {
            int extraCount = 0;
            if (hasHeaderView()) {
                extraCount++;
            }
            if (hasFooterView()) {
                extraCount++;
            }
            return mCityArray == null ? 0 : mCityArray.length + extraCount;
        }

        @Override
        public long getItemId(int position) {
            if (hasHeaderView() && position == 0) {
                return -1;
            } else if (hasFooterView() && position == (getItemCount() - 1)) {
                return -1;
            } else {
                return mCityArray == null ? -1 : mCityArray[getRealPositionForData(position)].id;
            }
        }

        public CityList.City getItem(int position) {
            if (hasHeaderView() && position == 0) {
                return null;
            } else if (hasFooterView() && position == (getItemCount() - 1)) {
                return null;
            } else {
                return mCityArray == null ? null : mCityArray[getRealPositionForData(position)];
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (hasHeaderView() && position == 0) {
                return TYPE_HEADER;
            } else if (hasFooterView() && position == (getItemCount() - 1)) {
                return TYPE_FOOTER;
            } else {
                return TYPE_NORMAL;
            }
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            if (mCityArray == null) {
                return -1;
            }
            if (sectionIndex == '#') {
                return 0;
            }
            for (int i = 0; i < mCityArray.length; i++) {
                if (mCityArray[i].pinyin.toUpperCase().charAt(0) == sectionIndex) {
                    if (hasHeaderView()) {
                        i++;
                    }
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (mCityArray == null) {
                return -1;
            }
            if (position == 0) {
                return '#';
            } else {
                return mCityArray[getRealPositionForData(position)].pinyin.toUpperCase().charAt(0);
            }
        }
    }
}
