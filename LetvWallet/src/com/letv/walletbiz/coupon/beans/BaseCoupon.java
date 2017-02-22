package com.letv.walletbiz.coupon.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by lijunying on 16-4-18.
 */
public class BaseCoupon implements LetvBaseBean {

    public static final int JUMP_TYPE_APP = 1;
    public static final int JUMP_TYPE_WEB = 2;

    public static final int STATE_UNUSE = 1;
    public static final int STATE_USED = 5;
    public static final int STATE_EXPIRED = 10;

    public long ucoupon_id;
    public String ucoupon_code;
    public long rank_id;
    public int type;
    public String title;
    public String service_name;
    public String use_condition;
    public String use_detail_link;
    public String icon;
    public int jump_type;
    public String jump_param;
    public String package_name;
    public String jump_link;
    public long start_time;
    public long end_time;
    public String valid_date_desc;
    public int state;
    public CouponItem[] showItems;
    //是否已经上传数据埋点
    public boolean upData = false;

    public static class CouponItem implements LetvBaseBean {

        public String key;
        public String name;
        public String value;
        public int rank;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public int getRank() {
            return rank;
        }

    }

    public String getTitle() {
        return title;
    }

    public String getService_name() {
        return service_name;
    }


    public long getUcoupon_id() {
        return ucoupon_id;
    }

    public void setUcoupon_id(long ucoupon_id) {
        this.ucoupon_id = ucoupon_id;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getUcoupon_code() {
        return ucoupon_code;

    }

    public void setUcoupon_code(String ucoupon_code) {
        this.ucoupon_code = ucoupon_code;
    }


    public String getUse_detail_link() {
        return use_detail_link;
    }

    public void setUse_detail_link(String use_detail_link) {
        this.use_detail_link = use_detail_link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getJump_type() {
        return jump_type;
    }

    public void setJump_type(int jump_type) {
        this.jump_type = jump_type;
    }

    public String getJump_param() {
        return jump_param;
    }

    public void setJump_param(String jump_param) {
        this.jump_param = jump_param;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getJump_link() {
        return jump_link;
    }

    public void setJump_link(String jump_link) {
        this.jump_link = jump_link;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CouponItem[] getShowItems() {
        return showItems;
    }

    public void setShowItems(CouponItem[] showItems) {
        this.showItems = showItems;
    }

    public String getUse_condition() {
        return use_condition;
    }

    public void setUse_condition(String use_condition) {
        this.use_condition = use_condition;
    }

    public long getRank_id() {
        return rank_id;
    }

    public void setRank_id(long rank_id) {
        this.rank_id = rank_id;
    }
}
