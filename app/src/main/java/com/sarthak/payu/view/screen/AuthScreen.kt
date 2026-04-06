package com.sarthak.payu.view.screen



import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.ui.theme.DarkBg
import com.sarthak.payu.ui.theme.DarkSurface
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.view.components.PayUButton
import com.sarthak.payu.view.components.PayUTextField
import com.sarthak.payu.view.components.SegmentedToggle
import com.sarthak.payu.vm.AuthViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0=SignIn, 1=SignUp
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 32.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Welcome to PayU",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                "Track expenses. Own your finances.",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .padding(20.dp)
            ) {
                Column {
                    Text("Get started", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "Sign in to your account or create a new one",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    SegmentedToggle(
                        options = listOf("Sign In", "Sign Up"),
                        selectedIndex = selectedTab,
                        onSelect = { selectedTab = it }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Animated form switch
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            slideInHorizontally { if (targetState == 1) it else -it } togetherWith
                                    slideOutHorizontally { if (targetState == 1) -it else it }
                        },
                        label = "auth_form"
                    ) { tab ->
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (tab == 1) {
                                PayUTextField(
                                    value = state.name,
                                    onValueChange = viewModel::onNameChange,
                                    placeholder = "Enter your full name",
                                    label = "Full Name",
                                    isError = state.nameError != null,
                                    errorMessage = state.nameError,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    )
                                )
                            }

                            PayUTextField(
                                value = state.email,
                                onValueChange = viewModel::onEmailChange,
                                placeholder = "Enter your email",
                                label = "Email",
                                isError = state.emailError != null,
                                errorMessage = state.emailError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                )
                            )

                            PayUTextField(
                                value = state.password,
                                onValueChange = viewModel::onPasswordChange,
                                placeholder = if (tab == 0) "Enter your password" else "Create a password",
                                label = "Password",
                                isError = state.passwordError != null,
                                errorMessage = state.passwordError,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (tab == 1) ImeAction.Next else ImeAction.Done
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            )

                            if (tab == 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text("Forgot password?", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }

                            if (tab == 1) {
                                PayUTextField(
                                    value = state.confirmPassword,
                                    onValueChange = viewModel::onConfirmPasswordChange,
                                    placeholder = "Confirm your password",
                                    label = "Confirm Password",
                                    isError = state.confirmPasswordError != null,
                                    errorMessage = state.confirmPasswordError,
                                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { showConfirm = !showConfirm }) {
                                            Icon(
                                                if (showConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                                onClick = { if (tab == 0) viewModel.signIn() else viewModel.signUp() },
                                isLoading = state.isLoading
                            )
                        }
                    }
                }
            }
        }
    }
}