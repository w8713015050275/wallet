package com.letv.walletbiz.base.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.PriorityExecutorHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.util.OrderDetailTask;

import org.xutils.common.task.PriorityExecutor;

/**
 * Created by linquan on 15-11-27.
 */
public abstract class OrderDetailActivity extends BaseWalletFragmentActivity {
    private static final String TAG = OrderDetailActivity.class.getSimpleName();
    private static final int MSG_DEATAIL_LOAD_SUCCEED = 1;

    private ViewGroup mWrapper;
    private View mContentView;
    protected String mOrderNum;
    private OrderBaseBean mOrderBean;

    private PriorityExecutor mExecutor;
    private OrderDetailTask mTask;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DEATAIL_LOAD_SUCCEED:
                    hideLoadingView();
                    int erroCode = msg.arg1;
                    if (msg.obj != null && erroCode == CommonCallback.NO_ERROR) {
                        mOrderBean = (OrderBaseBean) msg.obj;
                        setData(mContentView, mOrderBean);
                    } else if (erroCode == CommonCallback.ERROR_NETWORK) {
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(blankClickLis);
                        clearData();
                    }
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    private CommonCallback mCallback = new CommonCallback() {

        @Override
        public void onLoadFinished(Object result, int errorCode) {
            mTask = null;
            Message message = mHandler.obtainMessage(MSG_DEATAIL_LOAD_SUCCEED);
            message.arg1 = errorCode;
            message.obj = result;
            message.sendToTarget();
        }
    };

    private void loadData() {
        if (isNetworkAvailable()) {
            if (mTask == null) {
                showLoadingView();
                mTask = new OrderDetailTask(this, getRequestBean(), getTypeToken(), mCallback);
                mExecutor.execute(mTask);
            }
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    protected void clearData() {
        mOrderBean = null;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && mOrderBean == null) {
            loadData();
        }
    }

   protected View.OnClickListener blankClickLis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isNetworkAvailable() && mOrderBean == null) {
                loadData();
            }
        }
    };


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.order_detail_main);
        initV();
        if (!TextUtils.isEmpty(mOrderNum)) {
            mContentView = LayoutInflater.from(this).inflate(layoutResID, mWrapper, false);
            mWrapper.addView(mContentView,
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            loadData();
        } else {
            finish();
        }
    }

    private void initV() {
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri == null) {
                mOrderNum = intent.getStringExtra(Constants.INFO_PARAM.ORDER_NO);
            } else {
                String encodedQuery = uri.getEncodedQuery();
                if (!TextUtils.isEmpty(encodedQuery) && encodedQuery.contains(Constants.INFO_PARAM.ORDER_NO)) {
                    mOrderNum = intent.getData().getQueryParameter(Constants.INFO_PARAM.ORDER_NO);
                }
            }
        }
        mWrapper = (ViewGroup) findViewById(R.id.id_content);
        mExecutor = PriorityExecutorHelper.getPriorityExecutor();
    }

    public abstract void setData(View v, OrderBaseBean bean);

    public abstract TypeToken getTypeToken();

    public abstract BaseRequestParams getRequestBean();
}

