package com.letv.walletbiz.base.util;


import android.text.TextUtils;

import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
import com.letv.wallet.common.util.BaseAction;

import java.util.HashMap;
import java.util.Map;


public class Action extends BaseAction {

    //Widget
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

    //Mobile
    public static final String MOBILE_FLOW_PAY_CLICK = "5.1.1";
    public static final String MOBILE_FEE_PAY_CLICK = "5.1.2";
    public static final String MOBILE_FLOW_PRODUCT_CLICK = "5.2.1";
    public static final String MOBILE_FEE_PRODUCT_CLICK = "5.2.2";

    //Update
    public static final String UPGRADE_POPUP_SHOW = "7";
    public static final String UPGRADE_POPUP_CLICK = "7.1";
    public static final String UPGRADE_SUCCESS = "7.1.1";
    public static final String UPGRADE_AUTOMATIC_INSTALLATION = "7.2";

    //Wallet
    public static final String WALLET_MAIN_EXPOSE = "1.4";
    //首页卡券的曝光
    public static final String WALLET_HOME_COUPON_EXPOSE = "1.5";

    //Me
    public static final String ME_PAGE_EXPOSE = "8";

    //Member
    public static final String MEMBER_FIRST_TAB_EXPOSE = "9";
    public static final String MEMBER_SECOND_TAB_EXPOSE = "9.1";
    public static final String MEMBER_PRODUCT_ORDER_EXPOSE = "9.2.1"; //购买
    public static final String MEMBER_PAY_NOW_PURCHASE = "9.2.2"; //立刻支付
    public static final String MEMBER_FIRST_BANNER_CLICK = "9.3.1";
    public static final String MEMBER_SECOND_BANNER_CLICK = "9.3.2";

    //Quick entry
    public static final String QUICK_ENTRY_LELEHUA_CLICK = "11";
    public static final String QUICK_ENTRY_BANKCARD_CLICK = "11.3";
    //乐乐花未激活转激活
    public static final String QUICK_ENTRY_LELEHUA_ACTIVE_CLICK = "11.1";

    //Recommend
    public static final String RECOMMEND_PAGE_EXPOSE = "12.1.1";
    public static final String RECOMMEND_CARDS_EXPOSE = "12.1";
    public static final String RECOMMEND_CARDS_CLICK = "12.2";
    public static final String RECOMMEND_CARDS_BUTTON_CLICK = "12.2.1";

    //EventType
    public static final String EVENTTYPE_VERIFY = "verify";

    public static final String POSITIONID = "PositionId";
    public static final String PROP_DISTANCE = "distance";
    public static final String PROP_CINEMA = "cinema";

    public static final String EVENT_PROP_TO_HOME = "home";

    public static final String WALLET_SERVICE_CONTENT_FLOW = "flow";
    public static final String WALLET_SERVICE_CONTENT_FEE = "fee";
    public static final String WALLET_SERVICE_CONTENT_MOVIE = "movie";

    public static final String KEY_BUTTON = "button";

    public static void uploadSet(final String widget, final Object content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.getKeyId(), content);
                uploadCustomImpl(EventType.Set, widget, props);
            }
        });
    }

    // ------------ buy -----------
    public static void uploadPay(final String function, final Object content, final String cinema) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.getKeyId(), content);
                if (!TextUtils.isEmpty(cinema)) {
                    props.put(PROP_CINEMA, cinema);
                }
                uploadCustomImpl(EVENTTYPE_PAY, function, props);
            }
        });
    }

    public static void uploadBuy(String function, Object content) {
        uploadBuy(function, content, null);
    }

    public static void uploadBuy(final String function, final Object content, final String cinema) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (content != null) {
                    props.put(Key.Content.getKeyId(), content);
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
    public static void uploadSubscribeClick(final String widget, final Object content) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.getKeyId(), content);
                uploadCustomImpl(EVENTTYPE_SUBSCRIBE, widget, props);
            }
        });
    }

    public static void uploadSubscrible(final String widget, final int id, final int designerId) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.Content.getKeyId(), id);
                props.put(Key.Class.getKeyId(), designerId);
                uploadCustomImpl(EventType.Subscrible, widget, props);
            }
        });
    }
    // ----------- subscrible  ------------


    //---------- Click -------------
    public static void uploadMoviePhotoClick(String widget, Object content, String url) {
        uploadClick(widget, content, null, url, null);
    }

    public static void uploadClick(String widget) {
        uploadClick(widget, null);
    }

    public static void uploadClick(String widget, Object content) {
        uploadClick(widget, content, null);
    }

    public static void uploadClick(String widget, Object content, String distance) {
        uploadClick(widget, content, distance, null, null);
    }

    public static void uploadClick(final String widget, final Object content, final String distance, final String url, final String from) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                if (content != null) {
                    props.put(Key.Content.getKeyId(), content);
                }
                if (!TextUtils.isEmpty(distance)) {
                    props.put(PROP_DISTANCE, distance);
                }
                if (!TextUtils.isEmpty(url)) {
                    props.put(Key.Url.getKeyId(), url);
                }
                if (!TextUtils.isEmpty(from)) {
                    props.put(Key.From.getKeyId(), from);
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

    public static void uploadMovieDetailExpose(final Object content, final String from) {
        uploadExpose(MOVIE_DETAIL, content, from, null);
    }

    public static void uploadCinemaDetailExpose(final Object content, final String from) {
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
        uploadExpose(WALLET_PUSH, content, null);
    }

    public static void uploadPushExpose(String content, String to) {
        uploadExpose(WALLET_PUSH, content, to);
    }

    public static void uploadExposeTab(String widget) {
        uploadExpose(widget, null, null, null);
    }

    public static void uploadExposeTab(String widget, Object content) {
        uploadExpose(widget, content, null, null);
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
                props.put(Key.PackageName.getKeyId(), packageName);
                props.put(Key.Type.getKeyId(), type);
                uploadCustomImpl(EventType.Upgrade, UPGRADE_SUCCESS, props);
            }
        });
    }

    public static void uploadInstallSuccess(final String packageName) {
        mUploadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> props = new HashMap<>();
                props.put(Key.PackageName.getKeyId(), packageName);
                uploadCustomImpl(EventType.Install, UPGRADE_AUTOMATIC_INSTALLATION, props);
            }
        });
    }
    // ---------- Upgrade ----------------
}
