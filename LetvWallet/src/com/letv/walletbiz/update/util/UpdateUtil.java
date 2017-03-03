package com.letv.walletbiz.update.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.http.client.RspConstants;
import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.update.UpdateConstant;
import com.letv.walletbiz.update.beans.LocalAppInfo;
import com.letv.walletbiz.update.beans.RemoteAppInfo;
import com.letv.walletbiz.update.http.UpdateRequestParams;
import com.letv.walletbiz.update.service.UpgradeService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.KeyValue;
import org.xutils.http.RequestParams;
import org.xutils.xmain;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhangzhiwei1 on 16-8-8.
 */
public class UpdateUtil {

    public static boolean mIsStartedNewly;
    public static boolean mIsForceUpdate;
    public static boolean mIsAppAllDownload;
    public static boolean mDownloadInCeller;

    public static RemoteAppInfo mWalletbizAppInfo;
    public static boolean mApkDownloading;

    public static final long HALF_DAY = 1000 * 60 * 60 * 12;
    public static final long ONE_MIN = 1000 * 30;

    private static final ArrayList<String> mSupportedLanguages = new ArrayList<String>(Arrays.asList("zh-cn","zh-hk","zh-sg","zh-tw","en-us","es","th","ru"));


    public static boolean isUpdateTimeExpire() {
        long time = SharedPreferencesHelper.getLong(UpdateConstant.PREFERENCES_LAST_CHECK_UPDATE, -1);
        if ((System.currentTimeMillis() - time) > HALF_DAY) {
            return true;
        }
        return false;
    }

    public static List<String> getLocalAppList() {
        List<String> appList = new ArrayList<String>();
        appList.add("com.letv.walletbiz");
        appList.add("com.letv.wallet");
        return appList;
    }


    public static List<LocalAppInfo> getLocalAppInfo(Context context) {
        List<LocalAppInfo> localAppInfoList = new ArrayList<LocalAppInfo>();
        List<String> localAppList = getLocalAppList();
        for(int i = 0; i < localAppList.size(); i++) {
            String packageName = localAppList.get(i);
            LocalAppInfo info = new LocalAppInfo();
            info.mApkVersion = getVersion(context,packageName);
            info.mPackageName = packageName;
            localAppInfoList.add(info);
        }

        return localAppInfoList;
    }

