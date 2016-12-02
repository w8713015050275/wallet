package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.activity.MovieSeatActivity;
import com.letv.walletbiz.movie.beans.MovieSeatColumnInfo;
import com.letv.walletbiz.movie.beans.MovieSeatList;
import com.letv.walletbiz.movie.beans.MovieSeatRowInfo;
import com.letv.walletbiz.movie.beans.MovieSoldInfo;

/**
 * Created by changjiajie on 16-2-1.
 */
public class MovieSeatV extends View {

    private static final String TAG = MovieSeatV.class.getSimpleName();
    private static boolean TOUNCHLOG = true;
    private static boolean POSITIONXYLOG = true;
    private static boolean ZOOMSEATPARAMSLOG = true;
    private static boolean MOVIESEATPARAMSLOG = true;
    private Context mContext;
    public static final int DEFAULTINDEX = -1;
    private boolean zooming = false;
    private static final float ZOOMRATIO = 0.9F;
    private static final float MOVERATIO = 1.3F;

    private static final float THUMVMAXRATIO = 0.42F;

    private static int BGCOLOR;
    /**
     * 父容器高
     */
    private static float parents_height;
    /**
     * 状态信息及座位信息
     */
    private String mRoomId;
    private MovieSeatRowInfo[] mSeatInfoList;
    /**
     * 缩略图
     */
    private MovieSeatThumbV mThumbV;

    /**
     * 手势监听
     */
    private GestureDetector mGestureDetector;

    private Paint mPaint, mRowAxisTextPaint, mScreenCenterTextPaint, mThumbPaint;

    private Bitmap mThumbBitmap;
    private Canvas mCanvas;
    private Rect mRoomVRect, mSeatVRect, mRoomCentertvRect, mThumbSeatRect, mThumbLocationAreaRect;
    private RectF mRoomCentertvBgRectF, mRowAxisBgRect, mThumbBgRect;

    boolean FIRST = true;

    /**
     * 回调监听
     */
    private MovieSeatActivity.OnSeatClickListener mOnSeatClickLis;

    public MovieSeatV(Context context) {
        this(context, null);
    }

    public MovieSeatV(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieSeatV(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    //初始化函数
    public void init(MovieSeatThumbV thumbV) {
        this.mThumbV = thumbV;
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        this.screenWidth = dm.widthPixels;
        loadData();
        checkData();
        initResource();
        initTools();
        initRect();
        loadSeatParams();
        loadThumbParams();
        this.mGestureDetector = new GestureDetector(this.mContext, new GestureDetectorLis(this.mContext, this));
    }

    private void loadData() {
        MovieSeatList seatList = mOnSeatClickLis.getDataManager().getMovieSeatList();
        if (seatList == null) return;
        this.mTotalRow = seatList.seat_info.length;
        this.mRoomId = seatList.roomid;
        this.mSeatInfoList = seatList.seat_info;
        this.mTotalColumnCount = getColumnCount();
    }

    private void initTools() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);

        this.mScreenCenterTextPaint = new Paint();
        this.mScreenCenterTextPaint.setTextSize(this.screen_center_tv_font_size);
        this.mScreenCenterTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mScreenCenterTextPaint.measureText(mMovieSeatScreenCenterTv);
        Paint.FontMetrics fm = this.mScreenCenterTextPaint.getFontMetrics();
        this.screen_center_tv_font_height = (float) Math.ceil(fm.descent - fm.ascent);

        this.mRowAxisTextPaint = new Paint();
        this.mRowAxisTextPaint.setAntiAlias(true);
        this.mRowAxisTextPaint.setTextSize(this.rownum_axis_font_size);
        this.mRowAxisTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mRowAxisTextPaint.measureText("1");
        fm = this.mRowAxisTextPaint.getFontMetrics();
        this.row_axis_num_tv_font_height = (float) Math.ceil(fm.descent - fm.ascent);

        this.mThumbPaint = new Paint();

        this.mCanvas = new Canvas();
    }

    private void loadSeatParams() {
        initSeatLimitData();
        initSeatParams();
        calculateSeatCenterVParams();
        initPosition();
    }

    private void checkViewState() {
        checkData();
        initPosition();
    }

    private void checkData() {
        if (this.mTotalColumnCount == 0 || this.mTotalRow == 0)
            return;
    }

