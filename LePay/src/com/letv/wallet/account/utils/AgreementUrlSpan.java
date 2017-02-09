package com.letv.wallet.account.utils;

import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.letv.wallet.PayApplication;
import com.letv.wallet.R;
import com.letv.wallet.account.activity.AccountWebActivity;
import com.letv.wallet.common.util.CommonConstants;

/**
 * Created by lijunying on 17-2-10.
 */

public class AgreementUrlSpan extends ClickableSpan {
    private String url;

    public AgreementUrlSpan(String url){
        this.url = url;
    }

    @Override
    public void onClick(View widget) {
        Intent intent = new Intent(widget.getContext(), AccountWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL, url);
        widget.getContext().startActivity(intent);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(PayApplication.getApplication().getResources().getColor(R.color.account_primary_color));
        ds.setUnderlineText(false);
    }
}
