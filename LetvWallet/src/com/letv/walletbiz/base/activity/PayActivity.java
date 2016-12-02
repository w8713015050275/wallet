package com.letv.walletbiz.base.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeRadioButton;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.pay.PayAdapter;
import com.letv.walletbiz.base.pay.PayBase;
import com.letv.walletbiz.base.pay.PrepayBean;
import com.letv.walletbiz.base.pay.Product;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.text.SimpleDateFormat;

/**
 * Created by linquan on 15-12-7.
 */
public class PayActivity extends BaseWalletFragmentActivity implements PayBase.Callback {

    private static final String TAG = "PayActivity";

    private static final int PAY_RESULT_RETURN = 101;
    private static final int PAY_ASYNC_EXIT = 100;

    private static final String MSG_RESULT = "result";
    private static final String MSG_STATUS = "status";


    protected PayAdapter mAdapter;
    protected Product mProduct;

    private payTask mTask;
    private RadioGroup mPayOptionGroup;
    private TextView mPayBtn;
    private TextView mCountDown;


    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PAY_RESULT_RETURN:
                    mTask = null;
                    Bundle b = msg.getData();
                    String status = b.getString(MSG_STATUS);
                    int result = b.getInt(MSG_RESULT);
                    if (mAdapter.onPayResult(result)) {
                        mProduct.showPayResult(PayActivity.this, result, status);
                    }
                    finish();
                    break;
                case PAY_ASYNC_EXIT:
                    LogHelper.d("[%s] PAY_ASYNC_EXIT get %d", TAG, msg.obj);
                    if ((Integer) msg.obj != Constants.PAY_STATUS.STATUS_PAY_STARTED)
                        mTask = null;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        mProduct = (Product) bundle.getSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT);

        if (mProduct == null) {
            finish();
        }

        mAdapter = (PayAdapter) mProduct.getPayAdapter();
        setTitle(mAdapter.getTitle());

        setContentView(R.layout.pay_main);

        ViewGroup wrapper = (ViewGroup) findViewById(R.id.id_content);
        View content = mAdapter.createContentView(this, wrapper);
        if (content != null) {
            wrapper.addView(content);
        }

        ViewGroup footWrapper = (ViewGroup) findViewById(R.id.id_footnoted);
        View footer = mAdapter.createFootNotedView(this, footWrapper);
        if (footer != null) {
            footWrapper.addView(footer);
        }

        TextView tCost = (TextView) findViewById(R.id.tv_cost);
        if (tCost != null) {
            tCost.setText(mAdapter.getCost());
        }

        mPayBtn = (TextView) findViewById(R.id.ibtn_pay);


        int timeLimitation = mAdapter.getTimeLimitation();
        if (timeLimitation != 0) {
            final long step = 1000;

            View v_counter = findViewById(R.id.v_counter);
            v_counter.setVisibility(View.VISIBLE);
            mCountDown = (TextView) findViewById(R.id.id_countdown);

            new CountDownTimer(timeLimitation * 60 * step, step) {

                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub

                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    mCountDown.setText(formatter.format(millisUntilFinished / step));
                }

                @Override
                public void onFinish() {
                    setPayButtonState(false);
                    // TODO Auto-generated method stub
                }
            }.start();
        }
        //By default, set first radiobutton as selected
        mPayOptionGroup = (RadioGroup) findViewById(R.id.pay_by_radiogroup);
        ((LeRadioButton) mPayOptionGroup.getChildAt(0)).setChecked(true);
    }

    private void setPayButtonState(boolean state) {
        if (mPayBtn != null) {
            mPayBtn.setEnabled(state);
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public void onBackPressed() {
        LogHelper.d("[%s] onBackPressed mTask is%s null ", TAG, mTask == null ? "" : " not");

        if (mTask != null)
            return;
        super.onBackPressed();
    }

    public void startPay(View v) {
        LogHelper.d("[%s] startPay mTask is%s null ", TAG, mTask == null ? "" : " not");

        if (mTask == null) {
            int radioId = mPayOptionGroup.getCheckedRadioButtonId();
            int platform;
            switch (radioId) {
                case R.id.rbtn_wechat_mobile:
                    LogHelper.d("[%s] startPay radioId is %d ", TAG, radioId);
                    if (!WXAPIFactory.createWXAPI(this, (String) null).isWXAppInstalled()) {
                        Toast.makeText(this, R.string.pay_wx_notavailable_prompt, Toast.LENGTH_LONG).show();
                        return;
                    } else if (WXAPIFactory.createWXAPI(this, (String) null).getWXAppSupportAPI() < 570425345) {
                        Toast.makeText(this, R.string.pay_wx_pay_notavailable_prompt, Toast.LENGTH_LONG).show();
                        return;
                    }
                    platform = Constants.PLATFORM.WECHAT;
                    break;
                case R.id.rbtn_ali_mobile:
                    LogHelper.d("[%s] startPay radioId is %d ", TAG, radioId);
                default:
                    platform = Constants.PLATFORM.ALIPAY;
                    break;
            }
            LogHelper.d("[%s] startPay platform is %d", TAG,
                    radioId, platform);

            mTask = new payTask(platform);
            mTask.execute();
        }
    }


    @Override
    public boolean onPayResult(int result, String status) {
        Message msg = Message.obtain();
        mHandler.removeMessages(PAY_RESULT_RETURN);
        Bundle b = new Bundle();
        b.putInt(MSG_RESULT, result);
        b.putString(MSG_STATUS, status);
        msg.setData(b);
        msg.what = PAY_RESULT_RETURN;
        mHandler.sendMessage(msg);
        return true;
    }


    public void onPayAsyncExit(int status) {
        Message msg = Message.obtain();
        mHandler.removeMessages(PAY_ASYNC_EXIT);
        msg.obj = status;
        msg.what = PAY_ASYNC_EXIT;
        mHandler.sendMessage(msg);
    }


    private class payTask extends AsyncTask<Integer, Integer, Integer> {
        private int mPlatform;
        private int mStatus = Constants.PAY_STATUS.STATUS_OK;

        public payTask() {
            this(Constants.PLATFORM.ALIPAY);
        }

        public payTask(int platform) {
            mPlatform = platform;

        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            BaseResponse<PrepayBean> response = null;
            String payInfo = mAdapter.getPrepayInfo(PayActivity.this, mPlatform);

            if (payInfo != null) {
                if (payInfo.startsWith("http")) {
                    Intent intent = new Intent(PayActivity.this, PayWebViewActivity.class);
                    intent.putExtra(CommonConstants.EXTRA_URL, payInfo);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payInfo));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            } else {
                mStatus = Constants.PAY_STATUS.STATUS_PAYINFO_ERROR;
            }
            LogHelper.d("[%s] doInBackground return %d", TAG, mStatus);
            return mStatus;
        }

        @Override
        protected void onPostExecute(Integer result) {
            LogHelper.d("[%s] onPostExecute result %d", TAG, result);
            switch (result) {
                case Constants.PAY_STATUS.STATUS_PAYSTART_ERROR:
                    //Todo show prompt
                    Toast.makeText(PayActivity.this, R.string.pay_selected_option_dont_work,
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PAY_STATUS.STATUS_PAYINFO_ERROR:
                    Toast.makeText(PayActivity.this, R.string.pay_info_response_error,
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PAY_STATUS.STATUS_NETWORK_ERROR:
                    Toast.makeText(PayActivity.this, R.string.pay_network_error,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            onPayAsyncExit(mStatus);
        }
    }

}
