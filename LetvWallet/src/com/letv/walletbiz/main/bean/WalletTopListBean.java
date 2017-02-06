package com.letv.walletbiz.main.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 16-4-11.
 */
public class WalletTopListBean implements LetvBaseBean {

    public String doc_key;
    public String doc_title;
    public WalletTopBean[] list;

    //数据版本 最新更新时间
    public long version;

    public static class WalletTopBean implements LetvBaseBean, Comparable<WalletTopBean> {


        //服务名称，作为唯一标示
        public String name;

        //按钮提示名称
        public String title;

        //icon图标
        public String icon;

        //排序值，值越小，越靠前
        public int rank;

        //更新版本
        public long version;

        @Override
        public int compareTo(WalletTopBean another) {
            return rank < another.rank ? -1 : 1;
        }

        @Override
        public String toString() {
            return "name=" + name + ";title=" + title + ";icon=" + icon + ";rank=" + rank;
        }
    }
}
