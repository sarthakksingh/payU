package com.sarthak.payu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.ui.theme.PayUTheme
import com.sarthak.payu.utils.UserPreferences
import com.sarthak.payu.view.navigation.AppNavHost
import com.sarthak.payu.view.screen.SplashScreen
import com.sarthak.payu.vm.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val isDarkMode by authViewModel.isDarkMode.collectAsState()
            var showSplash by remember { mutableStateOf(true) }
            var prefsReady by remember { mutableStateOf(false) }
            var initialLoggedIn by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                initialLoggedIn = userPreferences.isLoggedIn.first()
                prefsReady = true
            }

            PayUTheme(darkTheme = isDarkMode) {
                if (showSplash || !prefsReady) {
                    SplashScreen(
                        isDarkTheme = isDarkMode,
                        onFinished = { showSplash = false }
                    )
                } else {
                    AppNavHost(
                        authViewModel = authViewModel,
                        initialLoggedIn = initialLoggedIn
                    )
                }
            }
        }
    }
}
