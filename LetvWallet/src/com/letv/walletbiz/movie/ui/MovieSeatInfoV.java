package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.activity.MovieSeatActivity;

/**
 * Created by changjiajie on 16-2-2.
 */
public class MovieSeatInfoV extends LinearLayout {

    private Context mContext;
    private TextView mSeatInfoV;
    private MovieSeatActivity.OnSeatClickListener mSeatCancelClickLis;

    private String mArea;
    private int mColumnNum;
    private int mRowNum;
    private String desc;
    private String n;
    private String TAG;

    public MovieSeatInfoV(Context context) {
        this(context, null);
    }

    public MovieSeatInfoV(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieSeatInfoV(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setLayoutParams();
        initV();
        setOnClickListener();
    }

    public void setSeatCancelClickListener(MovieSeatActivity.OnSeatClickListener seatCancelClickLis) {
        this.mSeatCancelClickLis = seatCancelClickLis;
    }

    private void initV() {
        LayoutInflater.from(mContext)
                .inflate(R.layout.movie_seat_info_v, this, true);
        mSeatInfoV = (TextView) findViewById(R.id.seat_info);
    }

    private void setOnClickListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeatCancelClickLis.cancelSeat(MovieSeatInfoV.this, mArea, mRowNum, mColumnNum, desc, n);
            }
        });
    }

    private void setLayoutParams() {
        setBackgroundResource(R.drawable.btn_round_shape_blue);
        setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) DensityUtils.dip2px(5);
        params.rightMargin = (int) DensityUtils.dip2px(5);
        params.gravity = Gravity.CENTER_VERTICAL;
        setLayoutParams(params);
        setPadding((int) DensityUtils.dip2px(8), (int) DensityUtils.dip2px(3), 0, (int) DensityUtils.dip2px(3));
    }

    public void setInfo(String area, int rowNum, int columnNum, String desc, String n) {
        this.mArea = area;
        this.mRowNum = rowNum;
        this.mColumnNum = columnNum;
        this.desc = desc;
        this.n = n;
        this.mSeatInfoV.setText(getString(R.string.movie_seat_info_formatter, desc, n));
        this.TAG = creatTag(area, desc, n);
        this.setTag(mArea);
    }

    private String getString(int resorceId, String... str) {
        return getContext().getString(resorceId, str[0], str[1]);
    }

    public static String creatTag(String area, String desc, String n) {
        return area + desc + n;
    }

    public String getTag() {
        return this.TAG;
    }
}
