import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services") version "4.3.15"
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val appName = "aragones.sergio.readercollection"

val versionMajor = 2
val versionMinor = 5
val versionPatch = 1
val versionBuild = 0 // bump for dogfood builds, public betas, etc.

android {

    namespace = appName
    compileSdk = 34

    signingConfigs {
        create("release") {
            if (project.hasProperty("READER_COLLECTION_STORE_FILE")) {
                storeFile = file(keystoreProperties["READER_COLLECTION_STORE_FILE"] as String)
                storePassword = keystoreProperties["READER_COLLECTION_STORE_PASSWORD"] as String
                keyAlias = keystoreProperties["READER_COLLECTION_KEY_ALIAS"] as String
                keyPassword = keystoreProperties["READER_COLLECTION_KEY_PASSWORD"] as String
            }
        }
    }

    defaultConfig {

        applicationId = appName
        minSdk = 23
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionCode = versionMajor * 100000 + versionMinor * 1000 + versionPatch * 10 + versionBuild
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        resourceConfigurations += listOf("en", "es")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["appName"] = "@string/app_name"
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
            manifestPlaceholders["appName"] = "Reader Collection - Pre"
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.fragment.ktx)
    implementation(libs.material)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.dynamic.features.fragment)

    implementation(libs.moshi)
    kapt(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)
    implementation(libs.retrofit.adapter.rxjava)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.rxjava)
    implementation(libs.rxkotlin)
    implementation(libs.rxandroid)

    implementation(libs.picasso)

    implementation(libs.materialratingbar)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.rxjava)
    implementation(libs.room.rxjava3.bridge)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.security.crypto)

    implementation(libs.android.chart)

    implementation(libs.tap.target.view)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.app.update.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}