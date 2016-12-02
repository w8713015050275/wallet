package com.letv.leui.common.recommend.widget.adapter.dto;

/**
 * Created by dupengtao on 15-1-16.
 */
public class RecommendVideoDTO {

    private String type;
    private String id;
    private String recommendId;
    private RecommendVideoContent content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecommendId() {
        return recommendId;
    }

    public void setRecommendId(String recommendId) {
        this.recommendId = recommendId;
    }

    public RecommendVideoContent getContent() {
        return content;
    }

    public void setContent(RecommendVideoContent content) {
        this.content = content;
    }
}
