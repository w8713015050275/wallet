package com.letv.walletbiz.base.util;


import android.text.TextUtils;

import com.letv.tracker.agnes.Agnes;
import com.letv.tracker.agnes.App;
import com.letv.tracker.agnes.Event;
import com.letv.tracker.agnes.Widget;
import com.letv.tracker.enums.EventType;
import com.letv.tracker.enums.Key;
import com.letv.tracker.enums.LeUIApp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class Action {
    private static final String TAG = "userAction";
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


    private static final String PROP_DISTANCE = "distance";
    private static final String PROP_CINEMA = "cinema";
    private static final String POSITIONID = "PositionId";

    private static final String EVENTTYPE_SUBSCRIBE = "subscribe";
    private static final String EVENTTYPE_PURCHASE = "purchase";
    private static final String EVENTTYPE_PAY = "pay";
    private static final String EVENTTYPE_APPLY = "apply";

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

    // expose tab
    public static void uploadExposeTab(final String tag) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadExposeTabImpl(tag);
            }
        });
    }

    private static void uploadExposeTabImpl(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        App app = getApp();
        Widget widget = app.createWidget(tag);
        Event event = widget.createEvent(EventType.Expose);
        Agnes.getInstance().report(event);
    }

    // expose tab
    public static void uploadExposeTab(final String tag, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadExposeTabImpl(tag, content);
            }
        });
    }

    private static void uploadExposeTabImpl(String tag, String content) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        App app = getApp();
        Widget widget = app.createWidget(tag);
        Event event = widget.createEvent(EventType.Expose);
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }

    public static void uploadCouponListExpose(final String from) {
        mUploadExecutor.submit(new Runnable() {

            @Override
            public void run() {
                uploadFromExposeIml(COUPON_EXPOSE, from);
            }
        });
    }

    public static void uploadMovieDetailExpose(final String content, final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadFromExposeIml(MOVIE_DETAIL, content, from);
            }
        });
    }

    public static void uploadCinemaDetailExpose(final String content, final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadFromExposeIml(MOVIE_DETAIL_CINEMA, content, from);
            }
        });
    }

    public static void uploadFlowExpose(final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadFromExposeIml(WALLET_SERVICE, WALLET_SERVICE_CONTENT_FLOW, from);
            }
        });
    }

    public static void uploadFeeExpose(final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadFromExposeIml(WALLET_SERVICE, WALLET_SERVICE_CONTENT_FEE, from);
            }
        });
    }

    public static void uploadMovieExpose(final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadFromExposeIml(WALLET_SERVICE, WALLET_SERVICE_CONTENT_MOVIE, from);
            }
        });
    }

    private static void uploadFromExposeIml(String widget, String from) {
        if (TextUtils.isEmpty(widget) || TextUtils.isEmpty(from)) {
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Expose);
        event.addProp(Key.From, from);
        Agnes.getInstance().report(event);
    }

    private static void uploadFromExposeIml(String widget, String content, String from) {
        if (TextUtils.isEmpty(widget) || (TextUtils.isEmpty(content) && TextUtils.isEmpty(from))) {
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Expose);
        event.addProp(Key.Content, content);
        event.addProp(Key.From, from);
        Agnes.getInstance().report(event);
    }

    public static void uploadPushExpose(final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadPushExposeIml(content);
            }
        });
    }

    private static void uploadPushExposeIml(String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(WALLET_PUSH);
        Event event = wdt.createEvent(EventType.Expose);
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }

    public static void uploadPushExpose(final String content, final String to) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadPushExposeIml(content, to);
            }
        });
    }

    private static void uploadPushExposeIml(String content, String to) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        App app = getApp();
        Widget wdt = app.createWidget(WALLET_PUSH);
        Event event = wdt.createEvent(EventType.Expose);
        event.addProp(Key.Content, content);
        event.addProp(Key.To, to);
        Agnes.getInstance().report(event);
    }

    public static void uploadClick(final String widget) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadClickImpl(widget);
            }
        });
    }

    private static void uploadClickImpl(String widget) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Click);
        Agnes.getInstance().report(event);
    }

    public static void uploadClick(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadClickImpl(widget, content);
            }
        });
    }

    private static void uploadClickImpl(String widget, String content) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Click);
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }

    public static void uploadClick(final String widget, final String content, final String distance) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadClickImpl(widget, content, distance);
            }
        });
    }

    private static void uploadClickImpl(String widget, String content, String distance) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Click);
        event.addProp(Key.Content, content);
        if (Key.isExsited(PROP_DISTANCE)) {
            event.addProp(Key.valueOf(PROP_DISTANCE), distance);
        } else {
            event.addProp(PROP_DISTANCE, distance);
        }
        Agnes.getInstance().report(event);
    }

    public static void uploadMoviePhotoClick(final String widget, final String content, final String url) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadMoviePhotoClickImpl(widget, content, url);
            }
        });
    }

    private static void uploadMoviePhotoClickImpl(String widget, String content, String url) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Click);
        event.addProp(Key.Content, content);
        event.addProp(Key.Url, url);
        Agnes.getInstance().report(event);
    }

    public static void uploadSet(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadSetImpl(widget, content);
            }
        });
    }

    private static void uploadSetImpl(String widget, String content) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Set);
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }

    public static void uploadSubscribeClick(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadSubscribeClickImpl(widget, content);
            }
        });
    }

    private static void uploadSubscribeClickImpl(String widget, String content) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event;
        if (EventType.isExsited(EVENTTYPE_SUBSCRIBE)) {
            event = wdt.createEvent(EventType.valueOf(EVENTTYPE_SUBSCRIBE));
        } else {
            event = wdt.createEvent(EVENTTYPE_SUBSCRIBE);
        }
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }


    // buy
    public static void uploadPay(final String function, final String content, final String cinema) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadPayImpl(function, content, cinema);
            }
        });
    }

    private static void uploadPayImpl(String function, String content, String cinema) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event;
        if (EventType.isExsited(EVENTTYPE_PAY)) {
            event = widget.createEvent(EventType.valueOf(EVENTTYPE_PAY));
        } else {
            event = widget.createEvent(EVENTTYPE_PAY);
        }
        event.addProp(Key.Content, content);
        if (Key.isExsited(PROP_CINEMA)) {
            event.addProp(Key.valueOf(PROP_CINEMA), cinema);
        } else {
            event.addProp(PROP_CINEMA, cinema);
        }
        Agnes.getInstance().report(event);
    }

    // buy
    public static void uploadBuy(final String function, final String content, final String cinema) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadBuyImpl(function, content, cinema);
            }
        });
    }

    private static void uploadBuyImpl(String function, String content, String cinema) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event;
        if (EventType.isExsited(EVENTTYPE_PURCHASE)) {
            event = widget.createEvent(EventType.valueOf(EVENTTYPE_PURCHASE));
        } else {
            event = widget.createEvent(EVENTTYPE_PURCHASE);
        }
        event.addProp(Key.Content, content);
        if (Key.isExsited(PROP_CINEMA)) {
            event.addProp(Key.valueOf(PROP_CINEMA), cinema);
        } else {
            event.addProp(PROP_CINEMA, cinema);
        }
        Agnes.getInstance().report(event);
    }

    // buy
    public static void uploadBuy(final String function, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadBuyImpl(function, content);
            }
        });
    }

    private static void uploadBuyImpl(String function, String content) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event;
        if (EventType.isExsited(EVENTTYPE_PURCHASE)) {
            event = widget.createEvent(EventType.valueOf(EVENTTYPE_PURCHASE));
        } else {
            event = widget.createEvent(EVENTTYPE_PURCHASE);
        }
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }

    public static void uploadCallout(final String widget) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCalloutImpl(widget);
            }
        });
    }

    private static void uploadCalloutImpl(String widget) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.CallOut);
        Agnes.getInstance().report(event);
    }

    public static void uploadClickBanner(final String widget, final String content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadClickBannerImpl(widget, content);
            }
        });
    }

    private static void uploadClickBannerImpl(String widget, String content) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Click);
        event.addProp(Key.Content, content);
        Agnes.getInstance().report(event);
    }

    // click list item
    public static void uploadClickListItem(final String widget, final int id, final int position) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadClickListItemImpl(widget, id, position);
            }
        });
    }

    private static void uploadClickListItemImpl(final String widget, final int id, final int position) {
        App app = getApp();
        Widget wdt = app.createWidget(widget);
        Event event = wdt.createEvent(EventType.Click);
        event.addProp(Key.Content, id + "");
        // now no exsited
        if (Key.isExsited(POSITIONID))
            event.addProp(Key.valueOf(POSITIONID), position + "");
        else
            event.addProp(POSITIONID, position + "");
        Agnes.getInstance().report(event);
    }

    // collect
    public static void uploadCollect(final String function, final int id) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadCollectImpl(function, id);
            }
        });
    }

    private static void uploadCollectImpl(final String function, final int id) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event = widget.createEvent(EventType.Book);
        event.addProp(Key.Content, id + "");
        Agnes.getInstance().report(event);
    }

    // share
    public static void uploadShare(final String function, final int id, final String to) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadShareImpl(function, id, to);
            }
        });
    }

    private static void uploadShareImpl(final String function, final int id, final String to) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event = widget.createEvent(EventType.Share);
        event.addProp(Key.Content, id + "");
        event.addProp(Key.To, to);
        Agnes.getInstance().report(event);
    }

    // download
    public static void uploadDownload(final String function, final int id) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadDownloadImpl(function, id);
            }
        });
    }

    private static void uploadDownloadImpl(final String function, final int id) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event = widget.createEvent(EventType.Download);
        event.addProp(Key.Content, id + "");
        Agnes.getInstance().report(event);
    }

    // apply
    public static void uploadApply(final String function, final int id) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadApplyImpl(function, id);
            }
        });
    }

    private static void uploadApplyImpl(final String function, final int id) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event;
        if (EventType.isExsited(EVENTTYPE_APPLY)) {
            event = widget.createEvent(EventType.valueOf(EVENTTYPE_APPLY));
        } else {
            event = widget.createEvent(EVENTTYPE_APPLY);
        }
        event.addProp(Key.Content, id + "");
        Agnes.getInstance().report(event);
    }

    // subscrible
    public static void uploadSubscrible(final String function, final int id, final int designerId) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                uploadSubscribleImpl(function, id, designerId);
            }
        });
    }

    private static void uploadSubscribleImpl(final String function, final int id, final int designerId) {
        App app = getApp();
        Widget widget = app.createWidget(function);
        Event event = widget.createEvent(EventType.Subscrible);
        event.addProp(Key.Content, id + "");
        event.addProp(Key.Class, designerId + "");
        Agnes.getInstance().report(event);
    }

}
