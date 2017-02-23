package com.letv.wallet.account.ui;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.letv.wallet.R;

import java.util.regex.Pattern;

/**
 * Created by lijunying on 17-2-9.
 */

public class RealNameEditText extends TextInputLayout implements View.OnFocusChangeListener {
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
        inflate(context, R.layout.account_real_name_view, this);
        NAME_INVALID_MSG = getResources().getString(R.string.account_verify_name_invalid);
        getEditText().setSingleLine(true);
        getEditText().addTextChangedListener(mTextWatcher);
        getEditText().setOnFocusChangeListener(this);
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
        if (checkRealName(getEditText().getText().toString())) {
            return true;
        }
        setError(NAME_INVALID_MSG);
        return false;
    }

    public Editable getText(){
        return getEditText().getText();
    }

    private EditTextActionCallback mCallback;

    public void setCallback(EditTextActionCallback callback) {
        mCallback = callback;
    }


    @Override
    public void onFocusChange(View v, boolean focused) {
        if (!focused && !isNameValidate) {
            setError(NAME_INVALID_MSG);
        }
    }
}
