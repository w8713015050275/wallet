package com.letv.walletbiz.movie.pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.PayResultActivity;
import com.letv.walletbiz.movie.MovieTicketConstant;
import com.letv.walletbiz.movie.activity.MovieOrderDetailActivity;
import com.letv.walletbiz.movie.activity.MovieTicketActivity;
import com.letv.walletbiz.movie.beans.MovieProduct;
import com.letv.walletbiz.movie.widget.MovieProductBrief;

/**
 * Created by linquan on 15-12-9.
 */
public class MoviePayResultAdapter implements PayResultActivity.PayResultAdapter {
    MovieProduct mProduct;


    public MoviePayResultAdapter(MovieProduct product) {
        mProduct = product;

    }

    @Override
    public int getTitle() {
        return R.string.movie_order_view_label;
    }

    @Override
    public int getStatus() {
        return mProduct.getPayResult();
    }

    @Override
    public String getCost() {
        return mProduct.getPrice();
    }

    @Override
    public View createContentView(Context context, ViewGroup parent) {
        MovieProductBrief v = new MovieProductBrief(context);
        v.setData(mProduct);
        return v;
    }

    private boolean isPaid() {
        return getStatus() == 1;
    }

    @Override
    public int getActionLabel() {
        return isPaid() ? R.string.label_order_desc : R.string.pay_now;
    }

    @Override
    public void onAction(Context context) {
        if (!isPaid()) {
            mProduct.pay(context);
        } else {
            if (mProduct.getMovieOrder() != null) {
                Intent intent = new Intent(context, MovieOrderDetailActivity.class);
                intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_ORDER_NUM, mProduct.getMovieOrder().getMovieOrderNo());
                context.startActivity(intent);
            }
        }

    }

    @Override
    public void onBack(Activity activity) {
        if (activity != null) {
            activity.finish();
            Intent intent = new Intent(activity, MovieTicketActivity.class);
            intent.putExtra(MovieTicketConstant.EXTRA_MOVIE_TICKET_TAB_ID, MovieTicketActivity.ID_MOVIE);
            activity.startActivity(intent);
        }
    }


}
