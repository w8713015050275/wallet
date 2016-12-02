package com.letv.walletbiz.movie.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.pay.Constants;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieOrder;
import com.letv.walletbiz.movie.beans.MovieProduct;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MovieOrderDetailTask;
import com.letv.walletbiz.movie.utils.MoviePriorityExecutorHelper;
import com.letv.walletbiz.movie.utils.MovieTicketHelper;

import org.xutils.common.task.PriorityExecutor;

import java.util.Date;


/**
 * Created by liuliang on 16-3-2.
 */
public class MovieOrderDetailActivity extends BaseWalletFragmentActivity implements View.OnClickListener {

    private TextView mMovieNameTextView;
    private Button mTicketStatusButton;
    private TextView mCinemaNameTextView;
    private TextView mRoomNameTextView;
    private TextView mTimeTextView;
    private TextView mSeateInfoTextView;
    private TextView mRedeemCodeLabelTextView;
    private TextView mRedeemCodeTextView;
    private TextView mTicketCodeDesc;
    private View mTicketCodeContainer;
    private View mSerialNumContainer;
    private TextView mSerialNumLabelTextView;
    private TextView mSerialNumTextView;
    private TextView mContactInfoCinemaNameTextView;
    private TextView mContactInfoCinemaAddressTextView;
    private ImageView mContactInfoTeleImageView;
    private TextView mOrderNumTextView , mOrderPriceTextView, mOrderDateTextView;
    private TextView mWeipiaoTeleTextView;

    private PriorityExecutor mExecutor;
    private MovieOrderDetailTask mTask;

    private String mOrderNum;

    private static final int MSG_LOAD_DATA_FINISHED = 1;

