package com.letv.walletbiz.mobile.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.letv.wallet.common.util.IOUtils;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.provider.MobileContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by changjiajie on 16-1-13.
 */
public class HistoryRecordHelper {

    private static int SHOWCOUNT = 3;
    public static int MAXCOUNT = 30;

    public static HistoryRecordNumberBean getContactFromDBsync(Context context) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[]{
                MobileContact.ContactNumberCacheTable.PHONE_NUMBER,
                MobileContact.ContactNumberCacheTable._TIME,
        };
        Cursor cursor = null;
        List<HistoryRecordNumberBean.RecordInfoBean> record_info = new ArrayList<HistoryRecordNumberBean.RecordInfoBean>();
        try {
            cursor = context.getContentResolver().query(MobileContact.ContactNumberCacheTable.CONTENT_URI, projection, null, null, MobileContact.ContactNumberCacheTable._TIME + " desc  LIMIT " + SHOWCOUNT + " OFFSET 0");
            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }
            cursor.moveToFirst();
            do {
                HistoryRecordNumberBean.RecordInfoBean info = new HistoryRecordNumberBean.RecordInfoBean();
                info.phoneNum = cursor.getString(0);
                info.time = cursor.getLong(1);
                record_info.add(info);
            } while (cursor.moveToNext());
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        HistoryRecordNumberBean numBerListBean = new HistoryRecordNumberBean();
        numBerListBean.setRecordInfo(record_info);
        return numBerListBean;
    }

    public static boolean insertContactToDBsync(Context context, HistoryRecordNumberBean.RecordInfoBean number) {
        if (number == null) {
            return false;
        }
        if (context == null) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(MobileContact.ContactNumberCacheTable.PHONE_NUMBER, number.getPhoneNum());
        values.put(MobileContact.ContactNumberCacheTable._TIME, number.getTime());
        Uri uri = context.getContentResolver().insert(MobileContact.ContactNumberCacheTable.CONTENT_URI, values);
        if (uri == null) return false;
        return true;

    }

    public static int deleteContactToDBsync(Context context) {
        if (context == null) {
            return 0;
        }
        int count = context.getContentResolver().delete(MobileContact.ContactNumberCacheTable.CONTENT_URI, null, null);
        return count;

    }
}
