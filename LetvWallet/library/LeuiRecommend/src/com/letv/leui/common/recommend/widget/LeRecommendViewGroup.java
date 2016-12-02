package com.letv.leui.common.recommend.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.net.*;
import com.letv.leui.common.recommend.net.request.BaseDTORequest;
import com.letv.leui.common.recommend.report.ReportData;
import com.letv.leui.common.recommend.report.ReportDataHelper;
import com.letv.leui.common.recommend.utils.AppUtil;
import com.letv.leui.common.recommend.utils.LogHelper;
import com.letv.leui.common.recommend.utils.Uiutil;
import com.letv.leui.common.recommend.volley.AuthFailureError;
import com.letv.leui.common.recommend.volley.Response;
import com.letv.leui.common.recommend.volley.VolleyError;
import com.letv.leui.common.recommend.widget.adapter.dto.*;
import com.letv.leui.common.recommend.widget.adapter.listener.ItemClickListener;
import com.letv.leui.common.recommend.widget.adapter.listener.OnBaseItemClickListener;
import com.letv.leui.common.recommend.widget.moduleview.*;

import java.util.*;

/**
 * Created by dupengtao on 14-12-4.
 */
public class LeRecommendViewGroup extends LinearLayout {
    private static final String TAG = "LeRecommendViewGroup";

    private Context mContext;

    private LinkedHashMap<LeRecommendType, AbsLeRecommendView> mLeRecommendViewMaps =
            new LinkedHashMap<LeRecommendType, AbsLeRecommendView>();
    private Set<LeRecommendType> mExcludeTypes;
    private Map<LeRecommendType, Integer> mModulesHeightMap;
    private HashMap<LeRecommendType, OnBaseItemClickListener> mModuleItemClickListenterMap;

    private LeRecommendViewStyle viewStyle;

    public static ReportDataHelper reportDataHelper;

    public LeRecommendViewGroup(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public LeRecommendViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
        LogHelper.i(TAG, "VERSION_CODE---" + WidgetConstants.VERSION_CODE);
        LogHelper.i(TAG, "VERSION_DATE---" + WidgetConstants.VERSION_DATE);
        this.setOrientation(LinearLayout.VERTICAL);
        new Thread() {
            @Override
            public void run() {
                VolleyController.getInstance(mContext.getApplicationContext()).getRequestQueue();
                VolleyController.getInstance(mContext.getApplicationContext()).getImageLoader();
            }
        }.start();
    }

