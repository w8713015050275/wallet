package com.letv.leui.common.recommend.report;

import android.content.Context;
import com.letv.leui.common.recommend.widget.LeRecommendViewGroup;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendTaginfoDTO;
import com.letv.tracker2.agnes.Agnes;
import com.letv.tracker2.agnes.App;
import com.letv.tracker2.agnes.Event;
import com.letv.tracker2.agnes.Widget;
import com.letv.tracker2.enums.AppType;
import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
import com.letv.tracker2.enums.LeUIApp;

/**
 * Created by dupengtao on 15-3-12.
 */
public class ReportDataHelper implements IReportData {

    private final App app;
    private final Agnes agnes;
    private final ReportData reportData;
    private String mTagId;

    public ReportDataHelper(ReportData reportData, Context context) {
        app = reportData.getApp();
        agnes = Agnes.getInstance();
        agnes.setContext(context);
//        agnes.getConfig().enableLog();
        this.reportData = reportData;
    }

    @Override
    public void reportRecommendExposeView() {
        Widget widget = app.createWidget("R.1");
        Event e1 = widget.createEvent(EventType.Expose);
        // as this new expose event happens in the same widget, you can set from widget id as itself
        e1.addProp(Key.From, reportData.getType().name());
        // what contents have been exposed
        e1.addProp(Key.Content, mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportMusicExposeModule() {
        Widget widget = app.createWidget("R.1.5");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportHotProductExposeModule() {
        Widget widget = app.createWidget("R.1.4");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportLastNewsExposeModule() {
        Widget widget = app.createWidget("R.1.1");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportWallpaperExposeModule() {
        Widget widget = app.createWidget("R.1.6");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportMediaNewsExposeModule() {
        Widget widget = app.createWidget("R.1.3");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportCalendarExposeModule() {
        Widget widget = app.createWidget("R.1.2");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportWeiBoExposeModule() {
        Widget widget = app.createWidget("R.1.7");
        Event e1 = widget.createEvent(EventType.Expose);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }


    @Override
    public void reportHotProductJump(String id) {
        Widget widget = app.createWidget("R.1.4");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, "LetvUltimateed");
        e1.addProp(Key.Class,mTagId);
        e1.addProp(Key.Content,id);
        agnes.report(e1);
    }

    @Override
    public void reportHotProductMoreJump() {
        Widget widget = app.createWidget("R.1.4.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, "LetvUltimateed");
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportLastNewsJump(String id) {
        Widget widget = app.createWidget("R.1.1");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, "LetvUltimateed");
        e1.addProp(Key.Class,mTagId);
        e1.addProp(Key.Content,id);
        agnes.report(e1);
    }

    @Override
    public void reportLastNewsMoreJump() {
        Widget widget = app.createWidget("R.1.1.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, "LetvUltimateed");
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportWallpaperJump(String id) {
        Widget widget = app.createWidget("R.1.6");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Wallpaper.name());
        e1.addProp(Key.Content,id);
        e1.addProp(Key.Class,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportWallpaperMoreJump() {
        Widget widget = app.createWidget("R.1.6.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Wallpaper.name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportMediaNewsJump(String id) {
        Widget widget = app.createWidget("R.1.3");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Browser.name());
        e1.addProp(Key.Class,mTagId);
        e1.addProp(Key.Content,id);
        agnes.report(e1);
    }

    @Override
    public void reportCalendarJump(String id) {
        Widget widget = app.createWidget("R.1.2");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Calendar.name());
        e1.addProp(Key.Class,mTagId);
        e1.addProp(Key.Content,id);
        agnes.report(e1);
    }

    @Override
    public void reportCalendarMoreJump() {
        Widget widget = app.createWidget("R.1.2.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Calendar.name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportMusicJump(String id) {
        Widget widget = app.createWidget("R.1.5");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Music.name());
        e1.addProp(Key.Class,mTagId);
        e1.addProp(Key.Content,id);
        agnes.report(e1);
    }

    @Override
    public void reportMusicMoreJump() {
        Widget widget = app.createWidget("R.1.5.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Music.name());
        e1.addProp(Key.Content,mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportWeiBoJump(String id) {
        Widget widget = app.createWidget("R.1.7");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, AppType.Weibo.name());//
        e1.addProp(Key.Class,mTagId);
        e1.addProp(Key.Content,id);
        agnes.report(e1);
    }

    @Override
    public void reportArtistsJump() {
        Widget widget = app.createWidget("R.1.3");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, reportData.getType().name());//
        e1.addProp(Key.Class, mTagId);
        e1.addProp(Key.Content, mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportArtistsMoreJump() {
        Widget widget = app.createWidget("R.1.3.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, reportData.getType().name());//
        e1.addProp(Key.Content, mTagId);
        agnes.report(e1);
    }

    @Override
    public void reportAlbumJump(String ZJid) {
        Widget widget = app.createWidget("R.1.2");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Music.name());//
        e1.addProp(Key.Class, mTagId);
        e1.addProp(Key.Content, ZJid);
        agnes.report(e1);
    }

    @Override
    public void reportAlbumMoreJump() {
        Widget widget = app.createWidget("R.1.2.M");
        Event e1 = widget.createEvent(EventType.Jump);
        e1.addProp(Key.From, reportData.getType().name());
        e1.addProp(Key.To, LeUIApp.Music.name());//
        e1.addProp(Key.Content, mTagId);
        agnes.report(e1);
    }

    public void setTagId(String tagId) {
        mTagId = tagId;
    }

}
