package com.letv.walletbiz.movie.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.WalletApplication;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.movie.MovieTicketConstant;

/**
 * Created by liuliang on 16-4-28.
 *
 * wiki:http://wiki.letv.cn/pages/viewpage.action?pageId=47027717
 */
public class MovieSearchHelper {

    /**
     * dt=1,7,18 (dt表示搜索支持搜索的数据类型，1表示专辑，7表示关注词，18表示影院)
     * from=wallet_cinema (from参数表示调起万象的产品线，钱包传wallet_cinema即可)
     * card_id=203 (首页的数据模块按card划分，要显示哪个card就请求哪个，203是音乐推荐的card，后期如果有影院或上映影片的card再增加)
     * limit_rst=0 （表示搜索结果是否要限制条数。由于万象承接了各方应用内容，展示所有数据类型的所有返回结果cost太大，我们做了每个应用只展示TOP3的策略，万象是会固定传limit_rst=1的，但钱包不需要限制条数，所以传0.）
     */
    public static void startSearch(Context context) {
        if (context == null) {
            return;
        }
        int versioncode = getQuickSearchVersion();
        try {

            Action.uploadClick(Action.MOVIE_SEARCH_CLICK);
            if (versioncode >= 210) {
                int cityId = SharedPreferencesHelper.getInt(MovieTicketConstant.PREFERENCES_CURRENT_CITY_ID, -1);
                String uriStr = "panosearch://search/main?from=wallet_cinema&card_id=203&limit_rst=0&dt=1,7,18";
                if (cityId != -1) {
                    uriStr += "&city_id=" + cityId;
                }
                Uri uri = Uri.parse(uriStr);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(uri);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent("com.letv.supersearch.action.LAUNCH");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            LogHelper.e("can not start wanxiangsousuo");
        }

    }

    public static int getQuickSearchVersion() {
        int versionCode = -1;
        try {
            Context context = WalletApplication.getApplication();
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo("com.letv.android.quicksearchbox", 0);
            versionCode = info.versionCode;
        } catch (Exception e) {
            LogHelper.d(e.toString());
            versionCode = -1;
        }
        return versionCode;
    }
}
