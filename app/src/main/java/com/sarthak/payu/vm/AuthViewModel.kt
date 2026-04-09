package com.sarthak.payu.vm

import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.payu.utils.GoogleAuthClient
import com.sarthak.payu.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val needsRegistration: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = prefs.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isDarkMode: StateFlow<Boolean> = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = prefs.isDarkMode.first()
            prefs.toggleDarkMode(!current)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            prefs.toggleDarkMode(isDark)
        }
    }

    suspend fun getGoogleSignInSender(): IntentSenderRequest? {
        return googleAuthClient.getSignInIntentSender()
    }

    fun getLegacyGoogleSignInIntent(): Intent? {
        return googleAuthClient.getLegacySignInIntent()
    }

    fun handleGoogleSignInResult(intent: Intent?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, needsRegistration = false) }
            val result = if (intent != null) {
                googleAuthClient.signInWithIntent(intent)
            } else {
                googleAuthClient.getCurrentUserFallback()
            }
            if (result != null) {
                val email = result.email.orEmpty().trim()
                val storedProfile = if (email.isNotBlank()) prefs.getKnownProfile(email) else null
                if (storedProfile != null) {
                    prefs.saveUser(storedProfile.name, storedProfile.email)
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    val suggestedName = result.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = suggestedName,
                            email = email,
                            needsRegistration = true,
                            errorMessage = "Google account not registered yet. Please complete sign up."
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Google sign-in cancelled"
                    )
                }
            }
        }
    }

    fun handleLegacyGoogleSignInResult(intent: Intent?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, needsRegistration = false) }
            val result = if (intent != null) {
                googleAuthClient.signInWithLegacyIntent(intent)
            } else {
                googleAuthClient.getCurrentUserFallback()
            }
            if (result != null) {
                val email = result.email.orEmpty().trim()
                val storedProfile = if (email.isNotBlank()) prefs.getKnownProfile(email) else null
                if (storedProfile != null) {
                    prefs.saveUser(storedProfile.name, storedProfile.email)
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    val suggestedName = result.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = suggestedName,
                            email = email,
                            needsRegistration = true,
                            errorMessage = "Google account not registered yet. Please complete sign up."
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Google sign-in cancelled"
                    )
                }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null) }
    fun onConfirmPasswordChange(v: String) = _state.update { it.copy(confirmPassword = v, confirmPasswordError = null) }

    fun signIn() {
        val s = _state.value
        var valid = true
        if (s.email.isBlank()) {
            _state.update { it.copy(emailError = "Email is required") }; valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }; valid = false
        }
        if (s.password.isBlank()) {
            _state.update { it.copy(passwordError = "Password is required") }; valid = false
        } else if (s.password.length < 6) {
            _state.update { it.copy(passwordError = "Minimum 6 characters") }; valid = false
        }
        if (!valid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(800)
            val storedProfile = prefs.getKnownProfile(s.email)
            val name = storedProfile?.name?.takeIf { it.isNotBlank() }
                ?: s.email.substringBefore("@").replaceFirstChar { it.uppercase() }
            prefs.saveUser(name, s.email)
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    fun signUp() {
        val s = _state.value
        var valid = true
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }; valid = false
        }
        if (s.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _state.update { it.copy(emailError = "Valid email required") }; valid = false
        }
        if (s.password.length < 6) {
            _state.update { it.copy(passwordError = "Minimum 6 characters") }; valid = false
        }
        if (s.confirmPassword != s.password) {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }; valid = false
        }
        if (!valid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(800)
            prefs.saveUser(s.name, s.email)
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
