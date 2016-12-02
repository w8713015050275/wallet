package com.letv.walletbiz.movie.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.CinemaListByMovieActivity;
import com.letv.walletbiz.movie.beans.Movie;
import com.letv.walletbiz.movie.ui.MovieTabFlowLayout;
import com.letv.walletbiz.movie.utils.DrawableUtils;

import org.xutils.xmain;

import java.util.List;

/**
 * Created by lijujying on 16-6-29.
 */
public class MovieListAdapter extends RecyclerView.Adapter implements View.OnClickListener {

    private Context mContext;

    private List<Movie> mData;

    public MovieListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Movie movie = mData.get(position);
        ItemHolder itemHolder = (ItemHolder) holder;
        xmain.image().bind(itemHolder.mPosterView, movie.poster_url, ImageOptionsHelper.getDefaltImageLoaderOptions());
        itemHolder.mNameView.setText(movie.name);
        itemHolder.mSimpleRemark.setText(movie.simple_remark);

        itemHolder.mActorView.setText(movie.actor);
        if (movie.sche_date == null || movie.sche_date.length == 0) {
            itemHolder.mBuyButton.setVisibility(View.GONE);
        } else if (movie.is_booking == 1) {
            itemHolder.mBuyButton.setVisibility(View.VISIBLE);
            itemHolder.mBuyButton.setText(R.string.movie_btn_presell_ticket);
            itemHolder.mBuyButton.setTextColor(mContext.getResources().getColor(R.color.movie_schedule_price, mContext.getTheme()));
            itemHolder.mBuyButton.setBackground(DrawableUtils.getMovieYellowBtnDrawable(mContext));
        } else {
            itemHolder.mBuyButton.setVisibility(View.VISIBLE);
            itemHolder.mBuyButton.setText(R.string.movie_btn_purchase_ticket);
            itemHolder.mBuyButton.setTextColor(mContext.getResources().getColor(R.color.movie_primary_color, mContext.getTheme()));
            itemHolder.mBuyButton.setBackground(DrawableUtils.getMovieBtnDrawable(mContext));
        }

        itemHolder.mScoreView.setText(getScoreStr(movie));
        itemHolder.mBuyButton.setTag(movie);
        itemHolder.mBuyButton.setOnClickListener(this);

        itemHolder.mMovieTagContainer.removeAllViews();
        String[] array = movie.getVersionArray();
        TextView child;
        if (array != null) {
            for (String temp : array) {
                child = (TextView) LayoutInflater.from(mContext).inflate(R.layout.movie_special_item, itemHolder.mMovieTagContainer, false);
                child.setText(temp);
                itemHolder.mMovieTagContainer.addView(child);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public Movie getItem(int position) {
        if (mData != null && position >= 0 && position < mData.size()) {
            return mData.get(position);
        }
        return null;
    }

    public void setData(List<Movie> data) {
        mData = data;
        notifyDataSetChanged();
    }

    private CharSequence getScoreStr(Movie movie) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (movie.score > 0) {
            builder.append(String.valueOf(movie.score));
            builder.append(mContext.getString(R.string.movie_score), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            builder.append(String.valueOf(movie.want_count));
            builder.append(mContext.getString(R.string.movie_want), new AbsoluteSizeSpan(10, true), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    @Override
    public void onClick(View v) {
        Movie movie = (Movie) v.getTag();
        if (movie == null || mContext == null) {
            return;
        }
        Intent intent = new Intent(mContext, CinemaListByMovieActivity.class);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_NAME, movie.name);
        intent.putExtra(MovieTicketConstant.EXTRA_SCHE_DATE, movie.sche_date);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, movie.id);
        mContext.startActivity(intent);
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        ImageView mPosterView;
        TextView mNameView;
        MovieTabFlowLayout mMovieTagContainer;
        TextView mSimpleRemark;
        TextView mActorView;
        TextView mScoreView;
        Button mBuyButton;

        public ItemHolder(View itemView) {
            super(itemView);
            mPosterView = (ImageView) itemView.findViewById(R.id.movie_poster);
            mNameView = (TextView) itemView.findViewById(R.id.movie_name);
            mMovieTagContainer = (MovieTabFlowLayout) itemView.findViewById(R.id.movie_tag_container);
            mSimpleRemark = (TextView) itemView.findViewById(R.id.movie_simple_remark);
            mActorView = (TextView) itemView.findViewById(R.id.actor);
            mScoreView = (TextView) itemView.findViewById(R.id.score);
            mBuyButton = (Button) itemView.findViewById(R.id.btn_buy_ticket);
        }
    }

}
