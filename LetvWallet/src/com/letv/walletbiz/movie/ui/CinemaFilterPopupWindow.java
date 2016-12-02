package com.letv.walletbiz.movie.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.letv.wallet.common.view.DividerItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.movie.beans.CinemaFilterBean;
import com.letv.walletbiz.movie.beans.CinemaList.Cinema;

import java.util.HashMap;
import java.util.List;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 16-2-18.
 */
public class CinemaFilterPopupWindow extends PopupWindow {

    public interface OnCategoryChangedListener {
        void onCategoryChanged(int category, String secCategoryName, Cinema[] cinemaArray);
    }

    private Context mContext;
    private CinemaFilterBean mCinemaFilterBean;

    private List<String> mAreaNameArray;
    private List<String> mBrandNameArray;
    private List<String> mSpecialNameArray;

    private SparseArray<Integer> mCateFilterArray;
    private HashMap<String, List<Cinema>> mAreaCinemaMap = new HashMap<String, List<Cinema>>();
    private HashMap<String, List<Cinema>> mBrandCinemaMap = new HashMap<String, List<Cinema>>();
    private HashMap<String, List<Cinema>> mSpecialCinemaMap = new HashMap<String, List<Cinema>>();

    private RecyclerView mCategoryView;
    private CategoryAdapter mCategoryAdapter;
    private RecyclerView mCategoryListView;
    private CategoryListAdapter mCategoryListAdapter;

    private OnCategoryChangedListener mListener;

    private int mCurrentCategory;

    private int mCurrentCategoryPos;

    private int mSelectedCategoryPos = 0;
    private int mSelectedCategoryListPos = 0;

    public CinemaFilterPopupWindow(Context context) {
        this(context, null);
    }

