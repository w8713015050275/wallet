package com.letv.wallet.account.aidl.v1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lijunying on 16-12-26.
 */

public class BankAvailableInfo implements Parcelable {

    public BankInfo bankList ;

    public static class BankInfo implements Parcelable {

        public long rank ; //序号
        public String name ; // 银行名称
        public String sn ; // 编号
        public String icon ; // 银行图标

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.rank);
            dest.writeString(this.name);
            dest.writeString(this.sn);
            dest.writeString(this.icon);
        }

        public BankInfo() {
        }

        protected BankInfo(Parcel in) {
            this.rank = in.readLong();
            this.name = in.readString();
            this.sn = in.readString();
            this.icon = in.readString();
        }

        public static final Parcelable.Creator<BankInfo> CREATOR = new Parcelable.Creator<BankInfo>() {
            @Override
            public BankInfo createFromParcel(Parcel source) {
                return new BankInfo(source);
            }

            @Override
            public BankInfo[] newArray(int size) {
                return new BankInfo[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.bankList, flags);
    }

    public BankAvailableInfo() {
    }

    protected BankAvailableInfo(Parcel in) {
        this.bankList = in.readParcelable(BankInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<BankAvailableInfo> CREATOR = new Parcelable.Creator<BankAvailableInfo>() {
        @Override
        public BankAvailableInfo createFromParcel(Parcel source) {
            return new BankAvailableInfo(source);
        }

        @Override
        public BankAvailableInfo[] newArray(int size) {
            return new BankAvailableInfo[size];
        }
    };
}
