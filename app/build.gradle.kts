import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("androidx.navigation.safeargs.kotlin")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val appName = "aragones.sergio.readercollection"

val versionMajor = 2
val versionMinor = 8
val versionPatch = 3
val versionBuild = 0 // bump for dogfood builds, public betas, etc.

android {

    namespace = appName
    compileSdk = libs.versions.sdk.compile.get().toInt()

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
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionCode = versionMajor * 100000 + versionMinor * 1000 + versionPatch * 10 + versionBuild
        versionName = "$versionMajor.$versionMinor.$versionPatch"

        buildConfigField("String", "API_KEY", keystoreProperties.getProperty("api.key"))
    }
    
    androidResources {
        localeFilters += listOf("en", "es")
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
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
    }

    kotlin {
        jvmToolchain(libs.versions.jdk.get().toInt())
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jdk.get())
    }
}

dependencies {

    implementation(projects.core.database)
    implementation(projects.core.util)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.android.chart)
    implementation(libs.app.update.ktx)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.firebase)
    implementation(platform(libs.compose.bom))
    implementation(libs.coil)
    implementation(libs.core.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lottie)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.security.crypto)
    implementation(libs.work.manager)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.test.junit)

    debugImplementation(libs.leak.canary)
    debugImplementation(libs.compose.test.manifest)
}