package com.letv.walletbiz.movie.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.view.BlankPage;
import com.letv.walletbiz.R;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.xmain;

/**
 * Created by liuliang on 16-3-31.
 */
public class MoviePhotoGalleryItemFragment extends BaseFragment {

    private ImageView mPhotoView;
    private int mPosition;
    private String mUrl;
    private Drawable mDrawable;

    private View.OnClickListener mOnViewClickListener;

    private static final int MSG_LOAD_SUCCESS = 1;
    private static final int MSG_LOAD_FAILED = 2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (!isAdded() || isDetached()) {
                return;
            }
            switch (msg.what) {
                case MSG_LOAD_SUCCESS:
                    hideLoadingView();
                    mDrawable = (Drawable) msg.obj;
                    if (mDrawable != null) {
                        mPhotoView.setImageDrawable(mDrawable);
                    }
                    break;
                case MSG_LOAD_FAILED:
                    hideLoadingView();
                    if (isNetworkAvailable()) {
                        showBlankPage().setCustomPage(getString(R.string.movie_photo_gallery_failed), BlankPage.Icon.NO_ACCESS);
                    } else {
                        showBlankPage(BlankPage.STATE_NO_NETWORK);
                    }
                    break;
            }
        }
    };

    private Callback.CommonCallback<Drawable> mCallback = new Callback.CommonCallback<Drawable>() {

        @Override
        public void onSuccess(Drawable result) {
            Message msg= mHandler.obtainMessage(MSG_LOAD_SUCCESS);
            msg.obj = result;
            msg.sendToTarget();
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            Message msg= mHandler.obtainMessage(MSG_LOAD_FAILED);
            msg.sendToTarget();
        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    };

    public MoviePhotoGalleryItemFragment() {
    }

    public MoviePhotoGalleryItemFragment(int position, String url, View.OnClickListener listener) {
        mPosition = position;
        mUrl = url;
        mOnViewClickListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPhotoView = (ImageView) inflater.inflate(R.layout.movie_photo_item, container, false);
        if (mOnViewClickListener != null) {
            container.setOnClickListener(mOnViewClickListener);
        }
        return mPhotoView;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDrawable == null) {
            loadData();
        }
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (mDrawable == null && isNetworkAvailable) {
            loadData();
        }
    }

    private void loadData() {
        showLoadingView();
        ImageOptions.Builder builder = new ImageOptions.Builder();
        builder.setCrop(false);
        builder.setConfig(Bitmap.Config.ARGB_8888);
        builder.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
        xmain.image().loadDrawable(mUrl, builder.build(), mCallback);
    }
}
