package com.letv.walletbiz.mobile.ui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.dbhelper.HistoryRecordHelper;
import com.letv.walletbiz.mobile.util.UiUtils;

/**
 * Created by changjiajie on 16-1-13.
 */
public class HistoryRecordNumberV extends RecyclerView {

    private final String TAG = HistoryRecordNumberV.class.getSimpleName();

    private Context mContext;
    private HistoryRecordNumberAdapter mAdapter;
    private HistoryRecordNumberBean mAllRecord;

    public HistoryRecordNumberV(Context context) {
        super(context, null);
    }

    public HistoryRecordNumberV(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initV();
    }

    public void initV() {
        setBackgroundColor(getContext().getResources().getColor(R.color.content_item_bg_color));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        setLayoutManager(layoutManager);
        DividerItemDecoration dividerLine = new DividerItemDecoration(getContext(), getResources().getColor(R.color.colorDividerLineBg),
                DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
        dividerLine.setTopAndBottomLine(true, getResources().getDimensionPixelSize(R.dimen.divider_width));
        addItemDecoration(dividerLine);
        setAdapter(mAdapter = new HistoryRecordNumberAdapter());
    }

    public void setHistoryNumberClickListener(HistoryRecordNumberAdapter.EnventCallback mRecordClickListener) {
        if (mAdapter == null) mAdapter = (HistoryRecordNumberAdapter) getAdapter();
        mAdapter.setmRecordClickListener(mRecordClickListener);
    }

    public void loadRecordNumber() {
        loadData(true);
    }

    /**
     * 获取缓存数据
     *
     * @return
     */
    private void loadData(final boolean isFirst) {
        AsyncTask<String, Integer, HistoryRecordNumberBean> task = new AsyncTask<String, Integer, HistoryRecordNumberBean>() {
            @Override
            protected void onPostExecute(HistoryRecordNumberBean historyRecordNumberBean) {
                HistoryRecordNumberAdapter.EnventCallback callback = null;
                if (mAdapter != null) {
                    callback = mAdapter.getEvnentCallback();
                    if (callback == null)
                        return;
                }
                if (historyRecordNumberBean == null) {
                    callback.hideV();
                    if (isFirst) {
                        callback.setRecordNumber(null);
                    }
                    return;
                }
                String simCard0Model = DeviceUtils.getPhoneNumber0(mContext);
                String simCard1Model = DeviceUtils.getPhoneNumber1(mContext);
                for (int i = historyRecordNumberBean.record_info.size() - 1; i >= 0; i--) {
                    String phoneNumber = historyRecordNumberBean.record_info.get(i).getPhoneNum();
                    if (phoneNumber != null && (phoneNumber.equals(simCard0Model) || phoneNumber.equals(simCard1Model))) {
                        historyRecordNumberBean.record_info.get(i).setName(
                                mContext.getResources().getString(R.string.mobile_divider_phone_number_show));
                    } else {
                        String contactName = null;
                        try {
                            if (PermissionCheckHelper.checkContactsPermission((Activity) mContext, -1) == PermissionCheckHelper.PERMISSION_ALLOWED) {
                                contactName = UiUtils.getContactNameByNumber(mContext, phoneNumber);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (contactName == null || contactName.equals("")) {
                                contactName = mContext.getResources().getString(R.string.mobile_phone_number_unknown);
                            } else if (contactName.equals(phoneNumber)) {
                                contactName = mContext.getResources().getString(R.string.mobile_phone_number_no_name);
                            }
                            historyRecordNumberBean.record_info.get(i).setName(contactName);
                        }
                    }
                }
                mAllRecord = historyRecordNumberBean;
                if (isFirst) {
                    callback.setRecordNumber(mAllRecord);
                } else {
                    mAdapter.setmRecordBean(mAllRecord);
                    mAdapter.notifyDataSetChanged();
                    callback.showV();
                }
                super.onPostExecute(historyRecordNumberBean);
            }

            @Override
            protected HistoryRecordNumberBean doInBackground(String... params) {
                return HistoryRecordHelper.getContactFromDBsync(mContext);
            }
        }.execute("");
    }

    public HistoryRecordNumberAdapter getAdapter() {
        return mAdapter;
    }

    public void show() {
        if (mAdapter == null) mAdapter = (HistoryRecordNumberAdapter) getAdapter();
        loadData(false);
    }

    public void updateData(HistoryRecordNumberBean historyRecordNumberBean) {
        mAdapter.setmRecordBean(historyRecordNumberBean);
        mAdapter.notifyDataSetChanged();
    }

    public void clearData() {
        mAllRecord = null;
    }

    public HistoryRecordNumberBean getData() {
        if (mAllRecord == null) {
            loadData(false);
        }
        return mAllRecord;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
