plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.chaquopy)
}

android {
    defaultConfig {
        namespace = "io.github.lemcoder.core"
        compileSdk = 36
        minSdk = 31

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }
}

kotlin {
    androidTarget()

    val xcfName = "HaystackCoreKit"

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.koog.agents)
                implementation(libs.koog.edge)

                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.androidx.datastore.core)
                implementation(libs.androidx.datastore.preferences)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }

}

chaquopy {
    defaultConfig{
        // version = "3.9"
        buildPython("/usr/bin/python3")
        pip {
            install("matplotlib")
            install("requests")
        }
    }
}