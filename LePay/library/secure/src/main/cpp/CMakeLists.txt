
#
# Copyright (C) The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
MESSAGE('编译开始了，不要停'+${CMAKE_BUILD_TYPE}+${CMAKE_SOURCE_DIR})
cmake_minimum_required(VERSION 3.4.1)

# configure import libs
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../../../tools/externallib/)

MESSAGE(${distribution_DIR})


########################################
add_library(lib_ssl SHARED IMPORTED)
set_target_properties(lib_ssl PROPERTIES IMPORTED_LOCATION
    ${distribution_DIR}/ssl/lib/${ANDROID_ABI}/libssl.so)

add_library(lib_crypto SHARED IMPORTED)
set_target_properties(lib_crypto PROPERTIES IMPORTED_LOCATION
    ${distribution_DIR}/ssl/lib/${ANDROID_ABI}/libcrypto.so)
###########################################

# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

add_library(openssl-jni SHARED
            openssl.cpp)

target_include_directories(openssl-jni PRIVATE
                           ${distribution_DIR}/ssl/include
                           )

target_link_libraries(openssl-jni
                      android
                      lib_crypto
                      lib_ssl
                      log)
