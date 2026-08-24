plugins {
    kotlin("android")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.tencent.kuikly-open.kuikly") version "2.25.0-2.1.21"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Kuikly 跨端框架核心依赖（预编译 AAR，二进制兼容 Kotlin 2.4.10）
    implementation("com.tencent.kuikly-open:core:2.25.0-2.1.21")
    implementation("com.tencent.kuikly-open:core-annotations:2.25.0-2.1.21")
    // Kuikly Android 渲染引擎
    implementation("com.tencent.kuikly-open:core-render-android:2.25.0-2.1.21")
}

android {
    namespace = "com.helix.kuikly"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
