package com.letv.leui.common.recommend.widget.adapter;


import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.volley.toolbox.ImageLoader;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendCalendarDTO;

import java.util.Calendar;
import java.util.List;

/**
 * Created by dupengtao on 14-12-6.
 */
public class RecommendCalendarAdapter extends BaseRecommendAdapter<RecommendCalendarDTO, RecommendCalendarAdapter.RecommendCalendarViewHolder> {

    private LeRecommendViewStyle mCurViewStyle;

    public RecommendCalendarAdapter(List<RecommendCalendarDTO> dataSet) {
        this(dataSet, LeRecommendViewStyle.NORMAL);
    }

    public RecommendCalendarAdapter(List<RecommendCalendarDTO> dataSet,LeRecommendViewStyle style) {
        super(dataSet);
        mCurViewStyle=style;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_recommend_calendar;
    }

    @Override
    RecommendCalendarViewHolder getViewHolder(RelativeLayout itemView) {
        return new RecommendCalendarViewHolder(itemView);
    }

    @Override
    float getScreenItemCount(int screenW, int parentPaddingLeft, int parentPaddingRight, int end, int start) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecommendCalendarViewHolder holder, int i) {
        if (mHasMoreItem && i == getItemCount() - 1) {
            holder.setItemState(true);
        } else {
            holder.setItemState(false);
            RecommendCalendarDTO vo = mDataSet.get(i);
            holder.setContent(vo.getTitle());
            try {
                long l = Long.parseLong(vo.getTime());
                holder.setMonth(getMonth(getMonth(l)));
                holder.setData(String.valueOf(getData(l)));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public int getMonth(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public String getMonth(int m) {
        switch (m) {
            case 1: {
                return "一月";
            }
            case 2: {
                return "二月";
            }
            case 3: {
                return "三月";
            }
            case 4: {
                return "四月";
            }
            case 5: {
                return "五月";
            }
            case 6: {
                return "六月";
            }
            case 7: {
                return "七月";
            }
            case 8: {
                return "八月";
            }
            case 9: {
                return "九月";
            }
            case 10: {
                return "十月";
            }
            case 11: {
                return "十一月";
            }
            case 12: {
                return "十二月";
            }
        }
        return "";
    }

    public int getData(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DATE);
    }

    public class RecommendCalendarViewHolder extends BaseRecommendAdapter.BaseRecommendViewHolder {
        ImageLoader.ImageContainer imageContainer;
        RelativeLayout rlContext, lrMoreItem;
        private TextView tvMonth, tvData, tvContent;

        public RecommendCalendarViewHolder(RelativeLayout itemView) {
            super(itemView);
        }

        @Override
        protected void initItemView(RelativeLayout mItemView) {
            rlContext = (RelativeLayout) mItemView.findViewById(R.id.rl_context_box);
            lrMoreItem = (RelativeLayout) mItemView.findViewById(R.id.item_recommend_more);
            tvMonth = (TextView) mItemView.findViewById(R.id.tv_calendar_month);
            tvData = (TextView) mItemView.findViewById(R.id.tv_calendar_data);
            tvContent = (TextView) mItemView.findViewById(R.id.tv_calendar_content);
            if(LeRecommendViewStyle.WHITE==mCurViewStyle){
                tvMonth.setTextColor(Color.parseColor("#80FFFFFF"));
                tvData.setTextColor(Color.WHITE);
                tvContent.setTextColor(Color.WHITE);
            }
        }

        public void setItemState(boolean isMoreItem) {
            if (isMoreItem) {
                lrMoreItem.setVisibility(View.VISIBLE);
                rlContext.setVisibility(View.GONE);
            } else {
                lrMoreItem.setVisibility(View.GONE);
                rlContext.setVisibility(View.VISIBLE);
            }
        }

        public void setMonth(String text) {
            tvMonth.setText(text);
        }

        public void setData(String text) {
            tvData.setText(text);
        }

        public void setContent(String text) {
            tvContent.setText(text);
        }
    }
}
