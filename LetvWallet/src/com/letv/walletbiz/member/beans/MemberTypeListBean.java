package com.letv.walletbiz.member.beans;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

import com.letv.wallet.common.http.beans.LetvBaseBean;

public class MemberTypeListBean implements LetvBaseBean {

    public MemberTypeBean[] list;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberTypeListBean)) {
            return false;
        }
        final MemberTypeListBean u = (MemberTypeListBean)o;

        if (list.length != u.list.length) {
            return false;
        }

        for (int i = 0; i < list.length; i++) {
            MemberTypeBean thisBean = list[i];
            boolean noChange = false;
            for (int j = 0; j < u.list.length; j++) {
                MemberTypeBean uBean = u.list[j];
                if(thisBean.name != null && thisBean.name.equals(uBean.name) && thisBean.update_time == uBean.update_time) {
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

    public static class MemberTypeBean implements LetvBaseBean, Comparable<MemberTypeBean> {

        public String id;
        public String name;
        public String type;
        public String goods_id;
        public String state;
        public String protocol_link;
        public String img_url;
        public String description;
        public String rank;
        public String update_time;
        public String add_time;
        public String operator;
        public String goods_json;
        public GoodItem[] goods;

        @Override
        public int compareTo(MemberTypeBean another) {
            return Integer.valueOf(rank) < Integer.valueOf(another.rank) ? -1 : 1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{id:"+id+",");
            sb.append("name:"+name+",");
            sb.append("type:"+type+",");
            sb.append("protocol_link:"+protocol_link+",");
            sb.append("img_url:"+img_url+",");
            sb.append("description:"+description+",");
            sb.append("rank:"+rank+",");
            sb.append("update_time:"+update_time+"}");
            return sb.toString();
        }
    }

    public static class GoodItem implements LetvBaseBean {

        public String id;
        public String sku_no;
        public String spu_id;
        public String name;
        public String duration;
        public String price;
        public String month_price;
        public String tag;
        public String description;
        public String protocol_url;
    }
}
