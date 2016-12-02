package com.letv.leui.common.recommend.widget.adapter.dto;

/**
 * Created by dupengtao on 15-1-6.
 */
public class RecommendAllDTO {

    private String errno;
    private String errmsg;
    private RecommendAllDataDTO data;

    public String getErrno() {
        return errno;
    }

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public RecommendAllDataDTO getData() {
        return data;
    }

    public void setData(RecommendAllDataDTO data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
