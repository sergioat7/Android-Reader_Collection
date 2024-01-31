buildscript {

    dependencies {
        classpath("com.google.firebase:firebase-crashlytics-gradle:${libs.versions.crashlytics.get()}")//
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${libs.versions.navigation.get()}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")//
    }
}

plugins {
    id("com.android.application") version "7.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}