    public CinemaFilterPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CinemaFilterPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(context.getColor(R.color.movie_cinema_filter_bg)));

        mContext = context;
        setOutsideTouchable(true);
        initView();
    }

    private void initView() {
        Resources resources = mContext.getResources();
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_cinema_filter, null, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.movie_cinema_filter_height)));
        mCategoryView = (RecyclerView) view.findViewById(R.id.cinema_category);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCategoryView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, resources.getColor(R.color.colorDividerLineBg),
                DividerItemDecoration.VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.divider_width));
        mCategoryView.addItemDecoration(dividerItemDecoration);
        mCategoryAdapter = new CategoryAdapter(mContext);
        mCategoryView.setAdapter(mCategoryAdapter);

        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                mCurrentCategoryPos = position;
                mCategoryAdapter.setSelectedPos(position);
                List<String> nameArray = null;
                HashMap<String, List<Cinema>> categoryMap = null;
                int item = mCategoryAdapter.getItem(position);
                mCurrentCategory = item;
                if (item == CinemaFilterBean.CINEMA_CATEGORY_AREA) {
                    nameArray = mAreaNameArray;
                    categoryMap = mAreaCinemaMap;
                } else if (item == CinemaFilterBean.CINEMA_CATEGORY_BRAND) {
                    nameArray = mBrandNameArray;
                    categoryMap = mBrandCinemaMap;
                } else if (item == CinemaFilterBean.CINEMA_CATEGORY_SPECIAL) {
                    nameArray = mSpecialNameArray;
                    categoryMap = mSpecialCinemaMap;
                }
                mCategoryListAdapter.setData(nameArray, categoryMap);
                if (mSelectedCategoryPos == position && mSelectedCategoryListPos != 0) {
                    mCategoryListAdapter.setSelectedPos(mSelectedCategoryListPos);
                } else {
                    mCategoryListAdapter.setSelectedPos(mSelectedCategoryListPos > 0 ? -1 : 0);
                }
            }
        });
        mCategoryView.addOnItemTouchListener(itemClickListener);

        mCategoryListView = (RecyclerView) view.findViewById(R.id.cinema_category_list);
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCategoryListView.setLayoutManager(layoutManager);
        dividerItemDecoration = new DividerItemDecoration(mContext, resources.getColor(R.color.colorDividerLineBg),
                DividerItemDecoration.VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.divider_width));
        mCategoryListView.addItemDecoration(dividerItemDecoration);
        mCategoryListAdapter = new CategoryListAdapter(mContext);
        mCategoryListView.setAdapter(mCategoryListAdapter);

        RecyclerItemClickListener categoryListOnclick = new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                mSelectedCategoryPos = mCurrentCategoryPos;
                mSelectedCategoryListPos = position;
                mCategoryListAdapter.setSelectedPos(position);
                if (mListener != null) {
                    mListener.onCategoryChanged(mCurrentCategory, mCategoryListAdapter.getSecCategoryName(position), mCategoryListAdapter.getCinemaArrayAtPosition(position));
                }
            }
        });
        mCategoryListView.addOnItemTouchListener(categoryListOnclick);

        View transparentView = view.findViewById(R.id.half_transparent_view);
        transparentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(view);
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (mCinemaFilterBean != null) {
            if (mSelectedCategoryPos != -1) {
                mCategoryAdapter.setSelectedPos(mSelectedCategoryPos);
            }
            if (mSelectedCategoryListPos != -1) {
                mCategoryListAdapter.setSelectedPos(mSelectedCategoryListPos);
            }
            super.showAsDropDown(anchor);
        }
    }

    public void setData(CinemaFilterBean filterBean) {
        if (filterBean == null || filterBean == mCinemaFilterBean) {
            return;
        }
        mCinemaFilterBean = filterBean;
        initData(filterBean);
        mCategoryAdapter.setData(mCateFilterArray);

        int category = mCateFilterArray.keyAt(0);
        mCurrentCategory = category;
        mCategoryListAdapter.setData(getNameArray(category), getCinemaMap(category));
    }

    private List<String> getNameArray(int category) {
        List<String> list = null;
        switch (category) {
            case CinemaFilterBean.CINEMA_CATEGORY_AREA:
                list = mAreaNameArray;
                break;
            case CinemaFilterBean.CINEMA_CATEGORY_BRAND:
                list = mBrandNameArray;
                break;
            case CinemaFilterBean.CINEMA_CATEGORY_SPECIAL:
                list = mSpecialNameArray;
                break;
        }
        return list;
    }

    private HashMap<String, List<Cinema>> getCinemaMap(int category) {
        HashMap<String, List<Cinema>> map = null;
        switch (category) {
            case CinemaFilterBean.CINEMA_CATEGORY_AREA:
                map = mAreaCinemaMap;
                break;
            case CinemaFilterBean.CINEMA_CATEGORY_BRAND:
                map = mBrandCinemaMap;
                break;
            case CinemaFilterBean.CINEMA_CATEGORY_SPECIAL:
                map = mSpecialCinemaMap;
                break;
        }
        return map;
    }

    private void initData(CinemaFilterBean filterBean) {
        mCateFilterArray = new SparseArray<>();
        SparseArray<List<String>> nameMap = filterBean.getNameMap();
        for (int i=0; i<nameMap.size(); i++) {
            int key = nameMap.keyAt(i);
            mCateFilterArray.put(key, filterBean.getCategoryNameRes(key));
        }
        mAreaNameArray = filterBean.getNameArray(CinemaFilterBean.CINEMA_CATEGORY_AREA);
        mAreaCinemaMap = filterBean.getCinemaMap(CinemaFilterBean.CINEMA_CATEGORY_AREA);
        mBrandNameArray = filterBean.getNameArray(CinemaFilterBean.CINEMA_CATEGORY_BRAND);
        mBrandCinemaMap = filterBean.getCinemaMap(CinemaFilterBean.CINEMA_CATEGORY_BRAND);
        mSpecialNameArray = filterBean.getNameArray(CinemaFilterBean.CINEMA_CATEGORY_SPECIAL);
        mSpecialCinemaMap = filterBean.getCinemaMap(CinemaFilterBean.CINEMA_CATEGORY_SPECIAL);
    }

    public void setOnCategoryChangedListener(OnCategoryChangedListener listener) {
        mListener = listener;
    }

    class CategoryAdapter extends RecyclerView.Adapter<ItemHolder> {

        private LayoutInflater mInflater;
        private SparseArray<Integer> mData;
        private int mSelectedPos = -1;

        public CategoryAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(SparseArray<Integer> data) {
            mData = data;
            notifyDataSetChanged();
        }

        public void setSelectedPos(int position) {
            mSelectedPos = position;
            notifyDataSetChanged();
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.movie_cinema_filter_category_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.mNameView.setText(mData.valueAt(position));
            if (mSelectedPos == position) {
                holder.mNameView.setSelected(true);
            } else {
                holder.mNameView.setSelected(false);
            }
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public int getItem(int position) {
            if (mData == null || position < 0 || position >= mData.size()) {
                return -1;
            }
            return mData.keyAt(position);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        public TextView mNameView;
        public TextView mCountView;

        public ItemHolder(View itemView) {
            super(itemView);
            mNameView = (TextView) itemView.findViewById(R.id.category_name);
            mCountView = (TextView) itemView.findViewById(R.id.cinema_count);
        }
    }

    class CategoryListAdapter extends RecyclerView.Adapter<ItemHolder> {

        private Context mContext;
        private LayoutInflater mInflater;
        private HashMap<String, List<Cinema>> mCategoryMap;
        private List<String> mData;

        private int mSelectedPos = -1;

        public CategoryListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(List<String> data, HashMap<String, List<Cinema>> categoryMap) {
            mData = data;
            mCategoryMap = categoryMap;
            notifyDataSetChanged();
        }

        public void setSelectedPos(int position) {
            mSelectedPos = position;
            notifyDataSetChanged();
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.movie_cinema_filter_category_list_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            if (mData != null && position < mData.size()) {
                if (mSelectedPos == position) {
                    holder.itemView.setSelected(true);
                } else {
                    holder.itemView.setSelected(false);
                }
                holder.mNameView.setText(mData.get(position));
                holder.mCountView.setText(mContext.getResources().getQuantityString(R.plurals.movie_cinema_filter_cinema_count,
                        mCategoryMap.get(mData.get(position)).size(), mCategoryMap.get(mData.get(position)).size()));
            }
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public String getSecCategoryName(int position) {
            if (mData == null || position < 0 || position >= mData.size()) {
                return null;
            }
            return mData.get(position);
        }

        public Cinema[] getCinemaArrayAtPosition(int position) {
            if (mData != null && position >= 0 && position < mData.size() && mCategoryMap != null) {
                List<Cinema> list = mCategoryMap.get(mData.get(position));
                return list == null ? null : list.toArray(new Cinema[0]);
            }
            return null;
        }
    }
}
