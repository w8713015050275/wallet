package com.letv.wallet.common.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.letv.wallet.common.R;

;

/**
 * Created by liuliang on 16-3-25.
 */
public class BlankPage extends com.letv.leui.support.widget.LeEmptyView{

    public static final int STATE_INIT = 0;
    public static final int STATE_CUSTOM = 1;
    public static final int STATE_NO_NETWORK = 2;
    public static final int STATE_NETWORK_ABNORMAL = 3; // 网络异常
    public static final int STATE_NO_LOGIN = 4;
    public static final int STATE_DATA_EXCEPTION = 5;

    private int mCurrentState = STATE_INIT;
    private Drawable icon;

    private int walletPrimaryColor;

    public BlankPage(Context context) {
        this(context, null);
    }

    public BlankPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlankPage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources.Theme theme = context.getTheme();

        TypedArray array = theme.obtainStyledAttributes(new int[]{R.attr.walletPrimaryColor});
        walletPrimaryColor = array.getColor(0, context.getColor(R.color.wallet_common_primary_color));
    }

    /**
     * @param state
     * @param iconViewListener 网络异常时, iconView处理点击刷新事件
     */
    public void setPageState(int state, View.OnClickListener iconViewListener) {
        mCurrentState = state;
        switch (state) {
            case STATE_INIT:
                //show nothing
                break;
            case STATE_NO_NETWORK:
                setEmptyViewStyle(com.le.eui.support.widget.R.style.LeEmptyView_NoNetwork);
                break;
            case STATE_NETWORK_ABNORMAL:
                setEmptyViewStyle(com.le.eui.support.widget.R.style.LeEmptyView_NetworkAbnormal);
                break;
            case STATE_DATA_EXCEPTION:
                setEmptyViewStyle(com.le.eui.support.widget.R.style.LeEmptyView_NoData);
                break;
            case STATE_NO_LOGIN:
                setEmptyViewStyle(R.style.Blankpage_NoLogin);
                break;
        }
        getIconView().setOnClickListener(iconViewListener);
        setButtonColor(walletPrimaryColor);
    }

    /*public void setCustomPage(String description) {
        setCustomPage(description, null);
    }
*/
    public void setCustomPage(String description, Icon icon) {
        if (!TextUtils.isEmpty(description) || icon != null) {
            setEmptyViewStyle(0);
            setIcon(icon);
            setDescription(description);

            mCurrentState = STATE_CUSTOM;
        }
    }

    private void setIcon(Icon icon) {
        this.icon = icon == null ? null : getContext().getDrawable(icon.getIconResId());
        setIcon(this.icon);
    }

    /** 公共图标:
     * NETWORK_EXCEPTION_RELOAD 网络异常重新加载
     * NO_ACCESS    无通知访问权限 暂未购买任何国际套餐 暂无内容 暂无相关讨论 暂无交易记录 数据异常
     * NO_ATTENTION   暂无关注
     * NO_CLOCK       暂无闹钟
     * NO_CONTENT     暂无内容
     * NO_COLLECT     暂无收藏
     * NO_CONVERSATION    暂无会话
     * NO_DATA    暂无数据
     * NO_DOWNLOAD    暂无下载内容
     * NO_EMAIL   暂无邮件
     * NO_EVENT   暂无事件
     * NO_FILE     无账号 无无黑名单 暂无文件 文件夹为空 暂无信纸 暂无下载文档
     * NO_HISTORY 暂无记录或暂无历史记录
     * NO_HISTORY_ORDER   暂无历史订单
     * NO_LOGIN_OR_CONTACTS   暂无联系人或尚未登陆
     * NO_MUSIC   暂无音乐
     * NO_NET   无网络连接
     * NO_PIC    暂无图片或壁纸
     * NO_PRINTER 未安装服务请到应用商店下载打印服务
     * NO_RECORD  暂无录音
     * NO_RESULT  未找到搜索结果
     * NO_THEME   此主题已下架
     * NO_VIDEO   暂无视频
     */
    public enum Icon {
        NETWORK_EXCEPTION_RELOAD(com.le.eui.support.widget.R.drawable.le_ic_prompt_network_exception_reload) {},
        NO_ACCESS(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_access) {},
        NO_ATTENTION(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_attention) {},
        NO_CLOCK(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_clock) {},
        NO_CONTENT(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_content) {},
        NO_COLLECT(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_collect) {},
        NO_CONVERSATION(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_conversation) {},
        NO_DATA(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_data) {},
        NO_DOWNLOAD(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_download) {},
        NO_EMAIL(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_email) {},
        NO_EVENT(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_event) {},
        NO_FILE(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_file) {},
        NO_HISTORY(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_history) {},
        NO_HISTORY_ORDER(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_history_order) {},
        NO_LOGIN_OR_CONTACTS(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_login_or_contacts) {},
        NO_MUSIC(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_music) {},
        NO_NET(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_net) {},
        NO_PIC(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_pic) {},
        NO_PRINTER(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_printer) {},
        NO_RECORD(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_record) {},
        NO_RESULT(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_result) {},
        NO_THEME(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_theme) {},
        NO_VIDEO(com.le.eui.support.widget.R.drawable.le_ic_prompt_no_video) {};

        private final int iconResId;

        Icon(int iconResId) {
            this.iconResId = iconResId;
        }

        public int getIconResId() {
            return iconResId;
        }
    }


}
