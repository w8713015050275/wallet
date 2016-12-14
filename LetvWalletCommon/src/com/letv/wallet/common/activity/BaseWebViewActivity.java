package com.letv.wallet.common.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.letv.wallet.common.BaseApplication;
import com.letv.wallet.common.R;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.view.BlankPage;

import java.util.Map;

/**
 * Created by linquan on 16-4-27.
 */
public class BaseWebViewActivity extends BaseFragmentActivity {
    private static final String TAG = "BaseWebView";

    private FrameLayout mContainer;

    protected WebView mWebView;
    protected String mUrl;
    protected String mTitle;

    private BaseWebViewClient mWebViewClient = new BaseWebViewClient();

    private View.OnClickListener mRefreshClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mWebView != null) {
                mWebView.reload();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        mContainer = new FrameLayout(this);
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView = new WebView(getApplicationContext());
        mContainer.addView(mWebView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(mContainer);

        Toolbar toolbar = getToolbar();
        ImageButton refreshView = new ImageButton(this, null, android.support.v7.appcompat.R.attr.toolbarNavigationButtonStyle);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        refreshView.setLayoutParams(layoutParams);
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        layoutParams.rightMargin = (int) DensityUtils.dip2px(10);
        refreshView.setImageResource(R.drawable.ic_wallet_action_refresh);
        refreshView.setOnClickListener(mRefreshClickListener);
        toolbar.addView(refreshView, layoutParams);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_wallet_action_close);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        } else {
            mUrl = intent.getStringExtra(CommonConstants.EXTRA_URL);
            if (TextUtils.isEmpty(mUrl)) {
                finish();
                return;
            }
            mTitle = intent.getStringExtra(CommonConstants.EXTRA_TITLE_NAME);
            if (!TextUtils.isEmpty(mTitle)) {
                setTitle(mTitle);
            }
        }

        initWebView();
        if (!isNetworkAvailable()) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
            return;
        }

        showLoadingView();
        mWebView.loadUrl(mUrl, getAdditionalHttpHeaders());

    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && mContainer.getVisibility() != View.VISIBLE) {
            hideBlankPage();
            showLoadingView();
            mWebView.loadUrl(mUrl);
        }
    }

    protected boolean needUpdateTitle() {
        return false;
    }

    protected Map<String, String> getAdditionalHttpHeaders() {
        return null;
    }

    protected void initWebView() {
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                    try {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    hideLoadingView();
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (needUpdateTitle() && !TextUtils.isEmpty(title)) {
                    mTitle = title;
                    setTitle(title);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (callback != null) {
                    callback.invoke(origin, true, false);
                }
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });
        WebSettings settings = mWebView.getSettings();
        BaseApplication application = BaseApplication.getApplication();
        if (application != null) {
            String ua = new StringBuilder()
                    .append(settings.getUserAgentString())
                    .append(' ')
                    .append(application.getAppUA())
                    .toString();
            settings.setUserAgentString(ua);
        }

        settings.setJavaScriptEnabled(true);

        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        String cacheDir = getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        String dbDir = getApplicationContext().getDir("databases", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(cacheDir);
        settings.setAppCacheMaxSize(5 * 1024 * 1024);
        settings.setDatabasePath(dbDir);

        settings.setGeolocationDatabasePath(dbDir);

        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        settings.setGeolocationEnabled(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(true);
        CookieManager.getInstance().setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        LogHelper.d("[%s]initWebView end", TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        mWebView.setWebChromeClient(null);
        mWebView.setDownloadListener(null);
        mWebView.setWebViewClient(null);
        mWebView.destroy();
        mContainer.removeAllViews();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(mWebView.canGoBack()) {
                mWebView.goBack();//返回上一页面
                return true;
            } else {
                finish();//退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public class BaseWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //返回值是true的人为控制打开逻辑，为false时webview自己处理
            LogHelper.d("[" + TAG + "]shouldOverrideUrlLoading url is: " + url);
            if (!url.startsWith("http")) {
                LogHelper.d("["+TAG+"]shouldOverrideUrlLoading go browser");
                Intent intent = null;
                if (url.startsWith("intent:")) {
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    } catch (Exception e) {
                        LogHelper.d(e.toString());
                        intent = null;
                    }
                }
                try {
                    if (intent == null) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    }
                } catch (Exception e) {
                    LogHelper.d(e.toString());
                }
                if (intent == null) {
                    return false;
                }
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    LogHelper.d(e.toString());
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

}


