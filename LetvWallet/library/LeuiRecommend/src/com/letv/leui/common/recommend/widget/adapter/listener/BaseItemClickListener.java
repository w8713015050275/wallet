package com.letv.leui.common.recommend.widget.adapter.listener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.letv.leui.common.R;
import com.letv.leui.common.recommend.report.ReportDataHelper;
import com.letv.leui.common.recommend.utils.AppUtil;
import com.letv.leui.common.recommend.widget.LeRecommendType;
import com.letv.leui.common.recommend.widget.LeRecommendViewGroup;
import com.letv.leui.common.recommend.utils.LogHelper;
import com.letv.leui.common.recommend.widget.adapter.BaseRecommendAdapter;
import com.letv.leui.common.recommend.widget.adapter.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dupengtao on 14-12-6.
 */
public class BaseItemClickListener implements OnBaseItemClickListener {
    private static final String TAG = "BaseItemClickListener";
    private ReportDataHelper reportDataHelper;
    private Context mContext;

    private LeRecommendType mCurType;
    private List<RecommendHotDTOItem> mHotDTOList;
    private ArrayList<RecommendLatestNewsDTO> mMediaNews;
    private ArrayList<RecommendWallpaperDTO> mWallpaperList;
    private ArrayList<RecommendMusicDTO> mMusicList;
    private ArrayList<RecommendVideoDTO> mVideoList;
    private ArrayList<RecommendWeiBoDTO> mTweetList;
    private RecommendTweetDTO mTweet;
    private ArrayList<RecommendCalendarDTO> mCalendarList;
    private ArrayList<RecommendAlbumDTO> mAlbumList;
    private ArrayList<RecommendArtistsDTO> mArtistsList;
    private ArrayList<RecommendSiteDTO> mSitesList;

    private RecommendTaginfoDTO mTagInfo;
    private String mTagId;

    public BaseItemClickListener(Context context, LeRecommendType curType) {
        this.mCurType = curType;
        this.mContext = context;
        if (LeRecommendViewGroup.reportDataHelper != null) {
            reportDataHelper = LeRecommendViewGroup.reportDataHelper;
        }
    }

