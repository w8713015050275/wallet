package com.letv.wallet.online.ui;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.R;
import com.letv.wallet.online.LePayConstants;
import com.letv.wallet.online.bean.LePayChannelBean;

import org.xutils.xmain;

import java.util.List;

/**
 * Created by changjiajie on 17-1-10.
 */

public class LePayChannelListAdapter extends RecyclerView.Adapter<LePayChannelListAdapter.ViewHolder> {

    private List<LePayChannelBean> mLePayChannelListBean;
    private View.OnClickListener mItemLis;

    public LePayChannelListAdapter(View.OnClickListener itemLis) {
        this.mItemLis = itemLis;
    }

    public void setData(List<LePayChannelBean> lepayChannelListBeen) {
        this.mLePayChannelListBean = lepayChannelListBeen;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lepay_channel_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(this.mLePayChannelListBean.get(position));
        holder.itemView.setOnClickListener(this.mItemLis);
    }

    @Override
    public int getItemCount() {
        if (mLePayChannelListBean == null) {
            return 0;
        }
        return mLePayChannelListBean.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        protected ImageView mIcon;
        protected TextView mTitle;
        protected TextView mSubTitle;
        protected TextView mInfo;
        protected LePayTagFlowLayout mTagsContainer;

        public ViewHolder(View v) {
            super(v);
            initV();
        }

        private void initV() {
            mIcon = (ImageView) itemView.findViewById(R.id.lepay_channel_icon_iv);
            mTitle = (TextView) itemView.findViewById(R.id.lepay_channel_title_tv);
            mSubTitle = (TextView) itemView.findViewById(R.id.lepay_channel_sub_title_tv);
            mInfo = (TextView) itemView.findViewById(R.id.lepay_channel_info_tv);
            mTagsContainer = (LePayTagFlowLayout) itemView.findViewById(R.id.lepay_channel_tag_container);
        }

        public void setData(LePayChannelBean channelBean) {
            if (channelBean != null) {
                xmain.image().bind(mIcon, channelBean.getIcon());
                itemView.setTag(channelBean);
                mTitle.setText(channelBean.getTitle());
                if (TextUtils.isEmpty(channelBean.getSubTitle())) {
                    mSubTitle.setVisibility(View.GONE);
                } else {
                    mSubTitle.setVisibility(View.VISIBLE);
                    mSubTitle.setText(channelBean.getSubTitle());
                }
                String[] array = channelBean.getTags();
                if (array == null || array.length <= 0) {
                    if (mTagsContainer.getChildCount() > 0) {
                        mTagsContainer.removeAllViews();
                    }
                    mTagsContainer.setVisibility(View.GONE);
                } else {
                    if (mTagsContainer.getChildCount() > 0) {
                        mTagsContainer.removeAllViews();
                    }
                    TextView child;
                    mTagsContainer.setVisibility(View.VISIBLE);
                    for (String temp : array) {
                        child = (TextView) LayoutInflater.from(itemView.getContext()).inflate(R.layout.lepay_tag_item, mTagsContainer, false);
                        child.setText(temp);
                        mTagsContainer.addView(child);
                    }
                }
                if (channelBean.getChannelId() == LePayConstants.PAY_CHANNEL.CHANNEL_Π) {
                    // 判断ypa状态
                    if (LePayConstants.YOUΠ_ACTIVE.NOTACTIVATION_CAN_APPLY == channelBean.getChannelStatus()
                            || LePayConstants.YOUΠ_ACTIVE.NOTACTIVATION_CANNOT_APPLY == channelBean.getChannelStatus()) {
                        // 未激活状态，显示未激活
                        mInfo.setText(R.string.lepay_ypa_notactivation);
                        // TODO 未激活状态需要显示成灰色背景
                    } else {
                        if (!TextUtils.isEmpty(channelBean.getAvailableLimit())) {
                            String info = String.format(itemView.getContext().getString(R.string.lepay_payment_available_limit_str), channelBean.getAvailableLimit());
                            mInfo.setText(info);
                        }
                    }
                    mInfo.setVisibility(View.VISIBLE);
                } else {
                    mInfo.setVisibility(View.GONE);
                }
                if (channelBean.getActive() == LePayConstants.PAY_ACTIVE.UNAVAILABLE) {
                    // TODO 置灰条目背景
                }
            }
        }
    }
}
