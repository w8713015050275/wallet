package com.letv.walletbiz.mobile.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.letv.wallet.common.util.ViewUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.fragment.BaseOrderListFragment;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderRequestBean;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.view.OrderListViewAdapter;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.activity.MobileOrderDetailActivity;
import com.letv.walletbiz.mobile.beans.OrderBean;
import com.letv.walletbiz.mobile.pay.MobileProduct;
import com.letv.walletbiz.mobile.util.PayInfoCommonCallback;
import com.letv.walletbiz.mobile.util.PayPreInfoTask;
import com.letv.walletbiz.mobile.util.RecordPhoneNumberTask;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 15-12-29.
 */
public class MobileOrderListFragment extends BaseOrderListFragment implements PayPreInfoTask.ProvidePayPreParams
        , PayInfoCommonCallback<String> {

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
    int mFeeOrFlow;
    private Toast mToast;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PAY_INFO_RET:
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
                    hideDialog();
                    break;
                case EXCEPTION_RET:
                    hideDialog();
                    switch (msg.arg1) {
                        case EXCEPTION_PROMPT1:
                            promptNoNetWork();
                            break;
                        case EXCEPTION_PROMPT2:
                            Toast.makeText(getContext(), R.string.pay_info_response_error, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent in = getActivity().getIntent();
        String strType = in.getStringExtra(ActivityConstant.MOBILE_PARAM.TYPE);
        if (ActivityConstant.MOBILE_PARAM.FEE.equals(strType)) {
            mFeeOrFlow = MobileConstant.PRODUCT_TYPE.MOBILE_FEE;
        } else {
            mFeeOrFlow = MobileConstant.PRODUCT_TYPE.MOBILE_FLOW;
        }
        mToast = Toast.makeText(getContext(), getString(R.string.mobile_prompt_net_connection_fail), Toast.LENGTH_SHORT);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
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
                            Toast.makeText(getContext(), R.string.pay_unable_to_pay_prompt, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (mMobileProduct != null) {
                        mMobileProduct.showPayResult(getContext(), payResult);
                    }
                    if (payResult == Constants.RESULT_STATUS.SUCCESS) {
                        resetRefresh();
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
    protected OrderRequestBean getRequestBean() {
        String uToken = AccountHelper.getInstance().getToken(getContext());
        OrderRequestBean requestBean = new OrderRequestBean(MobileConstant.PATH.ORDER_QUERY);
        requestBean.addQueryStringParameter(MobileConstant.PARAM.TYPE, String.valueOf(mFeeOrFlow));
        requestBean.addQueryStringParameter(MobileConstant.PARAM.TOKEN, uToken);
        return requestBean;
    }

    @Override
    protected Type getResponseType() {
        TypeToken typeToken = new TypeToken<BaseResponse<OrderListBaseBean<OrderBean>>>() {
        };
        return typeToken.getType();
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

    @Override
    public OrderListViewAdapter.BaseOrderViewHolder getViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.mobile_orderlist_item_v, parent, false);
        return new MobileOrderListViewHolder(v);
    }

    @Override
    public OrderListViewAdapter.BaseHeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    protected RecyclerItemClickListener getRecycleritemClickListener() {
        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == RecyclerView.NO_POSITION)
                    return;
                if (!NetworkHelper.isNetworkAvailable()) {
                    mToast.show();
                    return;
                }
                OrderListViewAdapter adapater = getOrderListAdapter();
                if (adapater == null) {
                    return;
                }
                OrderBean orderBean = (OrderBean) adapater.getOrderItem(position);
                if (orderBean == null) {
                    return;
                }
                goOrderDetail(orderBean);
            }
        });
        return itemClickListener;
    }

    private void goOrderDetail(OrderBean orderBean) {
        Intent intent = new Intent(MobileOrderListFragment.this.getContext(), MobileOrderDetailActivity.class);
        intent.putExtra(Constants.INFO_PARAM.ORDER_NO, orderBean.getOrderId());
        startActivity(intent);
    }

    private void getPrePayInfoTask() {
        checkPrePayAsyncTask();
        showDialog();
        ExecutorHelper.getExecutor().runnableExecutor(mPrePayTask);
    }


    private boolean checkPrePayAsyncTask() {
        if (mPrePayTask == null) {
            mPrePayTask = new PayPreInfoTask(getContext(), this, this);
            return true;
        } else {
            if (mPrePayTask.isCancelled()) {
                mPrePayTask.onCancelled();
            }
            mPrePayTask = new PayPreInfoTask(getContext(), this, this);
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
            mDialog = new ProgressDialog(getContext());
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

    public class MobileOrderListViewHolder extends OrderListViewAdapter.BaseOrderViewHolder implements View.OnClickListener {

        private OrderBean mOrderBean;
        private LinearLayout mIdsDescV;
        private TextView mVPay;
        private TextView mNumberTv;
        private TextView mDescLine1Tv;
        private TextView mStatusTv;
        private TextView mSNTv;
        private TextView mPriceTv;
        private TextView mTimeTv;

        public MobileOrderListViewHolder(View v) {
            super(v);
            initV();
        }

        private void initV() {
            mIdsDescV = (LinearLayout) itemView.findViewById(R.id.ids_desc_v);
            mNumberTv = (TextView) itemView.findViewById(R.id.tv_number);
            mDescLine1Tv = (TextView) itemView.findViewById(R.id.tv_order_prodcut);
            mStatusTv = (TextView) itemView.findViewById(R.id.tv_order_status);
            mSNTv = (TextView) itemView.findViewById(R.id.tv_order_sn);
            mPriceTv = (TextView) itemView.findViewById(R.id.tv_price);
            mTimeTv = (TextView) itemView.findViewById(R.id.tv_time);
            mVPay = (TextView) itemView.findViewById(R.id.ibtn_order_action);
        }

        @Override
        protected void setData(OrderBaseBean orderBaseBean, int position) {
            this.mOrderBean = (OrderBean) orderBaseBean;
            if (this.mOrderBean.price == 0.0F || TextUtils.isEmpty(this.mOrderBean.product_name)) {
                LogHelper.d("[MobileOrderList]  data == " + this.mOrderBean.toString());
            }
            mNumberTv.setText(mOrderBean.number);
            mDescLine1Tv.setText(mOrderBean.product_name);
            mStatusTv.setText(mOrderBean.getOrderStatus(getContext()));
            mSNTv.setText(mOrderBean.getOrderSN());
            mPriceTv.setText(mOrderBean.getPrice());
            mTimeTv.setText(DateUtils.getDayStr(mOrderBean.getOrderCTime()));
            if (mOrderBean.getOrderTodo() == MobileConstant.ORDER_TODO.TOPAY) {
                mVPay.setOnClickListener(this);
                ViewUtils.expandViewTouchDelegate(mVPay, 30, 30, 0, 0);
                mVPay.setVisibility(View.VISIBLE);
                mVPay.setEnabled(true);
            } else {
                mVPay.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (!NetworkHelper.isNetworkAvailable()) {
                mToast.show();
                return;
            }
            if (mIdsDescV.isEnabled() && v.getId() == R.id.ibtn_order_action) {
                //Todo multiple tap
                mMobileProduct = new MobileProduct(R.string.label_mobile_deposite, mOrderBean);
                getPrePayInfoTask();
            }
        }
    }

}
