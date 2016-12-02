package com.letv.leui.common.recommend.report;

/**
 * Created by dupengtao on 15-3-12.
 */
public interface IReportData {

    void reportRecommendExposeView();

    void reportHotProductExposeModule();//HOT_PRODUCT

    void reportLastNewsExposeModule();//LATEST_NEWS

    void reportWallpaperExposeModule();//WALLPAPER

    void reportMediaNewsExposeModule();//MEDIA_NEWS

    void reportCalendarExposeModule();//CALENDAR

    void reportMusicExposeModule();//MUSIC

    void reportWeiBoExposeModule();//WEI_BO

    void reportHotProductJump(String id);//HOT_PRODUCT

    void reportHotProductMoreJump();//HOT_PRODUCT

    void reportLastNewsJump(String id);//LATEST_NEWS

    void reportLastNewsMoreJump();//LATEST_NEWS

    void reportWallpaperJump(String id);//WALLPAPER

    void reportWallpaperMoreJump();//WALLPAPER

    void reportMediaNewsJump(String id);//MEDIA_NEWS

    void reportCalendarJump(String id);//WALLPAPER

    void reportCalendarMoreJump();//WALLPAPER

    void reportMusicJump(String id);//MUSIC

    void reportMusicMoreJump();//MUSIC

    void reportWeiBoJump(String id);//WeiBo

    void reportArtistsJump();//Artists

    void reportArtistsMoreJump();

    void reportAlbumJump(String ZJid);//Album

    void reportAlbumMoreJump();
}
