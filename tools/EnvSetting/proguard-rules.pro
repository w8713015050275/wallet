# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/huhb/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class * extends android.app.Activity
-keep class * extends android.app.Fragment
-keep class * extends android.app.Dialog
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider


#不混淆android　support包v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *;}


#不混淆android　support包v7
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }

-dontnote android.support.v7.**