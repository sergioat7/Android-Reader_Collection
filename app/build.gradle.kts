import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.jetbrains)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id("androidx.navigation.safeargs.kotlin")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val appName = "aragones.sergio.readercollection"

val versionMajor = 2
val versionMinor = 8
val versionPatch = 4
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
}

kotlin {
    androidTarget()

    jvmToolchain(libs.versions.jdk.get().toInt())
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.database)
                implementation(projects.core.util)

                implementation(libs.bundles.firebase)
                implementation(libs.bundles.koin)
                implementation(libs.bundles.ktor)
                implementation(libs.cmp.components.resources)
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(projects.core.database)
                implementation(projects.core.util)
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

                implementation(libs.android.chart)
                implementation(libs.app.update.ktx)
                implementation(libs.bundles.compose)
                implementation(libs.bundles.firebase)
                implementation(libs.bundles.koin)
                implementation(libs.bundles.ktor)
                implementation(libs.ktor.client.okhttp)
                implementation(project.dependencies.platform(libs.compose.bom))
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.coil)
                implementation(libs.coil.network.ktor3)
                implementation(libs.core.ktx)
                implementation(libs.koin.android)
                implementation(libs.koin.compose.viewmodel.navigation)
                implementation(libs.koin.work.manager)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.lifecycle.viewmodel.ktx)
                implementation(libs.lottie)
                implementation(libs.material)
                implementation(libs.material3)
                implementation(libs.navigation.compose)
                implementation(libs.security.crypto)
                implementation(libs.work.manager)
            }
        }

        val commonTest by getting {
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.coroutines.test)
                implementation(libs.kotlinx.test.core)
                implementation(libs.ktor.client.mock)
                implementation(libs.mockk)
                implementation(libs.robolectric)
                implementation(libs.turbine)
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.kotlinx.test.core)
                implementation(libs.espresso.core)
                implementation(libs.compose.test.junit)
            }
        }
    }
}

dependencies {
    add("debugImplementation", libs.leak.canary)
    add("debugImplementation", libs.compose.test.manifest)
}