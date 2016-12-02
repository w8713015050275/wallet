package com.letv.wallet.common.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.letv.wallet.common.R;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by liuliang on 16-3-25.
 */
public class AccountBaseActivity extends BaseFragmentActivity {

    private static Toast mNetFailToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return true;
    }

    @Override
    public BlankPage showBlankPage(int state) {
        if (state == BlankPage.STATE_NO_LOGIN) {
            return showNoLoginBlankPage();
        }
        return super.showBlankPage(state);
    }

    /**
     * @param state
     * @param iconViewClickListener 网络异常时, iconView处理点击刷新事件
     * @return
     */
    @Override
    public BlankPage showBlankPage(int state, View.OnClickListener iconViewClickListener) {
        if (state == BlankPage.STATE_NO_LOGIN && iconViewClickListener == null) {
            return showNoLoginBlankPage();
        }
        return super.showBlankPage(state, iconViewClickListener);
    }

    public BlankPage showNoLoginBlankPage() {
        BlankPage blankPage = showBlankPage();
        blankPage.setPageState(BlankPage.STATE_NO_LOGIN, null);
        blankPage.getPrimaryBtn().setOnClickListener(new BlankPageOnclickListener());
        return blankPage;
    }

    protected void promptNoNetWork() {
        if (mNetFailToast == null) {
            mNetFailToast = Toast.makeText(this, getString(R.string.empty_no_network), Toast.LENGTH_SHORT);
        }
        mNetFailToast.show();
    }

    private class BlankPageOnclickListener implements View.OnClickListener {

        public static final int GOLOGIN = 1;
        private int tag = GOLOGIN;

        public BlankPageOnclickListener() {

        }

        @Override
        public void onClick(View v) {
            switch (tag) {
                case GOLOGIN:
                    if (!isNetworkAvailable()) {
                        promptNoNetWork();
                        return;
                    }
                    AccountHelper.getInstance().loginLetvAccountIfNot(
                            AccountBaseActivity.this, null);
                    break;
            }
        }

    }
}
