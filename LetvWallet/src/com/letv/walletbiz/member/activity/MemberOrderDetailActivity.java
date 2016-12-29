package com.letv.walletbiz.member.activity;

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
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.PriorityExecutorHelper;
import com.letv.wallet.common.widget.LabeledTextView;
import com.letv.walletbiz.BuildConfig;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.OrderDetailActivity;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.OrderInfoBean;
import com.letv.walletbiz.member.beans.OrderPayBean;
import com.letv.walletbiz.member.pay.MemberProduct;
import com.letv.walletbiz.member.task.OrderCreateTask;
import com.letv.walletbiz.member.task.OrderPrePayTask;
import com.letv.walletbiz.member.util.MemberCommonCallback;
import com.letv.walletbiz.member.util.MemberUtils;
import com.letv.walletbiz.member.widget.MemberProductBrief;
import com.letv.walletbiz.mobile.util.UiUtils;
import com.letv.walletbiz.mobile.widget.MobileCostLabeledTextView;
import com.letv.walletbiz.mobile.widget.MobileProductCostBrief;

import org.xutils.common.task.PriorityExecutor;

import java.util.Map;

/**
 * Created by zhanghuancheng on 16-11-25.
 */
public class MemberOrderDetailActivity extends OrderDetailActivity {
    private static final String TAG = MemberOrderDetailActivity.class.getName();

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

    private int mTitleId;
    private PriorityExecutor mExecutor;
    private ProgressDialog mDialog;
    private MemberProduct mMemberProduct;
    private OrderInfoBean mOrderInfoBean;
    private OrderPrePayTask mPrePayTask;
    private OrderCreateTask mOrderCreateTask;
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
                                if (mMemberProduct != null) {
                                    mMemberProduct.setPrepayInfo(info);
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
                    if (mOrderCreateTask != null) {
                        mOrderCreateTask = null;
                    }
                    switch (msg.arg1) {
                        case EXCEPTION_PROMPT1:
                            Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT).show();
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

    private void sendMessageRet(int arg1) {
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = EXCEPTION_RET;
            msg.arg1 = arg1;
            handler.sendMessage(msg);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PAY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    int ret = data.getExtras().getInt(Constants.INFO_PARAM.LEPAY_RETURN_RESULT);
                    int payResult = Constants.RESULT_STATUS.FAIL;
                    switch (ret) {
                        case SUCCESSED://支付成功
                            payResult = Constants.RESULT_STATUS.SUCCESS;
                            if (mMemberProduct != null) {

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
                    if (mMemberProduct != null) {
                        mMemberProduct.showPayResult(getBaseContext(), payResult);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_order_detail);
        setTitle("");
        mExecutor = PriorityExecutorHelper.getPriorityExecutor();
    }

    @Override
    public void setData(View v, OrderBaseBean bean) {
        mOrderInfoBean = (OrderInfoBean) bean;
        switch (Integer.valueOf(mOrderInfoBean.getOrderStatus())) {
            case MemberConstant.ORDER_STATUS.CREATED:
                mTitleId = R.string.mobile_order_view_surelabel;
                break;
            default:
                mTitleId = R.string.mobile_order_view_label;
                break;
        }
        setTitle(mTitleId);

        MobileProductCostBrief costBrief = null;
        MemberProductBrief vProduct = (MemberProductBrief) v.findViewById(R.id.v_prodcut);
        vProduct.setAllLines(mOrderInfoBean.getSnapshot().getName(), mOrderInfoBean.getOrderId(),
                mOrderInfoBean.getSnapshot().getDuration() + getResources().getString(R.string.member_month_count),
                mOrderInfoBean.getSnapshot().getDescription());

        LinearLayout llPriceTop = (LinearLayout) v.findViewById(R.id.ll_price_top);
        LinearLayout llPriceBottom = (LinearLayout) v.findViewById(R.id.ll_price_bottom);
        LinearLayout llOrderPayInfo = (LinearLayout) v.findViewById(R.id.ll_order_pay_info);
        LabeledTextView ltvStatus = (LabeledTextView) v.findViewById(R.id.ltv_status);
        LabeledTextView ltvTime = (LabeledTextView) v.findViewById(R.id.ltv_time);
        TextView btnAction = (TextView) v.findViewById(R.id.ibtn_action);
        String statusStr;
        switch (Integer.valueOf(mOrderInfoBean.getOrderStatus())) {
            case MemberConstant.ORDER_STATUS.CREATED: //待支付页
                costBrief = (MobileProductCostBrief) v.findViewById(R.id.v_bottom_price_brief);
                btnAction.setText(R.string.pay_now);
                btnAction.setEnabled(true);
                btnAction.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!NetworkHelper.isNetworkAvailable()) {
                            Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mMemberProduct = new MemberProduct(mTitleId, mOrderInfoBean);
                        runPrePayTask();
                    }
                });
                vProduct.setBackgroundResource(R.color.mobile_order_desc_prodct_bg_color);
                llPriceTop.setVisibility(View.GONE);
                llPriceBottom.setVisibility(View.VISIBLE);
                llOrderPayInfo.setVisibility(View.GONE);
                btnAction.setVisibility(View.VISIBLE);
                break;

            case MemberConstant.ORDER_STATUS.PAID:
            case MemberConstant.ORDER_STATUS.COMPLISHED: //订单详情页
                costBrief = (MobileProductCostBrief) v.findViewById(R.id.v_top_price_brief);

                statusStr = MemberUtils.getOrderStatusStringByStatus(this, Integer.valueOf(mOrderInfoBean.getOrderStatus()));
                ltvStatus.setTextSummery(statusStr);
                ltvTime.setTextSummery(DateUtils.getTimeStr(mOrderInfoBean.getOrderCTime() * 1000));

                llPriceTop.setVisibility(View.VISIBLE);
                llPriceBottom.setVisibility(View.GONE);
                llOrderPayInfo.setVisibility(View.VISIBLE);
                btnAction.setVisibility(View.GONE);
                break;
            case MemberConstant.ORDER_STATUS.CANCELLED: //已关闭的订单
                costBrief = (MobileProductCostBrief) v.findViewById(R.id.v_top_price_brief);

                statusStr = MemberUtils.getOrderStatusStringByStatus(this, Integer.valueOf(mOrderInfoBean.getOrderStatus()));
                ltvStatus.setTextSummery(statusStr);

                llPriceTop.setVisibility(View.VISIBLE);
                llPriceBottom.setVisibility(View.GONE);
                llOrderPayInfo.setVisibility(View.VISIBLE);
                ltvTime.setVisibility(View.GONE);
                btnAction.setVisibility(View.GONE);
                break;
        }
        if (costBrief != null) {
            costBrief.setPriceBottomLineShow(MobileProductCostBrief.TAG_ALL, true);
            costBrief.setTotalPrice(String.valueOf(mOrderInfoBean.getPrice()));
            costBrief.setCouponPrice(MemberOrderDetailActivity.this, Float.valueOf(mOrderInfoBean.getDiscount_price()));
            costBrief.setPayPrice(String.valueOf(mOrderInfoBean.getReal_price()), MobileCostLabeledTextView.BLUECOLOR);
        }

    }

