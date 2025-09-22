# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ----------------------------------
# 基本Android保留规则
# ----------------------------------

# 保留所有Activity、Service、BroadcastReceiver和ContentProvider子类
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference



# 保留所有View及其子类，以及它们的方法
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# 保留所有枚举类及其values()和valueOf()方法
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Parcelable实现类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留R类及其内部类
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# 保留本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留自定义View的构造函数
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留回调方法
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

# ----------------------------------
# 支持库规则
# ----------------------------------

# Support库规则
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-dontwarn android.support.**

# AndroidX库规则
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }
-dontwarn androidx.**
-dontwarn com.google.android.material.**

# ----------------------------------
# 第三方库规则
# ----------------------------------

# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
# ----------------------------------
# OkHttp 规则（Retrofit 依赖）
# ----------------------------------
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# 保留 OkHttp 的拦截器
-keep class * implements okhttp3.Interceptor { *; }

## Retrofit2
## 保留 Retrofit 接口
#-keepclasseswithmembers class * {
#    @retrofit2.http.* <methods>;
#}
#
## 保留 Retrofit 的注解
#-keepattributes *Annotation*
#-keepclassmembers class * {
#    @retrofit2.http.* *;
#}
#
## 保留 Retrofit 的响应和请求模型
#-keep class * implements retrofit2.Call { *; }

# Gson 序列化/反序列化规则
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

# 保留被 @SerializedName 注解的字段
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}


# 如果需要保留特定的类型适配器
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**


# ----------------------------------
# RxJava 2/3 规则
# ----------------------------------
-dontwarn io.reactivex.**
-dontwarn rx.**
-keep class io.reactivex.** { *; }
-keep class rx.** { *; }
-keep class **$$Lambda$* { *; }

# 保留 RxJava 的调度器和操作符
-keep class io.reactivex.schedulers.** { *; }
-keep class io.reactivex.internal.schedulers.** { *; }
-keep class io.reactivex.internal.operators.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
# Firebase Remote Config
-keep class com.google.firebase.remoteconfig.** { *; }

# Firebase Crashlytics
-keep class com.crashlytics.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-dontwarn com.google.firebase.crashlytics.**

# Firebase Analytics
-keep class com.google.firebase.analytics.FirebaseAnalytics { *; }
-keep class com.google.android.gms.measurement.AppMeasurement { *; }
-keep class com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**


# ========== Google Firebase 消息服务 ==========
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Kotlin相关规则
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-keep class org.jetbrains.annotations.** { *; }

# 保留 Kotlin 协程相关类
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 保留Kotlin元数据
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keepattributes *Annotation*

# ----------------------------------
# 应用特定规则
# ----------------------------------

# 保留模型类（根据你的包名调整）
-keep class com.myphonecleaner.phonecleantool.model.** { *; }
-keepclassmembers class com.myphonecleaner.phonecleantool.model.** {
    *;
}


# ----------------------------------
# AndroidX Lifecycle 组件
# ----------------------------------
-keep class androidx.lifecycle.** { *; }
-keep class * implements androidx.lifecycle.DefaultLifecycleObserver

# 保留 ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
    <init>(...);
}

# 保留 LiveData 观察者相关方法
-keepclasseswithmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}

# 保留 LifecycleOwner 实现
-keep class * implements androidx.lifecycle.LifecycleOwner {
    <init>();
}







# 保留所有带有@Keep注解的类和方法
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# 保留日志代码（可选，如果不需要日志可以删除）
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ----------------------------------
# 通用优化规则
# ----------------------------------

# 代码优化选项
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-dontpreverify

# 保持泛型信息
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# 忽略警告（某些警告可以安全忽略）
-dontwarn android.webkit.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.api.client.**
-dontwarn com.google.ads.**
-dontwarn org.w3c.dom.**
-dontwarn javax.annotation.**
-dontwarn java.lang.invoke.**

# 保留行号信息（便于崩溃报告分析）
-keepattributes SourceFile,LineNumberTable

# ----------------------------------
# 资源压缩辅助规则（与shrinkResources true配合使用）
# ----------------------------------

# 保留使用了特殊方法的类（这些方法可能在运行时通过反射调用）
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# 保留JavaScript接口（如果使用了WebView）
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.myphonecleaner.phonecleantool.view.** { *; }


