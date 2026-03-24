plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.mhss.app.calendar.data"
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
                implementation(projects.core.ui)
                implementation(projects.core.datetime)
                implementation(projects.calendar.domain)

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
