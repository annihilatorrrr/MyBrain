import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.mhss.app.mybrain.appfunctions"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}

dependencies {
    implementation(projects.notes.domain)
    implementation(projects.tasks.domain)
    implementation(projects.diary.domain)
    implementation(projects.bookmarks.domain)

    api(libs.androidx.appfunctions)
    ksp(libs.androidx.appfunctions.compiler)

    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
}

ksp {
    arg("appfunctions:aggregateAppFunctions", "true")
}

koinCompiler {
    compileSafety = false
}
