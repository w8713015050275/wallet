package com.letv.walletbiz.movie.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.beans.MovieProduct;

/**
 * Created by linquan on 16-1-5.
 */
public class MovieProductBrief extends LinearLayout {

    private TextView mMovieName;
    private TextView mCinemaName;
    private TextView mRoomName;
    private TextView mTime;
    private TextView mSeatInfo;

    public MovieProductBrief(Context context) {
        this(context, null);
    }

    public MovieProductBrief(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MovieProductBrief(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 初始化函数
     */
    public void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.movie_order_product_brief, this);
        mMovieName = (TextView) view.findViewById(R.id.movie_name);
        mCinemaName = (TextView) view.findViewById(R.id.cinema_name);
        mRoomName = (TextView) view.findViewById(R.id.room_name);
        mTime = (TextView) view.findViewById(R.id.time);
        mSeatInfo = (TextView) view.findViewById(R.id.seat_info);
    }

    public void setMovieName(String s) {
        mMovieName.setText(s);
    }

    public void setCinemaName(String s) {
        mCinemaName.setText(s);
    }

    public void setRoomName(String s) {
        mRoomName.setText(s);
    }

    public void setTime(String s) {
        mTime.setText(s);
    }

    public void setSeatInfo(String s) {
        mSeatInfo.setText(s);
    }

    public void setData(MovieProduct product) {
        setMovieName(product.getProductName());
        setCinemaName(product.getCinemaName());
        setRoomName(product.getRoomText());
        setTime(product.getDate());
        setSeatInfo(product.getSeatDesc(getContext()));
    }
}
