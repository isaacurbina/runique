package com.plcoding.runique

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private lateinit var splitInstallManager: SplitInstallManager
    private val splitInstallListener by lazy {
        SplitInstallStateUpdatedListener { state ->
            viewModel.handleInstallUpdate(
                splitInstallSessionState = state.status(),
                onAction = { action ->
                    when (action) {
                        MainActivityAction.RequiresUserConfirmation ->
                            splitInstallManager.startConfirmationDialogForResult(state, this, 0)
                    }
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.isCheckingAuth
            }
        }
        splitInstallManager = SplitInstallManagerFactory.create(applicationContext)
        setContent {
            RuniqueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    LaunchedEffect(key1 = viewModel.state.state) {
                        viewModel.state.state?.let {
                            context.showToast(it)
                        }
                    }
                    if (!viewModel.state.isCheckingAuth) {
                        val navController = rememberNavController()
                        NavigationRoot(
                            navController = navController,
                            isLoggedIn = viewModel.state.isLoggedIn,
                            onAnalyticsClick = {
                                installOrStartAnalyticsFeature()
                            }
                        )
                    }
                    if (viewModel.state.showAnalyticsInstallDialog) {
                        Dialog(onDismissRequest = {}) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape((16.dp)))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                viewModel.state.state?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(it),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        splitInstallManager.registerListener(splitInstallListener)
    }

    override fun onPause() {
        splitInstallManager.unregisterListener(splitInstallListener)
        super.onPause()
    }

    private fun installOrStartAnalyticsFeature() {
        if (splitInstallManager.installedModules.contains(ANALYTICS_FEATURE_MODULE)) {
            launchAnalyticsFeature()
        } else installAnalyticsFeature()
    }

    private fun launchAnalyticsFeature() {
        Intent()
            .setClassName(
                packageName, "com.plcoding.analytics.analyticsfeature.AnalyticsActivity"
            )
            .also(::startActivity)
    }

    private fun installAnalyticsFeature() {
        val request = SplitInstallRequest.newBuilder()
            .addModule(ANALYTICS_FEATURE_MODULE)
            .build()
        splitInstallManager
            .startInstall(request)
            .addOnFailureListener {
                it.printStackTrace()
                showToast(R.string.couldnt_load_module)
            }
    }

    private fun Context.showToast(@StringRes message: Int) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val ANALYTICS_FEATURE_MODULE = "analyticsfeature"
    }
}

