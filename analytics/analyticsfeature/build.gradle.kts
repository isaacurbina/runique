plugins {
    alias(libs.plugins.runique.android.dynamic.feature)
}
android {
    namespace = "com.plcoding.analytics.analyticsfeature"
}

dependencies {
    implementation(project(":app"))
    implementation(libs.androidx.navigation.compose)
    api(projects.analytics.presentation)
    implementation(projects.analytics.domain)
    implementation(projects.analytics.data)
    implementation(projects.core.database)
}
