package com.letv.walletbiz.base.widget;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.activity.CouponListActivity;
import com.letv.walletbiz.main.bean.WalletTopListBean;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.xmain;

import java.io.IOException;

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
        setOrientation(LinearLayout.VERTICAL);
        button = new Button(context);
        textView = new TextView(context);
        button.setBackground(null);
        addView(button, new LinearLayout.LayoutParams(133, 143));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = distance;
        addView(textView, params);


        //button.setTextColor(buttonTextColor);
        button.setTextSize(buttonTextSize);

        //textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        this.setOnClickListener(this);

    }

//    public static int px2dip(Context context, float pxValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (pxValue / scale + 0.5f);
//    }
//
//    public static int dip2px(Context context, float dipValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dipValue * scale + 0.5f);
//    }
//
//    public static int px2sp(Context context, float pxValue) {
//        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
//        return (int) (pxValue / fontScale + 0.5f);
//    }

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
                if (AccountHelper.getInstance().isLogin(context)) {
                    Action.uploadClick(Action.QUICK_ENTRY_BANKCARD_CLICK);
                    Intent intent = new Intent("com.letv.wallet.cardlist");
                    if (accountInfo != null && accountInfo.cardList != null) {
                        intent.putExtra("LePayCardBinInfo", accountInfo.cardList);
                    }
                    context.startActivity(intent);
                }else {
                    final Intent intent = new Intent("com.letv.wallet.cardlist");
                    AccountHelper.getInstance().loginLetvAccountIfNot((MainActivity)context, new AccountManagerCallback() {

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

    private Drawable dataDrawable;

    public void setData(WalletTopListBean.WalletTopBean currentBean) {
        bean = currentBean;
        setButtonType(currentBean);
        if (!TextUtils.isEmpty(currentBean.icon) && TextUtils.isDigitsOnly(currentBean.icon)) {
            //如果是假数据，资源来自本地
            dataDrawable = context.getDrawable(Integer.parseInt(currentBean.icon));
            button.setBackground(dataDrawable);
        } else {
            ImageOptions options = new ImageOptions.Builder().build();
            xmain.image().loadDrawable(currentBean.icon, options, new Callback.CommonCallback<Drawable>() {
                @Override
                public void onSuccess(Drawable result) {
                    if (accountInfo != null && accountInfo.cardList != null && accountInfo.cardList.length > 0) {
                        //此时可能已经获取到银行卡的数量，不需要加载默认图片
                    }else{
                        dataDrawable = result;
                        button.setBackground(result);
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
                //乐乐花暂时不做
                break;
            case 1:
                //卡全包不做更改
                break;
            case 2:
                //强制转化，不知道传递的会是什么值
                accountInfo = (AccountInfo) info;
                if (accountInfo != null && accountInfo.cardList != null && accountInfo.cardList.length > 0) {
                    button.setText("" + accountInfo.cardList.length);
                    button.setBackground(null);
                }
//                else {
//
//                    if (accountInfo.cardList == null) {
//                        accountInfo.cardList = new AccountInfo.CardBin[1];
//                        AccountInfo.CardBin bin = new AccountInfo.CardBin();
//                        accountInfo.cardList[0] = bin;
//                    }
//
//                }
                break;
            default:
                break;
        }

    }

    public void resetDrawable() {
        if (null != dataDrawable) {
            button.setBackground(dataDrawable);
        } else {
            button.setBackground(null);
        }
        button.setText("");
    }

    public void setTextViewColor(int color) {
        textView.setTextColor(color);
    }

    public void setButtonTextColor(int color) {
        button.setTextColor(color);
    }
}
