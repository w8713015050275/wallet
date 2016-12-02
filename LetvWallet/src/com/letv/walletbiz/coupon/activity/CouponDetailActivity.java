package com.letv.walletbiz.coupon.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.widget.LabeledTextView;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.coupon.beans.BaseCoupon;
import com.letv.walletbiz.coupon.utils.CouponUtils;
import com.letv.walletbiz.main.WalletMainWebActivity;

import org.xutils.xmain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by linquan on 16-4-19.
 */
public class CouponDetailActivity extends BaseWalletFragmentActivity implements View.OnClickListener {
    private static final String TAG = "CouponDetailActivity";
    private BaseCoupon mCoupon;

    private ImageView mViewCouponIcon;
    private TextView mViewCouponeName;
    private TextView mViewCouponType;
    private LinearLayout mViewInfo;
    private LinearLayout mViewDetail;
    private LabeledTextView mViewUseDetail;
    private TextView tv_usecondition;

    private View mUseCouponView;
    private TextView mUseCouponButton;
    private static final String KEY_CODE = "code";
    private LeBottomSheet mCopyCodeDialog;
    private ClipboardManager mClipboard;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mCoupon = (BaseCoupon) intent.getSerializableExtra(CouponConstant.EXTRA_COUPON_BEAN);
            if (mCoupon == null) {
                //Todo remove :For test only
//                mCoupon = stub();
                LogHelper.d("[%s] Input CouponObj is null", TAG);
                finish();
                return;
            }
        }
        setContentView(R.layout.coupon_detail_layout);
        initView();
        loadData(mCoupon);
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    private void initView() {
        LogHelper.d("[%s] init View ", TAG);
        mViewCouponeName = (TextView) findViewById(R.id.tv_coupon_name);
        mViewCouponType = (TextView) findViewById(R.id.tv_coupon_type);

        mViewInfo = (LinearLayout) findViewById(R.id.v_info);
        tv_usecondition = (TextView) findViewById(R.id.tv_usecondition);
        mViewUseDetail = (LabeledTextView) findViewById(R.id.ltv_usedetail);
        mViewCouponIcon = (ImageView) findViewById(R.id.img_icon);

        mUseCouponView = findViewById(R.id.view_use_coupon);
        mUseCouponButton = (TextView) findViewById(R.id.btn_use_coupon);
        mViewDetail = (LinearLayout) findViewById(R.id.v_detail);

    }

    private void loadData(BaseCoupon coupon) {

        xmain.image().bind(mViewCouponIcon, coupon.getIcon());
        mViewCouponeName.setText(coupon.getTitle());
        mViewCouponType.setText(coupon.getService_name());

        tv_usecondition.setText(coupon.getUse_condition());

        String link = mCoupon.getUse_detail_link();

        if (link == null || TextUtils.isEmpty(link)) {
            mViewDetail.setVisibility(View.GONE);
        } else {
            mViewUseDetail.setIconShow();
            mViewUseDetail.setOnClickListener(this);
        }

        List<BaseCoupon.CouponItem> descItems = getDescList(coupon.showItems);
        if (descItems != null) {
            Collections.sort(descItems, new CouponComparator());

            int n = descItems.size();

            for (int i = 0; i < n; i++) {
                LabeledTextView aView = new LabeledTextView(this);
                if (aView != null && descItems.get(i) != null) {
                    aView.setTextTitle(descItems.get(i).getName());
                    aView.setTextSummery(descItems.get(i).getValue());
                    if (KEY_CODE.equalsIgnoreCase(descItems.get(i).key) && aView.getSummeryView() != null && coupon.state == BaseCoupon.STATE_UNUSE) {
                        code = descItems.get(i).getValue();
                        aView.getSummeryView().setOnLongClickListener(longClickListener);
                    }
                    mViewInfo.addView(aView, 0);
                }
            }
        }

        if (coupon.state == BaseCoupon.STATE_UNUSE) {
            mUseCouponView.setVisibility(View.VISIBLE);
        } else {
            mUseCouponView.setVisibility(View.GONE);
        }
    }

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            showCopyCodeDialog(CouponDetailActivity.this, code);
            return true;
        }
    };

    private void showCopyCodeDialog(Context context, final String copyText) {
        if (context == null) {
            return;
        }
        if (mCopyCodeDialog == null) {
            mCopyCodeDialog = new LeBottomSheet(this);
            mCopyCodeDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCopyCodeDialog.dismiss();
                            copyToClipboard(copyText);
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCopyCodeDialog.dismiss();
                        }
                    }, null,
                    new String[]{
                            context.getString(R.string.coupon_copy_code), context.getString(R.string.coupon_copy_code_cancel)
                    },
                    null,
                    null, null, context.getResources().getColor(R.color.colorBtnBlue), false);
        }
        if (!mCopyCodeDialog.isShowing()) {
            mCopyCodeDialog.show();
        }
    }

    private void copyToClipboard(final String str) {
        if (TextUtils.isEmpty(str)) return;
        if (null == mClipboard) {
            mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        }
        mClipboard.setPrimaryClip(ClipData.newPlainText(null, str));
        CouponUtils.showToast(getApplicationContext(), R.string.coupon_copy_code_notify);
    }


    private List<BaseCoupon.CouponItem> getDescList(BaseCoupon.CouponItem[] inItems) {
        if (inItems == null || inItems.length <= 0) return null;

        List<BaseCoupon.CouponItem> items = new ArrayList<BaseCoupon.CouponItem>();
        for (int i = 0; i < inItems.length; i++) {
            items.add(inItems[i]);
        }
        return items;
    }

    @Override
    public void onClick(View view) {
        if (view == mViewUseDetail) {
            goToUseDetail();
        }
    }

    public void goToUseDetail() {
        Intent intent;
        intent = new Intent(CouponDetailActivity.this, CouponUseDetailActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, mCoupon.getUse_detail_link());
        startActivity(intent);
    }

    public void useCoupon(View view) {
        LogHelper.d("[%s] useCoupon clicked ", TAG);
        Bundle bundle = new Bundle();
        bundle.putLong(CouponConstant.EXTRA_COUPON_BEAN_ID, mCoupon.ucoupon_id);

        if (mCoupon != null) {
            copyToClipboard(code);
            if (mCoupon.jump_type == BaseCoupon.JUMP_TYPE_APP) {
                AppUtils.LaunchAppWithBundle(this, mCoupon.package_name, mCoupon.jump_param, bundle);
            } else if (mCoupon.jump_type == BaseCoupon.JUMP_TYPE_WEB) {
                AppUtils.LaunchUrlWithBundle(this, mCoupon.jump_link, mCoupon.service_name, WalletMainWebActivity.class, bundle);
            }
        }
    }

    private class CouponComparator implements Comparator<BaseCoupon.CouponItem> {
        @Override
        public int compare(BaseCoupon.CouponItem item1, BaseCoupon.CouponItem item2) {
            int rank1 = item1.getRank();
            int rank2 = item2.getRank();

            if (rank1 < rank2) {
                return 1;
            } else if (rank1 > rank2) {
                return -1;
            }
            return 0;
        }
    }
}
