package com.letv.walletbiz.movie.beans;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.pay.Product;
import com.letv.walletbiz.base.util.PayHelper;
import com.letv.walletbiz.movie.activity.MoviePayActivity;
import com.letv.walletbiz.movie.pay.MoviePayAdapter;
import com.letv.walletbiz.movie.pay.MoviePayResultAdapter;

/**
 * Created by liuliang on 16-2-16.
 */
public class MovieProduct extends Product {

    private BaseMovieOrder mMovieOrder;

    private long mMovieId;
    private int mCinemaId;
    private String mCinemaName;
    private String mDate;
    private String mRoomText;

    private String mSeatDesc;

    private String mPhoneNumber;

    public MovieProduct(BaseMovieOrder order, long movieId, int cinemaId, String movieName, String cinemaName, String date, String time, String roomName) {
        this(order, movieName, cinemaName, date, time, roomName);
        mMovieId = movieId;
        mCinemaId = cinemaId;
    }

    public MovieProduct(BaseMovieOrder order, String movieName, String cinemaName, String date, String time, String roomName) {
        mMovieOrder = order;
        mSN = mMovieOrder.getMovieOrderNo();
        mPrice = String.valueOf(mMovieOrder.getMoviePrice());
        mName = movieName;
        mCinemaName = cinemaName;
        mDate = getDateStr(date, time);
        mRoomText = roomName;
        mPhoneNumber = AccountHelper.getInstance().getUserMobile();
        intAdapter();
    }

    private void intAdapter() {
        mPayAdapter = new MoviePayAdapter(this);
        mResultAdapter = new MoviePayResultAdapter(this);
    }

    @Override
    public void showPayResult(Context context, int result, String status) {
        mPayResult = result;
        mPayStatus = status;
        PayHelper.startPayResultActivity(context, this);
    }

    public long getMovieId() {
        return mMovieId;
    }

    public int getCinemaId() {
        return mCinemaId;
    }

    public BaseMovieOrder getMovieOrder() {
        return mMovieOrder;
    }

    public String getCinemaName() {
        return mCinemaName;
    }

    public String getDate() {
        return mDate;
    }

    public String getRoomText() {
        return mRoomText;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getSeatDesc(Context context) {
        if (TextUtils.isEmpty(mSeatDesc)) {
            mSeatDesc = getSeatDescStr(context, mMovieOrder.getMovieSeat());
        }
        return mSeatDesc;
    }

    private String getDateStr(String date, String time) {
        String dateStr = DateUtils.convertPatternForDate(date, "yyyyMMdd", "yyyy-MM-dd");
        return dateStr + " " + time;
    }

    private String getSeatDescStr(Context context, String seatStr) {
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
            String desc = getSeatInfo(context, temp[0], temp[1]);
            if (desc != null) {
                builder.append(desc);
                builder.append("  ");
            }
        }

        return builder.toString();
    }

    private String getSeatInfo(Context context, String row, String column) {
        try {
            return context.getString(R.string.movie_seat_info_formatter, row, column);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void pay(Context context) {
        pay(context, false);
    }
    public void pay(Context context, boolean isFromOrderList) {
        Intent intent = new Intent(context, MoviePayActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(ActivityConstant.PAY_PARAM.PAY_PRODUCT, this);
        b.putBoolean(MoviePayActivity.EXTRA_FROM_ORDER_LIST, isFromOrderList);
        intent.putExtras(b);
        context.startActivity(intent);
    }

}
