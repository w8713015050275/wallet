package com.letv.walletbiz.push.schedule;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import com.letv.walletbiz.push.schedule.worker.FakeWorker;
import com.letv.walletbiz.push.schedule.worker.JobCallback;
import com.letv.walletbiz.push.schedule.worker.PushWorker;
import com.letv.walletbiz.push.schedule.worker.SingleWorker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijujying on 16-7-12.
 */
public class ScheduleService extends JobService implements JobCallback {

    private static final String TAG = ScheduleService.class.getSimpleName();
    private Map<Integer, SingleWorker> mWorker;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mWorker == null) {
            mWorker = new HashMap<Integer, SingleWorker>(3);
        }
    }

    @Override
    public void onDestroy() {
        if (mWorker != null) {
            mWorker.clear();
            mWorker = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (mWorker == null || params == null) {
            return false;
        }
        final int jobId = params.getJobId();
        SingleWorker worker = mWorker.get(jobId);
        if (worker == null && (worker = createWork(jobId)) == null) {
            return false;
        }
        mWorker.put(jobId, worker);
        worker.onReceivedStartJob(params);
        return true;
    }

    /**
     * 系统回调结束该任务
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        if (mWorker == null || params == null) {
            return false;
        }
        SingleWorker worker = mWorker.get(params.getJobId());
        if (worker != null) {
            worker.onReceivedStopJob(params);
        }
        return false;
    }

    /**
     * 根据jobId构建对应的worker
     *
     * @param jobId
     * @return
     */
    private SingleWorker createWork(int jobId) {
        SingleWorker work = null;
        switch (jobId) {
            case SingleWorker.PUSH_JOB_ID:
                work = new PushWorker(this);
                break;
            case SingleWorker.FAKE_JOB_ID:
                work = new FakeWorker(this);
                break;
        }
        return work;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public boolean callJobFinished(JobParameters params, boolean needReschedule) {
        if (params == null) {
            return false;
        }
        jobFinished(params, needReschedule);
        return true;
    }
}
