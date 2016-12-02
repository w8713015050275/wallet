package com.letv.walletbiz.movie.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.reflect.TypeToken;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.wallet.common.view.BlankPage;
import com.letv.wallet.common.view.DividerGridItemDecoration;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.beans.MovieDetail;
import com.letv.walletbiz.movie.utils.MovieCommonCallback;
import com.letv.walletbiz.movie.utils.MoviePriorityExecutorHelper;

import org.xutils.common.task.PriorityExecutor;
import org.xutils.xmain;

import java.util.List;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by liuliang on 16-3-22.
 */
public class MovieStillListActivity extends BaseWalletFragmentActivity {

    private long mMovieId = -1;

    private RecyclerView mRecyclerView;
    private StillListAdapter mAdapter;

    private List<MovieDetail.MovieAllSizePhoto> mPhotoList;

    private PriorityExecutor mExecutor;
    private MovieStillListRunnable mStillListRunnable;

    private MovieCommonCallback<List<MovieDetail.MovieAllSizePhoto>> mCallback = new MovieCommonCallback<List<MovieDetail.MovieAllSizePhoto>>() {

        @Override
        public void onLoadFinished(List<MovieDetail.MovieAllSizePhoto> result, int errorCode) {
            Message msg = mHandler.obtainMessage(MSG_LOAD_FINISHED);
            msg.obj = result;
            msg.arg1 = errorCode;
            msg.sendToTarget();
        }
    };

    private static final int MSG_LOAD_FINISHED = 1;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_FINISHED:
                    mStillListRunnable = null;
                    hideLoadingView();
                    mPhotoList = (List<MovieDetail.MovieAllSizePhoto>) msg.obj;
                    if (msg.arg1 == MovieCommonCallback.NO_ERROR) {
                        if (mAdapter != null) {
                            mAdapter.setData(mPhotoList);
                        }
                    } else if (msg.arg1 == MovieCommonCallback.ERROR_NETWORK) {
                        showBlankPage().setPageState(BlankPage.STATE_NETWORK_ABNORMAL, mRetryClickListener);
                    }
                    break;
            }
        }
    };

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            loadData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        setTitle(R.string.movie_detail_photo);
        mRecyclerView = new RecyclerView(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(this, Color.WHITE, (int) DensityUtils.dip2px(1)));
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                List<MovieDetail.MovieAllSizePhoto> data = mAdapter.getData();
                if (data != null) {
                    Intent intent = new Intent(MovieStillListActivity.this, MoviePhotoGalleryActivity.class);
                    intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_PHOTO_INDEX, position);
                    intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_PHOTO_ARRAY, data.toArray(new MovieDetail.MovieAllSizePhoto[0]));
                    startActivity(intent);
                    MovieDetail.MovieAllSizePhoto phone = data.get(position);
                    String url = "";
                    if (phone != null) {
                        url = phone.middle;
                    }
                    Action.uploadMoviePhotoClick(Action.MOVIE_MOVIE_STILLS_CLICK, String.valueOf(mMovieId), url);
                }
            }
        }));
        mAdapter = new StillListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        setContentView(mRecyclerView);

        Intent intent = getIntent();
        if (intent != null) {
            mMovieId = intent.getLongExtra(MovieTicketConstant.EXTRA_MOVIE_ID, -1);
        }

        if (mMovieId == -1) {
            finish();
        }

        mExecutor = MoviePriorityExecutorHelper.getPriorityExecutor();
        loadData();
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (mPhotoList == null && isNetworkAvailable) {
            loadData();
        }
    }

    private void loadData() {
        if (isNetworkAvailable()) {
            if (mStillListRunnable == null) {
                showLoadingView();
                mStillListRunnable = new MovieStillListRunnable(this, mMovieId, mCallback);
                mExecutor.execute(mStillListRunnable);
            }
        } else {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
    }

    private class StillListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context mContext;

        private List<MovieDetail.MovieAllSizePhoto> mData;

        public StillListAdapter(Context context) {
            mContext = context;
        }

        public void setData(List<MovieDetail.MovieAllSizePhoto> list) {
            mData = list;
            notifyDataSetChanged();
        }

        public List<MovieDetail.MovieAllSizePhoto> getData() {
            return mData;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.movie_still_list_item, parent, false);
            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(imageView) {};
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (mData == null || position < 0 || position >= mData.size()) {
                return;
            }
            ImageView imageView = (ImageView) holder.itemView;
            xmain.image().bind(imageView, mData.get(position).middle);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }
    }

    private class MovieStillListRunnable implements Runnable {

        private Context mContext;
        private long mMovieId;
        private MovieCommonCallback<List<MovieDetail.MovieAllSizePhoto>> mCallback;
        private int mErrorcode = MovieCommonCallback.NO_ERROR;

        public MovieStillListRunnable(Context context, long movieId, MovieCommonCallback<List<MovieDetail.MovieAllSizePhoto>> callback) {
            mContext = context;
            mMovieId = movieId;
            mCallback = callback;
        }

        @Override
        public void run() {
            List<MovieDetail.MovieAllSizePhoto> response = getMovieStillList();
            if (mCallback != null) {
                mCallback.onLoadFinished(response, mErrorcode);
            }
        }

        private List<MovieDetail.MovieAllSizePhoto> getMovieStillList() {
            if (mContext == null || mMovieId < 0) {
                mErrorcode = MovieCommonCallback.ERROR_PARAM;
                return null;
            }
            if (!NetworkHelper.isNetworkAvailable()) {
                mErrorcode = MovieCommonCallback.ERROR_NO_NETWORK;
                return null;
            }
            BaseRequestParams params = new BaseRequestParams(MovieTicketConstant.MOVIE_PATH_STILL);
            params.addParameter(MovieTicketConstant.MOVIE_PARAM_MOVIE_ID, mMovieId);
            String responseJSON;
            try {
                responseJSON = xmain.http().getSync(params, String.class);
            } catch (Throwable throwable) {
                mErrorcode = MovieCommonCallback.ERROR_NETWORK;
                responseJSON = null;
            }
            if (TextUtils.isEmpty(responseJSON)) {
                mErrorcode = MovieCommonCallback.ERROR_NETWORK;
                return null;
            }
            TypeToken typeToken = new TypeToken<BaseResponse<List<MovieDetail.MovieAllSizePhoto>>>() {};
            BaseResponse<List<MovieDetail.MovieAllSizePhoto>> response = ParseHelper.parseByGson(responseJSON, typeToken.getType());
            if (response == null) {
                mErrorcode = MovieCommonCallback.ERROR_PARSE_JSON;
                return null;
            }
            if (response.errno != 10000) {
                mErrorcode = MovieCommonCallback.ERROR_NETWORK;
                return null;
            }
            return response.data;
        }
    }
}
