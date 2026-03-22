plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.room3)
}

kotlin {
    android {
        namespace = "com.mhss.app.database"
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
                implementation(project(":tasks:domain"))
                implementation(project(":notes:domain"))
                implementation(project(":bookmarks:domain"))
                implementation(project(":diary:domain"))
                implementation(project(":core:alarm"))

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}
