// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // KSP 2.3.x requires the Kotlin 2.3 compiler; this module pins Kotlin 2.2.0
    // (see gradle/libs.versions.toml), so use the matching 2.2.x KSP release line.
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
}