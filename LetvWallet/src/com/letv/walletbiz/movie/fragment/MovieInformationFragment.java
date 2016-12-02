package com.letv.walletbiz.movie.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.IOUtils;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.coupon.utils.ImageOptionsHelper;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MoviePhotoGalleryActivity;
import com.letv.walletbiz.movie.activity.MovieStillListActivity;
import com.letv.walletbiz.movie.beans.MovieDetail;
import com.letv.walletbiz.movie.utils.MoviePriorityExecutorHelper;

import org.xutils.common.task.PriorityExecutor;
import org.xutils.xmain;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 16-3-18.
 */
public class MovieInformationFragment extends BaseFragment implements View.OnClickListener {

    public static final int MOVIE_DESCRIPTION_MORE_UP = 1;
    public static final int MOVIE_DESCRIPTION_MORE_DOWN = 2;

    private PriorityExecutor mPriorityExecutor;
    private SparseArray<AttentionRunnable> mAttentionRunnableArray = new SparseArray<>();

    private MovieDetail mMovieDetail;

    private View mDescriptionContainer;
    private TextView mDescriptionView;
    private ImageView mDescriptionMoreView;

    private View mStarContainer;
    private RecyclerView mStarRecyclerView;
    private StarAdapter mStarAdapter;

    private View mPrevueContainer;
    private RecyclerView mPrevueRecyclerView;
    private PrevueAdapter mPrevueAdapter;

    private View mMovieAttentionContainer;
    private ImageView mMovieIconView;
    private TextView mMovieNameView;
    private TextView mMovieAttentionCount;
    private Button mMovieAttentionButton;

    private View mMoviePhotoContainer;
    private TextView mMoviePhotoMoreView;
    private RecyclerView mMoviePhotoRecyclerView;
    private PhotoAdapter mMoviePhotoAdapter;

    private AttentionCallback mAttentionCallback = new AttentionCallback() {

        @Override
        public void onQueryFinished(boolean isAttention) {
            Message msg = mHandler.obtainMessage(MSG_ATTENTION_QUERY_FINISHED);
            msg.obj = isAttention;
            msg.sendToTarget();
        }

        @Override
        public void onAttentionFinished(int mode, boolean succeed) {
            Message msg;
            if (mode == AttentionRunnable.MODE_SUBSCRIBE) {
                msg = mHandler.obtainMessage(MSG_ATTENTION_SUBSCRIBE_FINISHED);
            } else {
                msg = mHandler.obtainMessage(MSG_ATTENTION_UNSUBSCRIBE_FINISHED);
            }
            msg.obj = succeed;
            msg.sendToTarget();
        }
    };

