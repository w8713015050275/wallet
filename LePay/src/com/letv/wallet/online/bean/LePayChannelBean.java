package com.letv.wallet.online.bean;

import android.text.TextUtils;

import com.letv.wallet.common.http.beans.LetvBaseBean;
import com.letv.wallet.online.LePayConstants;

import org.w3c.dom.Text;

/**
 * Created by changjiajie on 17-1-9.
 */

public class LePayChannelBean implements LetvBaseBean {

    protected String icon;
    protected String name;
    protected String title;
    protected String sub_title;
    protected String[] tags;
    protected int sourcing;
    protected int active;
    protected int channel_status;
    protected String active_link;
    protected String credit_limit;
    protected String used_limit;
    protected String available_limit;

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return sub_title;
    }

    public String[] getTags() {
        return tags;
    }

    public int getSourcing() {
        return sourcing;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getActive() {
        return active;
    }

    public int getChannelStatus() {
        return channel_status;
    }

    public void setActiveLink(String activeLink) {
        this.active_link = activeLink;
    }

    public String getActiveLink() {
        return active_link;
    }

    public String getCreditLimit() {
        return credit_limit;
    }

    public String getUsedLimit() {
        return used_limit;
    }

    public String getAvailableLimit() {
        return available_limit;
    }

    public int getChannelId() {
        int channelId = LePayConstants.PAY_CHANNEL.CHANNEL_Π;
        if (!TextUtils.isEmpty(getName())) {
            if (getName().equals(LePayConstants.PAY_CHANNEL.CHANNEL_Π_NAME)) {
                channelId = LePayConstants.PAY_CHANNEL.CHANNEL_Π;
            } else if (getName().equals(LePayConstants.PAY_CHANNEL.CHANNEL_ONLINE_BANK_NAME)) {
                channelId = LePayConstants.PAY_CHANNEL.CHANNEL_ONLINE_BANK;
            } else if (getName().equals(LePayConstants.PAY_CHANNEL.CHANNEL_QPAY_NAME)) {
                channelId = LePayConstants.PAY_CHANNEL.CHANNEL_QPAY;
            } else if (getName().equals(LePayConstants.PAY_CHANNEL.CHANNEL_WXPAY_NAME)) {
                channelId = LePayConstants.PAY_CHANNEL.CHANNEL_WXPAY;
            } else if (getName().equals(LePayConstants.PAY_CHANNEL.CHANNEL_ALIPAY_NAME)) {
                channelId = LePayConstants.PAY_CHANNEL.CHANNEL_ALIPAY;
            }
        }
        return channelId;
    }
}
