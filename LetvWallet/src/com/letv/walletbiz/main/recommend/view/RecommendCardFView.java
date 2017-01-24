package com.letv.walletbiz.main.recommend.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.wallet.common.util.DensityUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.recommend.RecommendUtils;
import com.letv.walletbiz.main.recommend.bean.BaseCardBean;
import com.letv.walletbiz.main.recommend.bean.CardFBean;

import org.xutils.xmain;

import java.util.HashMap;
import java.util.List;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 2017/2/6.
 */

public class RecommendCardFView extends LinearLayout implements BaseCardView {

    private TextView mTitleView;
    private RecyclerView mRecyclerView;

    private CardFRecyclerAdapter mAdapter;

    private CardFBean mCardBean;

    public RecommendCardFView(Context context) {
        this(context, null);
    }

    public RecommendCardFView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecommendCardFView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        int padding = (int) DensityUtils.dip2px(10);
        setPadding(0, padding, 0, padding);
        View.inflate(context, R.layout.main_recommend_cardview_f, this);
        mTitleView = (TextView) findViewById(R.id.content_title);
        mRecyclerView = (RecyclerView) findViewById(R.id.content_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration((int) DensityUtils.dip2px(5)));
        mAdapter = new CardFRecyclerAdapter(LayoutInflater.from(context));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (mAdapter == null) {
                    return;
                }
                CardFBean.ImageBean bean = mAdapter.getItem(position);
                if (bean == null || TextUtils.isEmpty(bean.img_link)) {
                    return;
                }
                RecommendUtils.launchUrl(getContext(), bean.img_link);
            }
        }));
    }

    @Override
    public boolean checkContent() {
        return true;
    }

    @Override
    public HashMap<String, String> getContentParam() {
        return null;
    }

    @Override
    public void bindView(List<BaseCardBean> cardList) {
        if (cardList == null || cardList.size() <= 0) {
            return;
        }
        if (mCardBean == cardList.get(0) || !(cardList.get(0) instanceof CardFBean)) {
            return;
        }
        mCardBean = (CardFBean) cardList.get(0);
        if (mCardBean != null) {
            mTitleView.setText(mCardBean.title);
            mAdapter.setData(mCardBean.imgs);
        }
    }

    @Override
    public boolean needTopDivider() {
        return true;
    }

    @Override
    public boolean needBottomDivider() {
        return true;
    }

    static class CardFViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImagView;
        private TextView mTitleView;

        public CardFViewHolder(View itemView) {
            super(itemView);
            mImagView = (ImageView) itemView.findViewById(R.id.content_img);
            mTitleView = (TextView) itemView.findViewById(R.id.content_title);
        }
    }

    static class CardFRecyclerAdapter extends RecyclerView.Adapter<CardFViewHolder> {

        private LayoutInflater mInflater;

        private CardFBean.ImageBean[] mImageArray;

        public CardFRecyclerAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public void setData(CardFBean.ImageBean[] imageArray) {
            mImageArray = imageArray;
        }

        public CardFBean.ImageBean getItem(int position) {
            if (mImageArray == null) {
                return null;
            }
            if (position < 0 || position >= mImageArray.length) {
                return null;
            }
            return mImageArray[position];
        }

        @Override
        public CardFViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mInflater == null) {
                return null;
            }
            View view = mInflater.inflate(R.layout.main_recommend_cardview_f_item, parent, false);
            CardFViewHolder holder = new CardFViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(CardFViewHolder holder, int position) {
            CardFBean.ImageBean bean = mImageArray[position];
            xmain.image().bind(holder.mImagView, bean.img_url);
            holder.mTitleView.setText(bean.img_desc);
        }

        @Override
        public int getItemCount() {
            return mImageArray == null ? 0 : mImageArray.length;
        }
    }

    static class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int mSpace;

        public SpaceItemDecoration(int space) {
            mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            RecyclerView.Adapter adapter = parent.getAdapter();
            if (adapter == null || adapter.getItemCount() <= 1) {
                super.getItemOffsets(outRect, view, parent, state);
                return;
            }
            outRect.right = 0;
            outRect.top = 0;
            outRect.bottom = 0;
            int pos = parent.getChildAdapterPosition(view);
            if (pos != 0) {
                outRect.left = mSpace;
            } else {
                outRect.left = (int) DensityUtils.dip2px(10);
            }
        }
    }
}
