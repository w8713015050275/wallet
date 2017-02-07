package com.letv.wallet.account.aidl.v1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lijunying on 16-12-26.
 */

public class CardbinAvailableInfo implements Parcelable {

    public String bankName ;
    public String bankNo;
    public String cardBin;
    public String cardName;
    public String cardType;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bankName);
        dest.writeString(this.bankNo);
        dest.writeString(this.cardBin);
        dest.writeString(this.cardName);
        dest.writeString(this.cardType);
    }

    public CardbinAvailableInfo() {
    }

    protected CardbinAvailableInfo(Parcel in) {
        this.bankName = in.readString();
        this.bankNo = in.readString();
        this.cardBin = in.readString();
        this.cardName = in.readString();
        this.cardType = in.readString();
    }

    public static final Creator<CardbinAvailableInfo> CREATOR = new Creator<CardbinAvailableInfo>() {
        @Override
        public CardbinAvailableInfo createFromParcel(Parcel source) {
            return new CardbinAvailableInfo(source);
        }

        @Override
        public CardbinAvailableInfo[] newArray(int size) {
            return new CardbinAvailableInfo[size];
        }
    };
}
