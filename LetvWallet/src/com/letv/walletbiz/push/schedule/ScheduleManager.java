package com.letv.walletbiz.push.schedule;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.push.schedule.worker.SingleWorker;

/**
 * Created by lijujying on 16-7-12.
 */
public class ScheduleManager {

    private static final String TAG = ScheduleManager.class.getSimpleName();
    // 有wifi时，6次短重试全部失败，设置重试job的间隔时间，1个小时
    public static final long PUSH_RETYR_JOB_MIN_INTERVAL = 3600000l;
    public static final long PUSH_RETYR_JOB_MAX_INTERVAL = 2 * 3600000l;

    private static ScheduleManager mInstance;

    private Context mContext;
    private JobScheduler mJobScheduler;

    public static ScheduleManager getInstance() {
        if (mInstance == null) {
            synchronized (ScheduleManager.class) {
                if (mInstance == null)
                    mInstance = new ScheduleManager();
            }
        }
        return mInstance;
    }

    private ScheduleManager() {
        mContext = WalletApplication.getApplication();
    }

    /**
     * 启动job服务
     */
    private final void scheduleJobLocked(RetryData data) {

        if (mJobScheduler == null) {
            mJobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

        mJobScheduler.cancel(data.jobId);

        ComponentName serviceComponent = new ComponentName(mContext, ScheduleService.class);
        JobInfo.Builder builder = new JobInfo.Builder(data.jobId, serviceComponent);
        builder.setMinimumLatency(data.minLatencyMillis);
        if (data.maxExecutionDelayMillis > 0) {
            builder.setOverrideDeadline(data.maxExecutionDelayMillis);
        }
        builder.setRequiredNetworkType(data.networkType);
        int result = mJobScheduler.schedule(builder.build());
        LogHelper.d("[%s] scheduleJobLocked, result: " + result + ", data: " + data, TAG);
    }

    /**
     * 取消任务
     *
     * @param jobId
     */
    private final void cancelJob(int jobId) {
        if (jobId <= 0) {
            return;
        }
        if (mJobScheduler == null) {
            mJobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        mJobScheduler.cancel(jobId);
    }

    /**
     * 设置监听网络的job
     *
     */
    public static final void scheduleNetWorkPushJob() {
        // 设置push job
        RetryData data = new RetryData(SingleWorker.PUSH_JOB_ID, 0, 0, JobInfo.NETWORK_TYPE_ANY);
        getInstance().scheduleJobLocked(data);
        RetryData fakeData = new RetryData(SingleWorker.FAKE_JOB_ID, 0, 0, JobInfo.NETWORK_TYPE_ANY);
        getInstance().scheduleJobLocked(fakeData);
    }

    /**
     * 设置定时 job
     *
     * @param delay
     * @param maxTime
     */
    public static final void scheduleLongTimePushJob(long delay, long maxTime) {
        RetryData data = new RetryData(SingleWorker.PUSH_JOB_ID, delay, maxTime, JobInfo.NETWORK_TYPE_ANY);
        getInstance().scheduleJobLocked(data);
    }

    /**
     * 取消push相关job
     */
    public static final void cancelSchedulePushJob() {
        getInstance().cancelJob(SingleWorker.PUSH_JOB_ID);
    }

}
