package com.letv.walletbiz.mobile.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.beans.ProductBean;

import java.text.DecimalFormat;

/**
 * Created by linquan on 15-11-11.
 */
public class ProductsPanelAdapter extends RecyclerView.Adapter<ProductsPanelAdapter.ViewHolder> {
    private static int mFeeOrFlow;
    private ProductBean mData;
    private OnMobileProductItemClickListener mOnItemClickListener = null;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ProductsPanelAdapter(ProductBean initEntityData) {
        mData = initEntityData;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mobile_option_item_v, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ProductBean.product pItem = mData.product_list[position];
        String nameStr = pItem.getProductNameValue();
        int uStrId = mFeeOrFlow == MobileConstant.PRODUCT_TYPE.MOBILE_FLOW ?
                R.string.label_mobile_product_unit_flow : R.string.label_mobile_product_unit_fee;
        if (mFeeOrFlow == MobileConstant.PRODUCT_TYPE.MOBILE_FLOW) {
            float nameValue = Integer.valueOf(nameStr);
            if (nameValue >= MobileConstant.NUMBER.MTG) {
                nameStr = String.valueOf(new DecimalFormat("#.##").format(nameValue / MobileConstant.NUMBER.MTG));
                uStrId = R.string.label_mobile_product_unit_g_flow;
            }
        }
        TextView vName = (TextView) holder.mView.findViewById(R.id.tv_product_name);
        vName.setText(nameStr);

        TextView vLabel = (TextView) holder.mView.findViewById(R.id.tv_price_label);
        vLabel.setText(pItem.getProductPrice());

        TextView vUnit = (TextView) holder.mView.findViewById(R.id.tv_product_unit);
        vUnit.setText(uStrId);

        boolean iEnabled = !mData.isStub();
        vName.setEnabled(iEnabled);
        vUnit.setEnabled(iEnabled);
        View vPriceBar = holder.mView.findViewById(R.id.ids_price_bar);
        vPriceBar.setVisibility(iEnabled ? View.VISIBLE : View.INVISIBLE);
        holder.mView.setEnabled(iEnabled);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mData == null)
            return 0;
        return mData.product_list.length;
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
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            v.setOnClickListener(this);
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
