package com.letv.wallet.evmsettingapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final String DEVELOP_URL = "https://test-wallet.scloud.letv.com";

    public static final String WALLET_PACKAGE = "com.letv.walletbiz";
    public static final String LEPAY_PACKAGE = "com.letv.wallet";

    public static final String WALLET_PAY = "wallet_pay";

    public static final String DEVELOP_URL_KEY = "develop_url";

    public static final String WALLET_KEY = "wallet";
    public static final String WALLETBIZ_KEY = "walletbiz";
    public static final boolean PRODUCT = false;
    public static final boolean DEVELOP = true;

    private RadioGroup rgWallet;
    private RadioGroup rgWalletbiz;
    private RadioButton developWallet, productWallet;
    private RadioButton developWalletbiz, productWalletbiz;
    private Button button;
    private TextView msgView;

    private SharedPreferences sp;

    private boolean walletEvm;
    private boolean walletbizEvm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        bindView();
        if(checkMainPermission(PERMISSIONS_REQUEST_CODE)){
            initData();
        }
    }

    private void findView() {
        rgWallet = (RadioGroup) findViewById(R.id.rg_wallet);
        rgWalletbiz = (RadioGroup) findViewById(R.id.rg_walletbiz);
        developWallet = (RadioButton) findViewById(R.id.develop_wallet);
        productWallet = (RadioButton) findViewById(R.id.product_wallet);
        developWalletbiz = (RadioButton) findViewById(R.id.develop_walletbiz);
        productWalletbiz = (RadioButton) findViewById(R.id.product_walletbiz);
        button = (Button) findViewById(R.id.button);
        msgView = (TextView) findViewById(R.id.msg_view);
    }

    private void bindView() {
        rgWallet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                killWallet(LEPAY_PACKAGE);
                switch (checkedId) {
                    case R.id.product_wallet:
                        saveDate(WALLET_KEY, PRODUCT);
                        break;
                    case R.id.develop_wallet:
                        saveDate(WALLET_KEY, DEVELOP);
                        saveDevelopUrl(DEVELOP_URL_KEY,DEVELOP_URL);
                        break;
                }
            }
        });
        rgWalletbiz.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                killWallet(WALLET_PACKAGE);
                switch (checkedId) {
                    case R.id.product_walletbiz:
                        saveDate(WALLETBIZ_KEY, PRODUCT);
                        break;
                    case R.id.develop_walletbiz:
                        saveDate(WALLETBIZ_KEY, DEVELOP);
                        saveDevelopUrl(DEVELOP_URL_KEY,DEVELOP_URL);
                        break;
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgView.setText("walletbiz evm " + sp.getBoolean(WALLETBIZ_KEY, true) + "\nwallet evm " + sp.getBoolean(WALLET_KEY, true));
            }
        });
    }

    private void initData() {
        sp = getSharedPreferences(WALLET_PAY, Context.MODE_PRIVATE);
        //默认测试环境
        walletEvm = sp.getBoolean(WALLET_KEY, PRODUCT);
        walletbizEvm = sp.getBoolean(WALLETBIZ_KEY, PRODUCT);
        if (!walletbizEvm) {
            productWalletbiz.setChecked(true);
        } else {
            developWalletbiz.setChecked(true);
        }
        if (!walletEvm) {
            productWallet.setChecked(true);
        } else {
            developWallet.setChecked(true);
        }
    }

    private void saveDate(String key, boolean value) {
        if(sp != null){
            sp.edit().putBoolean(key, value).commit();
        }
    }

    private void saveDevelopUrl(String key, String url){
        if(sp != null){
            if(TextUtils.isEmpty(sp.getString(DEVELOP_URL_KEY,null))){
                sp.edit().putString(key,url).commit();
            }
        }
    }

    private void killWallet(String packageName){
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(packageName);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.kill_error_msg)+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    PackageManager packageManager = this.getPackageManager();
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            try {
                                PermissionInfo info = packageManager.getPermissionInfo(permissions[i], 0);
                                if(info!=null && Manifest.permission.KILL_BACKGROUND_PROCESSES.equals(info.toString())){
                                    finish();
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    public boolean checkMainPermission(int requestCode) {
        boolean result = true;
        String[] permissionList = new String[]{Manifest.permission.KILL_BACKGROUND_PROCESSES};
        ArrayList<String> deniedPermissionList = new ArrayList<String>();
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
                deniedPermissionList.add(permission);
            }
        }
        Log.e("MainActivity", "deniedPermissionList size "+deniedPermissionList.size());
        if (deniedPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, deniedPermissionList.toArray(new String[0]), requestCode);
        }
        return result;
    }
}
