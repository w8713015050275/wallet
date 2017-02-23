package com.letv.walletbiz.main.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.letv.shared.widget.LeLicenceDialog;
import com.letv.shared.widget.LeNeverPermissionRequestDialog;
import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.R;
import com.letv.walletbiz.update.util.UpdateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuchuntao on 16-12-21.
 */
public abstract class MainFragment extends BaseFragment {


    private LeLicenceDialog mLeLicenceDialog;
    private LeNeverPermissionRequestDialog mLeNeverPermissionRequestDialog = null;

    private boolean displayLicence = false;
    private boolean isRequestingPermissin = false;

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private boolean setDisplayVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //第一次加载时，控制actionbar的展示
        //changeActionbar();
        setDisplayVisible = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        List<PermissionInfo> leNeverPermissions = new ArrayList<PermissionInfo>();
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                isRequestingPermissin = false;
                if (grantResults.length > 0) {
                    PackageManager packageManager = getContext().getPackageManager();
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            try {
                                PermissionInfo info = packageManager.getPermissionInfo(permissions[i], 0);
                                leNeverPermissions.add(info);
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                        }
                    }
                }
                if (leNeverPermissions.size() <= 0) {
                    UpdateUtil.mIsStartedNewly = true;
                    //mUpdateHelper.requreyVersion();
                    ((MainActivity) getActivity()).updateAction(0);
                    startLoadData();
                } else {
                    showLeNeverPermissionRequestDialog(leNeverPermissions);
                }
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isLicenceAccept()) {
            if (SharedPreferencesHelper.getInt(MainActivity.WALLET_LICENCE_ACCEPT, MainActivity.WALLET_LICENCE_REJECT) == MainActivity.WALLET_LICENCE_REJECT) {
                //当用户选择的是拒绝接受条款时，直接弹框提示用户接受
                showLeLicenceDialog();

            } else if (SharedPreferencesHelper.getInt(MainActivity.WALLET_LICENCE_ACCEPT, MainActivity.WALLET_LICENCE_REJECT) == MainActivity.WALLET_LICENCE_ACCEPT_ONCE && displayLicence) {
                //当用户选择当次不再提示,并且已经显示过提示框的时候，不在提示用户接受，直接进入接下来的逻辑
                if (hasPermission()) {
                    startOpenData();
                    LocationHelper.getInstance().getAddress(true);
                } else if (!isRequestingPermissin) {
                    checkMainPermission(PERMISSIONS_REQUEST_CODE);
                }
            } else {
                //当用户选择不再提示，并且没有显示过提示框的时候，直接提示用户接受
                showLeLicenceDialog();
            }

        } else {
            if (hasPermission()) {
                startOpenData();
                LocationHelper.getInstance().getAddress(true);
            } else if (!isRequestingPermissin) {
                checkMainPermission(PERMISSIONS_REQUEST_CODE);
            }
        }
        setDisplayVisible = false;

    }


    private boolean isLicenceAccept() {
        return SharedPreferencesHelper.getInt(MainActivity.WALLET_LICENCE_ACCEPT, MainActivity.WALLET_LICENCE_REJECT) == MainActivity.WALLET_LICENCE_ACCEPT_FOREVER;
    }

    private void showLeLicenceDialog() {
        if (isLicenceAccept()) {
            return;
        }

        if (mLeLicenceDialog != null) {
            mLeLicenceDialog.dismiss();
        }
        mLeLicenceDialog = new LeLicenceDialog(getContext(), getString(R.string.app_name),
                LeLicenceDialog.TYPE_USER_PRIVACY_CONTACTS_LOCATION_NET)
                .setLeLicenceDialogClickListener(new LeLicenceDialog.LeLicenceDialogClickListener() {

                    @Override
                    public void onClickListener(LeLicenceDialog.KEY key) {
                        switch (key) {
                            case BTN_AGREE:
                                acceptLicense();
                                //mUpdateHelper.startUpgradeService();
                                ((MainActivity) getActivity()).updateAction(1);
                                break;
                            case BTN_CANCEL:
                                rejectLicense();
                                break;
                            case OUTSIDE:
                                getActivity().finish();
                                break;
                        }
                    }
                }).show();
    }

    private void acceptLicense() {
        if (mLeLicenceDialog != null) {
            SharedPreferencesHelper.putInt(MainActivity.WALLET_LICENCE_ACCEPT,
                    mLeLicenceDialog.isChecked() ? MainActivity.WALLET_LICENCE_ACCEPT_FOREVER : MainActivity.WALLET_LICENCE_ACCEPT_ONCE);
            displayLicence = mLeLicenceDialog.isChecked() ? false : true;
            mLeLicenceDialog.dismiss();
            if (!hasPermission()) {
                checkMainPermission(PERMISSIONS_REQUEST_CODE);
            } else {
                startLoadData();
            }
        }
    }

    private boolean hasPermission() {
        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean result = true;
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }

    public boolean checkMainPermission(int requestCode) {
        boolean result = true;
        String[] permissionList = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ArrayList<String> deniedPermissionList = new ArrayList<String>();
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
                deniedPermissionList.add(permission);
            }
        }
        if (deniedPermissionList.size() > 0) {
            isRequestingPermissin = true;
            ActivityCompat.requestPermissions(getActivity(), deniedPermissionList.toArray(new String[0]), requestCode);
        }
        return result;
    }

    private void showLeNeverPermissionRequestDialog(List<PermissionInfo> leNeverPermissions) {
        if (mLeNeverPermissionRequestDialog == null) {
            try {
                mLeNeverPermissionRequestDialog = new LeNeverPermissionRequestDialog(getContext(), leNeverPermissions, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLeNeverPermissionRequestDialog.disappear();
                        mLeNeverPermissionRequestDialog = null;
                        getActivity().finish();
                    }
                });
                mLeNeverPermissionRequestDialog.setCancelable(false);
            } catch (Exception e) {
            }
        }
        mLeNeverPermissionRequestDialog.appear();
    }

    private void rejectLicense() {
        SharedPreferencesHelper.remove(MainActivity.WALLET_LICENCE_ACCEPT);
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLeLicenceDialog != null) {
            mLeLicenceDialog.dismiss();
        }
    }


    public void startOpenData() {
        if (setDisplayVisible) {
            fragmentDisplay();
        }
        startLoadData();
    }

    public abstract void startLoadData();

    public abstract void onNetWorkChanged(boolean isNetworkAvailable);

    //public abstract boolean displayActionbar();

    //保存从其它界面跳转时，不同的传递数据
    public Bundle bundle;

    public void gotoChildScreen(int type, Bundle bundle) {
        this.bundle = bundle;
        gotoNext(type, bundle);
    }


    public abstract void gotoNext(int type, Bundle bundle);

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        //一直在主页点击跳转时，控制顶部actionbar是否展示
        if (!hidden) {
            //changeActionbar();
            fragmentDisplay();
        }
    }

    public abstract void fragmentDisplay();

    //更改actionbar的显示状态
//    public void changeActionbar() {
//        BaseWalletFragmentActivity a = (BaseWalletFragmentActivity) getActivity();
//        if (a != null) {
//            ActionBar ab = a.getSupportActionBar();
//            if (displayActionbar()) {
//                if (ab != null && !ab.isShowing())
//                    ab.show();
//            } else {
//                if (ab != null && ab.isShowing())
//                    ab.hide();
//            }
//        }
//    }
}
