
package com.letv.walletbiz.movie.utils.GaussianBlur;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.ScriptIntrinsicBlur;

import com.letv.wallet.common.util.LogHelper;

public class RSGaussianBlur extends RSBaseBlur {
    private final String TAG = "RSGaussianBlur";
    
    // constant blur radius which is supported by ScriptIntrinsicBlur class
    protected static final int MAX_BLUR_RADIUS = 25;

    protected ScriptIntrinsicBlur mIntrinsic;

    public RSGaussianBlur(Context context) {
        super(context);
        mIntrinsic = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
    }
    
    @Override
    public void blur(int radius, Bitmap in, Bitmap out) {
        if (radius < 1 || in == null || out == null) {
            LogHelper.d("[%S] radius < 1 or in == null or out == null", TAG);
            return;
        }
        
        if (in.getWidth() % 4 != 0) {
            LogHelper.d("[%S] input bitmap width must be multiple of 4 due to RenderScript limitation", TAG);
            return;
        }
        
        Allocation tmpIn = Allocation.createFromBitmap(mRS, in, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation tmpOut = Allocation.createFromBitmap(mRS, out, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        radius = Math.min(radius, MAX_BLUR_RADIUS);
        LogHelper.d("[%S] radius " + radius , TAG);

        mIntrinsic.setRadius(radius);
        mIntrinsic.setInput(tmpIn);
        mIntrinsic.forEach(tmpOut);

        tmpOut.copyTo(out);
        tmpIn.destroy();
        tmpOut.destroy();
    }

    @Override
    public void recycle() {
        super.recycle();
        if (mIntrinsic != null) {
            mIntrinsic.destroy();
        }
    }

}
