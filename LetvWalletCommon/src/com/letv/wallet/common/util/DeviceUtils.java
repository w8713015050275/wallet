package com.letv.wallet.common.util;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liuliang on 16-11-21.
 */

public class DeviceUtils {

    public static String getDeviceImei(Context context) {
        if (context == null) {
            return null;
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e) {
            LogHelper.e(e.toString());
        }
        return null;
    }

    public static String getPhoneNumber0(Context context) {
        return getPhoneNumber(context, 0);
    }

    public static String getPhoneNumber1(Context context) {
        return getPhoneNumber(context, 1);
    }

    public static String getPhoneNumber(Context context, int slotid) {
        if (context == null) {
            return null;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = -1;
        try {
            simState = getSimState(telephonyManager, slotid);
        } catch (Exception e) {
        }
        if (simState == TelephonyManager.SIM_STATE_READY) {
            return getSimcardModel(slotid, context);
        }
        return null;
    }

    private static int getSimState(TelephonyManager telephonyManager, int slotid)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class clazz = Class.forName("android.telephony.TelephonyManager");
        Method method = clazz.getMethod("getSimState", int.class);
        return (int) method.invoke(telephonyManager, slotid);
    }

    private static String getSimcardModel(int slotid, Context ctx) {
        String phoneNmber = null;
        try {
            Class<SubscriptionManager> subMgr = SubscriptionManager.class;
            Method getSubId = subMgr.getMethod("getSubId", int.class);
            Method getActiveSubscriptionInfo = subMgr.getMethod("getActiveSubscriptionInfo", int.class);
            int[] subid = (int[]) getSubId.invoke(subMgr, slotid);
            if (subid[0] <= -1) {
                return phoneNmber;
            }
            Method getSubMgrObj = subMgr.getMethod("from", Context.class);
            Object subMgrObj = getSubMgrObj.invoke(subMgr, ctx);
            if (subMgrObj == null) {
                return phoneNmber;
            }

            Object subInfoObj = getActiveSubscriptionInfo.invoke(subMgrObj, subid[0]);
            if (subInfoObj != null) {
                Class subInfoRecord = subInfoObj.getClass();

                Method getNumber = subInfoRecord.getMethod("getNumber");
                String number = (String) getNumber.invoke(subInfoObj);
                if (!TextUtils.isEmpty(number)) {
                    if (number.startsWith("+86")) {
                        number = number.replace("+86", "");
                    }
                    phoneNmber = number;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return phoneNmber;
    }

    public static boolean isDeviceRoot() {
        return RootCheckHelper.isRoot();
    }

    public static boolean isEmulator() {
        return EmulatorCheckHelper.getEmulatorCheckHelper().isEmulator();
    }
}
