package com.letv.walletbiz.member.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeCheckBox;
import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.StringUtils;
import com.letv.walletbiz.base.widget.CouponBrief;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.CouponBean;
import com.letv.walletbiz.member.beans.OrderCreateBean;
import com.letv.walletbiz.member.beans.OrderPayBean;
import com.letv.walletbiz.member.pay.MemberProduct;
import com.letv.walletbiz.member.task.CouponAvailableTask;
import com.letv.walletbiz.member.task.OrderCreateTask;
import com.letv.walletbiz.member.task.OrderPrePayTask;
import com.letv.walletbiz.member.util.MemberCommonCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhanghuancheng on 16-11-16.
 */
public class MemberOrderConfirmActivity extends AccountBaseActivity {
    private static final int PLACE_ORDER_RET = 100;
    private static final int EXCEPTION_RET = 101;
    private static final int PAY_INFO_RET = 102;

    private static final int EXCEPTION_PROMPT1 = 200;
    private static final int EXCEPTION_PROMPT2 = 201;
    private static final int EXCEPTION_PROMPT3 = 202;

    public static final int COUPON_LIST_FORRESULT = 1000;
    public static final int PAY_REQUEST_CODE = 1001;

    public static final int SUCCESSED = 1;//支付成功
    public static final int PAYINFO_ERROR = 2;//支付信息错误
    public static final int PAY_CANCLE = 3;//取消支付
    public static final int PAY_FAILED = 4;//支付失败
    public static final int PAY_USEBYOTHER = 5;//支付中

    private static float zero_f = 0F;

    private String mOrderSN;

    private TextView mBtnAction;

    private TextView mTvProductName;
    private TextView mTvDuration;
    private TextView mTvDescription;
    private CouponBrief mCouponV;
    private TextView mTvPrice;
    private TextView mTvDiscount;
    private TextView mTvCost;
    private LinearLayout mBtnlayout;
    private TextView mAgreeTV;
    private LeCheckBox mAgreeCB;

    private long mUcouponId;
    private ProgressDialog mDialog;
    private OrderPrePayTask mPrePayTask;
    private MemberProduct mMemberProduct;
    private boolean mSelectedCoupon;
    private CouponAvailableTask mAvailableCouponListTask;
    private OrderCreateTask mOrderCreateTask;
    private CouponBean[] mCouponBeans;
    private String mSkus;

