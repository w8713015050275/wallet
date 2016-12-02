package com.letv.walletbiz.mobile.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.dbhelper.HistoryRecordHelper;

/**
 * Created by changjiajie on 16-1-13.
 */
public class HistoryRecordNumberAdapter extends RecyclerView.Adapter<HistoryRecordNumberAdapter.ViewHolder> {

    private HistoryRecordNumberBean mRecordBean;

    public EnventCallback mCallback;

    protected static final int clearVType = 1;

    private static int recordlis_size = 0;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v;
        switch (viewType) {
            case clearVType:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.mobile_history_clear_v, parent, false);
                break;
            default: {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.mobile_history_phonenum_item, parent, false);
            }
        }
//        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        recordlis_size = mRecordBean.getRecordInfo().size();
        if (position < recordlis_size) {
            if (recordlis_size > 0) {
                HistoryRecordNumberBean.RecordInfoBean record_info
                        = mRecordBean.getRecordInfo().get(position);
                if (record_info != null) {
                    holder.setData(record_info);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mRecordBean.getRecordInfo().size()) {
            return clearVType;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public int getItemCount() {
        return mRecordBean == null ? 0 : mRecordBean.getRecordInfo().size() + 1;
    }

    public HistoryRecordNumberBean.RecordInfoBean getOrderItem(int position) {
        if (position < mRecordBean.getRecordInfo().size()) {
            return mRecordBean.getRecordInfo().size() == 0 ? null : (HistoryRecordNumberBean.RecordInfoBean) mRecordBean.getRecordInfo().get(position);
        } else {
            return null;
        }
    }

    public EnventCallback getEvnentCallback() {
        return mCallback;
    }

    public void setmRecordClickListener(EnventCallback callback) {
        this.mCallback = callback;
    }

    public void setmRecordBean(HistoryRecordNumberBean recordBean) {
        this.mRecordBean = recordBean;
    }

    public interface EnventCallback {

        /*
        status is -1 Delete the failure
         */
        void onHistoryNumberStatus(int state);

        void onHistoryRecordNumberItemClick(View view);

        void hideV();

        void showV();

        void setRecordNumber(HistoryRecordNumberBean numberBean);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View mView;
        public TextView history_phone;
        public TextView history_phone_name;

        public ViewHolder(View v, int viewType) {
            super(v);
            mView = v;
            initV(viewType);
        }

        private void initV(int viewType) {
            setOnClickListener();
            if (viewType != clearVType) {
                history_phone = (TextView) mView.findViewById(R.id.history_phone);
                history_phone_name = (TextView) mView.findViewById(R.id.history_phone_name);
            }
        }

        private void setOnClickListener() {
            mView.setOnClickListener(this);
        }

        public void setData(HistoryRecordNumberBean.RecordInfoBean record_info) {
            String number = PhoneNumberUtils.checkPhoneNumber(record_info.getPhoneNum(), true);
            history_phone.setText(number);
            history_phone_name.setText(record_info.getName());
            mView.setTag(record_info);
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                switch (v.getId()) {
                    case R.id.mobile_history_clear_tv:
                        new Thread(new ClearHistoryRecordNumberThread()).start();
                        break;
                    case R.id.mobile_history_record_itemv:
                        mCallback.onHistoryRecordNumberItemClick(v);
                        break;
                }
            }
        }
    }

    public class ClearHistoryRecordNumberThread implements Runnable {

        @Override
        public void run() {
            int returnState = HistoryRecordHelper.deleteContactToDBsync(WalletApplication.getApplication());
            mCallback.onHistoryNumberStatus(returnState);
        }
    }
}
