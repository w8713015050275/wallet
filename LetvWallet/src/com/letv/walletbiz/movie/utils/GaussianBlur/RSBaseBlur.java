package com.letv.walletbiz.movie.utils.GaussianBlur;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.RenderScript;

public abstract class RSBaseBlur extends BaseBlur {
	protected RenderScript mRS;
	
	
	public RSBaseBlur(Context context) {
	    mRS = RenderScript.create(context);
	}
	
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
            
            // This is added due to RenderScript limitations.
            // If bitmap width is not multiple of 4 - in RenderScript
            // index = y * width does not calculate correct index for line start index.
            smallWidth = smallWidth  & ~0x03;
            
            int smallHeight = in.getHeight() * smallWidth / in.getWidth();
            
            Bitmap out = Bitmap.createScaledBitmap(in, smallWidth, smallHeight, false);
            
            //radius = (int)(radius * 1f / inSampleSize + 0.5f);
            radius = Math.round(radius / inSampleSize + 0.5f);
            blur(radius, out, out);
            
            mTimeMs = System.currentTimeMillis() - start;

            return out;
        }
    }
    
    @Override
    public Bitmap blur(int radius, Bitmap in) {
        if (radius < 0 || in == null) {
            return null;
        }
        
        long start = System.currentTimeMillis();
        
        
        // make sure input image width is multiple of 4
        int width = in.getWidth();
        int height = in.getHeight();
        
        Bitmap out;
        if (width % 4 != 0) {
            // This is added due to RenderScript limitations.
            // If bitmap width is not multiple of 4 - in RenderScript
            // index = y * width does not calculate correct index for line start index.
            width = width & ~0x03;
            out = Bitmap.createScaledBitmap(in, width, height, false);
            
        } else {
            
            if (in.isMutable()) {
                out = in;
            } else {
                out = in.copy(in.getConfig(), true);
            }
        }
        
        blur(radius, out, out);
        
        mTimeMs = System.currentTimeMillis() - start;
        
        return out;
    }
    
    
    @Override
    public long getTimeMs() {
        return mTimeMs;
    }
    
	
	@Override
	public void recycle() {
		if (mRS != null) { 
		    mRS.destroy();
		    mRS = null;
		}
	}
	
}
