package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeDatePickerDialog;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.recommend.bean.BaseCardBean;
import com.letv.walletbiz.main.recommend.bean.CardEBean;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 2017/2/6.
 */

public class RecommendCardEView extends LinearLayout implements BaseCardView {

    private List<CardEBean> mCardList;

    public RecommendCardEView(Context context) {
        this(context, null);
    }

    public RecommendCardEView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecommendCardEView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        int padding = (int) DensityUtils.dip2px(10);
        int paddingLeft = (int) DensityUtils.dip2px(16);
        setPadding(paddingLeft, padding, paddingLeft, padding);
        setBackgroundResource(R.drawable.main_recommend_edit_bg);

    }

    @Override
    public boolean checkContent() {
        View child;
        CardEBean bean;
        for (int i=0; i<getChildCount(); i++) {
            child = getChildAt(i);
            if (child instanceof EditText) {
                bean = (CardEBean) child.getTag();
                if (bean == null) {
                    continue;
                }
                if (!bean.matcher(((EditText) child).getText().toString())) {
                    Toast.makeText(getContext(), R.string.main_recommend_input_error, Toast.LENGTH_SHORT).show();
                    child.requestFocus();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public HashMap<String, String> getContentParam() {
        if (getChildCount() <= 0) {
            return null;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        View child;
        for (int i=0; i<getChildCount(); i++) {
            child = getChildAt(i);
            if (child instanceof EditText) {
                CardEBean bean = (CardEBean) child.getTag();
                if (bean == null) {
                    continue;
                }
                map.put(bean.i_name, ((EditText) child).getText().toString());
            } else if (child instanceof TextView) {
                CardEBean bean = (CardEBean) child.getTag();
                if (bean != null) {
                    map.put(bean.i_name, ((TextView) child).getText().toString());
                }
            }
        }
        return map;
    }

    @Override
    public void bindView(List<BaseCardBean> cardList) {
        if (cardList == null || cardList.size() <= 0) {
            return;
        }
        if (cardList.equals(mCardList) || !(cardList.get(0) instanceof CardEBean)) {
            return;
        }
        mCardList = Arrays.asList(cardList.toArray(new CardEBean[]{}));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i< mCardList.size(); i++) {
            final CardEBean bean = mCardList.get(i);
            if (bean.i_type == CardEBean.I_TYPE_TEXT
                    || bean.i_type == CardEBean.I_TYPE_NUMBER || bean.i_type == CardEBean.I_TYPE_CITY) {
                EditText editText = new EditText(getContext());
                editText.setTextAppearance(R.style.Recommend_Card_EditText);
                editText.setBackground(null);
                editText.setHint(bean.i_hint);
                editText.setTag(bean);
                if (bean.i_type == CardEBean.I_TYPE_NUMBER) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                addView(editText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            } else if (bean.i_type == CardEBean.I_TYPE_DATE) {
                final TextView textView = new TextView(getContext());
                textView.setText(DateUtils.getDayStr(System.currentTimeMillis()));
                textView.setTextAppearance(R.style.Recommend_Card_Title);
                int pading = (int) DensityUtils.dip2px(10);
                textView.setPadding(0, pading, 0, pading);
                textView.setGravity(Gravity.CENTER);
                textView.setTag(bean);
                final CardDateSetListener listener = new CardDateSetListener();
                listener.setView(textView);
                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Calendar calendar = Calendar.getInstance();
                        LeDatePickerDialog datePickerDialog = new LeDatePickerDialog(getContext(), listener,
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                    }
                });
                addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            }
            if (mCardList.size() > 1 && i != (mCardList.size() - 1)) {
                inflater.inflate(R.layout.divider_horizontal, this, true);
            }
        }
    }

    @Override
    public boolean needTopDivider() {
        return false;
    }

    @Override
    public boolean needBottomDivider() {
        return false;
    }

    static class CardDateSetListener implements LeDatePickerDialog.OnDateSetListener {

        private TextView mView;

        public void setView(TextView view) {
            mView = view;
        }

        @Override
        public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            if (mView != null) {
                mView.setText(DateUtils.getDayStr(calendar.getTimeInMillis()));
            }
        }
    }
}
