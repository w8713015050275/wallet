package com.letv.leui.common.recommend.widget.adapter.dto;

import com.letv.leui.common.recommend.widget.adapter.vo.RecommendLatestNewsVO;

/**
 * Created by dupengtao on 15-1-6.
 */
public class RecommendLatestNewsDTO extends RecommendLatestNewsVO {

    private String score;
    private String recommendId;
    private String type;
    private String id;
    private RecommendLatestNewsDTOContent content;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRecommendId() {
        return recommendId;
    }

    public void setRecommendId(String recommendId) {
        this.recommendId = recommendId;
    }

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

    public RecommendLatestNewsDTOContent getContent() {
        return content;
    }

    public void setContent(RecommendLatestNewsDTOContent content) {
        this.content = content;
    }
}
