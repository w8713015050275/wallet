package com.letv.walletbiz.update.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.update.UpdateConstant;
import com.letv.walletbiz.update.UpdateHelper;
import com.letv.walletbiz.update.util.UpdateUtil;
import com.letv.walletbiz.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangzhiwei1 on 16-8-15.
 */
public class UpgradeApkInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent();
        i.setAction(UpdateHelper.HIDE_UPGRADE_DIALOG);
        context.sendBroadcast(i);

        if (intent.getAction().equalsIgnoreCase("android.intent.action.PACKAGE_REPLACED")) {
//            //获得远程版本和Replaced后的本地版本；升级-版本相同；卸载-版本不同
//            //1.1.2版本及以前版本没有设置，所以为0
//            long latestBizVersionCode = SharedPreferencesHelper.getLong(UpdateConstant.PREFERENCES_SAVE_REMOTE_BIZ_VERSION_CODE, 0);
//            long latestPayVersionCode = SharedPreferencesHelper.getLong(UpdateConstant.PREFERENCES_SAVE_REMOTE_PAY_VERSION_CODE, 0);
//            //必不为0的整数
//            long currentBizVersionCode = Long.valueOf(UpdateUtil.getVersion(context, UpdateUtil.getLocalAppList().get(0)));
//            long currentPayVersionCode = Long.valueOf(UpdateUtil.getVersion(context, UpdateUtil.getLocalAppList().get(1)));
            String packageStr = intent.getDataString();
            String[] strs = packageStr.split(":");
            String packageName = intent.getStringExtra("PackageName");
            if (strs != null && strs.length > 0) {
                packageName = strs[strs.length-1];
            }
            String contextPackageName = context.getPackageName();

//            //只要有一个升级成功，就进行数据上报：针对1.1.3以上版本，1.1.3升级到更高版本
//            if ((currentBizVersionCode == latestBizVersionCode) ||
//                    (currentPayVersionCode == latestPayVersionCode)) {
                ArrayList<String> packagesList = (ArrayList<String>) UpdateUtil.getLocalAppList();
                if (packagesList.contains(packageName)) {
                    if (SharedPreferencesHelper.getBoolean(UpdateConstant.PREFERENCES_NOTIFY_LATER, false)) {
                        Action.uploadInstallSuccess(packageName);
                    } else {
                        Action.uploadUpgradeSuccess(String.valueOf(SharedPreferencesHelper.getBoolean(UpdateConstant.PREFERENCES_FORCE_UPGRADE, false) ? 2 : 1), packageName);
                    }
                }

                if (packageName != null && contextPackageName != null) {
                    if (packageName.trim().equalsIgnoreCase(contextPackageName.trim())) {
                        UpdateUtil.removeDownloadedFile(packageName);
                        Toast.makeText(context, context.getString(R.string.upgrade_success_note), Toast.LENGTH_SHORT).show();
                        restartWallet(context);
                    }
                }
//            } else {
//                //卸载成功：重启wallet，不提示升级成功+数据上报
//                if (packageName != null && contextPackageName != null) {
//                    if (packageName.trim().equalsIgnoreCase(contextPackageName.trim())) {
//                        restartWallet(context);
//                    }
//                }
//            }

        }  else if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED_FAILED")) {
            String failPackageName = intent.getStringExtra("PackageName");
            List<String> packages = UpdateUtil.getLocalAppList();
            if (packages.contains(failPackageName)) {
                UpdateUtil.removeDownloadedFile(failPackageName);
                Toast.makeText(context, R.string.upgrade_fail_note, Toast.LENGTH_SHORT).show();
                if(UpdateUtil.mIsForceUpdate) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    },1500);
                }
            }
        }
    }

    private void restartWallet(Context context) {
        Intent intent = new Intent(context,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}