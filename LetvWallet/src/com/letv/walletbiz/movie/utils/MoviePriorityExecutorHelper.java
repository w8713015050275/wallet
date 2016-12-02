package com.letv.walletbiz.movie.utils;

import org.xutils.common.task.PriorityExecutor;

/**
 * Created by liuliang on 16-3-7.
 */
public class MoviePriorityExecutorHelper {

    private static PriorityExecutor mExecutor = new PriorityExecutor();

    public static PriorityExecutor getPriorityExecutor() {
        return mExecutor;
    }
}
