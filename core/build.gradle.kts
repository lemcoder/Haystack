plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.lint)
}

kotlin {
    androidLibrary {
        namespace = "io.github.lemcoder.core"
        compileSdk = 36
        minSdk = 31

        withHostTestBuilder {
        }
    }

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