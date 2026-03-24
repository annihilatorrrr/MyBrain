plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "com.mhss.app.settings.data"
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
                implementation(projects.core.storage)
                implementation(projects.core.preferences)
                implementation(projects.settings.domain)
                implementation(projects.notes.domain)
                implementation(projects.tasks.domain)
                implementation(projects.diary.domain)
                implementation(projects.bookmarks.domain)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
            }
        }

        androidMain {
            dependencies {
                implementation(projects.core.database)

                implementation(libs.koin.android.workmanager)

                implementation(libs.androidx.work.runtime.ktx)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
}
