package com.letv.walletbiz.coupon.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.letv.walletbiz.R;

import org.xutils.image.ImageOptions;

/**
 * Created by lijujying on 16-5-19.
 */
public class ImageOptionsHelper {
    public final static String TAG = ImageOptionsHelper.class.getSimpleName();


    public static ImageOptions getDefaltImageLoaderOptions() {

        return new ImageOptions.Builder().setFailureDrawableId(R.drawable.place_holder_img)
                .setLoadingDrawableId(R.drawable.place_holder_img).build();
    }

    public static ImageOptions getImageLoaderOptions(Context mContext, int width, int height) {
        Drawable d = zoomDrawable(mContext, width, height);
        return new ImageOptions.Builder().setFailureDrawable(d).setLoadingDrawable(d).build();
    }

    public static Drawable zoomDrawable(Context mContext,  int w, int h) {
        Drawable drawable = mContext.getDrawable(R.drawable.place_holder_img);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable, width, height);
        Matrix matrix = new Matrix();
        float sx = ((float) w / width);
        float sy = ((float) h / height);
        matrix.postScale(sx, sy);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(mContext.getResources(), newbmp);
    }

    public static Bitmap drawableToBitmap(Drawable drawable, int w, int h) {
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }


}
