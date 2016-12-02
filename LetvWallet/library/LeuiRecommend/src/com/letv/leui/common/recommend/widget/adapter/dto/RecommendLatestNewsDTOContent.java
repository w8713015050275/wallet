package com.letv.leui.common.recommend.widget.adapter.dto;

import java.util.List;

/**
 * Created by dupengtao on 15-1-6.
 */
public class RecommendLatestNewsDTOContent {

    private String date;
    private String source_type;
    private List<String> content_imgs;
    private String url;
    private String title;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getContent_imgs() {
        return content_imgs;
    }

    public void setContent_imgs(List<String> content_imgs) {
        this.content_imgs = content_imgs;
    }
}
