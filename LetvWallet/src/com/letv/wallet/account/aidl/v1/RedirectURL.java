package com.letv.wallet.account.aidl.v1;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by lijunying on 17-1-10.
 */

public class RedirectURL implements Parcelable {
    public String set_pay_pwd;
    public String mod_pay_pwd;
    public String reset_pay_pwd;
    public String add_card;
    public String card_list;
    public String lelehua_active;
    public String lelehua_home;
    public String lelehua_noactive;
    public String lelehua_bill_list;
    public String user_grade;
    public String sso_bind_mobile;

    public String getUrl(String jType) {
        if (TextUtils.isEmpty(jType)) {
            return null;
        }
        switch (jType) {
            case AccountConstant.JTYPE_SET_PAY_PWD:
                return set_pay_pwd;

            case AccountConstant.JTYPE_MOD_PAY_PWD:
                return mod_pay_pwd;

            case AccountConstant.JTYPE_RESET_PAY_PWD:
                return reset_pay_pwd;

            case AccountConstant.JTYPE_ADD_CARD:
                return add_card;

            case AccountConstant.JTYPE_CARD_LIST:
                return card_list;

            case AccountConstant.JTYPE_LELEHUA_NOACTIVE:
                return lelehua_noactive;

            case AccountConstant.JTYPE_LELEHUA_ACTIVE:
                return lelehua_active;

            case AccountConstant.JTYPE_LELEHUA_HOME:
                return lelehua_home;

            case AccountConstant.JTYPE_LELEHUA_BILL_LIST:
                return lelehua_bill_list;

            case AccountConstant.JTYPE_USER_GRADE:
                return user_grade;

            case AccountConstant.JTYPE_SSO_BIND_MOBILE:
                return sso_bind_mobile;

            default:
                return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.set_pay_pwd);
        dest.writeString(this.mod_pay_pwd);
        dest.writeString(this.reset_pay_pwd);
        dest.writeString(this.add_card);
        dest.writeString(this.card_list);
        dest.writeString(this.lelehua_active);
        dest.writeString(this.lelehua_home);
        dest.writeString(this.lelehua_noactive);
        dest.writeString(this.lelehua_bill_list);
        dest.writeString(this.user_grade);
        dest.writeString(this.sso_bind_mobile);
    }

    public RedirectURL() {
    }

    protected RedirectURL(Parcel in) {
        this.set_pay_pwd = in.readString();
        this.mod_pay_pwd = in.readString();
        this.reset_pay_pwd = in.readString();
        this.add_card = in.readString();
        this.card_list = in.readString();
        this.lelehua_active = in.readString();
        this.lelehua_home = in.readString();
        this.lelehua_noactive = in.readString();
        this.lelehua_bill_list = in.readString();
        this.user_grade = in.readString();
        this.sso_bind_mobile = in.readString();
    }

    public static final Creator<RedirectURL> CREATOR = new Creator<RedirectURL>() {
        @Override
        public RedirectURL createFromParcel(Parcel source) {
            return new RedirectURL(source);
        }

        @Override
        public RedirectURL[] newArray(int size) {
            return new RedirectURL[size];
        }
    };
}
