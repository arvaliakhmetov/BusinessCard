plugins {
    kotlin("jvm") version "1.9.0"
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

buildscript {
    val kotlin_version = "1.9.0"
    val agp_version = "8.1.1"

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.47")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }

    repositories {
        google()
        mavenCentral()
    }
}

