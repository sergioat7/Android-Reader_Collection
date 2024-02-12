import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.services)
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.hilt)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
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
            storeFile = file(keystoreProperties.getProperty("keystore.storeFile"))
            storePassword = keystoreProperties.getProperty("keystore.storePassword")
            keyAlias = keystoreProperties.getProperty("keystore.keyAlias")
            keyPassword = keystoreProperties.getProperty("keystore.keyPassword")
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
            signingConfig = signingConfigs.getByName(name)
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
        buildConfig = true
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

    implementation(libs.bundles.navigation)
    implementation(libs.moshi)
    kapt(libs.moshi.kotlin.codegen)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.rx)
    implementation(libs.picasso)
    implementation(libs.materialratingbar)
    implementation(libs.bundles.room)
    kapt(libs.room.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.security.crypto)
    implementation(libs.android.chart)
    implementation(libs.tap.target.view)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.app.update.ktx)

    debugImplementation(libs.leak.canary)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}