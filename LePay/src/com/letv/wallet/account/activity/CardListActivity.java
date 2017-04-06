package com.letv.wallet.account.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.tracker2.enums.EventType;
import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.AccountCommonConstant;
import com.letv.wallet.account.adapter.CardListAdapter;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.account.task.AccountCommonCallback;
import com.letv.wallet.account.task.AccountCreateTask;
import com.letv.wallet.account.task.AccountQueryTask;
import com.letv.wallet.account.task.RedirectTask;
import com.letv.wallet.account.utils.AccountUtils;
import com.letv.wallet.account.utils.ActionUtils;
import com.letv.wallet.base.util.Action;
import com.letv.wallet.common.activity.AccountBaseActivity;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.view.BlankPage;

import timehop.stickyheader.RecyclerItemClickListener;

/**
 * Created by lijunying on 17-2-10.
 */

public class CardListActivity extends AccountBaseActivity implements View.OnClickListener, AccountHelper.OnAccountChangedListener {
    private static final String TAG = "CARD";
    private RecyclerView mRecyclerView;
    private CardListAdapter mAdapter ;
    private AccountCreateTask createTask;
    private AccountQueryTask queryTask;
    private RedirectTask redirectTask;

    private boolean ACCOUNT_FAIL_REASON_PHONE_NULL = false;
    private boolean hasCreateAccount = false;
    private boolean hasVerifyAccount = false;

    private boolean isDataValidate = false;

    private RedirectURL redirectURL = null;

    private TextView btnAddCard;

    private static final String EXTRA_CARDBIN = "LePayCardBinInfo";

