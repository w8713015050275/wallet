package com.letv.walletbiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.google.android.gms.analytics.ExceptionParser;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.letv.tracker2.agnes.Agnes;
import com.letv.tracker2.enums.Area;
import com.letv.tracker2.enums.HwType;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.EnvUtil;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.push.PushRegisterService;
import com.letv.walletbiz.update.UpdateConstant;
import com.letv.walletbiz.update.util.CrashHandler;
import com.letv.walletbiz.update.util.UpdateUtil;

import org.xutils.xmain;

/**
 * Created by linquan on 15-12-25.
 */
public class WalletApplication extends BaseApplication {

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        initExceptionHandler();
        initEnv();
        getUToken();
        xmain.Ext.init(this);
        xmain.Ext.setDebug(true);
        Agnes.getInstance(HwType.PHONE_LETV, Area.CN).setContext(this);
        //TODO,上传国别信息待定
        //Agnes.getInstance().setRegion(Locale.getDefault().getCountry());
        initPushService();

        UpdateUtil.mIsStartedNewly = true;
        CrashHandler.getInstance().init(this);
        UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH = getExternalCacheDir().getPath();
    }

    @Override
    public String getAppUA() {
        return WalletConstant.WALLET_UA_NAME + " " + getAppVersion();
    }

    /* Try to get Letv account Token Since Start*/
    private void getUToken() {
        AccountHelper accountHelper = AccountHelper.getInstance();
        if (accountHelper.isLogin(this)){
            accountHelper.getTokenASync(this);
        }
    }
    private void initPushService() {
        PushRegisterService.initPush();
    }

    private void initExceptionHandler() {
        AnalyticsExceptionReporter customReportHandler = new AnalyticsExceptionReporter(
                getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                this);
        Thread.setDefaultUncaughtExceptionHandler(customReportHandler);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.app_tracker);
        }
        return mTracker;
    }

    private void initEnv() {
        Context targetAPPContext = null;
        try {
            targetAPPContext = createPackageContext("com.letv.wallet.evmsettingapp", CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (targetAPPContext != null) {
            SharedPreferences mPreferences = targetAPPContext.getSharedPreferences("wallet_pay", Context.MODE_PRIVATE);
            if (mPreferences != null) {
                boolean result = mPreferences.getBoolean("walletbiz", false);
                LogHelper.d("WalletApplication Env is " + result);
                EnvUtil.getInstance().setWalletTest(result);
            }
        }
    }

    private class AnalyticsExceptionParser implements ExceptionParser {
        @Override
        public String getDescription(String arg0, Throwable t) {
            if (t == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();

            sb.append(t.toString());
            sb.append("\n");

            for (StackTraceElement element : t.getStackTrace()) {
                sb.append(element.toString());
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private class AnalyticsExceptionReporter extends ExceptionReporter {

        public AnalyticsExceptionReporter(Tracker tracker, Thread.UncaughtExceptionHandler originalHandler, Context context) {
            super(tracker, originalHandler, context);
            setExceptionParser(new AnalyticsExceptionParser());
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            super.uncaughtException(t, e);
        }
    }
}