    private boolean mIsFirstLoadData = true;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLACE_ORDER_RET:
                    if (mOrderCreateTask != null) {
                        mOrderCreateTask = null;
                    }
                    if (mMemberProduct != null) {
                        String order_sn = (String) msg.obj;
                        mMemberProduct.setSN(order_sn);
                        mOrderSN = order_sn;
                        queryCouponInfo();
                        runPrePayTask();
                    } else {
                        sendMessageRet(EXCEPTION_PROMPT1);
                    }
                    break;
                case PAY_INFO_RET:
                    hideDialog();
                    switch (msg.arg1) {
                        case Constants.PAY_STATUS.STATUS_OK:
                            if (msg.obj != null) {
                                String info = (String) msg.obj;
                                if (mMemberProduct != null) {
                                    mMemberProduct.setPrepayInfo(info);
                                }
                                goToPay(info);
                            } else {
                                sendMessageRet(EXCEPTION_PROMPT3);
                            }
                            break;
                        case Constants.PAY_STATUS.STATUS_NETWORK_ERROR:
                            sendMessageRet(EXCEPTION_PROMPT2);
                            break;
                        case Constants.PAY_STATUS.STATUS_PAYINFO_ERROR:
                            sendMessageRet(EXCEPTION_PROMPT3);
                            break;
                    }
                    break;
                case EXCEPTION_RET:
                    hideDialog();
                    if (mOrderCreateTask != null) {
                        mOrderCreateTask = null;
                    }
                    switch (msg.arg1) {
                        case EXCEPTION_PROMPT1:
                            Toast.makeText(MemberOrderConfirmActivity.this, R.string.mobile_request_order_fail_prom, Toast.LENGTH_SHORT).show();
                            break;
                        case EXCEPTION_PROMPT2:
                            promptNoNetWork();
                            break;
                        case EXCEPTION_PROMPT3:
                            Toast.makeText(MemberOrderConfirmActivity.this, R.string.pay_info_response_error, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private boolean mIsShowDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.member_order_confirm_layout);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case COUPON_LIST_FORRESULT:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    Bundle bundle = data.getExtras();
                    if (bundle == null) return;
                    int couponListCount = bundle.getInt(MemberConstant.PARAM.COUPON_LIST_COUNT_KEY);
                    CouponBean couponBean = (CouponBean) bundle.getSerializable(MemberConstant.PARAM.COUPON_DATA_KEY);
                    mUcouponId = bundle.getLong(MemberConstant.PARAM.COUPON_ID_KEY);
                    if (couponBean != null) {
                        mSelectedCoupon = true;
                    }
                    updateCouponAndPriceUi(couponBean, couponListCount, true);
                }
                break;
            case PAY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    int ret = data.getExtras().getInt(Constants.INFO_PARAM.LEPAY_RETURN_RESULT);
                    int payResult = Constants.RESULT_STATUS.FAIL;
                    switch (ret) {
                        case SUCCESSED://支付成功
                            payResult = Constants.RESULT_STATUS.SUCCESS;
                            break;
                        case PAYINFO_ERROR://支付信息错误
                            payResult = Constants.RESULT_STATUS.FAIL;
                            break;
                        case PAY_CANCLE://取消支付
                            payResult = Constants.RESULT_STATUS.CANCEL;
                            break;
                        case PAY_FAILED://支付失败
                            payResult = Constants.RESULT_STATUS.FAIL;
                            break;
                        case PAY_USEBYOTHER:
                            payResult = Constants.RESULT_STATUS.PENDING;
                            Toast.makeText(MemberOrderConfirmActivity.this, R.string.pay_unable_to_pay_prompt, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (mMemberProduct != null) {
                        mMemberProduct.showPayResult(MemberOrderConfirmActivity.this, payResult);
                    }
                    if (payResult == Constants.RESULT_STATUS.SUCCESS) {
                        finish();
                    }
                }
                break;
        }
    }

    private void initView() {
        Bundle bundle = getIntent().getExtras();
        mMemberProduct = (MemberProduct) bundle.getSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT);
        Action.uploadExpose(Action.MEMBER_PRODUCT_ORDER_EXPOSE, String.valueOf(mMemberProduct.getProductId()), null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sku", mMemberProduct.getSku_no());
            jsonObject.put("num", MemberConstant.PARAM.PRODUCT_COUNT);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            mSkus = jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mMemberProduct == null) {
            finish();
            return;
        }

        mTvProductName = (TextView) findViewById(R.id.tv_product_name);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mTvDescription = (TextView) findViewById(R.id.tv_detail);

        mCouponV = (CouponBrief) findViewById(R.id.v_coupon);

        mTvPrice = (TextView) findViewById(R.id.tv_price);
        mTvDiscount = (TextView) findViewById(R.id.tv_discount);
        mTvCost = (TextView) findViewById(R.id.tv_cost);

        mBtnlayout = (LinearLayout) findViewById(R.id.btn_layout);
        mAgreeCB = (LeCheckBox) findViewById(R.id.agree_check_box);
        mAgreeTV = (TextView) findViewById(R.id.agree_agreement_link);

        mBtnAction = (TextView) findViewById(R.id.ibtn_action);

        mDialog = new ProgressDialog(MemberOrderConfirmActivity.this);

        setTitle(R.string.mobile_order_view_surelabel);
        mTvProductName.setText(mMemberProduct.getProductName());
        mTvDuration.setText(mMemberProduct.getDuration() + getResources().getString(R.string.member_month_count));
        mTvDescription.setText(mMemberProduct.getDescription());
        mTvPrice.setText(StringUtils.getPriceUnit(getBaseContext(), mMemberProduct.getPrice()));
        mTvCost.setText(StringUtils.getPriceUnit(getBaseContext(), mMemberProduct.getPrice()));

        mBtnlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAgreeCB.isChecked()) {
                    Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.member_prompt_not_agree_agreement), Toast.LENGTH_SHORT).show();
                }
            }
        });
        String agreeStringFormat = getResources().getString(R.string.member_agree_agreement);
        String agreeString = String.format(agreeStringFormat, mMemberProduct.getType());
        mAgreeTV.setText(agreeString);
        mAgreeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberOrderConfirmActivity.this, MemberAgreementActivity.class);
                intent.putExtra(CommonConstants.EXTRA_URL, mMemberProduct.getProtocol_url());
                intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, mMemberProduct.getType() + MemberOrderConfirmActivity.this.getString(R.string.label_agreement));
                startActivity(intent);
            }
        });
        mAgreeCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBtnAction.setEnabled(true);
                    mBtnAction.setClickable(true);
                } else {
                    mBtnAction.setEnabled(false);
                    mBtnAction.setClickable(false);
                }
            }
        });

        mAgreeCB.setChecked(true);
        mBtnAction.setText(R.string.pay_now);

        mBtnAction.setEnabled(true);
        mBtnAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT).show();
                    return;
                }
                Action.uploadBuy(Action.MEMBER_PAY_NOW_PURCHASE, null);
                showDialog();
                runOrderSNAsyncTask(mMemberProduct.getSku_no(), new String[]{String.valueOf(mUcouponId)});
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!AccountHelper.getInstance().isLogin(this)) {
            showNoLoginBlankPage();
            return;
        }
        if (mIsShowDialog && isNetworkAvailable()) {
            showDialog();
            return;
        }
        if (!isNetworkAvailable()) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
        if (mIsFirstLoadData) {
            loadData();
        }
    }

    private void loadData() {
        if (isNetworkAvailable()) {
            if (mSelectedCoupon || mMemberProduct == null) return;
            mIsFirstLoadData = false;
            queryCouponInfo();
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if (mCouponBeans == null) {
                loadData();
            } else {
                hideBlankPage();
            }
        }
    }

    @Override
    protected void onStop() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mDialog = null;
        mSelectedCoupon = false;
        super.onDestroy();
    }

    private void showDialog() {
        mDialog.setTitle(R.string.wallet_prompt_title);
        mDialog.setMessage(getString(R.string.wallet_prompt_create_order));
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
        mIsShowDialog = true;
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mIsShowDialog = false;
        }
    }

    private void queryCouponInfo() {
        if (!isNetworkAvailable()) {
            sendMessageRet(EXCEPTION_PROMPT2);
            return;
        }
        if (mIsFirstLoadData) {
            showLoadingView();
        }
        String uToken = AccountHelper.getInstance().getToken(MemberOrderConfirmActivity.this);
        if (mAvailableCouponListTask == null) {

            mAvailableCouponListTask = new CouponAvailableTask(uToken, mSkus, new MemberCommonCallback<CouponBean[]>() {

                @Override
                public void onLoadFinished(CouponBean[] result, int errorCode, boolean needUpdate) {
                    hideLoadingView();
                    if (result == null) {
                        if (!isNetworkAvailable())
                            sendMessageRet(EXCEPTION_PROMPT2);
                    }
                    updateCouponData(result);
                    mAvailableCouponListTask = null;
                }
            });
        }
        ExecutorHelper.getExecutor().runnableExecutor(mAvailableCouponListTask);
    }

    private void updateCouponData(CouponBean[] couponBeanList) {
        mCouponBeans = couponBeanList;
        CouponBean couponBean = null;
        int couponlistCount = 0;
        if (mCouponBeans != null && mCouponBeans.length > 0) {
            couponlistCount = mCouponBeans.length;
            if (mUcouponId != 0) {
                couponlistCount = mCouponBeans.length;
                for (int i = 0; i < mCouponBeans.length; i++) {
                    if (mCouponBeans[i].ucoupon_id == mUcouponId) {
                        couponBean = mCouponBeans[i];
                    }
                }
            }
        }
        updateCouponAndPriceUi(couponBean, couponlistCount, false);
    }

    private void setDiscountPrice(float price) {
        if (price > 0.0F) {
            mTvDiscount.setText(StringUtils.getDiscountPriceUnit(price));
        } else {
            mTvDiscount.setText("-￥0");
        }
    }

    private void setCouponListCount(int count) {
        if (mCouponV == null) return;
        mCouponV.setContent(String.format(getString(R.string.coupon_request_data), count));
    }

    public void updateCouponAndPriceUi(CouponBean couponBean, int couponlistCount, Boolean recordCouponId) {
        if (couponBean == null) {
            if (recordCouponId) {
                mUcouponId = 0;
            }
            mTvPrice.setText(StringUtils.getPriceUnit(getBaseContext(), mMemberProduct.getPrice()));
            setDiscountPrice(zero_f);
            mTvCost.setText(StringUtils.getPriceUnit(getBaseContext(), mMemberProduct.getPrice()));
        } else {
            if (recordCouponId) {
                mUcouponId = couponBean.ucoupon_id;
            }
            mTvPrice.setText(StringUtils.getPriceUnit(getBaseContext(), couponBean.total_price));
            setDiscountPrice(couponBean.discount_price);
            mTvCost.setText(StringUtils.getPriceUnit(getBaseContext(), couponBean.real_price));
            mMemberProduct.setPrice(String.valueOf(couponBean.real_price));
        }
        setCouponListCount(couponlistCount);
        mCouponV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemberOrderConfirmActivity.this, MemberCouponListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MemberConstant.PARAM.COUPONLIST_KEY, mCouponBeans);
                bundle.putLong(MemberConstant.PARAM.COUPON_ID_KEY, mUcouponId);
                bundle.putString(MemberConstant.PARAM.MEMBER_SKUS, mSkus);
                intent.putExtras(bundle);
                startActivityForResult(intent, COUPON_LIST_FORRESULT);
            }
        });
    }

    private void sendMessageRet(int arg1) {
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = EXCEPTION_RET;
            msg.arg1 = arg1;
            handler.sendMessage(msg);
        }
    }

    private void runPrePayTask() {
        if (mDialog != null) {
            mDialog.setMessage(getString(R.string.wallet_prompt_create_payinfo));
        }
        if (mPrePayTask == null) {
            String uToken = AccountHelper.getInstance().getToken(MemberOrderConfirmActivity.this);
            boolean dbg = false;
            if ("userdebug".equalsIgnoreCase(Build.TYPE)) {
                dbg = true;
            }
            mPrePayTask = new OrderPrePayTask(uToken, MemberConstant.PARAM.CLIENT_TYPE, mOrderSN, MemberConstant.PARAM.PLATFORM, dbg, new MemberCommonCallback<OrderPayBean>() {
                @Override
                public void onLoadFinished(OrderPayBean result, int errorCode, boolean needUpdate) {
                    int status = Constants.PAY_STATUS.STATUS_OK;
                    if (result != null) {
                        if (TextUtils.isEmpty(result.pay_info)) {
                            if (isNetworkAvailable()) {
                                status = Constants.PAY_STATUS.STATUS_PAYINFO_ERROR;
                            } else {
                                status = Constants.PAY_STATUS.STATUS_NETWORK_ERROR;
                            }
                        }
                        onPayAsyncExit(status, result.pay_info);
                    } else {
                        onPayAsyncExit(Constants.PAY_STATUS.STATUS_PAYINFO_ERROR, "");
                    }
                    mPrePayTask = null;
                }
            });
            ExecutorHelper.getExecutor().runnableExecutor(mPrePayTask);
        }
    }

    private void onPayAsyncExit(int status, String payInfo) {
        Message msg = Message.obtain();
        handler.removeMessages(PAY_INFO_RET);
        msg.what = PAY_INFO_RET;
        msg.arg1 = status;
        msg.obj = payInfo;
        handler.sendMessage(msg);
    }

    private void goToPay(String payInfo) {
        try {
            if (payInfo == null) return;
            Intent intent = new Intent(ActivityConstant.PAY_PARAM.PAY_ACTION);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INFO_PARAM.LEPAY_INFO, payInfo);
            intent.putExtras(bundle);
            startActivityForResult(intent, PAY_REQUEST_CODE);
        } catch (Exception e) {

        }
    }

    private void runOrderSNAsyncTask(String sku_no, String[] couponIds) {
        if (!isNetworkAvailable()) {
            sendMessageRet(EXCEPTION_PROMPT2);
            return;
        }
        String uToken = AccountHelper.getInstance().getToken(MemberOrderConfirmActivity.this);
        String coupons = "";
        if (couponIds != null && couponIds.length > 0) {
            int size = couponIds.length;
            for (int i = 0; i < size; i++) {
                if (i == (size - 1)) {
                    coupons += String.valueOf(couponIds[i]);
                } else {
                    coupons += (String.valueOf(couponIds[i]) + ",");
                }
            }
        }
        if (mOrderCreateTask == null) {
            mOrderCreateTask = new OrderCreateTask(uToken, sku_no, coupons, new MemberCommonCallback<OrderCreateBean>() {
                @Override
                public void onLoadFinished(OrderCreateBean result, int errorCode, boolean needUpdate) {
                    if (result != null && result.order_sn != null && errorCode == MemberCommonCallback.NO_ERROR) {
                        mSelectedCoupon = false;
                        Message msg = Message.obtain();
                        msg.obj = result.order_sn;
                        msg.what = PLACE_ORDER_RET;
                        handler.sendMessage(msg);
                    } else {
                        if (!isNetworkAvailable())
                            sendMessageRet(EXCEPTION_PROMPT2);
                        sendMessageRet(EXCEPTION_PROMPT1);
                    }
                }
            });
            ExecutorHelper.getExecutor().runnableExecutor(mOrderCreateTask);
        }
    }
}
