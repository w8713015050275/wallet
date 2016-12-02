package com.letv.wallet.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.letv.shared.widget.LeNeverPermissionRequestDialog;

import java.util.ArrayList;

/**
 * Created by liuliang on 16-3-10.
 */
public class PermissionCheckHelper {

    public static final int PERMISSION_ALLOWED = 1;
    public static final int PERMISSION_REFUSED = 2;
    public static final int PERMISSION_REQUESTING = 3;

    public static LeNeverPermissionRequestDialog showLeNeverPermissionRequestDialog(Context context, String permission, View.OnClickListener cancelClickListener) {
        LeNeverPermissionRequestDialog dialog = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PermissionInfo info = packageManager.getPermissionInfo(permission, 0);
            ArrayList<PermissionInfo> list = new ArrayList<PermissionInfo>(1);
            list.add(info);
            dialog = new LeNeverPermissionRequestDialog(context, list, cancelClickListener);
            dialog.setCancelable(false);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (dialog != null) {
            dialog.appear();
        }
        return dialog;
    }

    public static int checkReadPhoneStatePermission(Activity activity, int requestCode) {
        if (activity == null) {
            return PERMISSION_REFUSED;
        }
        int result = PERMISSION_ALLOWED;
        boolean hasPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, requestCode);
            result = PERMISSION_REQUESTING;
        }
        return result;
    }

    public static int checkContactsPermission(Activity activity, int requestCode) {
        if (activity == null) {
            return PERMISSION_REFUSED;
        }
        int result = PERMISSION_ALLOWED;
        boolean hasPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission && requestCode != -1) {
            result = PERMISSION_REQUESTING;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, requestCode);
        }
        return result;
    }

    public static int checkCallPermission(Activity activity, int requestCode) {
        if (activity == null) {
            return PERMISSION_REFUSED;
        }
        int result = PERMISSION_ALLOWED;
        boolean hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            result = PERMISSION_REQUESTING;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, requestCode);
        }
        return result;
    }

    public static int checkLocationPermission(Activity activity, int requestCode) {
        if (activity == null) {
            return PERMISSION_REFUSED;
        }
        boolean hasFinePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarsePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        int result = PERMISSION_REFUSED;
        if (hasFinePermission && hasCoarsePermission) {
            result = PERMISSION_ALLOWED;
        } else if (requestCode != -1) {
            result = PERMISSION_REQUESTING;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }
        return result;
    }
}
