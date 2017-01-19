package com.letv.walletbiz.movie.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.shared.widget.LeLoadingDialog;
import com.letv.shared.widget.LeRadioButton;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.widget.PhoneEditText;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.activity.PayWebViewActivity;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.pay.PayAdapter;
import com.letv.walletbiz.base.pay.PayBase;
import com.letv.walletbiz.base.pay.PrepayBean;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.StringUtils;
import com.letv.walletbiz.movie.beans.BaseMovieOrder;
import com.letv.walletbiz.movie.beans.LockSeatOrder;
import com.letv.walletbiz.movie.beans.MoviePayResult;
import com.letv.walletbiz.movie.beans.MovieProduct;
import com.letv.walletbiz.movie.ui.TimerTextView;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MoviePayResultTask;
import com.letv.walletbiz.movie.utils.MoviePriorityExecutorHelper;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.xutils.common.task.PriorityExecutor;

/**
 * Created by linquan on 15-12-7.
 */
public class MoviePayActivity extends BaseWalletFragmentActivity implements PhoneEditText.ActionCallback, PayBase.Callback {

    private static final String TAG = "MoviePayActivity";

    public static final String EXTRA_FROM_ORDER_LIST = "from_order_list";

    private static final int PAY_RESULT_RETURN = 101;
    private static final int PAY_ASYNC_EXIT = 100;

    private static final int MOBILE_LEN = 11;

    private static final String MSG_RESULT = "result";
    private static final String MSG_STATUS = "status";
    private static String mPromptInputRightNumber;

    private PriorityExecutor mExecutor;
    private MoviePayResultTask mMoviePayResultTask;

    protected PayAdapter mAdapter;
    protected MovieProduct mProduct;

    private payTask mTask;
    private RadioGroup mPayOptionGroup;
    private TextView mPayBtn;

    private Toast mPhoneNumberToast;
    private Toast mErrorPhoneNumberToast;
    private LeLoadingDialog mPayResultQueryDialog;
    private LeBottomSheet mUserSelectDialog;
    private LeBottomSheet mUnlockDialog;

    private PhoneEditText mPhoneEditView;
    private TextView mPhoneView;

    private boolean isPaying = false;

    private boolean isFromOrderList = false;

    private TextView mRemainTimeLabelView;
    private TimerTextView mRemainTimeView;

    private TimerTextView.OnTimerFinishedListener mOnTimerFinishedListener = new TimerTextView.OnTimerFinishedListener() {

        @Override
        public void onTimerFinished(View view) {
            mRemainTimeLabelView.setText(R.string.movie_pay_overtime);
            mRemainTimeView.setVisibility(View.GONE);
            Toast.makeText(MoviePayActivity.this, R.string.movie_pay_overtime, Toast.LENGTH_LONG).show();
            setPayButtonState(false);
        }
    };

