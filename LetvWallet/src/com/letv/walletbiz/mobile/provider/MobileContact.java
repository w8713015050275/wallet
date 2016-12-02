package com.letv.walletbiz.mobile.provider;

import android.net.Uri;

/**
 * Created by changjiajie on 16-1-13.
 */
public class MobileContact {

    public static final String AUTHORITY = "com.letv.mobile.record";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class ContactNumberCacheTable {

        private ContactNumberCacheTable() {
        }

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "mobilecache");
        public static final String TABLE_NAME = "mobilecache";
        public static final String _ID = "_id";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String _TIME = "_time";
    }


}
