package com.letv.walletbiz.coupon.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DateUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.utils.CouponUtils;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.xmain;

import java.util.List;

/**
 * Created by lijunying on 16-4-15.
 */
public class CouponListAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private LayoutInflater mInflater;

    public static final int VIEW_TYPE_CARD_TITLE = 3;
    public static final int VIEW_TYPE_CARD_ITEM = 4;
    public static final int VIEW_TYPE_CARD_MORE = 2;

    public static final int VIEW_TYPE_COUPON_TITLE = 0;
    public static final int VIEW_TYPE_COUPON_ITEM = 1;
    private int viewType = -1;
    private CatalogHolder titleHoder;   //title
    private CouponHolder couponHodler;  //优惠劵item
    private CardMoreHolder cardMoreHodler; //卡劵更多
    private CardHolder cardHolder;  //卡劵item
    private List<BaseCoupon> mCouponList;
    private List<MovieOrder> mCardList;
    private int couponViewCount = 0, cardViewCount = 0;
    private int couponViewPosition = -1;
    private Resources res;
    private int[] arraysResId = {R.drawable.coupon_item_green_bg, R.drawable.coupon_item_orange_bg,
            R.drawable.coupon_item_red_bg, R.drawable.coupon_item_yellow_bg};

    public CouponListAdapter(Context mContext) {
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        res = mContext.getResources();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder mHolder = null;
        switch (viewType) {
            case VIEW_TYPE_CARD_TITLE:
            case VIEW_TYPE_COUPON_TITLE:
                mHolder = new CatalogHolder(mInflater.inflate(R.layout.coupon_list_catalog_item, parent, false));
                break;
            case VIEW_TYPE_CARD_ITEM:
                mHolder = new CardHolder(mInflater.inflate(R.layout.coupon_list_card_item, parent, false));
                break;
            case VIEW_TYPE_CARD_MORE:
                mHolder = new CardMoreHolder(mInflater.inflate(R.layout.coupon_list_more_item, parent, false));
                break;
            case VIEW_TYPE_COUPON_ITEM:
                mHolder = new CouponHolder(mInflater.inflate(R.layout.coupon_list_coupon_item, parent, false));
                break;
        }
        return mHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_CARD_TITLE && holder instanceof CatalogHolder) { //卡劵列表title
            titleHoder = (CatalogHolder) holder;
            titleHoder.tvCatalog.setText(R.string.coupon_card);

        } else if (viewType == VIEW_TYPE_CARD_ITEM && holder instanceof CardHolder) {
            cardHolder = (CardHolder) holder;
            cardHolder.itemView.setBackgroundResource(arraysResId[position % 4]);
            if (mCardList.get(position - 1) != null) {
                cardHolder.tvCardName.setText(mCardList.get(position - 1).movie_name);
                if (!TextUtils.isEmpty(mCardList.get(position - 1).code)) {
                    cardHolder.tvCardCode.setText(mCardList.get(position - 1).code);
                    cardHolder.cardCodell.setVisibility(View.VISIBLE);
                    cardHolder.cardNoll.setVisibility(View.GONE);
                }else {
                    cardHolder.tvCardNo.setText(mCardList.get(position - 1).third_no);
                    cardHolder.cardNoll.setVisibility(View.VISIBLE);
                    cardHolder.cardCodell.setVisibility(View.GONE);
                }
                cardHolder.tvCardDate.setText(getDateTime(mCardList.get(position - 1)));
                xmain.image().bind(cardHolder.imgCardIcon, mCardList.get(position - 1).poster_url, ImageOptionsHelper.getDefaltImageLoaderOptions());
            }

        } else if (viewType == VIEW_TYPE_CARD_MORE && holder instanceof CardMoreHolder) {  //卡劵moreview
            cardMoreHodler = (CardMoreHolder) holder;
            if (onClickListener != null) {
                cardMoreHodler.itemView.setOnClickListener(onClickListener);
            }
            cardMoreHodler.tvMore.setText(R.string.coupon_list_more_item);

        } else if (viewType == VIEW_TYPE_COUPON_TITLE && holder instanceof CatalogHolder) {  //优惠劵列表title
            titleHoder = (CatalogHolder) holder;
            titleHoder.tvCatalog.setText(R.string.label_coupon);

        } else if (viewType == VIEW_TYPE_COUPON_ITEM && holder instanceof CouponHolder) {
            couponHodler = (CouponHolder) holder;
            couponHodler.itemView.setBackgroundResource(arraysResId[3 - position % 4]);
            couponViewPosition = position - cardViewCount - 1;
            if (mCouponList.get(couponViewPosition) != null) {
                couponHodler.tvCouponName.setText(mCouponList.get(couponViewPosition).getTitle());
                couponHodler.tvCouponType.setText(mCouponList.get(couponViewPosition).getService_name());
                couponHodler.tvCouponDate.setText(res.getString(R.string.coupon_validite_period)+
                        CouponUtils.getCouponExpiryDateStr(mContext, mCouponList.get(couponViewPosition)));
                xmain.image().bind(couponHodler.imgCouponIcon, mCouponList.get(couponViewPosition).getIcon());
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (cardViewCount > 0) {
            if (position == 0) {
                return VIEW_TYPE_CARD_TITLE; //卡劵 title

            } else if (position <= mCardList.size()) {
                return VIEW_TYPE_CARD_ITEM;

            } else if (position < cardViewCount) {
                return VIEW_TYPE_CARD_MORE;   // 更多卡劵

            } else if (position < cardViewCount + 1) {  //优惠劵 title
                return VIEW_TYPE_COUPON_TITLE;

            } else {
                return VIEW_TYPE_COUPON_ITEM;
            }

        } else {         // 卡劵列表为空
            if (position == 0) {
                return VIEW_TYPE_COUPON_TITLE;
            } else {
                return VIEW_TYPE_COUPON_ITEM;
            }
        }

    }

    public Object getItem(int position) {
        if (position <0 || position > couponViewCount+cardViewCount-1) {
            return null;
        }
        switch (getItemViewType(position)) {
            case VIEW_TYPE_CARD_ITEM:
                return mCardList == null ? null : mCardList.get(position - 1);

            case VIEW_TYPE_COUPON_ITEM:
                return mCouponList == null ? null : mCouponList.get(position - cardViewCount - 1);
        }
        return null;

    }

    @Override
    public int getItemCount() {
        return countCouponItemView() + countCardItemView();
    }

    public void setCouponData(List<BaseCoupon> mCouponList) {
        this.mCouponList = mCouponList;
        countCouponItemView();
        notifyDataSetChanged();
    }

    public void setCardData(List<MovieOrder> mCardList) {
        this.mCardList = mCardList;
        countCardItemView();
        notifyDataSetChanged();
    }

    public void setData(List<BaseCoupon> mCouponList, List<MovieOrder> mCardList) {
        this.mCouponList = mCouponList;
        this.mCardList = mCardList;
        countCouponItemView();
        countCardItemView();
        notifyDataSetChanged();
    }

    private boolean moreCard = true;

    public void enableLoadMoreCard(boolean moreCard) {
        this.moreCard = moreCard;
        countCardItemView();
        notifyDataSetChanged();
    }

    public void setMoreViewOnClickListener(View.OnClickListener l) {
        this.onClickListener = l;
    }

    private View.OnClickListener onClickListener;

    private int countCouponItemView() {
        couponViewCount = 0;
        if (mCouponList != null && mCouponList.size() > 0) {
            couponViewCount = mCouponList.size() + 1;
        }

        return couponViewCount;
    }

    private int countCardItemView() {
        cardViewCount = 0;
        if (mCardList != null && mCardList.size() > 0) {
            cardViewCount = mCardList.size() + 1; //第0条为catalog
            if (moreCard) {
                cardViewCount++;  //更多view
            }
        }
        return cardViewCount;
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

    static class CatalogHolder extends RecyclerView.ViewHolder {

        TextView tvCatalog;

        public CatalogHolder(View itemView) {
            super(itemView);
            tvCatalog = (TextView) itemView.findViewById(R.id.tv_coupon_list_catalog);
        }
    }

    static class CouponHolder extends RecyclerView.ViewHolder {

        TextView tvCouponName, tvCouponType, tvCouponDate;
        ImageView imgCouponIcon;

        public CouponHolder(View itemView) {
            super(itemView);
            tvCouponName = (TextView) itemView.findViewById(R.id.tv_coupon_name);
            tvCouponType = (TextView) itemView.findViewById(R.id.tv_coupon_type);
            tvCouponDate = (TextView) itemView.findViewById(R.id.tv_coupon_date);
            imgCouponIcon = (ImageView) itemView.findViewById(R.id.img_coupon_icon);
        }
    }

    static class CardHolder extends RecyclerView.ViewHolder {

        TextView tvCardName, tvCardNo,tvCardCode, tvCardDate;
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
            imgCardIcon = (ImageView) itemView.findViewById(R.id.img_card_icon);
        }
    }

    static class CardMoreHolder extends RecyclerView.ViewHolder {

        TextView tvMore;

        public CardMoreHolder(View itemView) {
            super(itemView);
            tvMore = (TextView) itemView.findViewById(R.id.tv_coupon_list_more);
        }
    }


}
