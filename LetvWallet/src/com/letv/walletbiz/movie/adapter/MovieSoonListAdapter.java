package com.letv.walletbiz.movie.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.letv.wallet.common.util.DateUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.CinemaListByMovieActivity;
import com.letv.walletbiz.movie.beans.Movie;
import com.letv.walletbiz.movie.ui.MovieSoonHorizontalScrollView;
import com.letv.walletbiz.movie.ui.MovieTabFlowLayout;
import com.letv.walletbiz.movie.utils.DrawableUtils;

import org.xutils.xmain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import timehop.stickyheader.StickyRecyclerHeadersAdapter;

/**
 * Created by lijujying on 16-6-29.
 */
public class MovieSoonListAdapter extends RecyclerView.Adapter implements StickyRecyclerHeadersAdapter, SectionIndexer,View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Context mContext;

    private List<Movie> mData;

    private List<Movie> mFilterDatas;

    private RadioGroup rgTimeFilter;

    private List<RadioButton> rbList = new ArrayList<>() ;

    private int currenttSortMode = SORT_TIME;

    private String currentMonthOfyear;

    private int mCurrentCityID;

    public MovieSoonListAdapter(Context context, RadioGroup rgFilter, int mCurrentCityID) {
        mContext = context;
        this.rgTimeFilter = rgFilter;
        this.rgTimeFilter.setOnCheckedChangeListener(this);
        this.mCurrentCityID = mCurrentCityID;
        currentMonthOfyear = mContext.getResources().getString(R.string.movie_soon_filter_time_all);
        setHasStableIds(true);
    }

    public Movie getItem(int position) {
        if (mFilterDatas != null && position >= 0 && position < mFilterDatas.size()) {
            return mFilterDatas.get(position);
        }
        return null;
    }

    public void setData(List<Movie> data) {
        sortMovieAsyn(data , currenttSortMode);
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Movie movie = mFilterDatas.get(position);
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
    public long getHeaderId(int position) {
        if (mFilterDatas == null || mFilterDatas.size() == 0) {
            return -1;
        }
        return Long.valueOf(mFilterDatas.get(position).date);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_city_list_section, parent, false);
        return new SectionHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(DateUtils.convertPatternForDate(mFilterDatas.get(position).date, "yyyyMMdd", mContext.getString(R.string.movie_soon_list_section)));

    }

    @Override
    public int getItemCount() {
        return mFilterDatas == null ? 0 : mFilterDatas.size();
    }

    @Override
    public long getItemId(int position) {
        return mFilterDatas == null ? -1 : mFilterDatas.get(position).id;
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
        intent.putExtra(MovieTicketConstant.EXTRA_CITY_ID, mCurrentCityID);
        mContext.startActivity(intent);
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mFilterDatas == null || mFilterDatas.size() == 0) {
            return -1;
        }
        for (int i = 0; i < mFilterDatas.size(); i++) {
            if (Integer.valueOf(mFilterDatas.get(i).date) == sectionIndex) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (mFilterDatas == null || mFilterDatas.size() == 0) {
            return -1;
        }
        return Integer.valueOf(mFilterDatas.get(position).date);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        View child = group.findViewById(checkedId);
        if(child == null || !(child instanceof RadioButton)){
            return;
        }
        if (group.getParent() instanceof MovieSoonHorizontalScrollView) {
            ((MovieSoonHorizontalScrollView) group.getParent()).resetScrollWidth(group.indexOfChild(child));
        }
        String str = (String) child.getTag();
        currentMonthOfyear = str ;
        this.mFilterDatas = getSortMovieListByMonth(str);
        notifyDataSetChanged();
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

    static class SectionHolder extends RecyclerView.ViewHolder {

        public SectionHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 获取指定月份的movieList
     * @param monthOfyear yyyyMM
     * @return
     */
    private ArrayList<Movie> getSortMovieListByMonth(String monthOfyear)
    {
        ArrayList localList = new ArrayList();
        if ((mData != null) && (mData.size() > 0))
        {
            if (monthOfyear.equals(mContext.getResources().getString(R.string.movie_soon_filter_time_all))) {
                localList.addAll(mData);
            } else {
                Iterator localIterator = mData.iterator();
                while (localIterator.hasNext())
                {
                    Movie localMovie = (Movie)localIterator.next();
                    if (monthOfyear.equals(DateUtils.convertPatternForDate(localMovie.date, "yyyyMMdd", "yyyyMM"))){
                        localList.add(localMovie);
                    }
                }
            }
        }
        return localList;
    }

    /**
     * 获取 list 的月份 filters
     * @param list 已按时间排序的 list
     */
    private ArrayList<String> getSortCheckFilters(ArrayList<Movie> list)
    {
        if ((list == null) || (list.size() == 0)) return null;

        LinkedHashSet localSet = new LinkedHashSet();
        localSet.add(mContext.getResources().getString(R.string.movie_soon_filter_time_all));
        String previous = DateUtils.convertPatternForDate(list.get(0).date, "yyyyMMdd", "yyyyMM");
        localSet.add(previous);
        int i = 1;
        while (i < list.size())
        {
            String str = DateUtils.convertPatternForDate(list.get(i).date, "yyyyMMdd", "yyyyMM");
            if (!previous.equals(str))
            {
                localSet.add(str);
                previous = str;
            }
            i++;
        }
        return  new ArrayList(localSet);
    }

    /**
     *
     * @param filters 过滤的年月
     */
    private void showTimerFilters(ArrayList<String> filters){
        if (rgTimeFilter != null) { rgTimeFilter.removeAllViews();}
        rbList.clear();
        if (filters == null || filters.size() == 0) return;

        RadioButton rb;
        String str;
        for (int i = 0; i < filters.size(); i++) {
            str = filters.get(i);
            rb = new RadioButton(this.mContext);
            rb.setId(i);
            rb.setTag(str);
            if (str.equals(mContext.getResources().getString(R.string.movie_soon_filter_time_all))) {
                rb.setChecked(true);
                rb.setText(str);
            } else {
                rb.setText(getDateFormatStr(str));
            }
            rb.setButtonDrawable(null);
            rb.setTextColor(mContext.getColorStateList(R.color.movie_soon_filter_text_color));
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0F);
            rb.setPadding(0, 0, (int)mContext.getResources().getDimension(R.dimen.movie_movie_list_fiters_padding_horizon), 0);
            rbList.add(rb);
            rgTimeFilter.addView(rb);
        }
        mFilterDatas = getSortMovieListByMonth(currentMonthOfyear);
        //刷新数据后, 当前月份不存在, 默认选择全部
        if(mFilterDatas == null || mFilterDatas.size() == 0){
            mFilterDatas = mData;
            currentMonthOfyear = mContext.getResources().getString(R.string.movie_soon_filter_time_all);
        }
        //更新当前月份
        if(! currentMonthOfyear.equalsIgnoreCase(mContext.getResources().getString(R.string.movie_soon_filter_time_all))){
            View currentRb = rgTimeFilter.findViewWithTag(currentMonthOfyear);
            if(currentRb != null && currentRb instanceof RadioButton){
                ((RadioButton)currentRb).setChecked(true);
            }
        }
    }

    private String getDateFormatStr(String date) {
        Date dateObj = DateUtils.parseDate(date, "yyyyMM");
        String result = null;
        if (DateUtils.isThisYear(dateObj)) {
            result = DateUtils.formatDate(dateObj, mContext.getString(R.string.movie_soon_list_filter_this_year));
        } else {
            result = DateUtils.formatDate(dateObj, mContext.getString(R.string.movie_soon_list_filter_other));
        }
        return result;
    }

    public static final int SORT_TIME = 0;
    public static final int SORT_WANT_COUNT = 1;

    private void sortMovieAsyn(final List<Movie> list, final int sortMode) {
        if (list == null || list.size() == 0) {
            return;
        }

        AsyncTask<Void, Void, List<Movie>> task = new AsyncTask<Void, Void, List<Movie>>() {

            @Override
            protected List<Movie> doInBackground(Void... params) {
                Collections.sort(list, new Comparator<Movie>() {
                    public int compare(Movie current, Movie then) {
                        switch (sortMode) {
                            case SORT_TIME:
                                return  DateUtils.parseDate(current.date, "yyyyMMdd").compareTo(DateUtils.parseDate(then.date, "yyyyMMdd"));

                            case SORT_WANT_COUNT:
                                return current.want_count < then.want_count ? 1 : (current.want_count == then.want_count ? 0 : -1);
                        }
                        return 0;
                    }
                });
                return list;
            }

            @Override
            protected void onPostExecute(List<Movie> result) {
                mData = result;
                if (sortMode == SORT_TIME) {
                    showTimerFilters(getSortCheckFilters((ArrayList<Movie>) mData));
                }else if(sortMode == SORT_WANT_COUNT){
                    mFilterDatas = mData;
                }

                notifyDataSetChanged();
            }
        };
        task.execute();
    }

    public void updateSortMode(int sorMode){
        currenttSortMode = sorMode;
        sortMovieAsyn(mData, currenttSortMode);
    }

}
