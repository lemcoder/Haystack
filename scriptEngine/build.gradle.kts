import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.DependencyDirectories.localKonanDir

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.konan.plugin)
}

val moduleName = "scriptEngine"
val s: String = File.separator

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "io.github.lemcoder.scriptEngine"
        compileSdk = 36
        minSdk = 31

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {}.configure {}
    }

    buildList {
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            add(iosArm64())
            add(iosSimulatorArm64())
        }
    }.forEach { target ->
        target.apply {
            val main by compilations.getting

            main.cinterops.create("liblua") {
                val includeDir = File(rootDir, "${moduleName}${s}native${s}include")

                definitionFile = File(rootDir, "${moduleName}${s}native${s}liblua.def")
                includeDirs(includeDir)

                headers(
                    *fileTree(includeDir) {
                        include("**/*.h")
                    }.files.map { it.absolutePath }.toTypedArray()
                )

                extraOpts(
                    "-libraryPath",
                    "$rootDir${s}${moduleName}${s}native${s}lib${s}${target.konanTarget.name}"
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
            implementation(libs.luaj)
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
            add(KonanTarget.IOS_SIMULATOR_ARM64.name)
            // add(KonanTarget.MACOS_ARM64.name)
        }
    }
    sourceDir = "${rootDir}/${moduleName}/native/src"
    headerDir = "${rootDir}/${moduleName}/native/include"
    outputDir = "${rootDir}/${moduleName}/native/lib"
    libName = "lua"
    konanPath = localKonanDir.listFiles()?.first {
        it.name.contains(libs.versions.kotlin.get())
    }?.absolutePath
    additionalCompilerArgs = listOf("-DLUA_USE_IOS")
}
