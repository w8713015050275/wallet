package com.letv.walletbiz.push.schedule.worker;

import android.app.job.JobParameters;

import com.letv.walletbiz.push.PushRegisterService;

public class PushWorker extends SingleWorker {

    public PushWorker(JobCallback jobCallback) {
        super(jobCallback);
    }

    @Override
    public int getJobId() {
        return PUSH_JOB_ID;
    }

    @Override
    public int start(JobParameters params) {
        PushRegisterService.initPush();
        mJobCallback.callJobFinished(params, false);
        return 0;
    }
}