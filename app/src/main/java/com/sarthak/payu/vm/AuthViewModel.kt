package com.sarthak.payu.vm



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payu.util.UserPreferences
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
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = prefs.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
            // Simulate auth — replace with real auth if needed
            kotlinx.coroutines.delay(800)
            val name = s.email.substringBefore("@").replaceFirstChar { it.uppercase() }
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