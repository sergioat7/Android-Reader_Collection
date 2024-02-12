/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/2/2024
 */

buildscript {
    dependencies {
        classpath("com.google.firebase:firebase-crashlytics-gradle:${libs.versions.crashlytics.get()}")//
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${libs.versions.navigation.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}