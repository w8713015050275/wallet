package com.letv.walletbiz.movie.utils.GaussianBlur;

import android.graphics.Bitmap;


public abstract class BaseBlur implements IBlur {
    protected long mTimeMs;
    
    @Override
    public Bitmap blur(int radius, Bitmap in, int inSampleSize) {
        if (radius <= 0 || in == null) {
            return null;
        }
        
        if (inSampleSize <= 1) {
            return blur(radius, in);
            
        } else {
            long start = System.currentTimeMillis();

            int smallWidth = in.getWidth() / inSampleSize;
            int smallHeight = in.getHeight() / inSampleSize;
            
            Bitmap in_small = Bitmap.createScaledBitmap(in, smallWidth, smallHeight, false);
            Bitmap out = Bitmap.createBitmap(smallWidth, smallHeight, in.getConfig());
            
            radius = (int)(radius * 1f / inSampleSize + 0.5f);
            blur(radius, in_small, out);
            
            in_small.recycle();
            
            mTimeMs = System.currentTimeMillis() - start;

            return out;
        }
    }
    
}
