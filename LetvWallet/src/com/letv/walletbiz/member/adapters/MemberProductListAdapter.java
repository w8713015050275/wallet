package com.letv.walletbiz.member.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.member.activity.MemberActivity;
import com.letv.walletbiz.member.beans.ProductListBean;
import com.letv.walletbiz.member.pay.MemberProduct;

import org.xutils.xmain;

/**
 * Created by zhanghuancheng on 2016/11/9.
 */
public class MemberProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int MONTH = 1;
    private static final int SEASON = 2;
    private static final int YEAR = 3;
    private static final int CONTINUOUS = 4;
    private Context mContext;
    private ProductListBean.ProductBean[] mData;

    public MemberProductListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.member_product_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ProductListBean.ProductBean itemBean = mData[position];
        ItemHolder itemHolder = (ItemHolder) holder;
        itemHolder.icon.setBackgroundResource(getTextByKind(itemBean.kind));
        String totalPriceFormat = mContext.getResources().getString(R.string.total_price);
        String totalPrice = String.format(totalPriceFormat, itemBean.price);
        itemHolder.totalPrice.setText(totalPrice);

        String monthPriceFormat = mContext.getResources().getString(R.string.month_price);
        String monthPrice = String.format(monthPriceFormat, itemBean.month_price);
        itemHolder.monthPrice.setText(monthPrice);
        if (Integer.parseInt(itemBean.kind) == CONTINUOUS || Integer.parseInt(itemBean.kind) == MONTH) {
            itemHolder.monthPrice.setVisibility(View.GONE);
        }

        itemHolder.tag.setText(itemBean.tag);
        itemHolder.copy.setText(itemBean.description);
        itemHolder.purchase.setSelected(true);
        itemHolder.purchase.setTag(itemBean);
        itemHolder.purchase.setOnClickListener(this);
    }

    private int getTextByKind(String kind) {
        int resId = R.drawable.member_month;
        if (kind != null) {
            switch (Integer.valueOf(kind)) {
                case CONTINUOUS:
                    resId = R.drawable.member_continuous;
                    break;
                case MONTH:
                    resId = R.drawable.member_month;
                    break;
                case SEASON:
                    resId = R.drawable.member_season;
                    break;
                case YEAR:
                    resId = R.drawable.member_year;
                    break;
            }
        }
        return resId;
    }

    public ProductListBean.ProductBean[] getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.length;
    }

    public void setData(ProductListBean.ProductBean[] result) {
        mData = result;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (!((BaseFragmentActivity)mContext).isNetworkAvailable()) {
            ((MemberActivity)mContext).showNetFailToast();
            return;
        }
        ProductListBean.ProductBean itemBean = (ProductListBean.ProductBean) v.getTag();
        if (AccountHelper.getInstance().loginLetvAccountIfNot((Activity)mContext, null)) {
            MemberProduct order = new MemberProduct(R.string.movie_order_view_label, itemBean.id, itemBean.sku_no, itemBean.name, itemBean.duration, itemBean.description, itemBean.price, itemBean.spu_name, itemBean.protocol_url);//itemBean to MemberProduct
            order.showOrderSure(mContext, -1L);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView totalPrice;
        private TextView monthPrice;
        private TextView tag;
        private TextView copy;
        private Button purchase;

        public ItemHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            totalPrice = (TextView) itemView.findViewById(R.id.price_total);
            monthPrice = (TextView) itemView.findViewById(R.id.price_per_month);
            tag = (TextView) itemView.findViewById(R.id.tag);
            copy = (TextView) itemView.findViewById(R.id.copy);
            purchase = (Button) itemView.findViewById(R.id.purchase);
        }
    }
}
