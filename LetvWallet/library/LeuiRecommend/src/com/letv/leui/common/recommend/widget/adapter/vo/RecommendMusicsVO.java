package com.letv.leui.common.recommend.widget.adapter.vo;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendMusicsVO {
    private String musicSongsName;
    private String musicAlbumName;
    private String musicPhotoUrl;

    public RecommendMusicsVO(String musicSongsName, String musicAlbumName, String musicPhotoUrl) {
        this.musicSongsName = musicSongsName;
        this.musicAlbumName = musicAlbumName;
        this.musicPhotoUrl = musicPhotoUrl;
    }

    public String getMusicSongsName() {
        return musicSongsName;
    }

    public void setMusicSongsName(String musicSongsName) {
        this.musicSongsName = musicSongsName;
    }

    public String getMusicAlbumName() {
        return musicAlbumName;
    }

    public void setMusicAlbumName(String musicAlbumName) {
        this.musicAlbumName = musicAlbumName;
    }

    public String getMusicPhotoUrl() {
        return musicPhotoUrl;
    }

    public void setMusicPhotoUrl(String musicPhotoUrl) {
        this.musicPhotoUrl = musicPhotoUrl;
    }
}
