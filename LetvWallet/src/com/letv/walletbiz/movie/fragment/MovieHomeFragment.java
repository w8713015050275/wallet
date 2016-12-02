package com.letv.walletbiz.movie.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.CityListActivity;
import com.letv.walletbiz.movie.activity.MovieTicketActivity;
import com.letv.walletbiz.movie.beans.CityList;
import com.letv.walletbiz.movie.utils.CityListHelper;
import com.letv.walletbiz.movie.utils.MovieSearchHelper;

/**
 * Created by lijujying on 16-6-22.
 */
public class MovieHomeFragment extends BaseFragment {
    private View mRootView;
    private String[] tabArrays;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdpter mViewPagerAdapter;

    private  MovieListFragment movieFragment;
    private  MovieListFragment movieWillFragment;

    /**
     * 定位相关
     */
    //private static final int MSG_CURRENT_CITY_CHANGED = 2;
    private static final int MSG_LOCATION_UPDATE = 3;
    private static final int MSG_OBTAIN_LOCATION_CITY_ID = 4;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    public static final int REQUEST_CODE_SET_CURRENT_CITY = 10;

    private LeBottomSheet mChangeCityDialog;
    private String mCurrentCityName = null;
    private int mCurrentCityID = -1;
    private int cityId = -1;
    private String cityName = null;
    private int mLocationCityId = -1;
    private String mLocationCityname = null;
    private boolean isLocationPermissionRequest = false;

    private LocationHelper mLocationHelper;
    private AsyncTask<String, Void, CityList.City> mLocationTask;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_UPDATE:
                    removeMessages(MSG_LOCATION_UPDATE);
                    if (msg.obj != null) {
                        mLocationCityname = (String) msg.obj;
                        if (mLocationTask == null) {
                            getLocationCityId(mLocationCityname);
                        }
                    }
                    break;

