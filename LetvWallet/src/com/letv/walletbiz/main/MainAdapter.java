package com.letv.walletbiz.main;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
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
import com.letv.wallet.common.view.DividerGridItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.beans.CardCouponList;
import com.letv.walletbiz.coupon.utils.CouponUtils;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.main.bean.WalletBannerListBean.WalletBannerBean;
import com.letv.walletbiz.main.bean.WalletServiceListBean.WalletServiceBean;
import com.letv.walletbiz.movie.beans.MovieOrder;

import org.xutils.xmain;

/**
 * Created by liuliang on 16-4-26.
 */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_BANNER = 1;
    private static final int VIEW_TYPE_SERVICE = 2;
    public static final int VIEW_TYPE_CARD_COUPON = 3;
    public static final int VIEW_TYPE_COUPON = 4;

    public static final int POSITION_BANNER = 0;
    public static final int POSITION_SERVICE = 1;
    public static final int HEADER_COUNT = 2;

    private Context mContext;

    private WalletBannerBean[] mBannerList;
    private WalletServiceBean[] mServiceList;

    private CardCouponList mCardCouponList;
    private MovieOrder[] mMovieOrderList;
    private BaseCoupon[] mCouponList;

    public MainAdapter(Context context) {
        mContext = context;
    }

    public void setBannerList(WalletBannerBean[] bannerList) {
        mBannerList = bannerList;
        notifyDataSetChanged();
    }

    public void setServiceList(WalletServiceBean[] serviceList) {
        mServiceList = serviceList;
        notifyDataSetChanged();
    }

    public void setCardCouponList(CardCouponList cardCouponList) {
        mCardCouponList = cardCouponList;
        mMovieOrderList = mCardCouponList != null ? mCardCouponList.list : null;
        notifyDataSetChanged();
    }

    public void setCouponList(BaseCoupon[] couponList) {
        mCouponList = couponList;
        notifyDataSetChanged();
    }

    public void setCouponAndCardList(CardCouponList cardCouponList, BaseCoupon[] couponList) {
        mCardCouponList = cardCouponList;
        mMovieOrderList = mCardCouponList != null ? mCardCouponList.list : null;
        mCouponList = couponList;
        notifyDataSetChanged();

    }

    public void addMoreCouponList(BaseCoupon[] couponList) {
        if (couponList == null || couponList.length <= 0) {
            return;
        }
        int length = couponList.length;
        if (mCouponList != null) {
            length += mCouponList.length;
        }
        BaseCoupon[] newArray = new BaseCoupon[length];
        int start = 0;
        if (mCouponList != null && mCouponList.length > 0) {
            System.arraycopy(mCouponList, 0, newArray, start, mCouponList.length);
            start = mCouponList.length;
        }
        System.arraycopy(couponList, 0, newArray, start, couponList.length);
        mCouponList = newArray;
        notifyDataSetChanged();
    }

    public long getCouponLastId() {
        if (mCouponList != null && mCouponList.length > 0) {
            return mCouponList[mCouponList.length - 1].rank_id;
        }
        return -1;
    }

    public boolean hasCoupon() {
        return mCouponList != null && mCouponList.length > 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder viewHolder = null;
        View view;
        switch (viewType) {
            case VIEW_TYPE_BANNER:
                view = inflater.inflate(R.layout.wallet_main_item_banner, parent, false);
                viewHolder = new BannerViewHolder(view, mContext);
                break;
            case VIEW_TYPE_SERVICE:
                view = inflater.inflate(R.layout.wallet_main_item_service, parent, false);
                viewHolder = new ServiceViewHolder(view, mContext);
                break;
            case VIEW_TYPE_CARD_COUPON:
                view = inflater.inflate(R.layout.coupon_list_card_item, parent, false);
                viewHolder = new CardCouponViewHolder(view);
                break;
            case VIEW_TYPE_COUPON:
                view = inflater.inflate(R.layout.coupon_list_coupon_item, parent, false);
                viewHolder = new CouponViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_BANNER:
                if (holder instanceof BannerViewHolder) {
                    bindBannerView((BannerViewHolder) holder, mBannerList);
                }
                break;
            case VIEW_TYPE_SERVICE:
                if (holder instanceof ServiceViewHolder) {
                    bindServiceView((ServiceViewHolder) holder, mServiceList);
                }
                break;
            case VIEW_TYPE_CARD_COUPON:
                position -= HEADER_COUNT;
                if (mMovieOrderList != null && position < mMovieOrderList.length) {
                    bindCardCouponView(mMovieOrderList[position], (CardCouponViewHolder) holder, position);
                }
                break;
            case VIEW_TYPE_COUPON:
                position -= HEADER_COUNT;
                if (mMovieOrderList != null) {
                    position = position - mMovieOrderList.length;
                }
                if (mCouponList != null && position >= 0 && position < mCouponList.length) {
                    bindCouponView(mCouponList[position], (CouponViewHolder) holder, position);
                }
                break;
        }
    }

    public Object getItem(int position) {
        if (position < HEADER_COUNT) {
            return null;
        }
        position -= HEADER_COUNT;
        if (mMovieOrderList != null) {
            if (position < mMovieOrderList.length) {
                return mMovieOrderList[position];
            } else {
                position -= mMovieOrderList.length;
            }
        }
        if (mCouponList != null && position < mCouponList.length) {
            return mCouponList[position];
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_BANNER;
        } else if (position == 1) {
            return VIEW_TYPE_SERVICE;
        }
        position -= HEADER_COUNT;
        if (mMovieOrderList != null && position >= 0 && position < mMovieOrderList.length) {
            return VIEW_TYPE_CARD_COUPON;
        }
        return VIEW_TYPE_COUPON;
    }

    @Override
    public int getItemCount() {
        int count = HEADER_COUNT;
        if (mMovieOrderList != null) {
            count += mMovieOrderList.length;
        }
        if (mCouponList != null) {
            count += mCouponList.length;
        }
        return count;
    }

    public int getDataPosition(int position) {
        if (position < HEADER_COUNT) {
            return 0;
        }
        position -= HEADER_COUNT;
        if (mMovieOrderList != null) {
            if (position < mMovieOrderList.length) {
                return position;
            } else {
                position -= mMovieOrderList.length;
            }
        }
        if (mCouponList != null && position < mCouponList.length) {
            return position;
        }
        return 0;
    }

    private void bindBannerView(BannerViewHolder holder, WalletBannerBean[] bannerList) {
        if (holder.bannerAdapter != null && holder.bannerAdapter.getData() != bannerList) {
            holder.indicatorContainer.setTotalPageSize(bannerList.length);
            holder.indicatorContainer.setCurrentPage(0);
            holder.bannerAdapter.setData(bannerList);
            holder.bannerPager.dataSetChanged();
        }
    }

    private void bindServiceView(ServiceViewHolder holder, WalletServiceBean[] serviceList) {
        if (holder.mainPanelAdapter != null && holder.mainPanelAdapter.getData() != serviceList) {
            holder.mainPanelAdapter.setData(serviceList);
            holder.mainPanelLayoutManager.setSpanCount(holder.mainPanelAdapter.getSpanCount());
            holder.mainPanelAdapter.notifyDataSetChanged();
        }
    }

    private void bindCardCouponView(MovieOrder movieOrder, CardCouponViewHolder holder, int position) {
        updateItemBg(holder.itemView, movieOrder.movie_id);
        holder.cardNameView.setText(movieOrder.movie_name);
        if (!TextUtils.isEmpty(movieOrder.code)) {
            holder.cardCodeView.setText(movieOrder.code);
            holder.cardCodell.setVisibility(View.VISIBLE);
            holder.cardNoll.setVisibility(View.GONE);
        } else {
            holder.cardNoView.setText(movieOrder.third_no);
            holder.cardNoll.setVisibility(View.VISIBLE);
            holder.cardCodell.setVisibility(View.GONE);
        }
        holder.cardDateView.setText(getDateTime(movieOrder));
        xmain.image().bind(holder.cardIconView, movieOrder.poster_url, ImageOptionsHelper.getDefaltImageLoaderOptions());
    }

    private void bindCouponView(BaseCoupon coupon, CouponViewHolder holder, int position) {
        updateItemBg(holder.itemView, coupon.ucoupon_id);
        holder.couponNameView.setText(coupon.title);
        holder.couponTypeView.setText(coupon.service_name);
        holder.couponDateView.setText(mContext.getString(R.string.coupon_validite_period) + CouponUtils.getCouponExpiryDateStr(mContext, coupon));
        xmain.image().bind(holder.couponIconView, coupon.icon);
    }

    private void updateItemBg(View itemView, long id) {
        long temp = id % 4;
        int resId = R.drawable.coupon_item_green_bg;
        if (temp == 1) {
            resId = R.drawable.coupon_item_orange_bg;
        } else if (temp == 2) {
            resId = R.drawable.coupon_item_red_bg;
        } else if (temp == 3) {
            resId = R.drawable.coupon_item_yellow_bg;
        }
        itemView.setBackgroundResource(resId);
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

    public static class BannerViewHolder extends RecyclerView.ViewHolder {

        public AutoSlideViewpager bannerPager;
        public PagerIndicator indicatorContainer;
        private WalletBannerAdapter bannerAdapter;

        public BannerViewHolder(View itemView, Context context) {
            super(itemView);
            bannerPager = (AutoSlideViewpager) itemView.findViewById(R.id.banner_pager);
            indicatorContainer = (PagerIndicator) itemView.findViewById(R.id.indicator_container);
            bannerPager.setPagerIndicator(indicatorContainer);
            bannerAdapter = new WalletBannerAdapter(context);
            bannerPager.setAdapter(bannerAdapter);
        }
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {

        public RecyclerView mainPanelView;
        public MainPanelAdapter mainPanelAdapter;
        public GridLayoutManager mainPanelLayoutManager;

        public ServiceViewHolder(View itemView, Context context) {
            super(itemView);
            mainPanelView = (RecyclerView) itemView;
            mainPanelLayoutManager = new GridLayoutManager(context, 3);
            mainPanelView.setLayoutManager(mainPanelLayoutManager);
            mainPanelView.setNestedScrollingEnabled(false);
            DividerGridItemDecoration itemDecoration = new DividerGridItemDecoration(context, context.getColor(R.color.colorDivider), (int) DensityUtils.dip2px(0.25f));
            //itemDecoration.setDrawRect(false, false, true, true);
            mainPanelView.addItemDecoration(itemDecoration);
            mainPanelAdapter = new MainPanelAdapter(context);
            mainPanelView.setAdapter(mainPanelAdapter);
        }
    }

    public static class CardCouponViewHolder extends RecyclerView.ViewHolder {

        public TextView cardNameView;
        public TextView cardNoView;
        public TextView cardCodeView;
        private TextView cardDateView;
        public ImageView cardIconView;
        public LinearLayout cardNoll;
        public LinearLayout cardCodell;

        public CardCouponViewHolder(View itemView) {
            super(itemView);
            cardNameView = (TextView) itemView.findViewById(R.id.tv_card_name);
            cardNoView = (TextView) itemView.findViewById(R.id.tv_card_no);
            cardNoll = (LinearLayout) itemView.findViewById(R.id.viewNo);
            cardCodeView = (TextView) itemView.findViewById(R.id.tv_card_code);
            cardCodell = (LinearLayout) itemView.findViewById(R.id.viewCode);
            cardDateView = (TextView) itemView.findViewById(R.id.tv_card_date);
            cardIconView = (ImageView) itemView.findViewById(R.id.img_card_icon);
        }
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {

        public TextView couponNameView;
        public TextView couponTypeView;
        public TextView couponDateView;
        public ImageView couponIconView;

        public CouponViewHolder(View itemView) {
            super(itemView);
            couponNameView = (TextView) itemView.findViewById(R.id.tv_coupon_name);
            couponTypeView = (TextView) itemView.findViewById(R.id.tv_coupon_type);
            couponDateView = (TextView) itemView.findViewById(R.id.tv_coupon_date);
            couponIconView = (ImageView) itemView.findViewById(R.id.img_coupon_icon);
        }
    }
}
