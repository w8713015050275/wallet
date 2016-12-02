package com.letv.leui.common.recommend.widget.adapter.dto;

/**
 * Created by zhangjiahao on 15-5-28.
 */
public class RecommendArtistsDTO {
    private int tag_id;
    private String name;
    private String logo;

    public int getTag_id() {
        return tag_id;
    }

    public void setTag_id(int tag_id) {
        this.tag_id = tag_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
