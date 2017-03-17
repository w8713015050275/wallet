package com.letv.walletbiz.base.widget;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.activity.CouponListActivity;
import com.letv.walletbiz.main.bean.WalletTopListBean;
import com.letv.walletbiz.me.activity.AccountWebActivity;
import com.letv.walletbiz.me.utils.AccountUtils;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.xmain;

import java.io.IOException;

/**
 * Created by changjiajie on 16-8-30.
 */
public class MainTopButton extends RelativeLayout implements View.OnClickListener {

    /**
     * 底部textview
     */
    private TextView textView;

    /**
     * 上边按钮位
     */
    private ImageView imageView;

    /**
     * 数字栏位
     */
    private TextView numberView;

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


    public static final String TOP_KEY_LELEHUA = "lelehua";
    public static final String TOP_KEY_CARD = "card";
    public static final String TOP_KEY_BANK = "bank";

    private WalletTopListBean.WalletTopBean bean;

    private AccountInfo accountInfo;


    private Context context;

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
        textView = new TextView(context);
        textView.setId(generateViewId());
        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        bottomParams.topMargin = distance;
        bottomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        textView.setTextColor(context.getColor(R.color.main_top_textview_color));
        addView(textView, bottomParams);


        imageView = new ImageView(context);
        imageView.setBackground(null);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(133, 133);
        imageParams.addRule(RelativeLayout.ABOVE, textView.getId());
        imageParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        addView(imageView, imageParams);


        numberView = new TextView(context);
        RelativeLayout.LayoutParams numberParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        numberParams.addRule(RelativeLayout.ABOVE, textView.getId());
        numberParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        //初始化的时候隐藏此控件
        numberView.setVisibility(View.GONE);
        numberView.setGravity(Gravity.CENTER);
        numberView.setTextColor(context.getColor(R.color.main_top_button_color));
        addView(numberView, numberParams);

        numberView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        numberView.setTypeface(Typeface.DEFAULT_BOLD);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(Typeface.create("Roboto-Bold", Typeface.NORMAL));
        this.setOnClickListener(this);

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
                if (null != accountInfo && null != accountInfo.lelehua) {
                    if (AccountHelper.getInstance().loginLetvAccountIfNot((MainActivity) context, null)) {
                        String jType = AccountUtils.getLeLeHuaJtype(AccountUtils.LELEHUA_HOME, accountInfo.lelehua.active_status);
                        Intent intent = new Intent(context, AccountWebActivity.class);
                        if (!TextUtils.isEmpty(jType)) {
                            intent.putExtra(AccountWebActivity.EXTRA_KEY_JTYPE, jType);
                        }
                        context.startActivity(intent);
                    }
                }
                Action.uploadClick(Action.QUICK_ENTRY_LELEHUA_CLICK);
            } else if (this.bean.name.equals(TOP_KEY_CARD)) {
                Action.uploadExposeTab(Action.WALLET_HOME_COUPON);
                Intent intent = new Intent(context, CouponListActivity.class);
                intent.putExtra(WalletConstant.EXTRA_FROM, Action.EVENT_PROP_FROM_ICON);
                context.startActivity(intent);
            } else if (this.bean.name.equals(TOP_KEY_BANK)) {
                if (AccountHelper.getInstance().isLogin(context)) {
                    Action.uploadClick(Action.QUICK_ENTRY_BANKCARD_CLICK);
                    Intent intent = new Intent("com.letv.wallet.cardlist");
                    if (accountInfo != null && accountInfo.cardList != null) {
                        intent.putExtra("LePayCardBinInfo", accountInfo.cardList);
                    }
                    context.startActivity(intent);
                } else {
                    final Intent intent = new Intent("com.letv.wallet.cardlist");
                    AccountHelper.getInstance().loginLetvAccountIfNot((MainActivity) context, new AccountManagerCallback() {

                        @Override
                        public void run(AccountManagerFuture future) {
                            try {
                                if (context != null && future.getResult() != null && AccountHelper.getInstance().isLogin(context)) {
                                    context.startActivity(intent);
                                }
                            } catch (OperationCanceledException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (AuthenticatorException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        }

    }

    public void setData(WalletTopListBean.WalletTopBean currentBean) {
        bean = currentBean;
        setButtonType(currentBean);
        if (!TextUtils.isEmpty(currentBean.icon) && TextUtils.isDigitsOnly(currentBean.icon)) {
            //如果是假数据，资源来自本地
            imageView.setBackground(context.getDrawable(Integer.parseInt(currentBean.icon)));
        } else {
            ImageOptions options = new ImageOptions.Builder().build();
            xmain.image().loadDrawable(currentBean.icon, options, new Callback.CommonCallback<Drawable>() {
                @Override
                public void onSuccess(Drawable result) {
                    if (accountInfo != null && accountInfo.cardList != null && accountInfo.cardList.length > 0) {
                        //此时可能已经获取到银行卡的数量，不需要加载默认图片
                    } else {
                        imageView.setBackground(result);
                    }
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
        }

        textView.setText(currentBean.title);
    }

    private void setButtonType(WalletTopListBean.WalletTopBean currentBean) {
        if (null == currentBean) {
            return;
        } else {
            if (currentBean.name.equals(TOP_KEY_LELEHUA)) {
                type = 0;
            } else if (currentBean.name.equals(TOP_KEY_CARD)) {
                type = 1;
            } else if (currentBean.name.equals(TOP_KEY_BANK)) {
                type = 2;
            }
        }
    }

    public void setNumber(Object info) {
        switch (type) {
            case 0:
                accountInfo = (AccountInfo) info;
                if (accountInfo != null && accountInfo.lelehua != null) {
                    if (accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED || accountInfo.lelehua.active_status == AccountConstant.LELEHUA_ACCOUNT_STATE_ACTIVATED_FROZEN) {
                        if (TextUtils.isEmpty(accountInfo.lelehua.available_limit)) {
                            numberView.setText(String.format(context.getString(R.string.main_top_lelehua_amount), "0"));
                        } else {
                            numberView.setText(String.format(context.getString(R.string.main_top_lelehua_amount), accountInfo.lelehua.available_limit));
                        }
                        numberView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                    }
                }
                break;
            case 1:
                //卡全包不做更改
                break;
            case 2:
                //强制转化，不知道传递的会是什么值
                accountInfo = (AccountInfo) info;
                if (accountInfo != null && accountInfo.cardList != null && accountInfo.cardList.length > 0) {
                    numberView.setText("" + accountInfo.cardList.length);
                    numberView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }

    }

    public void resetDrawable() {
        imageView.setVisibility(View.VISIBLE);
        numberView.setVisibility(View.GONE);
        numberView.setText("");
    }

}
