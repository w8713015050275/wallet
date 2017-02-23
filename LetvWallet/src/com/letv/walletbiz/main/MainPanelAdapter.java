package com.letv.walletbiz.main;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.main.bean.WalletServiceListBean.WalletServiceBean;

import org.xutils.xmain;

import java.io.IOException;

/**
 * Created by liuliang on 16-4-8.
 */
public class MainPanelAdapter extends RecyclerView.Adapter<MainPanelAdapter.ViewHolder> implements View.OnClickListener {

    public static final int MAX_COUNT = 6;
    public static final int MAX_LINE = 3;
    private Context mContext;
    private WalletServiceBean[] mData;
    private int mSpanCount = 3;
    private boolean hasMore = false;

    public MainPanelAdapter(Context context) {
        mContext = context;
    }

    public void setData(WalletServiceBean[] data) {
        mData = data;
        if (data != null) {
            if (data.length > MAX_COUNT) {
                mSpanCount = 4;
            } else {
                mSpanCount = 3;
            }
            if (mSpanCount == 4 && data.length > 4 * MAX_LINE) {
                hasMore = true;
            } else {
                hasMore = false;
            }
        }
    }

    public WalletServiceBean[] getData() {
        return mData;
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public boolean isMoreItem(int position) {
        if (!hasMore) {
            return false;
        }
        return position == (mSpanCount * MAX_LINE - 1);
    }

    public void updatehasMore(boolean hasMore) {
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        if (isMoreItem(position)) {
            updatehasMore(false);
        } else {
            final WalletServiceBean bean = getItem(position);
            if (bean != null) {
                Action.uploadExposeTab(Action.WALLET_HOME_LIST + bean.service_id);
                if (bean.jump_type == WalletServiceBean.JUMP_TYPE_APP) {
                    Bundle bundle = null;
                    if (mContext != null
                            && !TextUtils.isEmpty(bean.package_name)
                            && mContext.getPackageName().startsWith(bean.package_name)) {
                        bundle = new Bundle();
                        bundle.putString(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_ICON);
                    }
                    AppUtils.LaunchAppWithBundle(mContext, bean.package_name, bean.jump_param, bundle, true);
                } else if (bean.jump_type == WalletServiceBean.JUMP_TYPE_WEB) {
                    if (AccountHelper.getInstance().isLogin(mContext)) {
                        jumpWeb(bean);
                    } else {
                        AccountHelper.getInstance().loginLetvAccountIfNot((Activity) mContext,  new AccountManagerCallback() {

                            @Override
                            public void run(AccountManagerFuture future) {
                                try {
                                    if (mContext != null && future.getResult() != null && AccountHelper.getInstance().isLogin(mContext)) {
                                        jumpWeb(bean);
                                    }
                                } catch (OperationCanceledException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (AuthenticatorException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void jumpWeb(WalletServiceBean bean){
        if (bean == null) {
            return ;
        }
        Intent intent = new Intent(mContext, WalletMainWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, bean.jump_link);
        intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, bean.service_name);
        intent.putExtra(WalletConstant.EXTRA_WEB_WITH_ACCOUNT, bean.need_token == 1);
        mContext.startActivity(intent);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.wallet_main_panel_item, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (hasMore && position == (mSpanCount * MAX_LINE - 1)) {
            holder.iconView.setImageResource(R.drawable.ic_main_more);
            holder.nameView.setText(R.string.label_more);
            holder.isMore = true;
        } else {
            WalletServiceBean serviceBeans = mData[position];
            if (serviceBeans != null) {
                xmain.image().bind(holder.iconView, serviceBeans.icon);
                holder.nameView.setText(serviceBeans.service_name);
                holder.isMore = false;
            }
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        }
        if (hasMore) {
            return mSpanCount * MAX_LINE;
        } else {
            return mData.length;
        }
    }

    public WalletServiceBean getItem(int position) {
        if (mData == null || position < 0 || position >= mData.length) {
            return null;
        }
        return mData[position];
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView iconView;
        public TextView nameView;
        public boolean isMore = false;

        public ViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.service_icon);
            nameView = (TextView) itemView.findViewById(R.id.service_name);
        }
    }
}
