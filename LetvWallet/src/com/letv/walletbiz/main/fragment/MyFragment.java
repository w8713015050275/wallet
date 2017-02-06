package com.letv.walletbiz.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;

/**
 * Created by zhuchuntao on 16-12-21.
 */
public class MyFragment extends MainFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent in = getActivity().getIntent();
        String strType = in.getStringExtra(ActivityConstant.MOBILE_PARAM.TYPE);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_my,null);
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
