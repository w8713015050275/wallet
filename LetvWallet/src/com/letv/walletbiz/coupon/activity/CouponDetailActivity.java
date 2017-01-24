package com.letv.walletbiz.coupon.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.ParseHelper;
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
            Uri uri = intent.getData();
            if (uri != null) {
                mCoupon = parseUri(uri);
            } else {
                mCoupon = (BaseCoupon) intent.getSerializableExtra(CouponConstant.EXTRA_COUPON_BEAN);
            }
            if (mCoupon == null) {
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

    /**
     * letvwallet://coupon?source_merchant_id=0&source_merchant_name=&source=1&coupon_from=1&coupon_id=40
     * &coupon_distribute_id=182&ucoupon_id=30000050&rank_id=30000050
     * &ucoupon_code=Ydm2Kdd7n4YcZ5Yb4vx&type=1&title=lxt测试1&service_name=优惠券类型-测试
     * &use_condition=优惠券使用条件&valid_date_desc=2017-02-21&use_detail_link=使用详细说明
     * &icon=http://static.scloud.letv.com/res/3b99c8f8-3870-46a3-b917-123f1c0e98ff.png&jump_type=1&jump_param=abcdef&package_name=abcef
     * &jump_link=&start_time=1487001600&end_time=1487606400&state=1
     * &showItems=[{"key":"price","name":"金额","value":"0.01元","rank":5},{"key":"code","name":"券码","value":"Ydm2Kdd7n4YcZ5Yb4vx","rank":4},{"key":"validate_date","name":"有效期","value":"2017-02-21","rank":3}]
     * &goods_category_ids[0]=2
     * @param uri
     * @return
     */
    private BaseCoupon parseUri(Uri uri) {
        BaseCoupon coupon = new BaseCoupon();
        try {
            coupon.ucoupon_id = Long.parseLong(uri.getQueryParameter("ucoupon_id"));
        } catch (Exception e) {
        }
        coupon.ucoupon_code = uri.getQueryParameter("ucoupon_code");
        try {
            coupon.rank_id = Long.parseLong(uri.getQueryParameter("rank_id"));
        } catch (Exception e) {
        }
        try {
            coupon.type = Integer.parseInt(uri.getQueryParameter("type"));
        } catch (Exception e) {
        }
        coupon.title = uri.getQueryParameter("title");
        coupon.service_name = uri.getQueryParameter("service_name");
        coupon.use_condition = uri.getQueryParameter("use_condition");
        coupon.use_detail_link = uri.getQueryParameter("use_detail_link");
        coupon.icon = uri.getQueryParameter("icon");
        try {
            coupon.jump_type = Integer.parseInt(uri.getQueryParameter("jump_type"));
        } catch (Exception e) {
        }
        coupon.jump_param = uri.getQueryParameter("jump_param");
        coupon.package_name = uri.getQueryParameter("package_name");
        coupon.jump_link = uri.getQueryParameter("jump_link");
        try {
            coupon.start_time = Long.parseLong(uri.getQueryParameter("start_time"));
        } catch (Exception e) {
        }
        try {
            coupon.end_time = Long.parseLong(uri.getQueryParameter("end_time"));
        } catch (Exception e) {
        }
        coupon.valid_date_desc = uri.getQueryParameter("valid_date_desc");
        try {
            coupon.state = Integer.parseInt(uri.getQueryParameter("state"));
        } catch (Exception e) {
        }
        String showItemJson = uri.getQueryParameter("showItems");
        if (!TextUtils.isEmpty(showItemJson)) {
            try {
                TypeToken<BaseCoupon.CouponItem> typeToken = new TypeToken<BaseCoupon.CouponItem>(){};
                List<BaseCoupon.CouponItem> itemList = ParseHelper.parseArrayByGson(showItemJson, typeToken.getType());
                if (itemList != null) {
                    coupon.showItems = itemList.toArray(new BaseCoupon.CouponItem[0]);
                }
            } catch (Exception e) {

            }
        }

        return coupon;
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
