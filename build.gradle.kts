import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding

buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${libs.versions.navigation.get()}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:${libs.versions.crashlytics.get()}")//
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
}

tasks.register("cleanProject", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

configure<SpotlessExtension> {
    // https://github.com/diffplug/spotless/issues/1644
    lineEndings = LineEnding.UNIX

    kotlin {
        target("**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "android_studio",
                    "ij_kotlin_allow_trailing_comma" to true,
                    "ij_kotlin_allow_trailing_comma_on_call_site" to true,
                    "ktlint_standard_chain-method-continuation" to "enabled",
                    "ktlint_standard_if-else-bracing" to "enabled",
                    "ktlint_standard_no-blank-line-in-list" to "enabled",
                    "ktlint_standard_no-consecutive-comments" to "enabled",
                    "ktlint_standard_try-catch-finally-spacing" to "enabled",
                    "ktlint_standard_string-template-indent" to "enabled",
                    "ktlint_standard_if-else-wrapping" to "enabled",
                    "ktlint_standard_annotation" to "disabled",
                    "ktlint_standard_binary-expression-wrapping" to "disabled",
                    "ktlint_standard_class-signature" to "disabled",
                    "ktlint_standard_final-newline" to "disabled",
                    "ktlint_standard_function-literal" to "disabled",
                    "ktlint_standard_function-type-modifier-spacing" to "disabled",
                    "ktlint_standard_class-naming" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_no-empty-file" to "disabled",
                    "ktlint_experimental" to "disabled",
                    "ij_kotlin_line_break_after_multiline_when_entry" to false,
                    "ktlint_standard_blank-line-between-when-conditions" to "enabled",
                    "ktlint_standard_mixed-condition-operators" to "enabled",
                    "ktlint_standard_square-brackets-spacing" to "enabled",
                    "ktlint_standard_when-entry-bracing" to "enabled",
                ),
            )
    }
}
