package com.letv.walletbiz.movie.beans;

import android.util.SparseArray;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuliang on 16-3-24.
 */
public class CinemaFilterBean implements LetvBaseBean {

    public static final int CINEMA_CATEGORY_AREA = 1;
    public static final int CINEMA_CATEGORY_BRAND = 2;
    public static final int CINEMA_CATEGORY_SPECIAL = 3;

    private SparseArray<List<String>> nameMap = new SparseArray<>();

    private SparseArray<HashMap<String, List<CinemaList.Cinema>>> cinemaListArray = new SparseArray<>();

    public SparseArray<List<String>> getNameMap() {
        return nameMap;
    }

    public List<String> getNameArray(int category) {
        return nameMap.get(category);
    }

    public SparseArray<HashMap<String, List<CinemaList.Cinema>>> getCinemaListArray() {
        return cinemaListArray;
    }

    public HashMap<String, List<CinemaList.Cinema>> getCinemaMap(int category) {
        return cinemaListArray.get(category);
    }

    public List<CinemaList.Cinema> getCinemaList(int category, String secCategory) {
        HashMap<String, List<CinemaList.Cinema>> map = cinemaListArray.get(category);
        if (map != null) {
            return map.get(secCategory);
        }
        return null;
    }

    public void addName(int category, ArrayList<String> nameArray) {
        nameMap.put(category, nameArray);
    }

    public void addCinemaList(int category, HashMap<String, List<CinemaList.Cinema>> map) {
        cinemaListArray.put(category, map);
    }

    public int getCategoryNameRes(int category) {
        int resId = -1;
        switch (category) {
            case CINEMA_CATEGORY_AREA:
                resId = R.string.movie_cinema_filter_category_area;
                break;
            case CINEMA_CATEGORY_BRAND:
                resId = R.string.movie_cinema_filter_category_brand;
                break;
            case CINEMA_CATEGORY_SPECIAL:
                resId = R.string.movie_cinema_filter_category_special;
                break;
        }
        return resId;
    }
}
