package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.common.util.CommonCallback;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.recommend.RecommendFooterTask;
import com.letv.walletbiz.main.recommend.RecommendUtils;
import com.letv.walletbiz.main.recommend.bean.BaseCardBean;
import com.letv.walletbiz.main.recommend.bean.RecommendCardBean;
import com.letv.walletbiz.main.recommend.bean.RecommendCardBean.CardFooter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by liuliang on 17-1-18.
 */

public class RecommendCardView extends LinearLayout {

    private RecommendCardBean mCardBean;

    private BaseCardView mContentView;
    private LinearLayout mFooterContainer;

    private static final int MSG_FOOTER_LOAD_FINISHED = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FOOTER_LOAD_FINISHED:
                    mFooterTask = null;
                    if (msg.arg1 != CommonCallback.NO_ERROR) {
                        Toast.makeText(getContext(), R.string.main_recommend_error_network, Toast.LENGTH_SHORT).show();
                    } else {
                        List<CardFooter> list = (List<CardFooter>) msg.obj;
                        mCardBean.footer = (list == null ? null : list.toArray(new CardFooter[0]));
                        initFooterView();
                    }
                    break;
            }
        }
    };

    private RecommendFooterTask mFooterTask;
    private CommonCallback<List<CardFooter>> mFooterCallback = new CommonCallback<List<CardFooter>>() {

        @Override
        public void onLoadFinished(List<CardFooter> result, int errorCode) {
            Message msg = mHandler.obtainMessage(MSG_FOOTER_LOAD_FINISHED);
            msg.obj = result;
            msg.arg1 = errorCode;
        }
    };

    private OnClickListener mFooterClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mCardBean == null) {
                return;
            }
            Map<String, String> param = null;
            if (mContentView != null) {
                if (mContentView.checkContent()) {
                    param = mContentView.getContentParam();
                } else {
                    return;
                }
            }
            if (!TextUtils.isEmpty(mCardBean.footer_req_url) && !TextUtils.isEmpty(mCardBean.footer_req_url_method)) {
                if (!NetworkHelper.isNetworkAvailable()) {
                    Toast.makeText(getContext(), R.string.main_recommend_error_no_network, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mFooterTask == null) {
                    mFooterTask = new RecommendFooterTask(mFooterCallback, mCardBean.footer_req_url, param, mCardBean.footer_req_url_method);
                    ExecutorHelper.getExecutor().runnableExecutor(mFooterTask);
                }
                return;
            }
            CardFooter footer = (CardFooter) view.getTag();
            if (footer != null) {
                RecommendUtils.launchUrl(getContext(), footer.key_link);
            }
        }
    };

    public RecommendCardView(Context context) {
        this(context, null);
    }

    public RecommendCardView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecommendCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCardBean(RecommendCardBean cardBean) {
        if (mCardBean == cardBean) {
            return;
        }
        mCardBean = cardBean;
        initView();
    }

    private void init() {
        int padding = (int) DensityUtils.dip2px(10);
        setPadding(padding, padding, padding, padding);
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundResource(R.drawable.main_recommend_card_bg);
    }

    private void initView() {
        if (mCardBean == null) {
            return;
        }
        removeAllViews();
        mContentView = null;
        mFooterContainer = null;
        boolean hasHeader = initHeaderView();
        initCardContentView(hasHeader);
        initFooterView();
    }

    private boolean initHeaderView() {
        if (mCardBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(mCardBean.header_left_title) && TextUtils.isEmpty(mCardBean.header_right_title)) {
            return false;
        }
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setPadding(0, 0, 0, (int) DensityUtils.dip2px(10));
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView leftTextView = getTextView(mCardBean.header_left_title, mCardBean.header_left_link, R.style.Recommend_Card_Title);
        if (leftTextView != null) {
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.rightMargin = (int) DensityUtils.dip2px(10);
            params.weight = 1;
            header.addView(leftTextView, params);
        }

        TextView rightTextView = getTextView(mCardBean.header_right_title, mCardBean.header_right_link, R.style.Recommend_Card_SubTitle);
        if (rightTextView != null) {
            header.addView(rightTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        return true;
    }

    private TextView getTextView(String text, final String link, int textAppearance) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        TextView textView = new TextView(getContext());
        textView.setTextAppearance(textAppearance);
        textView.setText(text);
        if (!TextUtils.isEmpty(link)) {
            textView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    RecommendUtils.launchUrl(getContext(), link);
                }
            });
        }
        return textView;
    }

    private boolean initCardContentView(boolean hasHeader) {
        if (mCardBean == null) {
            return false;
        }
        List<BaseCardBean> cardList = mCardBean.getCardContent();
        View view = RecommendCardFactory.createCardView(getContext(), mCardBean.card_type);
        mContentView = (BaseCardView) view;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (view != null) {
            ((BaseCardView) view).bindView(cardList);
            if (hasHeader && inflater != null && ((BaseCardView) view).needTopDivider()) {
                inflater.inflate(R.layout.divider_horizontal, this, true);
            }
            addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return true;
        }
        return false;
    }

    private boolean initFooterView() {
        if (mCardBean == null) {
            return false;
        }
        Arrays.sort(mCardBean.footer);
        CardFooter[] footerArray = mCardBean.footer;
        if (footerArray == null || footerArray.length <= 0) {
            return false;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (mFooterContainer == null) {
            if (mContentView == null || (mContentView != null && mContentView.needBottomDivider())) {
                inflater.inflate(R.layout.divider_horizontal, this, true);
            }
            mFooterContainer = new LinearLayout(getContext());
            mFooterContainer.setOrientation(LinearLayout.HORIZONTAL);
            int padding = (int) DensityUtils.dip2px(10);
            mFooterContainer.setPadding(0, padding, 0, padding);
            addView(mFooterContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        } else {
            mFooterContainer.removeAllViews();
        }

        CardFooter footer;
        TextView button;
        for (int i=0; i<footerArray.length; i++) {
            footer = footerArray[i];
            button = new TextView(getContext());
            button.setTextAppearance(R.style.Recommend_Card_Footer);
            button.setText(footer.key_title);
            button.setGravity(Gravity.CENTER);
            button.setOnClickListener(mFooterClickListener);
            button.setTag(footer);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            mFooterContainer.addView(button, params);
            if (footerArray.length > 1 && i != (footerArray.length - 1)) {
                inflater.inflate(R.layout.divider_vertical, mFooterContainer, true);
            }
        }
        mFooterContainer.requestLayout();
        return true;
    }
}
