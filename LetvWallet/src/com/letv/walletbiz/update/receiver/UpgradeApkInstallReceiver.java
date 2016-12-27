package com.letv.walletbiz.update.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.update.UpdateHelper;
import com.letv.walletbiz.update.util.UpdateUtil;
import com.letv.walletbiz.R;

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
            String packageStr = intent.getDataString();
            String[] strs = packageStr.split(":");
            String packageName = intent.getStringExtra("PackageName");;
            if (strs != null && strs.length > 0) {
                packageName = strs[strs.length-1];
            }

            String contextPackageName = context.getPackageName();
            if (packageName != null && contextPackageName != null) {
                if (packageName.trim().equalsIgnoreCase(contextPackageName.trim())) {
                    UpdateUtil.removeDownloadedFile(packageName);
                    Toast.makeText(context, context.getString(R.string.upgrade_success_note),Toast.LENGTH_SHORT).show();
                    restartWallet(context);
                }
            }

        }  else if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED_FAILED")) {
            String failPackageName = intent.getStringExtra("PackageName");
            if (failPackageName.equals(context.getPackageName())) {
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