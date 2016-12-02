package com.letv.leui.common.recommend.net.parse;

import android.text.TextUtils;
import com.letv.leui.common.recommend.widget.adapter.dto.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dupengtao on 14-11-19.
 */
public class DefaultParserUtils {

    public static RecommendAllDTO parserRecommendAllDTO(JSONObject js) {
        RecommendAllDTO info = new RecommendAllDTO();
        try {
            parserBaseResult(info, js);
            if (!"10000".equals(info.getErrno())) {
                return null;
            }
            JSONObject jsData = js.optJSONObject("data");
            if (jsData != null) {
                RecommendAllDataDTO dataDTO = new RecommendAllDataDTO();
                info.setData(dataDTO);
                dataDTO.setTagid(jsData.optString("tagid"));

                //taginfo
                JSONObject jsTaginfo = jsData.optJSONObject("taginfo");
                if (jsTaginfo != null) {
                    RecommendTaginfoDTO taginfoDTO = new RecommendTaginfoDTO();
                    taginfoDTO.setLeword_type_name(jsTaginfo.optString("leword_type_name"));
                    taginfoDTO.setLeword_type(jsTaginfo.optString("leword_type"));
                    taginfoDTO.setName(jsTaginfo.optString("name"));
                    dataDTO.setTaginfo(taginfoDTO);
                }

                //hotworks hotworks
                JSONArray jsHotWorks = jsData.optJSONArray("hotworks");
                if (jsHotWorks != null && jsHotWorks.length() > 0) {
                    ArrayList<RecommendHotDTOItem> recommendHotDTOItems = new ArrayList<RecommendHotDTOItem>();
                    dataDTO.setHotworks(recommendHotDTOItems);
                    parserHotWorks(recommendHotDTOItems, jsHotWorks);
                }
                //news
                //JSONArray jsNews = jsData.optJSONArray("news");
                //if (jsNews != null && jsNews.length() > 0) {
                //    ArrayList<RecommendLatestNewsDTO> newsDTOs = new ArrayList<RecommendLatestNewsDTO>();
                //    dataDTO.setNews(newsDTOs);
                //    parserNews(newsDTOs, jsNews);
                //}
                //calendar
                //JSONArray jsCalendar = jsData.optJSONArray("calendar");
                //if (jsCalendar != null && jsCalendar.length() > 0) {
                //    ArrayList<RecommendCalendarDTO> calendarDTOs = new ArrayList<RecommendCalendarDTO>();
                //    dataDTO.setCalendar(calendarDTOs);
                //    parserCalendar(calendarDTOs, jsCalendar);
                //}
                //music
                JSONArray jsMusic = jsData.optJSONArray("music");
                if (jsMusic != null && jsMusic.length() > 0) {
                    ArrayList<RecommendMusicDTO> musicDTOs = new ArrayList<RecommendMusicDTO>();
                    dataDTO.setMusic(musicDTOs);
                    parserMusic(musicDTOs, jsMusic);
                }

                //album
                JSONArray jsAlbum = jsData.optJSONArray("album");
                if (jsAlbum != null && jsAlbum.length() > 0) {
                    ArrayList<RecommendAlbumDTO> albumDTOs = new ArrayList<RecommendAlbumDTO>();
                    parserAlbum(albumDTOs, jsAlbum);
                    dataDTO.setAlbum(albumDTOs);
                }

                //wallpaper
                JSONArray jsWallpaper = jsData.optJSONArray("wallpaper");
                if (jsWallpaper != null && jsWallpaper.length() > 0) {
                    ArrayList<RecommendWallpaperDTO> wallpaperDTOs = new ArrayList<RecommendWallpaperDTO>();
                    dataDTO.setWallpaper(wallpaperDTOs);
                    parserWallpaper(wallpaperDTOs, jsWallpaper);
                }

                //video
                JSONArray jsVideo = jsData.optJSONArray("video");
                if (jsVideo != null && jsVideo.length() > 0) {
                    ArrayList<RecommendVideoDTO> videoDTOs = new ArrayList<RecommendVideoDTO>();
                    dataDTO.setVideo(videoDTOs);
                    parserVideo(videoDTOs, jsVideo);
                }

                //weiBo
                JSONObject jsTweet = jsData.optJSONObject("tweet");
                if (jsTweet != null && jsTweet.length() > 0) {
                    JSONObject starinfo = jsTweet.optJSONObject("starinfo");
                    RecommendTweetDTO tweetDTO = new RecommendTweetDTO();
                    if (starinfo != null && starinfo.length() > 0) {
                        RecommendTweetDTO.RecommendWeiboStarInfo starInfoDTO = new RecommendTweetDTO.RecommendWeiboStarInfo();
                        starInfoDTO.setId(starinfo.optLong("id"));
                        starInfoDTO.setScreen_name(starinfo.optString("screen_name"));
                        starInfoDTO.setAvatar_hd(starinfo.optString("avatar_hd"));
                        starInfoDTO.setAvatar_large(starinfo.optString("avatar_large"));
                        starInfoDTO.setName(starinfo.optString("name"));
                        tweetDTO.setStarinfo(starInfoDTO);
                    }
                    dataDTO.setTweetData(tweetDTO);
                }

                //artists
                JSONArray jsArtists = jsData.optJSONArray("artists");
                if (jsArtists != null && jsArtists.length() > 0) {
                    ArrayList<RecommendArtistsDTO> artistsDTOs = new ArrayList<>();
                    parserArtists(artistsDTOs, jsArtists);
                    dataDTO.setArtists(artistsDTOs);
                }

                //sites
                JSONArray jsSites = jsData.optJSONArray("sites");
                if (jsSites != null && jsSites.length() > 0) {
                    ArrayList<RecommendSiteDTO> sitesDTOs = new ArrayList<>();
                    parserSites(sitesDTOs, jsSites);
                    dataDTO.setSites(sitesDTOs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return info;
    }

    private static void parserSites(ArrayList<RecommendSiteDTO> sitesDTOs, JSONArray jsSites) {
        for (int i = 0; i < jsSites.length(); i++) {
            JSONObject jsonObject = jsSites.optJSONObject(i);
            RecommendSiteDTO item = new RecommendSiteDTO();
            item.setName(jsonObject.optString("name"));
            item.setPic(jsonObject.optString("pic"));
            item.setLink(jsonObject.optString("link"));
            sitesDTOs.add(item);
        }
    }

    private static void parserArtists(ArrayList<RecommendArtistsDTO> artistsDTOs, JSONArray jsArtists) {
        for (int i = 0; i < jsArtists.length(); i++) {
            RecommendArtistsDTO item = new RecommendArtistsDTO();
            JSONObject jsonObject = jsArtists.optJSONObject(i);
            item.setTag_id(jsonObject.optInt("tag_id"));
            item.setName(jsonObject.optString("name"));
            item.setLogo(jsonObject.optString("logo"));
            artistsDTOs.add(item);
        }
    }

    private static void parserAlbum(ArrayList<RecommendAlbumDTO> albumDTOs, JSONArray jsAlbum) {
        for (int i = 0; i < jsAlbum.length(); i++) {
            RecommendAlbumDTO item = new RecommendAlbumDTO();
            JSONObject jsonObject = jsAlbum.optJSONObject(i);
            item.setId(jsonObject.optInt("id"));
            item.setScore(jsonObject.optInt("score"));
            item.setType(jsonObject.optString("type"));
            JSONObject jsAlbumContent = jsonObject.optJSONObject("content");
            if (jsAlbumContent != null) {
                RecommendAlbumDTOContent content = new RecommendAlbumDTOContent();
                parserAlbumComtent(content, jsAlbumContent);
                item.setContent(content);
            }
            albumDTOs.add(item);
        }
    }

    private static void parserAlbumComtent(RecommendAlbumDTOContent content, JSONObject jsAlbumContent) {
        content.setAlbum_id(jsAlbumContent.optInt("album_id"));
        content.setAlbum_name(jsAlbumContent.optString("album_name"));
        content.setArtist_id(jsAlbumContent.optInt("artist_id"));
        content.setArtist_name(jsAlbumContent.optString("artist_name"));
        content.setCompany(jsAlbumContent.optString("company"));
        content.setLanguage(jsAlbumContent.optString("language"));
        content.setLogo(jsAlbumContent.optString("logo"));
    }

    private static void parserTweet(ArrayList<RecommendWeiBoDTO> info, JSONArray array) {
//        for (int i = 0, j = array.length(); i < j; i++) {
//            RecommendWeiBoDTO item = new RecommendWeiBoDTO();
//            JSONObject jsListItem = array.optJSONObject(i);
//            item.setRecommendId(jsListItem.optString("recommendId"));
//            item.setId(jsListItem.optString("id"));
//            item.setType(jsListItem.optString("type"));
//            JSONObject jsTweetContent = jsListItem.optJSONObject("content");
//            if (jsTweetContent != null) {
//                RecommendWeiBoDTOContent itemContent = new RecommendWeiBoDTOContent();
//                item.setContent(itemContent);
//                parserWeiBoItemContent(itemContent, jsTweetContent);
//            }
//            info.add(item);
//        }
    }

    private static void parserWeiBoItemContent(RecommendWeiBoDTOContent item, JSONObject js) {
        item.setDate(js.optString("date"));
        item.setSource_type(js.optString("source_type"));

        JSONArray content_imgs = js.optJSONArray("content_imgs");
        if (content_imgs != null && content_imgs.length() > 0) {
            item.setContent_imgs(content_imgs.optString(0));
        }
        item.setTitle(js.optString("title"));
    }

    private static void parserVideo(ArrayList<RecommendVideoDTO> info, JSONArray array) {
        for (int i = 0, j = array.length(); i < j; i++) {
            RecommendVideoDTO item = new RecommendVideoDTO();
            JSONObject jsListItem = array.optJSONObject(i);
            item.setRecommendId(jsListItem.optString("recommendId"));
            item.setId(jsListItem.optString("id"));
            item.setType(jsListItem.optString("type"));
            JSONObject jsVideoContent = jsListItem.optJSONObject("content");
            if (jsVideoContent != null) {
                RecommendVideoContent itemContent = new RecommendVideoContent();
                item.setContent(itemContent);
                parserVideoItemContent(itemContent, jsVideoContent);
            }
            info.add(item);
        }
    }

    private static void parserVideoItemContent(RecommendVideoContent item, JSONObject js) {
        item.setVtype(js.optString("vtype"));
        item.setVid(js.optString("vid"));
        item.setCid(js.optString("cid"));
        item.setSub_title(js.optString("sub_title"));
        item.setIsend(js.optString("isend"));
        item.setPid(js.optString("pid"));
        item.setCid_name(js.optString("cid_name"));
        item.setPid_name(js.optString("pid_name"));
        item.setSource_type(js.optString("source_type"));
        item.setPid_pic(js.optString("pid_pic"));
        item.setDate(js.optString("date"));
        item.setVid_name(js.optString("vid_name"));
        item.setIsalbum(js.optString("isalbum"));
        item.setChannel(js.optString("channel"));
        item.setVid_pic(js.optString("vid_pic"));
    }

    private static void parserWallpaper(ArrayList<RecommendWallpaperDTO> info, JSONArray array) {
        for (int i = 0, j = array.length(); i < j; i++) {
            RecommendWallpaperDTO item = new RecommendWallpaperDTO();
            JSONObject jsListItem = array.optJSONObject(i);
            item.setRecommendId(jsListItem.optString("recommendId"));
            item.setId(jsListItem.optString("id"));
            item.setType(jsListItem.optString("type"));
            JSONObject jsWallpaperContent = jsListItem.optJSONObject("content");
            if (jsWallpaperContent != null) {
                RecommendWallpaperContent itemContent = new RecommendWallpaperContent();
                item.setContent(itemContent);
                parserWallpaperItemContent(itemContent, jsWallpaperContent);
            }
            info.add(item);
        }
    }

    private static void parserWallpaperItemContent(RecommendWallpaperContent item, JSONObject js) {
        item.setName(js.optString("name"));
        item.setUrl(js.optString("url"));
        item.setPid(js.optString("pid"));
        item.setType(js.optString("type"));
        item.setThumbnail(js.optString("thumbnail"));
        item.setSize(js.optString("size"));
    }

    private static void parserMusic(ArrayList<RecommendMusicDTO> info, JSONArray array) {
        for (int i = 0, j = array.length(); i < j; i++) {
            RecommendMusicDTO item = new RecommendMusicDTO();
            JSONObject jsListItem = array.optJSONObject(i);
            item.setRecommendId(jsListItem.optString("recommendId"));
            item.setId(jsListItem.optString("id"));
            item.setType(jsListItem.optString("type"));
            JSONObject jsMusicContent = jsListItem.optJSONObject("content");
            if (jsMusicContent != null) {
                RecommendMusicContent itemContent = new RecommendMusicContent();
                item.setContent(itemContent);
                parserMusicItemContent(itemContent, jsMusicContent);
            }
            info.add(item);
        }
    }

    private static void parserMusicItemContent(RecommendMusicContent item, JSONObject js) {
        item.setSong_id(js.optString("song_id"));
        item.setListen_file(js.optString("listen_file"));
        item.setAlbum_logo(js.optString("album_logo"));
        item.setTitle(js.optString("title"));
        item.setSong_name(js.optString("song_name"));
        item.setAlbum_id(js.optString("album_id"));
        item.setLyric_file(js.optString("lyric_file"));
        item.setFlag(js.optString("flag"));
        item.setArtist_name(js.optString("artist_name"));
        item.setPlay_seconds(js.optString("play_seconds"));
        item.setArtist_logo(js.optString("artist_logo"));
        item.setPlay_counts(js.optString("play_counts"));
        item.setLogo(js.optString("logo"));
        item.setSingers(js.optString("singers"));
        item.setLength(js.optString("length"));
        item.setAlbum_id(js.optString("artist_id"));
        item.setAlbum_name(js.optString("album_name"));
        item.setCd_serial(js.optString("cd_serial"));
        item.setName(js.optString("name"));
    }

    private static void parserCalendar(ArrayList<RecommendCalendarDTO> info, JSONArray jsonArray) {
        for (int i = 0, j = jsonArray.length(); i < j; i++) {
            RecommendCalendarDTO item = new RecommendCalendarDTO();
            JSONObject jsListItem = jsonArray.optJSONObject(i);
            item.setTime(jsListItem.optString("time"));
            item.setType(jsListItem.optString("type"));
            item.setEid(jsListItem.optString("eid"));
            item.setTitle(jsListItem.optString("title"));

            JSONObject extJb = jsListItem.optJSONObject("ext");
            if (extJb != null) {
                JSONObject jb5 = extJb.optJSONObject("5");
                if (jb5 != null) {
                    String pid = jb5.optString("pid");
                    if (!TextUtils.isEmpty(pid)) {
                        item.setPid(pid);
                    }
                }
                JSONObject jb8 = extJb.optJSONObject("8");
                if (jb8 != null) {
                    String mid = jb8.optString("mid");
                    if (!TextUtils.isEmpty(mid)) {
                        item.setMid(mid);
                    }
                }
            }


            info.add(item);
        }
    }

    private static void parserNews(ArrayList<RecommendLatestNewsDTO> info, JSONArray array) {
        for (int i = 0, j = array.length(); i < j; i++) {
            RecommendLatestNewsDTO item = new RecommendLatestNewsDTO();
            JSONObject jsListItem = array.optJSONObject(i);
            item.setScore(jsListItem.optString("score"));
            item.setRecommendId(jsListItem.optString("recommendId"));
            item.setId(jsListItem.optString("id"));
            item.setType(jsListItem.optString("type"));
            JSONObject jsNewsContent = jsListItem.optJSONObject("content");
            if (jsNewsContent != null) {
                RecommendLatestNewsDTOContent itemContent = new RecommendLatestNewsDTOContent();
                item.setContent(itemContent);
                parserNewsItemContent(itemContent, jsNewsContent);
            }
            info.add(item);
        }
    }

    private static void parserNewsItemContent(RecommendLatestNewsDTOContent itemContent, JSONObject jsNewsContent) {
        itemContent.setDate(jsNewsContent.optString("date"));
        itemContent.setSource_type(jsNewsContent.optString("source_type"));

        JSONArray content_imgs = jsNewsContent.optJSONArray("content_imgs");
        if (content_imgs != null && content_imgs.length() > 0) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0, j = content_imgs.length(); i < j; i++) {
                list.add(content_imgs.optString(i));
            }
            itemContent.setContent_imgs(list);
        }
        itemContent.setUrl(jsNewsContent.optString("url"));
        itemContent.setTitle(jsNewsContent.optString("title"));
    }

    private static void parserHotWorks(ArrayList<RecommendHotDTOItem> info, JSONArray array) {
        for (int i = 0, j = array.length(); i < j; i++) {
            RecommendHotDTOItem item = new RecommendHotDTOItem();
            JSONObject jsListItem = array.optJSONObject(i);
            item.setScore(jsListItem.optString("score"));
            item.setRecommendId(jsListItem.optString("recommendId"));
            item.setId(jsListItem.optString("id"));
            item.setType(jsListItem.optString("type"));
            JSONObject jsHotContent = jsListItem.optJSONObject("content");
            if (jsHotContent != null) {
                RecommendHotDTOItemContent itemContent = new RecommendHotDTOItemContent();
                item.setContent(itemContent);
                parserHotItemContent(itemContent, jsHotContent);
            }
            info.add(item);
        }
    }

    private static void parserHotItemContent(RecommendHotDTOItemContent item, JSONObject jsHotContent) {
        item.setVtype(jsHotContent.optString("vtype"));
        item.setTotalepisode(jsHotContent.optString("totalepisode"));
        item.setSub_title(jsHotContent.optString("sub_title"));
        item.setCid_name(jsHotContent.optString("cid_name"));
        item.setPid_name(jsHotContent.optString("pid_name"));
        item.setSource_type(jsHotContent.optString("source_type"));
        item.setNewestepisode(jsHotContent.optString("newestepisode"));
        item.setPid_pic(jsHotContent.optString("pid_pic"));
        item.setDate(jsHotContent.optString("date"));
        item.setVid_name(jsHotContent.optString("vid_name"));
        item.setIsalbum(jsHotContent.optString("isalbum"));
        item.setChannel(jsHotContent.optString("channel"));
        item.setVid_pic(jsHotContent.optString("vid_pic"));
        item.setLatest_vid(jsHotContent.optString("latest_vid"));
        item.setOldest_vid(jsHotContent.optString("oldest_vid"));
        item.setPid(jsHotContent.optString("pid"));
    }

    private static void parserBaseResult(RecommendAllDTO info, JSONObject js) {
        info.setErrno(js.optString("errno"));
        info.setErrmsg(js.optString("errmsg"));
    }

}
