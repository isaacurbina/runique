import com.plcoding.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidJUnit5ConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.apply("runique.jvm.junit5")
            pluginManager.apply("de.mannodermaus.android-junit5")
            dependencies {
                "androidTestImplementation"(libs.findLibrary("junit5.api").get())
                "androidTestImplementation"(libs.findLibrary("junit5.params").get())
                "androidTestImplementation"(libs.findLibrary("junit5.android.test.compose").get())
                "debugImplementation"(libs.findLibrary("androidx.compose.ui.test.manifest").get())
                "androidTestRuntimeOnly"(libs.findLibrary("junit5.engine").get())

                "testImplementation"(libs.findLibrary("assertk").get())
                "testImplementation"(libs.findLibrary("coroutines.test").get())
                "testImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}
