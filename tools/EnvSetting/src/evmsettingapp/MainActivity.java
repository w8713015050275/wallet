package com.letv.wallet.evmsettingapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String WALLET_PACKAGE = "com.letv.walletbiz";
    public static final String LEPAY_PACKAGE = "com.letv.wallet";

    public static final String WALLET_PAY = "wallet_pay";
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
        initData();
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
        sp.edit().putBoolean(key, value).commit();
    }

    private void killWallet(String packageName) {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(packageName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.kill_error_msg) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