    private void connectNetwork(final String tagId, final ILoadStatusListener loadStatusListener) {
        if (mExcludeTypes == null) {
            mExcludeTypes = new HashSet<LeRecommendType>();
        }
        mModulesHeightMap = new LinkedHashMap<LeRecommendType, Integer>();
        String recommendListUri = null;
        try {
            recommendListUri = UriHelper.getRecommendListUri(tagId, null, null);
            LogHelper.e(TAG, "url=" + recommendListUri);
        } catch (IllegalArgumentException e) {
            if (loadStatusListener != null) {
                loadStatusListener.onError(ILoadStatusListener.ERROR_URL, e);
                loadStatusListener.onFinish(ILoadStatusListener.ERROR_URL);
            }
        }
        Response.Listener<RecommendAllDTO> listener = new Response.Listener<RecommendAllDTO>() {
            @Override
            public void onResponse(RecommendAllDTO response) {
                LogHelper.e(TAG, response.toString());
                excludeModuleByType(response);
                initItemView(tagId, response, loadStatusListener);
                setModuleItemClickListener();
                setResponseData(response, loadStatusListener);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = VolleyErrorHelper.getMessage(error);
                LogHelper.e(TAG, message);
                int statusId = ILoadStatusListener.ERROR_OTHERS;
                if (VolleyErrorHelper.ERROR_NO_INTERNET.equals(message)) {
                    statusId = ILoadStatusListener.ERROR_NO_INTERNET;
                }
                if (loadStatusListener != null) {
                    loadStatusListener.onError(statusId, new RuntimeException("error"));
                    loadStatusListener.onFinish(statusId);
                }
            }
        };
        BaseDTORequest baseDTORequest = new BaseDTORequest<RecommendAllDTO>(recommendListUri, RecommendAllDTO.class, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headMap = new HashMap<>();
                String product = Build.DEVICE;
                String buildId = Build.ID;
                String pkg = mContext.getPackageName();
                String user_agent = product + ";" + buildId + ";" + pkg;
                headMap.put("user-agent", user_agent);
                return headMap;
            }
        };
        VolleyController.getInstance(mContext).addToRequestQueue(baseDTORequest);
    }

    private void setModuleItemClickListener() {
        if (mModuleItemClickListenterMap != null && mModuleItemClickListenterMap.size() > 0) {
            for (LeRecommendType type : mModuleItemClickListenterMap.keySet()) {
                AbsLeRecommendView leRecommendView = mLeRecommendViewMaps.get(type);
                if (leRecommendView != null) {
                    leRecommendView.addItemClickListener((ItemClickListener) mModuleItemClickListenterMap.get(type));
                }
            }
            mModuleItemClickListenterMap.clear();
        }
    }

    private void excludeModuleByType(RecommendAllDTO response) {
        String type = response.getData().getTaginfo().getLeword_type_name();
        LeRecommendWordsType.excludeModuleByType(mContext, type, mExcludeTypes);
    }

    private void initItemView(String tagId, RecommendAllDTO response, ILoadStatusListener loadStatusListener) {

        //hotWorkList
        ArrayList<RecommendHotDTOItem> hotWorkList = response.getData().getHotworks();
        if (hotWorkList != null && hotWorkList.size() > 0) {
            int height = (int) Uiutil.dipToPixels(mContext, 0);
            mModulesHeightMap.put(LeRecommendType.HOT_PRODUCT, height);
            mLeRecommendViewMaps.put(LeRecommendType.HOT_PRODUCT, new LeRecommendView(mContext));
        }

        //artists
        ArrayList<RecommendArtistsDTO> artistsList = response.getData().getArtists();
        if (artistsList != null && artistsList.size() > 0) {
            int height = (int) Uiutil.dipToPixels(mContext, 0);
            mModulesHeightMap.put(LeRecommendType.ARTISTS, height);
            final LeRecommendView leRecommendView = new LeRecommendView(mContext);
            mLeRecommendViewMaps.put(LeRecommendType.ARTISTS, leRecommendView);
        }

        //wallpaper
        ArrayList<RecommendWallpaperDTO> wallpaperList = response.getData().getWallpaper();
        if (wallpaperList != null && wallpaperList.size() > 0) {
            int height = (int) Uiutil.dipToPixels(mContext, 0);
            mModulesHeightMap.put(LeRecommendType.WALLPAPER, height);
            final LeRecommendView leRecommendView = new LeRecommendView(mContext);
            mLeRecommendViewMaps.put(LeRecommendType.WALLPAPER, leRecommendView);
        }

        //music
        ArrayList<RecommendMusicDTO> musicList = response.getData().getMusic();
        if (musicList != null && musicList.size() > 0) {
            int height = (int) Uiutil.dipToPixels(mContext, 0);
            mModulesHeightMap.put(LeRecommendType.MUSIC, height);
            int size = musicList.size();
            if (size > 5) {
                mLeRecommendViewMaps.put(LeRecommendType.MUSIC, new LeRecommendView(mContext));
            } else {
                mLeRecommendViewMaps.put(LeRecommendType.MUSIC, new LeRecommendVerticalMusicView(mContext, size));
            }
        }

        //album
        ArrayList<RecommendAlbumDTO> albumList = response.getData().getAlbum();
        if (albumList != null && albumList.size() > 0) {
            int height = (int) Uiutil.dipToPixels(mContext, 0);
            mModulesHeightMap.put(LeRecommendType.ALBUM, height);
            mLeRecommendViewMaps.put(LeRecommendType.ALBUM, new LeRecommendView(mContext));
        }

        //video
        ArrayList<RecommendVideoDTO> videoList = response.getData().getVideo();
        if (videoList != null && videoList.size() > 0) {
            int height = (int) Uiutil.dipToPixels(mContext, 0);
            mModulesHeightMap.put(LeRecommendType.LATEST_NEWS, height);
            mLeRecommendViewMaps.put(LeRecommendType.LATEST_NEWS, new LeRecommendView(mContext));
        }

        //weiBo
//        ArrayList<RecommendWeiBoDTO> weiBoList = response.getData().getTweet();
        RecommendTweetDTO tweet = response.getData().getTweetData();
        RecommendTweetDTO.RecommendWeiboStarInfo starinfo = tweet.getStarinfo();
        if (starinfo != null) {
            int count = 1;
            mLeRecommendViewMaps.put(LeRecommendType.WEI_BO, new LeRecommendWeiboView(mContext, count));
        }

        //TODO sites
        ArrayList<RecommendSiteDTO> sites = response.getData().getSites();
        if (sites != null && sites.size() > 0) {
            mLeRecommendViewMaps.put(LeRecommendType.SITES, new LeRecommendSitesView(mContext, sites.size()));
        }
    }

    private void setResponseData(RecommendAllDTO response, ILoadStatusListener loadStatusListener) {
        //get lable name according to data type
        String[] labelArray = mContext.getResources().getStringArray(getLabelNameResource(response));

        if (reportDataHelper != null) {
            reportDataHelper.setTagId(response.getData().getTagid());
        }

        boolean isDataEmpty = true;
        for (Map.Entry<LeRecommendType, AbsLeRecommendView> viewEntry : mLeRecommendViewMaps.entrySet()) {

            LeRecommendType key = viewEntry.getKey();
            boolean isExclude = mExcludeTypes.contains(key);
            if (isExclude) {
                continue;
            }

            AbsLeRecommendView value = viewEntry.getValue();
            this.addView(value);

            value.setTagInfo(response.getData().getTaginfo());
            value.setTagId(response.getData().getTagid());

            if (key == LeRecommendType.HOT_PRODUCT) {
                ArrayList<RecommendHotDTOItem> hotWorkList = response.getData().getHotworks();
                if (hotWorkList != null && hotWorkList.size() > 0) {
                    value.setRecommendType(key, hotWorkList, viewStyle);
                    value.setLabelNameText(labelArray[0]);
                    value.setLabelActionBoxVisible(View.VISIBLE);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.LATEST_NEWS) {
                ArrayList<RecommendVideoDTO> videoList = response.getData().getVideo();
                if (videoList != null && videoList.size() > 0) {
                    value.setRecommendType(key, videoList, viewStyle);
                    value.setLabelNameText(labelArray[1]);
                    value.setLabelActionBoxVisible(View.GONE);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.WALLPAPER) {
                ArrayList<RecommendWallpaperDTO> wallpaperList = response.getData().getWallpaper();
                if (wallpaperList != null && wallpaperList.size() > 0) {
                    value.setRecommendType(key, wallpaperList, viewStyle);
                    value.setLabelNameText(labelArray[2]);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.MUSIC) {
                ArrayList<RecommendMusicDTO> musicList = response.getData().getMusic();
                if (musicList != null && musicList.size() > 0) {
                    value.setRecommendType(key, musicList, viewStyle);
                    value.setLabelNameText(labelArray[3]);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.ALBUM) {
                ArrayList<RecommendAlbumDTO> albumList = response.getData().getAlbum();
                if (albumList != null && albumList.size() > 0) {
                    value.setRecommendType(key, albumList, viewStyle);
                    value.setLabelNameText(labelArray[4]);
                    value.setLabelActionBoxVisible(View.VISIBLE);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.ARTISTS) {
                ArrayList<RecommendArtistsDTO> artistsList = response.getData().getArtists();
                if (artistsList != null && artistsList.size() > 0) {
                    value.setRecommendType(key, artistsList, viewStyle);
                    value.setLabelNameText(labelArray[5]);
                    value.setLabelActionBoxVisible(View.GONE);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.WEI_BO) {
                RecommendTweetDTO tweet = response.getData().getTweetData();
                if (tweet != null) {
                    ArrayList<RecommendTweetDTO> list = new ArrayList<>();
                    list.add(tweet);
                    value.setRecommendType(key, list, viewStyle);
                    value.setLabelNameText(labelArray[6]);
                    value.setLabelActionBoxVisible(View.GONE);
                    isDataEmpty = false;
                }
            } else if (key == LeRecommendType.SITES) {
                ArrayList<RecommendSiteDTO> sites = response.getData().getSites();
                if (sites != null) {
                    value.setRecommendType(key, sites, viewStyle);
                    value.setLabelNameText(labelArray[7]);
                    value.setLabelActionBoxVisible(View.GONE);
                    isDataEmpty = false;
                }
            }
        }

        View blankView = new View(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Uiutil.dipToPixels(mContext, 25));
        this.addView(blankView, params);

        if (loadStatusListener != null) {
            int i = isDataEmpty ? ILoadStatusListener.SUCCESS_DATA_EMPTY : ILoadStatusListener.SUCCESS_DEFAULT;
            loadStatusListener.onSuccess(i);
            loadStatusListener.onFinish(i);
        }
    }

    private int getLabelNameResource(RecommendAllDTO response) {
        String name = response.getData().getTaginfo().getLeword_type_name();
        LogHelper.i(TAG, "type = " + name);
        int id = R.array.star;//default type
        if (LeRecommendWordsType.STAR.equals(name)) {
            id = R.array.star;
        } else if (LeRecommendWordsType.MOVIE.equals(name)) {
            id = R.array.movie;
        } else if (LeRecommendWordsType.PLAYER.equals(name)) {
            id = R.array.player;
        } else if (LeRecommendWordsType.TEAM.equals(name)) {
            id = R.array.team;
        } else if (LeRecommendWordsType.BRAND.equals(name)) {
            id = R.array.brand;
        } else if (LeRecommendWordsType.REGION.equals(name)) {
            id = R.array.region;
        }
        return id;
    }

    /**
     * One of {@link com.letv.leui.common.recommend.widget.LeRecommendType#MUSIC} <br/> or
     * {@link com.letv.leui.common.recommend.widget.LeRecommendType#WALLPAPER} <br/> or
     * {@link com.letv.leui.common.recommend.widget.LeRecommendType#LATEST_NEWS} <br/> or
     * {@link com.letv.leui.common.recommend.widget.LeRecommendType#HOT_PRODUCT}
     *
     * @return the child at the specified type .
     * @see com.letv.leui.common.recommend.widget.LeRecommendType
     */
    public RelativeLayout getRecommendViewByType(LeRecommendType recommendType) {
        return mLeRecommendViewMaps.get(recommendType);
    }

    /**
     * Returns all items of a map
     */
    public LinkedHashMap<LeRecommendType, AbsLeRecommendView> getAllRecommendViewMaps() {
        return mLeRecommendViewMaps;
    }

    /**
     * Set exclude type
     *
     * @param excludeTypes {@link com.letv.leui.common.recommend.widget.LeRecommendType} collection
     */
    public void setExcludeTypes(Set<LeRecommendType> excludeTypes) {
        mExcludeTypes = excludeTypes == null ? new HashSet<LeRecommendType>() : excludeTypes;
    }

    /**
     * Start loading the network data
     *
     * @param tagId 乐词id
     */
    public void load(String tagId) {
        load(tagId, null);
    }

    /**
     * Start loading the network data and set load callback listener
     *
     * @param tagId              乐词id
     * @param loadStatusListener Callback interface for load Method
     */
    public void load(String tagId, ILoadStatusListener loadStatusListener) {
        if (loadStatusListener != null) {
            loadStatusListener.onStart();
        }
        if (TextUtils.isEmpty(tagId)) {
            if (loadStatusListener != null) {
                loadStatusListener.onError(ILoadStatusListener.ERROR_ID_NULL, new NullPointerException("tagId is null"));
                loadStatusListener.onFinish(ILoadStatusListener.ERROR_ID_NULL);
            }
        }
        clearAllViewAndData();
        connectNetwork(tagId, loadStatusListener);
    }

    private void clearAllViewAndData() {
        this.removeAllViews();
        if (mLeRecommendViewMaps != null) {
            mLeRecommendViewMaps.clear();
        }
        if (mExcludeTypes != null) {
            mExcludeTypes.clear();
        }
        if (mModulesHeightMap != null) {
            mModulesHeightMap.clear();
        }
    }

    /**
     * Cancel request data
     */
    public void cancelRequest() {
        VolleyClient.cancelAllRequests(VolleyController.getInstance(mContext));
    }

    public LeRecommendViewStyle getViewStyle() {
        return viewStyle;
    }

    public void setViewStyle(LeRecommendViewStyle viewStyle) {
        this.viewStyle = viewStyle;
        if (LeRecommendViewStyle.WHITE == viewStyle) {
            setDividerDrawable(getResources().getDrawable(R.drawable.shape_recommend_divider_line_white));
        }
    }


    public Set<LeRecommendType> getAllInitModules() {
        return mModulesHeightMap.keySet();
    }

    public Map<LeRecommendType, Integer> getAllInitModulesHeight() {
        return mModulesHeightMap;
    }

    public ReportDataHelper initReporter(ReportData data) {
        reportDataHelper = new ReportDataHelper(data, mContext.getApplicationContext());
        return reportDataHelper;
    }

    /**
     * module item click listener
     * @param type
     * @param onItemClickListener
     */
    public void addModuleItemClickListener(LeRecommendType type, OnBaseItemClickListener onItemClickListener) {
        if (mModuleItemClickListenterMap == null) {
            mModuleItemClickListenterMap = new HashMap<>();
        } else {
            mModuleItemClickListenterMap.clear();
        }
        mModuleItemClickListenterMap.put(type, onItemClickListener);
    }

}
