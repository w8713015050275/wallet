package com.letv.walletbiz.update.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.letv.walletbiz.update.util.UpdateUtil;
import com.letv.walletbiz.update.beans.LocalAppInfo;
import com.letv.walletbiz.update.beans.RemoteAppInfo;

import java.util.List;

/**
 * Created by zhangzhiwei1 on 16-8-8.
 */
public class VersionCheckTask extends AsyncTask<Void,Integer,RemoteAppInfo[]> {

    public interface OnGetUpgradeInfoCallback {
        void onGetUpgradeInfoCallback(RemoteAppInfo[] remoteAppInfoList);
    }

    private OnGetUpgradeInfoCallback mCallback;
    private Context mContext;

    public VersionCheckTask(Context context, OnGetUpgradeInfoCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected RemoteAppInfo[] doInBackground(Void... voids) {
        List<LocalAppInfo> localInfoList = UpdateUtil.getLocalAppInfo(mContext);
        RemoteAppInfo[] remoteAppInfoList = UpdateUtil.getUpdateInfoFromNetwork(mContext,localInfoList);
        return remoteAppInfoList;
    }

    @Override
    protected void onPostExecute(RemoteAppInfo[] list2Upgrade) {
        if(mCallback != null) {
            mCallback.onGetUpgradeInfoCallback(list2Upgrade);
        }
    }
}
