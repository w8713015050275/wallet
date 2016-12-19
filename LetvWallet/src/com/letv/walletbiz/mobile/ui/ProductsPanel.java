package com.letv.walletbiz.mobile.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.letv.wallet.common.view.GridDividerItemDecoration;
import com.letv.wallet.common.view.SpacesItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.mobile.beans.ProductBean;

/**
 * Created by linquan on 15-11-11.
 */
public class ProductsPanel extends RecyclerView {
    private static final String TAG = "ProductsPanel";
    private static final int LAYOUT_COLUMES = 3;

    private static int mFeeOrFlow;
    private static String mMobileNumber;
    private final Context mContext;
    private ProductBean mData;

    private ProgressDialog dialog;

    public ProductsPanel(Context context) {
        this(context, null);

    }

    public ProductsPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setFocusable(false);
        mContext = context;
        GridLayoutManager layoutManager = new GridLayoutManager(context, LAYOUT_COLUMES);
        setLayoutManager(layoutManager);
        addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelOffset(R.dimen.mobile_products_panel_space)));
    }


    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public ProductBean getData() {
        return mData;
    }

    public void setData(ProductBean data) {
        mData = data;
    }

    public int getItemActionId(int i) {
        return mData.product_list[i].getProductId();
    }

    public ProductBean.product getProductItem(int i) {
        return mData.product_list[i];
    }

    public void setType(int type) {
        mFeeOrFlow = type;
        ProductsPanelAdapter adapter = (ProductsPanelAdapter) getAdapter();

        adapter.setType(type);
    }

    public void update() {
        ProductsPanelAdapter adapter = (ProductsPanelAdapter) getAdapter();
        adapter.setData(mData);
        adapter.notifyDataSetChanged();
    }


}
