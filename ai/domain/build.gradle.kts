plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
}

kotlin {
    androidLibrary {
        namespace = "com.mhss.app.ai.domain"
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
                implementation(project(":core:preferences"))
                implementation(project(":notes:domain"))
                implementation(project(":tasks:domain"))
                implementation(project(":calendar:domain"))

                implementation(libs.kotlinx.coroutines.core)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
}
