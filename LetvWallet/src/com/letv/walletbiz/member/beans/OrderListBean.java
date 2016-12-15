package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import android.content.Context;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.base.http.beans.order.OrderBaseBean;
import com.letv.walletbiz.mobile.util.UiUtils;

public class OrderListBean implements LetvBaseBean {

    public OrderBean[] list;
    public String limit;
    public String offset;

    public static class OrderBean implements OrderBaseBean {

        public String rank_id;
        public String price;
        public String real_price;
        public String order_status;
        public String progress;
        public String order_sn;
        public Snapshot snapshot;
        public String order_ctime;

        @Override
        public long getOrderCTime() {
            return Long.valueOf(order_ctime); //utime 格式
        }

        @Override
        public String getOrderId() {
            return order_sn;
        }

        @Override
        public long getRankId() {
            return Long.parseLong(rank_id);
        }

        @Override
        public String getPrice() {
            return price;
        }

        public String getReal_price() {
            return real_price;
        }

        public String getOrderStatus(Context context) {
            return UiUtils.getOrderStatusStringbyValue(context, Integer.valueOf(order_status));
        }

        @Override
        public String toString() {
            return "OrderBean{" +
                    "rank_id='" + rank_id + '\'' +
                    ", price='" + price + '\'' +
                    ", real_price='" + real_price + '\'' +
                    ", order_status='" + order_status + '\'' +
                    ", progress='" + progress + '\'' +
                    ", order_sn='" + order_sn + '\'' +
                    ", snapshot=" + snapshot +
                    ", order_ctime='" + order_ctime + '\'' +
                    '}';
        }
    }

    public static class Snapshot implements LetvBaseBean {

        public String id;
        public String sku_no;
        public String spu_id;
        public String goods_id;
        public String name;
        public String type;
        public String duration;
        public String month_price;
        public String tag;
        public String merchant;
        public String description;
        public String expiration;
        public String prime_price;
        public String price;
        public String pre_status;
        public String extra;
        public String end_time;
        public String update_time;
        public String op_user;
        public String add_time;

        @Override
        public String toString() {
            return "Snapshot{" +
                    "id='" + id + '\'' +
                    ", sku_no='" + sku_no + '\'' +
                    ", spu_id='" + spu_id + '\'' +
                    ", goods_id='" + goods_id + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", duration='" + duration + '\'' +
                    ", month_price='" + month_price + '\'' +
                    ", tag='" + tag + '\'' +
                    ", merchant='" + merchant + '\'' +
                    ", description='" + description + '\'' +
                    ", expiration='" + expiration + '\'' +
                    ", prime_price='" + prime_price + '\'' +
                    ", price='" + price + '\'' +
                    ", pre_status='" + pre_status + '\'' +
                    ", extra='" + extra + '\'' +
                    ", end_time='" + end_time + '\'' +
                    ", update_time='" + update_time + '\'' +
                    ", op_user='" + op_user + '\'' +
                    ", add_time='" + add_time + '\'' +
                    '}';
        }
    }
}
