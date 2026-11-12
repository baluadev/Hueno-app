package com.hueno.coloridentifier

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hueno.coloridentifier.ui.HuenoApp
import com.hueno.coloridentifier.ui.splash.SplashOverlay

private const val LARGE_SCREEN_DP = 600

class MainActivity : ComponentActivity() {
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Folding or unfolding changes the screen class without recreating the activity.
        requestedOrientation = if (newConfig.smallestScreenWidthDp < LARGE_SCREEN_DP) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: it swaps the starting theme for the app theme.
        // The system splash is removed in one cut rather than faded: the intro's first frame is
        // the same picture, so a fade would only show a dip between two identical images.
        installSplashScreen().setOnExitAnimationListener { it.remove() }
        super.onCreate(savedInstanceState)
        // Phones stay upright: turning one sideways only gives the picture less room. Tablets and
        // unfolded foldables may turn (Android 16 ignores the lock there anyway), and rotating does
        // not recreate the activity, so the camera, its calibration and a frozen frame survive.
        if (resources.configuration.smallestScreenWidthDp < LARGE_SCREEN_DP) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        enableEdgeToEdge()
        setContent {
            // Saved across rotation, so the intro plays once per launch and not on every turn.
            var introDone by rememberSaveable { mutableStateOf(false) }
            var appStarted by rememberSaveable { mutableStateOf(false) }
            Box(Modifier.fillMaxSize()) {
                // The app starts once the intro's spin has finished, then loads behind the rest.
                if (appStarted) HuenoApp()
                if (!introDone) {
                    SplashOverlay(
                        onReveal = { appStarted = true },
                        onDone = { introDone = true },
                    )
                }
            }
        }
    }
}
