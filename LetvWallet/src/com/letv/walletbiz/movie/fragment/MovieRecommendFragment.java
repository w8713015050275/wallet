package com.letv.walletbiz.movie.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letv.leui.common.recommend.widget.LeRecommendViewGroup;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.walletbiz.R;

/**
 * Created by liuliang on 16-3-21.
 */
public class MovieRecommendFragment extends BaseFragment {

    private long mTagId;
    private LeRecommendViewGroup mRecommendView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_recommend_layout, container, false);
        mRecommendView = (LeRecommendViewGroup) view.findViewById(R.id.lrvg);
        mRecommendView.setViewStyle(LeRecommendViewStyle.NORMAL);
        mRecommendView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                updateReconnendView(mRecommendView);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {

            }
        });
        return view;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    public void setTagId(long tagId) {
        mTagId = tagId;
        mRecommendView.load(String.valueOf(mTagId));
    }

    private void updateReconnendView(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        View view;
        for (int i = 0; i < count; i++) {
            view = viewGroup.getChildAt(i);
            if (view instanceof RecyclerView) {
                ((RecyclerView) view).setNestedScrollingEnabled(false);
            } else if (view instanceof ViewGroup) {
                updateReconnendView((ViewGroup) view);
            }
        }
    }
}
