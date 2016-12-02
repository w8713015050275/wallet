package com.letv.wallet.common.util;

import org.xutils.common.task.PriorityExecutor;

/**
 * Created by changjiajie on 16-4-13.
 */
public class PriorityExecutorHelper {

    private static PriorityExecutor mExecutor = new PriorityExecutor();

    public static PriorityExecutor getPriorityExecutor() {
        return mExecutor;
    }
}
