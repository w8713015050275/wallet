package com.letv.walletbiz.movie.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.http.client.RspConstants;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DateUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.CinemaSchedule;
import com.letv.walletbiz.movie.beans.LockSeatOrder;
import com.letv.walletbiz.movie.beans.MovieProduct;
import com.letv.walletbiz.movie.beans.MovieSeatColumnInfo;
import com.letv.walletbiz.movie.beans.MovieSeatList;
import com.letv.walletbiz.movie.beans.MovieSeatRowInfo;
import com.letv.walletbiz.movie.beans.MovieSoldInfo;
import com.letv.walletbiz.movie.ui.MovieSeatInfoV;
import com.letv.walletbiz.movie.ui.MovieSeatThumbV;
import com.letv.walletbiz.movie.ui.MovieSeatV;
import com.letv.walletbiz.movie.utils.MovieSeatDataManager;

import org.xutils.xmain;

import java.util.Date;
import java.util.List;

/**
 * Created by changjiajie on 16-2-1.
 */
public class MovieSeatActivity extends AccountBaseActivity {

    private static final String CACHEDATA = "cacheData";
    /**
     * 电影id
     */
    private long mMovieId;
    /**
     * 影院id
     */
    private int mCinemaId;
    /**
     * 影厅id
     */
    private String mRoomId;
    /**
     * 影厅Name
     */
    private String mRoomName;
    /**
     * 排期
     */
    private String mpid;
    /**
     * 影院名称
     */
    private String mCinemaName;
    /**
     * 电影名称
     */
    private String mMovieName;
    private String mMovieData;
    private String mMovieTime;

    private TextView mMovieNameV;
    private TextView mMovieDataV;
    private TextView mMovieTimeV;
    private TextView mMovieScreenInfoTv;
    private TextView mBtnBuyTicket;
    private RelativeLayout mMovieSeatV;
    private RelativeLayout mMovieSeatVParentsRl;
    private LinearLayout mMoiveSeatMainll;
    /**
     * 未选座前显示
     */
    private LinearLayout mSeatStateInfoV;
    /**
     * 选座后显示
     */
    private LinearLayout mSeatCheckedV;
    /**
     * 放置选择的座位View
     */
    private LinearLayout mSeatCheckedInfoV;

