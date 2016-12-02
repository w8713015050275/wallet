package com.letv.walletbiz.update.task;

import android.content.Context;
import com.letv.walletbiz.update.beans.RemoteAppInfo;
import com.letv.walletbiz.update.service.UpgradeService;
import org.xutils.common.task.PriorityExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangzhiwei1 on 16-10-13.
 */

public class DownLoadManager {
    private List<DownloadTask> mDownloadTasks;
    private PriorityExecutor mExecutor;

    public DownLoadManager(PriorityExecutor priorityExecutor) {
        mExecutor = priorityExecutor;
        mDownloadTasks = new ArrayList<DownloadTask>();
    }

    public void startDownload(Context context, RemoteAppInfo appInfo, UpgradeService.DownloadCallback downloadCallback, int upgradeTypeByUser) {
        for (int i = mDownloadTasks.size() - 1; i >= 0 ; i--) {
            DownloadTask dt = mDownloadTasks.get(i);
            if (dt.getRemoteAppInfo().getPackageName().equals(appInfo.getPackageName())) {
                if (!dt.isFinish()) {
                    return;
                } else {
                    mDownloadTasks.remove(i);
                    break;
                }
            }
        }
        DownloadTask downloadTask = new DownloadTask(context,appInfo,downloadCallback,upgradeTypeByUser);
        mDownloadTasks.add(downloadTask);
        mExecutor.execute(downloadTask);
    }

    public void restartDownload(RemoteAppInfo appInfo) {
        for (int i = mDownloadTasks.size() - 1; i >= 0 ; i--) {
            DownloadTask dt = mDownloadTasks.get(i);
            if (dt.getRemoteAppInfo().getPackageName().equals(appInfo.getPackageName())) {
                dt.setFinish(false);
                mExecutor.execute(dt);
                break;
            }
        }
    }

    public void cancelAllNotification() {
        for (int i = mDownloadTasks.size() - 1; i >= 0 ; i--) {
            DownloadTask dt = mDownloadTasks.get(i);
            dt.cancelNotification();
            mDownloadTasks.remove(dt);
        }
        mDownloadTasks.clear();
    }
}
