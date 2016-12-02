package com.letv.leui.common.recommend.widget.moduleview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.letv.leui.common.recommend.widget.LeRecommendType;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener;
import com.letv.leui.common.R;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-9-1.
 */
public abstract class LeRecommendVerticalView<T extends LeRecommendVerticalView.BaseViewHolder, K> extends AbsLeRecommendView<K> {

    protected LinearLayout ll_rootView;
    private BaseItemClickListener mBaseItemClickListener;

    public LeRecommendVerticalView(Context context, int count) {
        super(context);
        addChildView(count);
    }

    @Override
    protected final int getContentLayoutResId() {
        return R.layout.view_recommend_vertical;
    }

    @Override
    protected final void initContentView(View rootView) {
        ll_rootView = (LinearLayout) rootView;
    }

    private void addChildView(int count) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < count; i++) {
            ViewGroup childView = createItemView(inflater);

            T viewHolder = getViewHolder(childView);

            childView.setTag(viewHolder);

            ViewGroup.LayoutParams layoutParams = childView.getLayoutParams();
            layoutParams.width =
                    layoutParams.width == LayoutParams.WRAP_CONTENT ? LayoutParams.MATCH_PARENT : layoutParams.width;
            layoutParams.height =
                    layoutParams.height == LayoutParams.WRAP_CONTENT ? LayoutParams.WRAP_CONTENT : layoutParams.height;
            childView.setLayoutParams(layoutParams);

            ll_rootView.addView(childView, i);
        }
    }

    @Override
    protected final void setItemClickListener(BaseItemClickListener listener) {
        this.mBaseItemClickListener = listener;
    }

    @Override
    protected final void setViewData(LeRecommendType recommendType, ArrayList itemList, LeRecommendViewStyle style) {
        setDataForItemClickListener(mBaseItemClickListener, itemList);

        for (int i = 0; i < ll_rootView.getChildCount(); i++) {

            ViewGroup child = (ViewGroup) ll_rootView.getChildAt(i);
            T holder = (T) child.getTag();

            final int position = i;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBaseItemClickListener.onItemClick(v, position, ll_rootView.getChildCount());
                }
            });

            onBindViewHolder(holder, i, itemList, style);
        }
    }

    protected abstract void setDataForItemClickListener(BaseItemClickListener baseItemClickListener, ArrayList<K> itemList);

    protected abstract ViewGroup createItemView(LayoutInflater inflater);

    protected abstract T getViewHolder(ViewGroup itemView);

    protected abstract void onBindViewHolder(T holder, int i, ArrayList<K> list, LeRecommendViewStyle style);

    protected abstract class BaseViewHolder {

        public BaseViewHolder(ViewGroup viewGroup) {
            initViewHolder(viewGroup);
        }

        protected abstract void initViewHolder(ViewGroup viewGroup);
    }
}
