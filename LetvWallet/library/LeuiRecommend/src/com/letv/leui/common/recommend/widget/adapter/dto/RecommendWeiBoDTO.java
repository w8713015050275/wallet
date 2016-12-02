package com.letv.leui.common.recommend.widget.adapter.dto;

/**
 * Created by dupengtao on 15-1-26.
 */
public class RecommendWeiBoDTO {

    private String vtype;
    private String type;
    private String id;
    private String recommendId;
    private RecommendWeiBoDTOContent content;

    public String getVtype() {
        return vtype;
    }

    public void setVtype(String vtype) {
        this.vtype = vtype;
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

    public String getRecommendId() {
        return recommendId;
    }

    public void setRecommendId(String recommendId) {
        this.recommendId = recommendId;
    }

    public RecommendWeiBoDTOContent getContent() {
        return content;
    }

    public void setContent(RecommendWeiBoDTOContent content) {
        this.content = content;
    }
}
