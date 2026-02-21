plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(property("RELEASE_STORE_FILE") as String)
            storePassword = property("RELEASE_STORE_PASSWORD") as String
            keyAlias = property("RELEASE_KEY_ALIAS") as String
            keyPassword = property("RELEASE_KEY_PASSWORD") as String
        }
    }
    namespace = "com.example.annamapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.annamapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // For Kotlin projects using KSP:

    ksp {

        arg("room.schemaLocation", "$projectDir/schemas")

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            //signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    //androidResources {

    //    generateLocaleConfig = true

    //}

    testOptions {

        unitTests {

            isIncludeAndroidResources = true

        }

    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

configurations {

    create("cleanedAnnotations")

    implementation {

        //exclude(group = "org.jetbrains", module = "annotations")

        exclude(group = "com.intellij", module = "annotations")

    }

}

dependencies {

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.runtime.ktx)

    implementation(libs.androidx.navigation.compose)

    //implementation(libs.androidx.navigation.testing)

    //implementation(libs.core.ktx)

    implementation(libs.androidx.compose.ui.test.junit4)

    implementation(libs.androidx.room.compiler)

    implementation(libs.androidx.room.runtime)

    implementation(libs.androidx.compose.ui)
    //implementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    //debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.robolectric)

    // Needed for createComposeRule(), but not for createAndroidComposeRule<YourActivity>():

    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // androidTestImplementation(libs.androidx.junit)

    // androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Test rules and transitive dependencies:

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)

    // See Add the KSP plugin to your project

    ksp(libs.androidx.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room

    implementation(libs.androidx.room.ktx)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.androidx.room.testing)

    //testImplementation(kotlin("test"))
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)

    implementation(libs.datastore.preferences)
    //implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.media3.exoplayer)
    //implementation(libs.kotlinx.coroutines.core)
    //implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material.icons.core)
}