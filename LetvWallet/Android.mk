# Copyright 2014 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

wallet_common_dir := ../LetvWalletCommon

src_dirs := src $(wallet_common_dir)/src
res_dirs := res $(wallet_common_dir)/res $(wallet_common_dir)/library/leui/res

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

#打印当前版本
$(warning "this version is $(TARGET_CARRIER)") 

LOCAL_PACKAGE_NAME := LetvWallet
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := \
    lepaymm \
    alisdk \
    alisec \
    aliutdid \
    com.letv.wallet.common \
    letv.push.sdk

LOCAL_STATIC_JAVA_AAR_LIBRARIES:= \
       wallet-appcompat-v7 \
       wallet-design \
       wallet-eui-support \
       wallet-recyclerview

LOCAL_STATIC_LIBRARIES := libwabp

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages com.letv.wallet.common \
        --extra-packages android.support.v7.appcompat \
        --extra-packages android.support.design \
        --extra-packages android.support.v7.recyclerview \
        --extra-packages com.le.eui.support.widget

LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_ENABLED := full
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#Star Recommend start
LOCAL_RESOURCE_DIR +=$ \
    vendor/letv/frameworks/sdk/common/res \
    frameworks/support/v7/cardview/res

LOCAL_SRC_FILES += $(call all-java-files-under, ../../../../vendor/letv/frameworks/sdk/common/src)

ifeq (1,$(strip $(shell expr $(PLATFORM_VERSION) \>= 6.0)))
    LOCAL_JAVA_LIBRARIES := org.apache.http.legacy.boot
endif
LOCAL_STATIC_JAVA_LIBRARIES += \
    android-support-v7-cardview \

LOCAL_AAPT_FLAGS += \
    --extra-packages com.letv.leui.common \
    --extra-packages android.support.v7.cardview
#Star Recommend end

LOCAL_JNI_SHARED_LIBRARIES := libwallet

LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

############################################
# third party jar
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
        lepaymm:libs/libammsdk.jar \
        alisdk:libs/alipaysdk.jar \
        alisec:libs/alipaysecsdk.jar \
        aliutdid:libs/alipayutdid.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
