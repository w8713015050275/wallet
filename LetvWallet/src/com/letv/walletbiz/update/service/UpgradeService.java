package com.letv.walletbiz.update.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.update.UpdateConstant;
import com.letv.walletbiz.update.beans.LocalAppInfo;
import com.letv.walletbiz.update.task.DownLoadManager;
import com.letv.walletbiz.update.util.UpdateUtil;
import com.letv.walletbiz.update.beans.RemoteAppInfo;
import com.letv.walletbiz.update.receiver.ScreenObserver;

import org.xutils.common.task.PriorityExecutor;

import java.util.ArrayList;
import java.util.List;
import com.letv.walletbiz.R;
import com.letv.walletbiz.update.task.InstallTask;

/**
 * Created by zhangzhiwei1 on 16-8-11.
 */
public class UpgradeService extends Service {

    private final String TAG = "UpgradeService";

    public interface DownloadCallback {
        void onSuccess(RemoteAppInfo appInfo);
        void onError(boolean needRerequest);
        void onCancelled();
        void onFinished(RemoteAppInfo appInfo, boolean certifiedWifi);
    }

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private List<RemoteAppInfo> mList2Upgrade;
    private int mUpgradeTypeByUser = -1;
    private int mNeedToUpgradeApkCount;
    private List<RemoteAppInfo> mPendingList2Install;

    private ScreenObserver mScreenObserver;
    private PriorityExecutor mExecutor = new PriorityExecutor(3);
    private DownLoadManager mDownLoadManager;
    private boolean mScreenIsOff;
    private int mNetworkHashCode = 0;

    private final int INSTALL_PACKAPGE = 1;
    private final int DOWNLOAD_PACKAPGE = 2;
    private final int REGISTER_SCREEN_OBSERVER = 3;
    private final int ADD_NETWORK_STATE_CHANGED_LISTENER = 4;
    private final int STOP_SERVICE = 5;
    private final int RETRY_TO_GET_UPGRADE_INFO = 6;
    private final int QUERY_NEW_VERSION_ASYNC = 7;

    private final int HANDLE_BROADCAST_SCREEN_ON = 8;
    private final int HANDLE_BROADCAST_SCREEN_OFF = 9;
    private final int HANDLE_BROADCAST_USER_PRESENT = 10;

    private boolean mIsRestartAfterCrashed;
    private boolean mNeedTryToQueryVersion;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("UpgradeService");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        sendHandlerMessage(REGISTER_SCREEN_OBSERVER,null);
        sendHandlerMessage(ADD_NETWORK_STATE_CHANGED_LISTENER,null);

