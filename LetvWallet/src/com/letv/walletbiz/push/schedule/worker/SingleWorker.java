package com.letv.walletbiz.push.schedule.worker;

import android.app.job.JobParameters;


public abstract class SingleWorker {

    //JobScheduler的相关job id
    public static final int PUSH_JOB_ID = 1001;
    public static final int FAKE_JOB_ID = 1003;

    protected JobCallback mJobCallback;

    public SingleWorker(JobCallback jobCallback) {
        mJobCallback = jobCallback;
    }

    public abstract int getJobId();

    public void onReceivedStartJob(JobParameters params) {
        start(params);
    }

    public abstract int start(JobParameters params);


    public void onReceivedStopJob(JobParameters params) {
    }
}