    private MovieOrder mMovieOrder;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_DATA_FINISHED:
                    hideLoadingView();
                    int erroCode = msg.arg1;
                    MovieOrder order = (MovieOrder) msg.obj;
                    if (erroCode == MovieCommonCallback.NO_ERROR) {
                        mMovieOrder = order;
                        int status = order.getTicketStatus();
                        if (status == MovieOrder.MOVIE_TICKET_STATUS_UNPAY) {
                            MovieProduct product = new MovieProduct(order, order.movie_name, order.cinema_name, order.ticket_info.date, order.ticket_info.time, order.ticket_info.roomname);
                            product.pay(MovieOrderDetailActivity.this, true);
                            MovieOrderDetailActivity.this.finish();
                        } else {
                            updateView(order);
                        }
                    } else if (erroCode == MovieCommonCallback.ERROR_NETWORK) {
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(mRetryClickListener);
                        mMovieOrder = null;
                    }
                    break;
            }
        }
    };

    private MovieCommonCallback mCallback = new MovieCommonCallback() {

        @Override
        public void onLoadFinished(Object result, int errorCode) {
            mTask = null;
            Message message = mHandler.obtainMessage(MSG_LOAD_DATA_FINISHED);
            message.arg1 = errorCode;
            message.obj = result;
            message.sendToTarget();
        }
    };

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            loadData();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setContentView(R.layout.movie_order_detail_layout);
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri == null) {
                mOrderNum = intent.getStringExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM);
            } else {
                String encodedQuery = uri.getEncodedQuery();
                if (!TextUtils.isEmpty(encodedQuery) && encodedQuery.contains(Constants.INFO_PARAM.ORDER_NO)) {
                    mOrderNum = intent.getData().getQueryParameter(Constants.INFO_PARAM.ORDER_NO);
                }
                if (TextUtils.isEmpty(mOrderNum)) {
                    finish();
                    return;
                }
            }
        }

        mExecutor = MoviePriorityExecutorHelper.getPriorityExecutor();
        initView();
        loadData();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && mMovieOrder == null) {
            loadData();
        }
    }

    @Override
    public void onClick(View v) {
        if (mMovieOrder == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.contact_info_tele:
                if (PermissionCheckHelper.checkCallPermission(this, 0) == PermissionCheckHelper.PERMISSION_ALLOWED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mMovieOrder.cinema_tele));
                    startActivity(intent);
                }
                break;
            case R.id.tele_weipiao:
                if (PermissionCheckHelper.checkCallPermission(this, 0) == PermissionCheckHelper.PERMISSION_ALLOWED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mWeipiaoTeleTextView.getText()));
                    startActivity(intent);
                }
                break;
            case R.id.contact_info_cinema_address:
                try {
                    Uri uri = Uri.parse("geo:" + mMovieOrder.latitude + "," + mMovieOrder.longitude + "?q=" + Uri.encode(mMovieOrder.cinema_addr));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
                } catch (Exception e) {
                }
                try {
                    Uri uri = Uri.parse("http://map.baidu.com/mobile/webapp/search/search/qt=s&searchFlag=bigBox&version=5&exptype=dep&c=undefined&wd=" + Uri.encode(mMovieOrder.cinema_addr));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e1) {
                }
                break;
        }
    }

    private void initView() {
        mMovieNameTextView = (TextView) findViewById(R.id.movie_name);
        mTicketStatusButton = (Button) findViewById(R.id.ticket_status);
        mCinemaNameTextView = (TextView) findViewById(R.id.cinema_name);
        mRoomNameTextView = (TextView) findViewById(R.id.room_name);
        mTimeTextView = (TextView) findViewById(R.id.time);
        mSeateInfoTextView = (TextView) findViewById(R.id.seat_info);
        mTicketCodeDesc = (TextView) findViewById(R.id.ticket_code_desc);
        mTicketCodeContainer = findViewById(R.id.ticket_code_container);
        mSerialNumContainer = findViewById(R.id.ticket_serial_num_container);
        mSerialNumTextView = (TextView) findViewById(R.id.ticket_serial_number);
        mSerialNumLabelTextView = (TextView) findViewById(R.id.ticket_serial_number_label);
        mRedeemCodeTextView = (TextView) findViewById(R.id.ticket_redeem_code);
        mRedeemCodeLabelTextView = (TextView) findViewById(R.id.ticket_redeem_code_label);
        mContactInfoCinemaNameTextView = (TextView) findViewById(R.id.contact_info_cinema_name);
        mContactInfoCinemaAddressTextView = (TextView) findViewById(R.id.contact_info_cinema_address);
        mContactInfoTeleImageView = (ImageView) findViewById(R.id.contact_info_tele);
        mContactInfoTeleImageView.setOnClickListener(this);
        mOrderNumTextView = (TextView) findViewById(R.id.ticket_order_num);
        mOrderPriceTextView = (TextView) findViewById(R.id.ticket_order_price);
        mOrderDateTextView = (TextView) findViewById(R.id.ticket_order_date);
        mWeipiaoTeleTextView = (TextView) findViewById(R.id.tele_weipiao);
        mWeipiaoTeleTextView.setOnClickListener(this);
    }

    private void updateView(MovieOrder order) {
        if (order == null) {
            return;
        }
        mMovieNameTextView.setText(order.movie_name);
        int status = order.getTicketStatus();
        mTicketStatusButton.setText(order.getTicketStatusResId(status));
        mTicketStatusButton.setTextColor(getColor(order.getTicketStatusColorResId(status)));
        mTicketStatusButton.setBackground(order.getTicketStatusBgResId(status, this));
        mCinemaNameTextView.setText(order.cinema_name);
        if (order.ticket_info != null) {
            mRoomNameTextView.setText(order.ticket_info.roomname);
            mTimeTextView.setText(getDateStr(order.ticket_info.date, order.ticket_info.time));
            mSeateInfoTextView.setText(MovieTicketHelper.getSeatDescStr(this, order.ticket_info.seat));
        }
        String[] array = getTicketCode(order.code);
        if (array != null) {
            mTicketCodeDesc.setVisibility(View.VISIBLE);
            mTicketCodeContainer.setVisibility(View.VISIBLE);
            if (array.length == 2) {
                mSerialNumTextView.setText(array[0]);
                mSerialNumTextView.setTextColor(getColor(order.getTicketRedeemColorResId(status)));
                mSerialNumLabelTextView.setTextColor(getColor(order.getTicketRedeemColorResId(status)));
                mRedeemCodeTextView.setText(array[1]);
                mSerialNumContainer.setVisibility(View.VISIBLE);
            } else if (array.length == 1) {
                mSerialNumContainer.setVisibility(View.GONE);
                mRedeemCodeTextView.setText(array[0]);
            }
            mRedeemCodeTextView.setTextColor(getColor(order.getTicketRedeemColorResId(status)));
            mRedeemCodeLabelTextView.setTextColor(getColor(order.getTicketRedeemColorResId(status)));
        } else {
            mTicketCodeDesc.setVisibility(View.GONE);
            mTicketCodeContainer.setVisibility(View.GONE);
        }
        mContactInfoCinemaNameTextView.setText(order.cinema_name);
        mContactInfoCinemaAddressTextView.setText(order.cinema_addr);
        mContactInfoCinemaAddressTextView.setOnClickListener(this);
        mOrderNumTextView.setText(order.third_no);
        mOrderPriceTextView.setText(order.price + getString(R.string.movie_ticket_price_unit));
        mOrderDateTextView.setText(DateUtils.formatDate(new Date(order.add_time * 1000), "yyyy-MM-dd HH:mm"));
    }

    private void loadData() {
        if (isNetworkAvailable()) {
            if (mTask == null) {
                showLoadingView();
                mTask = new MovieOrderDetailTask(this, mOrderNum, mCallback);
                mExecutor.execute(mTask);
            }
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    private CharSequence getDateStr(String date, String time) {
        String dateStr = DateUtils.convertPatternForDate(date, "yyyyMMdd", "yyyy-MM-dd");
        return dateStr + "  " + time;
    }

    private String[] getTicketCode(String code) {
        if (TextUtils.isEmpty(code)) {
            return null;
        }
        return code.split("\\|");
    }
}