    private void initPosition() {
        if (getX() + this.mViewWidth < 0.0F || getY() + this.mViewHeight < 0.0F) {
            setScrollInfo(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    private void adjustPosition() {
        if (getX() + this.mViewWidth < this.screenWidth) {
            setScrollXInfo(0.0F, 0.0F);
        }
        if (getY() + this.mViewHeight < this.parents_height) {
            setScrollYInfo(0.0F, 0.0F);
        }
    }

    private void initResource() {
        this.mSeatNormalBitmap = getBitmap(R.drawable.movie_seat_normal);
        this.mSeatCheckedBitmap = getBitmap(R.drawable.movie_seat_checked);
        this.mSeatSoldBitmap = getBitmap(R.drawable.movie_seat_sold);
        this.mSeatLoverBitmap = getBitmap(R.drawable.movie_seat_lover);

        this.BGCOLOR = mContext.getResources().getColor(R.color.colorMovieSeatBg);
        this.CENTERLINECOLOR = mContext.getResources().getColor(R.color.colorMovieSeatCenterLineBg);
        this.SCREENCENTERTVBGCOLOR = mContext.getResources().getColor(R.color.colorMovieScreenCenterTvBg);
        this.SCREENCENTERTVCOLOR = mContext.getResources().getColor(R.color.colorMovieScreenCenterTv);
        this.SEATTHUMBBG = mContext.getResources().getColor(R.color.colorMovieThumbBg);
        this.SEATROWAXISBG = mContext.getResources().getColor(R.color.colorMovieSeatRowAxisBg);
        this.ROWAXISTEXTCOLOR = mContext.getResources().getColor(R.color.colorRowAxisText);
        this.SEATTHUMBRECTLINECOLOR = mContext.getResources().getColor(R.color.colorMovieThumbRectLineBg);
        this.mMovieSeatScreenCenterTv = mContext.getResources().getString(R.string.movie_seat_screen_center_tv);

        this.seat_init_width = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_init_width);
        this.seat_init_height = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_init_height);
        this.seatv_init_margin_left = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_margin_left);
        this.seatv_init_margin_right = this.seatv_init_margin_left;
        this.seatv_init_margin_top = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_margin_top);
        this.seatv_init_margin_bottom = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_margin_bottom);
        this.rownum_axis_margin_offset = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_rownum_axis_margin_offset);
        this.rownum_axis_font_size = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_rownum_axis_font_size);
        this.row_axis_bg_width = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_v_rownum_axis_v_width);
        this.screen_center_tv_font_size = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_screen_center_tv_font_size);
        this.screen_center_tv_width = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_screen_center_tv_width);
        this.screen_center_tv_height = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_screen_center_tv_height);
        this.screen_center_tv_paddingH = getResources().getDimensionPixelSize(R.dimen.movie_seat_screen_center_tv_padding_horizontal);
        this.screen_center_tv_paddingV = getResources().getDimensionPixelSize(R.dimen.movie_seat_screen_center_tv_padding_vertical);

        this.seat_thumb_width = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_thumb_w);
        this.seat_thumb_v_margin_left = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_thumb_margin_left);
        this.seat_thumb_v_margin_top = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_thumb_margin_top);
        this.seat_thumb_v_padding_top = mContext.getResources().getDimensionPixelOffset(R.dimen.movie_seat_thumb_padding);
        this.seat_thumb_v_padding_left = this.seat_thumb_v_padding_top;

        this.h_w_ratio = this.seat_init_height * 1.0F / this.seat_init_width;
    }

    private void initSeatLimitData() {
        int init_margin_top = this.seatv_init_margin_top;
        this.seatv_init_margin_top = 0;
        this.seatv_init_margin_top = (int) (init_margin_top + this.screen_center_tv_font_height + this.screen_center_tv_paddingH);
        this.seat_min_width = Math.round((this.screenWidth - this.seatv_init_margin_left * 2)
                / ((this.mTotalColumnCount + this.mMinSeatOffsetNum) * (this.aisle_seat_horizontal_ratio + 1)));
        if (this.seat_min_width >= this.seat_init_width) {
            this.seat_init_width = this.seat_min_width;
        }
        this.seat_minwatch_width = Math.round(this.seat_min_width * this.seat_v_minwatch_ratio);
        this.seat_minwatch_height = Math.round(this.seat_minwatch_width * this.h_w_ratio);
        //最小可以装下所有座位
        this.seat_max_width = Math.round(this.seat_init_width * this.seat_v_max_ratio);
        this.seat_maxwatch_width = Math.round(this.seat_init_width * this.seat_v_maxwatch_ratio);
        this.seat_maxwatch_height = Math.round(this.seat_maxwatch_width * this.h_w_ratio);
        this.seatv_max_margin_left = Math.round(this.seatv_init_margin_left * this.seatv_max_margin_left_ratio);
        this.seat_init_horizontal_margin = getSeatHorizontalMargin();
        this.seat_init_vertical_margin = getSeatVerticalMargin();
        this.seat_min_horizontal_margin = this.seat_minwatch_width * this.aisle_seat_horizontal_ratio;
        this.seat_min_vertical_margin = this.seat_minwatch_width * this.h_w_ratio * this.aisle_seat_vertical_ratio;
        //计算初始化宽高
        this.mInitAllSeatWidth = this.seat_init_width * this.mTotalColumnCount + this.seat_init_horizontal_margin * (this.mTotalColumnCount - 1);
        this.mInitAllSeatHeight = this.seat_init_height * this.mTotalRow + this.seat_init_vertical_margin * (this.mTotalRow - 1);
        this.mInitViewWidth = this.mInitAllSeatWidth + this.seatv_init_margin_left + this.seatv_init_margin_right;
        this.mInitViewHeight = this.mInitAllSeatHeight + this.seatv_init_margin_top + this.seatv_init_margin_bottom;
        LogHelper.d("[%S] [initSeatData] seat_minwatch_width == %S | seat_minwatch_height == %S", TAG, seat_minwatch_width, seat_minwatch_height);
        LogHelper.d("[%S] [initSeatData] seat_maxwatch_width == %S | seat_maxwatch_height == %S", TAG, seat_minwatch_width, seat_minwatch_height);
    }

    private void initSeatParams() {
        this.seat_horizontal_margin = this.seat_init_horizontal_margin * this.zoom;
        this.seat_vertical_margin = this.seat_init_vertical_margin * this.zoom;
        this.seat_current_width = this.seat_init_width * this.zoom;
        this.seat_current_height = this.seat_current_width * this.h_w_ratio;
        this.seatv_margin_left = Math.round(this.seatv_init_margin_left * this.zoom);
        this.seatv_margin_right = this.seatv_margin_left;
        this.seatv_margin_top = this.seatv_init_margin_top;
        this.seatv_margin_bottom = this.seatv_init_margin_bottom;

        this.mAllSeatWidth = this.mInitAllSeatWidth;
        this.mAllSeatHeight = this.mInitAllSeatHeight;
        this.mViewWidth = this.mInitViewWidth;
        this.mViewHeight = this.mInitViewHeight;
    }

    private void initRect() {
        this.mRoomVRect = new Rect();
        this.mSeatVRect = new Rect();
        this.mRoomCentertvRect = new Rect();
        this.mThumbSeatRect = new Rect();
        this.mThumbLocationAreaRect = new Rect();

        this.mRoomCentertvBgRectF = new RectF();
        this.mRowAxisBgRect = new RectF();
        this.mThumbBgRect = new RectF();
    }

    private void calculateParamsUseSeatWidth() {
        calculateZoom();
        calculateSeatParams();
        calculateSeatCenterVParams();
        calculateThumbLocationArea();
    }

    private void calculateZoom() {
        this.zoom = this.seat_current_width / this.seat_init_width;
    }

    private void calculateSeatParams() {
        int temp_seatv_margin_left = Math.round(this.seatv_init_margin_left * this.zoom);
        if (temp_seatv_margin_left >= this.seatv_init_margin_left && temp_seatv_margin_left <= this.seatv_max_margin_left) {
            this.seatv_margin_left = temp_seatv_margin_left;
            this.seatv_margin_right = this.seatv_margin_left;
        }
        float vertical_magin = this.seat_init_vertical_margin * this.zoom;
        if (vertical_magin >= this.seat_min_vertical_margin) {
            this.seat_vertical_margin = vertical_magin;
        }
        float horizontal_margin = this.seat_init_horizontal_margin * this.zoom;
        if (horizontal_margin >= this.seat_min_horizontal_margin) {
            this.seat_horizontal_margin = horizontal_margin;
        }
        this.seatv_margin_top = this.seatv_init_margin_top;
        this.seatv_margin_bottom = this.seatv_init_margin_bottom;

        this.mAllSeatWidth = this.seat_current_width * this.mTotalColumnCount + this.seat_horizontal_margin * (this.mTotalColumnCount - 1);
        this.mAllSeatHeight = this.seat_current_height * this.mTotalRow + this.seat_vertical_margin * (this.mTotalRow - 1);
        this.mViewWidth = this.mAllSeatWidth + this.seatv_margin_left + this.seatv_margin_right;
        this.mViewHeight = this.mAllSeatHeight + this.seatv_margin_top + this.seatv_margin_bottom;
        zoomLog("mAllSeatWidth == %S | mAllSeatHeight == %S \n mViewWidth = %S | mViewHeight = %S",
                mAllSeatWidth, mAllSeatHeight, mViewWidth, mViewHeight);
    }

    private void calculateSeatCenterVParams() {
        //计算中线Params
        this.centerline_x = this.seatv_margin_left + this.seat_current_width * (this.mTotalColumnCount / 2)
                + this.seat_horizontal_margin * ((this.mTotalColumnCount / 2) - 1) + this.seat_horizontal_margin / 2;
        this.centerline_top_y = this.seatv_margin_top - this.screen_center_tv_height;
        this.centerline_bottom_y = this.mViewHeight - this.seatv_margin_bottom;
        int centertv_left = Math.round(this.centerline_x - this.screen_center_tv_width / 2);
        int centertv_top = (int) (this.centerline_top_y - this.screen_center_tv_height);
        int centertv_right = Math.round(this.centerline_x + this.screen_center_tv_width / 2);

        this.mRoomCentertvBgRectF.set(centertv_left, centertv_top, centertv_right, this.centerline_top_y);
        this.mRoomCentertvRect.set(0, 0, centertv_right, Math.round(this.centerline_top_y));
        zoomLog("centerline_x == %S | centerline_bottom_y == %S", this.centerline_x, this.centerline_bottom_y);
    }

    private void calculateSeartRowAixsVParams() {
        //计算排轴Params
        int temp_row_axis_left = (int) (Math.abs(getX()) + this.rownum_axis_margin_offset);
        int temp_row_axis_top = (int) (getY() + this.seatv_margin_top - this.rownum_axis_margin_top_offset);
        int temp_row_axis_right = (int) (Math.abs(getX()) + this.rownum_axis_margin_offset + this.row_axis_bg_width);
        int temp_row_axis_bottom = Math.round(this.seatv_margin_top - this.rownum_axis_margin_top_offset + this.mViewHeight);
        this.mRowAxisBgRect.set(temp_row_axis_left, temp_row_axis_top, temp_row_axis_right, temp_row_axis_bottom);
        zoomLog("temp_row_axis_left == %S | temp_row_axis_top == %S", TAG, temp_row_axis_left, temp_row_axis_top);
    }

    private float getSeatHorizontalMargin() {
        float margin = this.seat_init_width * this.aisle_seat_horizontal_ratio;
        if (margin <= 0.0F) {
            margin = 1.0F;
        }
        return margin;
    }

    private float getSeatVerticalMargin() {
        float margin = this.seat_init_height * this.aisle_seat_vertical_ratio;
        if (margin <= 0.0F) {
            margin = 1.0F;
        }
        return margin;
    }

    private void loadThumbParams() {
        //当初始化view宽小于屏幕宽
        if (this.mInitAllSeatWidth <= this.screenWidth) {
            calculateThumbVParams();
        } else {
            this.seat_thumb_width = this.mInitAllSeatWidth * this.thumbratio + this.seat_thumb_v_padding_left * 2;
            if (this.seat_thumb_width < this.screenWidth * this.THUMVMAXRATIO) {
                this.seat_thumb_height = this.mInitAllSeatHeight * this.thumbratio + this.seat_thumb_v_padding_top * 2;
            } else {
                this.seat_thumb_width = this.screenWidth * this.THUMVMAXRATIO;
                calculateThumbVParams();
            }
        }
        this.thumbX = this.seat_thumb_v_margin_left + this.seat_thumb_width;
        this.thumbY = this.seat_thumb_v_margin_top + this.seat_thumb_height;

        this.mThumbLocationAreaLeftLimit = this.seat_thumb_v_margin_left + this.seat_thumb_v_padding_left;
        this.mThumbLocationAreaTopLimit = this.seat_thumb_v_margin_top + this.seat_thumb_v_padding_top;
        this.mThumbLocationAreaRightLimit = this.thumbX - this.seat_thumb_v_padding_left;
        this.mThumbLocationAreaBottomLimit = this.thumbY - this.seat_thumb_v_padding_top;

        //缩略图内容宽高为准
        this.thumbLocationMaxRectWidth = this.seat_thumb_width - this.seat_thumb_v_padding_left * 2;
        this.thumbLocationMaxRectHeight = this.seat_thumb_height - this.seat_thumb_v_padding_top * 2;

        calculateThumbLocationArea();
    }

    private void calculateThumbVParams() {
        this.thumbratio = ((this.seat_thumb_width - (this.seat_thumb_v_padding_left * 2)) * 1.0F) / this.mInitAllSeatWidth;
        this.seat_thumb_height = this.mInitAllSeatHeight * this.thumbratio + this.seat_thumb_v_margin_top + this.seat_thumb_v_padding_top * 2;
    }

    private void calculateThumbLocationArea() {
        float tempThumbLocatorRectWidthRatio = this.screenWidth * 1.0F / this.mViewWidth;
        float tempThumbLocatorRectHeightRatio = this.parents_height * 1.0F / this.mViewHeight;
        float rectRatioWidth = this.thumbLocationMaxRectWidth * tempThumbLocatorRectWidthRatio;
        float rectRatioHeight = this.thumbLocationMaxRectHeight * tempThumbLocatorRectHeightRatio;

        if (rectRatioWidth <= this.thumbLocationMaxRectWidth) {
            this.thumbLocationRectWidth = rectRatioWidth;
        } else {
            this.thumbLocationRectWidth = this.thumbLocationMaxRectWidth;
        }
        this.thumbSeatVWRatio = this.thumbLocationMaxRectWidth * 1.0F / this.mAllSeatWidth;
        if (this.thumbSeatVWRatio <= 0.0F) {
            this.thumbSeatVWRatio = 1.0F;
        }
        if (rectRatioHeight <= this.thumbLocationMaxRectHeight) {
            this.thumbLocationRectHeight = rectRatioHeight;
        } else {
            this.thumbLocationRectHeight = this.thumbLocationMaxRectHeight;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkViewState();
        resetPosition();
        canvas.translate(getX(), getY());
        drawSeatBitmap(canvas);
        drawRoomCenterV(canvas);
        drawRowAxisV(canvas);
        drawThumb();
    }

    private void drawThumbLocationAreaRect(Canvas canvas) {
        this.mThumbPaint.setColor(this.SEATTHUMBRECTLINECOLOR);
        this.mThumbPaint.setStyle(Paint.Style.STROKE);
        this.mThumbPaint.setStrokeWidth(this.seat_rect_line);
        canvas.drawRect(getThumbLocationAreaRect(Math.abs(getX()), Math.abs(getY())),
                this.mThumbPaint);
        this.mThumbPaint.setStyle(Paint.Style.FILL);
    }

    private void resetPosition() {
        if (this.FIRST) {
            this.FIRST = false;
            float tempC = getViewWidth() / 2 - getMeasuredWidth() / 2;
            float tempX = getMeasuredWidth() / 2 - getViewWidth() / 2;
            if (tempC < 0) {
                tempC = 0;
            }
            if (tempX > 0) {
                tempX = 0.0F;
            }
            setScrollInfo(tempC, tempX, 0, 0.0F);
        }
    }

    private void setScrollYInfo(float y, float d) {
        setD(d);
        setY(y);
    }

    private void setScrollXInfo(float x, float c) {
        setC(c);
        setX(x);
    }

    private void setScrollInfo(float c, float x, float d, float y) {
        setScrollXInfo(x, c);
        setScrollYInfo(y, d);
    }

    /**
     * TODO:手势监听
     */
    public static class GestureDetectorLis extends GestureDetector.SimpleOnGestureListener {
        private MovieSeatV mSeatV;
        private Context mContext;

        public GestureDetectorLis(Context context, MovieSeatV seatV) {
            this.mContext = context;
            this.mSeatV = seatV;
        }

        public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
            int rowNum = mSeatV.getRowNum(paramMotionEvent.getY());
            int columnNum = mSeatV.getColumnNum(paramMotionEvent.getX());
            mSeatV.touchLog("SingleTapUp rowNum == %S | columnNum == %S", rowNum, columnNum);
            MovieSeatRowInfo[] seatInfoList = mSeatV.getSeatInfoList();
            if ((rowNum > 0 && rowNum <= mSeatV.getTotalRow())) {
                MovieSeatRowInfo seatInfo = seatInfoList[rowNum - 1];
                if (seatInfo == null)
                    return false;
                if (seatInfo.desc.equals("0")) {
                    //此处是过道区域
                    return false;
                }
                MovieSeatColumnInfo[] detailList = seatInfo.detail;
                if (detailList == null)
                    return false;
                if (columnNum > 0 &&
                        columnNum <= mSeatV.getTotalColumnCount()) {
                    MovieSeatColumnInfo detailInfo = detailList[columnNum - 1];
                    mSeatV.touchLog("SingleTapUp n == %S | state == %S", detailInfo.n, detailInfo.state);
                    if (detailInfo.damagedFlg.equals(MovieSeatColumnInfo.USABLE_IDENTIFY) && !detailInfo.n.equals(MovieSeatColumnInfo.AISLE_IDENTIFY)) {
                        if (detailInfo.state == MovieSeatColumnInfo.SOLDSEATSTATE || !detailInfo.loveInd.equals(MovieSeatColumnInfo.LOVER_IDENTIFY0)) {
                            return false;
                        }
                        //判断是否是已选座位
                        if (mSeatV.mOnSeatClickLis.getDataManager().checkCheckedSeat(seatInfo.area, rowNum, columnNum) == MovieSeatV.DEFAULTINDEX) {
                            //添加已选座位信息
                            if (mSeatV.mOnSeatClickLis.getDataManager().isCheckedMaxSeatNum()) {
                                Toast.makeText(mContext, R.string.movie_seat_checked_exceed_max_count, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            mSeatV.addCheckedSeatInfo(seatInfo.area, rowNum, columnNum, seatInfo.desc, detailInfo.n);
                        } else {
                            mSeatV.cancelCheckedSeatInfo(seatInfo.area, rowNum, columnNum, seatInfo.desc, detailInfo.n);
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mSeatV.isMove()) {
                return false;
            }
            //左右滑动
            boolean boolLR = true;
            //上下滑动
            boolean boolTB = true;
            if ((mSeatV.getViewWidth() < mSeatV.getMeasuredWidth())
                    && (0.0F == mSeatV.getX())) {
                boolLR = false;
            }

            if ((mSeatV.getViewHeight() < mSeatV.getMeasuredHeight())
                    && (0.0F == mSeatV.getY())) {
                boolTB = false;
            }
            mSeatV.movieLog("boolLR == %S | boolTB == %S", boolLR, boolTB);
            if (boolLR) {
                int c = Math.round(distanceX * MOVERATIO);
                mSeatV.amendX((float) c);
                mSeatV.amendC(c);
                if (mSeatV.getC() < 0) {
                    mSeatV.setC(0);
                    mSeatV.setX(0.0F);
                }
                mSeatV.movieLog("seatVMeasuredWidth == %S | seatVWidth == %S", mSeatV.getMeasuredWidth(), mSeatV.getViewWidth());
                if (mSeatV.getC() + mSeatV.getMeasuredWidth() > mSeatV.getViewWidth()) {
                    mSeatV.setC(mSeatV.getViewWidth() - mSeatV.getMeasuredWidth());
                    mSeatV.setX(mSeatV.getMeasuredWidth() - mSeatV.getViewWidth());
                }
            }
            if (boolTB) {
                int y = Math.round(distanceY * MOVERATIO);
                mSeatV.amendY((float) y);
                mSeatV.amendD(y);
                if (mSeatV.getD() < 0) {
                    mSeatV.setD(0);
                    mSeatV.setY(0.0F);
                }
                mSeatV.movieLog("seatVMeasuredHeight == %S | seatVHeight == %S", mSeatV.getMeasuredHeight(), mSeatV.getViewHeight());
                if (mSeatV.getD() + mSeatV.getMeasuredHeight() > mSeatV.getViewHeight()) {
                    mSeatV.setD(mSeatV.getViewHeight() - mSeatV.getMeasuredHeight());
                    mSeatV.setY(mSeatV.getMeasuredHeight() - mSeatV.getViewHeight());
                }
            }
            mSeatV.showThumV();
            mSeatV.invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchLog("PointerCount == %S ", event.getPointerCount());
        if (event.getPointerCount() == 1) {
            if (this.isZoom) {
                this.isZoom = false;
                this.isMove = false;
                this.record_data_a = -1.0F;
            } else {
                this.isMove = true;
            }
            while (this.seat_current_width < this.seat_min_width) {
                this.seat_current_width++;
                this.seat_current_height = this.seat_current_width * this.h_w_ratio;
                float x = this.screenWidth / 2 - this.centerline_x;
                if (x > 0.0F) {
                    x = 0.0F;
                }
                setScrollInfo(0, x, 0, 0.0F);
                calculateParamsUseSeatWidth();
                invalidate();
            }
            while (this.seat_current_width > this.seat_max_width) {
                this.seat_current_width--;
                this.seat_current_height = this.seat_current_width * this.h_w_ratio;
                calculateParamsUseSeatWidth();
                invalidate();
            }
            this.mGestureDetector.onTouchEvent(event);
        } else {
            this.isZoom = true;
            showThumV();
            zoomView(event);
        }
        return true;
    }

    //私有函数
    private void zoomView(MotionEvent event) {
        if (!this.zooming) {
            this.zooming = true;
            float moveSize = getPoinitersDistance(event) * this.ZOOMRATIO;
            zoomLog("moveSize == %S ", moveSize);
            if (this.record_data_a < 0.0F) {
                this.record_data_a = moveSize;
            } else {
                float ratio = moveSize / this.record_data_a;
                this.record_data_a = moveSize;
                float tempRatio = ratio * this.zoom;
                float temp_width = this.seat_init_width * tempRatio;
                zoomLog("record_data_a == %S | ratio == %S | tempRatio = %S | temp_width = %S",
                        this.record_data_a, ratio, tempRatio, temp_width);
                float temp_height = temp_width * this.h_w_ratio;
                if ((this.isZoom) && (temp_width > this.seat_minwatch_width && temp_width < this.seat_maxwatch_width)
                        && (temp_height > this.seat_minwatch_height && temp_height < this.seat_maxwatch_height)) {
                    this.zoom = tempRatio;
                    this.seat_current_width = temp_width;
                    this.seat_current_height = temp_height;
                    zoomLog("seat_current_width == %S | seat_current_height == %S | zoom == %S",
                            this.seat_current_width, this.seat_current_height, this.zoom);
                    adjustPosition();
                    calculateParamsUseSeatWidth();
                    invalidate();
                }
            }
            this.zooming = false;
        }
    }

    //公共函数

    /**
     * 添加选择的座位信息
     *
     * @param area
     * @param mRowNum
     * @param mColumnNum
     * @param
     * @param n
     * @return
     */

    public void addCheckedSeatInfo(String area, int mRowNum, int mColumnNum, String desc, String n) {
        MovieSoldInfo soldInfo = getMovieSoldInfo(area, mRowNum, mColumnNum, desc, n);
        this.mSeatInfoList[mRowNum - 1].detail[mColumnNum - 1].state = MovieSeatColumnInfo.CHECKEDSTATE;
        this.mOnSeatClickLis.getDataManager().addCheckSeatInfo(soldInfo);
        this.mOnSeatClickLis.addSeat(area, mRowNum, mColumnNum, desc, n);
        touchLog("[addCheckedSeat] rowNum == %S | columnNum == %S | desc == %S | n == %S ",
                mRowNum, mColumnNum, desc, n);
        hideThumV();
        invalidate();
    }

    /**
     * 刪除选择的座位信息
     *
     * @param area
     * @param mRowNum
     * @param mColumnNum
     */
    public void deletCheckedSeatState(String area, int mRowNum, int mColumnNum) {
        int index = this.mOnSeatClickLis.getDataManager().checkCheckedSeat(area, mRowNum, mColumnNum);
        if (index != this.DEFAULTINDEX) {
            this.mSeatInfoList[mRowNum - 1].detail[mColumnNum - 1].state = MovieSeatColumnInfo.DEFAULTSTATE;
            this.mOnSeatClickLis.getDataManager().deleteCheckSeatInfo(index);
            touchLog("[cancelCheckedSeat] rowNum == %S | columnNum == %S",
                    mRowNum, mColumnNum);
            hideThumV();
            invalidate();
        }
    }

    public void cancelCheckedSeatInfo(String area, int mRowNum, int mColumnNum, String desc, String n) {
        this.mOnSeatClickLis.cancelSeat(area, mRowNum, mColumnNum, desc, n);
    }

    public void setOnClickListener(MovieSeatActivity.OnSeatClickListener clickListener) {
        this.mOnSeatClickLis = clickListener;
    }

    public MovieSeatActivity.OnSeatClickListener getOnClickListener() {
        return this.mOnSeatClickLis;
    }

    public String getRoomId() {
        return this.mRoomId;
    }

    /**
     * 设置父容器的高度
     *
     * @param height
     */
    public void setParentsViewHeight(int height, boolean isUpdate) {
        this.parents_height = height;
        if (isUpdate) {
            calculateThumbLocationArea();
            invalidate();
        }
    }

    /**
     * 获取座位信息
     *
     * @return
     */
    public MovieSeatRowInfo[] getSeatInfoList() {
        return this.mSeatInfoList;
    }

    /**
     * 获取最大排数
     *
     * @return
     */
    public int getTotalRow() {
        return this.mSeatInfoList.length;
    }

    /**
     * 获取最大列数
     *
     * @return
     */
    public int getTotalColumnCount() {
        return this.mTotalColumnCount;
    }

    /**
     * 获取View的宽度
     *
     * @return
     */
    public float getViewWidth() {
        return this.mViewWidth;
    }

    /**
     * 获取View的宽度
     *
     * @return
     */
    public float getViewHeight() {
        return this.mViewHeight;
    }

    /**
     * 获取x轴的偏移
     *
     * @return
     */
    public float getX() {
        return this.x;
    }

    /**
     * 设置x轴偏移量
     *
     * @param x The visual x position of this view, in pixels.
     */
    public void setX(float x) {
        xyLog(" before x == %S", getX());
        this.x = x;
        xyLog(" x == %S", getX());
    }

    /**
     * 修改x轴偏移量
     *
     * @param x The visual x position of this view, in pixels.
     */
    public void amendX(float x) {
        xyLog(" amend before x == %S", getX());
        this.x = this.x - x;
        xyLog(" amend x == %S", getX());
    }

    /**
     * 获取y轴的偏移
     *
     * @return
     */
    public float getY() {
        return this.y;
    }

    /**
     * 设置y轴偏移量
     *
     * @param y The visual y position of this view, in pixels.
     */
    public void setY(float y) {
        xyLog(" before y == %S", getY());
        this.y = y;
        xyLog(" y == %S", getY());
    }

    /**
     * 修改y轴偏移量
     *
     * @param y The visual y position of this view, in pixels.
     */
    public void amendY(float y) {
        xyLog(" amend before y == %S", getY());
        this.y = this.y - y;
        xyLog(" amend y == %S", getY());
    }

    private float getPoinitersDistance(MotionEvent event) {
        float moveSize = 0.0F;
        try {
            float xOffset = event.getX(0) - event.getX(1);
            float yOffset = event.getY(0) - event.getY(1);
            moveSize = (float) Math.sqrt(xOffset * xOffset + yOffset * yOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return moveSize;
    }


    private int getColumnCount() {
        int maxRow = 0;
        for (MovieSeatRowInfo seatInfo : this.mSeatInfoList) {
            int row = seatInfo.detail.length;
            if (maxRow < row) {
                maxRow = row;
            }
        }
        return maxRow;
    }

    private MovieSoldInfo getMovieSoldInfo(String area, int mRowNum, int mColumnNum, String desc, String seatNum) {
        MovieSoldInfo soldInfo = new MovieSoldInfo();
        soldInfo.area = area;
        soldInfo.mRowNum = mRowNum;
        soldInfo.mColumnNum = mColumnNum;
        soldInfo.desc = desc;
        soldInfo.n = seatNum;
        return soldInfo;
    }

    private Bitmap getBitmap(int resourceId) {
        BitmapDrawable drawable = (BitmapDrawable) mContext.getResources().getDrawable(resourceId);
        return drawable.getBitmap();
    }

    @Override
    public int getHorizontalFadingEdgeLength() {
        return super.getHorizontalFadingEdgeLength();
    }


    /**
     * TODO: 座位
     * Start Position
     */
    //参数
    /**
     * 橫向距离比例
     */
    private static final float aisle_seat_horizontal_ratio = 1 / 5F;
    /**
     * 纵向距离比例
     */
    private static final float aisle_seat_vertical_ratio = 1 / 3F;
    /**
     * 宽高比
     */
    private static float h_w_ratio = 1.0F;
    /**
     * 最大视觉宽高的比例
     */
    private static final float seat_v_maxwatch_ratio = 1.56F;
    /**
     * 最小视觉宽高的比例
     */
    private static final float seat_v_minwatch_ratio = 0.92F;
    /**
     * 最大宽高的比例
     */
    private static final float seat_v_max_ratio = 1.32F;
    /**
     * 最小宽高的比例
     */
    private static final float seat_v_current_ratio = 2.0F;
    /**
     * 座位横向之间距离
     */
    private float seat_init_horizontal_margin = 5.0F;
    private float seat_horizontal_margin = 5.0F;
    private float seat_min_horizontal_margin;
    /**
     * 座位纵向之间距离
     */
    private float seat_init_vertical_margin = 5.0F;
    private float seat_vertical_margin = 5.0F;
    private float seat_min_vertical_margin;

    /**
     * 定位信息
     */
    private static float x = 0.0F;
    private static float y = 0.0F;

    /**
     * 座位距离排数的距离
     */
    private static float c = 0;
    /**
     * 座位距离顶端的距离
     */
    private static float d = 0;
    /**
     * 座位位图
     */
    private Bitmap mSeatNormalBitmap;
    private Bitmap mSeatCheckedBitmap;
    private Bitmap mSeatSoldBitmap;
    private Bitmap mSeatLoverBitmap;
    /**
     * 屏幕宽高
     */
    private int screenWidth;
    /**
     * View大小
     */
    private float mInitViewWidth;
    private float mInitViewHeight;
    /**
     * View大小
     */
    private float mViewWidth;
    private float mViewHeight;

    /**
     * 座位所占宽高
     */
    private float mInitAllSeatWidth;
    private float mInitAllSeatHeight;
    private float mAllSeatWidth;
    private float mAllSeatHeight;
    /**
     * 座位寬高
     */
    private float seat_init_width;
    private float seat_init_height;
    private float seat_current_width;
    private float seat_current_height;
    /**
     * 座位距离顶端的距离
     */
    private float record_data_a = -1.0F;

    /**
     * seatView缩放比率
     */
    private float zoom = 1.0F;
    /**
     * view 左右上下的margin
     */
    private static float seatv_max_margin_left_ratio = 1.5F;
    private int seatv_init_margin_left;
    private int seatv_init_margin_right;
    private int seatv_margin_left;
    private int seatv_margin_right;
    private int seatv_init_margin_top;
    private int seatv_init_margin_bottom;
    private int seatv_margin_top;
    private int seatv_margin_bottom;
    private int seatv_max_margin_left;
    /**
     * 座位总列数
     */
    private int mTotalColumnCount;

    /**
     * 最小座位的偏移量
     */
    private static int mMinSeatOffsetNum = 3;
    /**
     * 座位总排数
     */
    private int mTotalRow;
    /**
     * 缩放
     */
    private static boolean isZoom = true;
    /**
     * 移动标识
     */
    private static boolean isMove = true;

    /**
     * 视觉最大最小宽高
     */
    private float seat_minwatch_width;
    private float seat_minwatch_height;
    private float seat_maxwatch_width;
    private float seat_maxwatch_height;

    /**
     * 座位的最小宽度
     */
    private float seat_min_width;

    /**
     * 座位的最大宽度
     */
    private float seat_max_width;

//公共函数

    /**
     * 获取列数
     *
     * @param x
     * @return
     */
    public int getColumnNum(float x) {
        int columnNum = this.DEFAULTINDEX;
        try {
            float pos = x + getC();
            //开始位置
            float startX = pos - this.seatv_margin_left;
            //加间距后宽度
            float w = this.seat_current_width + this.seat_horizontal_margin;
            //所在位置
            columnNum = (int) ((startX / w)) + 1;
            touchLog("SingleTapUp getColumnNum num == %S | w == %S | pos == %S | startX == %S", columnNum, w, pos, startX);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnNum;
    }

    /**
     * 获取排数
     *
     * @param y
     * @return
     */
    public int getRowNum(float y) {
        int rowNum = DEFAULTINDEX;
        try {
            float pos = y + getD();
            float startY = pos - this.seatv_margin_top;
            float h = this.seat_current_height + this.seat_vertical_margin;
            rowNum = Math.round(startY / h) + 1;
            touchLog("SingleTapUp getRowNum rowNum == %S | h == %S | pos == %S | startY == %S", rowNum, h, pos, startY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowNum;
    }

    /**
     * 获取是否可移动
     *
     * @return
     */

    public boolean isMove() {
        return this.isMove;
    }

    /**
     * 设置座位距离排数的距离
     *
     * @param c
     */
    public void setC(float c) {
        this.c = c;
    }

    /**
     * 修改座位距离排数的距离
     *
     * @param c
     */
    public void amendC(float c) {
        this.c = this.c + c;
    }

    /**
     * 获取座位距离排数的横向距离
     *
     * @return
     */
    public float getC() {
        return this.c;
    }

    /**
     * 设置座位距离顶端的距离
     *
     * @param d
     */
    public void setD(float d) {
        this.d = d;
    }

    /**
     * 修改座位距离顶端的距离
     *
     * @param d
     */
    public void amendD(float d) {
        this.d = this.d + d;
    }

    /**
     * 获取座位距离顶端的距离
     *
     * @return
     */
    public float getD() {
        return this.d;
    }

    //私有函數
    private void drawSeatBitmap(Canvas canvas) {
        //检索每一排
        this.mPaint.setColor(this.BGCOLOR);
        for (int row_i = 0; row_i < this.mTotalRow; row_i++) {
            MovieSeatRowInfo seatInfo = this.mSeatInfoList[row_i];
            if (seatInfo.desc.equals("0")) {
                //此处是过道区域
                continue;
            }
            //检索每一列
            for (int column_i = 0; column_i < this.mTotalColumnCount; column_i++) {
                MovieSeatColumnInfo detailInfo = seatInfo.detail[column_i];
                if (detailInfo.n.equals(MovieSeatColumnInfo.AISLE_IDENTIFY)) {
                    //此处没有座位区域
                    continue;
                }
                //座位区域
                if (!detailInfo.damagedFlg.equals(MovieSeatColumnInfo.USABLE_IDENTIFY)) {
                    //损坏座位区域
                    drawBitmap(canvas, this.mSeatSoldBitmap, this.mPaint, column_i, row_i);
                    continue;
                }
                switch (detailInfo.state) {
                    case MovieSeatColumnInfo.SOLDSEATSTATE:
                        drawBitmap(canvas, this.mSeatSoldBitmap, this.mPaint, column_i, row_i);
                        break;
                    case MovieSeatColumnInfo.CHECKEDSTATE:
                        //画已选区域
                        drawBitmap(canvas, this.mSeatCheckedBitmap, this.mPaint, column_i, row_i);
                        break;
                    default: {
                        //可用座位
                        if (!detailInfo.loveInd.equals(MovieSeatColumnInfo.LOVER_IDENTIFY0)) {
                            //情侣座区域
                            drawBitmap(canvas, this.mSeatSoldBitmap, this.mPaint, column_i, row_i);
                            break;
                        }
                        //普通区域
                        drawBitmap(canvas, this.mSeatNormalBitmap, this.mPaint, column_i, row_i);
                    }
                }
            }
        }
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, Paint paint, int seatNum, int rowNum) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, getSeatRect(seatNum, rowNum),
                    paint);
        }
    }

    /**
     * 画座位区域
     *
     * @param seatNum
     * @param rowNum
     * @return
     */
    private Rect getSeatRect(int seatNum, int rowNum) {
        try {
            int left = (int) (this.seatv_margin_left + seatNum * this.seat_current_width + seatNum * this.seat_horizontal_margin);
            int top = (int) (this.seatv_margin_top + rowNum * this.seat_current_height + rowNum * this.seat_vertical_margin);
            int right = (int) (this.seatv_margin_left + (seatNum + 1) * this.seat_current_width + seatNum * this.seat_horizontal_margin);
            int bottom = (int) (this.seatv_margin_top + (rowNum + 1) * this.seat_current_height + rowNum * this.seat_vertical_margin);
            mSeatVRect.set(left, top, right, bottom);
            return mSeatVRect;
        } catch (Exception localException) {
            localException.printStackTrace();
            return new Rect();
        }
    }
    /**
     * TODO: 座位
     * End Position
     */


/**
 * TODO: 缩略图
 * Start Position
 */
    //参数

    /**
     * 距顶部距离偏移量
     */
    private static int seat_thumb_v_margin_top;
    private static int seat_thumb_v_padding_left;
    private static int seat_thumb_v_padding_top;
    private static int seat_thumb_v_margin_left;
    /**
     * 左边排轴距离偏移量
     */
    private static float thumbratio = 1 / 5F;
    private static float thumbSeatVWRatio = 1.0F;
    private static float thumbBgRx = 10.0F;
    private static float thumbBgRy = 10.0F;
    private static float seat_rect_line = 2.0F;
    /**
     * 缩略图的宽高
     */
    private float seat_thumb_width;
    private float seat_thumb_height;

    /**
     * 显示缩略图的标识
     */
    private boolean showThumb = false;
    private static int SHOWTHUMBTIME = 2000;

    /**
     * 缩略图定位矩形的颜色
     */
    private static int SEATTHUMBRECTLINECOLOR;

    private float thumbX;
    private float thumbY;

    private float mThumbLocationAreaRightLimit;
    private float mThumbLocationAreaBottomLimit;
    private float mThumbLocationAreaLeftLimit;
    private float mThumbLocationAreaTopLimit;

    /**
     * 定位矩形的宽高
     */
    private static float thumbLocationMaxRectWidth;
    private static float thumbLocationMaxRectHeight;
    private static float thumbLocationRectWidth;
    private static float thumbLocationRectHeight;

    //公共函數

    /**
     * 显示缩略图
     */
    public void showThumV() {
        if (this.mThumbV.getVisibility() == View.INVISIBLE) {
            this.mThumbV.setVisibility(View.VISIBLE);
            startHideThumTask();
        }
        this.showThumb = true;
    }

    /**
     * 隐藏缩略图
     */
    public void hideThumV() {
        this.mThumbV.setVisibility(View.INVISIBLE);
        this.showThumb = false;
    }

    /**
     * 缩略图
     */
    private void drawThumb() {
        if (this.showThumb) {
            this.mThumbBitmap = Bitmap.createBitmap(Math.round(this.thumbX), Math.round(this.thumbY), Bitmap.Config.ARGB_8888);
            this.mCanvas.setBitmap(this.mThumbBitmap);
            this.mCanvas.save();
            this.mThumbPaint.setColor(this.SEATTHUMBBG);
            this.mCanvas.drawRoundRect(getThumbBgRect(), this.thumbBgRx, this.thumbBgRy, this.mThumbPaint);
            drawThumbSeatBitmap(this.mCanvas);
            drawThumbLocationAreaRect(this.mCanvas);
            if (this.mThumbV != null) {
                this.mThumbV.setBitmap(this.mThumbBitmap);
                this.mThumbV.invalidate();
            }
        }
    }

    private RectF getThumbBgRect() {
        this.mThumbBgRect.set(this.seat_thumb_v_margin_left, this.seat_thumb_v_margin_top, this.thumbX, this.thumbY);
        return this.mThumbBgRect;
    }

    private Handler mHideThumbHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            hideThumV();
        }
    };

    private void startHideThumTask() {
        mHideThumbHandler.removeCallbacks(runnable);
        mHideThumbHandler.postDelayed(runnable, SHOWTHUMBTIME);
    }


    //私有函數
    private void drawThumbSeatBitmap(Canvas canvas) {
        //检索每一排
        this.mPaint.setColor(-1);
        for (int row_i = 0; row_i < this.mTotalRow; row_i++) {
            MovieSeatRowInfo seatInfo = this.mSeatInfoList[row_i];
            if (seatInfo.desc.equals("0")) {
                //此处是过道区域
                continue;
            }
            //检索每一列
            for (int column_i = 0; column_i < this.mTotalColumnCount; column_i++) {
                MovieSeatColumnInfo detailInfo = seatInfo.detail[column_i];
                if (detailInfo.n.equals(MovieSeatColumnInfo.AISLE_IDENTIFY)) {
                    //此处没有座位区域
                    continue;
                }
                //座位区域
                if (!detailInfo.damagedFlg.equals(MovieSeatColumnInfo.USABLE_IDENTIFY)) {
                    //损坏座位区域
                    drawThumbBitmap(canvas, this.mSeatSoldBitmap, this.mPaint, column_i, row_i);
                    continue;
                }
                switch (detailInfo.state) {
                    case MovieSeatColumnInfo.SOLDSEATSTATE:
                        drawThumbBitmap(canvas, this.mSeatSoldBitmap, this.mPaint, column_i, row_i);
                        break;
                    case MovieSeatColumnInfo.CHECKEDSTATE:
                        //画已选区域
                        drawThumbBitmap(canvas, this.mSeatCheckedBitmap, this.mPaint, column_i, row_i);
                        break;
                    default: {
                        //可用座位
                        if (!detailInfo.loveInd.equals(MovieSeatColumnInfo.LOVER_IDENTIFY0)) {
                            //情侣座区域
                            drawThumbBitmap(canvas, this.mSeatSoldBitmap, this.mPaint, column_i, row_i);
                            break;
                        }
                        //普通区域
                        drawThumbBitmap(canvas, this.mSeatNormalBitmap, this.mPaint, column_i, row_i);
                    }
                }
            }
        }
    }

    private void drawThumbBitmap(Canvas canvas, Bitmap bitmap, Paint paint, int seatNum, int rowNum) {
        if (bitmap != null) {
            if (this.showThumb) {
                canvas.drawBitmap(bitmap, null, getThumbSeatRect(seatNum, rowNum), paint);
            }
        }
    }


    /**
     * 画缩略图矩形
     *
     * @param x
     * @param y
     * @return
     */
    private Rect getThumbLocationAreaRect(float x, float y) {
        try {
            float tempMoveX = x - this.seatv_margin_left;
            float tempMoveY = y - this.seatv_margin_top;
            float offsetXLeft = (tempMoveX * this.thumbSeatVWRatio);
            float offsetXRight = x * this.thumbSeatVWRatio;
            float offsetYTop = (tempMoveY * this.thumbSeatVWRatio);
            float offsetYBottom = y * this.thumbSeatVWRatio;
            if (offsetXLeft < 0) {
                offsetXLeft = 0;
            }
            float left = this.mThumbLocationAreaLeftLimit + offsetXLeft;
            float right;
            float tempThumbLocationRectWidth = this.mThumbLocationAreaRightLimit - left;
            if (tempThumbLocationRectWidth < this.thumbLocationRectWidth) {
                right = this.mThumbLocationAreaLeftLimit + offsetXRight + tempThumbLocationRectWidth;
            } else {
                right = this.mThumbLocationAreaLeftLimit + offsetXRight + this.thumbLocationRectWidth;
            }
            LogHelper.d("[%S] [ThumbLocationAreaRect] before right== %S", TAG, right);
            if (right > this.mThumbLocationAreaRightLimit) {
                right = this.mThumbLocationAreaRightLimit;
            }
            LogHelper.d("[%S] [ThumbLocationAreaRect] right== %S", TAG, right);
            if (offsetYTop <= 0) {
                offsetYTop = 0;
            }
            float top = this.mThumbLocationAreaTopLimit + offsetYTop;
            float bottom;
            float tempThumbLocationRectHeight = this.mThumbLocationAreaBottomLimit - top;
            if (tempThumbLocationRectHeight < this.thumbLocationRectHeight) {
                bottom = this.mThumbLocationAreaTopLimit + offsetYBottom + tempThumbLocationRectHeight;
            } else {
                bottom = this.mThumbLocationAreaTopLimit + offsetYBottom + this.thumbLocationRectHeight;
            }
            if (bottom > this.mThumbLocationAreaBottomLimit) {
                bottom = this.mThumbLocationAreaBottomLimit;
            }
            this.mThumbLocationAreaRect.set(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
            return this.mThumbLocationAreaRect;

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return new Rect();
        }
    }

    /**
     * 画缩放图的座位
     *
     * @param seatNum
     * @param rowNum
     * @return
     */
    private Rect getThumbSeatRect(int seatNum, int rowNum) {
        try {
            int left = Math.round(this.mThumbLocationAreaLeftLimit + seatNum * (this.seat_init_width * this.thumbratio)
                    + seatNum * (this.seat_init_horizontal_margin * this.thumbratio));
            int top = Math.round(this.mThumbLocationAreaTopLimit + rowNum * (this.seat_init_height * this.thumbratio)
                    + rowNum * (this.seat_init_vertical_margin * this.thumbratio));
            int right = Math.round(this.mThumbLocationAreaLeftLimit + (seatNum + 1) * (this.seat_init_width * this.thumbratio)
                    + seatNum * (this.seat_init_horizontal_margin * this.thumbratio));
            int bottom = Math.round(this.mThumbLocationAreaTopLimit + (rowNum + 1) * (this.seat_init_height * this.thumbratio)
                    + rowNum * (this.seat_init_vertical_margin * this.thumbratio));
            this.mThumbSeatRect.set(left, top, right, bottom);
            return this.mThumbSeatRect;
        } catch (Exception localException) {
            localException.printStackTrace();
            return new Rect();
        }
    }

    /**
     * TODO: 缩略图
     * End Position
     */


    /**
     * TODO: 排轴
     * Start Position
     */
    //参数

    /**
     * 排数坐标距底边的偏移量
     */
    private int rownum_axis_margin_top_offset = 16;
    /**
     * 排轴背景的角度半径
     */
    private static int rowBgRx = 36;
    private static int rowBgRy = 36;
    /**
     * 排轴字体大小
     */
    private static int rownum_axis_font_size;
    private static float row_axis_num_tv_font_height;
    /**
     * 排轴的背景颜色
     */
    private static int SEATROWAXISBG;

    /**
     * 排轴的背景颜色
     */
    private static int SEATTHUMBBG;
    private static int ROWAXISTEXTCOLOR;


    private static int row_axis_bg_width;
    private static int rownum_axis_margin_offset;

    private void drawRowAxisV(Canvas canvas) {
        drawRowAxisBg(canvas);
        drawRowAxisTv(canvas);
    }

    //私有函數
    private void drawRowAxisBg(Canvas canvas) {
        calculateSeartRowAixsVParams();
        this.mRowAxisTextPaint.setColor(this.SEATROWAXISBG);
        canvas.drawRoundRect(this.mRowAxisBgRect, this.rowBgRx, this.rowBgRy, this.mRowAxisTextPaint);
        drawRowAxisTv(canvas);
    }

    private void drawRowAxisTv(Canvas canvas) {
        this.mRowAxisTextPaint.setColor(this.ROWAXISTEXTCOLOR);
        float tvX;
        float tvY;
        for (int row_i = 0; row_i < this.mTotalRow; row_i++) {
            String columnNum = this.mSeatInfoList[row_i].desc;
            if (!columnNum.equals("0")) {
                int rowNumMarginBgleft = Math.round(this.row_axis_bg_width / 2);
                tvX = Math.abs(getX()) + this.rownum_axis_margin_offset + rowNumMarginBgleft;
                tvY = this.seatv_margin_top + (row_i + 1) * this.seat_current_height + row_i * this.seat_vertical_margin
                        - (this.seat_current_height - this.row_axis_num_tv_font_height) / 2;
                canvas.drawText(columnNum, tvX, tvY, this.mRowAxisTextPaint);
            }
        }
    }
    /**
     * TODO: 排轴
     * End Position
     */

    /**
     * TODO: 中轴线
     * Start Position
     */
    private float centerline_width = 1.0F;

    /**
     * 屏幕中央背景的角度半径
     */
    private static int screenCenterBgRx = 36;
    private static int screenCenterBgRy = 36;

    private static String mMovieSeatScreenCenterTv;
    private static float screen_center_tv_font_size;
    private static float screen_center_tv_font_height;
    private static float screen_center_tv_width;
    private static float screen_center_tv_height;
    private static int screen_center_tv_paddingH;
    private static int screen_center_tv_paddingV;
    /**
     * 中线颜色
     */
    private static int CENTERLINECOLOR;
    /**
     * 中央屏幕背景颜色
     */
    private static int SCREENCENTERTVBGCOLOR;
    /**
     * 中央屏幕字体颜色
     */
    private static int SCREENCENTERTVCOLOR;
    /**
     * 中线数据
     */
    private float centerline_x;
    private float centerline_top_y;
    private float centerline_bottom_y;

    // 私有函數

    private void drawRoomCenterV(Canvas canvas) {
        drawRoomCenterLineBitmap(canvas);
        drawRoomCenterTvBitmap(canvas);
    }

    private void drawRoomCenterLineBitmap(Canvas canvas) {
        //中线
        this.mScreenCenterTextPaint.setStrokeWidth(this.centerline_width);
        this.mScreenCenterTextPaint.setColor(this.CENTERLINECOLOR);
        canvas.drawLine(this.centerline_x, this.centerline_top_y, this.centerline_x,
                this.centerline_bottom_y, this.mScreenCenterTextPaint);
    }

    private void calculateTextWidth() {
        if (this.screen_center_tv_font_height >= this.screen_center_tv_height) {
            this.screen_center_tv_height = this.screen_center_tv_font_height + screen_center_tv_paddingH;
        }
        if (this.mScreenCenterTextPaint.measureText(this.mMovieSeatScreenCenterTv) + screen_center_tv_paddingV >= (this.mRoomCentertvBgRectF.width())) {
            this.screen_center_tv_width = this.mScreenCenterTextPaint.measureText(this.mMovieSeatScreenCenterTv) + screen_center_tv_paddingV;
        }
        int centertv_left = Math.round(this.centerline_x - screen_center_tv_width / 2);
        int centertv_right = Math.round(this.centerline_x + screen_center_tv_width / 2);
        int centertv_top = (int) (this.centerline_top_y - this.screen_center_tv_height);
        this.mRoomCentertvBgRectF.set(centertv_left, centertv_top, centertv_right, this.centerline_top_y);
    }

    private void drawRoomCenterTvBitmap(Canvas canvas) {
        //画文字框
        this.mScreenCenterTextPaint.setColor(this.SCREENCENTERTVBGCOLOR);
        calculateTextWidth();
        canvas.drawRoundRect(this.mRoomCentertvBgRectF, this.screenCenterBgRx, this.screenCenterBgRy, this.mScreenCenterTextPaint);
        //画文字
        this.mScreenCenterTextPaint.setColor(this.SCREENCENTERTVCOLOR);
        float y = this.centerline_top_y - this.screen_center_tv_height / 3;
        Paint.FontMetricsInt fontMetrics = this.mScreenCenterTextPaint.getFontMetricsInt();
        int baseline = (int) ((this.mRoomCentertvBgRectF.bottom + this.mRoomCentertvBgRectF.top - fontMetrics.bottom - fontMetrics.top) / 2);
        this.mScreenCenterTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(this.mMovieSeatScreenCenterTv, this.centerline_x, baseline, this.mScreenCenterTextPaint);
    }

    /**
     * TODO: 中轴线
     * End Position
     */


    public void movieLog(String message, Object... obj) {
        log(MOVIESEATPARAMSLOG, "[scroll] " + message, obj);
    }

    public void touchLog(String message, Object... obj) {
        log(TOUNCHLOG, "[onTouchEvent] " + message, obj);
    }

    public void zoomLog(String message, Object... obj) {
        log(ZOOMSEATPARAMSLOG, "[zoomView] " + message, obj);
    }

    public void xyLog(String message, Object... obj) {
        log(POSITIONXYLOG, "[XY] " + message, obj);
    }

    public void log(boolean showTag, String message, Object... args) {
        if (showTag) {
            LogHelper.d("[" + TAG + "] " + message, args);
        }
    }
}
