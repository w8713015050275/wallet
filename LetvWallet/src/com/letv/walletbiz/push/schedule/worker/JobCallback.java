package com.letv.walletbiz.push.schedule.worker;

import android.app.job.JobParameters;
import android.content.Context;

public interface JobCallback {

    Context getContext();

    /**
     * 取消其中一个任务
     */
    boolean callJobFinished(JobParameters params, boolean needReschedule);
}
