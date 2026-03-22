plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.mhss.app.notes.domain"
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
                implementation(project(":core:storage"))

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
}
