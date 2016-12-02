package com.letv.walletbiz.main.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AppUtils;
import com.letv.wallet.common.util.IOUtils;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.main.MainPanelHelper;
import com.letv.walletbiz.main.bean.WalletServiceListBean;
import com.letv.walletbiz.main.provider.WalletContract.ServiceTable;

/**
 * Created by liuliang on 16-4-18.
 */
public class WalletServiceByActionProvider extends ContentProvider {

    private static final int SERVICE_BY_ACTION = 1;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //用于第三方应用查询钱包中是否包含某个服务
        sUriMatcher.addURI("com.letv.wallet.main.service", "servicebyAction/*", SERVICE_BY_ACTION);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SERVICE_BY_ACTION:
                return queryServiceByAction(uri);
        }
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("not support");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("not support");
    }

    /**
     *
     * @param uri
     * @return  errno:1 成功; 2 无网络; 3 网络错误
     *          result:1有相应服务;0无相应服务
     */
    private Cursor queryServiceByAction(Uri uri) {
        String action = uri.getPathSegments().get(1);
        int errno = 1;
        boolean result = false;
        if (!NetworkHelper.isNetworkAvailable()) {
            errno = 2;
        } else {
            BaseResponse<WalletServiceListBean> response =  MainPanelHelper.getServiceListFromNetwork();
            if (response != null && response.errno == 10000) {
                WalletServiceListBean listBean = response.data;
                if (listBean != null && listBean.list != null) {
                    result = findAction(listBean.list, action);
                    final long identity = Binder.clearCallingIdentity();
                    if (needSaveToDb(getContext(), listBean.version)) {
                        MainPanelHelper.syncServiceListToDb(getContext(), listBean);
                    }
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
        String[] columnNames = new String[]{"errno", "result"};
        MatrixCursor cursor = new MatrixCursor(columnNames, 1);
        cursor.addRow(new Object[]{errno, result ? 1 : 0});
        //增加第二列返回缓存中的数据
        if (errno != 1) {
            final long identity = Binder.clearCallingIdentity();
            String[] projection = new String[]{ServiceTable.SERVICE_ID, ServiceTable.SERVICE_NAME};
            String selection = ServiceTable.JUMP_PARAM + "=? OR " + ServiceTable.JUMP_LINK + "=?";
            Cursor queryCursor = null;
            try {
                queryCursor = getContext().getContentResolver().query(ServiceTable.CONTENT_URI, projection, selection, new String[]{action, action}, null);
                boolean dbHasAction = false;
                if (queryCursor != null) {
                    if (queryCursor.getCount() > 0) {
                        dbHasAction = true;
                    }
                }
                cursor.addRow(new Object[]{1, dbHasAction ? 1 : 0});
            } finally {
                if (queryCursor != null) {
                    IOUtils.closeQuietly(queryCursor);
                }
            }
            Binder.restoreCallingIdentity(identity);
        }
        return cursor;
    }

    private boolean findAction(WalletServiceListBean.WalletServiceBean[] serviceBeans, String action) {
        if (serviceBeans == null || TextUtils.isEmpty(action)) {
            return false;
        }
        for (WalletServiceListBean.WalletServiceBean bean : serviceBeans) {
            if (bean != null
                    && bean.state == WalletServiceListBean.WalletServiceBean.STATE_ONLING
                    && bean.jump_param != null) {
                String[] array = AppUtils.getActionAndData(bean.jump_param);
                if (array[0] != null && (action.equals(array[0]))) {
                    return true;
                }
                if (action.equals(bean.jump_link)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean needSaveToDb(Context context, long version) {
        if (context == null) {
            return false;
        }
        Cursor cursor = context.getContentResolver().query(
                ServiceTable.CONTENT_URI,
                null,
                ServiceTable.UPDATE_TIME + "=?",
                new String[]{String.valueOf(version)}, null);
        if (cursor != null && cursor.getCount() > 0) {
            return false;
        }
        return true;
    }
}
