package com.sarthak.payu.view.screen

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.R
import com.sarthak.payu.data.model.POPULAR_BANKS
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.paymentMethodDisplayName
import com.sarthak.payu.ui.theme.*
import com.sarthak.payu.view.components.*
import com.sarthak.payu.vm.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onGoToCalendar: () -> Unit = {},
    onThemeToggle: (Offset) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val avatarContainer = if (isDarkTheme) Color.White else Color.Black
    val avatarContent = if (isDarkTheme) Color.Black else Color.White
    var showDrawer by remember { mutableStateOf(false) }
    val drawerProgress = remember { Animatable(0f) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPassword by remember { mutableStateOf(false) }
    var showEditForm by remember { mutableStateOf(false) }
    var editingMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var editName by remember(state.name) { mutableStateOf(state.name) }
    var editEmail by remember(state.email) { mutableStateOf(state.email) }
    var editPassword by remember { mutableStateOf("") }
    var isExportingCsv by remember { mutableStateOf(false) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    var themeButtonCenter by remember { mutableStateOf<Offset?>(null) }
    val themeIconFlipProgress = remember { Animatable(if (isDarkTheme) 0f else 1f) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val csv = pendingCsv
        if (uri != null && csv != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(csv.toByteArray())
                stream.flush()
            }
            Toast.makeText(context, "Transactions exported", Toast.LENGTH_SHORT).show()
        }
        pendingCsv = null
        isExportingCsv = false
    }

    LaunchedEffect(showDrawer) {
        if (showDrawer) {
            drawerProgress.animateTo(1f, animationSpec = tween(280))
        } else {
            drawerProgress.animateTo(0f, animationSpec = tween(240))
        }
    }

    val drawerVisible = showDrawer || drawerProgress.value > 0.01f

    BackHandler(enabled = drawerVisible) {
        showDrawer = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            PayUTopBar(
                paymentMethods = state.paymentMethods,
                onDrawerClick = { showDrawer = !showDrawer },
                onCalendarClick = onGoToCalendar
            )

            Spacer(Modifier.height(15.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(avatarContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.name.firstOrNull()?.uppercase() ?: "P",
                        color = avatarContent,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        state.name.ifBlank { "User" },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(state.email, color = TextSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(avatarContainer)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            themeButtonCenter = Offset(
                                x = pos.x + coords.size.width / 2f,
                                y = pos.y + coords.size.height / 2f
                            )
                        }
                        .clickable {
                            scope.launch {
                                val targetFlip = if (isDarkTheme) 1f else 0f
                                themeIconFlipProgress.animateTo(
                                    targetValue = targetFlip,
                                    animationSpec = tween(
                                        durationMillis = 1100,
                                        easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                                    )
                                )
                            }
                            themeButtonCenter?.let(onThemeToggle)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                rotationY = themeIconFlipProgress.value * 180f
                                cameraDistance = 24f * density.density
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val flip = themeIconFlipProgress.value
                        val moonAlpha = (1f - (flip * 2f)).coerceIn(0f, 1f)
                        val sunAlpha = ((flip - 0.5f) * 2f).coerceIn(0f, 1f)
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = avatarContent,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    alpha = moonAlpha
                                    rotationY = flip * 180f
                                }
                        )
                        Icon(
                            Icons.Default.LightMode,
                            contentDescription = null,
                            tint = avatarContent,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    alpha = sunAlpha
                                    rotationY = 180f - (flip * 180f)
                                }
                        )
                    }
                }
            }

            AnimatedPeriodChips(
                options = listOf("Account", "Methods"),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )

            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { if (targetState == 1) it else -it } togetherWith
                            slideOutHorizontally { if (targetState == 1) -it else it }
                },
                label = "profile_tab"
            ) { tab ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    if (tab == 0) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileInfoRow(
                                label = "Total spendings",
                                value = formatRupee(state.totalSpending)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ProfileInfoRow(label = "Email", value = state.email.ifBlank { "\u2014" })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ProfileInfoRow(
                                label = "Balance",
                                value = formatRupee(state.totalBalance)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ProfileInfoRow(
                                label = "Linked methods",
                                value = "${state.paymentMethods.size}"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ProfileInfoRow(
                                label = "Primary method",
                                value = state.primaryMethod?.let { paymentMethodDisplayName(it) } ?: "None"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ProfileInfoRow(
                                label = "Financial health",
                                value = "${state.financialHealthScore}/100"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                        }

                        // â”€â”€ PAYMENT METHODS SECTION â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val actionButtonContainer = if (isDarkTheme) Color.White else Color(0xFFF0E7DB)
                            val actionButtonContent = if (isDarkTheme) Color.Black else Color(0xFF1F1A17)

                            OutlinedButton(
                                onClick = { showEditForm = !showEditForm },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, actionButtonContainer),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = actionButtonContainer,
                                    contentColor = actionButtonContent
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.edit),
                                    contentDescription = null,
                                    tint = actionButtonContent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Edit",
                                    fontWeight = FontWeight.SemiBold,
                                    color = actionButtonContent,
                                    fontSize = 16.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isExportingCsv = true
                                        val csv = viewModel.buildTransactionsCsv()
                                        pendingCsv = csv
                                        exportLauncher.launch("payu-transactions.csv")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .scale(if (isExportingCsv) 0.98f else 1f),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, actionButtonContainer),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = actionButtonContainer,
                                    contentColor = actionButtonContent
                                ),
                                enabled = !isExportingCsv
                            ) {
                                AnimatedContent(
                                    targetState = isExportingCsv,
                                    label = "csv_export"
                                ) { exporting ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (exporting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = actionButtonContent
                                            )
                                            Text(
                                                "Exporting",
                                                fontWeight = FontWeight.SemiBold,
                                                color = actionButtonContent,
                                                fontSize = 16.sp
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(R.drawable.export),
                                                contentDescription = null,
                                                tint = actionButtonContent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "Export",
                                                fontWeight = FontWeight.SemiBold,
                                                color = actionButtonContent,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = showEditForm) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                PayUTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    placeholder = "Enter your full name",
                                    label = "Full Name",
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                )
                                PayUTextField(
                                    value = editEmail,
                                    onValueChange = { editEmail = it },
                                    placeholder = "Enter your email",
                                    label = "Email",
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    )
                                )
                                PayUTextField(
                                    value = editPassword,
                                    onValueChange = { editPassword = it },
                                    placeholder = "Create a new password",
                                    label = "Password",
                                    visualTransformation = if (showPassword)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
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
                                PayUButton(
                                    text = "Update Details",
                                    onClick = { viewModel.saveProfile(editName, editEmail) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                    } else {
                        // â”€â”€ EDIT TAB â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                        PaymentMethodsSection(
                            methods = state.paymentMethods,
                            atLimit = state.atMethodLimit,
                            primaryMethodId = state.primaryMethod?.id,
                            onAdd = { viewModel.showAddDialog() },
                            onRemove = { viewModel.removeMethod(it) },
                            onSetPrimary = { viewModel.setPrimary(it) },
                            onEditBalance = { editingMethod = it }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = { viewModel.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFEF4444)
                )
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }
        }
        }

        if (drawerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showDrawer = false
                    }
            )

            DraggablePaymentMethodsDrawerCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp, start = 14.dp, end = 14.dp),
                methods = state.paymentMethods,
                totalBalance = state.paymentMethods.sumOf { it.balance },
                drawerProgress = drawerProgress,
                primaryMethodId = state.primaryMethod?.id,
                onSetPrimary = viewModel::setPrimary,
                onAddMethod = { viewModel.showAddDialog() },
                onDismiss = { showDrawer = false }
            )
        }
    }

    // â”€â”€ Add Payment Method Dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    if (state.showAddMethodDialog) {
        AddPaymentMethodDialog(
            atLimit = state.atMethodLimit,
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { bankName, lastDigits, balance, type, accountNumber, cardNumber, cvv, expiryDate ->
                viewModel.addPaymentMethod(
                    bankName = bankName,
                    lastDigits = lastDigits,
                    balance = balance.toDoubleOrNull() ?: 0.0,
                    type = type,
                    accountNumber = accountNumber,
                    cardNumber = cardNumber,
                    cvv = cvv,
                    expiryDate = expiryDate
                )
                viewModel.dismissAddDialog()
            }
        )
    }

    if (editingMethod != null) {
        AddPaymentMethodDialog(
            atLimit = false,
            title = "Edit Payment Method",
            confirmLabel = "Save Changes",
            initialMethod = editingMethod,
            onDismiss = { editingMethod = null },
            onConfirm = { bankName, lastDigits, balance, type, accountNumber, cardNumber, cvv, expiryDate ->
                val current = editingMethod ?: return@AddPaymentMethodDialog
                viewModel.updatePaymentMethod(
                    current.copy(
                        bankName = bankName,
                        lastDigits = lastDigits,
                        balance = balance.toDoubleOrNull() ?: current.balance,
                        type = type,
                        accountNumber = accountNumber,
                        cardNumber = cardNumber,
                        cvv = cvv,
                        expiryDate = expiryDate
                    )
                )
                editingMethod = null
            }
        )
    }
}

