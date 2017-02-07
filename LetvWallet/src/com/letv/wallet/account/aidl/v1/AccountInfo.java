package com.letv.wallet.account.aidl.v1;

import android.os.Parcel;
import android.os.Parcelable;

import com.letv.wallet.common.http.beans.LetvBaseBean;

/**
 * Created by lijunying on 17-1-10.
 */

public class AccountInfo implements Parcelable {
    public BasicAccount basic;
    public LeLeHuaAccount lelehua;
    private String card;
    private CardBin[] cardList;
    public Vip[] vip;
    public Tips[] tips;

    /* 基本账户 */
    public static class BasicAccount implements LetvBaseBean, Parcelable {

        /**
         * 集团用户UID
         */
        public String uid;

        /**
         * sso头像的原始返回。每个尺寸的头像用英文逗号分隔
         */
        public String avatar;

        public String mobile;

        public String realName;

        public String idCardNo;

        public String memberName;

        /**
         * 会员状态
         * 未激活: {@link AccountConstant#BASIC_ACCOUNT_STATE_NON_ACTIVATED}
         * 正常: {@link AccountConstant#BASIC_ACCOUNT_STATE_NORMAL}
         * 休眠: {@link AccountConstant#BASIC_ACCOUNT_STATE_FREEZING}
         * 注销: {@link AccountConstant#BASIC_ACCOUNT_STATE_CLOSED}
         */
        public int status;

        /**
         * 锁状态
         * 解锁: {@link AccountConstant#BASIC_ACCOUNT_LOCK_STATE_UNLOCKED}
         * 锁定: {@link AccountConstant#BASIC_ACCOUNT_LOCK_STATE_LOCKED}
         */
        public int lockStatus;

        /**
         * 密码状态
         * 已设置: {@link AccountConstant#BASIC_ACCOUNT_PWD_STATE_SETTLED}
         * 未设置: {@link AccountConstant#BASIC_ACCOUNT_PWD_STATE_UNSETTLED}
         */
        public String pwdStatus;

        /**
         * 实名状态
         * 已实名: {@link AccountConstant#BASIC_ACCOUNT_VERIFY_STATE_AUTHENTICATED}
         * 未实名: {@link AccountConstant#BASIC_ACCOUNT_VERIFY_STATE_NON_AUTHENTICATED}
         */
        public String verifyStatus;

