// Файл build.gradle.kts (топ-левел)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
//        maven { url 'https://jitpack.io' }
    }
    dependencies {
        // Онови версію до тієї, що підтримує SDK 35
        classpath("com.android.tools.build:gradle:8.4.1")
    }
}
