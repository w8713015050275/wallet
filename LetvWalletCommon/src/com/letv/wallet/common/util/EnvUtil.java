package com.letv.wallet.common.util;

/**
 * Created by zhuchuntao on 17-2-22.
 */

public class EnvUtil {

    private static EnvUtil instance;

    private boolean isTest;

    /**
     * 线程安全的单例模式
     *
     * @return EnvUtil
     */
    public static EnvUtil getInstance() {
        if (null == instance) {
            synchronized (EnvUtil.class) {
                if (instance == null) {//二次检查
                    instance = new EnvUtil();
                }
            }
        }
        return instance;
    }

    public boolean isTest() {
        return false;
    }

    public void setTest(boolean isTest) {
        this.isTest = isTest;
    }


}
