package com.letv.leui.common.recommend.widget.adapter.dto;

/**
 * Created by zhangjiahao on 15-5-27.
 */
public class RecommendAlbumDTO {

    private int id;
    private int score;
    private String type;
    private RecommendAlbumDTOContent content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RecommendAlbumDTOContent getContent() {
        return content;
    }

    public void setContent(RecommendAlbumDTOContent content) {
        this.content = content;
    }
}
