package com.letv.leui.common.recommend.widget.adapter.vo;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendHotVO {
    private String hotType;
    private String hotFraction;
    private String hotProduct;
    private String hotPhotoUrl;

    public RecommendHotVO() {
    }

    public RecommendHotVO(String hotType, String hotFraction, String hotProduct, String hotPhotoUrl) {
        this.hotType = hotType;
        this.hotFraction = hotFraction;
        this.hotProduct = hotProduct;
        this.hotPhotoUrl = hotPhotoUrl;
    }

    public String getHotType() {
        return hotType;
    }

    public void setHotType(String hotType) {
        this.hotType = hotType;
    }

    public String getHotFraction() {
        return hotFraction;
    }

    public void setHotFraction(String hotFraction) {
        this.hotFraction = hotFraction;
    }

    public String getHotProduct() {
        return hotProduct;
    }

    public void setHotProduct(String hotProduct) {
        this.hotProduct = hotProduct;
    }

    public String getHotPhotoUrl() {
        return hotPhotoUrl;
    }

    public void setHotPhotoUrl(String hotPhotoUrl) {
        this.hotPhotoUrl = hotPhotoUrl;
    }
}
