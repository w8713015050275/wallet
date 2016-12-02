package com.letv.leui.common.recommend.widget.moduleview;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.widget.LeRecommendType;
import com.letv.leui.common.recommend.widget.LeRecommendViewStyle;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendAllDTO;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendTaginfoDTO;
import com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.listener.OnBaseItemClickListener;

import java.util.ArrayList;

/**
 * Created by zhangjiahao on 15-8-31.
 */
public abstract class AbsLeRecommendView<T> extends RelativeLayout {

    protected Context mContext;
    private TextView mLabelName, mLabelActionName;
    private ImageView mLabelActionIcon;
    private RelativeLayout mLabelActionBox;
    private BaseItemClickListener mItemClickListener;

    protected RecommendTaginfoDTO tagInfo;
    protected String tagId;

    public AbsLeRecommendView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    private void initView() {
        RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(rLP);
        View rootView = View.inflate(mContext, R.layout.view_recommend, this);
        mLabelName = (TextView) rootView.findViewById(R.id.tv_label_name);
        mLabelActionName = (TextView) rootView.findViewById(R.id.tv_label_action_name);
        mLabelActionIcon = (ImageView) rootView.findViewById(R.id.iv_label_action_icon);
        mLabelActionBox = (RelativeLayout) rootView.findViewById(R.id.rl_label_action_box);

        // 替换ViewStub
        ViewStub vs_content = (ViewStub) rootView.findViewById(R.id.vs_content);
        vs_content.setLayoutResource(getContentLayoutResId());
        vs_content.setInflatedId(View.NO_ID);
        vs_content.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {
                initContentView(inflated);
            }
        });
        vs_content.inflate();
    }

    public final void setRecommendType(LeRecommendType recommendType, ArrayList<T> itemList, LeRecommendViewStyle style) {
        // set more label name
        Resources resources = mContext.getResources();
        if (style == LeRecommendViewStyle.WHITE) {
            mLabelName.setTextColor(resources.getColor(R.color.item_recommend_view_style_front_color1));
            mLabelActionName.setTextColor(resources.getColor(R.color.item_recommend_view_style_front_color2));
        }

        // set item click listener
        if (mItemClickListener == null) {
            mItemClickListener = new BaseItemClickListener(mContext, recommendType);
        }
        mItemClickListener.setTagInfo(tagInfo);
        mItemClickListener.setTagId(tagId);
        mLabelActionBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onLabelActionClick(v);
            }
        });
        setItemClickListener((BaseItemClickListener) mItemClickListener);

        // set data
        setViewData(recommendType, itemList, style);
    }

    public void setTagInfo(RecommendTaginfoDTO tagInfo) {
        this.tagInfo = tagInfo;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    /**
     * Register a callback to be invoked when this view or one of item is clicked.
     * <p/>
     * default ItemClickListener is {@link com.letv.leui.common.recommend.widget.adapter.listener.BaseItemClickListener} <br/>
     * It implements the default logic
     *
     * @param itemClickListener The callback that will run.
     * @see com.letv.leui.common.recommend.widget.adapter.listener.OnBaseItemClickListener
     */
    public final void addItemClickListener(OnBaseItemClickListener itemClickListener) {
        this.mItemClickListener = (BaseItemClickListener) itemClickListener;
    }

    /**
     * Sets the string value of the label TextView.
     *
     * @see android.widget.TextView#setText(CharSequence)
     */
    public void setLabelNameText(String text) {
        mLabelName.setText(text);
    }

    /**
     * Sets the string value of the label action TextView.
     *
     * @see android.widget.TextView#setText(CharSequence)
     */
    public void setLabelActionNameText(String text) {
        mLabelActionName.setText(text);
    }

    public void setLabelActionBoxVisible(int visible) {
        mLabelActionBox.setVisibility(visible);
    }

    protected abstract int getContentLayoutResId();

    protected abstract void initContentView(View rootView);

    protected abstract void setItemClickListener(BaseItemClickListener listener);

    protected abstract void setViewData(LeRecommendType recommendType, ArrayList<T> itemList, LeRecommendViewStyle style);
}
