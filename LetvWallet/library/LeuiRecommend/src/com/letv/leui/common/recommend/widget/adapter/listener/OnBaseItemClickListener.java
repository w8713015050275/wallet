package com.letv.leui.common.recommend.widget.adapter.listener;

import android.view.View;

/**
 * Created by dupengtao on 14-12-6.
 */
public interface OnBaseItemClickListener {

    /**
     * Called when a item has been clicked.
     *  @param view     item view was clicked.
     * @param position the child's index
     * @param count item size
     */
    public void onItemClick(View view, int position, int count);

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @see android.view.View.OnClickListener
     */
    public void onLabelActionClick(View view);


}
