// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Shared SDK/version properties consumed by the bundled NewBlackbox modules
// (their Groovy build scripts read these via rootProject.ext.*).
extra["compileSdkVersion"] = 36
extra["minSdk"] = 27
extra["targetSdkVersion"] = 36
extra["versionCode"] = 45
extra["versionName"] = "1.7.1"

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // KSP "2.3.x" targets the Kotlin 2.3 compiler. This project pins Kotlin 2.2.0
    // (see gradle/libs.versions.toml), so we must use the 2.2.x KSP release line
    // (`2.2.0-<rev>`) whose FIRST number matches the pinned Kotlin compiler.
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}

