buildscript {
    dependencies {
        classpath("com.google.firebase:firebase-crashlytics-gradle:${libs.versions.crashlytics.get()}")//
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${libs.versions.navigation.get()}")
    }
}

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}