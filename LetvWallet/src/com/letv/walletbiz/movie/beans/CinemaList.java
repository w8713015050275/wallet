package com.letv.walletbiz.movie.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.wallet.common.util.LocationHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;

/**
 * Created by liuliang on 16-1-19.
 */
public class CinemaList implements LetvBaseBean {

    public Cinema[] cinema_list;

    public Area[] area_list;

    public String[] brand_list;

    public String[] special_list;

    public static class Cinema implements Comparable<Cinema>, LetvBaseBean {

        //影院id
        public int id;

        //影院名称
        public String name;

        //影院地址
        public String addr;

        //电影票最高价格
        public float ticket_max_price;

        //电影票最低价格
        public float ticket_min_price;

        //优惠信息
        public String discount_des;

        //今日剩余场次
        public int today_left_sche_count;

        //电话
        public String tele;

        //是否支持在线选座，1：支持， 0：不支持
        public int flag_seat_ticket;

        //纬度
        public double latitude;

        //经度
        public double longitude;

        //当前电影的近期场次
        public String recent_sche;

        //距离
        public double distance;

        //影院所在地区id
        public int area_id;

        //影院所在地区名称
        public String area_name;

        //影院所属品牌
        public String cinema_brand;

        //影院的特效
        public String[] special;

        //// 是否收藏，1：已收藏，0：未收藏
        public int is_favorite;

        public void updateDistance(double latitude1, double longitude1) {
            distance = LocationHelper.getDistance(latitude1, longitude1, latitude, longitude);
        }

        @Override
        public int compareTo(Cinema another) {
            if (is_favorite == another.is_favorite) { //收藏状态相同时, 按距离排序
                return distance < another.distance ? -1 : (distance == another.distance ? 0 : 1);
            } else {
                return is_favorite == MovieTicketConstant.MOVIE_PARAM_FAVORITED ? -1 : 1; //收藏状态不相同时, 已收藏的排前面
            }
        }
    }

    public static class Area implements LetvBaseBean {

        public int id;

        public String name;
    }

}
