package com.letv.wallet.common.util;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by liuliang on 16-4-28.
 */
public class AppUtils {

    public static void LaunchUrl(Context context, String url, String serviceName, Class<?> cls) {
        LaunchUrlWithBundle(context, url, serviceName, cls, null);
    }

    public static void LaunchUrlWithBundle(Context context, String url, String serviceName, Class<?> cls,
                                           Bundle bundle ) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, cls);
        intent.putExtra(CommonConstants.EXTRA_URL, url);
        intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, serviceName);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public static void LaunchApp(Context context, String packageName, String actionWithData) {
        LaunchAppWithBundle(context, packageName, actionWithData, null);
    }

    public static void LaunchAppWithBundle(Context context, String packageName,
                                           String actionWithData, Bundle bundle) {
        if (context == null) {
            return;
        }
        try {
            String[] array = getActionAndData(actionWithData);
            Intent intent = new Intent(array[0]);
            if (!TextUtils.isEmpty(array[1])) {
                intent.setData(Uri.parse(array[1]));
            }
            intent.putExtra("pkgName", packageName);
            if (!context.getPackageName().equals(array[0])) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            if (!startActivityByPackage(context, packageName)) {
                downloadApp(context, packageName);
            }
        }
    }

    public static boolean startActivityByPackage(Context context, String packageName) {
        return startActivityByPackageWithBundle(context,packageName,null);
    }

    public static boolean startActivityByPackageWithBundle(Context context, String packageName,
                                                           Bundle bundle) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            intent.putExtra("pkgName", packageName);
            if (!context.getPackageName().equals(packageName)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static void downloadApp(Context context, String packageName) {
        if (context == null) {
            return;
        }
        try {
            Intent intent = getAppStoreIntent(packageName);
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public static Intent getAppStoreIntent(String packageName){
        Intent intent = new Intent();
        ComponentName component = new ComponentName("com.letv.app.appstore", "com.letv.app.appstore.appmodule.details.DetailsActivity");
                intent.setComponent(component);
        intent.putExtra("packageName", packageName);
        intent. putExtra("detailFromPage", "LeWallet");
        return intent;
    }


    public static void downloadAppSilent(Context context, String packageName) {
        Intent intent = new Intent("com.letv.app.appstore.startdownload");
        intent.putExtra("packageName", packageName);
        intent.putExtra("fromPage", "LeWallet");
        intent.putExtra("callerPackageName", context.getPackageName());
        context.sendBroadcast(intent);
    }

    public static boolean checkLetvAppStorePermission(Context context) {
        if (context == null) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        int storagePermission = pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, "com.letv.app.appstore");
        int phonePermission = pm.checkPermission(Manifest.permission.READ_PHONE_STATE, "com.letv.app.appstore");
        return (storagePermission == PackageManager.PERMISSION_GRANTED) && (phonePermission == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }

    /**
     *
     * @param actionWithData
     * @return index 1: action  2:data
     */
    public static String[] getActionAndData(String actionWithData) {
        if (TextUtils.isEmpty(actionWithData)) {
            return new String[2];
        }
        int index = actionWithData.indexOf('@');
        String action = null;
        String dataStr = null;
        if (index > 0) {
            action = actionWithData.substring(0, index);
            dataStr = actionWithData.substring(index + 1);
        } else {
            action = actionWithData;
        }
        return new String[]{action, dataStr};
    }
}
