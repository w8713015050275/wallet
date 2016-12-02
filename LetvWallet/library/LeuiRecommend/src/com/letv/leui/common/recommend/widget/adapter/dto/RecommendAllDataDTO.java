package com.letv.leui.common.recommend.widget.adapter.dto;

import java.util.ArrayList;

/**
 * Created by dupengtao on 15-1-6.
 */
public class RecommendAllDataDTO {

    private String tagid;
    private RecommendTaginfoDTO taginfo;
    private ArrayList<RecommendHotDTOItem> hotworks;
    private ArrayList<RecommendLatestNewsDTO> news;
    private ArrayList<RecommendCalendarDTO> calendar;
    private ArrayList<RecommendMusicDTO> music;
    private ArrayList<RecommendWallpaperDTO> wallpaper;
    private ArrayList<RecommendVideoDTO> video;
    private ArrayList<RecommendWeiBoDTO> tweet;
    private ArrayList<RecommendAlbumDTO> album;
    private ArrayList<RecommendArtistsDTO> artists;
    private RecommendTweetDTO tweet2;
    private ArrayList<RecommendSiteDTO> mSites;

    public String getTagid() {
        return tagid;
    }

    public void setTagid(String tagid) {
        this.tagid = tagid;
    }

    public ArrayList<RecommendHotDTOItem> getHotworks() {
        return hotworks;
    }

    public void setHotworks(ArrayList<RecommendHotDTOItem> hotworks) {
        this.hotworks = hotworks;
    }

    public ArrayList<RecommendLatestNewsDTO> getNews() {
        return news;
    }

    public void setNews(ArrayList<RecommendLatestNewsDTO> news) {
        this.news = news;
    }

    public ArrayList<RecommendCalendarDTO> getCalendar() {
        return calendar;
    }

    public void setCalendar(ArrayList<RecommendCalendarDTO> calendar) {
        this.calendar = calendar;
    }

    public ArrayList<RecommendMusicDTO> getMusic() {
        return music;
    }

    public void setMusic(ArrayList<RecommendMusicDTO> music) {
        this.music = music;
    }

    public ArrayList<RecommendWallpaperDTO> getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(ArrayList<RecommendWallpaperDTO> wallpaper) {
        this.wallpaper = wallpaper;
    }

    public ArrayList<RecommendVideoDTO> getVideo() {
        return video;
    }

    public void setVideo(ArrayList<RecommendVideoDTO> video) {
        this.video = video;
    }

    public ArrayList<RecommendWeiBoDTO> getTweet() {
        return tweet;
    }

    public void setTweet(ArrayList<RecommendWeiBoDTO> tweet) {
        this.tweet = tweet;
    }

    public void setAlbum(ArrayList<RecommendAlbumDTO> album) {
        this.album = album;
    }

    public ArrayList<RecommendAlbumDTO> getAlbum() {
        return album;
    }

    public void setArtists(ArrayList<RecommendArtistsDTO> artists) {
        this.artists = artists;
    }

    public ArrayList<RecommendArtistsDTO> getArtists() {
        return artists;
    }

    public RecommendTaginfoDTO getTaginfo() {
        return taginfo;
    }

    public void setTaginfo(RecommendTaginfoDTO taginfo) {
        this.taginfo = taginfo;
    }

    public RecommendTweetDTO getTweetData() {
        return tweet2;
    }

    public void setTweetData(RecommendTweetDTO tweet2) {
        this.tweet2 = tweet2;
    }

    public void setSites(ArrayList<RecommendSiteDTO> sites) {
        mSites = sites;
    }

    public ArrayList<RecommendSiteDTO> getSites() {
        return mSites;
    }
}
