package com.letv.walletbiz.mobile.ui;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.ProductBean;

import java.text.DecimalFormat;

/**
 * Created by linquan on 15-11-11.
 */
public class ProductsPanelAdapter extends RecyclerView.Adapter<ProductsPanelAdapter.ViewHolder> {
    private int mFeeOrFlow;
    private static int POSITION_OFFSET = 1;
    public static int VIEW_NORMALTYPE = 0;
    public static int VIEW_MORETYPE = 1;
    private String mMoreContent;
    private ProductBean mData;
    private OnMobileProductItemClickListener mOnItemClickListener = null;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ProductsPanelAdapter(ProductBean initEntityData) {
        this(initEntityData, null);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ProductsPanelAdapter(ProductBean initEntityData, String moreContent) {
        mData = initEntityData;
        mMoreContent = moreContent;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mobile_option_item_v, parent, false);
        v.setTag(viewType);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder.isMoreView()) {
            holder.setMoreContent(mMoreContent);
            return;
        }
        ProductBean.product pItem = mData.product_list[position];
        holder.setData(pItem);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mData == null) {
            if (hasLastPosition()) {
                return POSITION_OFFSET;
            }
            return 0;
        }
        return hasLastPosition() ? (mData.product_list.length + POSITION_OFFSET) : mData.product_list.length;
    }

    @Override
    public int getItemViewType(int position) {
        if ((position + POSITION_OFFSET) == getItemCount() && hasLastPosition()) {
            return VIEW_MORETYPE;
        }
        return VIEW_NORMALTYPE;
    }

    public boolean hasLastPosition() {
        if (!TextUtils.isEmpty(mMoreContent)) {
            return true;
        }
        return false;
    }

    public void setType(int type) {
        mFeeOrFlow = type;
    }

    public void setData(ProductBean updateData) {
        mData = updateData;
    }

    public void setOnItemClickListener(OnMobileProductItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnMobileProductItemClickListener {
        void onItemClick(View view, int position);
    }

    // Provide a reference to the type of views that you are using
    // (custom viewholder)
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View mView;
        private LinearLayout mNormalLl;
        private LinearLayout mMoreLl;
        private TextView mName;
        private TextView mLabel;
        private TextView mUnit;
        private TextView mMoreTv;
        private View mPriceBar;


        public ViewHolder(View v) {
            super(v);
            mView = v;
            v.setOnClickListener(this);
            initV();
        }

        private void initV() {
            mNormalLl = (LinearLayout) mView.findViewById(R.id.normal_ll);
            mMoreLl = (LinearLayout) mView.findViewById(R.id.more_ll);
            mName = (TextView) mView.findViewById(R.id.tv_product_name);
            mLabel = (TextView) mView.findViewById(R.id.tv_price_label);
            mUnit = (TextView) mView.findViewById(R.id.tv_product_unit);
            mPriceBar = mView.findViewById(R.id.ids_price_bar);
            mMoreTv = (TextView) mView.findViewById(R.id.more_tv);
            if (isMoreView()) {
                mNormalLl.setVisibility(View.GONE);
                mMoreLl.setVisibility(View.VISIBLE);
            } else {
                mMoreLl.setVisibility(View.GONE);
                mNormalLl.setVisibility(View.VISIBLE);
            }
        }

        public boolean isMoreView() {
            Object tag = mView.getTag();
            int type = 0;
            if (tag != null) {
                try {
                    type = (int) tag;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (type == VIEW_MORETYPE) {
                return true;
            }
            return false;
        }

        public void setMoreContent(String content) {
            if (mMoreTv != null && !TextUtils.isEmpty(content)) {
                mMoreTv.setText(content);
            }
        }

        public void setData(ProductBean.product pItem) {
            float content = pItem.getContent();
            String nameStr = String.valueOf(content);
            int uStrId = mFeeOrFlow == MobileConstant.PRODUCT_TYPE.MOBILE_FLOW ?
                    R.string.label_mobile_product_unit_flow : R.string.label_mobile_product_unit_fee;
            if (mFeeOrFlow == MobileConstant.PRODUCT_TYPE.MOBILE_FLOW) {
                if (content >= MobileConstant.NUMBER.MTG) {
                    nameStr = String.valueOf(new DecimalFormat("#.##").format(content / MobileConstant.NUMBER.MTG));
                    uStrId = R.string.label_mobile_product_unit_g_flow;
                }
            }
            mName.setText(nameStr);
            mLabel.setText(pItem.getProductPrice());
            mUnit.setText(uStrId);
            boolean iEnabled = !mData.isStub();
            mName.setEnabled(iEnabled);
            mUnit.setEnabled(iEnabled);
            mPriceBar.setVisibility(iEnabled ? View.VISIBLE : View.GONE);
            mView.setEnabled(iEnabled);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                mOnItemClickListener.onItemClick(v, getPosition()); // getLayoutPosition());
            }
        }
    }

}
