package com.letv.walletbiz.member;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;
import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.util.IOUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.walletbiz.member.beans.BannerListBean;
import com.letv.walletbiz.member.beans.BannerListBean.BannerBean;
import com.letv.walletbiz.member.beans.MemberTypeListBean;
import com.letv.walletbiz.member.beans.MemberTypeListBean.MemberTypeBean;
import com.letv.walletbiz.member.beans.MemberTypeListBean.GoodItem;
import com.letv.walletbiz.member.beans.ProductListBean;
import com.letv.walletbiz.member.beans.ProductListBean.ProductBean;
import com.letv.walletbiz.member.provider.MemberDBConstant;
import com.letv.walletbiz.member.provider.MemberDBConstant.BannerTable;
import com.letv.walletbiz.member.provider.MemberDBConstant.MemberTypeTable;
import com.letv.walletbiz.member.provider.MemberDBConstant.ProductTable;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by zhangzhiwei1 on 16-11-21.
 */

public class MemberHelper {

    public static MemberTypeListBean getMemberTypeListFromDb(Context context) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[]{
                MemberTypeTable.ID,
                MemberTypeTable.NAME,
                MemberTypeTable.TYPE,
                MemberTypeTable.GOODS_ID,
                MemberTypeTable.STATE,
                MemberTypeTable.RANK,
                MemberTypeTable.PROTOCOL_LINK,
                MemberTypeTable.IMG_URL,
                MemberTypeTable.DESCRIPTION,
                MemberTypeTable.GOODS_JSON,
                MemberTypeTable.UPDATE_TIME,
                MemberTypeTable.ADD_TIME,
                MemberTypeTable.OPERATOR
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MemberTypeTable.CONTENT_URI, projection
                    , null, null, MemberTypeTable.RANK + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                MemberTypeListBean listBean = new MemberTypeListBean();
                MemberTypeBean[] array = new MemberTypeBean[cursor.getCount()];
                int index = 0;
                do {
                    MemberTypeBean bean = new MemberTypeBean();
                    bean.id = String.valueOf(cursor.getLong(0));
                    bean.name = cursor.getString(1);
                    bean.type = cursor.getString(2);
                    bean.goods_id = cursor.getString(3);
                    bean.state = cursor.getString(4);
                    bean.rank = cursor.getString(5);
                    bean.protocol_link = cursor.getString(6);
                    bean.img_url = cursor.getString(7);
                    bean.description = cursor.getString(8);

                    String goodsJsonStr = new String(cursor.getBlob(9), Charset.forName("UTF-8"));
                    TypeToken typeToken = new TypeToken<GoodItem[]>() {};
                    GoodItem[] goodsBean= ParseHelper.parseByGson(goodsJsonStr, typeToken.getType());
                    bean.goods = goodsBean;

                    bean.update_time = cursor.getString(10);
                    bean.add_time = cursor.getString(11);
                    bean.operator = cursor.getString(12);
                    array[index++] = bean;
                } while (cursor.moveToNext());
                if (array.length > 0) {
                    listBean.list = array;
                    return listBean;
                }
            }
        } catch(Exception e) {

        }
        finally{
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public static void syncMemberTypeListToDb(Context context, MemberTypeListBean listBean) {
        if (context == null || listBean == null) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        operationList.add(ContentProviderOperation.newDelete(MemberTypeTable.CONTENT_URI).build());
        if (listBean.list != null) {
            for (MemberTypeBean bean : listBean.list) {
                ContentValues values = new ContentValues();
                values.put(MemberTypeTable.ID, Integer.valueOf(bean.id));
                values.put(MemberTypeTable.NAME, bean.name);
                values.put(MemberTypeTable.TYPE, bean.type);
                values.put(MemberTypeTable.GOODS_ID, bean.goods_id);
                values.put(MemberTypeTable.STATE, bean.state);
                values.put(MemberTypeTable.RANK, bean.rank);
                values.put(MemberTypeTable.PROTOCOL_LINK, bean.protocol_link);
                values.put(MemberTypeTable.IMG_URL, bean.img_url);
                values.put(MemberTypeTable.DESCRIPTION, bean.description);
                values.put(MemberTypeTable.UPDATE_TIME, bean.update_time);
                values.put(MemberTypeTable.ADD_TIME, bean.add_time);
                values.put(MemberTypeTable.OPERATOR, bean.operator);

                if (!TextUtils.isEmpty(bean.goods_json)) {
                    values.put(MemberTypeTable.GOODS_JSON, bean.goods_json.getBytes(Charset.forName("UTF-8")));
                }

                operationList.add(ContentProviderOperation
                        .newInsert(MemberTypeTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }

        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(MemberDBConstant.AUTHORITY, operationList);
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
    }

    public static BannerListBean getBannerListFromDb(Context context,String memberType) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[]{
                BannerTable.BANNER_ID,
                BannerTable.BANNER_NAME,
                BannerTable.POSITION_ID,
                BannerTable.RANK,
                BannerTable.BANNER_TYPE,
                BannerTable.BANNER_POST,
                BannerTable.BANNER_LINK,
                BannerTable.BANNER_TOKEN,
                BannerTable.JUMP_PARA,
                BannerTable.PACKAGE_NAME,
                BannerTable.UPDATE_TIME,
                BannerTable.VERSION
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(BannerTable.CONTENT_URI, projection
                    , BannerTable.MEMBER_TYPE + "=?", new String[]{ memberType }, BannerTable.RANK + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                BannerListBean listBean = new BannerListBean();
                BannerBean[] array = new BannerBean[cursor.getCount()];
                int index = 0;
                long version = -1;
                do {
                    BannerBean bannerBean = new BannerBean();
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
                    version = cursor.getLong(11);;
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

    public static void syncBannerListToDb(Context context, BannerListBean listBean,String memberType) {
        if (context == null || listBean == null) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        operationList.add(ContentProviderOperation.newDelete(BannerTable.CONTENT_URI).withSelection(BannerTable.MEMBER_TYPE + "=?", new String[]{memberType}).build());
        if (listBean.list != null) {
            for (BannerBean bean : listBean.list) {
                ContentValues values = new ContentValues();
                values.put(BannerTable.MEMBER_TYPE, memberType);
                values.put(BannerTable.BANNER_ID, bean.banner_id);
                values.put(BannerTable.BANNER_NAME, bean.banner_name);
                values.put(BannerTable.POSITION_ID, bean.position_id);
                values.put(BannerTable.RANK, bean.rank);
                values.put(BannerTable.BANNER_TYPE, bean.banner_type);
                values.put(BannerTable.BANNER_POST, bean.banner_post);
                values.put(BannerTable.BANNER_LINK, bean.banner_link);
                values.put(BannerTable.BANNER_TOKEN, bean.need_token);
                values.put(BannerTable.JUMP_PARA, bean.jump_param);
                values.put(BannerTable.PACKAGE_NAME, bean.package_name);
                values.put(BannerTable.UPDATE_TIME, bean.update_time);
                values.put(BannerTable.VERSION, listBean.version);
                operationList.add(ContentProviderOperation
                        .newInsert(BannerTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }

        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(MemberDBConstant.AUTHORITY, operationList);
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
    }


    public static ProductListBean getProductListFromDb(Context context,String memberType) {
        if (context == null) {
            return null;
        }
        String[] projection = new String[]{
                ProductTable.ID,
                ProductTable.SKU_NO,
                ProductTable.NAME,
                ProductTable.PRICE,
                ProductTable.KIND,
                ProductTable.MONTH_PRICE,
                ProductTable.TAG,
                ProductTable.DESCRIPTION,
                ProductTable.DURATION,
                ProductTable.MEMBER_TYPE,
                ProductTable.SPU_NAME,
                ProductTable.PROTOCOL_URL
        };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ProductTable.CONTENT_URI, projection
                    , ProductTable.MEMBER_TYPE + "=?", new String[]{ memberType }, ProductTable.ID + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                ProductListBean listBean = new ProductListBean();
                ProductBean[] array = new ProductBean[cursor.getCount()];
                int index = 0;
                do {
                    ProductBean bean = new ProductBean();
                    bean.id = cursor.getInt(0);
                    bean.sku_no = cursor.getString(1);
                    bean.name = cursor.getString(2);
                    bean.price = cursor.getString(3);
                    bean.kind = cursor.getString(4);
                    bean.month_price = cursor.getString(5);
                    bean.tag = cursor.getString(6);
                    bean.description = cursor.getString(7);
                    bean.duration = cursor.getString(8);
                    bean.memberType = cursor.getString(9);
                    bean.spu_name = cursor.getString(10);
                    bean.protocol_url = cursor.getString(11);
                    array[index++] = bean;
                } while (cursor.moveToNext());
                if (array.length > 0) {
                    listBean.list = array;
                    return listBean;
                }
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        return null;
    }

    public static void syncProductListToDb(Context context, ProductListBean listBean, String memberType) {
        if (context == null || listBean == null) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        operationList.add(ContentProviderOperation.newDelete(ProductTable.CONTENT_URI).withSelection(ProductTable.MEMBER_TYPE + "=?", new String[]{memberType}).build());
        if (listBean.list != null) {
            for (ProductBean bean : listBean.list) {
                ContentValues values = new ContentValues();
                values.put(ProductTable.MEMBER_TYPE, memberType);
                values.put(ProductTable.ID, bean.id);
                values.put(ProductTable.SKU_NO, bean.sku_no);
                values.put(ProductTable.NAME, bean.name);
                values.put(ProductTable.PRICE, bean.price);
                values.put(ProductTable.KIND, bean.kind);
                values.put(ProductTable.MONTH_PRICE, bean.month_price);
                values.put(ProductTable.TAG, bean.tag);
                values.put(ProductTable.DESCRIPTION, bean.description);
                values.put(ProductTable.DURATION, bean.duration);
                values.put(ProductTable.SPU_NAME, bean.spu_name);
                values.put(ProductTable.PROTOCOL_URL, bean.protocol_url);
                operationList.add(ContentProviderOperation
                        .newInsert(ProductTable.CONTENT_URI)
                        .withValues(values)
                        .build());
            }
        }

        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(MemberDBConstant.AUTHORITY, operationList);
        } catch (RemoteException e) {
            LogHelper.e(e);
        } catch (OperationApplicationException e) {
            LogHelper.e(e);
        }
    }

    public static boolean isCacheExpire(Context mContext) {
        return true;
    }
}
