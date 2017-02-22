package com.letv.wallet.common.util;


import android.text.TextUtils;

import com.letv.tracker.agnes.Agnes;
import com.letv.tracker.agnes.App;
import com.letv.tracker.agnes.Event;
import com.letv.tracker.agnes.Widget;
import com.letv.tracker.enums.EventType;
import com.letv.tracker.enums.Key;
import com.letv.tracker.enums.LeUIApp;
import com.letv.tracker.msg.bean.Version;
import com.letv.wallet.common.BaseApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public abstract class BaseAction {
    private static final String TAG = "WalletAction";

    protected static final String APP_NAME = "Wallet";

    public static final String EVENTTYPE_SUBSCRIBE = "subscribe";
    public static final String EVENTTYPE_PURCHASE = "purchase";
    public static final String EVENTTYPE_PAY = "pay";
    public static final String EVENTTYPE_SET = "set";

    public static final String EVENT_PROP_FROM_ICON = "wallet_icon";
    public static final String EVENT_PROP_FROM_PUSH = "wallet_push";
    public static final String EVENT_PROP_FROM_APP = "app";
    public static final String EVENT_PROP_FROM_BANNER = "wallet_banner";
    public static final String EVENT_PROP_FROM_CARD = "wallet_card";

    /** 账户相关 **/
    public static final String EVENT_PROP_FROM_ACCOUNT_CARD_LIST = "card";

    public static final String EVENT_TYPE_VERIFY = "verify";


    static class ActionThreadFactory implements ThreadFactory {
        private static final String NAME = "USER_ACTION";

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, NAME);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    }

    protected static final ExecutorService mUploadExecutor = Executors.newFixedThreadPool(1, new ActionThreadFactory());

    protected static App getApp() {
        App app = null;
        if (LeUIApp.isExsited(APP_NAME)) {
            app = Agnes.getInstance().getApp(LeUIApp.valueOf(APP_NAME));
        } else {
            app = Agnes.getInstance().getApp(APP_NAME);
        }
        Version appVersion = app.getVersion();
        appVersion.setVersion(BaseApplication.getApplication().getAppVersion());
        return app;
    }

    // ------------ App ---------------
    // App create
    public static void uploadStartApp() {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadStartAppImpl();
            }
        });
    }

    private static void uploadStartAppImpl() {
        App app = getApp();
        app.run();
        app.ready();
        Agnes.getInstance().report(app);
    }

    // App stop
    public static void uploadStopApp() {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadStopAppImpl();
            }
        });
    }

    private static void uploadStopAppImpl() {
        App app = getApp();
        app.exit();
        Agnes.getInstance().report(app);
    }

    //----------  Expose  ----------------
    public static void uploadExpose(final String widget, final Object content, final String from, final String to) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (content != null) {
                    props.put(Key.Content.getKeyId(), content);
                }
                props.put(Key.From.getKeyId(), from);
                if (!TextUtils.isEmpty(to)) {
                    props.put(Key.To.getKeyId(), to);
                }
                uploadCustomImpl(EventType.Expose, widget, props);
            }
        });
    }

    // ----------------------

    public static void uploadCustom(final String eventType, final String widget) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCustomImpl(eventType, widget);
            }
        });
    }

    public static void uploadCustom(final EventType eventType, final String widget) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCustomImpl(eventType, widget);
            }
        });
    }

    public static void uploadCustom(final String eventType, final String widget, final Map<String, Object> prop) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCustomImpl(eventType, widget, prop);
            }
        });
    }

    public static void uploadCustom(final EventType eventType, final String widget, final Map<String, Object> prop) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCustomImpl(eventType, widget, prop);
            }
        });
    }

    protected static void uploadCustomImpl(String eventType, String widget) {
        uploadCustomImpl(eventType, widget, null);
    }

    protected static void uploadCustomImpl(EventType eventType, String widget) {
        uploadCustomImpl(eventType, widget, null);
    }

    protected static void uploadCustomImpl(String eventType, String widget, Map<String, Object> prop) {
        if (TextUtils.isEmpty(widget)) {
            LogHelper.i("[%S] Agen widget == null", TAG);
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event;
        if (EventType.isExsited(eventType)) {
            event = wdt.createEvent(EventType.valueOf(eventType));
        } else {
            event = wdt.createEvent(eventType);
        }
        if (prop != null) {
            for (Map.Entry<String, Object> entry : prop.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey())) {
                    if (entry.getKey().equals(Key.From.getKeyId())) {
                        String value = "";
                        if (entry.getValue() != null) {
                            value = entry.getValue().toString();
                        }
                        event.addProp(Key.valueOf(entry.getKey()), value);
                        LogHelper.i("[%S] Agen " + entry.getKey() + " = " + value, TAG);
                    } else {
                        if (entry.getValue() != null) {
                            if (Key.isExsited(entry.getKey())) {
                                event.addProp(Key.valueOf(entry.getKey()), entry.getValue().toString());
                            } else {
                                event.addProp(entry.getKey(), entry.getValue().toString());
                            }
                        }
                        LogHelper.i("[%S] Agen " + entry.getKey() + " = " + entry.getValue(), TAG);
                    }
                }
            }
        }
        Agnes.getInstance().report(event);
    }

    protected static void uploadCustomImpl(EventType eventType, String widget, Map<String, Object> prop) {
        if (TextUtils.isEmpty(widget)) {
            LogHelper.i("[%S] Agen widget == null", TAG);
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(eventType);
        if (prop != null) {
            for (Map.Entry<String, Object> entry : prop.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey())) {
                    if (entry.getKey().equals(Key.From.getKeyId())) {
                        String value = "";
                        if (entry.getValue() != null) {
                            value = entry.getValue().toString();
                        }
                        event.addProp(Key.valueOf(entry.getKey()), value);
                        LogHelper.i("[%S] Agen " + entry.getKey() + " = " + value, TAG);
                    } else {
                        if (Key.isExsited(entry.getKey())) {
                            event.addProp(Key.valueOf(entry.getKey()), entry.getValue().toString());
                        } else {
                            event.addProp(entry.getKey(), entry.getValue().toString());
                        }
                        LogHelper.i("[%S] Agen " + entry.getKey() + " = " + entry.getValue(), TAG);
                    }
                }
            }
        }
        Agnes.getInstance().report(event);
    }
}
