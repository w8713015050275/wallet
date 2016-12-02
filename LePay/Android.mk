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

LOCAL_PACKAGE_NAME := LePay
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := \
    paylepaymm \
    payalisdk \
    com.letv.wallet.common

LOCAL_STATIC_JAVA_AAR_LIBRARIES:= \
    wallet-appcompat-v7 \
    wallet-design \
    wallet-recyclerview \
    lepay-lepaysdk \
    wallet-eui-support

LOCAL_JNI_SHARED_LIBRARIES := libentryexpro \
    libpl_droidsonroids_gif_sdk \
    libpl_droidsonroids_gif_surface_sdk \
    libuptsmaddon


LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages com.letv.wallet.common \
        --extra-packages android.support.v7.appcompat \
        --extra-packages android.support.design \
        --extra-packages android.support.v7.recyclerview \
        --extra-packages com.letv.lepaysdk \
        --extra-packages com.le.eui.support.widget

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)
LOCAL_MODULE        := libentryexpro
LOCAL_MODULE_CLASS  := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS   := optional
LOCAL_SRC_FILES_32  := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_SRC_FILES_64  := libs/arm64-v8a/$(LOCAL_MODULE).so
LOCAL_MULTILIB      := both
include $(BUILD_PREBUILT)
##################################################

##################################################
include $(CLEAR_VARS)
LOCAL_MODULE        := libpl_droidsonroids_gif_sdk
LOCAL_MODULE_CLASS  := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS   := optional
LOCAL_SRC_FILES_32  := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_SRC_FILES_64  := libs/arm64-v8a/$(LOCAL_MODULE).so
LOCAL_MULTILIB      := both
include $(BUILD_PREBUILT)
##################################################

##################################################
include $(CLEAR_VARS)
LOCAL_MODULE        := libpl_droidsonroids_gif_surface_sdk
LOCAL_MODULE_CLASS  := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS   := optional
LOCAL_SRC_FILES_32  := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_SRC_FILES_64  := libs/arm64-v8a/$(LOCAL_MODULE).so
LOCAL_MULTILIB      := both
include $(BUILD_PREBUILT)
##################################################

##################################################
include $(CLEAR_VARS)
LOCAL_MODULE        := libuptsmaddon
LOCAL_MODULE_CLASS  := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS   := optional
LOCAL_SRC_FILES_32  := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_SRC_FILES_64  := libs/arm64-v8a/$(LOCAL_MODULE).so
LOCAL_MULTILIB      := both
include $(BUILD_PREBUILT)
##################################################


############################################
# third party jar
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
        lepay-lepaysdk:libs/lepaysdk.aar \
        paylepaymm:libs/libammsdk.jar \
        payalisdk:libs/alipaySdk-20160825.jar


include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
