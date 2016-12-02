package com.letv.walletbiz.mobile.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by changjiajie on 16-1-13.
 */
public class HistoryRecordNumberBean implements LetvBaseBean {

    public List<RecordInfoBean> record_info;

    public List<RecordInfoBean> getRecordInfo() {
        if (record_info == null) {
            record_info = new ArrayList<RecordInfoBean>();
        }
        return record_info;
    }

    public void setRecordInfo(List<RecordInfoBean> record_info) {
        this.record_info = record_info;
    }

    public static class RecordInfoBean implements Serializable {
        public String phoneNum;
        public String name;
        public long time;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNum() {
            return phoneNum;
        }

        public void setPhoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
        }
    }

}
