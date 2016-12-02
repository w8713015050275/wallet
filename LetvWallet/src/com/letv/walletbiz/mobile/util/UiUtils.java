package com.letv.walletbiz.mobile.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.mobile.MobileConstant;

/**
 * Created by linquan on 15-11-27.
 */
public class UiUtils {
    //1已创建, 2:支付中, 3:支付完成, 4:充值中, 5:充值完成
    private static final int MobileOrderStatusDisplayID[] = {
            R.string.mobile_order_status_created,
            R.string.mobile_order_status_created,
            R.string.mobile_order_status_paid,
            R.string.mobile_order_status_depositing,
            R.string.mobile_order_status_charged,
            R.string.mobile_order_status_cancelled
    };

    public static String getOrderStatusStringbyValue(Context context, int status) {

        if (status > 0 && status <= MobileOrderStatusDisplayID.length) {
            return context.getString(MobileOrderStatusDisplayID[status - 1]);
        }
        return null;
    }

    public static String getProductJsonStub(int type) {
        final String mobile = "{" +
                "  'provice' : 'beijing'," +
                "  'isp' : 'cmcc'," +
                "  'product_list' : [ " +
                "{'product_id':0,'orig_price':20, 'product_name':'20', 'price':0}," +
                "{'product_id':0,'orig_price':30, 'product_name':'30', 'price':0}," +
                "{'product_id':0,'orig_price':50, 'product_name':'50', 'price':0}," +
                "{'product_id':0,'orig_price':100, 'product_name':'100', 'price':0}," +
                "{'product_id':0,'orig_price':200,'product_name':'200','price':0}," +
                "{'product_id':0,'orig_price':500,'product_name':'500','price':0}" +
                "]" +
                " }";
        final String flow = "{" +
                "  'provice' : 'beijing'," +
                "  'isp' : 'cmcc'," +
                "  'product_list' : [ " +
                "{'product_id':0,'orig_price':10, 'product_name':'10', 'price':0}," +
                "{'product_id':0,'orig_price':20, 'product_name':'20', 'price':0}," +
                "{'product_id':0,'orig_price':30, 'product_name':'30', 'price':0}," +
                "{'product_id':0,'orig_price':50, 'product_name':'50', 'price':0}," +
                "{'product_id':0,'orig_price':100,'product_name':'100','price':0}," +
                "{'product_id':0,'orig_price':200,'product_name':'200','price':0}" +
                "]" +
                " }";
        final String stub = (type == MobileConstant.PRODUCT_TYPE.MOBILE_FEE ? mobile : flow);
        return stub;
    }

    public static String getDevicePhoneNumber(Context context) {
        String phoneNumber = DeviceUtils.getPhoneNumber0(context);
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumber = DeviceUtils.getPhoneNumber1(context);
        }
        return phoneNumber;
    }

    //查询指定电话的联系人姓名，邮箱
    public static String getContactNameByNumber(Context context, String number) throws Exception {
        String name = null;
        String phoneNumber = null;
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse(Phone.CONTENT_FILTER_URI + "/" + number);
        Cursor cursor = resolver.query(uri, new String[]{"display_name", Phone.NUMBER}, null, null, null);
        while (cursor.moveToNext()) {
            name = cursor.getString(0);
            LogHelper.v("[UiUtils] Contacts Name %s", name);
            phoneNumber = cursor.getString(1);
            LogHelper.v("[UiUtils] Contacts phoneNumber %s", phoneNumber);
            if (PhoneNumberUtils.checkPhoneNumber(phoneNumber, true) != null) {
                return name;
            }

        }
        cursor.close();
        return "";
    }
}
