package com.letv.wallet.common.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
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

    private View.OnClickListener mCloseButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_WebView);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        mUrl = getUrl(intent);

        mTitle = getWebViewTitle(intent);

        registerNetWorkReceiver();
        mContainer = new FrameLayout(this);
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView = new WebView(getApplicationContext());
        mContainer.addView(mWebView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(mContainer);

        initView();

        initWebView();

        if (TextUtils.isEmpty(mUrl)) {
            if (!hasRedirect()) { finish(); }
            return;
        }

        if (!TextUtils.isEmpty(mTitle)) {
            setTitle(mTitle);
        }

        if (!isNetworkAvailable()) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
            return;
        }

        loadPage();

    }

    protected void loadPage(){
        showLoadingView();
        mWebView.loadUrl(mUrl, getAdditionalHttpHeaders());
    }

    protected String getUrl(Intent intent) {
        if (intent != null) {
            return intent.getStringExtra(CommonConstants.EXTRA_URL);
        }
        return null;
    }

    protected String getWebViewTitle(Intent intent) {
        if (intent != null) {
            return intent.getStringExtra(CommonConstants.EXTRA_TITLE_NAME);
        }
        return null;
    }

    protected void initView() {
        Toolbar toolbar = getToolbar();
        if (showRefreshButton()) {
            ImageButton refreshView = new ImageButton(this, null, android.support.v7.appcompat.R.attr.toolbarNavigationButtonStyle);
            Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            refreshView.setLayoutParams(layoutParams);
            layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            layoutParams.rightMargin = (int) DensityUtils.dip2px(10);
            refreshView.setImageResource(R.drawable.ic_wallet_action_refresh);
            refreshView.setOnClickListener(getRefreshClickListener());
            toolbar.addView(refreshView, layoutParams);
        }
        updateHomeAsUpButton(toolbar);
    }

    protected void updateHomeAsUpButton(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        View.OnClickListener closeListener = getCloseButtonClickListener();
        if (closeListener != null) {
            toolbar.setNavigationOnClickListener(closeListener);
        }
        toolbar.setNavigationIcon(R.drawable.ic_wallet_action_close);
    }

    protected View.OnClickListener getCloseButtonClickListener() {
        return mCloseButtonClickListener;
    }

    protected boolean showRefreshButton() {
        return true;
    }

    protected View.OnClickListener getRefreshClickListener() {
        return mRefreshClickListener;
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
            mWebView.loadUrl(mUrl, getAdditionalHttpHeaders());
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = getUrl(getIntent());
        if (TextUtils.isEmpty(url) || url.equalsIgnoreCase(mUrl)) {
            return;
        }
        mTitle = getWebViewTitle(intent);
        if (!TextUtils.isEmpty(mTitle)) {
            setTitle(mTitle);
        }
        loadPage();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.setWebChromeClient(null);
            mWebView.setDownloadListener(null);
            mWebView.setWebViewClient(null);
            mContainer.removeAllViews();
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();//返回上一页面
        } else {
            super.onBackPressed();
        }
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
                    setFlagIfNeeded(intent);
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

    public void setFlagIfNeeded(Intent intent){

    }

    public boolean hasRedirect(){
        return false;
    }
}


