package com.letv.wallet.account.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.CardbinAvailableInfo;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.CardbinTask;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;

/**
 * Created by lijunying on 17-2-9.
 */

public class CardEditText extends EditTextWithCustomError {

    public static final int MINLENGTH = 16;
    public static final int MAXLENGTH = 19;
    public static final int SEPARATENUM = 4;
    public static final String SEPARATOR = " ";

    private boolean isCardValidate = false;

    private final String CARD_INVALID_MSG;

    private TextWatcher mTextWatcher = new TextWatcher() {
        String oldtext;
        int selectedIndex;
        int addOrDel;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            oldtext = s.toString();
            selectedIndex = start;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().length() > oldtext.length()) {
                addOrDel = 1;
            } else {
                addOrDel = 0;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            setError(null);
            removeTextChangedListener(mTextWatcher);
            String formateText = formatCardNum(s.toString());
            setText(formateText);
            if (formateText.length() != oldtext.length()) {
                selectedIndex += addOrDel;
            }
            if (formateText.length() != 0) {
                if (selectedIndex % 5 == 0 && addOrDel == 1) {
                    selectedIndex += 1;
                } else if (selectedIndex % 5 == 0 && addOrDel == 0) {
                    selectedIndex -= 1;
                }
                setSelection(selectedIndex >= 0 ? selectedIndex : 0);
            }
            addTextChangedListener(mTextWatcher);
            if (checkBankCard(getCardNum()) && mCallback != null) {
                mCallback.onNumberChanged(CardEditText.this);  //正则校验
            }
        }
    };

    public CardEditText(Context context) {
        this(context, null);
    }

    public CardEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        CARD_INVALID_MSG = getResources().getString(R.string.account_verify_card_num_invalid);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAXLENGTH + MAXLENGTH / SEPARATENUM)});
        this.setInputType(InputType.TYPE_CLASS_PHONE);
        this.setSingleLine(true);
        this.addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused && !isCardValidate) {
            setError(CARD_INVALID_MSG);
        }
    }

    public String getCardNum() {
        String cardNum = getText().toString();
        if (!TextUtils.isEmpty(cardNum)) {
            return cardNum.replace(" ", "");
        }
        return null;
    }

    public boolean isCardValidate() {
        return isCardValidate;
    }

    private String formatCardNum(String cardsrc) {
        if (TextUtils.isEmpty(cardsrc)) {
            return cardsrc;
        }
        String src = cardsrc.replace(" ", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < src.length(); i++) {
            if (i % 4 == 0 && i != 0) {
                sb.append(" ");
            }
            sb.append(src.charAt(i));
        }
        return sb.toString();
    }

    public boolean checkValidateWithError(){
        if (checkBankCard(getCardNum())) {
            return true;
        }
        setError(CARD_INVALID_MSG);
        return false;
    }

    public boolean checkBankCard(String cardId) {
        if (TextUtils.isEmpty(cardId) || !cardId.matches("\\d+")
                || cardId.length() < MINLENGTH
                || cardId.length() > MAXLENGTH) {
            isCardValidate = false;
        } else {
            isCardValidate = true;
        }
        LogHelper.e("checkBankCard = " + isCardValidate);
        return isCardValidate ;
    }

    private EditTextActionCallback mCallback;

    public void setCallback(EditTextActionCallback callback) {
        mCallback = callback;
    }


}
