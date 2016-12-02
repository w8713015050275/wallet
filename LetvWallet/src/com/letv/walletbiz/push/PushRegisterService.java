package com.letv.walletbiz.push;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.letv.android.lcm.LetvPushManager;
import com.letv.android.lcm.PushException;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.push.schedule.ScheduleManager;
import com.letv.walletbiz.push.utils.LePushConstant;

/**
 * Created by lijujying on 16-7-12.
 */
public class PushRegisterService extends Service {

    public static final String TAG = PushRegisterService.class.getSimpleName();

    private LetvPushManager mLpm;

    // 是否注册push服务相关值同步锁
    private final Object mDeviceLock = new Object();
    private boolean isRegister ;
    private String mRegId;

    private HandlerThread mHandlerThread;
    private PushWorkHandler mWorkHandler;
    private Looper mWorkLooper;

    // 重试计数器
    private int mRegRetryCount;

    private int mRequestPhase = LePushConstant.PHASE_EMPTY_CHECK;

    private Context mAppContext = WalletApplication.getApplication();

    // 是否注册了监听广播
    private boolean mHasReceiver;
    private PushServiceStatusReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startWorkHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null ) {
            stopSelf();
        }else {
            initPushState();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        cleanup();
        super.onDestroy();
    }

    /**
     * 必须在application中初始化
     */
    public static void initPush() {
        Intent intent = new Intent(WalletApplication.getApplication(), PushRegisterService.class);
        WalletApplication.getApplication().startService(intent);
    }

    private void startWorkHandler() {
        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mWorkLooper = mHandlerThread.getLooper();
        mWorkHandler = new PushWorkHandler(mWorkLooper);
    }

    public boolean isRegister() {
        synchronized (mDeviceLock) {
            return isRegister;
        }
    }

    public String getRegId() {
        synchronized (mDeviceLock) {
            return mRegId;
        }
    }

    public void initPushState() {
        if (mWorkHandler == null) {
            stopSelf();
            return;
        }else if (!mWorkHandler.hasMessages(LePushConstant.PUSH_APP_STATE_MSG)) {
            Message.obtain(mWorkHandler, LePushConstant.PUSH_APP_STATE_MSG).sendToTarget();
        }
    }

    private void getPushAppState() {
        String str = null;
        try {
            if (mLpm == null) {
                mLpm = LetvPushManager.getInstance(mAppContext);
            }
            str = mLpm.getAppState(LePushConstant.APP.APPID);
        } catch (PushException e) {
            e.printStackTrace();
        }
        isRegister = LePushConstant.PUSH_APP_STATE_NONE.equalsIgnoreCase(str) ? false : true;
        LogHelper.d("[%s] push app register state = " + str +" isRegister = " + isRegister, TAG);
        final boolean isRegister = isRegister();
        if (isRegister) {
            cleanup();
            stopSelf();
            return;
        }
        registerSync();
    }

    public void registerSync() {
        cancelRetryJob();
        if (mWorkHandler == null) {
            LogHelper.d("[%s] push handler is null", TAG);
            stopSelf();
            return;
        }else if (!mWorkHandler.hasMessages(LePushConstant.PUSH_REGISTER)) {
            Message.obtain(mWorkHandler, LePushConstant.PUSH_REGISTER).sendToTarget();
        }
    }

    private void registerPush() {
        if(isRegister()) {
            LogHelper.d("[%s] registerPush fail reason : already registered", TAG);
            return;
        }
        boolean result = true;
        try {
            if (mLpm == null) {
                mLpm = LetvPushManager.getInstance(mAppContext);
            }

            if (!NetworkHelper.isNetworkAvailable()) {
                LogHelper.d("[%s] registerPush fail reason : net unavailabe -- start sheduleJob", TAG);
                ScheduleManager.scheduleNetWorkPushJob();
                stopSelf();
                return;
            }
            final String regId = mLpm.register(LePushConstant.APP.APPID, LePushConstant.APP.APPKEY);
            synchronized (mDeviceLock) {
                if (TextUtils.isEmpty(regId)) {
                    mRegId = "";
                    isRegister = false;
                    result = false;
                } else {
                    mRegId = regId;
                    isRegister = true;
                }
            }
        } catch (RuntimeException ex) {
            LogHelper.d("[%s] doRegisterLocked, RuntimeException :" + ex.getMessage(), TAG);
            result = false;
        } catch (Exception ex) {
            LogHelper.d("[%s] doRegisterLocked, other ex :" + ex.getMessage() + ", " + ex.getClass().getName(), TAG);
            result = false;
        }
        LogHelper.d("[%s] push mRegId = " + mRegId, TAG);
        if (result) {
            onRegisterPushSuccess(); // 取消push服务状态广播监听
            stopSelf();
        } else {
            registerPushStateReceiver();  // 如果失败了, 注册push服务
            setRetryJob(LePushConstant.PHASE_PUSH_REGISTER); // 进行重试
        }
    }

    private long incRetryCount(int phase) {
        switch (phase) {
            case LePushConstant.PHASE_PUSH_REGISTER:
                if (mRegRetryCount < LePushConstant.MAX_RETRY_TIMES) {
                    mRegRetryCount++;
                    return LePushConstant.PUSH_SHORT_INTERVAL;
                }
                return -1;
        }
        return -1;
    }

    /**
     * 重试机制
     */
    public void notifyRetry(int phase) {
        switch (phase) {
            case LePushConstant.PHASE_PUSH_REGISTER: {
                registerSync();
            }
            break;
        }
    }

    private void setRetryJob(int phase) {
        if (mWorkHandler == null) {
            return;
        }
        synchronized (mDeviceLock) {
            switch (phase) {
                case LePushConstant.PHASE_PUSH_REGISTER:
                    mRequestPhase = phase;
                    long interval = incRetryCount(phase);
                    if (interval < 0) {
                        // 超过短重试次数, 使用alarm进行处理，1. 有可能是网络需要认证; 2. 网络暂时有问题
                        ScheduleManager.scheduleLongTimePushJob(ScheduleManager.PUSH_RETYR_JOB_MIN_INTERVAL, ScheduleManager.PUSH_RETYR_JOB_MAX_INTERVAL);
                        LogHelper.d("[%s] setRetryJob: " + interval + " short retry failed, set 15 minutes jobschdule...", TAG);
                        return;
                    }
                    Message msg = mWorkHandler.obtainMessage();
                    msg.arg1 = phase;
                    msg.what = LePushConstant.PUSH_SCHEDULER_MSG;
                    mWorkHandler.sendMessageDelayed(msg, interval);
                break;
            }
        }
    }

    public void cancelRetryJob() {
        if (mWorkHandler == null) {
            stopSelf();
            return;
        }
        synchronized (mDeviceLock) {
            mWorkHandler.removeMessages(LePushConstant.PUSH_SCHEDULER_MSG);
            mRequestPhase = LePushConstant.PHASE_EMPTY_CHECK;
        }
        ScheduleManager.cancelSchedulePushJob();
    }

    /**
     * 清除相关状态
     */
    public void cleanup() {
        if (mWorkLooper != null) {
            mWorkLooper.quit();
            mWorkLooper = null;
        }
        if (mWorkHandler != null) {
            mWorkHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        mRegRetryCount = 0;
        mRequestPhase = LePushConstant.PHASE_EMPTY_CHECK;
    }

    private void onRegisterPushSuccess() {
        unRegisterPushStateReceiver();
        cancelRetryJob();
        cleanup();
    }

    /**
     * 注册push服务注册状态
     */
    private final void registerPushStateReceiver() {
        if (isRegister) {
            return;
        }
        mReceiver = new PushServiceStatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LePushConstant.PUSH_STARTED_BROADCAST);
        filter.addAction(LePushConstant.PUSH_CONNECTED_BROADCAST);
        mAppContext.registerReceiver(mReceiver, filter);
        mHasReceiver = true;
    }

    private final void unRegisterPushStateReceiver() {
        if (!mHasReceiver) {
            return;
        }
        try {
            mAppContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
        }
        mHasReceiver = false;
    }

    // push服务启动或是登录广播接收
    private final class PushServiceStatusReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (LePushConstant.PUSH_STARTED_BROADCAST.equals(action)) {
                LogHelper.i("[%s] Service is started !", TAG);
            } else if (LePushConstant.PUSH_CONNECTED_BROADCAST.equals(action)) {
                LogHelper.i("[%s]  Connected push server !", TAG);
                if(mWorkHandler != null){
                    Message.obtain(mWorkHandler, LePushConstant.PUSH_SERVER_CONNECTED).sendToTarget();
                }
            }
        }
    }

    private final class PushWorkHandler extends Handler {
        public PushWorkHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LePushConstant.PUSH_SERVER_CONNECTED:
                    if(!isRegister()){
                        registerSync();
                    }
                    break;

                case LePushConstant.PUSH_REGISTER:
                    registerPush();
                    break;

                case LePushConstant.PUSH_SCHEDULER_MSG:
                    notifyRetry(msg.arg1);
                    break;

                case LePushConstant.PUSH_APP_STATE_MSG:
                    getPushAppState();
                    break;
            }
        }
    }

}