package com.letv.walletbiz.movie.beans;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.movie.utils.DrawableUtils;

/**
 * Created by liuliang on 16-2-29.
 */
public class MovieOrder implements OrderBaseBean, BaseMovieOrder {

    public static final int MOVIE_TICKET_STATUS_UNKNOWN = 0;
    public static final int MOVIE_TICKET_STATUS_UNPAY = 1;
    public static final int MOVIE_TICKET_STATUS_OVERTIME = 2;
    public static final int MOVIE_TICKET_STATUS_GENERATING_TICKET = 3;
    public static final int MOVIE_TICKET_STATUS_GENERATE_TICKET_FAILED = 4;
    public static final int MOVIE_TICKET_STATUS_UNCONSUMED = 5;
    public static final int MOVIE_TICKET_STATUS_REFUNDING = 6;
    public static final int MOVIE_TICKET_STATUS_REFUNDED = 7;
    public static final int MOVIE_TICKET_STATUS_SHOWN = 8;

    public static final int MOVIE_TICKET_PROGRESS_CANCEL = 1;
    public static final int MOVIE_TICKET_PROGRESS_UNPAY = 2;
    public static final int MOVIE_TICKET_PROGRESS_GENERATING_TICKET = 5;
    public static final int MOVIE_TICKET_PROGRESS_UNCONSUMED = 15;
    public static final int MOVIE_TICKET_PROGRESS_SHOWN = 20;

    public long rank_id;

    /**
     * 订单编号
     */
    public String order_no;

    /**
     * 微票订单号
     */
    public String third_no;

    /**
     * 订单状态
     * <p/>
     * 1	已取消
     * 2	待支付
     * 5	出票中（已支付）
     * 15	已发码（用户已收到取票码）
     * 20	已放映（已完成）
     */
    public int progress;

    /**
     * 电影名称
     */
    public String movie_name;

    /**
     * 电影id
     */
    public long movie_id;

    /**
     * 影片封面图
     */
    public String poster_url;

    /**
     * 电影院名称
     */
    public String cinema_name;

    /**
     * 电影院id
     */
    public int cinema_id;

    /**
     * 电影院地址
     */
    public String cinema_addr;

    /**
     * 影院电话
     */
    public String cinema_tele;

    /**
     * 纬度
     */
    public double latitude;

    /**
     * 经度
     */
    public double longitude;

    /**
     * 订单总价（折扣后的价格）
     */
    public float price;

    /**
     * 原价
     */
    public float original_price;

    /**
     * 是否有“特价活动”优惠，1：有，0：无
     */
    public int has_discount_activity;

    /**
     * 特价活动优惠信息
     */
    public LockSeatOrder.Discount discount_activity;

    /**
     * 是否有退款
     */
    public int is_refund;

    /**
     * 子状态
     * 1: 锁座成功未支付
     * 2: 未发码（出票中）
     * 6: 已发码
     * 11: 发码失败（出票成功，短信发送失败）
     * 20 :已出票
     * 21: 退款中
     * 22: 退款已驳回
     * 23: 已退款
     * 24: 退款审批通过
     * 25: 订单已解锁且未支付
     * 26: 出票失败
     */
    public int refund_progress;

    /**
     * 锁座起始时间
     */
    public long lock_time;

    /**
     * 锁座超时时间
     */
    public int lock_expire_time;

    /**
     * 订单创建时间
     */
    public long add_time;

    /**
     * 电影票详情
     */
    public TicketInfo ticket_info;

    /**
     * 取票码，如果有竖线分隔，则取票码是双码（格式："序列号|兑换码"），否则是单码
     */
    public String code;


    //是否已经上传数据埋点
    public boolean upData = false;


    public static class TicketInfo implements LetvBaseBean {

        /**
         * 排期id
         */
        public String mpid;

        /**
         * 影票数量
         */
        public int num;

        /**
         * 放映日期
         */
        public String date;

        /**
         * 放映时间
         */
        public String time;

        /**
         * 播放语种
         */
        public String language;

        /**
         * 放映类型
         */
        public String type;

        /**
         * 影厅id，字符串格式
         */
        public String roomid;

        /**
         * 影厅名称
         */
        public String roomname;

        /**
         * 座位信息：【行:列|行:列...】
         */
        public String seat;
    }

    public int getTicketStatus() {
        int result = MOVIE_TICKET_STATUS_UNKNOWN;
        if (progress == 20) {
            return MOVIE_TICKET_STATUS_SHOWN;
        }
        if (progress == 2 && (System.currentTimeMillis() / 1000 - lock_time) > lock_expire_time) {
            return MOVIE_TICKET_STATUS_OVERTIME;
        }
        switch (refund_progress) {
            case 1:
                result = MOVIE_TICKET_STATUS_UNPAY;
                break;
            case 2:
                result = MOVIE_TICKET_STATUS_GENERATING_TICKET;
                break;
            case 6:
            case 11:
            case 20:
                result = MOVIE_TICKET_STATUS_UNCONSUMED;
                break;
            case 21:
            case 24:
                result = MOVIE_TICKET_STATUS_REFUNDING;
                break;
            case 23:
                result = MOVIE_TICKET_STATUS_REFUNDED;
                break;
            case 25:
                result = MOVIE_TICKET_STATUS_OVERTIME;
                break;
            case 26:
                result = MOVIE_TICKET_STATUS_GENERATE_TICKET_FAILED;
                break;
        }
        return result;
    }

