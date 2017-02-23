package com.letv.walletbiz.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.main.bean.WalletBannerListBean.WalletBannerBean;

import org.xutils.xmain;

import java.util.ArrayList;

/**
 * Created by liuliang on 16-4-14.
 */
public class WalletBannerAdapter extends AutoSlidePagerAdapter implements View.OnClickListener {

    private Context mContext;

    private ArrayList<ImageView> mImageViewArray = new ArrayList<ImageView>();
    private WalletBannerBean[] mData;


    public WalletBannerAdapter(Context context) {
        mContext = context;
    }

    public void setData(WalletBannerBean[] data) {
        mData = data;
        notifyDataSetChanged();
    }

    public WalletBannerBean[] getData() {
        return mData;
    }

    @Override
    public int getRealCount() {
        return mData == null ? 0 : mData.length;
    }

    @Override
    public int getItemIndexForPosition(int position) {
        if (getRealCount() == 0) {
            return position;
        }
        return position % getRealCount();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position %= mData.length;
        ImageView imageView = null;
        if (mImageViewArray.size() > 0) {
            imageView = mImageViewArray.remove(0);
        }
        if (imageView == null) {
            imageView = new ImageView(mContext);
            imageView.setOnClickListener(this);
        }
        TypedValue outValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.le_img_alpha, outValue, true);
        imageView.setAlpha(outValue.getFloat());
        imageView.setTag(position);
        xmain.image().bind(imageView, mData[position].banner_post);
        container.addView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageView view = (ImageView) object;
        container.removeView(view);
        mImageViewArray.add(view);
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : (mData.length > 1 ? mData.length * 2 + 1: mData.length);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onClick(View v) {
        Integer temp = (Integer) v.getTag();
        if (mData == null || temp == null) {
            return;
        }
        int position = temp;
        if (position < 0 || position >= mData.length) {
            return;
        }
        if (mData[position].banner_type == WalletBannerBean.BANNER_TYPE_LINK) {
            if (mData[position].position_id == ActivityConstant.BUSINESS_ID.MAIN_ID) {
                Action.uploadExposeTab(Action.WALLET_HOME_BANNER, mData[position].banner_link);
            }
            Intent intent = new Intent(mContext, WalletMainWebActivity.class);
            intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, mData[position].banner_name);
            intent.putExtra(CommonConstants.EXTRA_URL, mData[position].banner_link);
            intent.putExtra(WalletConstant.EXTRA_WEB_WITH_ACCOUNT, mData[position].need_token == 1);
            intent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_BANNER);
            mContext.startActivity(intent);
        } else if (mData[position].banner_type == WalletBannerBean.BANNER_TYPE_APP) {
            Bundle bundle = new Bundle();
            bundle.putString(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_BANNER);
            AppUtils.LaunchAppWithBundle(mContext, mData[position].package_name, mData[position].jump_param, bundle, true);
        }
    }
}