# ----------------------------------
# DataBinding 规则
# ----------------------------------
-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.DataBinderMapper {
    public *;
}

# 保留绑定类
-keep class **BR { *; }
-keep class **.databinding.* { *; }

# 保留资源文件中的类引用
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Google Play App Update 库
-keep class com.google.android.play.core.** { *; }
-keep class com.google.android.play.** { *; }
-keep class * implements com.google.android.play.core.appupdate.AppUpdateManager { public *; }
-keep class * implements com.google.android.play.core.install.InstallStateUpdatedListener { public *; }
-keep class com.google.android.play.core.appupdate.AppUpdateInfo { public *; }
-keep class com.google.android.play.core.appupdate.AppUpdateOptions { public *; }
-keep class com.google.android.play.core.appupdate.AppUpdateManagerFactory { public *; }
-keep class com.google.android.play.core.install.InstallState { public *; }
-keep class com.google.android.play.core.tasks.** { *; }
-keep class * implements com.google.android.play.core.tasks.OnSuccessListener { public *; }
-keep class * implements com.google.android.play.core.tasks.OnFailureListener { public *; }
-keep class * implements com.google.android.play.core.tasks.OnCompleteListener { public *; }

# ----------------------------------
# EventBus 规则
# ----------------------------------
-keep class org.greenrobot.eventbus.** { *; }
-dontwarn org.greenrobot.eventbus.**
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keepclassmembers class ** {
    public void onEvent(**);
    public void onEventMainThread(**);
    public void onEventBackgroundThread(**);
    public void onEventAsync(**);
}

# ----------------------------------
# BaseRecyclerViewAdapterHelper 规则
# ----------------------------------
-keep class com.chad.library.adapter.base.** { *; }
-keep class com.chad.library.adapter.base.module.** { *; }
-dontwarn com.chad.library.adapter.base.**

-keep class * extends com.chad.library.adapter.base.BaseQuickAdapter {
    public <init>(...);
    protected *;
    public *;
}

-keep class * extends com.chad.library.adapter.base.viewholder.BaseViewHolder {
    public <init>(...);
    public *;
}

-keepclassmembers class * extends com.chad.library.adapter.base.BaseQuickAdapter {
    protected void convert(com.chad.library.adapter.base.viewholder.BaseViewHolder, ...);
}

# ----------------------------------
# AndroidX 规则
# ----------------------------------
-keep class androidx.recyclerview.widget.RecyclerView { *; }
-keep class androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }
-keep class androidx.recyclerview.widget.RecyclerView$Adapter { *; }

-keep class com.github.chrisbanes.photoview.** { *; }


-keep class com.makeramen.roundedimageview.** { *; }

# 保留 DataModel 相关的类及其公共成员（包括 getter/setter），以确保序列化正常工作
-keep class com.thinkingdata.analytics.** { *; }
-keep class cn.thinkingdata.android.** { *; }


# ========== 通用规则 ==========
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod

# ========== Google Play Services ==========
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }


# ========== Singular SDK ==========
-keep class com.singular.sdk.** { *; }
-keep class com.singular.** { *; }
-keepclassmembers class com.singular.sdk.* { *; }

# ========== AppLovin ==========
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**
-keep class com.applovin.sdk.** { *; }

# ==========  mediation 适配器 ==========
-keep class com.applovin.mediation.** { *; }
-keep class com.google.ads.mediation.** { *; }




# ========== 第三方广告网络 ==========
# Facebook
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**

# Unity Ads
-keep class com.unity3d.ads.** { *; }
-dontwarn com.unity3d.ads.**

# IronSource
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**

# Vungle
-keep class com.vungle.** { *; }
-dontwarn com.vungle.**

# Chartboost
-keep class com.chartboost.** { *; }
-dontwarn com.chartboost.**

# Fyber
-keep class com.fyber.** { *; }
-dontwarn com.fyber.**

# Mintegral
-keep class com.mbridge.** { *; }
-dontwarn com.mbridge.**

# ByteDance (Pangle)
-keep class com.bytedance.** { *; }
-dontwarn com.bytedance.**

# InMobi
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**

# Mintegral
-keep class com.mintegral.** { *; }
-dontwarn com.mintegral.**







