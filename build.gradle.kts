
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10" apply false
}

kotlin {

    jvmToolchain(25) // Use your desired version

}