plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.mhss.app.ai.data"
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
                implementation(project(":ai:domain"))
                implementation(project(":core:preferences"))
                implementation(project(":notes:domain"))
                implementation(project(":tasks:domain"))
                implementation(project(":calendar:domain"))
                implementation(project(":diary:domain"))
                implementation(project(":bookmarks:domain"))

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.cio)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.koog.agents)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
}
