package com.letv.walletbiz.main.fragment;

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
import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.PriorityExecutorHelper;
import com.letv.wallet.common.util.ViewUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.fragment.BaseOrderListFragment;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderRequestBean;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.base.view.OrderListViewAdapter;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.activity.MobileOrderDetailActivity;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.beans.OrderBean;
import com.letv.walletbiz.mobile.dbhelper.HistoryRecordHelper;
import com.letv.walletbiz.mobile.pay.MobileProduct;
import com.letv.walletbiz.mobile.util.PayInfoCommonCallback;
import com.letv.walletbiz.mobile.util.PayPreInfoTask;

import org.xutils.common.task.PriorityExecutor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by zhuchuntao on 16-12-21.
 */
public class RecommendFragment extends MainFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent in = getActivity().getIntent();
        String strType = in.getStringExtra(ActivityConstant.MOBILE_PARAM.TYPE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_recommend,null);
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void startLoadData() {

    }

    @Override
    public void onNetWorkChanged(boolean isNetworkAvailable) {

    }
}
