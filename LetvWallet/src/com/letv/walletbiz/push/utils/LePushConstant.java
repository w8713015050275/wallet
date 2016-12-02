package com.letv.walletbiz.push.utils;

/**
 * Created by liuliang on 16-5-23.
 */
public class LePushConstant {

    public static class APP {
        public static final String APPID = "id_b3e4a6095bb548c68770f84ee76c2456";
        public static final String APPKEY = "ak_qJRqgqBeOJOZLoMFghoE";
    }

    // push服务注册成功后，发送此广播(在第一次开机后，可能app先起来, push服务还没有注册成功)
    // push服务启动成功会发送服务启动广播
    public static final String PUSH_STARTED_BROADCAST = "com.stv.stvpush.ACTION_SERVICE_STARTED";
    // push服务连接服务器成功会发送服务启动广播
    public static final String PUSH_CONNECTED_BROADCAST = "com.stv.stvpush.ACTION_CONNECTED_PUSH";

    // 重试次数
    // 有wifi时，最大重试次数
    public static int MAX_RETRY_TIMES = 10;

    // 30秒短重试一次
    public static final long PUSH_SHORT_INTERVAL = 30 * 1000;

    public static final int PHASE_EMPTY_CHECK = -1;

    public static final int PHASE_PUSH_REGISTER = 20;

    public static final String PUSH_APP_STATE_NONE = "NONE";  // 无注册
    public static final String PUSH_APP_STATE_NORMAL = "NORMAL"; //状态正常
    public static final String PUSH_APP_STATE_PAUSE = "PAUSE"; //暂停状态


    /**
     * push管理类中handler
     */
    // 注册push
    public static final int PUSH_REGISTER = 1;
    public static final int PUSH_SERVER_CONNECTED = 2;
    // 注册push的retry 消息
    public static final int PUSH_SCHEDULER_MSG = 10010;
    public static final int PUSH_APP_STATE_MSG = 10011;
}