    public int getTicketStatusResId() {
        int status = getTicketStatus();
        return getTicketStatusResId(status);
    }

    public int getTicketStatusResId(int status) {
        int result = R.string.movie_ticket_status_unknown;
        switch (status) {
            case MOVIE_TICKET_STATUS_UNPAY:
                result = R.string.movie_ticket_status_unpay;
                break;
            case MOVIE_TICKET_STATUS_OVERTIME:
                result = R.string.movie_ticket_status_overtime;
                break;
            case MOVIE_TICKET_STATUS_GENERATING_TICKET:
                result = R.string.movie_ticket_status_generating_ticket;
                break;
            case MOVIE_TICKET_STATUS_UNCONSUMED:
                result = R.string.movie_ticket_status_unconsumed;
                break;
            case MOVIE_TICKET_STATUS_REFUNDING:
                result = R.string.movie_ticket_status_refunding;
                break;
            case MOVIE_TICKET_STATUS_REFUNDED:
                result = R.string.movie_ticket_status_refunded;
                break;
            case MOVIE_TICKET_STATUS_GENERATE_TICKET_FAILED:
                result = R.string.movie_ticket_status_generate_ticket_failed;
                break;
            case MOVIE_TICKET_STATUS_SHOWN:
                result = R.string.movie_ticket_status_shown;
                break;
        }
        return result;
    }

    public int getTicketStatusColorResId(int status) {
        int result = R.color.movie_primary_color;
        switch (status) {
            case MOVIE_TICKET_STATUS_GENERATING_TICKET:
            case MOVIE_TICKET_STATUS_UNCONSUMED:
                result = R.color.movie_primary_color;
                break;
            case MOVIE_TICKET_STATUS_UNPAY:
            case MOVIE_TICKET_STATUS_REFUNDING:
            case MOVIE_TICKET_STATUS_GENERATE_TICKET_FAILED:
                result = R.color.movie_btn_order_status_unpay;
                break;
            case MOVIE_TICKET_STATUS_SHOWN:
            case MOVIE_TICKET_STATUS_REFUNDED:
            case MOVIE_TICKET_STATUS_OVERTIME:
                result = R.color.movie_btn_order_status_shown;
                break;

        }
        return result;
    }

    public int getTicketRedeemColorResId(int status) {
        int result = R.color.black;
        switch (status) {
            case MOVIE_TICKET_STATUS_SHOWN:
            case MOVIE_TICKET_STATUS_REFUNDED:
            case MOVIE_TICKET_STATUS_OVERTIME:
                result = R.color.movie_btn_order_status_shown;
                break;
        }
        return result;
    }

    public Drawable getTicketStatusBgResId(int status, Context mContext) {
        Drawable result;
        switch (status) {
            case MOVIE_TICKET_STATUS_GENERATING_TICKET:
            case MOVIE_TICKET_STATUS_UNCONSUMED:
                result = DrawableUtils.getMovieBtnDrawable(mContext);
                break;
            case MOVIE_TICKET_STATUS_UNPAY:
            case MOVIE_TICKET_STATUS_REFUNDING:
            case MOVIE_TICKET_STATUS_GENERATE_TICKET_FAILED:
                result = DrawableUtils.getMovieRedBtnDrawable(mContext);
                break;
            case MOVIE_TICKET_STATUS_SHOWN:
            case MOVIE_TICKET_STATUS_REFUNDED:
            case MOVIE_TICKET_STATUS_OVERTIME:
                result = DrawableUtils.getMovieGreyBtnDrawable(mContext);
                break;
            default:
                result = DrawableUtils.getMovieBtnDrawable(mContext);
                break;

        }
        return result;
    }

    @Override
    public String getMovieOrderNo() {
        return order_no;
    }

    @Override
    public float getMoviePrice() {
        return price;
    }

    @Override
    public String getMovieSeat() {
        return ticket_info != null ? ticket_info.seat : null;
    }

    @Override
    public long getLockTime() {
        return lock_time;
    }

    @Override
    public int getLockExpireTime() {
        return lock_expire_time;
    }

    @Override
    public float getMovieOriginalPrice() {
        return original_price;
    }

    @Override
    public int hasDiscount() {
        return has_discount_activity;
    }

    @Override
    public LockSeatOrder.Discount getMovieDiscount() {
        return discount_activity;
    }

    @Override
    public long getOrderCTime() {
        return add_time * 1000;
    }

    @Override
    public String getOrderId() {
        return order_no;
    }

    @Override
    public long getRankId() {
        return rank_id;
    }

    @Override
    public String getPrice() {
        return String.valueOf(price);
    }
}
