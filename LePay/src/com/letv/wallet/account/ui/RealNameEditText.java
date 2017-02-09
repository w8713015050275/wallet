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

import com.letv.wallet.R;

import java.util.regex.Pattern;

/**
 * Created by lijunying on 17-2-9.
 */

public class RealNameEditText extends EditTextWithCustomError {
    public static final String REGEX_REAL_NAME = "^([\\u4e00-\\u9fa5]{1,20}|[a-zA-Z\\.\\s]{1,20})$";
    private boolean isNameValidate = false;
    private final String NAME_INVALID_MSG;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            setError(null);
            if (!checkRealName(s.toString()) && mCallback != null) {
                mCallback.onNumberChanged(RealNameEditText.this);
            }
        }
    };

    public RealNameEditText(Context context) {
        this(context, null);
    }

    public RealNameEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // Attention here !
    }

    public RealNameEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        NAME_INVALID_MSG = getResources().getString(R.string.account_verify_name_invalid);
        this.setSingleLine(true);
        this.addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused && !isNameValidate) {
            setError(NAME_INVALID_MSG);
        }
    }

    public boolean isNameValidate() {
        return isNameValidate;
    }

    private boolean checkRealName(String name) {
        if (TextUtils.isEmpty(name) || !Pattern.matches(REGEX_REAL_NAME, name)) {
            return isNameValidate = false;
        }
        return isNameValidate = true;
    }

    public boolean checkValidateWithError(){
        if (checkRealName(getText().toString())) {
            return true;
        }
        setError(NAME_INVALID_MSG);
        return false;
    }

    private EditTextActionCallback mCallback;

    public void setCallback(EditTextActionCallback callback) {
        mCallback = callback;
    }


}
