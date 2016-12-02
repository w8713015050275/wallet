//
// Created by linquan on 16-4-15.
//

#ifndef ANDROID_LOG_H
#define ANDROID_LOG_H
#include <android/log.h>

#define TAG "Wallet_N" // 这个是自定义的LOG的标识

#ifdef WDEBUG
    #define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
    #define ALOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
    #define ALOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
    #define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
    #define ALOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型
#else
    #define ALOGD(...)
    #define ALOGI(...)
    #define ALOGW(...)
    #define ALOGE(...)
    #define ALOGF(...)
#endif

#endif //ANDROID_LOG_H
