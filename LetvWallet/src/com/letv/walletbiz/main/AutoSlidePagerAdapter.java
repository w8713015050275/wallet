package com.letv.walletbiz.main;

import android.support.v4.view.PagerAdapter;

/**
 * Created by liuliang on 17-1-24.
 */

public abstract class AutoSlidePagerAdapter extends PagerAdapter {

    public abstract int getRealCount();

    public abstract int getItemIndexForPosition(int position);
}
