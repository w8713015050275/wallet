package com.letv.walletbiz.mobile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.widget.LabeledTextView;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.OrderDetailActivity;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.OrderDetailBean;
import com.letv.walletbiz.mobile.beans.OrderSnapshot;
import com.letv.walletbiz.mobile.pay.MobileProduct;
import com.letv.walletbiz.mobile.util.PayInfoCommonCallback;
import com.letv.walletbiz.mobile.util.PayPreInfoTask;
import com.letv.walletbiz.mobile.util.RecordPhoneNumberTask;
import com.letv.walletbiz.mobile.util.UiUtils;
import com.letv.walletbiz.mobile.widget.MobileCostLabeledTextView;
import com.letv.walletbiz.mobile.widget.MobileProductBrief;
import com.letv.walletbiz.mobile.widget.MobileProductCostBrief;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by changjiajie on 16-4-6.
 */
public class MobileOrderDetailActivity extends OrderDetailActivity implements PayPreInfoTask.ProvidePayPreParams
        , PayInfoCommonCallback<String> {

    private static final String TAG = MobileOrderDetailActivity.class.getSimpleName();

    private static final int PAY_INFO_RET = 100;
    private static final int EXCEPTION_RET = 101;

    private static final int EXCEPTION_PROMPT1 = 200;
    private static final int EXCEPTION_PROMPT2 = 201;

    public static final int PAY_REQUEST_CODE = 1000;

    public static final int SUCCESSED = 1;//支付成功
    public static final int PAYINFO_ERROR = 2;//支付信息错误
    public static final int PAY_CANCLE = 3;//取消支付
    public static final int PAY_FAILED = 4;//支付失败
    public static final int PAY_USEBYOTHER = 5;//支付正在被使用，请提示用户完成上一 次支付

    private PayPreInfoTask mPrePayTask;

    private MobileProduct mMobileProduct;

    private ProgressDialog mDialog;

    private Toast mToast;
    private OrderDetailBean mOrderDetailBean;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                                sendMessageRet(EXCEPTION_PROMPT2);
                            }
                            break;
                        case Constants.PAY_STATUS.STATUS_NETWORK_ERROR:
                            sendMessageRet(EXCEPTION_PROMPT1);
                            break;
                        case Constants.PAY_STATUS.STATUS_PAYINFO_ERROR:
                            sendMessageRet(EXCEPTION_PROMPT2);
                            break;
                    }
                    break;
                case EXCEPTION_RET:
                    hideDialog();
                    switch (msg.arg1) {
                        case EXCEPTION_PROMPT1:
                            mToast.show();
                            break;
                        case EXCEPTION_PROMPT2:
                            Toast.makeText(getBaseContext(), R.string.pay_info_response_error, Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.mobile_order_detail);
        setTitle("");
        mToast = Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT);
    }

    @Override
    public void setData(View v, OrderBaseBean bean) {
        mOrderDetailBean = (OrderDetailBean) bean;
        int mTitleId;
        switch (mOrderDetailBean.getStatusValue()) {
            case MobileConstant.ORDER_STATUS.PAY_ONGOING:
            case MobileConstant.ORDER_STATUS.DEPOSITING:
            case MobileConstant.ORDER_STATUS.CREATED: //待支付页
                mTitleId = R.string.mobile_order_view_surelabel;
                break;
            default:
                mTitleId = R.string.mobile_order_view_label;
                break;
        }
        setTitle(mTitleId);
        OrderSnapshot mSnapshot = mOrderDetailBean.getSnapshot();
        if (mSnapshot == null) {
            LogHelper.e("[%S] orderinfo : %s", TAG, mOrderDetailBean.toString());
            showBlankPage(BlankPage.STATE_DATA_EXCEPTION).getPrimaryBtn().setOnClickListener(blankClickLis);
            clearData();
            return;
        }
        MobileProductCostBrief costBrief = null;

        MobileProductBrief vProduct = (MobileProductBrief) v.findViewById(R.id.v_prodcut);
        vProduct.setAllLines(mSnapshot.getGoods_title(), mOrderDetailBean.getOrderId(), mOrderDetailBean.number);

        LinearLayout llPriceTop = (LinearLayout) v.findViewById(R.id.ll_price_top);
        LinearLayout llPriceBottom = (LinearLayout) v.findViewById(R.id.ll_price_bottom);
        LinearLayout llOrderPayInfo = (LinearLayout) v.findViewById(R.id.ll_order_pay_info);
        LabeledTextView ltvStatus = (LabeledTextView) v.findViewById(R.id.ltv_status);
        LabeledTextView ltvTime = (LabeledTextView) v.findViewById(R.id.ltv_time);
        TextView btnAction = (TextView) v.findViewById(R.id.ibtn_action);
        String statusStr;
        switch (mOrderDetailBean.getStatusValue()) {
            case MobileConstant.ORDER_STATUS.PAY_ONGOING:
            case MobileConstant.ORDER_STATUS.CREATED: //待支付页
                costBrief = (MobileProductCostBrief) v.findViewById(R.id.v_bottom_price_brief);
                btnAction.setText(R.string.pay_now);
                btnAction.setEnabled(true);
                btnAction.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!NetworkHelper.isNetworkAvailable()) {
                            mToast.show();
                            return;
                        }
                        mMobileProduct = new MobileProduct(mOrderDetailBean);
                        getPrePayInfoTask();
                    }
                });
                llPriceTop.setVisibility(View.GONE);
                llPriceBottom.setVisibility(View.VISIBLE);
                llOrderPayInfo.setVisibility(View.GONE);
                btnAction.setVisibility(View.VISIBLE);
                break;
            case MobileConstant.ORDER_STATUS.PAID:
            case MobileConstant.ORDER_STATUS.DEPOSITING:
            case MobileConstant.ORDER_STATUS.COMPLISHED: //订单详情页
                costBrief = (MobileProductCostBrief) v.findViewById(R.id.v_top_price_brief);

                statusStr = UiUtils.getOrderStatusStringbyValue(getBaseContext(), mOrderDetailBean.getStatusValue());
                if (mOrderDetailBean.showRefundProgress()) {
                    ltvStatus.setTextSummery(mOrderDetailBean.getRefundProgressStr(getBaseContext()), statusStr);
                } else {
                    ltvStatus.setTextSummery(statusStr);
                }
                ltvTime.setTextSummery(DateUtils.getTimeStr(mOrderDetailBean.getOrderCTime()));

                llPriceTop.setVisibility(View.VISIBLE);
                llPriceBottom.setVisibility(View.GONE);
                llOrderPayInfo.setVisibility(View.VISIBLE);
                btnAction.setVisibility(View.GONE);
                break;
            case MobileConstant.ORDER_STATUS.CANCELLED: //已关闭的订单
                costBrief = (MobileProductCostBrief) v.findViewById(R.id.v_top_price_brief);

                statusStr = UiUtils.getOrderStatusStringbyValue(getBaseContext(), mOrderDetailBean.getStatusValue());
                if (mOrderDetailBean.showRefundProgress()) {
                    ltvStatus.setTextSummery(mOrderDetailBean.getRefundProgressStr(getBaseContext()), statusStr);
                } else {
                    ltvStatus.setTextSummery(statusStr);
                }

                llPriceTop.setVisibility(View.VISIBLE);
                llPriceBottom.setVisibility(View.GONE);
                llOrderPayInfo.setVisibility(View.VISIBLE);
                ltvTime.setVisibility(View.GONE);
                btnAction.setVisibility(View.GONE);
                break;
        }
        if (costBrief != null) {
            costBrief.setPriceBottomLineShow(MobileProductCostBrief.TAG_ALL, true);
            costBrief.setTotalPrice(String.valueOf(mOrderDetailBean.getTotalPrice()));
            costBrief.setCouponPrice(mOrderDetailBean.getDiscountPrice());
            costBrief.setPayPrice(String.valueOf(mOrderDetailBean.getRealPrice()), MobileCostLabeledTextView.BLUECOLOR);
        }
    }

    @Override
    public BaseRequestParams getRequestBean() {
        String uToken = AccountHelper.getInstance().getToken(MobileOrderDetailActivity.this.getBaseContext());
        BaseRequestParams requestBean = new BaseRequestParams(MobileConstant.PATH.ORDER_DETAIL_QUERY);
        requestBean.addParameter(MobileConstant.PARAM.TOKEN, uToken);
        requestBean.addParameter(MobileConstant.PARAM.ORDER_SN, mOrderNum);
        return requestBean;
    }

    @Override
    public TypeToken getTypeToken() {
        return new TypeToken<BaseResponse<OrderDetailBean>>() {
        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PAY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
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
                        case PAY_USEBYOTHER://支付正在被使用，请提示用户完成上一 次支付
                            payResult = Constants.RESULT_STATUS.PENDING;
                            Toast.makeText(getBaseContext(), R.string.pay_unable_to_pay_prompt, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (mMobileProduct != null) {
                        mMobileProduct.showPayResult(getBaseContext(), payResult);
                    }
                    if (payResult == Constants.RESULT_STATUS.SUCCESS) {
                        finish();
                    }
                }
                break;
        }
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

    @Override
    public String getRequestPath() {
        return MobileConstant.PATH.SDKPAY;
    }

    @Override
    public Map<String, String> getRequestParams() {
        Map<String, String> reqMap = new HashMap<>();
        if (mMobileProduct != null) {
            reqMap.put(Constants.INFO_PARAM.ORDER_SN, mMobileProduct.getSN());
        }
        return reqMap;
    }

    private void getPrePayInfoTask() {
        checkPrePayAsyncTask();
        showDialog();
        ExecutorHelper.getExecutor().runnableExecutor(mPrePayTask);
    }

    private boolean checkPrePayAsyncTask() {
        if (mPrePayTask == null) {
            mPrePayTask = new PayPreInfoTask(getBaseContext(), this, this);
            return true;
        } else {
            if (mPrePayTask.isCancelled()) {
                mPrePayTask.onCancelled();
            }
            mPrePayTask = new PayPreInfoTask(getBaseContext(), this, this);
            return true;
        }
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

    private void showDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(MobileOrderDetailActivity.this);
            mDialog.setTitle(R.string.wallet_prompt_title);
            mDialog.setMessage(getString(R.string.wallet_prompt_create_payinfo));
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setCancelable(false);
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
