# Copyright 2012, The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

src_dirs := library/leui/src
res_dirs := res library/leui/res

LOCAL_MODULE := com.letv.wallet.common
LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_STATIC_JAVA_LIBRARIES := \
    wallet-support-v4 \
    letv-domain-common \
    libphonenumber \
    letv-gson-common \
    common_agnes_thin_client

LOCAL_STATIC_JAVA_AAR_LIBRARIES:= \
       wallet-appcompat-v7 \
       wallet-design \
       wallet-recyclerview \

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages android.support.v7.appcompat \
        --extra-packages android.support.design \
        --extra-packages android.support.v7.recyclerview \

LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_ENABLED := obfuscation
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_STATIC_LIBRARIES := libwabp

include $(BUILD_STATIC_JAVA_LIBRARY)

############################################
# third party jar
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
        wallet-support-v4:libs/android-support-v4.jar \
        wallet-appcompat-v7:libs/appcompat-v7-23.2.1.aar \
        wallet-design:libs/design-23.2.1.aar \
        wallet-recyclerview:libs/recyclerview-v7-23.2.1.aar \

LOCAL_PREBUILT_LIBS := libwabp:libs/armeabi/libwebpbackport.so

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
