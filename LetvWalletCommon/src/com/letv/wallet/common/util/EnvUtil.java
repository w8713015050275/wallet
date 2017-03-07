package com.letv.wallet.common.util;

/**
 * Created by zhuchuntao on 17-2-22.
 */

public class EnvUtil {

    private static EnvUtil instance;

    private boolean isWalletTest;
    private boolean isLePayTest;


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

    public boolean isWalletTest() {
        return isWalletTest;
    }

    public boolean isLePayTest() {
        return isLePayTest;
    }

    public void setWalletTest(boolean isWalletTest) {
        this.isWalletTest = isWalletTest;
    }

    public void setLePayTest(boolean isLePayTest) {
        this.isLePayTest = isLePayTest;
    }

}