    @Override
    public void onItemClick(View view, int position, int count) {

        if (position == BaseRecommendAdapter.MAX_ITEM_NUM) {
            onLabelActionClick(view);
            return;
        }
        if (LeRecommendType.HOT_PRODUCT == mCurType) {

//            if (!isAppInstalled(AppUtil.VIDEO_PACKAGE_NAME)) {
//                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_video));
//                return;
//            }

            String aid = null, vid = null;
            try {
                RecommendHotDTOItemContent content = mHotDTOList.get(position).getContent();
                aid = content.getPid();
                vid = content.getOldest_vid();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(vid)) {
                //String scheme = "letvclient://msiteAction?actionType=9&back=1&vid=" + vid + "&aid=" + aid;
                String scheme = jointLetvClientScheme(vid, aid);

                if (reportDataHelper != null) {
                    try {
                        reportDataHelper.reportHotProductJump(vid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(scheme));
                if (isIntentAvailable(mContext, intent)) {
                    mContext.startActivity(intent);
                } else {
                    toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_video));
                }
            }
            LogHelper.i(TAG, "onItemClick [LeRecommendType.HOT_PRODUCT] , position=" + position);
        } else if (LeRecommendType.LATEST_NEWS == mCurType) {
            LogHelper.i(TAG, "onItemClick [LeRecommendType.LATEST_NEWS] , position=" + position);

//            if (!isAppInstalled(AppUtil.VIDEO_PACKAGE_NAME)) {
//                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_video));
//                return;
//            }

            String aid = null, vid = null;
            try {
                RecommendVideoContent content = mVideoList.get(position).getContent();
                aid = content.getPid();
                vid = content.getVid();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(vid)) {

                String scheme = jointLetvClientScheme(vid, aid);

                if (reportDataHelper != null) {
                    try {
                        reportDataHelper.reportLastNewsJump(vid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(scheme));
                if (isIntentAvailable(mContext, intent)) {
                    mContext.startActivity(intent);
                } else {
                    toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_video));
                }
            }

        } else if (LeRecommendType.WALLPAPER == mCurType) {
            LogHelper.i(TAG, "onItemClick [LeRecommendType.WALLPAPER] , position=" + position);

            if (!isAppInstalled(AppUtil.WALLPAPER_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_wallpaper));
                return;
            }

            ArrayList<Integer> ids = new ArrayList<Integer>();
            for (int i = 0, j = mWallpaperList.size(); i < j; i++) {
                RecommendWallpaperDTO recommendWallpaperDTO = mWallpaperList.get(i);
                try {
                    ids.add(Integer.parseInt(recommendWallpaperDTO.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportWallpaperJump(String.valueOf(ids.get(position)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int p = position;
            Intent intent = new Intent("com.letv.android.wallpaperonline.intent.action.SHOW_WALLPAPER");
            intent.putExtra("ids", ids);
            intent.putExtra("position", p);
            if (isIntentAvailable(mContext, intent)) {
                mContext.startActivity(intent);
            } else {
                toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_wallpaper));
            }
        } else if (LeRecommendType.MUSIC == mCurType) {
            LogHelper.i(TAG, "onItemClick [LeRecommendType.MUSIC] , position=" + position);

            if (!isAppInstalled(AppUtil.MUSIC_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_music));
                return;
            }
            try {
                RecommendMusicContent content = mMusicList.get(position).getContent();
                String song_id = content.getSong_id();
                if (reportDataHelper != null) {
                    try {
                        reportDataHelper.reportMusicJump(song_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (content != null && !TextUtils.isEmpty(song_id)) {
                    LogHelper.e(TAG, "song_id---" + song_id);
                    Intent intent = new Intent("com.letv.music.view.song");
                    intent.putExtra("id", Long.parseLong(song_id)); // xiami online song id
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    if (isIntentAvailable(mContext, intent)) {
                        mContext.startActivity(intent);
                    } else {
                        toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_music));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (LeRecommendType.MEDIA_NEWS == mCurType) {
            LogHelper.i(TAG, "onItemClick [LeRecommendType.MEDIA_NEWS] , position=" + position);


            RecommendLatestNewsDTO latestNewsDTO = mMediaNews.get(position);
            //mContext.startActivity(intent);

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportLastNewsJump(latestNewsDTO.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (latestNewsDTO != null && !TextUtils.isEmpty(latestNewsDTO.getContent().getUrl())) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse(latestNewsDTO.getContent().getUrl());
                intent.setData(content_url);
                mContext.startActivity(intent);
            }

        } else if (LeRecommendType.CALENDAR == mCurType) {
            LogHelper.i(TAG, "onItemClick [LeRecommendType.CALENDAR] , position=" + position);


            if (!isAppInstalled(AppUtil.CALENDAR_PACKAGE_NAME)) {
                Toast.makeText(mContext, "app not install", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                RecommendCalendarDTO recommendCalendarDTO = mCalendarList.get(position);
                String eid = recommendCalendarDTO.getEid();
                String type = recommendCalendarDTO.getType();
                long lEid = -1;
                try {
                    lEid = Long.parseLong(eid);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (reportDataHelper != null) {
                    try {
                        reportDataHelper.reportCalendarJump(eid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!TextUtils.isEmpty(eid) && !TextUtils.isEmpty(type) && lEid != -1) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra("event_id", lEid);//事件id , long类型
                    intent.putExtra("source_from", 2);//表明是本地事件
                    ComponentName cn;
                    if ("1".equals(type)) {
                        // 体育类事件
                        cn = new ComponentName("com.android.calendar", "com.letv.calendar.ui.SubscribeItemSportsDetailActivity");
                        intent.setComponent(cn);
                        mContext.startActivity(intent);
                    } else if("2".equals(type)||"3".equals(type)) {
                        cn = new ComponentName("com.android.calendar", "com.letv.calendar.ui.SubscribeItemActivity");
                        intent.setComponent(cn);
                        mContext.startActivity(intent);
                    } else if("4".equals(type)||"5".equals(type)||"6".equals(type)){

                        String vid = recommendCalendarDTO.getMid();
                        String aid = recommendCalendarDTO.getPid();

                        if (!TextUtils.isEmpty(vid)) {
                            //String scheme = "letvclient://msiteAction?actionType=9&back=1&vid=" + vid + "&aid=" + aid;
                            String scheme;
                            if (TextUtils.isEmpty(aid) || "-".equals(aid)) {
                                scheme = "letvclient://msiteAction?actionType=9&back=1&vid=" + vid;
                            } else {
                                scheme = "letvclient://msiteAction?actionType=9&back=1&vid=" + vid + "&aid=" + aid;
                            }
                            LogHelper.e(TAG, "scheme" + scheme);
                            if (reportDataHelper != null) {
                                try {
                                    reportDataHelper.reportHotProductJump(vid);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            Intent intent1 = new Intent();
                            intent1.setAction(Intent.ACTION_VIEW);
                            intent1.setData(Uri.parse(scheme));
                            mContext.startActivity(intent1);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (LeRecommendType.WEI_BO == mCurType) {

//            RecommendWeiBoDTO recommendWeiBoDTO = mTweetList.get(0);
//            String id = recommendWeiBoDTO.getId();

            String id = mTweet.getStarinfo().getId() + "";

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportWeiBoJump(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isAppInstalled(AppUtil.WEI_PACKAGE_NAME)) {
                Intent intent = new Intent();
                //sinaweibo://detail?mblogid=XXX //XXX为微博id
                String scheme = "sinaweibo://userinfo?uid=" + id;
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse(scheme);
                intent.setData(content_url);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                if (isIntentAvailable(mContext, intent)) {
                    mContext.startActivity(intent);
                    return;
                }
            }

            Intent intent = new Intent();
            String scheme = "http://m.weibo.cn/" + id;
            intent.setAction(Intent.ACTION_VIEW);
            Uri content_url = Uri.parse(scheme);
            intent.setData(content_url);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            mContext.startActivity(intent);

        }else if (LeRecommendType.ALBUM == mCurType) {

            if (!isAppInstalled(AppUtil.MUSIC_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_music));
                return;
            }
            try {
                RecommendAlbumDTO recommendAlbumDTO = mAlbumList.get(position);
                int album_id = recommendAlbumDTO.getContent().getAlbum_id();
                if (reportDataHelper != null) {
                    try {
                        reportDataHelper.reportAlbumJump(album_id + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (album_id != 0) {
                    LogHelper.e(TAG, "album_id---" + album_id);
                    Intent intent = new Intent("com.letv.music.view.album");
                    intent.putExtra("id", (long) album_id); // xiami online album id
                    if (isIntentAvailable(mContext, intent)) {
                        mContext.startActivity(intent);
                    } else {
                        toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_music));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (LeRecommendType.ARTISTS == mCurType) {

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportArtistsJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }else if (LeRecommendType.SITES == mCurType) {
            String link = mSitesList.get(position).getLink();
            if (link != null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri url = Uri.parse(link);
                intent.setData(url);
                mContext.startActivity(intent);
            }
        }

    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfos.size() > 0;
    }

    private static void toastApplicationNotAvailable(Context context, String applicationName) {
        String prompt = context.getResources().getString(R.string.le_app_not_available);
        Toast.makeText(context, String.format(prompt, applicationName), Toast.LENGTH_SHORT).show();
    }

    private static void toastApplicationNotInstall(Context context, String applicationName) {
        String prompt = context.getResources().getString(R.string.le_app_not_install);
        Toast.makeText(context, String.format(prompt, applicationName), Toast.LENGTH_SHORT).show();
    }

    private String jointLetvClientScheme(String vid, String aid) {
        String scheme, from = "*";
        String packageName = mContext.getPackageName();
        if (packageName.equals(AppUtil.MUSIC_PACKAGE_NAME)) {
            from = "leui01";
        } else if (packageName.equals(AppUtil.CALENDAR_PACKAGE_NAME)) {
            from = "leui05";
        } else if (packageName.equals(AppUtil.WALLPAPER_PACKAGE_NAME)) {
            from = "leui09";
        }

        if (TextUtils.isEmpty(aid) || "-".equals(aid)) {
            scheme = "letvclient://msiteAction?actionType=9&back=1&vid=" + vid
                    + "&processId=" + android.os.Process.myPid()
                    + ("*".equals(from) ? "" : "&from=" + from);
        } else {
            scheme = "letvclient://msiteAction?actionType=9&back=1&vid=" + vid
                    + "&aid=" + aid
                    + "&processId=" + android.os.Process.myPid()
                    + ("*".equals(from) ? "" : "&from=" + from);
        }
        LogHelper.e(TAG, "scheme" + scheme);
        return scheme;
    }

    @Override
    public void onLabelActionClick(View view) {

        long lTagId = -1;
        int iTagId = -1;
        try {
            lTagId = Long.parseLong(mTagId);
            iTagId = Integer.parseInt(mTagId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (LeRecommendType.HOT_PRODUCT == mCurType) {
            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.HOT_PRODUCT] , more ");
            if (!isAppInstalled(AppUtil.LESO_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_video));
                return;
            }

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportHotProductMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String kw = mTagInfo.getName();
            String from = "*";
            String packageName = mContext.getPackageName();
            if (packageName.equals(AppUtil.MUSIC_PACKAGE_NAME)) {
                from = "superphone_music";
            } else if (packageName.equals(AppUtil.CALENDAR_PACKAGE_NAME)) {
                from = "superphone_calendar";
            } else if (packageName.equals(AppUtil.WALLPAPER_PACKAGE_NAME)) {
                from = "superphone_wallpaper";
            }
            String scheme = "leso://search?key_word=" + kw + "&from=" + from;
            LogHelper.i(TAG, "------->>> LESO === " + scheme);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (isIntentAvailable(mContext, intent)) {
                mContext.startActivity(intent);
            } else {
                toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_video));
            }
        } else if (LeRecommendType.LATEST_NEWS == mCurType) {
            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.LATEST_NEWS] , more ");

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportLastNewsMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            return;
            //if (!AppUtil.appInstallMap.get(AppUtil.VIDEO_PACKAGE_NAME)) {
            //    Toast.makeText(mContext, "app not install", Toast.LENGTH_SHORT).show();
            //    return;
            //}
        } else if (LeRecommendType.WALLPAPER == mCurType) {
            if (!isAppInstalled(AppUtil.WALLPAPER_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_wallpaper));
                return;
            }

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportWallpaperMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.WALLPAPER] , more ");
            Intent intent = new Intent("com.letv.android.wallpaperonline.intent.action.SHOW_ALL_WALLPAPER");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("tagId", iTagId);  //乐词id
            String tagName=mWallpaperList.get(0).getContent().getName();
            intent.putExtra("tagName", tagName); //乐词名称
            if (isIntentAvailable(mContext, intent)) {
                mContext.startActivity(intent);
            } else {
                toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_wallpaper));
            }
        } else if (LeRecommendType.MUSIC == mCurType) {

            if (!isAppInstalled(AppUtil.MUSIC_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_music));
                return;
            }

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportMusicMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.MUSIC] , more ");
            Intent intent = new Intent("com.letv.music.view.artist_song");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("id", lTagId);//2127
            if (isIntentAvailable(mContext, intent)) {
                mContext.startActivity(intent);
            } else {
                toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_music));
            }
        } else if (LeRecommendType.MEDIA_NEWS == mCurType) {
            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.MEDIA_NEWS] , more tagId=" + mTagId);
            //Intent intent = new Intent("com.android.launcher3.view.LECI_NEWS_MORE");
            //intent.putExtra("tag_id", lTagId);  //乐词id
            //mContext.startActivity(intent);
        } else if (LeRecommendType.CALENDAR == mCurType) {
            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.CALENDAR] , more tagId=" + mTagId);
            if (!isAppInstalled(AppUtil.CALENDAR_PACKAGE_NAME)) {
                Toast.makeText(mContext, "app not install", Toast.LENGTH_SHORT).show();
                return;
            }
            RecommendCalendarDTO recommendCalendarDTO = mCalendarList.get(0);
            boolean isSports = false;
            if (recommendCalendarDTO != null) {
                String type = recommendCalendarDTO.getType();
                isSports = "1".equals(type);
            }

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportCalendarMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent("com.android.calendar.RECENTLYLIST");
            intent.putExtra("isGame", isSports); //乐词是否是球队（最近动态事件类型是比赛）
            intent.putExtra("tag_id", lTagId); //乐词id
            mContext.startActivity(intent);
        } else if (LeRecommendType.ALBUM == mCurType) {

            if (!isAppInstalled(AppUtil.MUSIC_PACKAGE_NAME)) {
                toastApplicationNotInstall(mContext, mContext.getResources().getString(R.string.le_music));
                return;
            }

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportAlbumMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            LogHelper.i(TAG, "onLabelActionClick [LeRecommendType.Album] , more ");
            Intent intent = new Intent("com.letv.music.view.artist_song");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("id", lTagId); //专辑id
            intent.putExtra("pageIndex", 1);
            if (isIntentAvailable(mContext, intent)) {
                mContext.startActivity(intent);
            } else {
                toastApplicationNotAvailable(mContext, mContext.getResources().getString(R.string.le_music));
            }
        } else if (LeRecommendType.ARTISTS == mCurType) {

            if (reportDataHelper != null) {
                try {
                    reportDataHelper.reportArtistsMoreJump();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        LogHelper.e(TAG, "tag_id---" + mTagId);
    }

    private synchronized boolean isAppInstalled(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, 0);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public void setRecommendHotDTOs(List<RecommendHotDTOItem> dataSet) {
        mHotDTOList = dataSet;
    }

    public List<RecommendHotDTOItem> getHotDTOList() {
        return mHotDTOList;
    }

    public void setRecommendLatestNewsDTOs(ArrayList<RecommendLatestNewsDTO> mediaNews) {
        mMediaNews = mediaNews;
    }

    public void setWallpaperList(ArrayList<RecommendWallpaperDTO> wallpaperList) {
        this.mWallpaperList = wallpaperList;
    }

    public void setMusicList(ArrayList<RecommendMusicDTO> musicList) {
        this.mMusicList = musicList;
    }

    public void setVideoList(ArrayList<RecommendVideoDTO> videoList) {
        this.mVideoList = videoList;
    }

    public void setTweetList(ArrayList<RecommendWeiBoDTO> tweetList) {
        this.mTweetList = tweetList;
    }

    public void setTweetData(ArrayList<RecommendTweetDTO> tweetList) {
        this.mTweet = tweetList.get(0);
    }

    public void setCalendarList(ArrayList<RecommendCalendarDTO> calendarList) {
        this.mCalendarList = calendarList;
    }

    public void setAlbumList(ArrayList albumList) {
        this.mAlbumList = albumList;
    }

    public void setArtistsList(ArrayList artistsList) {
        this.mArtistsList = artistsList;
    }

    public void setSitesList(ArrayList<RecommendSiteDTO> sitesList) {
        mSitesList = sitesList;
    }

    public void setTagInfo(RecommendTaginfoDTO tagInfo) {
        mTagInfo = tagInfo;
    }

    public void setTagId(String tagId) {
        mTagId = tagId;
    }
}
