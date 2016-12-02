package com.letv.walletbiz.push.schedule;

import android.app.job.JobInfo;

/**
 * Created by lijujying on 16-7-12.
 */
public class RetryData {
    public int jobId = -1;
    public long minLatencyMillis = 0l;
    public long maxExecutionDelayMillis = 0l;
    public int networkType = JobInfo.NETWORK_TYPE_NONE;

    public RetryData(int jobId, long minLatencyMillis, long maxExecutionDelayMillis, int networkType) {
        this.jobId = jobId;
        this.minLatencyMillis = minLatencyMillis;
        this.maxExecutionDelayMillis = maxExecutionDelayMillis;
        this.networkType = networkType;
    }

}
