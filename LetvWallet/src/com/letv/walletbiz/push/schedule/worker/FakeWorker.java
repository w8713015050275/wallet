package com.letv.walletbiz.push.schedule.worker;

import android.app.job.JobParameters;

/**
 * Fake worker
 * 配合其他job一起使用，保证该改组的job一并启动
 * 针对x6的补丁
 */
public class FakeWorker extends SingleWorker {

    public FakeWorker(JobCallback jobCallback) {
        super(jobCallback);
    }

    @Override
    public int getJobId() {
        return FAKE_JOB_ID;
    }

    @Override
    public int start(JobParameters params) {
        mJobCallback.callJobFinished(params, false);
        return 0;
    }
}
