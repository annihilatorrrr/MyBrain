plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.mhss.app.notes.data"
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
                implementation(projects.core.database)
                implementation(projects.core.preferences)
                implementation(projects.notes.domain)

                implementation(libs.kotlinx.coroutines.core)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.documentfile)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
}