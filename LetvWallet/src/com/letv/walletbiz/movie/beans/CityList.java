package com.letv.walletbiz.movie.beans;


import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 15-12-31.
 */
public class CityList implements LetvBaseBean {

    public int curr_city_id;

    public City[] hot;

    public City[] list;

    public int version;

    public int getCurr_city_id() {
        return curr_city_id;
    }

    public void setCurr_city_id(int curr_city_id) {
        this.curr_city_id = curr_city_id;
    }

    public static class City implements Comparable<City>, LetvBaseBean {

        public String pinyin;

        public String name;

        public int id;

        public long last_selected;

        @Override
        public int compareTo(City another) {
            return pinyin.compareTo(another.pinyin);
        }
    }

}