    public static RemoteAppInfo[] getUpdateInfoFromNetwork(Context context, List<LocalAppInfo> localAppInfoList) {
        UpdateRequestParams requestParams = buildVersionRequest(context,localAppInfoList);
        BaseResponse<RemoteAppInfo[]> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<RemoteAppInfo[]>>() {};
            response = xmain.http().postSync(requestParams, typeToken.getType());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            response = null;
        }
        return response == null ? null : response.data;
    }

    private static UpdateRequestParams buildVersionRequest(Context context, List<LocalAppInfo> localAppInfoList) {
        UpdateRequestParams params = new UpdateRequestParams(UpdateConstant.UPGRADE_INFO_NETWORK_URI);
        params.setConnectTimeout(RspConstants.CONNECT_TIMEOUT_TEN);
        params.addQueryStringParameter(UpdateConstant.DEVICE_TYPE,"phone");
        params.addQueryStringParameter(UpdateConstant.DEVICE_ID, DeviceUtils.getDeviceImei(context));
        params.addQueryStringParameter(UpdateConstant.MODEL, Build.MODEL);
        params.addQueryStringParameter(UpdateConstant.REGION,getRegion(context));

        String language = getLanguage(context);
        if(mSupportedLanguages.contains(language)) {
            params.addQueryStringParameter(UpdateConstant.PERFER_LANGUAGE,language);
        } else {
            params.addQueryStringParameter(UpdateConstant.PERFER_LANGUAGE,"en-us");
        }

        JSONArray jsonArray = new JSONArray();
        for(LocalAppInfo info : localAppInfoList) {
            JSONObject object = new JSONObject();
            try {
                object.put("packageName",info.mPackageName);
                object.put("apkVersion",info.mApkVersion);
                jsonArray.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        params.addBodyParameter(UpdateConstant.DATA,jsonArray.toString());
        return params;
    }

    private HashMap<String, String> getParam2Map(HashMap<String, String> mapParams, List<KeyValue> params) {

        if (params != null) {
            for (KeyValue kv : params) {
                String name = kv.key;
                String value = kv.getValueStr();
                mapParams.put(name, value);
            }
        }
        return mapParams;
    }

    private HashMap<String, String> getParamMap(RequestParams params) {
        HashMap<String, String> mapParams = new HashMap<String, String>();
        getParam2Map(mapParams, params.getQueryStringParams());
        return mapParams;
    }


    public static String getVersion(Context context,String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            return "" + info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static String getVersionName(Context context, String packageName) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVersionfromApkfile(Context context,String filePath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if(info != null) {
            return ""+info.versionCode;
        }
        return "0";
    }

    public static String getApplicationName(Context context,String packageName) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }


    private static String getLanguage(Context context) {
        String language = Locale.getDefault().toString().replace('_','-');
        return language.toLowerCase();
    }

    private static String getRegion(Context context) {
        String  currentRegionValue = Settings.Secure.getString(context.getContentResolver(), "leui_country_area_region_settings");
        if (currentRegionValue == null) {
            Locale locale = context.getResources().getConfiguration().locale;
            currentRegionValue = locale.getCountry();
            if("GB".equals(currentRegionValue)) {
                currentRegionValue = "UK";
            }
        }
        return currentRegionValue;

    }

    public static boolean checkAppIsAllDownLoaded(Context context, List<RemoteAppInfo> list2Upgrade) {
        boolean isAllDownloaded = true;
        for (int i = 0; i < list2Upgrade.size(); i++) {
            RemoteAppInfo info = list2Upgrade.get(i);
            info.setIsDownloaded(false);
            String fileName = UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH + "/" + info.getPackageName().replace(".","_")+".apk";
            File file = new File(fileName);
            if (file.exists()) {
                String apkVersionStr = getVersionfromApkfile(context,fileName);
                int apkVersion = 0;
                int remoteVersion = 0;
                try {
                    apkVersion = Integer.valueOf(apkVersionStr);
                    remoteVersion = Integer.valueOf(info.getApkVersion());
                    if (apkVersion >= remoteVersion) {
                        info.setIsDownloaded(true);
                    } else {
                        file.delete();
                        isAllDownloaded = false;
                    }
                } catch (Exception e) {
                    file.delete();
                }
            } else {
                isAllDownloaded = false;
            }
        }

        return isAllDownloaded;
    }

    public static boolean needForceUpgrade(List<RemoteAppInfo> list2Upgrade) {
        if (list2Upgrade == null) {
            return false;
        }
        for (RemoteAppInfo info : list2Upgrade) {
            if (info.getUpgradeType() == UpdateConstant.FORCE_UPDATE) {
                return true;
            }
        }

        return false;
    }

    public static void startUpgradeService(Context context) {
        Intent intent = new Intent(context,UpgradeService.class);
        context.startService(intent);
    }

    public static void bindUpgradeService(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context,UpgradeService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static void removeDownloadedFile(String packageName) {
        List<String> packageNames = getLocalAppList();
        if (!packageNames.contains(packageName)) {
            return;
        }
        String fileName = UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH + "/" + packageName.replace(".","_") + ".apk";
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean inKeyguardRestrictedInputMode(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            return true;
        }
        return false;
    }

    public static void installPackageNormal(Context context,String packageName) {

        String fileName = UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH + "/" + packageName.replace(".","_") + ".apk";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + fileName),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void installPackageSlient(Context context,String packageName) {
        String fileName = UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH + "/" + packageName.replace(".","_") + ".apk";
        Uri uri = Uri.fromFile(new File(fileName));
        try {
            Class localClass = Class.forName("android.app.ApplicationPackageManager");
            Class[] arrayOfClass = new Class[4];
            arrayOfClass[0] = Uri.class;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            arrayOfClass[3] = String.class;

            Method localMethod = null;
            localMethod = localClass.getMethod("installPackageSlient", arrayOfClass);
            Object[] arrayOfObject = new Object[4];
            arrayOfObject[0] = uri;
            arrayOfObject[1] = new Integer(2);
            arrayOfObject[2] = packageName;
            arrayOfObject[3] = context.getPackageName();
            if (localMethod != null) {
                localMethod.invoke(context.getPackageManager(), arrayOfObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
