package com.letv.walletbiz.push.notice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.push.beans.PushMessage;

/**
 * Created by lijujying on 16-7-12.
 */
public class PushNoticeManager {
    private static final String TAG = PushNoticeManager.class.getSimpleName();

    private static final int NOTIFICATION_ID_DEFAULT = 1;

    public static void sendNotification(Context context, PushMessage pushMessage) {
        if (context == null || pushMessage == null) {
            LogHelper.w("[%S] : context & pushMessage is empty ", TAG);
            return;
        }
        Notification.Builder builder = new Notification.Builder(context);
        buildIcon(context, builder, pushMessage);
        builder.setTicker(pushMessage.getTitle());
        builder.setContentTitle(pushMessage.getTitle());
        builder.setContentText(pushMessage.getContent());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        Intent intent = new Intent(context, NoticeActionService.class);
        intent.putExtra("pushMessage", pushMessage);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_DEFAULT, builder.build());

    }

    // 设置图标
    private static void buildIcon(Context context, Notification.Builder builder, PushMessage info) {
        if (null != info.getBitmap()) {
            builder.setLargeIcon(info.getBitmap());
        } else {
            Drawable drawable = context.getResources().getDrawable(R.mipmap.ic_launcher);
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            builder.setLargeIcon(bitmap);
        }
        builder.setSmallIcon(R.drawable.wallet_statusbar_icon);
    }


    // 取消noticeId对应通知
    public static void cancel(Context context, int noticeId) {
        if (null != context) {
            NotificationManager manger = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manger.cancel(noticeId);
        }

    }

    // 取消所有通知
    public static void cancelAll(Context context) {
        cancel(context, NOTIFICATION_ID_DEFAULT);
    }

}
