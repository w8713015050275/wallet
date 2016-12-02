package com.letv.walletbiz.update.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by zhangzhiwei1 on 16-8-8.
 */
public class RemoteAppInfo implements LetvBaseBean{

    private int upgradeType;

    private String apkVersion;

    private String description;

    private String fileUrl;

    private String packageName;

    private boolean isDownloaded;

    private String applicationName;


    public void setUpgradeType(int upgradeType) {
        this.upgradeType = upgradeType;
    }

    public int getUpgradeType() {
        return upgradeType;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setIsDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public boolean getIsDownloaded() {
        return isDownloaded;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }
}

