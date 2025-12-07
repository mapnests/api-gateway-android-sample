// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
        classpath("com.mapnests.config-loader:com.mapnests.config-loader.gradle.plugin:2.0.0")
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
