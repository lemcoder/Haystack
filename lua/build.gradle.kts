import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.DependencyDirectories.localKonanDir

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.konan.plugin)
}

val s: String = File.separator

kotlin {
    androidLibrary {
        namespace = "io.github.lemcoder.lua"
        compileSdk = 36
        minSdk = 31

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }

    buildList {
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            add(iosArm64())
            add(macosArm64())
        }
    }.forEach { target ->
        target.apply {
            val main by compilations.getting

            main.cinterops.create("liblua") {
                val includeDir = File(rootDir, "lua${s}native${s}include")

                definitionFile = File(rootDir, "lua${s}native${s}liblua.def")
                includeDirs(includeDir)

                headers(
                    File(includeDir, "lua.h"),
                    File(includeDir, "luaconf.h"),
                    File(includeDir, "lualib.h"),
                    File(includeDir, "lauxlib.h"),
                    File(includeDir, "lstate.h")

                )

                extraOpts(
                    "-libraryPath",
                    "$rootDir${s}lua${s}native${s}lib${s}${target.konanTarget.name}"
                )
            }

        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.luajava)
            // TODO(lemcoder) Figure out TOML dependency for this
            runtimeOnly("party.iroiro.luajava:android:4.1.0:lua55@aar")
        }

        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.core)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

konanConfig {
    targets = buildList {
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            add(KonanTarget.IOS_ARM64.name)
            // add(KonanTarget.MACOS_ARM64.name)
        }
    }
    sourceDir = "${rootDir}/lua/native/src"
    headerDir = "${rootDir}/lua/native/include"
    outputDir = "${rootDir}/lua/native/lib"
    libName = "lua"
    konanPath = localKonanDir.listFiles()?.first {
        it.name.contains(libs.versions.kotlin.get())
    }?.absolutePath
    additionalCompilerArgs = listOf("-DLUA_USE_IOS")
}
