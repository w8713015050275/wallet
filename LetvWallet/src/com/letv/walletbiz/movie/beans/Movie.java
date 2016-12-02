package com.letv.walletbiz.movie.beans;

import android.text.TextUtils;

import com.letv.wallet.common.http.beans.LetvBaseBean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuliang on 15-12-30.
 */
public class Movie implements LetvBaseBean {

    public static Pattern mPattern = Pattern.compile("(^\\d+)");

    //电影id
    public long id;

    //电影名
    public String name;

    //英文名称
    public String en_name;

    //简介
    public String remark;

    //短评WS
    public String simple_remark;

    //评分
    public float score;

    //是否为预售
    public int is_booking;

    //想看的人数
    public int want_count;

    //主演，多个主演用”/“分割的
    public String actor;

    //导演，多个导演用”/“分割的
    public String director;

    //片长
    public String longs;

    private int movieLongs = -1;

    //影片所属地区（国家）
    public String country;

    //上映时间
    public String date;

    //封面图
    public String poster_url;

    //标签（类型）
    public String tags;

    //该影片总在上映的影院数
    public int total_cinema;

    //该影片总共排期数（列表根据该值倒序排序）
    public int total_schedule;

    //影片特效（版本）
    public String version;

    public String[] versionArray;

    //当前城市下此电影的排期日期
    public String[] sche_date;

    //电影所属的关注词的id
    public int main_follow_tag;

    public int getMovieLongs() {
        if (movieLongs == -1) {
            movieLongs = getIntLongs(longs);
        }
        return movieLongs;
    }

    private int getIntLongs(String longs) {
        if (TextUtils.isEmpty(longs)) {
            return -1;
        }
        Matcher matcher = mPattern.matcher(longs.trim());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    public String[] getVersionArray() {
        if (TextUtils.isEmpty(version)) {
            return null;
        }
        if (versionArray == null) {
            versionArray = version.split("/");
        }
        return versionArray;
    }

}
