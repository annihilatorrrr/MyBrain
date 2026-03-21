plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "com.mhss.app.widget"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 26
    }
}
