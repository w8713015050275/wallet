package com.letv.walletbiz.member.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.member.beans.CouponBean;

import org.xutils.xmain;

import java.util.Date;

/**
 * Created by zhanghuancheng on 16-11-23.
 */
public class CouponListAdapter extends RecyclerView.Adapter<CouponListAdapter.ViewHolder> {

    private Context mContext;
    private CouponBean[] mCouponList;
    private OnItemClickListener mOnItemClickListener = null;
    private long mUcouponId;

    public CouponListAdapter(Context context, CouponBean[] couponList, long ucouponId) {
        mContext = context;
        mCouponList = couponList;
        mUcouponId = ucouponId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coupon_list_select_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCouponList != null) {
            holder.setData(mCouponList[position]);
        }
    }

    @Override
    public int getItemCount() {
        if (mCouponList == null)
            return 0;
        return mCouponList.length;
    }

    public void setData(CouponBean[] couponList, long ucouponId) {
        mCouponList = couponList;
        mUcouponId = ucouponId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(CouponBean couponBean);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private static final int STATE_RED = 1;
        private static final int STATE_GREEN = 2;
        private static final int STATE_ORANGE = 3;
        private TextView tv_coupon_name;
        private TextView tv_coupon_type;
        private TextView tv_coupon_date;
        private ImageView img_coupon_icon;
        private ImageView img_select_icon;
        protected CouponBean mCouponBean;

        public ViewHolder(View v) {
            super(v);
            initV();
        }

        private void initV() {
            tv_coupon_name = (TextView) itemView.findViewById(R.id.tv_coupon_name);
            tv_coupon_type = (TextView) itemView.findViewById(R.id.tv_coupon_type);
            tv_coupon_date = (TextView) itemView.findViewById(R.id.tv_coupon_date);
            img_coupon_icon = (ImageView) itemView.findViewById(R.id.img_coupon_icon);
            img_select_icon = (ImageView) itemView.findViewById(R.id.img_select_icon);
            itemView.setOnClickListener(this);
        }

        public void setData(CouponBean couponBean) {
            if (couponBean != null) {
                mCouponBean = couponBean;
                int bgresId;
                switch (mCouponBean.getState()) {
                    case STATE_RED:
                        bgresId = R.drawable.coupon_item_red_bg;
                        break;
                    case STATE_GREEN:
                        bgresId = R.drawable.coupon_item_green_bg;
                        break;
                    case STATE_ORANGE:
                        bgresId = R.drawable.coupon_item_orange_bg;
                        break;
                    default: {
                        bgresId = R.drawable.coupon_item_green_bg;
                    }
                }
                itemView.setBackgroundResource(bgresId);
                tv_coupon_name.setText(mCouponBean.getTitle());
                tv_coupon_type.setText(mCouponBean.getService_name());
                tv_coupon_date.setText(getValiditePeriodDate(mContext, mCouponBean));
                xmain.image().bind(img_coupon_icon, mCouponBean.icon);
                if (mCouponBean.ucoupon_id != mUcouponId) {
                    img_select_icon.setVisibility(View.GONE);
                } else {
                    img_select_icon.setVisibility(View.VISIBLE);
                }
            }
        }

        private CharSequence getValiditePeriodDate(Context context, CouponBean coupon) {
            if (context == null || coupon == null) {
                return "";
            }
            String startDate = DateUtils.formatDate(new Date(coupon.start_time * 1000), "yyyy.MM.dd");
            String endDate = DateUtils.formatDate(new Date(coupon.end_time * 1000), "yyyy.MM.dd");
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(context.getString(R.string.coupon_validite_period));
            builder.append(startDate,
                    new AbsoluteSizeSpan(context.getResources().getDimensionPixelSize(R.dimen.tv_coupon_date_sec_textsize)), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.append(context.getString(R.string.coupon_validite_period_to));
            builder.append(endDate,
                    new AbsoluteSizeSpan(context.getResources().getDimensionPixelSize(R.dimen.tv_coupon_date_sec_textsize)), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return builder;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(mCouponBean);
            }
        }
    }
}
