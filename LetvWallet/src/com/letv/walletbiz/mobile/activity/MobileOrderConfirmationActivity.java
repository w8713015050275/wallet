package com.letv.walletbiz.mobile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.tracker.enums.EventType;
import com.letv.tracker.enums.Key;
import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.StringUtils;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.base.widget.CouponBrief;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.CouponBean;
import com.letv.walletbiz.mobile.beans.CouponListBean;
import com.letv.walletbiz.mobile.beans.OrderBean;
import com.letv.walletbiz.mobile.pay.MobileProduct;
import com.letv.walletbiz.mobile.util.CouponListTask;
import com.letv.walletbiz.mobile.util.PayInfoCommonCallback;
import com.letv.walletbiz.mobile.util.PayPreInfoTask;
import com.letv.walletbiz.mobile.util.RecordPhoneNumberTask;

import org.xutils.xmain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by changjiajie on 16-4-6.
 */
public class MobileOrderConfirmationActivity extends AccountBaseActivity implements CommonCallback<CouponListBean<CouponBean>>, PayPreInfoTask.ProvidePayPreParams
        , PayInfoCommonCallback<String> {

    private static final String TAG = MobileOrderConfirmationActivity.class.getSimpleName();

    private static final int PLACE_ORDER_RET = 100;
    private static final int EXCEPTION_RET = 101;
    private static final int PAY_INFO_RET = 102;

    private static final int EXCEPTION_PROMPT1 = 200;
    private static final int EXCEPTION_PROMPT2 = 201;
    private static final int EXCEPTION_PROMPT3 = 202;

    public static final int COUPON_LIST_FORRESULT = 1000;
    public static final int PAY_REQUEST_CODE = 1001;

    private static final int PRODUCT_ID = -1;

    public static final int SUCCESSED = 1;//支付成功
    public static final int PAYINFO_ERROR = 2;//支付信息错误
    public static final int PAY_CANCLE = 3;//取消支付
    public static final int PAY_FAILED = 4;//支付失败
    public static final int PAY_USEBYOTHER = 5;//支付中

    private static float zero_f = 0F;

    private ProgressDialog mDialog;
    private Toast mToast;

    private CouponListTask mCouponAsyncT;
    private GetOrderSNTask mGetOrderSNTask;
    private PayPreInfoTask mPrePayTask;

    private String mOrderSN;

    private MobileProduct mMobileProduct;
    private CouponListBean<CouponBean> mCouponListBean;

    private TextView mBtnAction;

    private TextView mTvProductName;
    private TextView mTvNumber;
    private CouponBrief mCouponV;
    private TextView mTvPrice;
    private TextView mTvDiscount;
    private TextView mTvCost;

    private long mUcouponId;
    private long mUseUcouponId;
    private int mContactType;
    private int mFeeOrFlow;
    private boolean mIsGoPay = false;
    private boolean mIsShowDialog = false;
    private boolean mIsFirstLoadData = true;
    private boolean mSelectedCoupon = false;
    private String mFrom;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLACE_ORDER_RET:
                    if (mMobileProduct != null) {
                        OrderBean orderBean = (OrderBean) msg.obj;
                        mMobileProduct.setSN(orderBean.order_sn);
                        mOrderSN = orderBean.order_sn;
                        getPrePayInfoTask();
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
                                if (mMobileProduct != null) {
                                    mMobileProduct.setPrepayInfo(info);
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
                    switch (msg.arg1) {
                        case EXCEPTION_PROMPT1:
                            Toast.makeText(MobileOrderConfirmationActivity.this, R.string.mobile_request_order_fail_prom, Toast.LENGTH_SHORT).show();
                            break;
                        case EXCEPTION_PROMPT2:
                            promptNoNetWork();
                            break;
                        case EXCEPTION_PROMPT3:
                            Toast.makeText(MobileOrderConfirmationActivity.this, R.string.pay_info_response_error, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.mobile_order_confirmation_v);
        initV();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case COUPON_LIST_FORRESULT:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return;
                    Bundle bundle = data.getExtras();
                    if (bundle == null) return;
                    int couponListCount = bundle.getInt(MobileConstant.PARAM.COUPON_LIST_COUNT_KEY);
                    CouponBean couponBean = (CouponBean) bundle.getSerializable(MobileConstant.PARAM.COUPON_DATA_KEY);
                    if (couponBean != null) {
                        mSelectedCoupon = true;
                    }
                    setData(couponBean, couponListCount);
                }
                break;
            case PAY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    mIsGoPay = false;
                    int ret = data.getExtras().getInt(Constants.INFO_PARAM.LEPAY_RETURN_RESULT);
                    int payResult = Constants.RESULT_STATUS.FAIL;
                    switch (ret) {
                        case SUCCESSED://支付成功
                            payResult = Constants.RESULT_STATUS.SUCCESS;
                            if (mMobileProduct != null) {
                                ExecutorHelper.getExecutor().runnableExecutor(new RecordPhoneNumberTask(mMobileProduct));
                            }
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
                            Toast.makeText(MobileOrderConfirmationActivity.this, R.string.pay_unable_to_pay_prompt, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (mMobileProduct != null) {
                        mMobileProduct.showPayResult(MobileOrderConfirmationActivity.this, payResult);
                    }
                    if (payResult != Constants.RESULT_STATUS.SUCCESS) {
                        if (AccountHelper.getInstance().isLogin(this)) {
                            loadData();
                        } else {
                            showNoLoginBlankPage();
                        }
                        return;
                    }
                    finish();
                }
                break;
        }
    }

    private void initV() {
        Bundle bundle = getIntent().getExtras();
        mMobileProduct = (MobileProduct) bundle.getSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT);
        if (mMobileProduct == null) {
            finish();
            return;
        }
        mUcouponId = bundle.getLong(CouponConstant.EXTRA_COUPON_BEAN_ID);
        mContactType = bundle.getInt(MobileConstant.PARAM.CONTACT_TYPE_KEY);
        mFeeOrFlow = bundle.getInt(MobileConstant.PARAM.FEEFLOW_KEY);
        mFrom = bundle.getString(WalletConstant.EXTRA_FROM);
        mUseUcouponId = mUcouponId;
        mTvProductName = (TextView) findViewById(R.id.tv_product_name);
        mTvNumber = (TextView) findViewById(R.id.tv_number);
        mCouponV = (CouponBrief) findViewById(R.id.v_coupon);
        mTvPrice = (TextView) findViewById(R.id.tv_price);
        mTvDiscount = (TextView) findViewById(R.id.tv_discount);
        mTvCost = (TextView) findViewById(R.id.tv_cost);
        mBtnAction = (TextView) findViewById(R.id.ibtn_action);

        mToast = Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT);
        mDialog = new ProgressDialog(MobileOrderConfirmationActivity.this);

        setTitle(R.string.mobile_order_view_surelabel);
        mTvProductName.setText(mMobileProduct.getProductName());
        setTvNumber(mMobileProduct.getNumber());
        mTvPrice.setText(StringUtils.getPriceUnit(getBaseContext(), mMobileProduct.getPrice()));
        mTvCost.setText(StringUtils.getPriceUnit(getBaseContext(), mMobileProduct.getPrice()));

        mBtnAction.setText(R.string.pay_now);
        mBtnAction.setEnabled(true);
        mBtnAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!NetworkHelper.isNetworkAvailable()) {
                    mToast.show();
                    return;
                }
                showDialog();
                String widget;
                if (mFeeOrFlow == MobileConstant.PRODUCT_TYPE.MOBILE_FEE) {
                    widget = Action.MOBILE_FEE_PAY_CLICK;
                } else {
                    widget = Action.MOBILE_FLOW_PAY_CLICK;
                }
                Map<String, Object> props = new HashMap<String, Object>();
                props.put(WalletConstant.EXTRA_FROM, mFrom);
                props.put(Key.Content.getKeyId(), mContactType);
                Action.uploadCustom(EventType.Click, widget, props);
                getOrderSNAsyncTask(mMobileProduct.getNumber(), mMobileProduct.getProductId(), new long[]{mUseUcouponId});
            }
        });
    }

    public void setData(CouponBean couponBean, int couponlistCount) {
        if (couponBean == null) {
            mUseUcouponId = 0;
            mTvPrice.setText(StringUtils.getPriceUnit(getBaseContext(), mMobileProduct.getPrice()));
            setDiscountPrice(zero_f);
            setCouponListCount(0);
            mTvCost.setText(StringUtils.getPriceUnit(getBaseContext(), mMobileProduct.getPrice()));
            mMobileProduct.setRealPrice(mMobileProduct.getPrice());
        } else {
            mUseUcouponId = couponBean.ucoupon_id;
            mTvPrice.setText(StringUtils.getPriceUnit(getBaseContext(), couponBean.getTotalPrice()));
            setDiscountPrice(couponBean.getDiscountPrice());
            setCouponListCount(couponlistCount);
            mTvCost.setText(StringUtils.getPriceUnit(getBaseContext(), couponBean.getRealPrice()));
            try {
                mMobileProduct.setRealPrice(String.valueOf(couponBean.getRealPrice())); // 更新 price 为实付金额
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mCouponV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MobileOrderConfirmationActivity.this, MobileCouponListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(MobileConstant.PARAM.COUPONLIST_KEY, mCouponListBean);
                bundle.putLong(MobileConstant.PARAM.COUPON_ID_KEY, mUseUcouponId);
                bundle.putString(MobileConstant.PARAM.SKU_SN, mMobileProduct.getSkuSN());
                intent.putExtras(bundle);
                startActivityForResult(intent, COUPON_LIST_FORRESULT);
            }
        });
    }

    private void setTvNumber(String number) {
        String formatNumber = PhoneNumberUtils.checkPhoneNumber(number, true);
        if (!TextUtils.isEmpty(formatNumber)) {
            mTvNumber.setText(formatNumber);
        }
    }

    private void setDiscountPrice(float price) {
        if (price > 0.0F) {
            mTvDiscount.setText(String.format(getString(R.string.mobile_order_desc_coupon_price), price));
        } else {
            mTvDiscount.setText("-￥0");
        }
    }

    private void setCouponListCount(int count) {
        if (mCouponV == null) return;
        mCouponV.setContent(String.format(getString(R.string.coupon_request_data), count));
    }

    private void updateCouponData(CouponListBean<CouponBean> couponListBean) {
        mCouponListBean = couponListBean;
        CouponBean couponBean = null;
        int couponlistCount = 0;
        mUseUcouponId = 0;
        if (mCouponListBean != null && mCouponListBean.list != null && mCouponListBean.list.length > 0) {
            couponBean = mCouponListBean.list[0];
            if (couponBean != null) {
                mUseUcouponId = couponBean.ucoupon_id;
            }
            couponlistCount = mCouponListBean.list.length;
            if (mUcouponId != 0) {
                for (int i = 0; i < mCouponListBean.list.length; i++) {
                    if (mCouponListBean.list[i].getUcoupon_id() == mUcouponId) {
                        couponBean = mCouponListBean.list[i];
                        mUseUcouponId = mUcouponId;
                    }
                }
            }
        }
        setData(couponBean, couponlistCount);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsGoPay && !AccountHelper.getInstance().isLogin(this)) {
            showNoLoginBlankPage();
            return;
        }
        if (mIsShowDialog && isNetworkAvailable()) {
            showDialog();
            return;
        }
        if ((!mIsGoPay || mIsFirstLoadData)) {
            loadData();
        }
    }

    private void loadData() {
        if (mSelectedCoupon || mMobileProduct == null) return;
        if (isNetworkAvailable()) {
            mIsFirstLoadData = false;
            queryCouponInfo(mMobileProduct.getSkuSN());
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            if (mCouponListBean == null) {
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
        destoryAsyncTask();
        super.onDestroy();
    }

    private void showDialog() {
        if (isFinishing() || mDialog == null) return;
        mDialog.setTitle(R.string.wallet_prompt_title);
        mDialog.setMessage(getString(R.string.wallet_prompt_create_order));
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
        mIsShowDialog = true;
    }

    private void hideDialog() {
        if (isFinishing() || mDialog == null) return;
        mIsShowDialog = false;
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void goToPay(String payInfo) {
        try {
            if (payInfo == null) return;
            Intent intent = new Intent(ActivityConstant.PAY_PARAM.PAY_ACTION);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INFO_PARAM.LEPAY_INFO, payInfo);
            bundle.putString(WalletConstant.EXTRA_FROM, mFrom);
            intent.putExtras(bundle);
            startActivityForResult(intent, PAY_REQUEST_CODE);
            mIsGoPay = true;
        } catch (Exception e) {

        }
    }

    private void queryCouponInfo(String skuSN) {
        setData(null, 0);
        showLoadingView();
        String uToken = AccountHelper.getInstance().getToken(MobileOrderConfirmationActivity.this);
        if (mCouponAsyncT != null) {
            mCouponAsyncT.onCancelled();
        }
        mCouponAsyncT = new CouponListTask(MobileOrderConfirmationActivity.this, this, uToken, skuSN);
        ExecutorHelper.getExecutor().runnableExecutor(mCouponAsyncT);
    }

    @Override
    public void onLoadFinished(CouponListBean<CouponBean> result, int errorCode) {
        hideLoadingView();
        if (result == null) {
            if (!isNetworkAvailable())
                sendMessageRet(EXCEPTION_PROMPT2);
        }
        updateCouponData(result);
        mCouponAsyncT = null;
    }

    private void getPrePayInfoTask() {
        checkPrePayAsyncTask();
        if (mDialog != null) {
            mDialog.setMessage(getString(R.string.wallet_prompt_create_payinfo));
        }
        ExecutorHelper.getExecutor().runnableExecutor(mPrePayTask);
    }


    private boolean checkPrePayAsyncTask() {
        if (mPrePayTask == null) {
            mPrePayTask = new PayPreInfoTask(getBaseContext(), this, this);
            return true;
        } else {
            if (!mPrePayTask.isCancelled()) {
                mPrePayTask.onCancelled();
            }
            mPrePayTask = new PayPreInfoTask(getBaseContext(), this, this);
            return true;
        }
    }

    @Override
    public String getRequestPath() {
        return MobileConstant.PATH.SDKPAY;
    }

    @Override
    public Map<String, String> getRequestParams() {
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put(Constants.INFO_PARAM.ORDER_SN, mOrderSN);
        return reqMap;
    }

    private void getOrderSNAsyncTask(String number, int productId, long[] couponIds) {
        if (!isNetworkAvailable()) {
            sendMessageRet(EXCEPTION_PROMPT2);
            return;
        }
        String uToken = AccountHelper.getInstance().getToken(MobileOrderConfirmationActivity.this);
        if (mGetOrderSNTask == null) {
            mGetOrderSNTask = new GetOrderSNTask();
        }
        mGetOrderSNTask.setData(uToken, number, productId, couponIds);
        ExecutorHelper.getExecutor().runnableExecutor(mGetOrderSNTask);
    }

    private void sendMessageRet(int arg1) {
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = EXCEPTION_RET;
            msg.arg1 = arg1;
            handler.sendMessage(msg);
        }
    }

    @Override
    public void onLoadPayInfoFinished(String result, int errorCode) {
        LogHelper.d("[%S] onLoadFinished result %s", TAG, result);
        int status = Constants.PAY_STATUS.STATUS_OK;
        if (TextUtils.isEmpty(result)) {
            if (isNetworkAvailable()) {
                status = Constants.PAY_STATUS.STATUS_PAYINFO_ERROR;
            } else {
                status = Constants.PAY_STATUS.STATUS_NETWORK_ERROR;
            }
        }
        onPayAsyncExit(status, result);
        mPrePayTask = null;
    }

    private void onPayAsyncExit(int status, String payInfo) {
        Message msg = Message.obtain();
        handler.removeMessages(PAY_INFO_RET);
        msg.what = PAY_INFO_RET;
        msg.arg1 = status;
        msg.obj = payInfo;
        handler.sendMessage(msg);
    }

    private void destoryAsyncTask() {
        if (mPrePayTask != null) {
            mPrePayTask.onCancelled();
            mPrePayTask = null;
        }
        mCouponAsyncT = null;
    }

    private class GetOrderSNTask implements Runnable {
        private String mUtoken;
        private String mNumber;
        private int mProductId = PRODUCT_ID;
        private long[] mCouponIds;

        public void setData(String token, String number, int productId, long[] couponIds) {
            mUtoken = token;
            mNumber = number;
            mProductId = productId;
            mCouponIds = couponIds;
        }

        @Override
        public void run() {
            BaseResponse<OrderBean> response = null;
            try {
                BaseRequestParams reqParams = new BaseRequestParams(MobileConstant.PATH.ORDER);
                reqParams.addBodyParameter(MobileConstant.PARAM.NUMBER, mNumber);
                reqParams.addBodyParameter(MobileConstant.PARAM.PRODUCT_ID, String.valueOf(mProductId));
                reqParams.addBodyParameter(MobileConstant.PARAM.TOKEN, mUtoken);
                String coupons = "";
                if (mCouponIds != null && mCouponIds.length > 0) {
                    int size = mCouponIds.length;
                    for (int i = 0; i < size; i++) {
                        if (i == (size - 1)) {
                            coupons += String.valueOf(mCouponIds[i]);
                        } else {
                            coupons += (String.valueOf(mCouponIds[i]) + ",");
                        }
                    }
                }
                reqParams.addBodyParameter(MobileConstant.PARAM.COUPON_ID, coupons);
                TypeToken typeToken = new TypeToken<BaseResponse<OrderBean>>() {
                };
                response = xmain.http().postSync(reqParams, typeToken.getType());
            } catch (Exception e) {
            } catch (Throwable throwable) {
            }
            if (response != null && response.data != null && response.errno == 10000) {
                mSelectedCoupon = false;
                OrderBean order = response.data;
                Message msg = Message.obtain();
                msg.obj = order;
                msg.what = PLACE_ORDER_RET;
                // 发送这个消息到消息队列中
                handler.sendMessage(msg);
            } else {
                if (!isNetworkAvailable())
                    sendMessageRet(EXCEPTION_PROMPT2);
                sendMessageRet(EXCEPTION_PROMPT1);
            }
        }
    }
}
