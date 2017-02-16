package com.letv.wallet.account.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.shared.widget.BorderedCircleImageView;
import com.letv.wallet.R;
import com.letv.wallet.account.aidl.v1.AccountConstant;
import com.letv.wallet.account.aidl.v1.AccountInfo.CardBin;

import org.xutils.xmain;

/**
 * Created by lijunying on 17-2-10.
 */
public class CardListAdapter extends RecyclerView.Adapter {

    private Context mContext;

    private CardBin[] mData;

    public CardListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(mContext).inflate(R.layout.account_card_list_item, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        CardBin cardBin = mData[position];
        if (cardBin != null) {
            if (!TextUtils.isEmpty(cardBin.background)) {
                itemHolder.itemView.getBackground().setTint(Color.parseColor(cardBin.background));
            }else{
                itemHolder.itemView.getBackground().setTint(mContext.getResources().getColor(R.color.account_primary_color));
            }
            xmain.image().bind(itemHolder.mBankIcon, cardBin.bankIcon);
            itemHolder.mBankName.setText(cardBin.bankCardName);
            itemHolder.mCardType.setText(getCardType(cardBin.cardType));
            itemHolder.mCardNum.setText(cardBin.bankCard);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.length;
    }


    public void setData(CardBin[] data) {
        mData = data;
        notifyDataSetChanged();
    }

    private String getCardType(String type) {
        int resId = R.string.account_card_type_other;
        if (!TextUtils.isEmpty(type)) {
            switch (type) {
                case AccountConstant.CARD_BIN_TYPE_DEBIT:
                    resId = R.string.account_card_type_debit;
                    break;

                case AccountConstant.CARD_BIN_TYPE_CREDIT:
                    resId = R.string.account_card_type_credit;
                    break;

                case AccountConstant.CARD_BIN_TYPE_PASSBOOK:
                    resId = R.string.account_card_type_passbook;
                    break;
            }
        }
        return mContext.getResources().getString(resId);
    }


    static class ItemHolder extends RecyclerView.ViewHolder {

        BorderedCircleImageView mBankIcon;
        TextView mBankName;
        TextView mCardType;
        TextView mCardNum;

        public ItemHolder(View itemView) {
            super(itemView);
            mBankIcon = (BorderedCircleImageView) itemView.findViewById(R.id.imgBankIcon);
            mBankName = (TextView) itemView.findViewById(R.id.tvBankName);
            mCardType = (TextView) itemView.findViewById(R.id.tvCardType);
            mCardNum = (TextView) itemView.findViewById(R.id.tvCardNum);
        }
    }

}
