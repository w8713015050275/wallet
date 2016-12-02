package com.letv.wallet.common.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.letv.wallet.common.R;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.PhoneNumberUtils;

/**
 * Created by changjiajie on 16-3-29.
 */
public class PhoneEditText extends LinearLayout implements PhoneNumberFormattingTextWatcherWithAction.ActionCallback {

    private static final int MOBILE_LEN = 11;
    public static final int PHONENUMBER_SIMPLE_VERIFICATION = 1;
    public static final int PHONENUMBER_RIGOROUS_VERIFICATION = 2;

    private String mRegex = CommonConstants.PHONENUMBER_RIGOROUS_REGEX;
    private boolean mReformat = false;

    private static String mPromptInputRightNumber;

    private EditText mPhoneEdittext;

    private ActionCallback mCallback;

    public PhoneEditText(Context context) {
        this(context, null);
    }

    public PhoneEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhoneEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PhoneEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initV();
    }

    private void initV() {
        mPhoneEdittext = (EditText) LayoutInflater.from(getContext()).inflate(R.layout.phone_edittext, this, false);
        if (getChildCount() > 0) {
            removeAllViews();
        }
        addView(mPhoneEdittext);
        mPromptInputRightNumber = getContext().getString(R.string.phonenumber_prompt_input_right_number);
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getContext(), mPhoneEdittext, this);
    }

    public void setTextAppearance(int styleid) {
        if (isNull()) return;
        mPhoneEdittext.setTextAppearance(getContext(), styleid);
    }

    public void setTextStyleBold() {
        if (isNull()) return;
        mPhoneEdittext.setTextAppearance(getContext(), R.style.BoldText);
    }

    public void setCallback(ActionCallback callback) {
        mCallback = callback;
    }

    public void setTextSize(float size) {
        if (isNull()) return;
        mPhoneEdittext.setTextSize(size);
    }

    public void setTextHintcolor(ColorStateList colors) {
        if (isNull()) return;
        mPhoneEdittext.setHintTextColor(colors);
    }

    public void setTextHintcolor(int color) {
        if (isNull()) return;
        mPhoneEdittext.setHintTextColor(color);
    }

    public void setTextColor(ColorStateList colors) {
        if (isNull()) return;
        mPhoneEdittext.setTextColor(colors);
    }

    public void setTextColor(int color) {
        if (isNull()) return;
        mPhoneEdittext.setTextColor(color);
    }

    public void setHint(CharSequence text) {
        if (isNull()) return;
        mPhoneEdittext.setHint(text);
    }

    public void setHint(int resid) {
        if (isNull()) return;
        mPhoneEdittext.setHint(resid);
    }

    public void setText(CharSequence text) {
        if (isNull()) return;
        mPhoneEdittext.setText(text);
    }

    public void setSelection(int index) {
        if (isNull()) return;
        mPhoneEdittext.setSelection(index);
    }

    public String getTextContent() {
        if (isNull()) return null;
        return mPhoneEdittext.getText().toString();
    }

    public void clearEdittextFocus() {
        if (isNull())
            return;
        mPhoneEdittext.clearFocus();
    }

    public boolean requestEdittextFocus() {
        if (isNull()) return false;
        return mPhoneEdittext.requestFocus();
    }

    public String getMobileNumber() {
        if (isNull()) return null;
        String s = mPhoneEdittext.getText().toString();
        String number = s.replaceAll(" ", "");
        return number;
    }

    public EditText getEditText() {
        return mPhoneEdittext;
    }

    private boolean isNull() {
        if (mPhoneEdittext == null) {
            return true;
        }
        return false;
    }

    /**
     * @param verificationLevel
     */
    public void setVerificationLevel(int verificationLevel) {
        switch (verificationLevel) {
            case PHONENUMBER_SIMPLE_VERIFICATION:
                mRegex = CommonConstants.PHONENUMBER_SIMPLE_REGEX;
                break;
            case PHONENUMBER_RIGOROUS_VERIFICATION:
                mRegex = CommonConstants.PHONENUMBER_RIGOROUS_REGEX;
                break;
        }
    }

    public void hideSoftkeyboard() {
        clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public boolean onNumberChanged() {
        final String newNumber = getMobileNumber();
        final int length = newNumber.length();
        if (length == MOBILE_LEN) {
            hideSoftkeyboard();
            if (TextUtils.isEmpty(PhoneNumberUtils.checkPhoneNumber(mRegex, newNumber, mReformat))) {
                Toast.makeText(getContext(), mPromptInputRightNumber, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (mCallback != null) {
            return mCallback.onNumberChanged(newNumber, length);
        }
        return false;
    }

    public interface ActionCallback {
        boolean onNumberChanged(String content, int length);
    }
}
