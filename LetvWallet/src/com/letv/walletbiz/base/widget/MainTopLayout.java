package com.letv.walletbiz.base.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.letv.wallet.account.LePayAccountManager;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.walletbiz.R;
import com.letv.walletbiz.main.MainPanelHelper;
import com.letv.walletbiz.main.MainTopTask;
import com.letv.walletbiz.main.bean.WalletTopListBean;

import org.xutils.common.task.PriorityExecutor;

/**
 * Created by zhuchuntao on 16-8-30.
 */
public class MainTopLayout extends LinearLayout implements AccountHelper.OnAccountChangedListener {

    private Context context;

    private final int MAX_CHIND_COUNT = 3;

    private int actualButtonNumber = 0;


    public MainTopLayout(Context context) {
        this(context, null);
    }

    public MainTopLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTopLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;


        setOrientation(LinearLayout.HORIZONTAL);
        //初始化的时候，不显示
        setVisibility(View.GONE);
        for (int i = 0; i < MAX_CHIND_COUNT; i++) {
            addButton();
        }
    }

    private MainTopButton addButton() {
        MainTopButton button = new MainTopButton(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
        addView(button, params);
        return button;
    }

    private MainTopButton getMainTopButton(int index) {
        return (MainTopButton) getChildAt(index);
    }

    private MainTopButton getMainTopButton(int searchNumber, String tag) {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        for (int i = 0; i < searchNumber; i++) {
            String childTag = (String) getChildAt(i).getTag();
            if (!TextUtils.isEmpty(childTag) && childTag.equals(tag)) {
                return (MainTopButton) getChildAt(i);
            }
        }
        return null;
    }

    public void setRealData(WalletTopListBean walletTop) {
        //获取到信息的时候展示
        setVisibility(View.VISIBLE);
        if (lastResult != null) {
            //预防界面刷新的问题，每次进来都要重新获取数据，但是没有更改
            if (lastResult.version == walletTop.version) {
                return;
            }
        }
        WalletTopListBean.WalletTopBean[] bean = walletTop.list;
        int buttonNumber = getRealBeanSize(bean);
        setViewDisplay(buttonNumber);

        //只所以传递dataSize，不能查找所有的button，防止服务器返回数据更改时，上一次有，这次没有
        setData(bean, buttonNumber);
        //这个时候去查询银行卡的数量
        loadAccountData();
    }

    //当服务器返回的数据错误时，本地只显示一个卡券包
    private void setViewDisplay(int dataSize) {
        if (dataSize == 0)
            dataSize = 1;
        for (int i = 0; i < MAX_CHIND_COUNT; i++) {
            if (i < dataSize) {
                getMainTopButton(i).setVisibility(View.VISIBLE);
            } else {
                getMainTopButton(i).setVisibility(View.GONE);
            }
        }
    }

    private void setData(WalletTopListBean.WalletTopBean[] bean, int dataSize) {

        if (dataSize == 0) {
            //此时默认为没有获取到服务器返回的数据
            dataSize = 1;
            WalletTopListBean.WalletTopBean cardBean = new WalletTopListBean.WalletTopBean();
            cardBean.name = MainTopButton.TOP_KEY_CARD;
            cardBean.title = context.getString(R.string.main_top_card);
            //继续设置，伪造假数据
            cardBean.icon = R.drawable.main_top_card_coupon + "";
            bean = new WalletTopListBean.WalletTopBean[1];
            bean[0] = cardBean;
        }

        for (int i = 0; i < dataSize; i++) {
            getMainTopButton(i).setData(bean[i]);
            getMainTopButton(i).setTag(bean[i].name);
        }
        actualButtonNumber = dataSize;
    }

    //检测返回的数据是否符合逻辑
    private int getRealBeanSize(WalletTopListBean.WalletTopBean[] bean) {
        if (bean == null || bean.length == 0) {
            return 0;
        } else {
            if (bean.length > MAX_CHIND_COUNT) {
                return MAX_CHIND_COUNT;
            }
            return bean.length;
        }
    }


    private PriorityExecutor mExecutor = new PriorityExecutor(3);
    private MainTopTask mTopTask;

    /**
     * 从服务器获取关于button的数据
     */
    public void loadButtonData() {
        if (mTopTask == null) {
            mTopTask = new MainTopTask(context, mTopCallback);
            mExecutor.execute(mTopTask);
        }
        //每次返回界面时，要重新刷新数量，防止用户的操作，此界面没有及时更新
        loadAccountData();
    }

    private WalletTopListBean lastResult;

    private MainPanelHelper.Callback<WalletTopListBean> mTopCallback = new MainPanelHelper.Callback<WalletTopListBean>() {

        @Override
        public void onLoadFromLocalFinished(WalletTopListBean result, int errorCode) {
            if (result != null && errorCode == MainPanelHelper.NO_ERROR) {
                //result.list=null;
                setRealData(result);
                lastResult = result;

            }
        }

        @Override
        public void onLoadFromNetworkFinished(WalletTopListBean result, int errorCode, boolean needUpdate) {
            mTopTask = null;
            if (result != null && errorCode == MainPanelHelper.NO_ERROR) {
                //result.list=null;
                setRealData(result);
                lastResult = result;

            }
        }
    };


    //因为本地和服务器的返回都会执行，不能查询两次
    private boolean isloadAccountData = false;

    /**
     * 从lepay获取关于银行卡的数据
     */
    public void loadAccountData() {
        loadLelehuaAccountData();

        if (!isloadAccountData) {
            if (AccountHelper.getInstance().isLogin(context) && NetworkHelper.isNetworkAvailable()) {
                isloadAccountData = true;
                setBankButtonClick(false);
                LePayAccountManager.getInstance().bankQueryAccountInfo(new LePayAccountManager.QueryAccountResult<AccountInfo>() {

                    @Override
                    public void queryAccountSuccess(AccountInfo result) {
                        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_BANK);
                        if (null != button) {
                            button.setClickable(true);
                            button.setNumber(result);
                            isloadAccountData = false;
                        } else {

                        }
                    }

                    @Override
                    public void queryAccountError() {
                        isloadAccountData = false;
                        setBankButtonClick(true);
                        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_BANK);
                        if (null != button) {
                            button.resetDrawable();
                        }
                    }
                });
            }
        }
    }

    /**
     * 从lepay获取关于乐乐花的数据
     */
    //因为本地和服务器的返回都会执行，不能查询两次
    private boolean isloadLelehuaAccountData = false;

    public void loadLelehuaAccountData() {
        if (!isloadLelehuaAccountData) {
            if (AccountHelper.getInstance().isLogin(context) && NetworkHelper.isNetworkAvailable()) {
                isloadLelehuaAccountData = true;
                setLelehuaButtonClick(false);
                LePayAccountManager.getInstance().lelehuaQueryAccountInfo(new LePayAccountManager.QueryAccountResult<AccountInfo>() {

                    @Override
                    public void queryAccountSuccess(AccountInfo result) {
                        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_LELEHUA);
                        if (null != button) {
                            button.setClickable(true);
                            button.setNumber(result);
                            isloadLelehuaAccountData = false;
                        } else {

                        }
                    }

                    @Override
                    public void queryAccountError() {
                        isloadLelehuaAccountData = false;
                        setLelehuaButtonClick(true);
                        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_LELEHUA);
                        if (null != button) {
                            button.resetDrawable();
                        }
                    }
                });
            }
        }
    }


    private void setLelehuaButtonClick(boolean click) {
        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_LELEHUA);
        if (null != button) {
            button.setClickable(click);
        }
    }

    private void setBankButtonClick(boolean click) {
        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_BANK);
        if (null != button) {
            button.setClickable(click);
        }
    }

    @Override
    public void onAccountLogin() {
    }

    @Override
    public void onAccountLogout() {
        MainTopButton button = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_BANK);
        if (button != null) {
            button.resetDrawable();
        }
        MainTopButton leButton = getMainTopButton(actualButtonNumber, MainTopButton.TOP_KEY_LELEHUA);
        if (leButton != null) {
            leButton.resetDrawable();
        }
        //设置为空是为了，登出之后进入，要重新查询数据
        lastResult = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
    }
}
