plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "com.mhss.app.ai.data"
        compileSdk {
            version = release(libs.versions.compileSdk.get().toInt())
        }
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.ai.domain)
                implementation(projects.core.preferences)
                implementation(projects.notes.domain)
                implementation(projects.tasks.domain)
                implementation(projects.calendar.domain)
                implementation(projects.diary.domain)
                implementation(projects.bookmarks.domain)

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
