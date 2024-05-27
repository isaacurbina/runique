plugins {
    alias(libs.plugins.runique.android.library.compose)
}

android {
    namespace = "com.plcoding.core.presentation.designsystemwear"
    defaultConfig {
        minSdk = 30
    }
}

dependencies {
    api(projects.core.presentation.designsystem)
    implementation(libs.androidx.wear.compose.material)
}
