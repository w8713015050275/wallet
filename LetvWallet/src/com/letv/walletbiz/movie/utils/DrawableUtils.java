package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.letv.walletbiz.R;

/**
 * Created by lijujying on 16-10-14.
 */

public class DrawableUtils {


    public static Drawable getMovieBtnDrawable(Context mContext){
        if(mContext == null) return null;
        return getDrawableByThemeId(mContext, com.le.eui.support.widget.R.drawable.le_btn_default_eui_holo, R.style.MovieTheme);
    }

    public static Drawable getMovieYellowBtnDrawable(Context mContext){
        if(mContext == null) return null;
        return getDrawableByThemeId(mContext, com.le.eui.support.widget.R.drawable.le_btn_default_eui_holo, R.style.MovieTheme_MovieListSmallYellow);
    }

    public static Drawable getMovieRedBtnDrawable(Context mContext){
        if(mContext == null) return null;
        return getDrawableByThemeId(mContext, com.le.eui.support.widget.R.drawable.le_btn_default_eui_holo, R.style.MovieTheme_MovieListSmallRed);
    }

    public static Drawable getMovieGreyBtnDrawable(Context mContext){
        if(mContext == null) return null;
        return getDrawableByThemeId(mContext, com.le.eui.support.widget.R.drawable.le_btn_default_eui_holo, R.style.MovieTheme_MovieListSmallGrey);
    }

    public static Drawable getDrawableByThemeId(Context mContext, int resDrawableId, int resThemeId){
        if(mContext == null) return null;
        return mContext.getResources().getDrawable(resDrawableId, getButtonThemeById(mContext, resThemeId));
    }

    public static Resources.Theme getButtonThemeById(Context mContext , int id) {
        if(mContext == null) return null;
        Resources.Theme theme = mContext.getResources().newTheme();
        theme.applyStyle(id, true);
        return theme;
    }
}
