package org.xutils.image;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.xutils.ImageManager;
import org.xutils.common.Callback;
import org.xutils.xmain;

import java.io.File;

/**
 * Created by wyouflf on 15/10/9.
 */
public final class ImageManagerImpl implements ImageManager {


    private static final Object lock = new Object();
    private static ImageManagerImpl instance;

    private ImageManagerImpl() {
    }

    public static void registerInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ImageManagerImpl();
                }
            }
        }
        xmain.Ext.setImageManager(instance);
    }


    @Override
    public void bind(final ImageView view, final String url) {
        xmain.task().autoPost(new Runnable() {
            @Override
            public void run() {
                ImageLoader.doBind(view, url, null, null);
            }
        });
    }

    @Override
    public void bind(final ImageView view, final String url, final ImageOptions options) {
        xmain.task().autoPost(new Runnable() {
            @Override
            public void run() {
                ImageLoader.doBind(view, url, options, null);
            }
        });
    }

    @Override
    public void bind(final ImageView view, final String url, final Callback.CommonCallback<Drawable> callback) {
        xmain.task().autoPost(new Runnable() {
            @Override
            public void run() {
                ImageLoader.doBind(view, url, null, callback);
            }
        });
    }

    @Override
    public void bind(final ImageView view, final String url, final ImageOptions options, final Callback.CommonCallback<Drawable> callback) {
        xmain.task().autoPost(new Runnable() {
            @Override
            public void run() {
                ImageLoader.doBind(view, url, options, callback);
            }
        });
    }

    @Override
    public Callback.Cancelable loadDrawable(String url, ImageOptions options, Callback.CommonCallback<Drawable> callback) {
        return ImageLoader.doLoadDrawable(url, options, callback);
    }

    @Override
    public Callback.Cancelable loadFile(String url, ImageOptions options, Callback.CommonCallback<File> callback) {
        return ImageLoader.doLoadFile(url, options, callback);
    }

    @Override
    public void clearMemCache() {
        ImageLoader.clearMemCache();
    }

    @Override
    public void clearCacheFiles() {
        ImageLoader.clearCacheFiles();
        ImageDecoder.clearCacheFiles();
    }
}