        public long createTime;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.uid);
            dest.writeString(this.avatar);
            dest.writeString(this.mobile);
            dest.writeString(this.realName);
            dest.writeString(this.idCardNo);
            dest.writeString(this.memberName);
            dest.writeInt(this.status);
            dest.writeInt(this.lockStatus);
            dest.writeString(this.pwdStatus);
            dest.writeString(this.verifyStatus);
            dest.writeLong(this.createTime);
        }

        public BasicAccount() {
        }

        protected BasicAccount(Parcel in) {
            this.uid = in.readString();
            this.avatar = in.readString();
            this.mobile = in.readString();
            this.realName = in.readString();
            this.idCardNo = in.readString();
            this.memberName = in.readString();
            this.status = in.readInt();
            this.lockStatus = in.readInt();
            this.pwdStatus = in.readString();
            this.verifyStatus = in.readString();
            this.createTime = in.readLong();
        }

        public static final Creator<BasicAccount> CREATOR = new Creator<BasicAccount>() {
            @Override
            public BasicAccount createFromParcel(Parcel source) {
                return new BasicAccount(source);
            }

            @Override
            public BasicAccount[] newArray(int size) {
                return new BasicAccount[size];
            }
        };
    }

    /* 乐乐花账户 */
    public static class LeLeHuaAccount implements LetvBaseBean, Parcelable {
        public int active_status;
        public float credit_limit;
        public float used_limit;
        public float available_limit;
        public float owe_amount;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.active_status);
            dest.writeFloat(this.credit_limit);
            dest.writeFloat(this.used_limit);
            dest.writeFloat(this.available_limit);
            dest.writeFloat(this.owe_amount);
        }

        public LeLeHuaAccount() {
        }

        protected LeLeHuaAccount(Parcel in) {
            this.active_status = in.readInt();
            this.credit_limit = in.readFloat();
            this.used_limit = in.readFloat();
            this.available_limit = in.readFloat();
            this.owe_amount = in.readFloat();
        }

        public static final Creator<LeLeHuaAccount> CREATOR = new Creator<LeLeHuaAccount>() {
            @Override
            public LeLeHuaAccount createFromParcel(Parcel source) {
                return new LeLeHuaAccount(source);
            }

            @Override
            public LeLeHuaAccount[] newArray(int size) {
                return new LeLeHuaAccount[size];
            }
        };
    }

    public static class CardBin implements LetvBaseBean, Parcelable {

        public long activateDate;

        public String bankCard; // 银行卡号

        public String bankCardName; // 银行名称

        public String bankMobile; // 手机号掩码

        public String bankNo; // 银行编号

        public String bankcardId; // 银行卡绑定ID

        /**
         * 卡属性
         * 对私: {@link AccountConstant#CARD_BIN_ATTRIBUTE_PRIVATE}
         * 对公（信用卡）: {@link AccountConstant#CARD_BIN_ATTRIBUTE_CORPORATE}
         */
        public String cardAttribute;

        /**
         * 卡类型
         * 借记: {@link AccountConstant#CARD_BIN_TYPE_DEBIT}
         * 贷记（信用卡）: {@link AccountConstant#CARD_BIN_TYPE_CREDIT}
         * 存折: {@link AccountConstant#CARD_BIN_TYPE_PASSBOOK}
         * 其它: {@link AccountConstant#CARD_BIN_TYPE_OTHER}
         */
        public String cardType;

        /**
         * 支付属性
         * 快捷: {@link AccountConstant#CARD_PAY_ATTRIBUTE_CORPORATE}
         */
        public String payAttribute;

        public String realName; // 持卡人姓名

        /**
         * 卡状态
         * 正常: {@link AccountConstant#CARD_STATE_NORMAL}
         * 锁定: {@link AccountConstant#CARD_STATE_LOCKED}
         */
        public int status;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.activateDate);
            dest.writeString(this.bankCard);
            dest.writeString(this.bankCardName);
            dest.writeString(this.bankMobile);
            dest.writeString(this.bankNo);
            dest.writeString(this.bankcardId);
            dest.writeString(this.cardAttribute);
            dest.writeString(this.cardType);
            dest.writeString(this.payAttribute);
            dest.writeString(this.realName);
            dest.writeInt(this.status);
        }

        public CardBin() {
        }

        protected CardBin(Parcel in) {
            this.activateDate = in.readLong();
            this.bankCard = in.readString();
            this.bankCardName = in.readString();
            this.bankMobile = in.readString();
            this.bankNo = in.readString();
            this.bankcardId = in.readString();
            this.cardAttribute = in.readString();
            this.cardType = in.readString();
            this.payAttribute = in.readString();
            this.realName = in.readString();
            this.status = in.readInt();
        }

        public static final Creator<CardBin> CREATOR = new Creator<CardBin>() {
            @Override
            public CardBin createFromParcel(Parcel source) {
                return new CardBin(source);
            }

            @Override
            public CardBin[] newArray(int size) {
                return new CardBin[size];
            }
        };
    }

    public static class Vip implements LetvBaseBean, Parcelable {
        public String title; //会员名称
        public int active; // 1-已激活，0-未激活
        public String name; // 会员标示

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.title);
            dest.writeInt(this.active);
            dest.writeString(this.name);
        }

        public Vip() {
        }

        protected Vip(Parcel in) {
            this.title = in.readString();
            this.active = in.readInt();
            this.name = in.readString();
        }

        public static final Creator<Vip> CREATOR = new Creator<Vip>() {
            @Override
            public Vip createFromParcel(Parcel source) {
                return new Vip(source);
            }

            @Override
            public Vip[] newArray(int size) {
                return new Vip[size];
            }
        };
    }

    public static class Tips implements LetvBaseBean, Parcelable {
        public String title;
        public int id;
        public String type;
        public String icon;
        public String jump_desc;
        public String jump_param;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.title);
            dest.writeInt(this.id);
            dest.writeString(this.type);
            dest.writeString(this.icon);
            dest.writeString(this.jump_desc);
            dest.writeString(this.jump_param);
        }

        public Tips() {
        }

        protected Tips(Parcel in) {
            this.title = in.readString();
            this.id = in.readInt();
            this.type = in.readString();
            this.icon = in.readString();
            this.jump_desc = in.readString();
            this.jump_param = in.readString();
        }

        public static final Creator<Tips> CREATOR = new Creator<Tips>() {
            @Override
            public Tips createFromParcel(Parcel source) {
                return new Tips(source);
            }

            @Override
            public Tips[] newArray(int size) {
                return new Tips[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.basic, flags);
        dest.writeParcelable(this.lelehua, flags);
        dest.writeString(this.card);
        dest.writeTypedArray(this.cardList, flags);
        dest.writeTypedArray(this.vip, flags);
        dest.writeTypedArray(this.tips, flags);
    }

    public AccountInfo() {
    }

    protected AccountInfo(Parcel in) {
        this.basic = in.readParcelable(BasicAccount.class.getClassLoader());
        this.lelehua = in.readParcelable(LeLeHuaAccount.class.getClassLoader());
        this.card = in.readString();
        this.cardList = in.createTypedArray(CardBin.CREATOR);
        this.vip = in.createTypedArray(Vip.CREATOR);
        this.tips = in.createTypedArray(Tips.CREATOR);
    }

    public static final Creator<AccountInfo> CREATOR = new Creator<AccountInfo>() {
        @Override
        public AccountInfo createFromParcel(Parcel source) {
            return new AccountInfo(source);
        }

        @Override
        public AccountInfo[] newArray(int size) {
            return new AccountInfo[size];
        }
    };
}
