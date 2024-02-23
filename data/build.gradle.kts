/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/2/2024
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("kotlin-kapt")
}

dependencies {

    implementation(project(":core:util"))

    implementation(libs.room.common)
    implementation(libs.moshi)
    kapt(libs.moshi.kotlin.codegen)
}