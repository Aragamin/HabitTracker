plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.habits"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.habits"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    // Начиная с Kotlin 2.x, kotlinCompilerExtensionVersion указывать не нужно.
    // composeOptions { kotlinCompilerExtensionVersion = "..." }  ← удалить
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Compose integration
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.navigation:navigation-compose:2.9.3")

    // Room + KSP
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // Work / Coroutines
    implementation("androidx.work:work-runtime-ktx:2.10.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
