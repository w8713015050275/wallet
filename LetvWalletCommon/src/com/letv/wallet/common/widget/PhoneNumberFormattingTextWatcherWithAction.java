package com.letv.wallet.common.widget;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.SystemUtils;

import java.util.Locale;

/**
 * Created by linquan on 15-12-15.
 */
public class PhoneNumberFormattingTextWatcherWithAction implements TextWatcher {
    /**
     * Indicates the change was caused by ourselves.
     */
    private boolean mSelfChange = false;

    /**
     * Indicates the formatting has been stopped.
     */
    private boolean mStopFormatting;

    private AsYouTypeFormatter mFormatter;
    private ActionCallback mCallback;

    private int mOldLength = 0;

    /**
     * The formatting is based on the current system locale and future locale changes
     * may not take effect on this instance.
     */
    public PhoneNumberFormattingTextWatcherWithAction(ActionCallback callback) {
        this(Locale.getDefault().getCountry(), callback);

    }

    /**
     * The formatting is based on the given <code>countryCode</code>.
     *
     * @param countryCode the ISO 3166-1 two-letter country code that indicates the country/region
     *                    where the phone number is being entered.
     */
    public PhoneNumberFormattingTextWatcherWithAction(String countryCode, ActionCallback callback) {
        if (countryCode == null) throw new IllegalArgumentException();
        mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
        mCallback = callback;
        mOldLength = 0;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        if (mSelfChange || mStopFormatting) {
            return;
        }
        // If the user manually deleted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mSelfChange || mStopFormatting) {
            return;
        }
        // If the user inserted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    @Override
    public synchronized void afterTextChanged(Editable s) {
        inputCheck(s);
        if (mStopFormatting) {
            // Restart the formatting when all texts were clear.
            mStopFormatting = !(s.length() == 0);
            mCallback.onNumberChanged();
            mOldLength = s.length();
            return;
        }
        if (mSelfChange) {
            // Ignore the change caused by s.replace().
            return;
        }
        String formatted = reformat(s, Selection.getSelectionEnd(s));
        if (formatted != null) {
            int rememberedPos = mFormatter.getRememberedPosition();
            mSelfChange = true;
            s.replace(0, s.length(), formatted, 0, formatted.length());
            // The text could be changed by other TextWatcher after we changed it. If we found the
            // text is not the one we were expecting, just give up calling setSelection().
            if (formatted.equals(s.toString())) {
                Selection.setSelection(s, rememberedPos);
            }
            mOldLength = s.length();
            mSelfChange = false;
        }
        mCallback.onNumberChanged();
    }

    /**
     * Check the input space and the length of the input more than mobile phone number
     *
     * @param s
     */
    private void inputCheck(Editable s) {
        int length = s.length();
        // To determine whether the input Spaces
        if (length > 0) {
            // Don't listen when removed
            if (length > mOldLength) {
                char endChar = s.toString().toCharArray()[s.length() - 1];
                if (" ".equals(endChar + "")) {
                    // If the input space automatically cleared
                    if (length == 1) {
                        s.replace(0, 1, "", 0, 0);
                        mOldLength = s.length();
                        return;
                    } else {
                        s.replace(0, length, s.subSequence(0, length - 1), 0, length - 1);
                        mOldLength = s.length();
                        return;
                    }
                } else {
                    // Length is greater than the mobile phone number when input is prohibited
                    String content = s.toString().replaceAll(" ", "");
                    if (content.length() > CommonConstants.PHONENUMBER_LENGTH) {
                        s.replace(0, length, s.subSequence(0, length - 1), 0, length - 1);
                        mOldLength = s.length();
                        return;
                    }
                }
            }
        } else {
            mOldLength = 0;
        }
    }

    /**
     * Generate the formatted number by ignoring all non-dialable chars and stick the cursor to the
     * nearest dialable char to the left. For instance, if the number is  (650) 123-45678 and '4' is
     * removed then the cursor should be behind '3' instead of '-'.
     */
    private String reformat(CharSequence s, int cursor) {
        // The index of char to the leftward of the cursor.
        int curIndex = cursor - 1;
        String formatted = null;
        mFormatter.clear();
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == curIndex) {
                hasCursor = true;
            }
        }
        if (lastNonSeparator != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor);
        }
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        String formattedNum = hasCursor ? mFormatter
                .inputDigitAndRememberPosition(lastNonSeparator)
                : mFormatter.inputDigit(lastNonSeparator);
        if (!SystemUtils.getSystemBooleanProperties("persist.env.sys.hypenenable", true)) {
            return formattedNum.replace("-", "");
        }
        return formattedNum;
    }

    private void stopFormatting() {
        mStopFormatting = true;
        mFormatter.clear();
    }

    private boolean hasSeparator(final CharSequence s, final int start, final int count) {
        for (int i = start; i < start + count; i++) {
            char c = s.charAt(i);
            if (!PhoneNumberUtils.isNonSeparator(c) && c != 32) {
                return true;
            }
        }
        return false;
    }

    public interface ActionCallback {

        boolean onNumberChanged();
    }
}



