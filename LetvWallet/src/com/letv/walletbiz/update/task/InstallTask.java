package com.letv.walletbiz.update.task;

import android.content.Context;

import com.letv.walletbiz.update.util.UpdateUtil;
import com.letv.walletbiz.update.beans.RemoteAppInfo;

/**
 * Created by zhangzhiwei1 on 16-8-11.
 */
public class InstallTask implements Runnable {

    private RemoteAppInfo mAppInfo;
    private Context mContext;
    private boolean mInstallSlient;

    public InstallTask(Context context,RemoteAppInfo appInfo,boolean installSlient) {
        mContext = context;
        mAppInfo = appInfo;
        mInstallSlient = installSlient;
    }

    @Override
    public void run() {
        if(mInstallSlient) {
            UpdateUtil.installPackageSlient(mContext,mAppInfo.getPackageName());
        } else {
            UpdateUtil.installPackageNormal(mContext,mAppInfo.getPackageName());
        }
    }
}
