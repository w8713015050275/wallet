package com.letv.walletbiz.push;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.letv.wallet.common.activity.BaseWebViewActivity;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.MainActivity;

import java.util.List;


/**
 * Created by lijujying on 16-7-12.
 */
public class PushWebActivity  extends BaseWebViewActivity {
    private static final String TAG = PushWebActivity.class.getSimpleName();

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isExsitMainActivity()) {
                startActivity(new Intent(PushWebActivity.this, MainActivity.class));
                LogHelper.d("%S Main is not in stack, start MainActivity", TAG);
            }
            finish();
        }
    };

    @Override
    protected boolean needUpdateTitle() {
        return true;
    }

    private boolean isExsitMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        ComponentName cmpName = intent.resolveActivity(getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            for (ActivityManager.AppTask task: tasks) {
                if (cmpName.equals(task.getTaskInfo().baseActivity)) { // 说明它已经启动了
                    flag = true;
                    break;
                }
            }
        }
        LogHelper.d("%S isExsitMainActivity = " + flag, TAG);
        return flag;
    }

    @Override
    protected View.OnClickListener getCloseButtonClickListener() {
        return mClickListener;
    }

    @Override
    public void onBackPressed() {
        LogHelper.d("%S onBackPressed", TAG);
        if(!mWebView.canGoBack()) {
            if (!isExsitMainActivity()) {
                startActivity(new Intent(this, MainActivity.class));
                LogHelper.d("%S Main is not in stack, start MainActivity", TAG);
            }
        }
        super.onBackPressed();
    }
}