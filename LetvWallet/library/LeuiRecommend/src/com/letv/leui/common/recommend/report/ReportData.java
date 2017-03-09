package com.letv.leui.common.recommend.report;

import com.letv.tracker2.agnes.App;
import com.letv.tracker2.enums.LeUIApp;

/**
 * Created by dupengtao on 15-3-12.
 */
public class ReportData {

    private LeUIApp type;
    private App app;

    public ReportData(LeUIApp type, App app) {
        this.type = type;
        this.app = app;
    }

    public LeUIApp getType() {
        return type;
    }

    public void setType(LeUIApp type) {
        this.type = type;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
