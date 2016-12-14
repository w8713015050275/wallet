package com.letv.walletbiz.movie.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.fragment.BaseOrderListFragment;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderListBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderRequestBean;
import com.letv.walletbiz.base.view.OrderListViewAdapter;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.beans.MovieOrder;
import com.letv.walletbiz.movie.beans.MovieProduct;
import com.letv.walletbiz.movie.ui.TimerTextView;
import com.letv.walletbiz.movie.utils.DrawableUtils;
import com.letv.walletbiz.movie.utils.MovieSearchHelper;
import com.letv.walletbiz.movie.utils.MovieTicketHelper;

import org.xutils.xmain;

import java.lang.reflect.Type;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 15-12-30.
 */
public class MovieOrderListFragment extends BaseOrderListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        OrderListViewAdapter adapter = getOrderListAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_movie_order_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            MovieSearchHelper.startSearch(getContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected OrderRequestBean getRequestBean() {
        String uToken = AccountHelper.getInstance().getToken(getContext());
        OrderRequestBean requestBean = new OrderRequestBean(MovieTicketConstant.MOVIE_PATH_ORDER_LIST);
        requestBean.addQueryStringParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, uToken);
        return requestBean;
    }

    @Override
    protected Type getResponseType() {
        TypeToken typeToken = new TypeToken<BaseResponse<OrderListBaseBean<MovieOrder>>>() {};
        return typeToken.getType();
    }

    @Override
    public OrderListViewAdapter.BaseOrderViewHolder getViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.movie_order_list_item, parent, false);
        return new MovieOrderListHolder(v);
    }

    @Override
    public OrderListViewAdapter.BaseHeaderViewHolder getHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    protected RecyclerItemClickListener getRecycleritemClickListener() {
        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == RecyclerView.NO_POSITION)
                    return;
                OrderListViewAdapter adapater = getOrderListAdapter();
                if (adapater != null) {
                    MovieOrder order = (MovieOrder) adapater.getOrderItem(position);
                    if (order == null) {
                        return;
                    }
                    int status = order.getTicketStatus();
                    if (status == MovieOrder.MOVIE_TICKET_STATUS_UNPAY) {
                        MovieProduct product = new MovieProduct(order, order.movie_name, order.cinema_name, order.ticket_info.date, order.ticket_info.time, order.ticket_info.roomname);
                        product.pay(getContext(), true);
                    } else if (status == MovieOrder.MOVIE_TICKET_STATUS_OVERTIME) {
                        Toast.makeText(getContext(), R.string.movie_toast_order_overtime, Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(getContext(), MovieOrderDetailActivity.class);
                        intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM, order.order_no);
                        startActivity(intent);
                    }
                }
            }
        });
        return itemClickListener;
    }

    class MovieOrderListHolder extends OrderListViewAdapter.BaseOrderViewHolder {

        public TextView mCinemaNameTextView;
        public TimerTextView mRemainTimeTextView;
        public TextView mTimeTextView;

        public RelativeLayout mMovieTicketContainer;
        public ImageView mPosterImageView;

        public LinearLayout mMovieMsgContainer;
        public TextView mPriceTextView;
        public TextView mTicketAmountTextView;
        public TextView mMovieNameTextView;

        public Button mTicketStatusButton;
        public TextView mTicketDescTextView;

        private TimerTextView.OnTimerFinishedListener mListener = new TimerTextView.OnTimerFinishedListener() {
            @Override
            public void onTimerFinished(View view) {
                view.setVisibility(View.GONE);
                mTicketStatusButton.setText(R.string.movie_ticket_status_overtime);
                int statusTextColor = getResources().getColor(R.color.movie_btn_order_status_shown, getContext().getTheme());
                mTicketStatusButton.setTextColor(statusTextColor);
                mTicketStatusButton.setBackground(DrawableUtils.getMovieGreyBtnDrawable(getContext()));
                mPriceTextView.setTextColor(statusTextColor);
                mTicketStatusButton.setEnabled(false);
            }
        };

        public MovieOrderListHolder(View v) {
            super(v);
            mCinemaNameTextView = (TextView) v.findViewById(R.id.cinema_name);

            mRemainTimeTextView = (TimerTextView) v.findViewById(R.id.remain_time);
            mRemainTimeTextView.setTimeTextFormatter(getString(R.string.movie_order_list_pay_remain_time));
            mRemainTimeTextView.setOnTimerFinishedListener(mListener);

            mMovieTicketContainer = (RelativeLayout) v.findViewById(R.id.movie_ticket_container);

            mPosterImageView = (ImageView) v.findViewById(R.id.poster_url);
            mTimeTextView = (TextView) v.findViewById(R.id.time);

            mMovieMsgContainer = (LinearLayout) v.findViewById(R.id.movie_msg_container);
            mPriceTextView = (TextView) v.findViewById(R.id.price);
            mTicketAmountTextView = (TextView) v.findViewById(R.id.ticket_amount);
            mMovieNameTextView = (TextView) v.findViewById(R.id.movie_name);

            mTicketStatusButton = (Button) v.findViewById(R.id.ticket_status);
            mTicketDescTextView = (TextView) v.findViewById(R.id.ticket_desc);
        }

        @Override
        protected void setData(OrderBaseBean orderBaseBean, int position) {
            MovieOrder order = (MovieOrder) orderBaseBean;
            mCinemaNameTextView.setText(order.cinema_name);
            xmain.image().bind(mPosterImageView, order.poster_url, ImageOptionsHelper.getDefaltImageLoaderOptions());
            if (order.ticket_info != null) {
                mTimeTextView.setText(getTicketTime(order.ticket_info.date, order.ticket_info.time));
                mTicketAmountTextView.setText(MovieTicketHelper.getTicketAmountStr(getContext(), order.ticket_info.num));
                mTicketDescTextView.setText(getTicketDesc(order.ticket_info.roomname, order.ticket_info.seat));
            }

            mMovieNameTextView.setText(order.movie_name);
            int status = order.getTicketStatus();
            if (status == MovieOrder.MOVIE_TICKET_STATUS_UNPAY) {
                mRemainTimeTextView.setVisibility(View.VISIBLE);
                mRemainTimeTextView.setRemainTime(order.lock_expire_time - Math.max(0, System.currentTimeMillis() / 1000 - order.lock_time));
            } else {
                mRemainTimeTextView.setVisibility(View.GONE);
            }
            int statusTextColor = getResources().getColor(order.getTicketStatusColorResId(status), getContext().getTheme());
            mPriceTextView.setText(MovieTicketHelper.getTicketPriceStr(order.price, getString(R.string.movie_ticket_price_unit), 10));
            mPriceTextView.setTextColor(statusTextColor);
            mTicketStatusButton.setText(order.getTicketStatusResId(status));
            mTicketStatusButton.setTextColor(statusTextColor);
            mTicketStatusButton.setBackground(order.getTicketStatusBgResId(status, getContext()));

            int width = DensityUtils.getScreenWidth();
            width -= mMovieTicketContainer.getPaddingLeft() + mMovieTicketContainer.getPaddingRight();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPosterImageView.getLayoutParams();
            if (params != null) {
                width -= params.width;
                width -= params.leftMargin + params.rightMargin;
            }
            mMovieNameTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mTicketAmountTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mPriceTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int requiredWidth = mMovieNameTextView.getMeasuredWidth() + mTicketAmountTextView.getMeasuredWidth() + mPriceTextView.getMeasuredWidth();
            LinearLayout.LayoutParams movieNameParam = (LinearLayout.LayoutParams) mMovieNameTextView.getLayoutParams();
            LinearLayout.LayoutParams ticketAmountParam = (LinearLayout.LayoutParams) mTicketAmountTextView.getLayoutParams();
            LinearLayout.LayoutParams priceParam = (LinearLayout.LayoutParams) mPriceTextView.getLayoutParams();
            if (movieNameParam != null) {
                requiredWidth += movieNameParam.leftMargin + movieNameParam.rightMargin;
            }
            if (ticketAmountParam != null) {
                requiredWidth += ticketAmountParam.leftMargin + ticketAmountParam.rightMargin;
            }
            if (priceParam != null) {
                requiredWidth += priceParam.leftMargin + priceParam.rightMargin;
            }
            if (requiredWidth > width) {
                if (movieNameParam == null) {
                    movieNameParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                if (movieNameParam.weight != 1) {
                    movieNameParam.weight = 1;
                    mMovieNameTextView.setLayoutParams(movieNameParam);
                    if (ticketAmountParam != null) {
                        ticketAmountParam.weight = 0;
                        mTicketAmountTextView.setLayoutParams(ticketAmountParam);
                    }
                }
            } else {
                if (ticketAmountParam == null) {
                    ticketAmountParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                if (ticketAmountParam.weight != 1) {
                    ticketAmountParam.weight = 1;
                    mTicketAmountTextView.setLayoutParams(ticketAmountParam);
                    if (movieNameParam != null) {
                        movieNameParam.weight = 0;
                        mMovieNameTextView.setLayoutParams(movieNameParam);
                    }
                }
            }

        }

        private String getTicketTime(String date, String time) {
            String dateStr = DateUtils.convertPatternForDate(date, "yyyyMMdd", "MM-dd");
            return dateStr + "  " + time;
        }

        private String getTicketDesc(String roomName, String seat) {
            Context context = getContext();
            if (context == null) {
                return "";
            }
            CharSequence seatDesc = MovieTicketHelper.getSeatDescStr(context, seat);
            if (TextUtils.isEmpty(roomName) || TextUtils.isEmpty(seatDesc)) {
                return "";
            }
            return roomName + "  " + seatDesc;
        }
    }
}
