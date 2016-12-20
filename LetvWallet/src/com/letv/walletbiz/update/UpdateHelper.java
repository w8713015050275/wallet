package com.letv.walletbiz.update;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letv.shared.widget.LeBottomSheet;
import com.letv.wallet.common.activity.BaseFragmentActivity;
import com.letv.wallet.common.util.NetworkHelper;
import com.letv.wallet.common.util.SharedPreferencesHelper;
import com.letv.walletbiz.MainActivity;
import com.letv.walletbiz.R;
import com.letv.walletbiz.update.beans.RemoteAppInfo;
import com.letv.walletbiz.update.util.UpdateUtil;

import java.util.ArrayList;

import java.util.List;

/**
 * Created by zhangzhiwei1 on 16-9-9.
 */
public class UpdateHelper {

    private BaseFragmentActivity mBaseActivity;
    private LeBottomSheet mUpgradeSheet;
    private LeBottomSheet mNoWifiDialog;
    private Messenger mSendToServiceMessenger;

    private boolean mIsUpgradeServiceRegistered;

    private boolean mIsNoWifiDialogShowing;
    private boolean mIsUpgradeSheetShowing;
    private Message mMsgFromServer;

    public static final String HIDE_UPGRADE_DIALOG = "walletbiz.action.hide_upgrade_dialog";
    private boolean isHideDialogReceiverRegisted;
    private BroadcastReceiver mHideUpgradeDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideUpgradeDialog();
        }
    };

    public UpdateHelper(BaseFragmentActivity activity) {
        mBaseActivity = activity;
        init();
    }

    public void init() {
        if (isUpgradeNoticePushedByServer(mBaseActivity.getIntent())) {
            UpdateUtil.mIsStartedNewly = true;
        }
    }

    public void registerHideDialogReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(HIDE_UPGRADE_DIALOG);
        mBaseActivity.registerReceiver(mHideUpgradeDialogReceiver, filter);
        isHideDialogReceiverRegisted = true;
    }

    public void unRegisterHideDialogReceiver() {
        if (isHideDialogReceiverRegisted == true) {
            mBaseActivity.unregisterReceiver(mHideUpgradeDialogReceiver);
            isHideDialogReceiverRegisted = false;
        }
    }

    public void startUpgradeService() {
        if(mIsNoWifiDialogShowing) {
            mIsNoWifiDialogShowing = false;
            if (mNoWifiDialog != null) {
                mNoWifiDialog.dismiss();
                mNoWifiDialog = null;
            }
            showNoWifiDialog(mBaseActivity);
            return;
        }

        if(mIsUpgradeSheetShowing) {
            mIsUpgradeSheetShowing = false;
            if(mUpgradeSheet != null) {
                mUpgradeSheet.dismiss();
                mUpgradeSheet = null;
            }
            ArrayList<RemoteAppInfo> remoteAppInfos = (ArrayList<RemoteAppInfo>) mMsgFromServer.obj;
            mMsgFromServer.arg1 = UpdateUtil.checkAppIsAllDownLoaded(mBaseActivity, remoteAppInfos) ? 1 : 0;
            showUpgradeDialog(mBaseActivity,mMsgFromServer);
            return;
        }
        if (UpdateUtil.mIsStartedNewly || UpdateUtil.isUpdateTimeExpire()) {
            if(NetworkHelper.isNetworkAvailable()) {
                UpdateUtil.startUpgradeService(mBaseActivity.getApplicationContext());
                UpdateUtil.bindUpgradeService(mBaseActivity, mConnection);
            } else {
                NetworkHelper.getInstance().addCallBack(1000, new NetworkHelper.NetworkAvailableCallBack() {
                    @Override
                    public void onNetworkAvailable() {
                        UpdateUtil.startUpgradeService(mBaseActivity.getApplicationContext());
                        UpdateUtil.bindUpgradeService(mBaseActivity, mConnection);
                    }
                });
            }
        }
    }

    private Boolean isUpgradeNoticePushedByServer(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return false;
        }
        if (intent.getData().toString().equals(UpdateConstant.UPGRADE_PUSH_MESSAGE_DATA)) {
            return true;
        }
        return false;
    }

    public  void handleNewIntent(Intent intent) {
        if (isUpgradeNoticePushedByServer(intent)) {
            sendMsgToService(UpdateConstant.QUREY_NEW_VERSION_TO_SERVICE_BY_PUSH_NOTICE);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mIsUpgradeServiceRegistered = true;
            mSendToServiceMessenger = new Messenger(service);
            sendMsgToService(UpdateConstant.QUREY_NEW_VERSION_TO_SERVICE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSendToServiceMessenger = null;
        }
    };

    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msgFromServer) {
            switch (msgFromServer.what) {
                case UpdateConstant.SHOW_UPDATE_DIALOG_TO_CLIENT_WITH_APPS_INFO:
                    if(mUpgradeSheet == null || !mUpgradeSheet.isShowing()) {
                        mMsgFromServer = Message.obtain();
                        mMsgFromServer.copyFrom(msgFromServer);

                        //kill current process,and MainActivity will be launched,and show upgrade dialog.
                        final boolean needForceUpgrade = msgFromServer.arg2 == 1;
                        if (!(mBaseActivity instanceof MainActivity)) {
                            if (needForceUpgrade) {
                                try {
                                    List<ActivityManager.AppTask> appTasks = ((ActivityManager) mBaseActivity
                                            .getSystemService(Context.ACTIVITY_SERVICE)).getAppTasks();
                                    if (appTasks != null && appTasks.size() > 0) {
                                        ActivityManager.RecentTaskInfo taskInfo = (ActivityManager.RecentTaskInfo)appTasks.get(0).getTaskInfo();
                                        String thisPackageName = mBaseActivity.getPackageName();
                                        if (taskInfo.numActivities > 1 && taskInfo.baseActivity.getClassName().indexOf(thisPackageName) >= 0) {
                                            exitCurrentApplication();
                                            return;
                                        }
                                    }
                                } catch (SecurityException e) {

                                }
                            }
                        }

                        if (!needForceUpgrade && !UpdateUtil.isUpdateTimeExpire()) {
                            return;
                        }
                        showUpgradeDialog(mBaseActivity,msgFromServer);
                    }
                    break;
                case UpdateConstant.EXIT_APPLICATION:
                    exitCurrentApplication();
                    break;
            }
        }
    });

    private void sendMsgToService(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.replyTo = mMessenger;

        try {
            mSendToServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showUpgradeDialog(Context context, Message msgFromServer) {
        if(mNoWifiDialog != null && mNoWifiDialog.isShowing()) {
            mNoWifiDialog.dismiss();
        }
        boolean isDownloaded = msgFromServer.arg1 == 1;
        final boolean needForceUpgrade = msgFromServer.arg2 == 1;
        RelativeLayout dialogLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.update_information_layout, null);
        final Button leBottomSheetConfirm = (Button) dialogLayout.findViewById(R.id.le_bottomsheet_btn_confirm5);
        Button leBottomSheetCancle = (Button) dialogLayout.findViewById(R.id.le_bottomsheet_btn_cancel5);
        ((TextView)(dialogLayout.findViewById(R.id.download_status))).setText(isDownloaded ? R.string.downloaded : R.string.undownloaded);

        String description = "";
        String remoteVersion = "1";
        if(UpdateUtil.mWalletbizAppInfo != null) {
            description = UpdateUtil.mWalletbizAppInfo.getDescription();
            remoteVersion = UpdateUtil.mWalletbizAppInfo.getApkVersion();
        }

        String content = TextUtils.isEmpty(description) ? mBaseActivity.getString(R.string.defautl_update_content) : description;
        String contentFormat = mBaseActivity.getResources().getString(R.string.update_content);
        ((TextView)(dialogLayout.findViewById(R.id.update_content))).setText(String.format(contentFormat, content));

        String versionFormat = mBaseActivity.getResources().getString(R.string.update_version);
        String version = String.format(versionFormat, remoteVersion);
        ((TextView)(dialogLayout.findViewById(R.id.update_version))).setText(version);


        UpdateUtil.mIsForceUpdate = needForceUpgrade;
        if (needForceUpgrade) {
            leBottomSheetConfirm.setText(UpdateUtil.mApkDownloading ? R.string.in_downloading : R.string.wallet_upgrade_now);
            leBottomSheetCancle.setText(R.string.wallet_exit_application);
            leBottomSheetConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //bug: LEUI-23548
                    if (!NetworkHelper.isNetworkAvailable()) {
                        Toast.makeText(mBaseActivity,mBaseActivity.getString(R.string.mobile_prompt_net_connection_fail),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    leBottomSheetConfirm.setText(R.string.in_downloading);
                    if(UpdateUtil.mApkDownloading) {
                        Toast.makeText(mBaseActivity,mBaseActivity.getString(R.string.in_downloading),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    continueToUpgrade();
                }
            });
            leBottomSheetCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUpgradeSheet.dismiss();
                    exitCurrentApplication();
                }
            });
        } else {
            leBottomSheetConfirm.setText(R.string.wallet_upgrade_now);
            leBottomSheetCancle.setText(R.string.wallet_upgrade_notify_later);
            leBottomSheetConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkHelper.isNetworkAvailable()) {
                        Toast.makeText(mBaseActivity,mBaseActivity.getString(R.string.mobile_prompt_net_connection_fail),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SharedPreferencesHelper.putLong(UpdateConstant.PREFERENCES_LAST_CHECK_UPDATE, System.currentTimeMillis());
                    mUpgradeSheet.dismiss();
                    continueToUpgrade();
                }
            });
            leBottomSheetCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUpgradeSheet.dismiss();
                    SharedPreferencesHelper.putLong(UpdateConstant.PREFERENCES_LAST_CHECK_UPDATE, System.currentTimeMillis());
                    sendMsgToService(UpdateConstant.NOTIFY_LATER_FROM_CLIENT);
                }
            });
        }

        mUpgradeSheet = new LeBottomSheet(context);
        mUpgradeSheet.setStyle(dialogLayout);
        mUpgradeSheet.setCanceledOnTouchOutside(false);
        mUpgradeSheet.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(needForceUpgrade) {
                    exitCurrentApplication();
                } else {
                    SharedPreferencesHelper.putLong(UpdateConstant.PREFERENCES_LAST_CHECK_UPDATE, System.currentTimeMillis());
                    sendMsgToService(UpdateConstant.NOTIFY_LATER_FROM_CLIENT);
                }
            }
        });

        mUpgradeSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsUpgradeSheetShowing = false;
            }
        });
        mUpgradeSheet.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mIsUpgradeSheetShowing = true;
            }
        });
        mUpgradeSheet.show();
    }

    private void hideUpgradeDialog() {
        if (mUpgradeSheet != null) {
            mUpgradeSheet.dismiss();
        }

        if (mNoWifiDialog != null) {
            mNoWifiDialog.dismiss();
        }
    }

    private void continueToUpgrade() {
        if (!NetworkHelper.isWifiAvailable()) {
            //bug: LEUI-23559
            if (UpdateUtil.mIsAppAllDownload) {
                sendMsgToService(UpdateConstant.UPDATE_NOW_FROM_CLIENT);
            } else {
                showNoWifiDialog(mBaseActivity);
            }
            return;
        }
        sendMsgToService(UpdateConstant.UPDATE_NOW_FROM_CLIENT);
    }

    private void exitCurrentApplication() {
        sendMsgToService(UpdateConstant.CANCEL_ALL_NOTIFICATION);
        reset();
        mBaseActivity.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void showNoWifiDialog(final Context context) {
        if(mUpgradeSheet != null && mUpgradeSheet.isShowing()) {
            mUpgradeSheet.dismiss();
        }
        String continueUpgrade = context.getString(R.string.continue_upgrade);
        String exitApplication = context.getString(R.string.wallet_exit_application);
        String downloadUnderWifi = context.getString(R.string.download_under_wifi);
        String downloading = context.getString(R.string.in_downloading);

        String[] dialogButtons = null;
        if (UpdateUtil.mIsForceUpdate == true) {
            dialogButtons = new String[] {UpdateUtil.mApkDownloading ? downloading : continueUpgrade, exitApplication};
        } else {
            dialogButtons = new String[] {continueUpgrade, downloadUnderWifi};
        }
        if (mNoWifiDialog == null) {
            mNoWifiDialog = new LeBottomSheet(context);
            mNoWifiDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!NetworkHelper.isNetworkAvailable()) {
                                Toast.makeText(mBaseActivity,context.getString(R.string.mobile_prompt_net_connection_fail),Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(UpdateUtil.mApkDownloading) {
                                Toast.makeText(mBaseActivity,mBaseActivity.getString(R.string.in_downloading),Toast.LENGTH_SHORT).show();
                                return;
                            }

                            UpdateUtil.mDownloadInCeller = true;
                            if(UpdateUtil.mIsForceUpdate) {
                                mNoWifiDialog.getBtn_confirm().setText(R.string.in_downloading);
                            } else {
                                mNoWifiDialog.dismiss();
                            }
                            //continue to upgrade
                            sendMsgToService(UpdateConstant.UPDATE_NOW_FROM_CLIENT);
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mNoWifiDialog.dismiss();
                            //bug: LEUI-23557
                            if (UpdateUtil.mIsForceUpdate == true) {
                                exitCurrentApplication();
                            }
                            //Downlaod when wifi is available
                            sendMsgToService(UpdateConstant.NOTIFY_LATER_FROM_CLIENT);
                        }
                    }, null,
                    dialogButtons,null,context.getString(R.string.no_wifi_update_notify),null,
                    new int[]{context.getResources().getColor(R.color.colorBtnBlue),context.getResources().getColor(R.color.black)},false);
            mNoWifiDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if(UpdateUtil.mIsForceUpdate) {
                        exitCurrentApplication();
                        return;
                    }
                    sendMsgToService(UpdateConstant.NOTIFY_LATER_FROM_CLIENT);
                }
            });
        }
        mNoWifiDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mIsNoWifiDialogShowing = false;
            }
        });
        mNoWifiDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mIsNoWifiDialogShowing = true;
            }
        });
        if (!mNoWifiDialog.isShowing()) {
            mNoWifiDialog.show();
        }
    }

    public void reset() {
        if (null != mSendToServiceMessenger && mIsUpgradeServiceRegistered) {
            mIsUpgradeServiceRegistered = false;
            if (mBaseActivity != null) {
                mBaseActivity.unbindService(mConnection);
            }
        }

        if (mUpgradeSheet != null) {
            mUpgradeSheet.setOnDismissListener(null);
            mUpgradeSheet.dismiss();
        }

        if (mNoWifiDialog != null) {
            mNoWifiDialog.setOnDismissListener(null);
            mNoWifiDialog.dismiss();
        }

    }

    public void requreyVersion() {
        if (mIsUpgradeServiceRegistered && mSendToServiceMessenger != null) {
            sendMsgToService(UpdateConstant.QUREY_NEW_VERSION_TO_SERVICE);
        }
    }
}
