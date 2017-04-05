package com.letv.walletbiz.update;

import android.os.Environment;

/**
 * Created by zhangzhiwei1 on 16-8-8.
 */
public class UpdateConstant {

    //request version params
    public static final String PACKAGE = "package";
    public static final String APK_VERSION = "apkVersion";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String DEVICE_ID = "deviceId";
    public static final String MODEL = "model";
    public static final String REGION = "region";
    public static final String PERFER_LANGUAGE = "user-prefer-language";
    public static final String UI = "ui";
    public static final String DATA = "data";

    //get upgradeinfo
    public static final String ACCESS_KEY = "ak_lMHsCi32Wgyaqg23g9YL";
    public static final String SECRET_KEY = "sk_xVU5x7uvHXse38f8axKN";

    private static final String BASE_URL = "ota.scloud.letv.com";
    public static final String UPGRADE_INFO_NETWORK_URI = "http://" + BASE_URL + "/apk/api/v1/getAllUpgradeInfo";

    //init in WalletApplication.java
    public static String UPGRADE_DOWNLOAD_APK_SAVE_PATH;

    public static final String UPGRADE_PUSH_MESSAGE_DATA = "letvwallet://mainactivity.update";

    //upgrade mode selected by user from dialog
    public static final int UPGRADE_NOW = 3;
    public static final int NOTIFY_LATER = 1;

    //upgrade type
    public static final int FORCE_UPDATE = 3;

    public static final String UPGRADE_AFTER_CRASHED = "try_to_upgrade_after_crashed";


    //service activity communicate command
    public static final int QUREY_NEW_VERSION_TO_SERVICE = 99;
    public static final int SHOW_UPDATE_DIALOG_TO_CLIENT_WITH_APPS_INFO = 100;
    public static final int UPDATE_NOW_FROM_CLIENT = 101;
    public static final int NOTIFY_LATER_FROM_CLIENT = 102;
    public static final int QUREY_NEW_VERSION_TO_SERVICE_BY_PUSH_NOTICE = 103;
    public static final int EXIT_APPLICATION = 104;
    public static final int CANCEL_ALL_NOTIFICATION = 105;

    public static final String PREFERENCES_LAST_CHECK_UPDATE = "last_check_update";
    public static final String PREFERENCES_NOTIFY_LATER = "notify_later";
    public static final String PREFERENCES_FORCE_UPGRADE = "force_upgrade";
    public static final String PREFERENCES_SAVE_REMOTE_BIZ_VERSION_CODE = "remote_biz_version_code";
    public static final String PREFERENCES_SAVE_REMOTE_PAY_VERSION_CODE = "remote_pay_version_code";
}