        mDownLoadManager = new DownLoadManager(mExecutor);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
           return super.onStartCommand(intent,flags,startId);
        }
        if (intent.getBooleanExtra(UpdateConstant.UPGRADE_AFTER_CRASHED,false)) {
            mIsRestartAfterCrashed = true;
            sendHandlerMessage(QUERY_NEW_VERSION_ASYNC,null);
        }
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mDownLoadManager != null) {
            mDownLoadManager.cancelAllNotification();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScreenObserver != null) {
            mScreenObserver.shutdownObserver();
            mScreenObserver = null;
        }

        mServiceLooper.quit();
    }

    private class DownloadCallbackImpl implements DownloadCallback {
        private  boolean isError;
        private  boolean isCancelled;
        private  boolean needRerequest;
        @Override
        public void onSuccess(RemoteAppInfo appInfo) {
            appInfo.setIsDownloaded(true);
            if (mPendingList2Install == null) {
                mPendingList2Install = new ArrayList<RemoteAppInfo>();
            }
            mPendingList2Install.add(appInfo);
            mList2Upgrade.remove(appInfo);
            //check all apk are downloaded,start to reinstall
            if (mUpgradeTypeByUser == UpdateConstant.UPGRADE_NOW) {
                installPackageIfPossible();
            } else {
                if (mScreenIsOff) {
                    boolean inKeyguardMode = UpdateUtil.inKeyguardRestrictedInputMode(UpgradeService.this);
                    if (inKeyguardMode) {
                        installPackageIfPossible();
                    }
                }
            }
        }

        @Override
        public void onError(boolean needRerequest) {
            isError = true;
            this.needRerequest = needRerequest;
        }

        @Override
        public void onCancelled() {
            isCancelled = true;
        }

        @Override
        public void onFinished(RemoteAppInfo appInfo, boolean certifiedWifi) {
            if (needRerequest && isError) {
                needRerequest = false;
                if (mUpgradeTypeByUser == UpdateConstant.UPGRADE_NOW) {
                    if (NetworkHelper.isNetworkAvailable()) {
                        isError = false;
                        mDownLoadManager.restartDownload(appInfo);
                        return;
                    }
                } else {
                    if (NetworkHelper.isWifiAvailable()){
                        isError = false;
                        mDownLoadManager.restartDownload(appInfo);
                        return;
                    }
                }
            }

            if (isError || isCancelled || !certifiedWifi) {
                if (mUpgradeTypeByUser == UpdateConstant.UPGRADE_NOW) {
                    if (!NetworkHelper.isWifiAvailable() && !UpdateUtil.mDownloadInCeller) {
                        Toast.makeText(getApplicationContext(), getString(R.string.wifi_fail_continue_next),Toast.LENGTH_SHORT).show();
                    } else if (!certifiedWifi) {
                        UpdateUtil.removeDownloadedFile(appInfo.getPackageName());
                        Toast.makeText(getApplicationContext(), getString(R.string.mobile_prompt_net_connection_fail),Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.download_fail),Toast.LENGTH_SHORT).show();
                    }
                    isError = false;
                    isCancelled = false;
                    if (UpdateUtil.mIsForceUpdate == true) {
                        mDownLoadManager.cancelAllNotification();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
            }
        }
    }

    private int mTryToGetUpgradeInfoCount = 0;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_PACKAPGE:
                    RemoteAppInfo appInfo = (RemoteAppInfo)msg.obj;
                    mDownLoadManager.startDownload(UpgradeService.this,appInfo,new DownloadCallbackImpl(),mUpgradeTypeByUser);
                    break;
                case INSTALL_PACKAPGE:
                    appInfo = (RemoteAppInfo)msg.obj;
                    InstallTask installTask = new InstallTask(UpgradeService.this,appInfo,true);
                    mExecutor.execute(installTask);
                    break;

                case REGISTER_SCREEN_OBSERVER:
                    if (mScreenObserver == null) {
                        mScreenObserver = new ScreenObserver(UpgradeService.this);
                    }
                    mScreenObserver.startObserver(new ScreenObserver.ScreenStateListener() {
                        @Override
                        public void onScreenOn() {
                            sendHandlerMessage(HANDLE_BROADCAST_SCREEN_ON,null);
                        }

                        @Override
                        public void onScreenOff() {
                            sendHandlerMessage(HANDLE_BROADCAST_SCREEN_OFF,null);
                        }

                        @Override
                        public void onUserPresent() {
                            sendHandlerMessage(HANDLE_BROADCAST_USER_PRESENT,null);
                        }
                    });
                    break;

                case ADD_NETWORK_STATE_CHANGED_LISTENER:
                    NetworkHelper.getInstance().addCallBack(++mNetworkHashCode, new NetworkHelper.NetworkAvailableCallBack() {
                        @Override
                        public void onNetworkAvailable() {
                            sendHandlerMessage(ADD_NETWORK_STATE_CHANGED_LISTENER,null);
                            if(mUpgradeTypeByUser == -1) {
                                return;
                            }
                            //restart to download if wifi is available
                            if(mUpgradeTypeByUser == UpdateConstant.NOTIFY_LATER) {
                                if (!NetworkHelper.isWifiAvailable()) {
                                    return;
                                }
                            }
                            if (mList2Upgrade != null) {
                                if (NetworkHelper.isWifiAvailable()) {
                                    for (RemoteAppInfo appInfo : mList2Upgrade) {
                                        if (!appInfo.getIsDownloaded()) {
                                            sendHandlerMessage(DOWNLOAD_PACKAPGE,appInfo);
                                        }
                                    }
                                }
                            }
                        }
                    });
                break;

                case RETRY_TO_GET_UPGRADE_INFO:
                    mTryToGetUpgradeInfoCount++;
                    if(mTryToGetUpgradeInfoCount >= 3) {
                        return;
                    }
                    mNeedTryToQueryVersion = true;
                    queryNewVersionSync(msg.replyTo);
                    break;
                case UpdateConstant.QUREY_NEW_VERSION_TO_SERVICE:
                    mNeedTryToQueryVersion = true;
                    queryNewVersionSync(msg.replyTo);
                    break;

                case UpdateConstant.QUREY_NEW_VERSION_TO_SERVICE_BY_PUSH_NOTICE:
                    mNeedTryToQueryVersion = true;
                    queryNewVersionSync(msg.replyTo);
                    break;
                case UpdateConstant.UPDATE_NOW_FROM_CLIENT:
                    mUpgradeTypeByUser = UpdateConstant.UPGRADE_NOW;
                    startUpgrade(mList2Upgrade);
                    break;

                case UpdateConstant.NOTIFY_LATER_FROM_CLIENT:
                    mUpgradeTypeByUser = UpdateConstant.NOTIFY_LATER;
                    startUpgrade(mList2Upgrade);
                    break;

                case STOP_SERVICE:
                    if (mScreenObserver != null) {
                        mScreenObserver.shutdownObserver();
                        mScreenObserver = null;
                    }
                    mServiceLooper.quit();
                    stopSelf();
                    break;
                case UpdateConstant.CANCEL_ALL_NOTIFICATION:
                    if (mDownLoadManager != null) {
                        mDownLoadManager.cancelAllNotification();
                    }
                    break;
                case QUERY_NEW_VERSION_ASYNC:
                    queryNewVersionSync(null);
                    break;

                case HANDLE_BROADCAST_SCREEN_ON:
                    mScreenIsOff = false;
                    break;

                case HANDLE_BROADCAST_SCREEN_OFF:
                    mScreenIsOff = true;
                    if (mPendingList2Install ==  null || mPendingList2Install.size() < mNeedToUpgradeApkCount) {
                        return;
                    }
                    boolean inKeyguardMode = UpdateUtil.inKeyguardRestrictedInputMode(UpgradeService.this);
                    if (inKeyguardMode) {
                        installPackageIfPossible();
                    }
                    break;

                case HANDLE_BROADCAST_USER_PRESENT:
                    mScreenIsOff = false;
                    break;
            }
        }
    }

    private Messenger mMessenger;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mMessenger = new Messenger(mServiceHandler);
        return mMessenger.getBinder();
    }

    private void startUpgrade(List<RemoteAppInfo> list2Upgrade) {
        if (list2Upgrade != null) {
            mNeedToUpgradeApkCount = list2Upgrade.size();
        }
        if(mPendingList2Install != null) {
            mPendingList2Install.clear();
        }
        for (int i = 0; i < list2Upgrade.size(); i++) {
            RemoteAppInfo itemInfo = list2Upgrade.get(i);
            if (!itemInfo.getIsDownloaded()) {
                if(mUpgradeTypeByUser == UpdateConstant.NOTIFY_LATER) {
                    if(!NetworkHelper.isWifiAvailable()) {
                        continue;
                    }
                }
                sendHandlerMessage(DOWNLOAD_PACKAPGE,itemInfo);
            } else {
                if (mPendingList2Install == null) {
                    mPendingList2Install = new ArrayList<RemoteAppInfo>();
                }
                list2Upgrade.remove(itemInfo);
                mPendingList2Install.add(itemInfo);
                i--;
            }
        }

        if (mUpgradeTypeByUser == UpdateConstant.UPGRADE_NOW) {
            installPackageIfPossible();
        }
    }

    public void sendHandlerMessage(int what,RemoteAppInfo appInfo) {
        Message msg = Message.obtain();
        msg.what = what;
        if (appInfo != null) {
            msg.obj = appInfo;
        }
        mServiceHandler.sendMessage(msg);
    }

    private void installPackageIfPossible() {
        if (mPendingList2Install != null && mNeedToUpgradeApkCount == mPendingList2Install.size()) {
            for (int i = mPendingList2Install.size()-1; i >= 0 ; i--) {
                RemoteAppInfo info = mPendingList2Install.get(i);
                mPendingList2Install.remove(info);
                sendHandlerMessage(INSTALL_PACKAPGE,info);
            }
            UpdateUtil.mApkDownloading = false;
            sendHandlerMessage(STOP_SERVICE,null);
        }
    }

    private void queryNewVersionSync(Messenger replyTo) {
        if (UpdateUtil.mIsStartedNewly || mNeedTryToQueryVersion) {
            UpdateUtil.mIsStartedNewly = false;
            mNeedTryToQueryVersion = false;
            if (NetworkHelper.isNetworkAvailable()) {
                List<LocalAppInfo> localInfoList = UpdateUtil.getLocalAppInfo(this);
                RemoteAppInfo[] remoteAppInfoList = UpdateUtil.getUpdateInfoFromNetwork(this,localInfoList);
                //handle error
                if(remoteAppInfoList ==  null || remoteAppInfoList.length == 0) {
                    Log.d(TAG, "===wallet remote app list is null");
                    mServiceHandler.sendEmptyMessage(RETRY_TO_GET_UPGRADE_INFO);
                    return;
                }
                //to update
                if (remoteAppInfoList != null && remoteAppInfoList.length > 0) {
                    if (mList2Upgrade == null) {
                        mList2Upgrade = new ArrayList<RemoteAppInfo>();
                    }
                    mList2Upgrade.clear();
                    List<LocalAppInfo> localAppInfolist = UpdateUtil.getLocalAppInfo(getApplicationContext());
                    for (LocalAppInfo localAppInfo : localAppInfolist) {
                        for (RemoteAppInfo remoteAppInfo : remoteAppInfoList) {
                            if (localAppInfo.mPackageName.equals(remoteAppInfo.getPackageName())) {
                                int local_version = 0;
                                int remote_version = 0;
                                try {
                                    local_version = Integer.valueOf(localAppInfo.mApkVersion);
                                    remote_version = Integer.valueOf(remoteAppInfo.getApkVersion());
                                    if (localAppInfo.mPackageName.equals(UpdateUtil.getLocalAppList().get(0))) {
                                        SharedPreferencesHelper.putLong(UpdateConstant.PREFERENCES_SAVE_REMOTE_BIZ_VERSION_CODE, remote_version);
                                    } else if (localAppInfo.mPackageName.equals(UpdateUtil.getLocalAppList().get(1))) {
                                        SharedPreferencesHelper.putLong(UpdateConstant.PREFERENCES_SAVE_REMOTE_PAY_VERSION_CODE, remote_version);
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                if (local_version < remote_version) {
                                    remoteAppInfo.setApplicationName(UpdateUtil.getApplicationName(getApplicationContext(),remoteAppInfo.getPackageName()));
                                    mList2Upgrade.add(remoteAppInfo);
                                }
                            }
                        }
                    }
                }
                if (mList2Upgrade != null && mList2Upgrade.size() > 0) {
                    //upgrade directly after crashed
                    if (mIsRestartAfterCrashed) {
                        mIsRestartAfterCrashed = false;
                        mUpgradeTypeByUser = UpdateConstant.UPGRADE_NOW;
                        startUpgrade(mList2Upgrade);
                    } else {
                        //qurey activity to upgrade or not
                        boolean isAllDownloaded = UpdateUtil.checkAppIsAllDownLoaded(UpgradeService.this,mList2Upgrade);
                        UpdateUtil.mIsAppAllDownload = isAllDownloaded;
                        boolean needForceUpgrade = UpdateUtil.needForceUpgrade(mList2Upgrade);
                        Message msgToClient = Message.obtain();
                        msgToClient.what = UpdateConstant.SHOW_UPDATE_DIALOG_TO_CLIENT_WITH_APPS_INFO;

                        RemoteAppInfo walletInfo = mList2Upgrade.get(0);
                        for (RemoteAppInfo info : mList2Upgrade) {
                            if(getPackageName().equals(info.getPackageName())) {
                                walletInfo = info;
                                break;
                            }
                        }
                        UpdateUtil.mWalletbizAppInfo = walletInfo;
                        msgToClient.arg1 = isAllDownloaded ? 1 : 0;
                        msgToClient.arg2 = needForceUpgrade ? 1 : 0;
                        msgToClient.obj = mList2Upgrade;
                        try {
                            if(replyTo != null) {
                                replyTo.send(msgToClient);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }
}
