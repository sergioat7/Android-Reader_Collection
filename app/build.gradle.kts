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
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val appName = "aragones.sergio.readercollection"

val versionMajor = 2
val versionMinor = 6
val versionPatch = 1
val versionBuild = 0 // bump for dogfood builds, public betas, etc.

android {

    namespace = appName
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("readercollection-keystore.jks")
            storePassword = keystoreProperties.getProperty("keystore.storePassword")
            keyAlias = keystoreProperties.getProperty("keystore.keyAlias")
            keyPassword = keystoreProperties.getProperty("keystore.keyPassword")
        }
    }

    defaultConfig {

        applicationId = appName
        minSdk = 23
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionCode = versionMajor * 100000 + versionMinor * 1000 + versionPatch * 10 + versionBuild
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        resourceConfigurations += listOf("en", "es")

        buildConfigField("String", "API_KEY", keystoreProperties.getProperty("api.key"))
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
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {

    implementation(project(":core:database"))
    implementation(project(":core:util"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.rx)
    implementation(libs.room.runtime)
    implementation(libs.room.rxjava)
    implementation(libs.room.rxjava3.bridge)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation(libs.security.crypto)
    implementation(libs.android.chart)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.app.update.ktx)

    debugImplementation(libs.leak.canary)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil)

    implementation(libs.lottie)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)
}