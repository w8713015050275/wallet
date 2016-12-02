LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# This is the target being built.
LOCAL_MODULE:= libwallet

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
                    $(LOCAL_PATH)/src \

#LOCAL_CFLAGS += -DWDEBUG

LOCAL_CPP_EXTENSION := .cpp

# All of the source files that we will compile.
LOCAL_SRC_FILES := src/com_letv_walletbiz_base_http_LeSignature.cpp


# Also need the JNI headers.
#LOCAL_C_INCLUDES += libcore/include 
    
LOCAL_SHARED_LIBRARIES := \
    libicuuc \
    libnativehelper \
    libcutils \
    libutils \
    liblog \
    libcrypto
    
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
