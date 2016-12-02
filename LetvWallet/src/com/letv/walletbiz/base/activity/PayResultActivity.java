package com.letv.walletbiz.base.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.pay.Product;

import java.io.Serializable;

/**
 * Created by linquan on 15-12-9.
 */
public class PayResultActivity extends BaseWalletFragmentActivity {

    private static final String TAG = "PayResultActivity";

    protected Product mProduct;
    protected PayResultAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        mProduct = (Product) bundle.getSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT);
        int themeId = bundle.getInt(ActivityConstant.PAY_PARAM.PAY_PRODUCT_THEME);
        if (themeId > 0) {
            setTheme(themeId);
        }
        if (mProduct == null) {
            finish();
        }
        fillData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    private void fillData() {
        if (mProduct != null) {
            mAdapter = mProduct.getPayResultAdapter();
            setContentView(R.layout.pay_result_main);

            ViewGroup wrapper = (ViewGroup) findViewById(R.id.id_content);
            View content = mAdapter.createContentView(this, wrapper);
            wrapper.addView(content);

            int title = mAdapter.getTitle();
            if (title != 0) {
                setTitle(getString(title));
            }

            int status = mAdapter.getStatus();
            ImageView statusImg = (ImageView) findViewById(R.id.img_status);
            statusImg.setImageResource(status == 1 ? R.drawable.ic_wallet_mobile_pay_success
                    : R.drawable.ic_wallet_mobile_pay_pending);
            TextView statusDesc = (TextView) findViewById(R.id.tv_status);
            statusDesc.setText(status == 1 ? R.string.label_pay_paid : R.string.pay_status_unpaid);

            TextView tvPrice = (TextView) findViewById(R.id.tv_cost);
            tvPrice.setText(mAdapter.getCost()+getString(R.string.label_price_unit));
            TextView tvPriceLabel = (TextView) findViewById(R.id.tv_label_cost);
            tvPriceLabel.setText(R.string.movie_ticket_order_price);

            TextView btnAction = (TextView) findViewById(R.id.ibtn_action);
            btnAction.setText(mAdapter.getActionLabel());
            btnAction.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAdapter.onAction(PayResultActivity.this);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        mAdapter.onBack(PayResultActivity.this);
        super.onBackPressed();
    }

    public interface PayResultAdapter extends Serializable {
        int getTitle();

        int getStatus();

        String getCost();

        View createContentView(Context context, ViewGroup parent);

        int getActionLabel();

        void onAction(Context context);

        void onBack(Activity activity);
    }
}