    private void runPrePayTask() {
        if (mDialog != null) {
            showDialog();
            mDialog.setMessage(getString(R.string.wallet_prompt_create_payinfo)); //正在获取支付信息
        }
        if (mPrePayTask == null) {
            String uToken = AccountHelper.getInstance().getToken(MemberOrderDetailActivity.this);
            boolean dbg = false;
            if ("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE)) {
                dbg = true;
            }
            mPrePayTask = new OrderPrePayTask(uToken, MemberConstant.PARAM.CLIENT_TYPE, mMemberProduct.getSN(), MemberConstant.PARAM.PLATFORM, dbg, new MemberCommonCallback<OrderPayBean>() {
                @Override
                public void onLoadFinished(OrderPayBean result, int errorCode, boolean needUpdate) {
                    int status = Constants.PAY_STATUS.STATUS_OK;
                    if (TextUtils.isEmpty(result.pay_info)) {
                        if (isNetworkAvailable()) {
                            status = Constants.PAY_STATUS.STATUS_PAYINFO_ERROR;
                        } else {
                            status = Constants.PAY_STATUS.STATUS_NETWORK_ERROR;
                        }
                    }
                    onPayAsyncExit(status, result.pay_info);
                    mPrePayTask = null;
                }
            });
            mExecutor.execute(mPrePayTask);
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

    @Override
    public TypeToken getTypeToken() {
        return new TypeToken<BaseResponse<OrderInfoBean>>() {
        };
    }

    @Override
    public BaseRequestParams getRequestBean() {
        String uToken = AccountHelper.getInstance().getToken(MemberOrderDetailActivity.this.getBaseContext());
        BaseRequestParams params = new BaseRequestParams(MemberConstant.MEMBER_GET_ORDER_INFO);
        params.addQueryStringParameter(MemberConstant.MEMBER_TOKEN, uToken);
        params.addQueryStringParameter(MemberConstant.MEMBER_ORDER_SN, mOrderNum);
        return params;
    }

    private void showDialog() {
        if (mDialog == null) {
            mDialog = new ProgressDialog(MemberOrderDetailActivity.this);
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
