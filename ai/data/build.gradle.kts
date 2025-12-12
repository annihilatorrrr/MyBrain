plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":ai:domain"))
    implementation(project(":core:preferences"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.cio)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    ksp(libs.koin.ksp.compiler)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koog.agents)
}