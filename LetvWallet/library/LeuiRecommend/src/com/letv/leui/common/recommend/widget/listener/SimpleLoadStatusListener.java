package com.letv.leui.common.recommend.widget.listener;

import com.letv.leui.common.recommend.net.ILoadStatusListener;

/**
 * Created by dupengtao on 15-1-8.
 */
public abstract class SimpleLoadStatusListener implements ILoadStatusListener{
    @Override
    public void onSuccess(int statusId) {

    }

    @Override
    public void onError(int statusId, Exception e) {

    }
}
