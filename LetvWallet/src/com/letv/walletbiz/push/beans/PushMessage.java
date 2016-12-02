package com.letv.walletbiz.push.beans;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by lijujying on 16-7-12.
 */
public class PushMessage implements LetvBaseBean {
    public final static int DEFAULT_ACTION = 0;
    public final static String DEFAULT_ICON_URI = "default";
    public final static int ACTION_OPEN_APP = 0; //打开app
    public final static int ACTION_OPEN_WEB = 1; //打开网页
    public final static int ACTION_OPEN_ACTIVITY = 2; //打开应用内页
    public final static int ACTION_CUSTOM = 3; //自定义行为

    public final static int CUSTOM_SHOW_SUBCRIPT = 1;

    private String msgId;
    private int action;
    private String webUri;
    private String intentUri;
    private String intentVal;
    private String customVal;
    private String title;
    private String content;
    private String iconUri;
    private Bitmap bitmap;

    public PushMessage(String msgId, int action, String webUri, String intentVal, String customVal, String title, String content, String iconUri, String intentUri) {
        this.msgId = msgId;
        this.action = action;
        this.webUri = webUri;
        this.intentVal = intentVal;
        this.customVal = customVal;
        this.title = title;
        this.content = content;
        this.iconUri = iconUri;
        this.intentUri = intentUri;
        if (TextUtils.isEmpty(iconUri)) {
            this.iconUri = DEFAULT_ICON_URI;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("msgId: ");
        sb.append(msgId);
        sb.append(" ");
        sb.append("action: ");
        sb.append(action);
        sb.append(" ");
        sb.append("webUri: ");
        sb.append(webUri);
        sb.append(" ");
        sb.append("intentVal: ");
        sb.append(intentVal);
        sb.append(" ");
        sb.append("customVal: ");
        sb.append(customVal);
        sb.append(" ");
        sb.append("title: ");
        sb.append(title);
        sb.append(" ");
        sb.append("content: ");
        sb.append(content);
        sb.append(" ");
        sb.append("iconUri: ");
        sb.append(iconUri);
        sb.append(" ");
        sb.append("}");
        return sb.toString();
    }

    public String getMsgId() {
        return msgId;
    }

    public int getAction() {
        return action;
    }

    public String getWebUri() {
        return webUri;
    }

    public String getIntentVal() {
        return intentVal;
    }

    public String getCustomVal() {
        return customVal;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getIconUri() {
        return iconUri;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getIntentUri() {
        return intentUri;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
