package com.letv.walletbiz.mobile.beans;

import com.letv.walletbiz.mobile.MobileConstant;

import java.io.Serializable;

/**
 * Created by changjiajie on 16-4-11.
 */
public class OrderSnapshot implements Serializable {

    protected int merchant_id;
    protected String goods_title;
    protected int goods_id;
    protected int sku_id;
    protected String sku_no;
    protected int goods_type;
    protected long num;
    protected float price;
    protected String sku_property;
    protected int goods_from;

    public String toString() {
        return "merchant_id: " + merchant_id +
                "\ngoods_title: " + goods_title +
                "\ngoods_id: " + goods_id +
                "\nsku_id: " + sku_id +
                "\nsku_no: " + sku_no +
                "\ngoods_type: " + goods_type +
                "\nprice: " + price +
                "\nsku_property: " + sku_property +
                "\ngoods_from: " + goods_from;
    }

    public int getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(int merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getGoods_title() {
        return goods_title;
    }

    public void setGoods_title(String goods_title) {
        this.goods_title = goods_title;
    }

    public int getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public int getSku_id() {
        return sku_id;
    }

    public void setSku_id(int sku_id) {
        this.sku_id = sku_id;
    }

    public String getSku_no() {
        return sku_no;
    }

    public void setSku_no(String sku_no) {
        this.sku_no = sku_no;
    }

    public int getGoods_type() {
        return goods_type;
    }

    public void setGoods_type(int goods_type) {
        this.goods_type = goods_type;
    }

    public long getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getPrice() {
        return String.format(MobileConstant.DPRODUCT.FORMAT_PRICE, price);
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getSku_property() {
        return sku_property;
    }

    public void setSku_property(String sku_property) {
        this.sku_property = sku_property;
    }

    public int getGoods_from() {
        return goods_from;
    }

    public void setGoods_from(int goods_from) {
        this.goods_from = goods_from;
    }
}
