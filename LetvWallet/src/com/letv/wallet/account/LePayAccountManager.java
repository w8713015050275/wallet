package com.letv.wallet.account;


import android.content.SharedPreferences;
import android.os.Looper;
import android.text.TextUtils;

import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo;
import com.letv.wallet.account.aidl.v1.RedirectURL;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.DigestUtils;
import com.letv.wallet.common.util.SharedPreferencesHelper;

import java.util.ArrayList;

/**
 * Created by lijunying on 17-1-17.
 */

public final class LePayAccountManager implements LePayEngine.CallBack {
    private static LePayAccountManager sInstance;
    public LePayEngine lepayEngine;


    private static ArrayList<LePayCommonCallback> callbacks = new ArrayList<LePayCommonCallback>();

    private LePayAccountManager() {
    }

    public synchronized static LePayAccountManager getInstance() {
        if (sInstance == null) {
            sInstance = new LePayAccountManager();
        }
        return sInstance;
    }

    private void registerCallback(LePayCommonCallback callback){
        callbacks.add(callback);
    }

    public static void unRegisterCallback(LePayCommonCallback callback){
        callbacks.remove(callbacks);
    }

    private boolean checkRunBefore(LePayCommonCallback callback){
        if (LePayUtils.isExistPayApp() && LePayUtils.isPayHasAidl() && Looper.myLooper() == Looper.getMainLooper()) {
            return true;
        }
        if (callback != null) {
            callback.onError(AccountConstant.RspCode.ERROR_PAY_VERSION , null);
        }
        return false;
    }

    private void checkEngine(){
        if (lepayEngine == null) {
            lepayEngine = new LePayEngine(this);
        }
        if (lepayEngine.isConnected()) {
            return;
        }
        lepayEngine.bindService();
    }

    public static boolean hasCreatedAccount() {
        SharedPreferences sharedPreferences = SharedPreferencesHelper.getUserIdPreferences(AccountConstant.LEPAY_PKG);
        if (sharedPreferences != null) {
           return sharedPreferences.getBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid()+AccountConstant.SHAREDPREFERENCES_CREATE_ACCOUNT_SUFFIX), false);
        }
        return false;
    }

    public static boolean hasVerifyAccount(){
        SharedPreferences sharedPreferences = SharedPreferencesHelper.getUserIdPreferences(AccountConstant.LEPAY_PKG);
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(DigestUtils.getMd5_30(AccountHelper.getInstance().getUid()+AccountConstant.SHAREDPREFERENCES_VERIFY_ACCOUNT_SUFFIX), false);
        }
        return false;
    }

    public void createAccount(final LePayCommonCallback callback) {
        if (!checkRunBefore(callback)) {
             return;
        }

        checkEngine();

        registerCallback(callback);

        if (lepayEngine.isConnected()) {
            lepayEngine.createAccount(callback);
        } else {
            LePayUtils.putBlockingOper(new Runnable() {
                @Override
                public void run() {
                    lepayEngine.createAccount(callback);
                }
            });
        }
    }

    public void queryAccount(final String qType, final LePayCommonCallback callback) {
        if (TextUtils.isEmpty(qType) || !checkRunBefore(callback))
            return;

        checkEngine();

        registerCallback(callback);

        if (lepayEngine.isConnected()) {
            lepayEngine.queryAccount(qType, callback);
        } else {
            LePayUtils.putBlockingOper(new Runnable() {
                @Override
                public void run() {
                    lepayEngine.queryAccount(qType, callback);
                }
            });
        }
    }

   public void redirect(final String[] jTypes , final LePayCommonCallback<RedirectURL> callback){
       if (jTypes == null || !checkRunBefore(callback)) {
           return;
       }

       checkEngine();

       registerCallback(callback);

       if (lepayEngine.isConnected()) {
           lepayEngine.redirect(jTypes, callback);
       } else {
           LePayUtils.putBlockingOper(new Runnable() {
               @Override
               public void run() {
                   lepayEngine.redirect(jTypes, callback);
               }
           });
       }
    }

    @Override
    public void onServiceReady(LePayEngine service) {
        LePayUtils.clearBlockingOper();
    }

    @Override
    public void onServiceLost() {
        for (LePayCommonCallback callback : callbacks) {
            callback.onError(AccountConstant.RspCode.ERROR_REMOTE_SERVICE_KILLED, null);
        }
    }

    private boolean checkCreateAccount(boolean isForceCreate, final CreateAccountResult createAccountResult) {
        boolean hasCreateAccount = hasCreatedAccount();
        if (!hasCreatedAccount() && isForceCreate) {
            createAccount(new LePayCommonCallback() {

                              @Override
                              public void onSuccess(Object o) {
                                  if (null != createAccountResult) {
                                      createAccountResult.createAccountSuccess();
                                  }
                              }

                              @Override
                              public void onError(int errorCode, String errorMsg) {
                                  if (null != createAccountResult) {
                                      createAccountResult.createAccountError();
                                  }
                              }
                          }
            ); //默认开一次户
        }
        return hasCreateAccount;
    }

    public void bankQueryAccountInfo(final QueryAccountResult queryAccountResult) {
        String qType = null;
        if (checkCreateAccount(false, null) && hasVerifyAccount()) { //用户已开户并已实名， 直接查询卡列表
            qType = AccountConstant.QTYPE_CARD;
        } else {
            qType = AccountConstant.QTYPE_ALL; //查询用户状态
        }
        queryAccount(qType, new LePayCommonCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo accountInfo) {
                if (checkCreateAccount(true, new CreateAccountResult() {

                    @Override
                    public void createAccountSuccess() {
                        if (null != queryAccountResult) {
                            queryAccountResult.queryAccountSuccess(null);
                        }
                    }

                    @Override
                    public void createAccountError() {
                        if (null != queryAccountResult) {
                            queryAccountResult.queryAccountError();
                        }
                    }
                })) {
                    if (null != queryAccountResult) {
                        queryAccountResult.queryAccountSuccess(accountInfo);
                    }
                }

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                //发生错误时，不做任何改变
                if (null != queryAccountResult) {
                    queryAccountResult.queryAccountError();
                }
            }
        });
    }

    public interface QueryAccountResult<T> {
        void queryAccountSuccess(T result);

        void queryAccountError();
    }

    public interface CreateAccountResult {
        void createAccountSuccess();

        void createAccountError();
    }

    public void lelehuaQueryAccountInfo(final QueryAccountResult queryAccountResult) {
        String qType = null;
        if (checkCreateAccount(false, null)) { //用户已开户并已实名， 直接查询卡列表
            qType = AccountConstant.QTYPE_LELEHUA;
        } else {
            qType = AccountConstant.QTYPE_ALL; //查询用户状态
        }
        queryAccount(qType, new LePayCommonCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo accountInfo) {
                if (checkCreateAccount(true, new CreateAccountResult() {

                    @Override
                    public void createAccountSuccess() {
                        if (null != queryAccountResult) {
                            queryAccountResult.queryAccountSuccess(null);
                        }
                    }

                    @Override
                    public void createAccountError() {
                        if (null != queryAccountResult) {
                            queryAccountResult.queryAccountError();
                        }
                    }
                })) {
                    //信息已经获取完成，再次获取跳转地址

                    if (null != queryAccountResult) {
                        queryAccountResult.queryAccountSuccess(accountInfo);
                    }
                }

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                //发生错误时，不做任何改变
                if (null != queryAccountResult) {
                    queryAccountResult.queryAccountError();
                }
            }
        });
    }
}
