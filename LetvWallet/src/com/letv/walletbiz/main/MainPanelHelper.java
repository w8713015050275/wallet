package com.letv.walletbiz.main;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.IOUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.main.bean.WalletBannerListBean;
import com.letv.walletbiz.main.bean.WalletServiceListBean;
import com.letv.walletbiz.main.bean.WalletTopListBean;
import com.letv.walletbiz.main.provider.WalletContract;

import org.xutils.xmain;

import java.util.ArrayList;

/**
 * Created by liuliang on 16-4-8.
 */
public class MainPanelHelper {

    public static final String MAIN_SERVICE_LIST = "wallet/api/v1/service/list";
    public static final String MAIN_BANNER_LIST = "wallet/api/v1/banner/list";

    public static final String MAIN_TOP_LIST = "wallet/api/v1/topservice";

    public static final String PARAM_POSITION_ID = "position_id";

    public static final int NO_ERROR = 0;
    public static final int ERROR_NETWORK = 1;
    public static final int ERROR_NO_NETWORK = 2;

    public static final String ROAMING_PACKAGE = "com.letv.roaming";

    public interface Callback<T> {
        void onLoadFromLocalFinished(T result, int errorCode);
        void onLoadFromNetworkFinished(T result, int errorCode, boolean needUpdate);
    }

