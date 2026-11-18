plugins {
    id("com.android.application")
}
android {
    namespace = "br.com.a01optimizer"
    compileSdk = 37
    defaultConfig {
        applicationId = "br.com.a01optimizer"
        minSdk = 30
        targetSdk = 32
        versionCode = 1
        versionName = "0.1.1-experimental"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("com.flyfishxu:kadb:2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
