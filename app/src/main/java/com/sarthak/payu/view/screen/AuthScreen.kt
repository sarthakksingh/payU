package com.sarthak.payu.view.screen

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.ui.theme.DarkBg
import com.sarthak.payu.ui.theme.DarkSurface
import com.sarthak.payu.ui.theme.DarkBorder
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.view.components.PayUButton
import com.sarthak.payu.view.components.PayUTextField
import com.sarthak.payu.view.components.SegmentedToggle
import com.sarthak.payu.vm.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data)
        } else {
            viewModel.handleGoogleSignInResult(null)
        }
    }

    val legacyGoogleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleLegacyGoogleSignInResult(result.data)
        } else {
            viewModel.handleLegacyGoogleSignInResult(null)
        }
    }

    // One BringIntoViewRequester wraps the entire form card.
    // When any field focuses we ask the scroll container to bring
    // the whole card bottom into view — this guarantees the button
    // and all fields are visible, not just the focused field.
    val formBringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            // imePadding at the Box level — the scroll container inside
            // will then have exactly the right height to work with
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo section ───────────────────────────────────────
            // We use weight-based spacing so the logo area compresses
            // naturally on smaller screens / when keyboard is open
            Spacer(Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Welcome to PayU",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                "Track expenses. Own your finances.",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ── Form card ──────────────────────────────────────────
            // bringIntoViewRequester wraps the whole card so the entire
            // form (including the submit button) scrolls into view on focus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .bringIntoViewRequester(formBringIntoViewRequester)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "Get started",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        "Sign in to your account or create a new one",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    SegmentedToggle(
                        options = listOf("Sign In", "Sign Up"),
                        selectedIndex = selectedTab,
                        onSelect = {
                            selectedTab = it
                            // When switching tabs scroll back to top of form
                            scope.launch { scrollState.animateScrollTo(0) }
                        }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Animated form
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            slideInHorizontally { if (targetState == 1) it else -it } togetherWith
                                    slideOutHorizontally { if (targetState == 1) -it else it }
                        },
                        label = "auth_form"
                    ) { tab ->
                        // onAnyFieldFocus: called by every field when it gains focus.
                        // Scrolls the parent so the bottom of the form card
                        // (including the button) is fully above the keyboard.
                        val onAnyFieldFocus = {
                            scope.launch {
                                // Small delay lets the keyboard finish animating in
                                kotlinx.coroutines.delay(80)
                                formBringIntoViewRequester.bringIntoView()
                            }
                            Unit
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (tab == 1) {
                                FocusAwareTextField(
                                    value = state.name,
                                    onValueChange = viewModel::onNameChange,
                                    placeholder = "Enter your full name",
                                    label = "Full Name",
                                    isError = state.nameError != null,
                                    errorMessage = state.nameError,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    onFocused = onAnyFieldFocus
                                )
                            }

                            FocusAwareTextField(
                                value = state.email,
                                onValueChange = viewModel::onEmailChange,
                                placeholder = "Enter your email",
                                label = "Email",
                                isError = state.emailError != null,
                                errorMessage = state.emailError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                onFocused = onAnyFieldFocus
                            )

                            FocusAwareTextField(
                                value = state.password,
                                onValueChange = viewModel::onPasswordChange,
                                placeholder = if (tab == 0) "Enter your password" else "Create a password",
                                label = "Password",
                                isError = state.passwordError != null,
                                errorMessage = state.passwordError,
                                visualTransformation = if (showPassword)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (tab == 1) ImeAction.Next else ImeAction.Done
                                ),
                                onFocused = onAnyFieldFocus,
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            )

                            if (tab == 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        "Forgot password?",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (tab == 1) {
                                FocusAwareTextField(
                                    value = state.confirmPassword,
                                    onValueChange = viewModel::onConfirmPasswordChange,
                                    placeholder = "Confirm your password",
                                    label = "Confirm Password",
                                    isError = state.confirmPasswordError != null,
                                    errorMessage = state.confirmPasswordError,
                                    visualTransformation = if (showConfirm)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    onFocused = onAnyFieldFocus,
                                    trailingIcon = {
                                        IconButton(onClick = { showConfirm = !showConfirm }) {
                                            Icon(
                                                if (showConfirm) Icons.Default.Visibility
                                                else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            PayUButton(
                                text = if (tab == 0) "Sign In" else "Create Account",
                                onClick = {
                                    if (tab == 0) viewModel.signIn() else viewModel.signUp()
                                },
                                isLoading = state.isLoading
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = DarkBorder.copy(alpha = 0.9f)
                                )
                                Text(
                                    "  or  ",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = DarkBorder.copy(alpha = 0.9f)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val sender = viewModel.getGoogleSignInSender()
                                        if (sender != null) {
                                            googleSignInLauncher.launch(sender)
                                        } else {
                                            viewModel.getLegacyGoogleSignInIntent()?.let {
                                                legacyGoogleSignInLauncher.launch(it)
                                            } ?: run {
                                                viewModel.handleLegacyGoogleSignInResult(null)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, DarkBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    "G",
                                    color = Color(0xFF4285F4),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    "Continue with Google",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Bottom spacer ──────────────────────────────────────
            // Fixed 32dp — imePadding() on the Box already provides
            // the keyboard clearance, so we don't need a dynamic spacer
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── FocusAwareTextField ────────────────────────────────────────────
// Thin wrapper around PayUTextField that fires onFocused() when the
// field gains focus. Kept separate so the call-site stays clean and
// we don't pollute PayUTextField's own bringIntoViewRequester logic.

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FocusAwareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    onFocused: () -> Unit
) {
    // Per-field requester still works for mid-form fields on very small screens
    val fieldRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    PayUTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        label = label,
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        modifier = Modifier.bringIntoViewRequester(fieldRequester),
        onFocusChanged = { focused ->
            if (focused) {
                scope.launch {
                    kotlinx.coroutines.delay(80)
                    fieldRequester.bringIntoView()
                }
                onFocused()
            }
        }
    )
}
