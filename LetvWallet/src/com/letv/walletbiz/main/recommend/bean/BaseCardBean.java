package com.letv.walletbiz.main.recommend.bean;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by liuliang on 17-1-20.
 */

public interface BaseCardBean extends LetvBaseBean, Comparable<BaseCardBean> {

    String getType();
}
