package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.beans.CityList;

/**
 * Created by liuliang on 16-1-18.
 */
public class ChooseCityHeaderView extends LinearLayout implements View.OnClickListener {

    private static final int LAYOUT_COLUMES = 3;

    private TextView mGeoCityHeaderView;
    private TextView mGeoCityView;
    private TextView mHotCityHeaderView;

    private LinearLayout mHotCitiesView;

    private OnClickListener mOnItemClickListener;

    public ChooseCityHeaderView(Context context) {
        this(context, null);
    }

    public ChooseCityHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChooseCityHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.movie_choose_city_header, this, true);

        mGeoCityHeaderView = (TextView) findViewById(R.id.geocity_header);
        mGeoCityHeaderView.setText(R.string.movie_location_city);
        mGeoCityView = (TextView) findViewById(R.id.geocity);
        mGeoCityView.setOnClickListener(this);
        mHotCityHeaderView = (TextView) findViewById(R.id.hotcity_header);
        mHotCityHeaderView.setText(R.string.movie_hot_cities);

        mHotCitiesView = (LinearLayout) findViewById(R.id.hot_cities);
    }

    public void setHotCityArray(CityList.City[] cityArray) {
        setupHotCityView(cityArray);
    }

    public void setOnHeaderItemClickListener(OnClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setGeoCity(int resId) {
        setGeoCity(getResources().getString(resId));
    }

    public void setGeoCity(CharSequence text) {
        mGeoCityView.setText(text);
    }

    private void setupHotCityView(CityList.City[] cityArray) {
        if (cityArray == null || cityArray.length <= 0) {
            return;
        }

        mHotCitiesView.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int paddingleft = (int) getResources().getDimension(R.dimen.movie_list_padding_left);
        int paddingRight = (int) getResources().getDimension(R.dimen.movie_list_alphabet_width);
        int hotPaddingleft = (int) getResources().getDimension(R.dimen.movie_hot_list_item_padding_left);
        int hotPaddingTop = (int) getResources().getDimension(R.dimen.movie_hot_list_item_padding_top);
        for (int i=0; i<cityArray.length; ) {
            LinearLayout row = new LinearLayout(getContext());
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(paddingleft, hotPaddingTop, paddingRight, hotPaddingTop);
            for (int j=0; j<3 && i<cityArray.length; j++, i++) {
                CityList.City city = cityArray[i];
                TextView view = (TextView) inflater.inflate(R.layout.movie_hot_city_item, row, false);
                view.setText(city.name);
                view.setTag(city);
                view.setOnClickListener(this);
                if (j == 1) {
                    view.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
                    view.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                row.addView(view);
            }
            mHotCitiesView.addView(row, layoutParams);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onClick(v);
        }
    }
}
