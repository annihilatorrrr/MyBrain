plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.mhss.app.data"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 26
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:storage"))
                implementation(project(":core:preferences"))
                implementation(project(":settings:domain"))
                implementation(project(":notes:domain"))
                implementation(project(":tasks:domain"))
                implementation(project(":diary:domain"))
                implementation(project(":bookmarks:domain"))

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }

        androidMain {
            dependencies {
                implementation(project(":core:database"))

                implementation(libs.koin.android)
                implementation(libs.koin.android.workmanager)

                implementation(libs.androidx.work.runtime.ktx)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
}
