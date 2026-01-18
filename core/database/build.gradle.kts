/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/2/2024
 */

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    androidLibrary {
        namespace = "com.aragones.sergio.database"
        compileSdk = libs.versions.sdk.compile.get().toInt()
        minSdk = libs.versions.sdk.min.get().toInt()

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(libs.versions.jdk.get().toInt())
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.room.runtime)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }

        commonTest.dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.kotlinx.test.core)
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.espresso.core)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}