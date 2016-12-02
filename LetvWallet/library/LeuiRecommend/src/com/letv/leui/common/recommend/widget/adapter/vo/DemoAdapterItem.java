package com.letv.leui.common.recommend.widget.adapter.vo;

/**
 * Created by dupengtao on 14-11-17.
 */
public class DemoAdapterItem {

    private String itemName;
    private String itemUrl;

    public DemoAdapterItem(String itemName, String itemUrl) {
        this.itemName = itemName;
        this.itemUrl = itemUrl;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }
}
