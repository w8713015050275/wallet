package com.letv.walletbiz.base.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.activity.CouponListActivity;
import com.letv.walletbiz.main.bean.WalletTopListBean;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.xmain;

/**
 * Created by changjiajie on 16-8-30.
 */
public class MainTopButton extends LinearLayout implements View.OnClickListener {
    private Button button;
    private TextView textView;

    /**
     * 定义控件的样式
     */
    private int type;

    /**
     * 定义button与textView之间的距离
     */
    private int distance;

    /**
     * 定义button初始的背景
     */
    private int defaultDrawable;

    /**
     * 定义数据更改时替换button的背景
     */
    private int nextDrawable;

    /**
     * 定义textView的字体大小
     */
    private float textSize;

    /**
     * 定义textView的字体颜色
     */
    private int textColor;

    /**
     * 定义textView的文本提示
     */
    private int text;

    /**
     * 定义button的字体颜色
     */
    private int buttonTextColor;

    /**
     * 定义button的字体大小
     */
    private float buttonTextSize;


    private static final String TOP_KEY_LELEHUA = "lelehua";
    private static final String TOP_KEY_CARD = "card";
    private static final String TOP_KEY_BANK = "bank";

    private WalletTopListBean.WalletTopBean bean;

    private Context context;

    private AccountInfo accountInfo;

    public MainTopButton(Context context) {
        this(context, null);
    }

    public MainTopButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTopButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainTopButton);
        type = a.getInteger(R.styleable.MainTopButton_type, 1);

        distance = a.getDimensionPixelSize(R.styleable.MainTopButton_distance, 1);

        defaultDrawable = a.getResourceId(R.styleable.MainTopButton_default_drawable, -1);
        nextDrawable = a.getResourceId(R.styleable.MainTopButton_next_drawable, -1);

        text = a.getResourceId(R.styleable.MainTopButton_text, -1);
        textSize = a.getDimension(R.styleable.MainTopButton_textSize, 12);
        textColor = a.getColor(R.styleable.MainTopButton_textColor, Color.BLUE);


        buttonTextSize = a.getDimension(R.styleable.MainTopButton_button_textSize, 12);
        buttonTextColor = a.getColor(R.styleable.MainTopButton_button_textColor, Color.BLUE);

        a.recycle();

        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        button = new Button(context);
        textView = new TextView(context);
        addView(button, new LinearLayout.LayoutParams(133, 133));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = distance;
        addView(textView, params);


        //button.setBackgroundResource(defaultDrawable);
        button.setTextColor(buttonTextColor);
        button.setTextSize(buttonTextSize);

        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        //textView.setText(text);
        this.setOnClickListener(this);

    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    public void setCardList(AccountInfo info) {
        this.accountInfo = info;
        if (accountInfo != null & accountInfo.cardList != null && accountInfo.cardList.length > 0) {
            switch (type) {
                case 1:
                    break;
                case 2:
                    //显示数量，并且把button的背景置为空
                    //button.setText(""+accountInfo.cardList.length);
                    //button.setBackground(null);
                    break;
                case 3:
                    //显示数量，并且更改button的背景为next_drawable
                    button.setText("" + accountInfo.cardList.length);
                    button.setBackground(null);
                    break;
            }
        }

    }

    public void setDefaultData(WalletTopListBean.WalletTopBean bean) {
        this.bean = bean;
        ImageOptions options = new ImageOptions.Builder().build();
        xmain.image().loadDrawable(bean.icon, options, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                button.setBackground(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
        textView.setText(bean.title);
    }


    /**
     * 拦截点击事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public void onClick(View view) {
        if (null == this.bean) {
            return;
        } else {
            if (this.bean.name.equals(TOP_KEY_LELEHUA)) {
                Action.uploadClick(Action.QUICK_ENTRY_LELEHUA_CLICK);
            } else if (this.bean.name.equals(TOP_KEY_CARD)) {
                Action.uploadExposeTab(Action.WALLET_HOME_COUPON);
                Intent intent = new Intent(context, CouponListActivity.class);
                intent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_ICON);
                context.startActivity(intent);
            } else if (this.bean.name.equals(TOP_KEY_BANK)) {
                Intent intent = new Intent("com.letv.wallet.cardlist");

                if (accountInfo != null) {
                    Action.uploadClick(Action.QUICK_ENTRY_BANKCARD_CLICK);
                    //intent.putExtra("LePayCardBinInfo", accountInfo.cardList);
                    //intent.putExtra("LePayCardBinInfo",accountInfo.card);

                }
                context.startActivity(intent);

            }
        }

    }
}
