package com.letv.walletbiz.movie.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-1-26.
 */
public class CinemaSchedule implements LetvBaseBean {

    //影院id
    public int id;

    //影院名称
    public String name;

    //影院地址
    public String addr;

    //电话
    public String tele;

    //纬度
    public double latitude;

    //经度
    public double longitude;

    // 是否收藏，1：已收藏，0：未收藏
    public int is_favorite;

    //当前影院座位停售时间，单位：分钟，（用于温馨提示）
    public int stop_time;

    public ScheduleMovie[] movie_list;

    // 影院标签列表
    public SpecialInfo[] special_info_list;

    /**
     * Created by liuliang on 16-1-26.
     *
     * MOVIE BEAN:
     * "id": 1745,           // 影片id
     * "director": "曹保平",  // 导演
     * "actor": "邓超/段奕宏/王珞丹/郭涛/高虎/吕颂贤", // 主演
     * "date": "20150123",   // 上映日期
     * "init_score": 82,      // 喜欢的百分比人数
     * "is_discount": 1,     // 是否有折扣（优惠）
     * "longs": "120分钟",    // 影片时长
     * "name": "烈日灼心",     // 影片名称
     * "poster_url": "http://appnfs.wepiao.com/dataImage/movie/poster.jpg", // 影片封面图
     * "score": "8.1",         // 影片平分
     * "want_count": 3823,     // 多少人想看
     * "is_booking": 0,        // 是否预售
     * "tags": "剧情/悬疑/犯罪", // 影片标签
     */
    public static class ScheduleMovie extends Movie {

        //喜欢的百分比人数
        public int init_score;

        //是否有折扣（优惠）
        public int is_discount;

        public ScheduleList[] sche;
    }

    public static class ScheduleList implements LetvBaseBean {

        public String date;

        public Schedule[] detail;

        public DiscountInfo[] discount_info;
    }

    public static class Schedule implements LetvBaseBean, Parcelable {

        //排期id
        public String mpid;

        //影厅id
        public String roomid;

        //影厅名称
        public String roomname;

        //影厅特效
        public String[] roomspecial;

        //播放语种
        public String language;

        //放映时间
        public String time;

        //停止售票时间
        public String stop_time;

        //放映类型
        public String type;

        //座位数
        public int seat_num;

        //有效座位数
        public int seat_num_valid;

        //市场价格
        public float market_price;

        //价格
        public float price;

        //折扣价格
        public float discount;

        //优惠信息描述
        public String discount_des;

        //优惠id
        public String discount_id;

        //优惠介绍
        public String discount_introduce;

        //新人优惠价格
        public float discount_newbie;

        protected Schedule(Parcel in) {
            mpid = in.readString();
            roomid = in.readString();
            roomname = in.readString();
            int length = in.readInt();
            roomspecial = new String[length];
            in.readStringArray(roomspecial);
            language = in.readString();
            time = in.readString();
            stop_time = in.readString();
            type = in.readString();
            seat_num = in.readInt();
            seat_num_valid = in.readInt();
            market_price = in.readFloat();
            price = in.readFloat();
            discount = in.readFloat();
            discount_des = in.readString();
            discount_id = in.readString();
            discount_introduce = in.readString();
            discount_newbie = in.readFloat();
        }

        public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
            @Override
            public Schedule createFromParcel(Parcel in) {
                return new Schedule(in);
            }

            @Override
            public Schedule[] newArray(int size) {
                return new Schedule[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mpid);
            dest.writeString(roomid);
            dest.writeString(roomname);
            dest.writeInt(roomspecial != null ? roomspecial.length : 0);
            dest.writeStringArray(roomspecial);
            dest.writeString(language);
            dest.writeString(time);
            dest.writeString(stop_time);
            dest.writeString(type);
            dest.writeInt(seat_num);
            dest.writeInt(seat_num_valid);
            dest.writeFloat(market_price);
            dest.writeFloat(price);
            dest.writeFloat(discount);
            dest.writeString(discount_des);
            dest.writeString(discount_id);
            dest.writeString(discount_introduce);
            dest.writeFloat(discount_newbie);
        }
    }

    public static class DiscountInfo implements LetvBaseBean, Parcelable {

        public String discount_id;

        public String discount_des;

        public String discount_introduce;

        protected DiscountInfo(Parcel in) {
            discount_id = in.readString();
            discount_des = in.readString();
            discount_introduce = in.readString();
        }

        public static final Creator<DiscountInfo> CREATOR = new Creator<DiscountInfo>() {
            @Override
            public DiscountInfo createFromParcel(Parcel in) {
                return new DiscountInfo(in);
            }

            @Override
            public DiscountInfo[] newArray(int size) {
                return new DiscountInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(discount_id);
            dest.writeString(discount_des);
            dest.writeString(discount_introduce);
        }
    }

    public static class SpecialInfo implements LetvBaseBean {
        public long id;
        public String name;
        public String text;
        public String desc;
    }

}
