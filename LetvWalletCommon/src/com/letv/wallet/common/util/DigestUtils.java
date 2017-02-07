package com.letv.wallet.common.util;

import android.text.TextUtils;

import java.security.MessageDigest;

/**
 * Created by lijunying on 17-1-10.
 */

public class DigestUtils {
    private static final char HEX_DIGITS[] = "0123456789abcdef".toCharArray();

    public static String getMd5_30(String source){
        return TextUtils.isEmpty(source) ? null : toMd5_30(source);

    }

    private static String toMd5_30(String source){
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(source.getBytes());
            byte messageDigest[] = digest.digest();
            return toHexString(messageDigest, 30);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String toHexString(byte[] b, int lenth) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.substring(0, lenth);
    }

}