                case MSG_OBTAIN_LOCATION_CITY_ID:
                    CityList.City city = (CityList.City) msg.obj;
                    if (city != null) {
                        mLocationCityname = city.name;
                        mLocationCityId = city.id;
                        if (mLocationCityId != mCurrentCityID && mLocationCityId != -1 && mCurrentCityID != -1) {
                            if (isVisible()) {
                                showChangeCityDialog(getActivity());
                            }
                        } else {
                            mLocationCityId = -1;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setHasOptionsMenu(true);
        SharedPreferencesHelper.getSharePreferences().registerOnSharedPreferenceChangeListener(onCityPreferenceChangeListener);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mRootView) {
            mRootView = inflater.inflate(R.layout.movie_home_fragment, container, false);
            tabArrays = getResources().getStringArray(R.array.movie_list_tab_array);
            mTabLayout = (TabLayout) mRootView.findViewById(R.id.tab_indicator);
            mViewPager = (ViewPager) mRootView.findViewById(R.id.viewpager);
            mViewPagerAdapter = new ViewPagerAdpter(getFragmentManager(), tabArrays);
            mViewPager.setAdapter(mViewPagerAdapter);
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            mTabLayout.setupWithViewPager(mViewPager);
            setTabMarginHorizontal((int) getResources().getDimension(R.dimen.tab_margin_horizontal));

        } else if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeAllViews();
        }
        return mRootView;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocationCityId != mCurrentCityID && mLocationCityId != -1 && mCurrentCityID != -1) {
            showChangeCityDialog(getActivity());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_location) {
            startActivityForResult(new Intent(getActivity(), CityListActivity.class), REQUEST_CODE_SET_CURRENT_CITY);
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            MovieSearchHelper.startSearch(getContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLocationHelper = LocationHelper.getInstance();
        mLocationHelper.addLocationCallback(mLocationCallback);
        updateCurrentCity();
        if (mCurrentCityID == -1) {
            Intent intent = new Intent(getActivity(), CityListActivity.class);
            intent.putExtra("first", true);
            startActivityForResult(intent, REQUEST_CODE_SET_CURRENT_CITY);
            if (getActivity() != null) {
                getActivity().finish();
            }
        } else {
            location();
            showCurrentCity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            if (mCurrentCityID < 0) {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && mLocationCityId != mCurrentCityID && mLocationCityId != -1 && mCurrentCityID != -1) {
            showChangeCityDialog(getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationHelper.removeLocationCallback(mLocationCallback);
        SharedPreferencesHelper.getSharePreferences().unregisterOnSharedPreferenceChangeListener(onCityPreferenceChangeListener);
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            location();
        }
    }

    class ViewPagerAdpter extends FragmentPagerAdapter {
        String[] mTabName;

        public ViewPagerAdpter(FragmentManager fm, String[] mTabName) {
            super(fm);
            this.mTabName = mTabName;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (movieFragment == null) {
                    movieFragment = MovieListFragment.newInstance(false, mCurrentCityID);
                }
                fragment = movieFragment;
            } else if (position == 1) {
                if (movieWillFragment == null) {
                    movieWillFragment = MovieListFragment.newInstance(true, mCurrentCityID);
                }
                fragment = movieWillFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mTabName == null ? 0 : mTabName.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabName[position];
        }
    }

    private void setTabMarginHorizontal(int margin_in_dp) {
        if (tabArrays == null || mTabLayout == null) return;
        for (int i = 0; i < tabArrays.length; i++) {
            LinearLayout layout = ((LinearLayout) ((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(i));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
            layoutParams.leftMargin = margin_in_dp;
            layoutParams.rightMargin = margin_in_dp;
            layout.setLayoutParams(layoutParams);
        }
    }

    private void updateCurrentCity() {
        mCurrentCityID = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
        mCurrentCityName = SharedPreferencesHelper.getString(MovieTicketConstant.PREFERENCES_CURRENT_CITY, null);
    }

    public void showCurrentCity() {
        if (getActivity() instanceof MovieTicketActivity) {
            ((MovieTicketActivity)getActivity()).showCurrentCity(mCurrentCityName);
        }
    }

    private void saveCurrentCity(int cityId, String cityName) {
        SharedPreferencesHelper.putInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, cityId);
        SharedPreferencesHelper.putString(MovieTicketConstant.PREFERENCES_CURRENT_CITY, cityName);
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
                    return CityListHelper.getLocationCityIdByCityName(getActivity(), name);
                }

                @Override
                protected void onPostExecute(CityList.City result) {
                    if (getActivity() == null || getActivity().isFinishing()) {
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

    private void location() {
        int result = PermissionCheckHelper.checkLocationPermission(getActivity(), isLocationPermissionRequest ? -1 : PERMISSIONS_REQUEST_CODE);
        isLocationPermissionRequest = true;
        if (result == PermissionCheckHelper.PERMISSION_ALLOWED) {
            mLocationHelper.getAddress(false);
            mHandler.sendEmptyMessageDelayed(MSG_LOCATION_UPDATE, 15000);
        } else if (result == PermissionCheckHelper.PERMISSION_REFUSED) {
            mHandler.obtainMessage(MSG_LOCATION_UPDATE).sendToTarget();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
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

    private void showChangeCityDialog(Context context) {
        if (context == null || getActivity()==null || getActivity().isFinishing()) {
            return;
        }
        if (mChangeCityDialog == null) {
            mChangeCityDialog = new LeBottomSheet(getActivity());
            mChangeCityDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mChangeCityDialog.dismiss();
                            saveCurrentCity(mLocationCityId, mLocationCityname);
                            mLocationCityId = -1;
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mChangeCityDialog.dismiss();
                            mLocationCityId = -1;
                        }
                    }, null,
                    new String[]{
                            context.getString(R.string.movie_ticket_change_city_btn_confirm),
                            context.getString(R.string.movie_ticket_change_city_btn_cancel)
                    },
                    String.format(context.getString(R.string.movie_ticket_change_city_title), mLocationCityname),
                    null, null, context.getResources().getColor(R.color.colorBtnBlue), false);
        }
        if (!mChangeCityDialog.isShowing()) {
            mChangeCityDialog.show();
        }
    }

    private LocationHelper.LocationCallback mLocationCallback = new LocationHelper.LocationCallback() {

        @Override
        public void onLocationUpdateFinished(Address address) {
            if (getActivity() == null || getActivity().isFinishing()) {
                return;
            }
            Message msg = mHandler.obtainMessage(MSG_LOCATION_UPDATE);
            msg.obj = address == null ? null : address.getLocality();
            mHandler.sendMessage(msg);
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener onCityPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID.equals(key)) {
                cityId = sharedPreferences.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID,-1);
            } else if (MovieTicketConstant.PREFERENCES_CURRENT_CITY.equals(key)) {
                cityName = sharedPreferences.getString(MovieTicketConstant.PREFERENCES_CURRENT_CITY,null);
            }
            if (cityId != -1 && !TextUtils.isEmpty(cityName) && cityId != mCurrentCityID && cityName != mCurrentCityName) {
                updateCurrentCity();
                showCurrentCity();
                cityId = -1;
                cityName = null;
            }
        }
    };
}