private fun formatRupee(amount: Double): String = "\u20B9${"%,.2f".format(amount)}"

// â”€â”€ Payment Methods Section â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun PaymentMethodsSection(
    methods: List<PaymentMethod>,
    atLimit: Boolean,
    primaryMethodId: String?,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onSetPrimary: (String) -> Unit,
    onEditBalance: (PaymentMethod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Payment Methods",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (!atLimit) {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealGreen.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = TealGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (methods.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("\uD83C\uDFE6", fontSize = 32.sp)
                Text(
                    "No payment methods added",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedButton(
                    onClick = onAdd,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, TealGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealGreen)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Method", fontSize = 13.sp)
                }
            }
        } else {
            val activePrimaryId = primaryMethodId ?: methods.firstOrNull { it.isPrimary }?.id
            methods.forEach { method ->
                PaymentMethodItem(
                    method = method,
                    isPrimary = method.id == activePrimaryId,
                    onSetPrimary = { onSetPrimary(method.id) },
                    onEditBalance = { onEditBalance(method) },
                    onRemove = { onRemove(method.id) }
                )
            }
            if (atLimit) {
                Text(
                    "Maximum 5 payment methods reached",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    isPrimary: Boolean,
    onSetPrimary: () -> Unit,
    onEditBalance: () -> Unit,
    onRemove: () -> Unit
) {
    val displayName = when (method.type) {
        PaymentMethodType.CASH -> "Cash"
        else -> "${method.bankName} \u2022\u2022\u2022\u2022 ${method.lastDigits}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (isPrimary) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(999.dp))
                    .background(TealGreen.copy(alpha = 0.16f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.primary),
                        contentDescription = null,
                        tint = TealGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "Primary",
                        color = TealGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isPrimary) 18.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (method.type == PaymentMethodType.CASH) {
                Text("\uD83D\uDCB5", fontSize = 22.sp)
            } else {
                BankLogoBadge(
                    bankName = method.bankName,
                    size = 38.dp,
                    logoSize = 22.dp,
                    background = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    modifier = Modifier
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    method.type.label,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Balance: ${formatRupee(method.balance)}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onEditBalance, contentPadding = PaddingValues(0.dp)) {
                        Text("Edit", color = TealGreen, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.size(32.dp))

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Remove",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentMethodDialog(
    atLimit: Boolean,
    title: String = "Add Payment Method",
    confirmLabel: String = "Save Payment Method",
    initialMethod: PaymentMethod? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        bankName: String,
        lastDigits: String,
        balance: String,
        type: PaymentMethodType,
        accountNumber: String,
        cardNumber: String,
        cvv: String,
        expiryDate: String
    ) -> Unit
) {
    fun unmask(value: String?, placeholder: String): String {
        val current = value?.trim().orEmpty()
        return if (current.isBlank() || current == placeholder || current.contains("xxxx")) "" else current
    }

    val initialType = initialMethod?.type ?: PaymentMethodType.BANK
    var selectedType by remember(initialMethod?.id) { mutableStateOf(initialType) }
    var bankName by remember(initialMethod?.id) { mutableStateOf(initialMethod?.bankName.orEmpty()) }
    var isCustomBank by remember(initialMethod?.id) {
        mutableStateOf(initialMethod?.bankName?.let { it !in POPULAR_BANKS } ?: false)
    }
    var customBankName by remember(initialMethod?.id) {
        mutableStateOf(
            if (initialMethod?.bankName?.let { it !in POPULAR_BANKS } == true) initialMethod.bankName else ""
        )
    }
    var lastDigits by remember(initialMethod?.id) { mutableStateOf(initialMethod?.lastDigits.orEmpty()) }
    var balance by remember(initialMethod?.id) {
        mutableStateOf(if (initialMethod == null || initialMethod.balance == 0.0) "" else initialMethod.balance.toString())
    }
    var accountNumber by remember(initialMethod?.id) {
        mutableStateOf(unmask(initialMethod?.accountNumber, "xxxx xxxx xxxx xxxx"))
    }
    var cardNumber by remember(initialMethod?.id) {
        mutableStateOf(unmask(initialMethod?.cardNumber, "xxxx xxxx xxxx xxxx"))
    }
    var cvv by remember(initialMethod?.id) { mutableStateOf(unmask(initialMethod?.cvv, "xxx")) }
    var expiryDate by remember(initialMethod?.id) { mutableStateOf(unmask(initialMethod?.expiryDate, "xx/xx")) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var bankNameError by remember { mutableStateOf<String?>(null) }
    var lastDigitsError by remember { mutableStateOf<String?>(null) }
    var balanceError by remember { mutableStateOf<String?>(null) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                }
            }

            if (atLimit) {
                Text("You've reached the maximum of 5 payment methods.", color = Color(0xFFEF4444), fontSize = 13.sp)
                PayUButton(text = "OK", onClick = onDismiss)
                return@Column
            }

            Text("Choose type", color = TextSecondary, fontSize = 12.sp)
            Text("Select the method you want to store", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentTypeChoiceChip(
                    modifier = Modifier.weight(1f),
                    icon = PaymentMethodType.BANK.emoji,
                    title = "Bank Account",
                    subtitle = "Savings, current, or salary account",
                    selected = selectedType == PaymentMethodType.BANK,
                    onClick = {
                        selectedType = PaymentMethodType.BANK
                        bankNameError = null
                        lastDigitsError = null
                        balanceError = null
                    }
                )
                PaymentTypeChoiceChip(
                    modifier = Modifier.weight(1f),
                    icon = PaymentMethodType.CARD.emoji,
                    title = "Card",
                    subtitle = "Debit or credit card",
                    selected = selectedType == PaymentMethodType.CARD,
                    onClick = {
                        selectedType = PaymentMethodType.CARD
                        bankNameError = null
                        lastDigitsError = null
                        balanceError = null
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (selectedType == PaymentMethodType.CARD) "Card / Bank name" else "Bank name",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (isCustomBank) "Other (custom)" else bankName.ifBlank { "Select bank" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bankDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = TealGreen,
                            unfocusedBorderColor = if (bankNameError != null) Color(0xFFEF4444) else MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = if (bankName.isBlank()) TextSecondary else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = bankNameError != null
                    )
                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        POPULAR_BANKS.forEach { bank ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BankLogoBadge(
                                            bankName = bank,
                                            size = 28.dp,
                                            logoSize = 16.dp,
                                            background = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            bank,
                                            color = if (bankName == bank) TealGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    bankName = bank
                                    isCustomBank = bank == "Other"
                                    bankNameError = null
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (bankNameError != null) {
                    Text(bankNameError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
                AnimatedVisibility(visible = isCustomBank) {
                    PayUTextField(
                        value = customBankName,
                        onValueChange = { customBankName = it; bankNameError = null },
                        placeholder = "Enter bank name",
                        label = "Custom bank name"
                    )
                }
            }

            PayUTextField(
                value = lastDigits,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        lastDigits = it
                        lastDigitsError = null
                    }
                },
                placeholder = "e.g. 4821",
                label = if (selectedType == PaymentMethodType.CARD) "Last 4 digits of card" else "Last 3-4 digits of account",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = lastDigitsError != null,
                errorMessage = lastDigitsError
            )

            PayUTextField(
                value = balance,
                onValueChange = {
                    if (it.isBlank() || it.toDoubleOrNull() != null) {
                        balance = it
                        balanceError = null
                    }
                },
                placeholder = "e.g. 25000",
                label = "Current balance (optional)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = balanceError != null,
                errorMessage = balanceError
            )

            if (selectedType == PaymentMethodType.BANK) {
                PayUTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    placeholder = "e.g. 123456789012",
                    label = "Account number (optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                PayUTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    placeholder = "e.g. 1234 5678 9012 3456",
                    label = "Card number (optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PayUTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        placeholder = "123",
                        label = "CVV (optional)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    PayUTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        placeholder = "MM/YY",
                        label = "Expiry",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            PayUButton(
                text = confirmLabel,
                onClick = {
                    val finalBank = if (isCustomBank) customBankName else bankName
                    var ok = true
                    if (finalBank.isBlank()) {
                        bankNameError = "Bank name required"
                        ok = false
                    }
                    if (lastDigits.length < 3) {
                        lastDigitsError = "Enter 3-4 digits"
                        ok = false
                    }
                    val parsedBalance = balance.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                    if (balance.isNotBlank() && parsedBalance == null) {
                        balanceError = "Enter a valid balance"
                        ok = false
                    }
                    if (ok) {
                        onConfirm(
                            finalBank,
                            lastDigits,
                            balance,
                            selectedType,
                            accountNumber.ifBlank { "xxxx xxxx xxxx xxxx" },
                            cardNumber.ifBlank { "xxxx xxxx xxxx xxxx" },
                            cvv.ifBlank { "xxx" },
                            expiryDate.ifBlank { "xx/xx" }
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceEditDialog(
    title: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.70f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Balance",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                }
            }

            Text(
                title,
                color = TextSecondary,
                fontSize = 12.sp
            )

            PayUTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "e.g. 25000",
                label = "Current balance",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                errorMessage = error
            )

            PayUButton(text = "Save Balance", onClick = onConfirm)
        }
    }
}

@Composable
private fun PaymentTypeChoiceChip(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
            .background(if (selected) TealGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) TealGreen else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 18.sp) }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}


@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}


