package com.letv.walletbiz.base.util;


import android.text.TextUtils;

import com.letv.tracker.agnes.Agnes;
import com.letv.tracker.agnes.App;
import com.letv.tracker.agnes.Event;
import com.letv.tracker.agnes.Widget;
import com.letv.tracker.enums.EventType;
import com.letv.tracker.enums.Key;
import com.letv.tracker.enums.LeUIApp;
import com.letv.wallet.common.util.LogHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class Action {
    private static final String TAG = "UserAction";
    private static final String WALLET = "Wallet";

    public static final String WALLET_HOME_TOTALORDER = "1.1";
    public static final String WALLET_HOME_COUPON = "1.2";
    public static final String COUPON_EXPOSE = "1.3";

    public static final String WALLET_HOME_BANNER = "2.1";
    public static final String WALLET_HOME_LIST = "s.";

    public static final String MOVIE_CITY_CLICK = "3.1.1";
    public static final String MOVIE_SEARCH_CLICK = "3.1.2";
    public static final String MOVIE_DETAIL = "3.1.3";
    public static final String MOVIE_DIRECTOR_STAR_CLICK = "3.1.3.1";
    public static final String MOVIE_PREVUE_CLICK = "3.1.3.2";
    public static final String MOVIE_ATTENTION_CLICK = "3.1.3.3";

    public static final String MOVIE_MOVIE_STILLS_CLICK = "3.1.3.4";
    public static final String MOVIE_PAY_CLICK = "3.1.3.5";
    public static final String MOVIE_RECOMMENDED_PAGE_CLICK = "3.1.3.6";

    public static final String MOVIE_CINEMA_CLICK = "3.1.4";
    public static final String MOVIE_DETAIL_CINEMA = "3.1.4.1";
    public static final String MOVIE_CINEMA_ADDRESS_CLICK = "3.1.4.2";
    public static final String MOVIE_CALLOUT_CLICK = "3.1.4.3";
    public static final String MOVIE_SELECT_SEAT_PAY_CLICK = "3.1.4.4";
    public static final String MOVIE_PAY_SUCCESS = "3.1.4.5";

    public static final String MOVIE_ORDER_CLICK = "3.1.5";

    public static final String WALLET_PUSH = "4";
    public static final String WALLET_SERVICE = "5";

    public static final String MOBILE_FLOW_PAY_CLICK = "5.1.1";
    public static final String MOBILE_FLEE_PAY_CLICK = "5.1.2";
    //type(1／2／3) from(各APP包名/icon) 1=联系人 2=非联系人 3=本机
    public static final String MOBILE_EXPOSURE = "5.2.1";
    public static final String MOBILE_FLEE_CONFIRM_PAY_CLICK = "5.3.1";
    public static final String MOBILE_FLOW_CONFIRM_PAY_CLICK = "5.3.2";

    //Update
    public static final String UPGRADE_POPUP_SHOW = "7";
    public static final String UPGRADE_POPUP_CLICK = "7.1";
    public static final String UPGRADE_SUCCESS = "7.1.1";
    public static final String UPGRADE_AUTOMATIC_INSTALLATION = "7.2";


    public static final String WALLET_MAIN_EXPOSE = "1.4";
    public static final String WALLET_MAIN_CARD_COUPONS_CARD_EXPOSE = "1.5";

    public static final String TAB_RECOMMEND_PAGE_EXPOSE = "8";
    public static final String PUSH_COUPONS_COUNT_ACCEPT = "8.2";
    public static final String TAB_SET_PAGE_EXPOSE = "8.3";

    //Member
    public static final String MEMBER_FIRST_TAB_EXPOSE = "9";
    public static final String MEMBER_SECOND_TAB_EXPOSE = "9.1";
    public static final String MEMBER_PRODUCT_ORDER_EXPOSE = "9.2.1"; //购买
    public static final String MEMBER_PAY_NOW_PURCHASE = "9.2.2"; //立刻支付
    public static final String MEMBER_BANNER_CLICK = "9.3";

    private static final String PROP_DISTANCE = "distance";
    private static final String PROP_CINEMA = "cinema";
    private static final String POSITIONID = "PositionId";

    //    private static final String EVENTTYPE_SUBSCRIBE = "subscribe";
    private static final String EVENTTYPE_PURCHASE = "purchase";
    private static final String EVENTTYPE_PAY = "pay";

    public static final String EVENT_PROP_TO_HOME = "home";

    public static final String EVENT_PROP_FROM_ICON = "icon";
    public static final String EVENT_PROP_FROM_PUSH = "push";
    public static final String EVENT_PROP_FROM_APP = "app";

    public static final String WALLET_SERVICE_CONTENT_FLOW = "flow";
    public static final String WALLET_SERVICE_CONTENT_FEE = "fee";
    public static final String WALLET_SERVICE_CONTENT_MOVIE = "movie";

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

    private static final ExecutorService mUploadExecutor = Executors.newFixedThreadPool(1, new ActionThreadFactory());

    private static App getApp() {
        App app = null;
        if (LeUIApp.isExsited(WALLET)) {
            app = Agnes.getInstance().getApp(LeUIApp.valueOf(WALLET));
        } else {
            app = Agnes.getInstance().getApp(WALLET);
        }
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
    // ------------ App ---------------

    public static void uploadSet(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.toString(), content);
                uploadCustomImpl(EventType.Set, widget, props);
            }
        });
    }

    public static void uploadPopup(final String widget) {
        uploadPopup(widget, null);
    }

    public static void uploadPopup(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (!TextUtils.isEmpty(content)) {
                    props.put(Key.Content.toString(), content);
                }
                uploadCustomImpl(EventType.Popup, widget, props);
            }
        });
    }


    // ------------ buy -----------

    public static void uploadPay(final String function, final String content, final String cinema) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.toString(), content);
                if (!TextUtils.isEmpty(cinema)) {
                    props.put(PROP_CINEMA, cinema);
                }
                uploadCustomImpl(EVENTTYPE_PAY, function, props);
            }
        });
    }

    public static void uploadBuy(String function, String content) {
        uploadBuy(function, content, null);
    }

    public static void uploadBuy(final String function, final String content, final String cinema) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (!TextUtils.isEmpty(content)) {
                    props.put(Key.Content.toString(), content);
                }
                if (!TextUtils.isEmpty(cinema)) {
                    props.put(PROP_CINEMA, cinema);
                }
                uploadCustomImpl(EVENTTYPE_PURCHASE, function, props);
            }
        });
    }

    // ------------ buy -----------


    public static void uploadCallout(final String widget) {
        uploadCustom(EventType.CallOut, widget);
    }


    // ----------- subscrible  ------------
    public static void uploadSubscribeClick(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.toString(), content);
                uploadCustomImpl(EventType.Subscrible, widget, props);
            }
        });
    }

    public static void uploadSubscrible(final String widget, final int id, final int designerId) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.toString(), id);
                props.put(Key.Class.toString(), designerId);
                uploadCustomImpl(EventType.Subscrible, widget, props);
            }
        });
    }
    // ----------- subscrible  ------------


    //---------- Click -------------
    public static void uploadMoviePhotoClick(String widget, String content, String url) {
        uploadClick(widget, content, null, url, null);
    }

    public static void uploadClick(String widget) {
        uploadClick(widget, null, null, null, null);
    }

    public static void uploadClick(String widget, String content) {
        uploadClick(widget, content, null, null, null);
    }

    public static void uploadClick(String widget, String content, String distance) {
        uploadClick(widget, content, distance, null, null);
    }

    public static void uploadClick(final String widget, final String content, final String distance, final String url, final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (!TextUtils.isEmpty(content)) {
                    props.put(Key.Content.toString(), content);
                }
                if (!TextUtils.isEmpty(distance)) {
                    props.put(PROP_DISTANCE, distance);
                }
                if (!TextUtils.isEmpty(url)) {
                    props.put(Key.Url.toString(), url);
                }
                if (!TextUtils.isEmpty(from)) {
                    props.put(Key.From.toString(), from);
                }
                uploadCustomImpl(EventType.Click, widget, props);
            }
        });
    }
    //---------- Click -------------


    //---------- Expose ---------------

    public static void uploadCouponListExpose(final String from) {
        uploadExpose(COUPON_EXPOSE, null, from, null);
    }

    public static void uploadMovieDetailExpose(final String content, final String from) {
        uploadExpose(MOVIE_DETAIL, content, from, null);
    }

    public static void uploadCinemaDetailExpose(final String content, final String from) {
        uploadExpose(MOVIE_DETAIL_CINEMA, content, from, null);
    }

    public static void uploadFlowExpose(String from) {
        uploadWalletServiceExpose(WALLET_SERVICE_CONTENT_FLOW, from);
    }

    public static void uploadFeeExpose(String from) {
        uploadWalletServiceExpose(WALLET_SERVICE_CONTENT_FEE, from);
    }

    public static void uploadMovieExpose(String from) {
        uploadWalletServiceExpose(WALLET_SERVICE_CONTENT_MOVIE, from);
    }

    public static void uploadWalletServiceExpose(String content, String from) {
        uploadExpose(WALLET_SERVICE, content, from, null);
    }

    public static void uploadPushExpose(String content) {
        uploadExpose(WALLET_PUSH, content, null, null);
    }

    public static void uploadPushExpose(String content, String to) {
        uploadExpose(WALLET_PUSH, content, null, to);
    }

    public static void uploadExposeTab(String widget) {
        uploadExpose(widget, null, null, null);
    }

    public static void uploadExposeTab(String widget, String content) {
        uploadExpose(widget, content, null, null);
    }

    public static void uploadExpose(final String widget, final String content, final String from, final String to) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (!TextUtils.isEmpty(content)) {
                    props.put(Key.Content.toString(), content);
                }
                if (!TextUtils.isEmpty(from)) {
                    props.put(Key.From.toString(), from);
                }
                if (!TextUtils.isEmpty(to)) {
                    props.put(Key.To.toString(), to);
                }
                uploadCustomImpl(EventType.Expose, widget, props);
            }
        });
    }
    // ---------- Expose ----------------

    // ---------- Upgrade ----------------
    public static void uploadUpgradeDialogPopup() {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCustomImpl(EventType.Popup, UPGRADE_POPUP_SHOW);
            }
        });
    }

    public static void uploadUpgradeSuccess(final String type, final String packageName) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.PackageName.toString(), packageName);
                props.put(Key.Type.toString(), type);
                uploadCustomImpl(EventType.Upgrade, UPGRADE_SUCCESS, props);
            }
        });
    }

    public static void uploadInstallSuccess(final String packageName) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.PackageName.toString(), packageName);
                uploadCustomImpl(EventType.Install, UPGRADE_AUTOMATIC_INSTALLATION, props);
            }
        });
    }
    // ---------- Upgrade ----------------

    // -------------------------------------------

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

    private static void uploadCustomImpl(String eventType, String widget) {
        uploadCustomImpl(eventType, widget, null);
    }

    private static void uploadCustomImpl(EventType eventType, String widget) {
        uploadCustomImpl(eventType, widget, null);
    }

    private static void uploadCustomImpl(String eventType, String widget, Map<String, Object> prop) {
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
                if (!TextUtils.isEmpty(entry.getKey()) && entry.getValue() != null) {
                    if (Key.isExsited(entry.getKey())) {
                        event.addProp(Key.valueOf(entry.getKey()), entry.getValue().toString());
                    } else {
                        event.addProp(entry.getKey(), entry.getValue().toString());
                    }
                    LogHelper.i("[%S] Agen " + entry.getKey() + " = " + entry.getValue(), TAG);
                }
            }
        }
        Agnes.getInstance().report(event);
    }

    private static void uploadCustomImpl(EventType eventType, String widget, Map<String, Object> prop) {
        if (TextUtils.isEmpty(widget)) {
            LogHelper.i("[%S] Agen widget == null", TAG);
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(eventType);
        if (prop != null) {
            for (Map.Entry<String, Object> entry : prop.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey()) && entry.getValue() != null) {
                    if (Key.isExsited(entry.getKey())) {
                        event.addProp(Key.valueOf(entry.getKey()), entry.getValue().toString());
                    } else {
                        event.addProp(entry.getKey(), entry.getValue().toString());
                    }
                    LogHelper.i("[%S] Agen " + entry.getKey() + " = " + entry.getValue(), TAG);
                }
            }
        }
        Agnes.getInstance().report(event);
    }
}
