package com.letv.wallet.common.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.letv.wallet.common.R;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.view.BlankPage;

/**
 * Created by liuliang on 15-12-29.
 */
public abstract class AccountBaseFragment extends BaseFragment {

    private static Toast mNetFailToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
            mNetFailToast = Toast.makeText(getContext(), getString(R.string.empty_no_network), Toast.LENGTH_SHORT);
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
                            AccountBaseFragment.this.getActivity(), null);
                    break;
            }
        }
    }
}
