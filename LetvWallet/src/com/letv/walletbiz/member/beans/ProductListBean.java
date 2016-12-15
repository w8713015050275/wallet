package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;

public class ProductListBean implements LetvBaseBean {

    public ProductBean[] list;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductListBean)) {
            return false;
        }
        final ProductListBean u = (ProductListBean)o;

        if (list.length != u.list.length) {
            return false;
        }

        for (int i = 0; i < list.length; i++) {
            ProductBean thisBean = list[i];
            boolean noChange = false;
            for (int j = 0; j < u.list.length; j++) {
                ProductBean uBean = u.list[j];
                if(thisBean.id == uBean.id && thisBean.sku_no.equals(uBean.sku_no)) {
                    noChange = true;
                    break;
                }
            }
            if(!noChange) {
                return false;
            }
        }
        return true;
    }

    public static class ProductBean implements LetvBaseBean {

        public String memberType;
        public int id;
        public String sku_no;
        public String name;
        public String price;
        public String kind;
        public String month_price;
        public String tag;
        public String description;
        public String duration;
        public String spu_name;
        public String protocol_url;

        @Override
        public String toString() {
            return "ProductBean{" +
                    "memberType='" + memberType + '\'' +
                    ", id=" + id +
                    ", sku_no='" + sku_no + '\'' +
                    ", name='" + name + '\'' +
                    ", price='" + price + '\'' +
                    ", kind='" + kind + '\'' +
                    ", month_price='" + month_price + '\'' +
                    ", tag='" + tag + '\'' +
                    ", description='" + description + '\'' +
                    ", duration='" + duration + '\'' +
                    ", spu_name='" + spu_name + '\'' +
                    ", protocol_url='" + protocol_url + '\'' +
                    '}';
        }
    }
}
