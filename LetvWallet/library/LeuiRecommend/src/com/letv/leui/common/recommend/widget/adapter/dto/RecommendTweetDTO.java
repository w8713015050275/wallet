package com.letv.leui.common.recommend.widget.adapter.dto;

/**
 * Created by dupengtao on 15-1-26.
 */
public class RecommendTweetDTO {

    private RecommendWeiboStarInfo starinfo;
    private RecommendWeiboTwitter twitter;

    public RecommendWeiboStarInfo getStarinfo() {
        return starinfo;
    }

    public void setStarinfo(RecommendWeiboStarInfo starinfo) {
        this.starinfo = starinfo;
    }

    public RecommendWeiboTwitter getTwitter() {
        return twitter;
    }

    public void setTwitter(RecommendWeiboTwitter twitter) {
        this.twitter = twitter;
    }

    public static class RecommendWeiboStarInfo{
        private long id;
        private String screen_name;
        private String avatar_hd;
        private String avatar_large;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getScreen_name() {
            return screen_name;
        }

        public void setScreen_name(String screen_name) {
            this.screen_name = screen_name;
        }

        public String getAvatar_hd() {
            return avatar_hd;
        }

        public void setAvatar_hd(String avatar_hd) {
            this.avatar_hd = avatar_hd;
        }

        public String getAvatar_large() {
            return avatar_large;
        }

        public void setAvatar_large(String avatar_large) {
            this.avatar_large = avatar_large;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class RecommendWeiboTwitter{

    }
}
