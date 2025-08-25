plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.habits"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.habits"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }

    // С Kotlin 2.x блок composeOptions не нужен

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // если java.time на API<26 - не нужен, но пусть будет
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Desugaring (безопасно оставить)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Compose BOM и UI
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.navigation:navigation-compose:2.9.3")

    implementation("com.google.android.material:material:1.12.0")

    // Иконки (стрелка назад, Done и т.п.)
    implementation("androidx.compose.material:material-icons-extended")

    // Room + KSP
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // Work / Coroutines
    implementation("androidx.work:work-runtime-ktx:2.10.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
