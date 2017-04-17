package com.letv.walletbiz.update.task;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.update.UpdateConstant;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.xmain;
import java.io.File;
import java.io.IOException;

import com.letv.walletbiz.update.beans.RemoteAppInfo;
import com.letv.walletbiz.update.service.UpgradeService.DownloadCallback;
import com.letv.walletbiz.R;
import com.letv.walletbiz.update.util.UpdateUtil;

/**
 * Created by zhangzhiwei1 on 16-8-8.
 */
public class DownloadTask implements Runnable {

    private RemoteAppInfo mAppInfo;
    private DownloadCallback mDownloadCallback;
    private Context mContext;

    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private int mNotifyId;
    private static int NotifiIdFactory = 0;
    private boolean mIsFinish;
    private boolean mIsNotificationCanceled;
    private boolean mNeedRerequest;
    private Object mObjLock = new Object();

    public DownloadTask(Context context,RemoteAppInfo appInfo, DownloadCallback downloadCallback,int upgradeTypeByUser) {
        mContext = context;
        mAppInfo = appInfo;
        mDownloadCallback = downloadCallback;
        if (upgradeTypeByUser == UpdateConstant.UPGRADE_NOW) {
            mNotifyId = NotifiIdFactory++;
            setupNotification(appInfo);
        }
    }

    public void setupNotification(RemoteAppInfo appInfo) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.download_app_notification);
        remoteViews.setTextViewText(R.id.title, mContext.getString(R.string.in_downloading) + appInfo.getApplicationName());
        remoteViews.setTextViewText(R.id.percent, "0/0");
        remoteViews.setProgressBar(R.id.progress, 100, 0, false);
        remoteViews.setImageViewBitmap(R.id.image,getAppIcon());
        remoteViews.setLong(R.id.time, "setTime", System.currentTimeMillis());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setOngoing(false);
        builder.setAutoCancel(false);
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.drawable.wallet_statusbar_icon);

        mNotification = builder.build();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.bigContentView = remoteViews;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotifyId, mNotification);
    }

    public RemoteAppInfo getRemoteAppInfo() {
        return mAppInfo;
    }

    public boolean isFinish() {
        return mIsFinish;
    }

    public void setFinish(boolean finish) {
        mIsFinish = finish;
    }

    public Bitmap getAppIcon() {
        PackageManager pm = mContext.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(mAppInfo.getPackageName(), 0);
            Drawable drawable =  info.loadIcon(pm);
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        } catch (Exception e) {
        }

        return null;
    }

    private Handler mHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            updateProgress(msg);
        }
    };

    public void updateProgress(Message msg) {
        if (mNotification == null) {
            return;
        }
        synchronized (mObjLock) {
            int percent = msg.what;
            if (percent >= 100) {
                mNotification.bigContentView.setTextViewText(R.id.title, mAppInfo.getApplicationName() + mContext.getString(R.string.download_sucess));
            }
            mNotification.bigContentView.setProgressBar(R.id.progress, 100, percent, false);
            mNotification.bigContentView.setTextViewText(R.id.percent,(String)msg.obj);
            if (!mIsNotificationCanceled || !mIsFinish) {
                mNotificationManager.notify(mNotifyId, mNotification);
            }
        }
    }

    @Override
    public void run() {
        UpdateUtil.mApkDownloading = true;
        File fileDir = new File(UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        String fileName = UpdateConstant.UPGRADE_DOWNLOAD_APK_SAVE_PATH + "/" + mAppInfo.getPackageName().replace(".","_")+".apk";
        //get apk
        RequestParams params = new RequestParams(mAppInfo.getFileUrl());
        params.setSaveFilePath(fileName);

        xmain.http().get(params,new Callback.ProgressCallback<File>() {

            @Override
            public void onSuccess(File result) {
                //针对没有认证的Wifi，xutils返回200，并产生一个568 Bytes大小的文件
                if (result.exists() && result.length() < 1024) {
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onFinished(mAppInfo, false);
                        return;
                    }
                }
                if (mDownloadCallback != null) {
                    mDownloadCallback.onSuccess(mAppInfo);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (mDownloadCallback != null) {
                    mNeedRerequest = false;
                    if((ex instanceof IOException) && !isOnCallback) {
                        if (NetworkHelper.isNetworkAvailable()) {
                            mNeedRerequest = true;
                        }
                    }
                    mDownloadCallback.onError(mNeedRerequest);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                if (mDownloadCallback != null) {
                    mDownloadCallback.onCancelled();
                }
            }

            @Override
            public void onFinished() {
                synchronized (mObjLock) {
                    mIsFinish = true;
                    if (mNotificationManager != null && !mNeedRerequest) {
                        mIsNotificationCanceled = true;
                        mNotificationManager.cancel(mNotifyId);
                    }
                    mNeedRerequest = false;
                }

                if (mDownloadCallback != null) {
                    mDownloadCallback.onFinished(mAppInfo, true);
                }
            }

            @Override
            public void onWaiting() {
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                int percent = (int) ((current * 100) / total);
                String progress = "" + current + "/" + total;
                Message msg = Message.obtain();
                msg.what = percent;
                msg.obj = progress;
                mHandle.sendMessage(msg);
            }
        });
    }

    public void cancelNotification() {
        if (mNotificationManager != null) {
            mIsNotificationCanceled = true;
            mIsFinish = true;
            mNotificationManager.cancel(mNotifyId);
        }
    }
}
