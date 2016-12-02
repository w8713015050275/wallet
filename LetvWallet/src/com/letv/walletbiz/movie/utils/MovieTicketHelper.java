package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;

import com.letv.walletbiz.R;

/**
 * Created by liuliang on 16-2-29.
 */
public class MovieTicketHelper {

    public static CharSequence getSeatDescStr(Context context, String seatStr) {
        if (TextUtils.isEmpty(seatStr)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String[] seatArray = seatStr.split("\\|");
        for (String seat : seatArray) {
            if (TextUtils.isEmpty(seat)) {
                continue;
            }
            String[] temp = seat.split(":");
            if (temp.length != 2) {
                continue;
            }
            CharSequence desc = getSeatInfo(context, temp[0], temp[1]);
            if (!TextUtils.isEmpty(desc)) {
                builder.append(desc);
                builder.append("  ");
            }
        }

        return builder.toString();
    }

    private static CharSequence getSeatInfo(Context context, String row, String column) {
        if (context == null || TextUtils.isEmpty(row) || TextUtils.isEmpty(column)) {
            return "";
        }
        String rowStr = row.replaceAll("^0+", "");
        String columnStr = column.replaceAll("^0+", "");
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(rowStr);
        builder.append(context.getString(R.string.movie_seat_info_row), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(String.valueOf(columnStr));
        builder.append(context.getString(R.string.movie_seat_info_column), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static CharSequence getTicketAmountStr(Context context, int amount) {
        if (context == null) {
            return "";
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(String.valueOf(amount));
        builder.append(context.getResources().getQuantityString(R.plurals.movie_ticket_amount, amount), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static CharSequence getTicketPriceStr(float price, String unit, int textSizeWithDip) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int temp = (int) price;
        if (temp == price) {
            builder.append(String.valueOf(temp));
        } else {
            builder.append(String.valueOf(price));
        }
        builder.append(unit, new AbsoluteSizeSpan(textSizeWithDip, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }
}
