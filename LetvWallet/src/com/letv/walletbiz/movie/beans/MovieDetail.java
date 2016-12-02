package com.letv.walletbiz.movie.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-3-17.
 */
public class MovieDetail implements LetvBaseBean {

    //致新作品库id
    public long id;

    //大作品库/乐搜id
    public long albumId;

    //微票电影id
    public long movie_id;

    //电影名称
    public String name;

    //电影时长
    public String longs;

    //电影简介
    public String description;

    //电影类型 eg:"科幻\/喜剧\/爱情"
    public String category;

    //上映时间
    public String relaseDate;

    //导演/明星
    public MovieStar[] peopleTag;

    //电影关注词，为了兼容后续，返回多个，客户端可取第一个作为展示
    public WorkTag[] workTag;

    public MoviePhoto pictures;

    //预告片
    public MoviePrevue[] prevue;

    //是否可购买：1-可购买，0-不可购买（无排期）
    public int ticketBuy;

    //当前城市下此电影的排期日期 eg:"2015-12-29","2016-03-03"
    public String[] sche_date;

    public static class MovieStar implements LetvBaseBean {

        //关注词id
        public long tag_id;

        //关注词名称
        public String name;

        //小图标
        public String icon;
    }

    public static class WorkTag implements LetvBaseBean {

        //关注词id
        public long tag_id;

        //关注词名称
        public String name;

        //多少人关注
        public String follow_count;

        //小图标
        public String icon;
    }

    public static class MovieAllSizePhoto implements LetvBaseBean, Parcelable {

        public String small;

        public String large;

        public String middle;

        protected MovieAllSizePhoto(Parcel in) {
            small = in.readString();
            large = in.readString();
            middle = in.readString();
        }

        public static final Creator<MovieAllSizePhoto> CREATOR = new Creator<MovieAllSizePhoto>() {
            @Override
            public MovieAllSizePhoto createFromParcel(Parcel in) {
                return new MovieAllSizePhoto(in);
            }

            @Override
            public MovieAllSizePhoto[] newArray(int size) {
                return new MovieAllSizePhoto[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(small);
            dest.writeString(large);
            dest.writeString(middle);
        }
    }

    public static class MoviePhoto implements LetvBaseBean {

        //960*540格式的背板图
        public String backgroundImg;

        //剧照，三种尺寸
        public MovieAllSizePhoto[] picAll;
    }

    public static class MoviePrevue implements LetvBaseBean {

        //预告片标题
        public String title;

        //封面
        public String cover;

        //视频ID
        public long mid;
    }

}
