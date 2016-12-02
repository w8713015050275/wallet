package com.letv.wallet.common.util;

import android.text.TextUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuliang on 16-7-4.
 */
public class PhoneNumberUtils {

    //Reformat phone number as required :
    //Todo: other conditions need to consider
    private static String reformatPhoneNumber(String regex, String phoneNumber, boolean isReformat) {
        if (TextUtils.isEmpty(phoneNumber))
            return null;
        PhoneNumberUtil util;
        Phonenumber.PhoneNumber phoneN;
        Pattern p = null;
        Matcher m = null;
        try {
            util = PhoneNumberUtil.getInstance();
            phoneN = util.parse(phoneNumber, "CN");
            LogHelper.d("[UiUtils] reformatPhoneNumber CountryCode == [%S]", phoneN.getCountryCode());
            if (phoneN.getCountryCode() != 86)
                return null;
            p = Pattern.compile(regex); // 验证手机号
            m = p.matcher(String.valueOf(phoneN.getNationalNumber()));
            if (!m.find())
                return null;
            if (isReformat) {
                phoneNumber = util.format(phoneN, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            }
            return phoneNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Reformat phone number as required :
    //Todo: other conditions need to consider
    public static String checkPhoneNumber(String phoneNumber, boolean isReformat) {
        return checkPhoneNumber(CommonConstants.PHONENUMBER_RIGOROUS_REGEX, phoneNumber, isReformat);
    }

    public static String checkPhoneNumber(String regex, String phoneNumber, boolean isReformat) {
        return reformatPhoneNumber(regex, phoneNumber, isReformat);
    }
}
