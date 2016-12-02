package com.letv.leui.common.recommend.widget.adapter.vo;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendLatestNewsVO {
    private String latestNewsTitle;
    private String latestNewsPhotoUrl;

    public RecommendLatestNewsVO() {
    }

    public RecommendLatestNewsVO(String latestNewsTitle, String latestNewsPhotoUrl) {
        this.latestNewsTitle = latestNewsTitle;
        this.latestNewsPhotoUrl = latestNewsPhotoUrl;
    }

    public String getLatestNewsTitle() {
        return latestNewsTitle;
    }

    public void setLatestNewsTitle(String latestNewsTitle) {
        this.latestNewsTitle = latestNewsTitle;
    }

    public String getLatestNewsPhotoUrl() {
        return latestNewsPhotoUrl;
    }

    public void setLatestNewsPhotoUrl(String latestNewsPhotoUrl) {
        this.latestNewsPhotoUrl = latestNewsPhotoUrl;
    }
}
