// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Removed the version for `google-services` as it is declared in `buildscript`
}

buildscript {
    repositories {
        google()  // This is fine as it's part of the buildscript
        mavenCentral()
    }
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        classpath("com.google.gms:google-services:4.3.15")
    }
}
