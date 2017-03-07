package com.letv.wallet.common.util;

import android.text.TextUtils;

import com.letv.domain.utils.LeDomainManager;
import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.http.client.RspConstants;

import java.util.Map;

/**
 * Created by linquan on 16-5-12.
 * wiki:http://wiki.letv.cn/pages/viewpage.action?pageId=53443344
 */
public class DomainHelper {

    public final static String TAG = "DomainHelper";
    private static DomainHelper instance;
    private static String mHost;

    static {
        instance = new DomainHelper();
    }

    public static DomainHelper getInstance() {
        return instance;
    }

    public synchronized String getHost() {
        if (TextUtils.isEmpty(mHost)) {
            mHost = getDomain();
        }
        return mHost;
    }

    private String getDomain() {
        LeDomainManager leDomainManager = new LeDomainManager(
                BaseApplication.getApplication().getContentResolver());
        Map<String, String> map =
                leDomainManager.getDomain(null, new String[]{RspConstants.DOMAIN_LABEL});
        String domain = map.get(RspConstants.DOMAIN_LABEL);
        String host;
        if (!TextUtils.isEmpty(domain)) {
            LogHelper.d("[%s] host: %s", TAG,domain);
            host = RspConstants.HOSTPROTOCL + domain;
        } else {
            LogHelper.d("[%s] host is null", TAG);
            host = null;
        }
        String packageName = BaseApplication.getApplication().getPackageName();
        if("com.letv.wallet".equals(packageName) && EnvUtil.getInstance().isLePayTest()){
            return "https://test-wallet.scloud.letv.com";
        }else if("com.letv.walletbiz".equals(packageName) && EnvUtil.getInstance().isWalletTest()){
            return "https://test-wallet.scloud.letv.com";
        }else{
            return host;
        }
    }
}
