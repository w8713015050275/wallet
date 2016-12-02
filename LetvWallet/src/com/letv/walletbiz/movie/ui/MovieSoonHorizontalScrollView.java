package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Created by lijujying on 16-8-1.
 */
public class MovieSoonHorizontalScrollView extends HorizontalScrollView {
    public MovieSoonHorizontalScrollView(Context context) {
        super(context);
    }

    public MovieSoonHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieSoonHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MovieSoonHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void resetScrollWidth(int index){
        ViewGroup parent = (ViewGroup)getChildAt(0);
        if(index < 0 || index >= parent.getChildCount()){
            return;
        }
        View view;
        int left = 0;
        for(int i = 0; i < index; i++){
            view = parent.getChildAt(i);
            view.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            left += view.getMeasuredWidth();
        }
        view = parent.getChildAt(index);
        view.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int right = left + view.getMeasuredWidth();

        if(right < getWidth()){
            this.smoothScrollTo(0, 0);
        }else{
            this.smoothScrollTo(right - getWidth(), 0);
        }
    }
}