    private static final int MSG_ATTENTION_QUERY_FINISHED = 1;
    private static final int MSG_ATTENTION_SUBSCRIBE_FINISHED = 2;
    private static final int MSG_ATTENTION_UNSUBSCRIBE_FINISHED = 3;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (isDetached() || !isAdded()) {
                return;
            }
            switch (msg.what) {
                case MSG_ATTENTION_QUERY_FINISHED:
                    mAttentionRunnableArray.remove(AttentionRunnable.MODE_QUERY);
                    Boolean isAttention = (Boolean) msg.obj;
                    if (isAttention != null && isAttention == true) {
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribed);
                        mMovieAttentionButton.setTag(true);
                    } else {
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribe);
                        mMovieAttentionButton.setTag(false);
                    }
                    break;
                case MSG_ATTENTION_SUBSCRIBE_FINISHED:
                    mAttentionRunnableArray.remove(AttentionRunnable.MODE_SUBSCRIBE);
                    Boolean succeed = (Boolean) msg.obj;
                    if (succeed != null && succeed == true) {
                        mMovieAttentionButton.setTag(true);
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribed);
                    } else {
                        mMovieAttentionButton.setTag(false);
                        Toast.makeText(getContext(), R.string.movie_detail_subscribe_failed, Toast.LENGTH_SHORT).show();
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribe);
                    }
                    break;
                case MSG_ATTENTION_UNSUBSCRIBE_FINISHED:
                    mAttentionRunnableArray.remove(AttentionRunnable.MODE_SUBSCRIBE);
                    succeed = (Boolean) msg.obj;
                    if (succeed != null && succeed == true) {
                        mMovieAttentionButton.setTag(false);
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribe);
                    } else {
                        mMovieAttentionButton.setTag(true);
                        Toast.makeText(getContext(), R.string.movie_detail_unsubscribe_failed, Toast.LENGTH_SHORT).show();
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribed);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPriorityExecutor = MoviePriorityExecutorHelper.getPriorityExecutor();
    }

    @Override
    public View onCreateCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_detail_information_layout, container, false);

        mDescriptionContainer = view.findViewById(R.id.movie_description_container);
        mDescriptionView = (TextView) view.findViewById(R.id.movie_description);
        mDescriptionMoreView = (ImageView) view.findViewById(R.id.movie_description_more);
        mDescriptionMoreView.setTag(MOVIE_DESCRIPTION_MORE_DOWN);
        mDescriptionMoreView.setOnClickListener(this);

        mStarContainer = view.findViewById(R.id.star_container);
        mStarRecyclerView = (RecyclerView) view.findViewById(R.id.star_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mStarRecyclerView.setLayoutManager(linearLayoutManager);
        mStarAdapter = new StarAdapter(getContext());
        mStarRecyclerView.setAdapter(mStarAdapter);
        mStarRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                MovieDetail.MovieStar star = mStarAdapter.getItem(position);
                if (star == null) {
                    return;
                }
                try {
                    Action.uploadClick(Action.MOVIE_DIRECTOR_STAR_CLICK, String.valueOf(star.tag_id));
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.letv.android.accountinfo", "com.letv.android.accountinfo.activity.StarPageActivity"));
                    intent.putExtra("starIconUrl", star.icon);
                    intent.putExtra("tagid", star.tag_id);
                    intent.putExtra("starName", star.name);
                    intent.putExtra("tagType", 2);
                    intent.putExtra("themeId", 0);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        mStarRecyclerView.setNestedScrollingEnabled(false);

        mPrevueContainer = view.findViewById(R.id.prevue_container);
        mPrevueRecyclerView = (RecyclerView) view.findViewById(R.id.prevue_recyclerview);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPrevueRecyclerView.setLayoutManager(linearLayoutManager);
        mPrevueAdapter = new PrevueAdapter(getContext());
        mPrevueRecyclerView.setAdapter(mPrevueAdapter);
        mPrevueRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                MovieDetail.MoviePrevue prevue = mPrevueAdapter.getItem(position);
                try {
                    if (prevue != null) {
                        Action.uploadClick(Action.MOVIE_PREVUE_CLICK, String.valueOf(prevue.mid));

                        String scheme = "letvclient://msiteAction?actionType=9&back=1&from=leui18&vid=" + prevue.mid
                                + "&processId=" + android.os.Process.myPid();
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(scheme));
                        startActivity(intent);
                    }
                } catch (Exception e) {
                }
            }
        }));
        mPrevueRecyclerView.setNestedScrollingEnabled(false);

        mMovieAttentionContainer = view.findViewById(R.id.movie_attention_container);
        mMovieIconView = (ImageView) view.findViewById(R.id.movie_icon);
        mMovieNameView = (TextView) view.findViewById(R.id.movie_name);
        mMovieAttentionCount = (TextView) view.findViewById(R.id.movie_attention_count);
        mMovieAttentionButton = (Button) view.findViewById(R.id.movie_attention);
        mMovieAttentionButton.setTag(false);
        mMovieAttentionButton.setOnClickListener(this);
        mMovieAttentionContainer.setOnClickListener(this);

        mMoviePhotoContainer = view.findViewById(R.id.movie_photo_container);
        mMoviePhotoMoreView = (TextView) view.findViewById(R.id.movie_photo_more);
        mMoviePhotoMoreView.setOnClickListener(this);
        mMoviePhotoRecyclerView = (RecyclerView) view.findViewById(R.id.movie_photo_recyclerview);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mMoviePhotoRecyclerView.setLayoutManager(linearLayoutManager);
        mMoviePhotoAdapter = new PhotoAdapter(getContext());
        mMoviePhotoRecyclerView.setAdapter(mMoviePhotoAdapter);
        mMoviePhotoRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), MoviePhotoGalleryActivity.class);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_PHOTO_INDEX, position);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_PHOTO_ARRAY, mMoviePhotoAdapter.getData());
                startActivity(intent);
                String url = "";
                if (mMoviePhotoAdapter.getData() != null) {
                    url = mMoviePhotoAdapter.getData()[position].middle;
                }
                Action.uploadMoviePhotoClick(Action.MOVIE_MOVIE_STILLS_CLICK, String.valueOf(mMovieDetail.movie_id), url);
            }
        }));
        mMoviePhotoRecyclerView.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMovieDetail != null) {
            attention(AttentionRunnable.MODE_QUERY);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.movie_description_more:
                updateDescriptionMoreView(v);
                break;
            case R.id.movie_photo_more:
                if (mMovieDetail == null) {
                    break;
                }
                intent = new Intent(getContext(), MovieStillListActivity.class);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ID, mMovieDetail.movie_id);
                startActivity(intent);
                break;
            case R.id.movie_attention:
                if (AccountHelper.getInstance().loginLetvAccountIfNot(getActivity(), null)) {
                    Boolean isAttention = (Boolean) v.getTag();
                    if (isAttention != null && isAttention == true) {
                        attention(AttentionRunnable.MODE_UNSUBSCRIBE);
                        mMovieAttentionButton.setText(R.string.movie_detail_unsubscribing);
                    } else {
                        attention(AttentionRunnable.MODE_SUBSCRIBE);
                        mMovieAttentionButton.setText(R.string.movie_detail_subscribing);
                    }
                }
                break;
            case R.id.movie_attention_container:
                if (mMovieDetail == null || mMovieDetail.workTag == null || mMovieDetail.workTag.length <= 0) {
                    break;
                }
                intent = new Intent();
                intent.setComponent(new ComponentName("com.letv.android.accountinfo", "com.letv.android.accountinfo.activity.StarPageActivity"));
                intent.putExtra("starIconUrl", mMovieDetail.workTag[0].icon);
                intent.putExtra("tagid", mMovieDetail.workTag[0].tag_id);
                intent.putExtra("starName", mMovieDetail.name);
                intent.putExtra("tagType", 2);
                intent.putExtra("themeId", 0);
                startActivity(intent);
                break;
        }
    }

    private void updateDescriptionMoreView(View v) {
        Integer tag = (Integer) v.getTag();
        if (tag == MOVIE_DESCRIPTION_MORE_DOWN) {
            mDescriptionView.setLines((Integer) mDescriptionView.getTag());
            mDescriptionMoreView.setImageResource(R.drawable.ic_movie_detail_collapse);
            mDescriptionMoreView.setTag(MOVIE_DESCRIPTION_MORE_UP);
        } else {
            mDescriptionView.setLines(4);
            mDescriptionMoreView.setImageResource(R.drawable.ic_movie_detail_expansion);
            mDescriptionMoreView.setTag(MOVIE_DESCRIPTION_MORE_DOWN);
        }

    }

    public void setData(MovieDetail movieDetail) {
        mMovieDetail = movieDetail;
        if (isDetached() || !isAdded()) {
            return;
        }
        attention(AttentionRunnable.MODE_QUERY);
        loadStarIcon();
        upateView();
    }

    private void attention(int mode) {
        if (mMovieDetail == null || mMovieDetail.workTag == null || mMovieDetail.workTag.length <= 0) {
            return;
        }
        int key = mode != AttentionRunnable.MODE_QUERY ? AttentionRunnable.MODE_SUBSCRIBE : mode;
        AttentionRunnable runnable = mAttentionRunnableArray.get(key);
        if (runnable == null) {
            if (mode == AttentionRunnable.MODE_SUBSCRIBE) {
                Action.uploadSubscribeClick(Action.MOVIE_ATTENTION_CLICK, String.valueOf(mMovieDetail.workTag[0].tag_id));
            }
            runnable = new AttentionRunnable(getContext(), mMovieDetail.workTag[0].tag_id, mode, mAttentionCallback);
            mAttentionRunnableArray.put(key, runnable);
            mPriorityExecutor.execute(runnable);
        }
    }

    private void loadStarIcon() {

    }

    private void upateView() {
        if (mMovieDetail == null) {
            return;
        }
        if (TextUtils.isEmpty(mMovieDetail.description)) {
            mDescriptionContainer.setVisibility(View.GONE);
        } else {
            mDescriptionContainer.setVisibility(View.VISIBLE);
            mDescriptionView.setText(mMovieDetail.description);
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mDescriptionView.getLayoutParams();
            if (mDescriptionView.getLineCount() > 4) {
                mDescriptionView.setTag(mDescriptionView.getLineCount());
                mDescriptionView.setLines(4);
                mDescriptionMoreView.setVisibility(View.VISIBLE);
                mDescriptionMoreView.setImageResource(R.drawable.ic_movie_detail_expansion);
                marginParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, 0);
            } else {
                mDescriptionMoreView.setVisibility(View.GONE);
                marginParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin,
                        getResources().getDimensionPixelOffset(R.dimen.movie_detail_description_padding_bottom));
            }
        }

        if (mMovieDetail.workTag != null && mMovieDetail.workTag.length > 0) {
            mMovieAttentionContainer.setVisibility(View.VISIBLE);
            MovieDetail.WorkTag workTag = mMovieDetail.workTag[0];
            if (workTag != null) {
                xmain.image().bind(mMovieIconView, workTag.icon);
                mMovieNameView.setText(workTag.name);
                if (!TextUtils.isEmpty(workTag.follow_count)) {
                    mMovieAttentionCount.setVisibility(View.VISIBLE);
                    mMovieAttentionCount.setText( getResources().getQuantityString(R.plurals.movie_attention_count_formatter, Integer.parseInt(workTag.follow_count), workTag.follow_count));
                } else {
                    mMovieAttentionCount.setVisibility(View.GONE);
                }
            }
        } else {
            mMovieAttentionContainer.setVisibility(View.GONE);
        }

        if (mMovieDetail.peopleTag == null || mMovieDetail.peopleTag.length <= 0) {
            mStarContainer.setVisibility(View.GONE);
        } else {
            mStarContainer.setVisibility(View.VISIBLE);
            mStarAdapter.setData(mMovieDetail.peopleTag);
        }
        if (mMovieDetail.prevue == null || mMovieDetail.prevue.length <= 0) {
            mPrevueContainer.setVisibility(View.GONE);
        } else {
            mPrevueContainer.setVisibility(View.VISIBLE);
            mPrevueAdapter.setData(mMovieDetail.prevue);
        }
        if (mMovieDetail.pictures == null || mMovieDetail.pictures.picAll == null || mMovieDetail.pictures.picAll.length <= 0) {
            mMoviePhotoContainer.setVisibility(View.GONE);
        } else {
            mMoviePhotoContainer.setVisibility(View.VISIBLE);
            mMoviePhotoAdapter.setData(mMovieDetail.pictures.picAll);
        }
    }

    private static class StarViewHolder extends RecyclerView.ViewHolder {

        public TextView starNameView;
        public ImageView starIconView;

        public StarViewHolder(View itemView) {
            super(itemView);
            starNameView = (TextView) itemView.findViewById(R.id.star_name);
            starIconView = (ImageView) itemView.findViewById(R.id.star_icon);
        }
    }

    private class StarAdapter extends RecyclerView.Adapter<StarViewHolder> {

        private Context mContext;

        private MovieDetail.MovieStar[] mStarArray;

        public StarAdapter(Context context) {
            mContext = context;
        }

        public void setData(MovieDetail.MovieStar[] starArray) {
            mStarArray = starArray;
            notifyDataSetChanged();
        }

        @Override
        public StarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_detail_star_item, parent, false);
            return new StarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StarViewHolder holder, int position) {
            if (mStarArray == null || position < 0 || position >= mStarArray.length) {
                return;
            }
            MovieDetail.MovieStar star = mStarArray[position];
            holder.starNameView.setText(star.name);
            xmain.image().bind(holder.starIconView, star.icon);
        }

        @Override
        public int getItemCount() {
            return mStarArray == null ? 0 : mStarArray.length;
        }

        public MovieDetail.MovieStar getItem(int position) {
            if (mStarArray == null || position < 0 || position >= mStarArray.length) {
                return null;
            }
            return mStarArray[position];
        }
    }

    private class AttentionRunnable implements Runnable {

        public static final int MODE_QUERY = 1;
        public static final int MODE_SUBSCRIBE = 2;
        public static final int MODE_UNSUBSCRIBE = 3;

        private Context mContext;
        private long mWorkTag;
        private AttentionCallback mCallback;
        private int mMode;

        public AttentionRunnable(Context context, long workTag, int mode, AttentionCallback callback) {
            mContext = context;
            mWorkTag = workTag;
            mMode = mode;
            mCallback = callback;
        }

        public int getMode() {
            return mMode;
        }

        @Override
        public void run() {
            if (mContext == null) {
                return;
            }
            boolean succeed;
            switch (mMode) {
                case MODE_QUERY:
                    if (mCallback != null) {
                        mCallback.onQueryFinished(queryAttention());
                    }
                    break;
                case MODE_SUBSCRIBE:
                    succeed = subscribe();
                    if (mCallback != null) {
                        mCallback.onAttentionFinished(MODE_SUBSCRIBE, succeed);
                    }
                    break;
                case MODE_UNSUBSCRIBE:
                    succeed = unSubscribe();
                    if (mCallback != null) {
                        mCallback.onAttentionFinished(MODE_UNSUBSCRIBE, succeed);
                    }
                    break;
            }


        }

        private boolean queryAttention() {
            Uri uri = Uri.parse("content://com.letv.android.eco/tags");
            String[] projection = new String[]{
                    "tagid",
                    "name",
                    "icon",
            };
            String selection = "tagid=?";
            String[] selectionArgs = new String[]{String.valueOf(mWorkTag)};
            Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            boolean isAttention = false;
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    isAttention = true;
                }
                return isAttention;
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }

        private boolean subscribe() {
            String token = AccountHelper.getInstance().getToken(mContext);
            Uri uri = Uri.parse("content://com.letv.android.eco/tags/subscribe/v1?sso_tk= + " + token + "&tagid=" + mWorkTag + "&appid=wallet");
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int errno = cursor.getInt(cursor.getColumnIndex("errno"));
                    if (errno == 10000) {
                        return true;
                    }
                }
                return false;
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }

        private boolean unSubscribe() {
            String token = AccountHelper.getInstance().getToken(mContext);
            Uri uri = Uri.parse("content://com.letv.android.eco/tags/unsubscribe/v1?sso_tk= + " + token + "&tagid=" + mWorkTag + "&appid=wallet");
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int errno = cursor.getInt(cursor.getColumnIndex("errno"));
                    if (errno == 10000) {
                        return true;
                    }
                }
                return false;
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
    }

    private interface AttentionCallback {
        void onQueryFinished(boolean isAttention);

        void onAttentionFinished(int mode, boolean succeed);
    }

    private class PrevueViewHolder extends RecyclerView.ViewHolder {

        public TextView prevueName;
        public ImageView prevueIcon;

        public PrevueViewHolder(View itemView) {
            super(itemView);
            prevueName = (TextView) itemView.findViewById(R.id.prevue_name);
            prevueIcon = (ImageView) itemView.findViewById(R.id.prevue_icon);
        }
    }

    private class PrevueAdapter extends RecyclerView.Adapter<PrevueViewHolder> {

        private Context mContext;

        private MovieDetail.MoviePrevue[] mPrevueArray;

        public PrevueAdapter(Context context) {
            mContext = context;
        }

        public void setData(MovieDetail.MoviePrevue[] prevueArray) {
            mPrevueArray = prevueArray;
            notifyDataSetChanged();
        }

        @Override
        public PrevueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_detail_prevue_item, parent, false);
            return new PrevueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PrevueViewHolder holder, int position) {
            if (mPrevueArray == null || position < 0 || position >= mPrevueArray.length) {
                return;
            }
            MovieDetail.MoviePrevue moviePrevues = mPrevueArray[position];
            xmain.image().bind(holder.prevueIcon, moviePrevues.cover, ImageOptionsHelper.getDefaltImageLoaderOptions());
            holder.prevueName.setText(moviePrevues.title);
        }

        @Override
        public int getItemCount() {
            return mPrevueArray == null ? 0 : mPrevueArray.length;
        }

        public MovieDetail.MoviePrevue getItem(int position) {
            if (mPrevueArray == null || position < 0 || position >= mPrevueArray.length) {
                return null;
            }
            return mPrevueArray[position];
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PrevueViewHolder> {

        private Context mContext;

        private MovieDetail.MovieAllSizePhoto[] mPhotoArray;

        public PhotoAdapter(Context context) {
            mContext = context;
        }

        public void setData(MovieDetail.MovieAllSizePhoto[] photoArray) {
            mPhotoArray = photoArray;
            notifyDataSetChanged();
        }

        public MovieDetail.MovieAllSizePhoto[] getData() {
            return mPhotoArray;
        }

        @Override
        public PrevueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_detail_prevue_item, parent, false);
            return new PrevueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PrevueViewHolder holder, int position) {
            if (mPhotoArray == null || position < 0 || position >= mPhotoArray.length) {
                return;
            }
            MovieDetail.MovieAllSizePhoto photo = mPhotoArray[position];
            xmain.image().bind(holder.prevueIcon, photo.middle, ImageOptionsHelper.getDefaltImageLoaderOptions());
            holder.prevueName.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return mPhotoArray == null ? 0 : mPhotoArray.length;
        }
    }
}
