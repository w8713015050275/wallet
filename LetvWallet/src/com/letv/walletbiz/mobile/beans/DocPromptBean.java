package com.letv.walletbiz.mobile.beans;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by changjiajie on 16-12-16.
 */

public class DocPromptBean implements LetvBaseBean {

    protected String doc_key;
    protected String doc_title;
    protected String doc_content;

    public String getDoc_content() {
        return doc_content;
    }

    public String getDoc_key() {
        return doc_key;
    }

    public String getDoc_title() {
        return doc_title;
    }

    @Override
    public String toString() {
        return "doc_key: " + doc_key +
                "\ndoc_title: " + doc_title +
                "\ndoc_content: " + doc_content;
    }
}
