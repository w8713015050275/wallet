
package com.letv.walletbiz.movie.utils.GaussianBlur;

import android.graphics.Bitmap;

/**
 * Interface for a blur algorithm
 */
public interface IBlur {
    /**
     * Takes a bitmap and blurs it with the given blur radius.
     *
     * @param radius blur radius, keep in mind some algorithms don't take all
     *            values (e.g. ScriptIntrinsicBlur will only take 1-25,
     *            RSStackBlur_xxx will only take 1-254)
     * @param in input bitmap
     * @return if in.isMutable() is true, modify input bitmap and return it if
     *         in.isMutable() is false, copy input bitmap to an new bitmap and
     *         return the new one
     */
    public Bitmap blur(int radius, Bitmap in);

    /**
     * Takes a bitmap and blurs it with the given blur radius.
     *
     * @param radius blur radius, keep in mind some algorithms don't take all
     *            values (e.g. ScriptIntrinsicBlur will only take 1-25,
     *            RSStackBlur_xxx will only take 1-254)
     * @param in input bitmap
     * @param inSampleSize If set to a value > 1, subsample the original image
     *            to an small image, blur the small image, and then return it.
     *            For example, inSampleSize == 4 returns an image that is 1/4
     *            the width/height of the original, and 1/16 the number of
     *            pixels. Any value <= 1 is treated the same as 1. Note: blur
     *            radius will be subsampled by inSampleSize
     * @return return an new small bitmap
     */
    public Bitmap blur(int radius, Bitmap in, int inSampleSize);

    /**
     * Takes a bitmap and blurs it with the given blur radius.
     *
     * @param radius blur radius, keep in mind some algorithms don't take all
     *            values (e.g. ScriptIntrinsicBlur will only take 1-25,
     *            RSStackBlur_xxx will only take 1-254)
     * @param in input bitmap
     * @param in output bitmap
     * 
     * it is forbidden to use, because input bitmap width must be multiple
     * due to RenderScript limitations
     */
    public void blur(int radius, Bitmap in, Bitmap out);

    /**
     * Recycle resources and threads You must call this to avoid resource
     * leaking
     */
    public void recycle();

    /**
     * Get Blur time consumed in milliseconds
     */
    public long getTimeMs();
}
