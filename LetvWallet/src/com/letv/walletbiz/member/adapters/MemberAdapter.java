package com.letv.walletbiz.member.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.main.AutoSlideViewpager;
import com.letv.walletbiz.main.PagerIndicator;
import com.letv.walletbiz.member.beans.BannerListBean;
import com.letv.walletbiz.member.beans.MemberTypeListBean;
import com.letv.walletbiz.member.beans.ProductListBean;
import com.letv.walletbiz.member.widget.BannerPtrFrameLayout;

import org.xutils.xmain;

/**
 * Created by zhanghuancheng on 16-11-15.
 */
public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_BANNER = 1;
    private static final int VIEW_TYPE_PRODUCT_LIST = 2;
    private static final int VIEW_TYPE_AGREEMENT = 3;
    private static final int VIEW_TYPE_AVAILABLE_SCOPE = 4;

    public static final int POSITION_BANNER = 0;
    public static final int POSITION_PRODUCT_LIST = 1;
    public static final int POSITION_AGREEMENT = 2;

    private static final int ITEM_COUNT = 4;
    private MemberTypeListBean.MemberTypeBean mMemberTypeBean;

    private Context mContext;
    private BannerListBean.BannerBean[] mBannerList;
    private ProductListBean.ProductBean[] mProductList;
    private BannerPtrFrameLayout mPtrFrameLayout;
    private int mBlankMarginPX;

    public BannerViewHolder getmBannerVH() {
        return mBannerVH;
    }

    private BannerViewHolder mBannerVH;
    private ViewGroup mRefreshView;


    public BannerListBean.BannerBean[] getBannerList() {
        return mBannerList;
    }

    public ProductListBean.ProductBean[] getProductList() {
        return mProductList;
    }

    public MemberAdapter(Context context, MemberTypeListBean.MemberTypeBean memberTypeBean, ViewGroup recyclerView) {
        mContext = context;
        mMemberTypeBean = memberTypeBean;
        mRefreshView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder viewHolder = null;
        View view;
        RecyclerView.LayoutParams layoutParams;
        mBlankMarginPX = (int) DensityUtils.dip2px(6.0F);
        switch (viewType) {
            case VIEW_TYPE_BANNER:
                view = inflater.inflate(R.layout.member_banner_layout, parent, false);
                layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (mBannerList != null) {
                    layoutParams.setMargins(0, mBlankMarginPX, 0, 0);
                }
                viewHolder = new BannerViewHolder(view, mContext);
                mBannerVH = (BannerViewHolder) viewHolder;
                mPtrFrameLayout.setViewPager(mBannerVH.bannerPager);
                break;
            case VIEW_TYPE_PRODUCT_LIST:
                view = inflater.inflate(R.layout.member_product_list_layout, parent, false);
                layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();

                layoutParams.setMargins(0, mBlankMarginPX, 0, 0);
                viewHolder = new ProductListViewHolder(view, mContext);
                break;
            case VIEW_TYPE_AGREEMENT:
                view = inflater.inflate(R.layout.member_agreement_layout, parent, false);
                viewHolder = new AgreementViewHolder(view);
                break;
            case VIEW_TYPE_AVAILABLE_SCOPE:
                view = inflater.inflate(R.layout.member_available_scope_layout, parent, false);
                layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
                layoutParams.setMargins(0, mBlankMarginPX, 0, 0);
                viewHolder = new AvailableScopeViewHolder(view);
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
            case VIEW_TYPE_PRODUCT_LIST:
                if (holder instanceof ProductListViewHolder) {
                    bindProductListView((ProductListViewHolder) holder, mProductList);
                }
                break;
            case VIEW_TYPE_AGREEMENT:
                bindAgreementView((AgreementViewHolder) holder);
                break;
            case VIEW_TYPE_AVAILABLE_SCOPE:
                bindAvailableScopeView((AvailableScopeViewHolder) holder);
                break;
        }
    }

    private void bindBannerView(BannerViewHolder holder, BannerListBean.BannerBean[] bannerList) {
        View parent = (View)holder.bannerFrameLayout.getParent();
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)parent.getLayoutParams();
        if (bannerList == null) {
            layoutParams.setMargins(0, 0, 0, 0);
            holder.bannerFrameLayout.setVisibility(View.GONE);
            return;
        }
        layoutParams.setMargins(0, mBlankMarginPX, 0, 0);
        holder.bannerFrameLayout.setVisibility(View.VISIBLE);
        if (holder.bannerAdapter != null && holder.bannerAdapter.getData() != bannerList) {
            holder.indicatorContainer.setTotalPageSize(bannerList.length);
            holder.indicatorContainer.setCurrentPage(0);
            holder.bannerAdapter.setData(bannerList);
            holder.bannerPager.dataSetChanged();
        }
    }

    private void bindProductListView(ProductListViewHolder holder, ProductListBean.ProductBean[] productList) {
        if (holder.itemListAdapter != null) {
            holder.itemListAdapter.setData(productList);
            holder.itemListAdapter.notifyDataSetChanged();
        }
    }

    private void bindAgreementView(AgreementViewHolder holder) {
        holder.agreement.setText(mMemberTypeBean.name + mContext.getString(R.string.label_agreement));
    }

    private void bindAvailableScopeView(AvailableScopeViewHolder holder) {
        holder.availableScope.setText(mMemberTypeBean.description);
        xmain.image().bind(holder.availableScopeIV, mMemberTypeBean.img_url, ImageOptionsHelper.getDefaltImageLoaderOptions());
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_BANNER;
        } else if (position == 1) {
            return VIEW_TYPE_PRODUCT_LIST;
        } else if (position == 2) {
            return VIEW_TYPE_AGREEMENT;
        } else if (position == 3) {
            return VIEW_TYPE_AVAILABLE_SCOPE;
        }

        return -1;
    }

    public void setBannerList(BannerListBean.BannerBean[] bannerList) {
        mBannerList = bannerList;
        notifyDataSetChanged();
    }

    public void setProductList(ProductListBean.ProductBean[] productList) {
        mProductList = productList;
        notifyDataSetChanged();
    }

    public void setPtrFrameLayout(BannerPtrFrameLayout ptrFrameLayout) {
        mPtrFrameLayout = ptrFrameLayout;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {

        public FrameLayout bannerFrameLayout;
        public AutoSlideViewpager bannerPager;
        public PagerIndicator indicatorContainer;
        private BannerAdapter bannerAdapter;

        public BannerViewHolder(View itemView, Context context) {
            super(itemView);
            bannerFrameLayout = (FrameLayout) itemView.findViewById(R.id.banner_frame_layout);
            bannerPager = (AutoSlideViewpager) itemView.findViewById(R.id.banner_pager);
            indicatorContainer = (PagerIndicator) itemView.findViewById(R.id.indicator_container);
            bannerPager.setPagerIndicator(indicatorContainer);
            bannerAdapter = new BannerAdapter(context);
            bannerPager.setAdapter(bannerAdapter);
        }
    }

    public static class ProductListViewHolder extends RecyclerView.ViewHolder {

        public RecyclerView productListView;
        public MemberProductListAdapter itemListAdapter;
        public LinearLayoutManager productListLayoutManager;

        public ProductListViewHolder(View itemView, Context context) {
            super(itemView);
            productListView = (RecyclerView) itemView;
            productListLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            productListView.setLayoutManager(productListLayoutManager);
            productListView.setNestedScrollingEnabled(false);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, context.getResources().getColor(R.color.colorDividerLineBg),
                    DividerItemDecoration.VERTICAL_LIST, context.getResources().getDimensionPixelSize(R.dimen.divider_width));
            productListView.addItemDecoration(dividerItemDecoration);
            itemListAdapter = new MemberProductListAdapter(context);
            productListView.setAdapter(itemListAdapter);
        }
    }

    public static class AgreementViewHolder extends RecyclerView.ViewHolder {

        public TextView agreement;
        public TextView arrow;

        public AgreementViewHolder(View itemView) {
            super(itemView);
            agreement = (TextView) itemView.findViewById(R.id.membership_agreement);
            arrow = (TextView) itemView.findViewById(R.id.more);
        }
    }

    public static class AvailableScopeViewHolder extends RecyclerView.ViewHolder {

        public TextView availableScope;
        public ImageView availableScopeIV;

        public AvailableScopeViewHolder(View itemView) {
            super(itemView);
            availableScope = (TextView) itemView.findViewById(R.id.available_scope_tv);
            availableScopeIV = (ImageView) itemView.findViewById(R.id.available_scope_iv);
        }
    }
}
