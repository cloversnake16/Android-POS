# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/Works/Android/android-sdk/tools/proguard/proguard-android.txt
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

-dontwarn **
-dontobfuscate
-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-keepattributes SourceFile, LineNumberTable
-keepattributes JavascriptInterface
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Common
-keepnames class android.support.** { *; }
-keepnames class com.google.gson.** { *; }

# ButterKnife
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

# Apache
-keep public class org.apache.commons.io.**
-keep class org.apache.** { *; }
-dontnote org.apache.**

# Data transport
-keep class no.susoft.mobile.JSONSerializable
-keepnames class * implements no.susoft.mobile.JSONSerializable
-keepclassmembers class * implements no.susoft.mobile.JSONSerializable { <fields>; }
-keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
}

# JsInterfaces
-keep class no.susoft.mobile.pos.ui.fragment.utils.CashcountJSInterface
-keep class * implements no.susoft.mobile.pos.ui.fragment.utils.CashcountJSInterface
-keepclassmembers class no.susoft.mobile.pos.ui.fragment.utils.CashcountJSInterface {
    <methods>;
}

# Bank Terminals
-keep class no.point.paypoint.** { *; }
-keep class eu.nets.baxi.** { *; }
-keep class com.ingenico.** { *; }

# Printers
-keep class mf.org.apache.xerces.** { *; }
-keep class mf.javax.xml.** { *; }
-keep class jpos.** { *; }
-keep class com.bxl.** { *; }
-keep class com.starmicronics.** { *; }

##---------------Begin: Proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
##---------------End: proguard configuration for Gson  ----------