    /**
     * 选座View
     */
    private MovieSeatV mSeatV;
    private MovieSeatThumbV mSeatThumbV;
    private MovieSeatDataManager mSeatDataManager;
    private ProgressDialog mProgressDialog;
    private SeatInfoAsyncTask mSeatInfoTask;
    private OrderAsynTask mOrderTask;
    private boolean isFirst = true;
    private LeBottomSheet mMovieDateAlertDialog;

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            querySeatInfo();
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_seat_main);
        registerNetWorkReceiver();
        getIntentData();
        if (savedInstanceState != null) {
            mSeatDataManager = (MovieSeatDataManager) savedInstanceState.getSerializable(CACHEDATA);
        }
        if (mSeatDataManager == null) {
            mSeatDataManager = new MovieSeatDataManager();
        }
        initV();
        List<MovieSoldInfo> mCheckSeatList = mSeatDataManager.getCheckedSeatList();
        if (mCheckSeatList != null) {
            for (MovieSoldInfo checkSeat : mCheckSeatList) {
                new SeatOnClickListener().addSeat(checkSeat.area, checkSeat.mRowNum, checkSeat.mColumnNum, checkSeat.desc, checkSeat.n);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!AccountHelper.getInstance().isLogin(this)) {
            showErrorBlankPage(BlankPage.STATE_NO_LOGIN, null);
            return;
        }
        if (!isNetworkAvailable()) {
            showErrorBlankPage(BlankPage.STATE_NO_NETWORK, null);
            return;
        }
        if (isFirst) {
            isFirst = false;
            showLoadingView();
            querySeatInfo();
            return;
        }
        hideBlankPage();
        querySeatInfo();
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (!isNetworkAvailable && mMovieSeatV == null)
            return;
        if (isBlankPageVisible()) {
            checkAsyncTask();
            mSeatInfoTask.execute();
        }
    }

    public void buyTicket(View view) {
        if (!isNetworkAvailable()) {
            promptNoNetWork();
            return;
        }
        if (checkSeatSeparatedState(mSeatV.getSeatInfoList(), mSeatDataManager.getCheckedSeatList())) {
            Toast.makeText(MovieSeatActivity.this, R.string.movie_seat_separated_seat_checked_attention, Toast.LENGTH_SHORT).show();
        } else {
            if (mOrderTask == null) {
                Action.uploadBuy(Action.MOVIE_SELECT_SEAT_PAY_CLICK, String.valueOf(mMovieId), String.valueOf(mCinemaId));
                showProgressDialog();
                List<MovieSoldInfo> list = mSeatDataManager.getCheckedSeatList();
                StringBuilder builder = new StringBuilder();
                for (MovieSoldInfo info : list) {
                    builder.append(info.desc);
                    builder.append(':');
                    builder.append(info.n);
                    builder.append('|');
                }
                builder.deleteCharAt(builder.length() - 1);
                mOrderTask = new OrderAsynTask(MovieSeatActivity.this, mpid, builder.toString());
                mOrderTask.execute();
            }
        }
        /**
         * 购买 ，跳转支付页面
         */
    }

    public int getSeatVParentsHeight() {
        if (mMovieSeatVParentsRl == null) return 0;
        return mMovieSeatVParentsRl.getHeight();
    }

    /**
     * 正常返回false,异常返回true
     *
     * @return
     */
    private boolean checkSeatSeparatedState(MovieSeatRowInfo[] seatInfoList, List<MovieSoldInfo> checkedSeatList) {
        boolean exception = false;
        for (MovieSoldInfo checkedSeatInfo : checkedSeatList) {
            MovieSeatRowInfo seatInfo = seatInfoList[checkedSeatInfo.mRowNum - 1];
            if (seatInfo.area.equals(checkedSeatInfo.area) && seatInfo.desc.equals(checkedSeatInfo.desc)) {
                int columnIndex = checkedSeatInfo.mColumnNum - 1;
                MovieSeatColumnInfo[] columnInfos = seatInfo.detail;
                //向左检查
                boolean[] leftException = checkSeatAround(columnInfos, columnIndex, -1);
                //左边正常，向右检查
                boolean[] rightException = checkSeatAround(columnInfos, columnIndex, 1);
                if (leftException[0]) {
                    // rightException[0] 左边有异常且右边有异常
                    // (!rightException[0] && !rightException[1]) 右边沒有异常及右边若没出现过道/已售/最后一列情况（rightException[1]==false）选的座位有异常
                    if (rightException[0] || (!rightException[0] && !rightException[1])) {
                        exception = true;
                        break;
                    }
                } else {
                    //左边没有异常,右边异常及左边若没出现过道/已售/第一列情况（leftException[1]==false）选的座位有异常
                    if (!leftException[1] && rightException[0]) {
                        exception = true;
                        break;
                    }
                }
            }
        }
        return exception;
    }

    /**
     * 左右两边都有空白区域及空白区域为1时
     * 正常返回false,异常返回true
     *
     * @param columnInfos
     * @param index
     * @param orientation
     * @return
     */
    private boolean[] checkSeatAround(MovieSeatColumnInfo[] columnInfos, int index, int orientation) {
        boolean leftException[] = new boolean[2];
        //第一个参数表示是否出现有隔座情况
        leftException[0] = false;
        //第二个参数表示当没有出现隔座情况,相邻位置是否有过道/已售或为第一列或者最后一列
        leftException[1] = false;
        int nativeIndex = index + orientation;
        int lisSize = columnInfos.length;
        if (nativeIndex < 0 || nativeIndex >= lisSize) {
            leftException[0] = false;
            leftException[1] = true;
            return leftException;
        }
        //第一个
        MovieSeatColumnInfo columnInfo = columnInfos[nativeIndex];
        if (columnInfo == null || columnInfo.n.equals(MovieSeatColumnInfo.AISLE_IDENTIFY)) {
            leftException[0] = false;
            leftException[1] = true;
            return leftException;
        }
        switch (columnInfo.state) {
            case MovieSeatColumnInfo.SOLDSEATSTATE:
                leftException[0] = false;
                leftException[1] = true;
                return leftException;
            case MovieSeatColumnInfo.CHECKEDSTATE:
                return checkSeatAround(columnInfos, index + orientation, orientation);
            default: {
                //第二个
                nativeIndex = nativeIndex + orientation;
                if (nativeIndex < 0 || nativeIndex >= lisSize) {
                    leftException[0] = true;
                    return leftException;
                }
                columnInfo = columnInfos[nativeIndex];
                if (columnInfo == null || columnInfo.n.equals(MovieSeatColumnInfo.AISLE_IDENTIFY)) {
                    leftException[0] = true;
                    return leftException;
                }
                switch (columnInfo.state) {
                    case MovieSeatColumnInfo.CHECKEDSTATE:
                        leftException[0] = true;
                        return leftException;
                    case MovieSeatColumnInfo.SOLDSEATSTATE:
                        leftException[0] = true;
                        return leftException;
                    default: {
                        leftException[0] = false;
                        return leftException;
                    }
                }
            }
        }
    }

    private void getIntentData() {
        mMovieId = getIntent().getLongExtra(MovieTicketConstant.EXTRA_MOVIE_ID, -1);
        mCinemaId = getIntent().getIntExtra(MovieTicketConstant.EXTRA_CINEMA_ID, -1);
        mCinemaName = getIntent().getStringExtra(MovieTicketConstant.EXTRA_CINEMA_NAME);
        mMovieName = getIntent().getStringExtra(MovieTicketConstant.EXTRA_MOVIE_NAME);
        mMovieData = getIntent().getStringExtra(MovieTicketConstant.EXTRA_DATE);
        CinemaSchedule.Schedule schedule = getIntent().getParcelableExtra(MovieTicketConstant.EXTRA_MOVIE_SCHEDULE);
        mpid = schedule.mpid;
        mRoomId = schedule.roomid;
        mRoomName = schedule.roomname;
        mMovieTime = schedule.time;
    }

    private String getDateFormatStr(String date) {
        Date dateObj = DateUtils.parseDate(date, "yyyyMMdd");
        String result = null;
        if (DateUtils.isToday(dateObj)) {
            result = DateUtils.formatDate(dateObj, getString(R.string.movie_seat_data_today));
        } else if (DateUtils.isTomorrow(dateObj)) {
            result = DateUtils.formatDate(dateObj, getString(R.string.movie_seat_data_tomorrow));
        } else {
            result = DateUtils.formatDate(dateObj, getString(R.string.movie_seat_data_normal));
        }
        return result;
    }

    private String getDateFormatStr_AlertDialog(String date) {
        Date dateObj = DateUtils.parseDate(date, "yyyyMMdd");
        String result = null;
        if (DateUtils.isToday(dateObj)) {
            result = DateUtils.formatDate(dateObj, getString(R.string.movie_seat_data_by_alert_today));
        } else if (DateUtils.isTomorrow(dateObj)) {
            result = DateUtils.formatDate(dateObj, getString(R.string.movie_seat_data_by_alert_tomorrow));
        } else {
            result = DateUtils.formatDate(dateObj, getString(R.string.movie_seat_data_by_alert_normal));
        }
        return result;
    }

    private void initV() {
        initLoadingDialog();
        setTitle(mCinemaName);
        findViewById();
        mMovieNameV.setText(mMovieName);
        mMovieDataV.setText(getDateFormatStr(mMovieData));
        mMovieTimeV.setText(mMovieTime);
        mMovieScreenInfoTv.setText(mRoomName + getResources().getString(R.string.movie_seat_room_info_tv));
        mSeatV = new MovieSeatV(MovieSeatActivity.this);
        mSeatV.setOnClickListener(new SeatOnClickListener());
        if (!DateUtils.isToday(DateUtils.parseDate(mMovieData, "yyyyMMdd"))) {
            showMovieDateAlertDialog(this);
        }

    }

    class SeatOnClickListener implements OnSeatClickListener {

        @Override
        public boolean addSeat(String area, int rowNum, int columnNum, String desc, String n) {
            /**
             * 选择的座位信息View
             */
            MovieSeatInfoV mSeatInfoV = new MovieSeatInfoV(MovieSeatActivity.this);
            mSeatInfoV.setSeatCancelClickListener(this);
            mSeatInfoV.setInfo(area, rowNum, columnNum, desc, n);
            mSeatCheckedInfoV.addView(mSeatInfoV);
            if (mSeatCheckedInfoV.getChildCount() > 0) {
                if (mSeatStateInfoV.getVisibility() == View.VISIBLE) {
                    mSeatStateInfoV.setVisibility(View.GONE);
                }
                if (mSeatCheckedV.getVisibility() == View.GONE) {
                    mSeatCheckedV.setVisibility(View.VISIBLE);
                    mSeatV.setParentsViewHeight(getSeatVParentsHeight(), false);
                }
            }
            return false;
        }

        @Override
        public boolean cancelSeat(String area, int rowNum, int columnNum, String desc, String n) {
            for (int i = 0; i < mSeatCheckedInfoV.getChildCount(); i++) {
                MovieSeatInfoV v = (MovieSeatInfoV) mSeatCheckedInfoV.getChildAt(i);
                if (v.getTag().equals(MovieSeatInfoV.creatTag(area, desc, n))) {
                    cancelSeat(v, area, rowNum, columnNum, desc, n);
                }
            }
            return false;
        }

        @Override
        public boolean cancelSeat(View view, String area, int rowNum, int columnNum, String desc, String n) {
            if (mSeatCheckedInfoV != null) {
                mSeatCheckedInfoV.removeView(view);
            }
            if (mSeatCheckedInfoV.getChildCount() <= 0) {
                mSeatCheckedV.setVisibility(View.GONE);
                mSeatStateInfoV.setVisibility(View.VISIBLE);
                mSeatV.setParentsViewHeight(getSeatVParentsHeight(), false);
            }
            if (mSeatV != null) {
                mSeatV.deletCheckedSeatState(area, rowNum, columnNum);
            }
            return false;
        }

        @Override
        public MovieSeatDataManager getDataManager() {
            return mSeatDataManager;
        }
    }

    private void findViewById() {
        mMoiveSeatMainll = (LinearLayout) findViewById(R.id.moive_seat_main_ll);
        mMovieNameV = (TextView) findViewById(R.id.movie_name);
        mMovieDataV = (TextView) findViewById(R.id.movie_data_info);
        mMovieTimeV = (TextView) findViewById(R.id.movie_time_info);
        mMovieScreenInfoTv = (TextView) findViewById(R.id.movie_screen_info_tv);
        mBtnBuyTicket = (TextView) findViewById(R.id.btn_buy_ticket);
        mMovieSeatV = (RelativeLayout) findViewById(R.id.movie_seat_v);
        mMovieSeatVParentsRl = (RelativeLayout) findViewById(R.id.movie_seat_v_parents_rl);
        mSeatThumbV = (MovieSeatThumbV) findViewById(R.id.movie_seat_thumb_v);
        mSeatThumbV.initV();
        mSeatStateInfoV = (LinearLayout) findViewById(R.id.movie_seat_state_info_v);
        mSeatCheckedV = (LinearLayout) findViewById(R.id.movie_seat_checked_v);
        mSeatCheckedInfoV = (LinearLayout) findViewById(R.id.movie_seat_checkedinfo_v);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putSerializable(CACHEDATA, mSeatDataManager);
        }
    }

    @Override
    protected void onDestroy() {
        isFirst = true;
        if (mSeatInfoTask != null) {
            mSeatInfoTask.cancel(true);
            mSeatInfoTask = null;
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void hideSeatMainV() {
        if (mMoiveSeatMainll != null) {
            mMoiveSeatMainll.setVisibility(View.GONE);
        }
    }

    private void showSeatMainV() {
        if (mMoiveSeatMainll != null) {
            mMoiveSeatMainll.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorBlankPage(int failState, View.OnClickListener listener) {
        hideLoadingView();
        switch (failState) {
            case ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD:
                Resources resources = getResources();
                showBlankPage().setCustomPage(resources.getString(R.string.movie_empty_no_seat_data), BlankPage.Icon.NO_CLOCK);
                break;
            default: {
                hideSeatMainV();
                showBlankPage(failState, listener);
            }
        }
    }

    private void initLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MovieSeatActivity.this);
            mProgressDialog.setMessage(getString(R.string.wallet_prompt_create_order));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog != null) mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }

    private void querySeatInfo() {
        showLoadingView();
        checkAsyncTask();
        mSeatInfoTask.execute();
    }

    private void checkAsyncTask() {
        if (mSeatInfoTask == null) {
            mSeatInfoTask = new SeatInfoAsyncTask();
        } else {
            if (mSeatInfoTask.getStatus() == AsyncTask.Status.RUNNING) {
                mSeatInfoTask.cancel(true);
            }
            mSeatInfoTask = new SeatInfoAsyncTask();
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public class SeatInfoAsyncTask extends AsyncTask<String, Integer, MovieSeatList> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(MovieSeatList result) {
            hideLoadingView();
            if (result != null) {
                if (result.seat_info != null) {
                    hideBlankPage();
                    showSeatMainV();
                    if (mMovieSeatV.getChildCount() > 0) {
                        mMovieSeatV.removeAllViews();
                    }
                    mMovieSeatV.addView(mSeatV);
                    mSeatDataManager.setMovieSeatList(result);
                    mSeatV.setParentsViewHeight(getSeatVParentsHeight(), true);
                    mSeatV.init(mSeatThumbV);
                } else {
                    showErrorBlankPage(ActivityConstant.RETURN_STATUS.FAIL_STATE_N0_RECORD, null);
                }
            } else {
                showErrorBlankPage(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
            }
        }

        @Override
        protected MovieSeatList doInBackground(String... params) {
            MovieSeatList seatInfosList = new MovieSeatList();
            try {
                BaseResponse<MovieSeatList> movieSeatList = null;
                BaseRequestParams seatListReqParams = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_CINEMA_ROOM_MAP);
                seatListReqParams.addQueryStringParameter(MovieTicketConstant.MOVIE_PARAM_CINEMA_ID, String.valueOf(mCinemaId));
                seatListReqParams.addQueryStringParameter(MovieTicketConstant.MOVIE_PARAM_ROOMID, mRoomId);
                TypeToken typeToken = new TypeToken<BaseResponse<MovieSeatList>>() {
                };
                if (isCancelled()) return null;
                movieSeatList = xmain.http().getSync(seatListReqParams, typeToken.getType());
                if (movieSeatList != null && movieSeatList.data != null) {
                    seatInfosList = movieSeatList.data;
                    LogHelper.d(" movieSeatList.data : " + movieSeatList.data.roomid);
                }
                String uToken = AccountHelper.getInstance().getToken(MovieSeatActivity.this);
                BaseRequestParams soldReqParams = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_CINEMA_ROOM_SOLD);
                soldReqParams.addQueryStringParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, uToken);
                soldReqParams.addQueryStringParameter(MovieTicketConstant.MOVIE_PARAM_MPID, mpid);
                TypeToken stringTypeToken = new TypeToken<BaseResponse<String>>() {
                };
                if (isCancelled()) return null;
                BaseResponse<String> soldData = xmain.http().getSync(soldReqParams, stringTypeToken.getType());
                if (seatInfosList != null && seatInfosList.seat_info != null) {
                    mSeatDataManager.mergeData(seatInfosList, mSeatDataManager.getSoldInfo(soldData.data));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } catch (Throwable throwable) {
                return null;
            }
            return seatInfosList;
        }
    }

    public interface OnSeatClickListener {
        boolean addSeat(String area, int rowNum, int columnNum, String desc, String n);

        boolean cancelSeat(View view, String area, int rowNum, int columnNum, String desc, String n);

        boolean cancelSeat(String area, int rowNum, int columnNum, String desc, String n);

        MovieSeatDataManager getDataManager();
    }

    class OrderAsynTask extends AsyncTask<Void, Void, BaseResponse<LockSeatOrder>> {

        private Context mContext;
        private String mMpid;
        private String mSeat;

        public OrderAsynTask(Context context, String mpid, String seat) {
            mContext = context;
            mMpid = mpid;
            mSeat = seat;
        }

        @Override
        protected BaseResponse<LockSeatOrder> doInBackground(Void... params) {
            BaseRequestParams requestParams = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_ORDER_ADD);
            requestParams.addParameter(MovieTicketConstant.MOVIE_PARAM_SSO_TK, AccountHelper.getInstance().getToken(mContext));
            requestParams.addParameter(MovieTicketConstant.MOVIE_PARAM_MPID, mMpid);
            requestParams.addParameter(MovieTicketConstant.MOVIE_PARAM_SEAT, mSeat);
            requestParams.setConnectTimeout(RspConstants.CONNECT_TIMEOUT_TEN);
            TypeToken<BaseResponse<LockSeatOrder>> typeToken = new TypeToken<BaseResponse<LockSeatOrder>>() {
            };
            BaseResponse<LockSeatOrder> response = null;
            try {
                response = xmain.http().postSync(requestParams, typeToken.getType());
            } catch (Throwable throwable) {
            }
            return response;
        }

        @Override
        protected void onPostExecute(BaseResponse<LockSeatOrder> response) {
            hideProgressDialog();
            if (response != null) {
                if (response.errno == 10000) {
                    LockSeatOrder order = response.data;
                    if (order != null) {
                        MovieProduct product = new MovieProduct(order, mMovieId, mCinemaId, mMovieName, mCinemaName, mMovieData, mMovieTime, mRoomName);
                        product.pay(mContext);
                    }
                } else if (response.errno == 10005) {
                    showErrorToast(response.errmsg);
                } else {
                    showErrorToast(getString(R.string.movie_create_order_failed));
                }
            } else {
                showErrorToast(getString(R.string.movie_create_order_failed));
            }
            mOrderTask = null;
        }

        private void showErrorToast(String text) {
            Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            if (mSeatV != null) {
                querySeatInfo();
            }
        }
    }

    private void showMovieDateAlertDialog(Context context) {
        if (context == null) {
            return;
        }
        if (mMovieDateAlertDialog == null) {
            mMovieDateAlertDialog = new LeBottomSheet(this);
            mMovieDateAlertDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mMovieDateAlertDialog.dismiss();
                        }
                    }, null, null,
                    new String[]{
                            context.getString(R.string.movie_seat_date_alert_ok)
                    },
                    String.format(context.getString(R.string.movie_seat_date_alert_text), getDateFormatStr_AlertDialog(mMovieData)),
                    null, null, context.getResources().getColor(R.color.colorBtnBlue), false);
        }
        if (!mMovieDateAlertDialog.isShowing()) {
            mMovieDateAlertDialog.show();
        }
    }
}
