package com.letv.walletbiz.member.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.walletbiz.main.WalletMainWebActivity;
import com.letv.walletbiz.member.MemberConstant;
import com.letv.walletbiz.member.beans.BannerListBean;

import org.xutils.xmain;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/10.
 */
public class BannerAdapter extends PagerAdapter implements View.OnClickListener{
    private Context mContext;

    private ArrayList<ImageView> mImageViewArray = new ArrayList<ImageView>();
    private BannerListBean.BannerBean[] mData;

    public BannerAdapter(Context context) {
        mContext = context;
    }

    public void setData(BannerListBean.BannerBean[] data) {
        mData = data;
        notifyDataSetChanged();
    }

    public BannerListBean.BannerBean[] getData() {
        return mData;
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
        return mData == null ? 0 : (mData.length > 1 ? Integer.MAX_VALUE : mData.length);
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
        if (mData[position].banner_type == MemberConstant.BANNER_TYPE_LINK) {
            Intent intent = new Intent(mContext, WalletMainWebActivity.class);
            intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, mData[position].banner_name);
            intent.putExtra(CommonConstants.EXTRA_URL, mData[position].banner_link);
            mContext.startActivity(intent);
        } else if (mData[position].banner_type == MemberConstant.BANNER_TYPE_APP) {
            AppUtils.LaunchApp(mContext, mData[position].package_name, mData[position].jump_param);
        }
    }
}
