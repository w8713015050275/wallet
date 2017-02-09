package com.letv.wallet.account.ui;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.CardbinAvailableInfo;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.CardbinTask;
import com.letv.wallet.common.util.ExecutorHelper;

import java.util.regex.Pattern;

/**
 * Created by lijunying on 17-2-9.
 */

public class IdNoEditText extends EditTextWithCustomError {

    public static final String REGEX_ID_CARD = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)";

    public static final int MINLENGTH = 15;
    public static final int MAXLENGTH = 18;
    public static final int SEPARATENUM = 4;
    public static final String SEPARATOR = " ";

    private boolean isIdValidate = false;

    private final String ID_INVALID_MSG;

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
            if (s.toString().length() > oldtext.length()) {// add
                addOrDel = 1;
            } else {
                addOrDel = 0;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            setError(null);
            removeTextChangedListener(mTextWatcher);
            String source = s.toString();
            String formateText = formatIdNum(source);
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
            if (checkId(getIdNum()) && mCallback != null) {
                mCallback.onNumberChanged(IdNoEditText.this);
            }
        }
    };

    public IdNoEditText(Context context) {
        this(context, null);
    }

    public IdNoEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // Attention here !
    }

    public IdNoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAXLENGTH + MAXLENGTH / SEPARATENUM)});
        this.addTextChangedListener(mTextWatcher);
        ID_INVALID_MSG = getResources().getString(R.string.account_verify_id_num_invalid);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused && !checkId(getIdNum())) {
            setError(ID_INVALID_MSG);
        }
    }

    public String getIdNum() {
        String cardNum = getText().toString();
        if (!TextUtils.isEmpty(cardNum)) {
            return cardNum.replace(" ", "");
        }
        return null;
    }

    public boolean isIdValidate() {
        return isIdValidate;
    }

    private String formatIdNum(String idsrc) {
        if (TextUtils.isEmpty(idsrc)) {
            return idsrc;
        }
        String src = idsrc.replace(" ", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < src.length(); i++) {
            if (i % 4 == 0 && i != 0) {
                sb.append(" ");
            }
            sb.append(src.charAt(i));
        }
        return sb.toString();
    }

    private boolean checkId(String idNo) {
        if (TextUtils.isEmpty(idNo) || !Pattern.matches(REGEX_ID_CARD, idNo)) {
            return isIdValidate = false;
        }
        return isIdValidate = true;
    }

    public boolean checkValidateWithError(){
        if (checkId(getIdNum())) {
            return true;
        }
        setError(ID_INVALID_MSG);
        return false;
    }

    private EditTextActionCallback mCallback;

    public void setCallback(EditTextActionCallback callback) {
        mCallback = callback;
    }


}
