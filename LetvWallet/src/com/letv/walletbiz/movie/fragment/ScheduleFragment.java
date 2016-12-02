package com.letv.walletbiz.movie.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieSeatActivity;
import com.letv.walletbiz.movie.beans.CinemaSchedule.DiscountInfo;
import com.letv.walletbiz.movie.beans.CinemaSchedule.Schedule;
import com.letv.walletbiz.movie.utils.MovieTicketHelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 16-1-29.
 */
public class ScheduleFragment extends BaseFragment {
    private static final String  TAG = ScheduleFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private int mMovieLongs  = -1;
    private Schedule[] mSceduleArray;
    private DiscountInfo[] mDiscountArray;

    private String mCinemaName;
    private long mMovieId;
    private int mCinemaId;
    private String mMovieName;
    private String mDate;
    private int stopTime;

    private ScheduleAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateArguments(Bundle bundle) {
        if (bundle == null || getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        mMovieLongs = bundle.getInt(MovieTicketConstant.EXTRA_MOVIE_LONGS, -1);
        Parcelable[] parcelableArray = bundle.getParcelableArray(MovieTicketConstant.EXTRA_MOVIE_SCHEDULE);
        if (parcelableArray != null) {
            mSceduleArray = Arrays.copyOf(parcelableArray, parcelableArray.length, Schedule[].class);
        }
        parcelableArray = bundle.getParcelableArray(MovieTicketConstant.EXTRA_MOVIE_DISCOUNT_INFO);
        if (parcelableArray != null) {
            mDiscountArray = Arrays.copyOf(parcelableArray, parcelableArray.length, DiscountInfo[].class);
        }
        mMovieId = bundle.getLong(MovieTicketConstant.EXTRA_MOVIE_ID, -1);
        mCinemaId = bundle.getInt(MovieTicketConstant.EXTRA_CINEMA_ID, -1);
        mCinemaName = bundle.getString(MovieTicketConstant.EXTRA_CINEMA_NAME);
        mMovieName = bundle.getString(MovieTicketConstant.EXTRA_MOVIE_NAME);
        mDate = bundle.getString(MovieTicketConstant.EXTRA_DATE);
        if (mAdapter != null) {
            mAdapter.setData(mSceduleArray, mDiscountArray);
        }
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.movie_schedule_fragment, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), getResources().getColor(R.color.colorDividerLineBg, getContext().getTheme()),
                DividerItemDecoration.VERTICAL_LIST, getResources().getDimensionPixelSize(R.dimen.divider_width));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new ScheduleAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!view.isEnabled()) {
                    return;
                }
                if (AccountHelper.getInstance().loginLetvAccountIfNot(getActivity(), null)) {
                    if (mAdapter != null) {
                        Schedule schedule = (Schedule) view.getTag();
                        if (schedule != null) {
                            enterSeatSelect(schedule);
                        }
                    }
                }
            }
        }));
        return mRecyclerView;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.setData(mSceduleArray, mDiscountArray);
    }

    private void enterSeatSelect(Schedule schedule) {
        Intent intent = new Intent(getContext(), MovieSeatActivity.class);
        intent.putExtra(MovieTicketConstant.EXTRA_CINEMA_ID, mCinemaId);
        intent.putExtra(MovieTicketConstant.EXTRA_CINEMA_NAME, mCinemaName);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_NAME, mMovieName);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, mMovieId);
        intent.putExtra(MovieTicketConstant.EXTRA_DATE, mDate);
        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_SCHEDULE, (Parcelable) schedule);
        startActivity(intent);
    }

    private static class ItemHolder extends RecyclerView.ViewHolder {

        public TextView startTimeView;
        public TextView endTimeView;
        public TextView languageView;
        public TextView roomNameView;
        public TextView priceView;
        public TextView discountView;
        public Button seatSelectButton;

        public ItemHolder(View itemView) {
            super(itemView);
            startTimeView = (TextView) itemView.findViewById(R.id.start_time);
            endTimeView = (TextView) itemView.findViewById(R.id.end_time);
            languageView = (TextView) itemView.findViewById(R.id.language);
            roomNameView = (TextView) itemView.findViewById(R.id.room_name);
            priceView = (TextView) itemView.findViewById(R.id.price);
            discountView = (TextView) itemView.findViewById(R.id.discount);
            seatSelectButton = (Button) itemView.findViewById(R.id.seat_select);
            seatSelectButton.setClickable(false);
        }
    }

    private static class DiscountItemHolder extends RecyclerView.ViewHolder {

        public TextView discountDes;
        public TextView discountIntroduce;

        public DiscountItemHolder(View itemView) {
            super(itemView);
            discountDes = (TextView) itemView.findViewById(R.id.discount_des);
            discountIntroduce = (TextView) itemView.findViewById(R.id.discount_introduce);
        }
    }

    private class ScheduleAdapter extends RecyclerView.Adapter{

        private Context mContext;
        private Schedule[] mData;
        private DiscountInfo[] mDiscountInfo ;
        public static final int VIEW_TYPE_DISCOUNT_ITEM = 0;
        public static final int VIEW_TYPE_SCHEDULE_ITEM = 1;
        public static final int VIEW_TYPE_INVALID = -1;
        private int viewType = -1 ;
        private int viewDiscountCount = 0;
        private LayoutInflater mInflater ;
        private ItemHolder scheduleHolder;
        private DiscountItemHolder discountHolder;

        public ScheduleAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(Schedule[] schedules , DiscountInfo[] discounts) {
            mData = schedules;
            mDiscountInfo = discounts;
            viewDiscountCount = (mDiscountInfo == null ? 0 : mDiscountInfo.length);
            notifyDataSetChanged();
        }

        public Object getItem(int position) {
            if ((mDiscountInfo == null && mData == null) || position < 0 || position >= getItemCount() ) {
                return null;
            }
            viewType = getItemViewType(position);
            if (VIEW_TYPE_DISCOUNT_ITEM == viewType) {
                return mDiscountInfo[position];
            } else if(VIEW_TYPE_SCHEDULE_ITEM == viewType) {
                return mData[position  - viewDiscountCount];
            }
            return null;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder mHolder = null;
            switch (viewType) {
                case VIEW_TYPE_DISCOUNT_ITEM:
                    mHolder = new DiscountItemHolder(mInflater.inflate(R.layout.movie_discount_item, parent, false));
                    break;
                case VIEW_TYPE_SCHEDULE_ITEM:
                    mHolder = new ItemHolder(mInflater.inflate(R.layout.movie_schedule_item, parent, false));
                    break;
            }
            return mHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            viewType = getItemViewType(position);
            if (viewType == VIEW_TYPE_DISCOUNT_ITEM && holder instanceof DiscountItemHolder) { //discount item
                DiscountInfo discountinfo = mDiscountInfo[position];
                discountHolder = (DiscountItemHolder) holder;
                discountHolder.discountDes.setText(discountinfo.discount_des);
                discountHolder.discountIntroduce.setText(discountinfo.discount_introduce);
            } else if (viewType == VIEW_TYPE_SCHEDULE_ITEM && holder instanceof ItemHolder) {  // schedule Item
                Schedule schedule = mData[position -  viewDiscountCount];
                scheduleHolder = (ItemHolder) holder;
                scheduleHolder.startTimeView.setText(schedule.time);
                scheduleHolder.endTimeView.setText(getEndTimeStr(schedule.time));
                scheduleHolder.languageView.setText(schedule.language);
                scheduleHolder.roomNameView.setText(schedule.roomname);
                scheduleHolder.discountView.setText(MovieTicketHelper.getTicketPriceStr(schedule.discount, getString(R.string.movie_ticket_price_unit), 10));
                if (!TextUtils.isEmpty(schedule.discount_des)) {
                    scheduleHolder.priceView.setText(" " + schedule.discount_des + " ");
                    scheduleHolder.priceView.setVisibility(View.VISIBLE);
                    scheduleHolder.priceView.setTextAppearance(R.style.MovieScheduleTextItemDiscountStyle);
                    scheduleHolder.priceView.setBackgroundResource(R.drawable.movie_special_orange_bg);
                    scheduleHolder.priceView.getPaint().setStrikeThruText(false);
                } else if (schedule.discount < schedule.market_price) {
                    scheduleHolder.priceView.setText(" " + schedule.market_price + " ");
                    scheduleHolder.priceView.setTextAppearance(R.style.MovieScheduleTextItemSumaryStyle);
                    scheduleHolder.priceView.getPaint().setStrikeThruText(true);
                    scheduleHolder.priceView.setVisibility(View.VISIBLE);
                    scheduleHolder.priceView.setBackgroundResource(0);
                } else {
                    scheduleHolder.priceView.setVisibility(View.GONE);
                }

                if (isStopSale(mDate, schedule.time, schedule.stop_time)) {
                    scheduleHolder.itemView.setEnabled(false);
                    scheduleHolder.seatSelectButton.setText(R.string.movie_stop_sale);
                } else {
                    scheduleHolder.itemView.setEnabled(true);
                    scheduleHolder.itemView.setTag(schedule);
                    scheduleHolder.seatSelectButton.setText(R.string.movie_select_seat_order);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position >= getItemCount()) return VIEW_TYPE_INVALID;

            if (position < viewDiscountCount) return VIEW_TYPE_DISCOUNT_ITEM;

            return VIEW_TYPE_SCHEDULE_ITEM;
        }

        private boolean isStopSale(String date, String time, String stopTime) {
            Date showDate = DateUtils.parseDate(date, "yyyyMMdd");
            Date showTime = DateUtils.parseDate(time, "HH:mm");
            int stop = -1;
            try {
                stop = Integer.parseInt(stopTime);
            } catch (Exception e) {
            }
            if (showDate == null || showTime == null || stop == -1) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(showDate);
            calendar.set(Calendar.HOUR_OF_DAY, showTime.getHours());
            calendar.set(Calendar.MINUTE, showTime.getMinutes());
            Calendar current = Calendar.getInstance();
            current.add(Calendar.MINUTE, stop);
            return !current.before(calendar);
        }

        @Override
        public int getItemCount() {
            return viewDiscountCount + getSheduleCount();
        }

        private int getSheduleCount(){
            return mData == null ? 0 : mData.length;
        }

        private String getEndTimeStr(String startTime) {
            if (TextUtils.isEmpty(startTime) || mMovieLongs == -1) {
                return "";
            }
            Date date = DateUtils.parseDate(startTime, "HH:mm");
            StringBuilder builder = new StringBuilder();
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MINUTE, mMovieLongs);
                builder.append(DateUtils.formatDate(calendar.getTime(), "HH:mm"));
                builder.append(getString(R.string.movie_show_finished));
                return builder.toString();
            }
            return "";
        }
    }
}
