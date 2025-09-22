import org.gradle.kotlin.dsl.annotationProcessor

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

android {
    namespace = "com.mycleaner.phonecleantool"
    compileSdk = 36

    packagingOptions {
        merge ("META-INF/DEPENDENCIES")
    }
    defaultConfig {
        applicationId = "com.mycleaner.phonecleantool"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file("cleanboost.jks")
            storePassword ="cxjc2025"
            keyAlias = "key0"
            keyPassword = "key02025"
        }
    }



    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable=false
            isMinifyEnabled = true
            isShrinkResources=true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
//        debug {
//            signingConfig = signingConfigs.getByName("release")
//            isDebuggable=false
//            isMinifyEnabled = true
//            isShrinkResources=true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }

}

dependencies {
    implementation(mapOf("name" to "trustlook_cleanjunk_sdk_release_3.0.4.20240711", "ext" to "aar"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    implementation(libs.androidx.constraintlayout)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //添加Gson依赖
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
//Retrofit
//    api(libs.logging.interceptor)
//    api(libs.retrofit)
    api(libs.converter.gson)
//    api(libs.adapter.rxjava3)
//    implementation(libs.rxandroid)
//    implementation(libs.rxjava)


    // 下拉刷新
    implementation(libs.refresh.layout.kernel)
    implementation(libs.refresh.header.classics)
    implementation(libs.refresh.header.falsify)
    implementation(libs.baserecyclerviewadapterhelper)
    api(libs.androidx.lifecycle.extensions)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.livedata)
    implementation(libs.eventbus)
    implementation(libs.androidx.databinding.runtime)
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
   implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-crashlytics-ndk")

    implementation("com.airbnb.android:lottie:5.2.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.material:material:1.6.0")
    implementation("cn.thinkingdata.android:ThinkingAnalyticsSDK:3.2.2")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.android.gms:play-services-ads:24.5.0")
    implementation("com.singular.sdk:singular_sdk:12.9.1")
    implementation("com.applovin:applovin-sdk:+")
    implementation("com.applovin.mediation:bigoads-adapter:+")
    implementation("com.applovin.mediation:chartboost-adapter:+")
    implementation("com.google.android.gms:play-services-base:16.1.0")
    implementation("com.applovin.mediation:fyber-adapter:+")
    implementation("com.applovin.mediation:google-ad-manager-adapter:+")
    implementation("com.applovin.mediation:google-adapter:+")
    implementation("com.applovin.mediation:inmobi-adapter:+")
    implementation("com.applovin.mediation:ironsource-adapter:+")
    implementation("com.applovin.mediation:vungle-adapter:+")
    implementation("com.applovin.mediation:facebook-adapter:+")
    implementation("com.applovin.mediation:mintegral-adapter:+")
    implementation("com.applovin.mediation:bytedance-adapter:+")
    implementation("com.applovin.mediation:unityads-adapter:+")
    implementation("com.google.ads.mediation:applovin:13.3.1.1")
    implementation("com.google.ads.mediation:chartboost:9.9.2.0")
    implementation("com.google.ads.mediation:fyber:8.3.8.0")
    implementation("com.google.ads.mediation:inmobi:10.8.7.0")
    implementation("com.google.ads.mediation:ironsource:8.10.0.0")
    implementation("com.google.ads.mediation:vungle:7.5.1.0")
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.ads.mediation:mintegral:16.9.91.0")
    implementation("com.google.ads.mediation:pangle:7.5.0.2.0")
    implementation("com.unity3d.ads:unity-ads:4.16.0")
    implementation("com.google.ads.mediation:unity:4.16.0.0")























}