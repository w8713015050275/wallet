package com.letv.wallet.account.ui;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Toast;

import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.CardbinAvailableInfo;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.CardbinTask;
import com.letv.wallet.common.util.ExecutorHelper;

/**
 * Created by lijunying on 17-2-9.
 */

public class SmsCodeEditText extends EditTextWithCustomError {

    public static final int LENGTH = 6;

    private boolean isCodeValidate = false;

    private final String SMSCODE_INVALID_MSG;

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
            if (checkSmsCode(s.toString()) && mCallback != null) {
                mCallback.onNumberChanged(SmsCodeEditText.this);
            }
        }
    };

    public SmsCodeEditText(Context context) {
        this(context, null);
    }

    public SmsCodeEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // Attention here !
    }

    public SmsCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SMSCODE_INVALID_MSG = getResources().getString(R.string.account_verify_sms_invalid);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(LENGTH)});
        this.setInputType(InputType.TYPE_CLASS_PHONE);
        this.setSingleLine(true);
        this.addTextChangedListener(mTextWatcher);
    }


    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (!focused && !isCodeValidate) {
           setError(SMSCODE_INVALID_MSG);
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public boolean isSmsCodeValidate() {
        return isCodeValidate;
    }

    private boolean checkSmsCode(String code) {
        if (TextUtils.isEmpty(code)|| !code.matches("\\d+")
                || code.length() != LENGTH) {
            return isCodeValidate = false;
        }
        return isCodeValidate = true;
    }

    public boolean checkValidateWithError(){
        if (checkSmsCode(getText().toString())) {
            return true;
        }
        setError(SMSCODE_INVALID_MSG);
        return false;
    }

    private EditTextActionCallback mCallback;

    public void setCallback(EditTextActionCallback callback) {
        mCallback = callback;
    }


}
