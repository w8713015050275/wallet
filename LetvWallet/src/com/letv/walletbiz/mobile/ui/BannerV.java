package com.letv.walletbiz.mobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.letv.walletbiz.R;
import com.letv.walletbiz.main.AutoSlideViewpager;
import com.letv.walletbiz.main.PagerIndicator;
import com.letv.walletbiz.main.WalletBannerAdapter;
import com.letv.walletbiz.main.bean.WalletBannerListBean;

/**
 * Created by changjiajie on 16-7-1.
 */
public class BannerV extends RelativeLayout {

    public AutoSlideViewpager bannerPager;
    public PagerIndicator indicatorContainer;
    private WalletBannerAdapter bannerAdapter;

    public BannerV(Context context) {
        this(context, null);
    }

    public BannerV(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initV(context);
    }

    private void initV(Context context) {
        View view = View.inflate(context, R.layout.wallet_mobile_item_banner, this);
        bannerPager = (AutoSlideViewpager) view.findViewById(R.id.banner_pager);
        indicatorContainer = (PagerIndicator) view.findViewById(R.id.indicator_container);
        bannerPager.setPagerIndicator(indicatorContainer);
        bannerAdapter = new WalletBannerAdapter(context);
        bannerPager.setAdapter(bannerAdapter);
    }

    public void bindBannerView(WalletBannerListBean.WalletBannerBean[] bannerList) {
        if (bannerAdapter != null && bannerAdapter.getData() != bannerList) {
            indicatorContainer.setTotalPageSize(bannerList.length);
            indicatorContainer.setCurrentPage(0);
            bannerAdapter.setData(bannerList);
            bannerPager.dataSetChanged();
        }
    }
}
