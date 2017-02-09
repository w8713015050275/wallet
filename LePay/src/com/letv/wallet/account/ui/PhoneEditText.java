package com.letv.wallet.account.ui;

import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.letv.wallet.R;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.wallet.common.widget.PhoneNumberFormatter;
import com.letv.wallet.common.widget.PhoneNumberFormattingTextWatcherWithAction;

/**
 * Created by lijunying on 17-2-9.
 */

public class PhoneEditText extends EditTextWithCustomError implements PhoneNumberFormattingTextWatcherWithAction.ActionCallback {
    private static final int MOBILE_LEN = 11;
    private String mRegex = CommonConstants.PHONENUMBER_RIGOROUS_REGEX;
    private boolean mReformat = false;

    private boolean isPhoneValidate = false;

    private EditTextActionCallback mCallback;

    private final String PHONE_INVALID_MSG;

    public PhoneEditText(Context context) {
        this(context, null);
    }

    public PhoneEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public PhoneEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PhoneEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getContext(), this, this);
        PHONE_INVALID_MSG = getResources().getString(R.string.account_verify_phone_invalid);
        this.setInputType(InputType.TYPE_CLASS_PHONE);
        this.setSingleLine(true);
    }

    public String getPhone() {
        String text = getText().toString();
        if (!TextUtils.isEmpty(text)) {
            return text.replaceAll(" ", "");
        }
        return null;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        LogHelper.e("onFocusChanged focused = " + focused + " isPhoneValidate = " + isPhoneValidate);
        if (!focused && !isPhoneValidate) {
            setError(PHONE_INVALID_MSG);
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onNumberChanged() {
        setError(null);
        if (checkPhoneValidate(getPhone()) && mCallback != null) {
            mCallback.onNumberChanged(PhoneEditText.this);
        }
        return false;
    }

    private boolean checkPhoneValidate(String newNumber ){
        final int length = newNumber == null ? 0 : newNumber.length();
        if (length == MOBILE_LEN && !TextUtils.isEmpty(PhoneNumberUtils.checkPhoneNumber(mRegex, newNumber, mReformat))) {
            isPhoneValidate = true;
        } else {
            isPhoneValidate = false;
        }
        return isPhoneValidate;
    }

    public boolean checkValidateWithError(){
        if (checkPhoneValidate(getPhone())) {
            return true;
        }
        setError(PHONE_INVALID_MSG);
        return false;
    }

    public boolean isPhoneValidate() {
        return isPhoneValidate;
    }


    public void setCallback(EditTextActionCallback callback) {
        mCallback = callback;
    }

}
