package com.letv.walletbiz.movie.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.letv.wallet.common.util.LogHelper;
import com.letv.walletbiz.movie.utils.GaussianBlur.IBlur;
import com.letv.walletbiz.movie.utils.GaussianBlur.RSGaussianBlur;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.xmain;

/**
 * Created by liuliang on 16-3-15.
 */
public class BlurImageHelper {
    public static final String TAG = BlurImageHelper.class.getName();

    private final static int MEM_CACHE_MAX_SIZE = 1024 * 1024 * 4; // 4M
    
    public static final int BLUR_RADIUS = 150;
    public static final int BLUR_INSAMPLE_SIZE = 4;

    private IBlur mIBlur;
    private BlurCallback mCallback;
    private ImageOptions mBlurImageOption;

    private final static LruCache<String, BitmapDrawable> MEM_CACHE =
            new LruCache<String, BitmapDrawable>(MEM_CACHE_MAX_SIZE) {

                @Override
                protected int sizeOf(String key, BitmapDrawable drawable) {
                    return drawable.getBitmap() == null ? 0 : drawable.getBitmap().getByteCount();
                }
            };

    static {
        int memClass = ((ActivityManager) xmain.app().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;  // Use 1/8th of the available memory for this memory cache.
        if (cacheSize > MEM_CACHE_MAX_SIZE) {
            cacheSize = MEM_CACHE_MAX_SIZE;
        }
        MEM_CACHE.resize(cacheSize);
    }

    public BlurImageHelper(Context mContext) {
        mIBlur = new RSGaussianBlur(mContext);
        mBlurImageOption = new ImageOptions.Builder().setConfig(Bitmap.Config.ARGB_8888).build();
    }

    public static void clearMemCache() {
        MEM_CACHE.evictAll();
    }

    public void getBlurImage(final String url, BlurCallback callback){
        if(TextUtils.isEmpty(url) || callback == null){
            return;
        }

        this.mCallback = callback;

        Drawable memDrawable = getBitmapFromMemoryCache(url);

        if (memDrawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) memDrawable).getBitmap();
            if (bitmap == null || bitmap.isRecycled()) {
                memDrawable = null;
            }
        }
        if (memDrawable != null) {
            mCallback.onBlurFinished(url, memDrawable);
        }else{
            xmain.image().loadDrawable(url, mBlurImageOption, new Callback.CommonCallback<Drawable>() {
                @Override
                public void onSuccess(Drawable result) {
                    BitmapDrawable blurIcon  = blur(result);
                    addBitmapToMemoryCache(url, blurIcon);
                    if(mCallback != null){
                        mCallback.onBlurFinished(url, blurIcon);
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
        }
    }

    public void recycle() {
        if (mIBlur != null) {
            mIBlur.recycle();
            mIBlur = null;
        }
    }

    public interface BlurCallback {
        void onBlurFinished(String url , Drawable blurIcon);
    }

    private BitmapDrawable blur(Drawable drawable) {
        BitmapDrawable blurIcon = null;
        if (drawable != null && drawable instanceof BitmapDrawable) {
            Bitmap in = ((BitmapDrawable) drawable).getBitmap();
            blurIcon = new BitmapDrawable(mIBlur.blur(BLUR_RADIUS, in, BLUR_INSAMPLE_SIZE));
            in.recycle();
        }
        LogHelper.d("[%S] blur - complete : " + mIBlur.getTimeMs()+ " Ms", TAG);
        return blurIcon;
    }

    private void addBitmapToMemoryCache(String key, BitmapDrawable drawable) {
        LogHelper.d("maxSize size kb = " + MEM_CACHE.maxSize()/ 1024);
        if (getBitmapFromMemoryCache(key) == null) {
            MEM_CACHE.put(key, drawable);
            LogHelper.d("now size kb = " + MEM_CACHE.size() / 1024);
        }
    }

    private BitmapDrawable getBitmapFromMemoryCache(String key) {
        return MEM_CACHE.get(key);
    }
}