    private String from ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_card_list_activity);
        initView();
        AccountHelper.getInstance().registerOnAccountChangeListener(this);
        registerNetWorkReceiver();
        Action.uploadExpose(Action.ACCOUNT_CARD_LIST_PAGE_EXPOSE, (from = ActionUtils.getFromExtra(getIntent())));
        if (getIntent() != null) {
            handleParcelableArray(getIntent().getParcelableArrayExtra(EXTRA_CARDBIN));
        }
    }

    private void initView() {
        btnAddCard = (TextView) findViewById(R.id.btnAddCard);
        btnAddCard.setOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CardListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(PayApplication.getApplication(), R.string.account_card_item_click_tip, Toast.LENGTH_SHORT).show();
                Action.uploadClick(Action.ACCOUNT_CARD_LIST_ITEM_CLICK);
            }
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkLogin() || !checkNetWork()) {
            return;
        }
        hideBlankPage();
        loadData(false);
    }

    @Override
    protected void onDestroy() {
        AccountHelper.getInstance().unregisterOnAccountChangeListener(this);
        super.onDestroy();
    }

    private void loadData(boolean isForce){
        if (!isForce && isDataValidate) { //非强制更新 & 数据未失效 返回
            return;
        }

        String qType = null;
        if (checkCreateAccount(false) && checkVerifyAccount()) { //用户已开户并已实名， 直接查询卡列表
            qType = AccountConstant.QTYPE_CARD;
        } else {
            qType = AccountConstant.QTYPE_ALL; //查询用户状态
        }
        queryAccount(qType);
    }

    private boolean checkLogin() {
        if(AccountHelper.getInstance().isLogin(this)){
            return true;
        }
        showNoLoginBlankPage();
        return false ;
    }

    public BlankPage showNoLoginBlankPage() {
        BlankPage blankPage = showBlankPage();
        blankPage.setPageState(BlankPage.STATE_NO_LOGIN, null);
        blankPage.getPrimaryBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    promptNoNetWork();
                    return;
                }
                AccountHelper.getInstance().loginOrJumpLetvAccount(CardListActivity.this);
            }
        });
        return blankPage;
    }

    private boolean checkNetWork(){
        if (NetworkHelper.isNetworkAvailable()) {
            return true;
        }
        if (mAdapter.getItemCount() <= 0) {
            showBlankPage(BlankPage.STATE_NO_NETWORK);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddCard:
                checkBindCard();
                break;
        }
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public void onAccountLogin() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_card_list) {
            Intent intent = new Intent(this, AccountWebActivity.class);
            intent.putExtra(CommonConstants.EXTRA_URL, AccountCommonConstant.URL_CARD_LIST_HELP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountLogout() {
        // 账号登出 清空卡列表， 取消task
        mAdapter.setData(null);
        isDataValidate = false;
        ACCOUNT_FAIL_REASON_PHONE_NULL = false;
        queryTask = null;
        createTask = null;
        redirectTask = null;
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable && AccountHelper.getInstance().isLogin(this)) {
            loadData(false);
        }
    }

    private void createAccount() {
        ACCOUNT_FAIL_REASON_PHONE_NULL = false;
        if (createTask == null) {
            createTask = new AccountCreateTask(new AccountCommonCallback() {
                @Override
                public void onSuccess(Object result) {
                    createTask = null;
                    hideLoadingView();
                    hasCreateAccount = true;
                    checkEmptyPage(getString(R.string.account_card_add_bankcard)); //开户成功，显示添加卡片，点击去实名认证
                }
                @Override
                public void onError(int errorCode, String errorMsg) {
                    hideLoadingView();
                    createTask = null;
                    if (errorCode == AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED) {
                        showNoLoginBlankPage(); //token过期，显示未登录；
                        return;
                    }
                    if (errorCode == AccountConstant.RspCode.ERRNO_MOBILE_EMPTY) {
                        ACCOUNT_FAIL_REASON_PHONE_NULL = true;
                        checkEmptyPage(getString(R.string.account_card_add_bankcard)); //无手机号开户失败，显示添加卡片 点击去绑手机号
                    }else if(errorCode == AccountConstant.RspCode.ERROR_NETWORK){
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showLoadingView();
                                createAccount();
                            }
                        });
                        return;
                    }else {
                        LogHelper.e("[%S] : createAccount errorCode = " + errorCode + " errorMsg = " + errorMsg, TAG);
                        checkEmptyPage(null); //其他原因开户失败,隐藏添加银行卡按钮；
                    }

                }

                @Override
                public void onNoNet() {
                    hideLoadingView();
                    createTask = null;
                    showBlankPage(BlankPage.STATE_NO_NETWORK);
                }
            });
            ExecutorHelper.getExecutor().runnableExecutor(createTask);
        }
    }

    private void queryAccount(final String qType){
        if (queryTask == null) {
            if (mAdapter.getItemCount() <= 0) {
                showLoadingView();
            }
            queryTask = new AccountQueryTask(qType, new AccountCommonCallback<AccountInfo>() {
                @Override
                public void onSuccess(AccountInfo result) {
                    queryTask = null;
                    // 已开户 直接更新数据 ; 未开户 去开户
                    if (checkCreateAccount(true)) {
                        hideLoadingView();
                        upateCardList(result == null ? null:result.cardList);
                        checkEmptyPage(getString(R.string.account_card_add_bankcard));
                    }
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    hideLoadingView();
                    queryTask = null;
                    LogHelper.e("[%S] : queryAccount qType = " + qType + " errorCode = " + errorCode + " errorMsg = " + errorMsg, TAG);
                    if (errorCode == AccountConstant.RspCode.ERRNO_USER_AUTH_FAILED) {
                        showNoLoginBlankPage();
                        return;

                    }
                    if (mAdapter.getItemCount() <= 0) {
                        showBlankPage(BlankPage.STATE_NETWORK_ABNORMAL).getIconView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                queryAccount(qType);
                            }
                        });
                    }else{
                        Toast.makeText(PayApplication.getApplication(), R.string.empty_network_error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNoNet() {
                    hideLoadingView();
                    queryTask = null;
                    if (mAdapter.getItemCount() > 0) {
                        Toast.makeText(PayApplication.getApplication(), R.string.empty_no_network, Toast.LENGTH_SHORT).show();
                    }else {
                        showBlankPage(BlankPage.STATE_NO_NETWORK);
                    }

                }
            });
            ExecutorHelper.getExecutor().runnableExecutor(queryTask);
        }
    }

    private boolean checkCreateAccount(boolean isForceCreate){
        hasCreateAccount = AccountUtils.hasCreatedAccount();
        if (!hasCreateAccount && isForceCreate) {
            createAccount();
        }
        return hasCreateAccount;
    }

    private boolean checkVerifyAccount(){
        hasVerifyAccount = AccountUtils.hasVerifyAccount();
        return hasVerifyAccount;
    }

    private void upateCardList(AccountInfo.CardBin[] cardList){
        if (cardList == null || cardList.length <= 0) {
            return;
        }
        mAdapter.setData(cardList);
        isDataValidate = true;
    }

    private void handleParcelableArray(Parcelable[] parcelableArrayExtra) {
        if (parcelableArrayExtra == null || parcelableArrayExtra.length == 0) {
            return;
        }

        checkCreateAccount(false) ;
        checkVerifyAccount();

        AccountInfo.CardBin[] cardBinList = new AccountInfo.CardBin[parcelableArrayExtra.length];
        int i = 0;
        try {
            for (Parcelable card : parcelableArrayExtra) {
                cardBinList[i++] = (AccountInfo.CardBin) card;
            }
        } catch (ClassCastException e) {
           return;
        }
        upateCardList(cardBinList);
    }

    private void jumpWeb(String jType) {
        AccountUtils.goToAccountWeb(this, jType);
    }

    private boolean checkAccountStatus(){
        if (!hasCreateAccount && !ACCOUNT_FAIL_REASON_PHONE_NULL) { //未开户，手机号不为空
            Toast.makeText(this, R.string.account_card_unavailable, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ACCOUNT_FAIL_REASON_PHONE_NULL) { //未开户，手机号为空，绑手机号
            Toast.makeText(this, R.string.account_card_no_phone, Toast.LENGTH_SHORT).show();
            jumpWeb(AccountConstant.JTYPE_SSO_BIND_MOBILE);
            ACCOUNT_FAIL_REASON_PHONE_NULL = false;
            return false;
        }
        if (!hasVerifyAccount) {//未实名
            startActivity(ActionUtils.newIntent(this, AccountVerifyActivity.class, Action.EVENT_PROP_FROM_ACCOUNT_CARD_LIST));
            return false;
        }
        return true;
    }

    private void checkBindCard(){
        Action.uploadCustom(EventType.Add, Action.ACCOUNT_CARD_LIST_CARD_ADD);

        isDataValidate = false ; //返回 更新数据

        if (checkAccountStatus()) {
            // 已开户 & 已实名
            jumpWeb(AccountConstant.JTYPE_ADD_CARD);
        }
    }

    private void checkEmptyPage(String primaryText){
        if (mAdapter.getItemCount() <= 0) {
            BlankPage blankPage = showBlankPage();
            blankPage.setCustomPage(getString(R.string.account_card_list_empty), BlankPage.Icon.NO_CLOCK);
            blankPage.getIconView().setBackground(getDrawable(R.drawable.card_empty_icon));
            blankPage.setPrimaryText(primaryText);
            blankPage.getPrimaryBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBindCard();
                }
            });
        }
    }
}