    private MovieCommonCallback<MoviePayResult> mCallback = new MovieCommonCallback<MoviePayResult>() {

        @Override
        public void onLoadFinished(MoviePayResult result, int errorCode) {
            mMoviePayResultTask = null;
            Message message = mHandler.obtainMessage(PAY_RESULT_RETURN);
            message.obj = result;
            message.arg1 = errorCode;
            message.sendToTarget();
        }
    };


    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PAY_RESULT_RETURN:
                    mTask = null;
                    hidePayResultQueryDialog();
                    MoviePayResult payResult = (MoviePayResult) msg.obj;
                    if (msg.arg1 == MovieCommonCallback.NO_ERROR) {
                        if (payResult.status == MoviePayResult.PAY_RESULT_PAID) {
                            if (mProduct != null) {
                                Action.uploadPay(Action.MOVIE_PAY_SUCCESS, String.valueOf(mProduct.getMovieId()), String.valueOf(mProduct.getCinemaId()));
                            }
                            mProduct.showPayResult(MoviePayActivity.this, Constants.RESULT_STATUS.SUCCESS, null);
                            finish();
                        }
                    } else {
                        if (isNetworkAvailable()) {
                            showUserSelectDialog(MoviePayActivity.this);
                        }
                    }
                    break;
                case PAY_ASYNC_EXIT:
                    LogHelper.d("[%s] PAY_ASYNC_EXIT get %d", TAG, msg.obj);
                    if (msg.arg1 == Constants.PAY_STATUS.STATUS_OK) {
                        isPaying = true;
                    }
                    if (msg.arg1 != Constants.PAY_STATUS.STATUS_PAY_STARTED)
                        mTask = null;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        Bundle bundle = getIntent().getExtras();
        mProduct = (MovieProduct) bundle.getSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT);
        isFromOrderList = bundle.getBoolean(EXTRA_FROM_ORDER_LIST, false);
        if (mProduct == null) {
            finish();
        }

        mExecutor = MoviePriorityExecutorHelper.getPriorityExecutor();

        mAdapter = (PayAdapter) mProduct.getPayAdapter();
        setTitle(mAdapter.getTitle());

        setContentView(R.layout.movie_pay_main);
        initView();
    }

    private void initView() {
        mPromptInputRightNumber = getString(com.letv.wallet.common.R.string.phonenumber_prompt_input_right_number);
        BaseMovieOrder order = mProduct.getMovieOrder();
        mRemainTimeView = (TimerTextView) findViewById(R.id.remain_time);
        mRemainTimeView.setOnTimerFinishedListener(mOnTimerFinishedListener);
        mRemainTimeLabelView = (TextView) findViewById(R.id.remain_time_label);
        TextView movieNameView = (TextView) findViewById(R.id.movie_name);
        movieNameView.setText(mProduct.getProductName());
        TextView cinemaNameView = (TextView) findViewById(R.id.cinema_address);
        cinemaNameView.setText(mProduct.getCinemaName());
        TextView cinemaRoomView = (TextView) findViewById(R.id.cinema_room);
        cinemaRoomView.setText(mProduct.getRoomText());
        TextView dateView = (TextView) findViewById(R.id.movie_time);
        dateView.setText(mProduct.getDate());
        TextView seatView = (TextView) findViewById(R.id.seat_info);
        seatView.setText(mProduct.getSeatDesc(this));

        mPhoneView = (TextView) findViewById(R.id.movie_phone_number);
        mPhoneEditView = (PhoneEditText) findViewById(R.id.movie_phone_number_edit);
        mPhoneEditView.setTextSize(DensityUtils.px2dip(14.0F));
        mPhoneEditView.setVerificationLevel(PhoneEditText.PHONENUMBER_SIMPLE_VERIFICATION);
        mPhoneEditView.setCallback(this);
        String phoneNumber = PhoneNumberUtils.checkPhoneNumber(CommonConstants.PHONENUMBER_SIMPLE_REGEX, mProduct.getPhoneNumber(), true);
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneEditView.setVisibility(View.VISIBLE);
            mPhoneView.setVisibility(View.GONE);
            showInputMethod(mPhoneEditView, this);
        } else {
            mPhoneView.setText(phoneNumber);
            mPhoneView.setVisibility(View.VISIBLE);
            mPhoneEditView.setVisibility(View.GONE);
        }

        ImageView editButton = (ImageView) findViewById(R.id.btn_edit_phone_number);
        editButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (View.VISIBLE != mPhoneEditView.getVisibility()) {
                    mPhoneView.setVisibility(View.GONE);
                    mPhoneEditView.setVisibility(View.VISIBLE);
                    mPhoneEditView.setText(PhoneNumberUtils.checkPhoneNumber(CommonConstants.PHONENUMBER_SIMPLE_REGEX, mProduct.getPhoneNumber(), true));
                    mPhoneEditView.requestEdittextFocus();
                }
                showInputMethod(mPhoneEditView.getEditText(), MoviePayActivity.this);
            }
        });

        ViewGroup footWrapper = (ViewGroup) findViewById(R.id.id_footnoted);
        View footer = mAdapter.createFootNotedView(this, footWrapper);
        if (footer != null) {
            footWrapper.addView(footer);
        }

        TextView tCost = (TextView) findViewById(R.id.tv_cost);
        if (tCost != null) {
            tCost.setText(StringUtils.getPriceUnit(getBaseContext(), mAdapter.getCost()));
        }

        TextView tPrice = (TextView) findViewById(R.id.tv_price); // 原始价格
        TextView tDiscout = (TextView) findViewById(R.id.tv_discount); // 优惠金额
        TextView tDiscoutLabel = (TextView) findViewById(R.id.tv_label_discount);
        if (order != null) {
            tPrice.setText(StringUtils.getPriceUnit(getBaseContext(), order.getMovieOriginalPrice()));
            String discountDes;
            if (order.hasDiscount() == LockSeatOrder.ORDER_HAS_DISCOUNT && order.getMovieDiscount() != null) {
                LockSeatOrder.Discount discountInfo = order.getMovieDiscount();
                tDiscout.setText(StringUtils.getPriceUnit(getBaseContext(), discountInfo.discount_price) + "×" + discountInfo.count + "  " +
                        +(0 - discountInfo.discount_price * discountInfo.count));
                discountDes = discountInfo.name;
            } else {
                tDiscout.setText("-￥0");
                discountDes = getString(R.string.label_order_desc_null_discount);
            }
            tDiscoutLabel.setText(getMovieDiscountDes(discountDes));
        }

        mPayBtn = (TextView) findViewById(R.id.ibtn_pay);
        //By default, set first radiobutton as selected
        mPayOptionGroup = (RadioGroup) findViewById(R.id.pay_by_radiogroup);
        ((LeRadioButton) mPayOptionGroup.getChildAt(1)).setChecked(true);
    }

    private void updateRemainTimeView(BaseMovieOrder order) {
        if (order == null || mRemainTimeView == null) {
            return;
        }
        mRemainTimeView.setRemainTime(order.getLockExpireTime() - Math.max(0, System.currentTimeMillis() / 1000 - order.getLockTime()));
    }

    public CharSequence getMovieDiscountDes(String discountDes) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.label_order_desc_discount_price) + ":  ");
        builder.append(discountDes, new ForegroundColorSpan(getColor(R.color.order_detial_lightgray_tv_color)), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }

    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            hideBlankPage();
            if (isPaying) {
                isPaying = false;
                queryPayResult();
            }
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    private void showInputMethod(View view, Context context) {
        if (view == null || context == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    protected void hideSoftkeyboard(View view) {
        if (view == null)
            return;
        mPhoneEditView.clearEdittextFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaying && isNetworkAvailable()) {
            isPaying = false;
            queryPayResult();
        }
        if (mProduct != null) {
            updateRemainTimeView(mProduct.getMovieOrder());
        }
    }

    private void queryPayResult() {
        if (mMoviePayResultTask != null) {
            return;
        }
        mMoviePayResultTask = new MoviePayResultTask(this, mProduct.getSN(), mCallback);
        mExecutor.execute(mMoviePayResultTask);
        showPayResultQueryDialog();
    }

    private void showPayResultQueryDialog() {
        if (mPayResultQueryDialog == null) {
            mPayResultQueryDialog = new LeLoadingDialog(this, 1, 48);
            mPayResultQueryDialog.setTitleStr(getString(R.string.movie_dialog_query_pay_result));
        }
        mPayResultQueryDialog.show();
    }

    private void hidePayResultQueryDialog() {
        if (mPayResultQueryDialog != null && mPayResultQueryDialog.isShowing()) {
            mPayResultQueryDialog.dismiss();
        }
    }

    private void showUnlockDialog(Context context) {
        BaseMovieOrder order = mProduct.getMovieOrder();
        long time = order.getLockExpireTime() - (System.currentTimeMillis() / 1000 - order.getLockTime());
        if (isFromOrderList || time <= 0) {
            finish();
            return;
        }
        if (mUnlockDialog == null) {
            mUnlockDialog = new LeBottomSheet(this);
            mUnlockDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mUnlockDialog.dismiss();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mUnlockDialog.dismiss();
                            finish();
                        }
                    }, null,
                    new String[]{
                            context.getString(R.string.movie_ticket_back_unlock_cancel),
                            context.getString(R.string.movie_ticket_back_unlock_back)
                    },
                    context.getString(R.string.movie_ticket_back_unlock_title),
                    null, null, context.getResources().getColor(R.color.colorBtnBlue), false);
        }
        if (!mUnlockDialog.isShowing()) {
            mUnlockDialog.show();
        }
    }

    private void showUserSelectDialog(Context context) {
        if (mUserSelectDialog == null) {
            mUserSelectDialog = new LeBottomSheet(this);
            mUserSelectDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mUserSelectDialog.dismiss();
                            queryPayResult();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mUserSelectDialog.dismiss();
                        }
                    },
                    null,
                    new String[]{
                            context.getString(R.string.movie_dialog_user_select_result_success),
                            context.getString(R.string.movie_dialog_user_select_result_failed)
                    },
                    context.getString(R.string.movie_dialog_user_select_result_title),
                    null, null, context.getResources().getColor(R.color.colorBtnBlue), false);
        }
        mUserSelectDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRemainTimeView != null) {
            mRemainTimeView.setOnTimerFinishedListener(null);
        }
        isPaying = false;
        if (mPayResultQueryDialog != null) {
            mPayResultQueryDialog.onDismissDialog4DestroyContext();
        }
        if (mUserSelectDialog != null) {
            mUserSelectDialog.dismiss();
        }
    }

    private void setPayButtonState(boolean state) {
        if (mPayBtn != null) {
            mPayBtn.setEnabled(state);
        }
    }

    @Override
    public void onBackPressed() {
        LogHelper.d("[%s] onBackPressed mTask is%s null ", TAG, mTask == null ? "" : " not");
        if (mPhoneEditView != null) {
            hideSoftkeyboard(mPhoneEditView.getEditText());
        }
        if (mTask != null)
            return;
        showUnlockDialog(this);
    }

    public void startPay(View v) {
        LogHelper.d("[%s] startPay mTask is%s null ", TAG, mTask == null ? "" : " not");

        if (TextUtils.isEmpty(mProduct.getPhoneNumber())) {
            if (mPhoneNumberToast == null) {
                mPhoneNumberToast = Toast.makeText(this, R.string.movie_phone_number_empty_toast, Toast.LENGTH_LONG);
            }
            mPhoneNumberToast.show();
            return;
        }
        String phoneNumber = mProduct.getPhoneNumber().replaceAll(" ", "");
        if (TextUtils.isEmpty(PhoneNumberUtils.checkPhoneNumber(phoneNumber, false))) {
            if (mErrorPhoneNumberToast == null) {
                mErrorPhoneNumberToast = Toast.makeText(this, R.string.movie_phone_number_error_toast, Toast.LENGTH_LONG);
            }
            mErrorPhoneNumberToast.show();
            return;
        }
        if (mTask == null) {
            int radioId = mPayOptionGroup.getCheckedRadioButtonId();
            int platform;
            switch (radioId) {
                case R.id.rbtn_wechat_mobile:
                    LogHelper.d("[%s] startPay radioId is %d ", TAG, radioId);
                    if (!WXAPIFactory.createWXAPI(this, (String) null).isWXAppInstalled()) {
                        showStatusToast(Constants.PAY_STATUS.STATUS_NOWECHAT_ERROR);
                        return;
                    } else if (WXAPIFactory.createWXAPI(this, (String) null).getWXAppSupportAPI() < 570425345) {
                        showStatusToast(Constants.PAY_STATUS.STATUS_WECHAT_NOPAY_ERROR);
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

            if (!NetworkHelper.isNetworkAvailable()) {
                showStatusToast(Constants.PAY_STATUS.STATUS_NONETWORK_ERROR);
            } else {
                mTask = new payTask(platform);
                mTask.execute();
            }
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
        msg.arg1 = status;
        msg.what = PAY_ASYNC_EXIT;
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onNumberChanged(String content, int length) {
        mProduct.setPhoneNumber(content);
        return false;
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
            String payInfo = mAdapter.getPrepayInfo(MoviePayActivity.this, mPlatform);

            if (!TextUtils.isEmpty(payInfo)) {
                if (payInfo.startsWith("http")) {
                    Intent intent = new Intent(MoviePayActivity.this, PayWebViewActivity.class);
                    intent.putExtra(CommonConstants.EXTRA_URL, payInfo);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payInfo));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
            showStatusToast(result);
            onPayAsyncExit(mStatus);
        }
    }

    private void showStatusToast(int result) {
        switch (result) {
            case Constants.PAY_STATUS.STATUS_PAYSTART_ERROR:
                //Todo show prompt
                Toast.makeText(MoviePayActivity.this, R.string.pay_selected_option_dont_work,
                        Toast.LENGTH_SHORT).show();
                break;
            case Constants.PAY_STATUS.STATUS_PAYINFO_ERROR:
                Toast.makeText(MoviePayActivity.this, R.string.pay_info_response_error,
                        Toast.LENGTH_SHORT).show();
                break;
            case Constants.PAY_STATUS.STATUS_NETWORK_ERROR:
                Toast.makeText(MoviePayActivity.this, R.string.empty_network_error,
                        Toast.LENGTH_SHORT).show();
                break;
            case Constants.PAY_STATUS.STATUS_NONETWORK_ERROR:
                Toast.makeText(MoviePayActivity.this, R.string.empty_no_network,
                        Toast.LENGTH_SHORT).show();
                break;
            case Constants.PAY_STATUS.STATUS_NOWECHAT_ERROR:
                Toast.makeText(this, R.string.pay_wx_notavailable_prompt, Toast.LENGTH_LONG).show();
                break;
            case Constants.PAY_STATUS.STATUS_WECHAT_NOPAY_ERROR:
                Toast.makeText(this, R.string.pay_wx_pay_notavailable_prompt, Toast.LENGTH_LONG).show();
                break;
        }
    }

}
