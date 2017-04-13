package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.letv.wallet.common.util.DateUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liuliang on 16-2-17.
 */
public class TimerTextView extends TextView {

    public interface OnTimerFinishedListener extends Serializable {
        void onTimerFinished(View view);
    }

    private static final int MSG_UPDATE_TEXTVIEW = 1;
    private static final int MSG_TIMER_FINISHED = 2;

    private String mText;

    private long mDuration;

    private OnTimerFinishedListener mListener;

    private String mFormatter = "mm:ss";

    //unit:s
    private long mInterval = 1;

    //unit:ms
    private long mStartTime = -1;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TEXTVIEW:
                    setText(mText);
                    break;
                case MSG_TIMER_FINISHED:
                    mStartTime = -1;
                    setText(mText);
                    if (mListener != null) {
                        mListener.onTimerFinished(TimerTextView.this);
                    }
                    break;
            }
        }
    };

    private Timer mTimer;

    public TimerTextView(Context context) {
        this(context, null);
    }

    public TimerTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if (screenState == View.SCREEN_STATE_OFF) {
            stopTimer();
        } else if (screenState == View.SCREEN_STATE_ON) {
            if (mStartTime != -1) {
                setRemainTime(mDuration - (System.currentTimeMillis() - mStartTime) / 1000);
            }
        }
    }

    /**
     * @param duration unit:s
     */
    public void setRemainTime(long duration) {
        mDuration = duration;
        mStartTime = System.currentTimeMillis();
        setText(getTimeStr(duration));
        startTimerTask();
    }

    public void setOnTimerFinishedListener(OnTimerFinishedListener listener) {
        mListener = listener;
    }

    public void setTimeTextFormatter(String formatter) {
        mFormatter = formatter;
    }

    private void startTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                long currentDuration = mDuration - (System.currentTimeMillis() - mStartTime) / 1000;
                mText = getTimeStr(currentDuration);
                mHandler.removeMessages(MSG_UPDATE_TEXTVIEW);
                if (currentDuration <= 0) {
                    mHandler.obtainMessage(MSG_TIMER_FINISHED).sendToTarget();
                    mTimer.cancel();
                    mTimer = null;
                } else {
                    mHandler.obtainMessage(MSG_UPDATE_TEXTVIEW).sendToTarget();
                }
            }
        }, 0, mInterval * 1000);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mHandler.removeMessages(MSG_UPDATE_TEXTVIEW);
            mHandler.removeMessages(MSG_TIMER_FINISHED);
        }
    }

    /**
     *
     * @param interval unit:s
     */
    public void setInterval(int interval) {
        mInterval = interval;
    }

    private String getTimeStr(long duration) {
        if (duration <= 0) {
            duration = 0;
        }
        return DateUtils.formatDate(new Date(duration * 1000), mFormatter);
    }
}
