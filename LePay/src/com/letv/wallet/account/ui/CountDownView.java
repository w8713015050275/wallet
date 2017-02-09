package com.letv.wallet.account.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.letv.wallet.R;
import com.letv.wallet.common.util.SharedPreferencesHelper;

import org.w3c.dom.Text;

/**
 * Created by lijunying on 17-2-10.
 */

public class CountDownView extends TextView {

    private TimeCount time;
    private String strS ;
    private static final long DURATION = 60000;
    private static final long INTERVAL = 1000;
    private static final String ACCOUNT_VERIFY_LAST_SMSCODE_TIME = "accountVerifyLastSmscodeTime";

    private TimeCount mTimer ;

    public CountDownView(Context context) {
        this(context, null);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strS = getResources().getString(R.string.account_verify_countdoun_s);
        long lastTick = SharedPreferencesHelper.getLong(ACCOUNT_VERIFY_LAST_SMSCODE_TIME, 0);
        if (lastTick != 0 && System.currentTimeMillis() - lastTick < DURATION) {
            mTimer = new TimeCount(System.currentTimeMillis() - lastTick, INTERVAL);
            mTimer.start();
        }else{
            setText(R.string.account_verify_get_sms_code);
            setEnabled(true);
        }
    }

    public void startTick(){
        SharedPreferencesHelper.putLong(ACCOUNT_VERIFY_LAST_SMSCODE_TIME, System.currentTimeMillis());
        mTimer = new TimeCount(DURATION, INTERVAL);
        mTimer.start();
    }

    public void cancle(){
        if (mTimer != null) {
            mTimer.cancel();
            SharedPreferencesHelper.putLong(ACCOUNT_VERIFY_LAST_SMSCODE_TIME, 0); //未成功发送短信， 清除缓存
            mTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            setEnabled(false);
            setText(millisUntilFinished / INTERVAL +strS);
        }

        @Override
        public void onFinish() {
            setText(R.string.account_verify_get_sms_code);
            setEnabled(true);
        }
    }

}
