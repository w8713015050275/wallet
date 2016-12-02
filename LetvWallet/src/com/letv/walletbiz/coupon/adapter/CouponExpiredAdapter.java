package com.letv.walletbiz.coupon.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.utils.CouponUtils;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.xmain;

import java.util.List;

/**
 * Created by lijunying on 16-4-19.
 */
public class CouponExpiredAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private int viewType;
    private List<BaseCoupon> mCouponExpiredList;
    private List<MovieOrder> mCardExpiredList;
    private CardHolder cardHolder;
    private int[] arraysResId = {R.drawable.coupon_item_green_bg, R.drawable.coupon_item_orange_bg,
            R.drawable.coupon_item_red_bg, R.drawable.coupon_item_yellow_bg};
    private int resIdState = -1;

    public CouponExpiredAdapter(Context mContext, int viewType) {
        this.mContext = mContext;
        this.viewType = viewType;
    }

    public void setData(List list) {
        if (viewType == CouponListAdapter.VIEW_TYPE_CARD_ITEM) {
            this.mCardExpiredList = list;
        } else if (viewType == CouponListAdapter.VIEW_TYPE_COUPON_ITEM) {
            this.mCouponExpiredList = list;
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder mHolder = null;
        switch (viewType) {
            case CouponListAdapter.VIEW_TYPE_CARD_ITEM:
                mHolder = new CardHolder(LayoutInflater.from(mContext).inflate(R.layout.coupon_list_card_item, parent, false));
                break;
            case CouponListAdapter.VIEW_TYPE_COUPON_ITEM:
                mHolder = new CouponHolder(LayoutInflater.from(mContext).inflate(R.layout.coupon_list_coupon_item, parent, false));
                break;
        }
        return mHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (viewType == CouponListAdapter.VIEW_TYPE_CARD_ITEM && holder instanceof CardHolder) {
            cardHolder = (CardHolder) holder;
            if (mCardExpiredList.get(position) != null) {
                cardHolder.tvCardName.setText(mCardExpiredList.get(position).movie_name);
                if (!TextUtils.isEmpty(mCardExpiredList.get(position).code)) {
                    cardHolder.tvCardCode.setText(mCardExpiredList.get(position).code);
                    cardHolder.cardCodell.setVisibility(View.VISIBLE);
                    cardHolder.cardNoll.setVisibility(View.GONE);
                }else {
                    cardHolder.tvCardNo.setText(mCardExpiredList.get(position).third_no);
                    cardHolder.cardNoll.setVisibility(View.VISIBLE);
                    cardHolder.cardCodell.setVisibility(View.GONE);
                }
                cardHolder.tvCardDate.setText(getDateTime(mCardExpiredList.get(position)));
  	            cardHolder.tvCardExpired.setText(R.string.coupon_list_expired_list_item_show_tag);
                xmain.image().bind(cardHolder.imgCardIcon, mCardExpiredList.get(position).poster_url, ImageOptionsHelper.getDefaltImageLoaderOptions());
                cardHolder.itemView.setBackgroundResource(arraysResId[position % 4]);
            }
        } else if (viewType == CouponListAdapter.VIEW_TYPE_COUPON_ITEM && holder instanceof CouponHolder) {
            CouponHolder itemHolder = (CouponHolder) holder;
            if (mCouponExpiredList.get(position) != null) {
                itemHolder.tvCouponName.setText(mCouponExpiredList.get(position).getTitle());
                itemHolder.tvCouponType.setText(mCouponExpiredList.get(position).getService_name());
                itemHolder.tvCouponDate.setText(mContext.getString(R.string.coupon_validite_period)+
                        CouponUtils.getCouponExpiryDateStr(mContext, mCouponExpiredList.get(position)));
                itemHolder.tvCouponExpired.setText(getCouponStateResId(mCouponExpiredList.get(position).getState()));
                xmain.image().bind(itemHolder.imgCouponIcon, mCouponExpiredList.get(position).getIcon());
                itemHolder.itemView.setBackgroundResource(arraysResId[(position + 2) % 4]);
            }
        }
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            if ((position == getItemCount() - 1)) {
                ((ViewGroup.MarginLayoutParams) params).bottomMargin = (int) DensityUtils.dip2px(35);
            } else {
                ((ViewGroup.MarginLayoutParams) params).bottomMargin = 0;
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @Override
    public int getItemCount() {
        if (viewType == CouponListAdapter.VIEW_TYPE_CARD_ITEM) {
            return mCardExpiredList == null ? 0 : mCardExpiredList.size();

        } else if (viewType == CouponListAdapter.VIEW_TYPE_COUPON_ITEM) {
            return mCouponExpiredList == null ? 0 : mCouponExpiredList.size();
        }
        return 0;
    }

    private int getCouponStateResId(int state){
        resIdState = -1;
        switch (state) {
            case CouponConstant.COUPON_STATE_UNCONSUMED:
                resIdState = R.string.coupon_list_expired_list_item_unconsumed_tag;
                break;
            case CouponConstant.COUPON_STATE_USED:
                resIdState = R.string.coupon_list_expired_list_item_used_tag;
                break;
            case CouponConstant.COUPON_STATE_EXPIRED:
                resIdState = R.string.coupon_list_expired_list_item_expired_tag;
                break;

        }
        return resIdState;
    }

    private String getDateTime(MovieOrder order) {
        if (order == null) {
            return "";
        }
        MovieOrder.TicketInfo info = order.ticket_info;
        if (info == null) {
            return "";
        }
        String date = DateUtils.convertPatternForDate(info.date, "yyyyMMdd", "MM-dd");
        return date + " " + info.time;
    }

    public Object getItem(int position) {
        switch (getItemViewType(position)) {
            case CouponListAdapter.VIEW_TYPE_COUPON_ITEM:
                return mCouponExpiredList == null ? null : mCouponExpiredList.get(position);

            case CouponListAdapter.VIEW_TYPE_CARD_ITEM:
                return mCardExpiredList == null ? null : mCardExpiredList.get(position);
        }
        return null;

    }

    static class CouponHolder extends RecyclerView.ViewHolder {

        TextView tvCouponName, tvCouponType, tvCouponDate, tvCouponExpired;
        ImageView imgCouponIcon;

        public CouponHolder(View itemView) {
            super(itemView);
            tvCouponName = (TextView) itemView.findViewById(R.id.tv_coupon_name);
            tvCouponType = (TextView) itemView.findViewById(R.id.tv_coupon_type);
            tvCouponDate = (TextView) itemView.findViewById(R.id.tv_coupon_date);
            tvCouponExpired = (TextView) itemView.findViewById(R.id.tv_coupon_expired);
            tvCouponExpired.setVisibility(View.VISIBLE);
            imgCouponIcon = (ImageView) itemView.findViewById(R.id.img_coupon_icon);
        }
    }

    static class CardHolder extends RecyclerView.ViewHolder {

        TextView tvCardName, tvCardNo, tvCardCode , tvCardDate, tvCardExpired;
        ImageView imgCardIcon;
        public LinearLayout cardNoll;
        public LinearLayout cardCodell;

        public CardHolder(View itemView) {
            super(itemView);
            tvCardName = (TextView) itemView.findViewById(R.id.tv_card_name);
            tvCardNo = (TextView) itemView.findViewById(R.id.tv_card_no);
            cardNoll = (LinearLayout) itemView.findViewById(R.id.viewNo);
            tvCardCode = (TextView) itemView.findViewById(R.id.tv_card_code);
            cardCodell = (LinearLayout) itemView.findViewById(R.id.viewCode);
            tvCardDate = (TextView) itemView.findViewById(R.id.tv_card_date);
            tvCardExpired = (TextView) itemView.findViewById(R.id.tv_card_expired);
            tvCardExpired.setVisibility(View.VISIBLE);
            imgCardIcon = (ImageView) itemView.findViewById(R.id.img_card_icon);
        }
    }
}
