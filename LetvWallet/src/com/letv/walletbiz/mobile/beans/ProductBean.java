package com.letv.walletbiz.mobile.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.walletbiz.mobile.MobileConstant;

import java.io.Serializable;

/**
 * Created by linquan on 15-11-17.
 */
public class ProductBean implements LetvBaseBean {

    public String provice;//province;

    public String isp;

    public product[] product_list;
    private boolean iStub = false;

    public boolean isStub() {
        return iStub;
    }

    public void setAsStub() {
        iStub = true;
    }

    public String getNumberDesc() {
        return provice + isp;
    }

    public class product implements Serializable {


        public int product_id;
        public float orig_price;
        public float price;
        public int content;
        public String product_name;
        public String sku_sn;

        public int getProductId() {
            return product_id;
        }

        public String getSkuSN() {
            return sku_sn;
        }

        public String getProductPrice() {
            return String.format(MobileConstant.DPRODUCT.FORMAT_PRICE, price);
        }

        public String getProductName() {
            return product_name;
        }

        public int getContent(){
            return content;
        }
    }

}
