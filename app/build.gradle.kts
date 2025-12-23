plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.chaquopy)
}

android {
    kotlin {
        jvmToolchain(17)
    }
    namespace = "io.github.lemcoder.haystack"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "io.github.lemcoder.haystack"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            // TODO remove when netty issue is resolved in koog
            excludes.add("META-INF/*")
        }
    }
}

dependencies {
    implementation(projects.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.koog.agents)
    implementation(libs.koog.edge)
    implementation(libs.cactus)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)

    androidTestImplementation(libs.androidx.espresso.core)
}

chaquopy {
    defaultConfig{
        // version = "3.9"
        buildPython("/usr/bin/python3")
        pip {
            install("requests")
        }
    }
}