    public static WalletServiceListBean getServiceListFromDb(Context context) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[] {
                WalletContract.ServiceTable.SERVICE_ID,
                WalletContract.ServiceTable.SERVICE_NAME,
                WalletContract.ServiceTable.ICON,
                WalletContract.ServiceTable.JUMP_TYPE,
                WalletContract.ServiceTable.JUMP_LINK,
                WalletContract.ServiceTable.NEED_TOKEN,
                WalletContract.ServiceTable.JUMP_PARAM,
                WalletContract.ServiceTable.PACKAGE_NAME,
                WalletContract.ServiceTable.STATE,
                WalletContract.ServiceTable.RANK,
                WalletContract.ServiceTable.UPDATE_TIME,
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(WalletContract.ServiceTable.CONTENT_URI, projection, null, null, WalletContract.ServiceTable.RANK + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                WalletServiceListBean listBean = new WalletServiceListBean();
                WalletServiceListBean.WalletServiceBean[] list = new WalletServiceListBean.WalletServiceBean[cursor.getCount()];
                int index = 0;
                long version = -1;
                do {
                    WalletServiceListBean.WalletServiceBean serviceBean = new WalletServiceListBean.WalletServiceBean();
                    serviceBean.service_id = cursor.getInt(0);
                    serviceBean.service_name = cursor.getString(1);
                    serviceBean.icon = cursor.getString(2);
                    serviceBean.jump_type = cursor.getInt(3);
                    serviceBean.jump_link = cursor.getString(4);
                    serviceBean.need_token = cursor.getInt(5);
                    serviceBean.jump_param = cursor.getString(6);
                    serviceBean.package_name = cursor.getString(7);
                    serviceBean.state = cursor.getInt(8);
                    serviceBean.rank = cursor.getInt(9);
                    serviceBean.update_time = cursor.getLong(10);
                    list[index++] = serviceBean;
                    version = Math.max(version, serviceBean.update_time);
                } while (cursor.moveToNext());
                if (list.length > 0) {
                    listBean.list = list;
                    listBean.version = version;
                    return listBean;
                }
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public static BaseResponse<WalletServiceListBean> getServiceListFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(MainPanelHelper.MAIN_SERVICE_LIST);
        BaseResponse<WalletServiceListBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<WalletServiceListBean>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static void syncServiceListToDb(Context context, WalletServiceListBean listBean) {
        if (context == null || listBean == null) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        operationList.add(ContentProviderOperation.newDelete(WalletContract.ServiceTable.CONTENT_URI).build());

        if (listBean.list != null) {
            for (WalletServiceListBean.WalletServiceBean bean : listBean.list) {
                ContentValues values = new ContentValues();
                values.put(WalletContract.ServiceTable.SERVICE_ID, bean.service_id);
                values.put(WalletContract.ServiceTable.SERVICE_NAME, bean.service_name);
                values.put(WalletContract.ServiceTable.ICON, bean.icon);
                values.put(WalletContract.ServiceTable.JUMP_TYPE, bean.jump_type);
                values.put(WalletContract.ServiceTable.JUMP_LINK, bean.jump_link);
                values.put(WalletContract.ServiceTable.NEED_TOKEN, bean.need_token);
                values.put(WalletContract.ServiceTable.JUMP_PARAM, bean.jump_param);
                values.put(WalletContract.ServiceTable.PACKAGE_NAME, bean.package_name);
                values.put(WalletContract.ServiceTable.STATE, bean.state);
                values.put(WalletContract.ServiceTable.RANK, bean.rank);
                values.put(WalletContract.ServiceTable.UPDATE_TIME, bean.update_time);
                operationList.add(ContentProviderOperation
                        .newInsert(WalletContract.ServiceTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }
        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(WalletContract.AUTHORITY, operationList);
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
    }

    public static WalletBannerListBean getBannerListFromDb(Context context, int positonId) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[] {
                WalletContract.BannerTable.BANNER_ID,
                WalletContract.BannerTable.BANNER_NAME,
                WalletContract.BannerTable.POSITION_ID,
                WalletContract.BannerTable.RANK,
                WalletContract.BannerTable.BANNER_TYPE,
                WalletContract.BannerTable.BANNER_POST,
                WalletContract.BannerTable.BANNER_LINK,
                WalletContract.BannerTable.NEED_TOKEN,
                WalletContract.BannerTable.JUMP_PARAM,
                WalletContract.BannerTable.PACKAGE_NAME,
                WalletContract.BannerTable.UPDATE_TIME
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(WalletContract.BannerTable.CONTENT_URI, projection
                    , WalletContract.BannerTable.POSITION_ID + "=?", new String[]{String.valueOf(positonId)}, WalletContract.BannerTable.RANK + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                WalletBannerListBean listBean = new WalletBannerListBean();
                WalletBannerListBean.WalletBannerBean[] array = new WalletBannerListBean.WalletBannerBean[cursor.getCount()];
                int index = 0;
                long version = -1;
                do {
                    WalletBannerListBean.WalletBannerBean bannerBean = new WalletBannerListBean.WalletBannerBean();
                    bannerBean.banner_id = cursor.getLong(0);
                    bannerBean.banner_name = cursor.getString(1);
                    bannerBean.position_id = cursor.getInt(2);
                    bannerBean.rank = cursor.getInt(3);
                    bannerBean.banner_type = cursor.getInt(4);
                    bannerBean.banner_post = cursor.getString(5);
                    bannerBean.banner_link = cursor.getString(6);
                    bannerBean.need_token = cursor.getInt(7);
                    bannerBean.jump_param = cursor.getString(8);
                    bannerBean.package_name = cursor.getString(9);
                    bannerBean.update_time = cursor.getLong(10);
                    array[index++] = bannerBean;
                    version = Math.max(version, bannerBean.update_time);
                } while (cursor.moveToNext());
                if (array.length > 0) {
                    listBean.list = array;
                    listBean.version = version;
                    return listBean;
                }
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public static BaseResponse<WalletBannerListBean> getBannerListFromNetwork(int positionId) {
        BaseRequestParams params = new BaseRequestParams(MainPanelHelper.MAIN_BANNER_LIST);
        params.addParameter(PARAM_POSITION_ID, positionId);
        BaseResponse<WalletBannerListBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<WalletBannerListBean>>() {
            };
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static void syncBannerListToDb(Context context, WalletBannerListBean listBean, int positonId) {
        if (context == null || listBean == null) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        operationList.add(ContentProviderOperation.newDelete(WalletContract.BannerTable.CONTENT_URI)
                .withSelection(WalletContract.BannerTable.POSITION_ID + "=?", new String[]{String.valueOf(positonId)}).build());
        if (listBean.list != null) {
            for (WalletBannerListBean.WalletBannerBean bean : listBean.list) {
                ContentValues values = new ContentValues();
                values.put(WalletContract.BannerTable.BANNER_ID, bean.banner_id);
                values.put(WalletContract.BannerTable.BANNER_NAME, bean.banner_name);
                values.put(WalletContract.BannerTable.POSITION_ID, bean.position_id);
                values.put(WalletContract.BannerTable.RANK, bean.rank);
                values.put(WalletContract.BannerTable.BANNER_TYPE, bean.banner_type);
                values.put(WalletContract.BannerTable.BANNER_POST, bean.banner_post);
                values.put(WalletContract.BannerTable.BANNER_LINK, bean.banner_link);
                values.put(WalletContract.BannerTable.NEED_TOKEN, bean.need_token);
                values.put(WalletContract.BannerTable.JUMP_PARAM, bean.jump_param);
                values.put(WalletContract.BannerTable.PACKAGE_NAME, bean.package_name);
                values.put(WalletContract.BannerTable.UPDATE_TIME, bean.update_time);
                operationList.add(ContentProviderOperation
                        .newInsert(WalletContract.BannerTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }

        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(WalletContract.AUTHORITY, operationList);
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
    }



    public static WalletTopListBean getTopListFromDb(Context context) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[] {
                WalletContract.MainTopTable.TOP_NAME,
                WalletContract.MainTopTable.TOP_HINT,
                WalletContract.MainTopTable.TOP_ICON,
                WalletContract.MainTopTable.TOP_RANK,
                WalletContract.MainTopTable.TOP_VERSION,
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(WalletContract.MainTopTable.CONTENT_URI, projection, null, null, WalletContract.MainTopTable.TOP_RANK + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                WalletTopListBean listBean = new WalletTopListBean();
                WalletTopListBean.WalletTopBean[] list = new WalletTopListBean.WalletTopBean[cursor.getCount()];
                int index = 0;
                long version = -1;
                do {
                    WalletTopListBean.WalletTopBean topBean = new WalletTopListBean.WalletTopBean();
                    topBean.name = cursor.getString(0);
                    topBean.title = cursor.getString(1);
                    topBean.icon = cursor.getString(2);
                    topBean.rank = cursor.getInt(3);
                    topBean.version=cursor.getLong(4);
                    list[index++] = topBean;
                    version = topBean.version;
                } while (cursor.moveToNext());
                if (list.length > 0) {
                    listBean.list = list;
                    listBean.version = version;
                    return listBean;
                }
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public static BaseResponse<WalletTopListBean> getTopListFromNetwork() {
        BaseRequestParams params = new BaseRequestParams(MainPanelHelper.MAIN_TOP_LIST);
        BaseResponse<WalletTopListBean> response = null;
        try {
            TypeToken typeToken = new TypeToken<BaseResponse<WalletTopListBean>>() {};
            response = xmain.http().getSync(params, typeToken.getType());
        } catch (Throwable throwable) {
            response = null;
        }
        return response;
    }

    public static void syncTopListToDb(Context context, WalletTopListBean listBean) {
        if (context == null || listBean == null) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        operationList.add(ContentProviderOperation.newDelete(WalletContract.MainTopTable.CONTENT_URI).build());

        if (listBean.list != null) {
            for (WalletTopListBean.WalletTopBean bean : listBean.list) {
                ContentValues values = new ContentValues();
                values.put(WalletContract.MainTopTable.TOP_NAME, bean.name);
                values.put(WalletContract.MainTopTable.TOP_HINT, bean.title);
                values.put(WalletContract.MainTopTable.TOP_ICON, bean.icon);
                values.put(WalletContract.MainTopTable.TOP_RANK, bean.rank);
                values.put(WalletContract.MainTopTable.TOP_VERSION, listBean.version);
                operationList.add(ContentProviderOperation
                        .newInsert(WalletContract.MainTopTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }
        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(WalletContract.AUTHORITY, operationList);
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
    }
}
