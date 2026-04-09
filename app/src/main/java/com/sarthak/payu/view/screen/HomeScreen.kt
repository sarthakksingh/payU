package com.sarthak.payu.view.screen


import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.model.TransactionType
import com.sarthak.payu.data.model.paymentMethodDisplayName
import com.sarthak.payu.ui.theme.DarkBorder
import com.sarthak.payu.ui.theme.DarkCard
import com.sarthak.payu.ui.theme.DarkSurface
import com.sarthak.payu.ui.theme.LightBorder
import com.sarthak.payu.ui.theme.LightCard
import com.sarthak.payu.ui.theme.LightSurface
import com.sarthak.payu.ui.theme.TealGreen
import com.sarthak.payu.ui.theme.TealGreenDark
import com.sarthak.payu.ui.theme.TextPrimaryLight
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.ui.theme.TextSecondaryLight
import com.sarthak.payu.view.components.BankLogoBadge
import com.sarthak.payu.view.components.FlippableHeroPaymentCard
import com.sarthak.payu.view.components.GradientCard
import com.sarthak.payu.view.components.PayUButton
import com.sarthak.payu.view.components.PayUTopBar
import com.sarthak.payu.view.components.ShimmerBox
import com.sarthak.payu.vm.HomeViewModel
import com.sarthak.payu.vm.PeriodHeaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onGoToCalendar: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val screenPrimaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val screenSecondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val screenSurface = if (isDarkTheme) DarkSurface else LightSurface
    val screenCard = if (isDarkTheme) DarkCard else LightCard
    val screenBorder = if (isDarkTheme) DarkBorder else LightBorder
    val screenOverlay = if (isDarkTheme) Color.Black.copy(alpha = 0.42f) else Color.Black.copy(alpha = 0.08f)
    val drawerGradient = if (isDarkTheme) {
        listOf(Color(0xFF1B1736), Color(0xFF10182F), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFF4EFE6), Color(0xFFE9E1D6), Color(0xFFDCD1C0))
    }
    val headerStart = if (isDarkTheme) Color(0xFF1F2937) else Color(0xFFEDE5D8)
    val headerEnd = if (isDarkTheme) Color(0xFF17455C) else Color(0xFFD9CDBD)
    var showDrawer by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var showTransactionSearch by remember { mutableStateOf(false) }
    var transactionSearch by remember { mutableStateOf("") }
    val hints = remember {
        listOf(
            "Here's your financial overview",
            "Tap on the card to see back",
            "Double tap on the card to add or remove filter",
            "Tap on the calendar to see heatmap"
        )
    }
    var hintIndex by remember { mutableIntStateOf(0) }
    val drawerProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            hintIndex = (hintIndex + 1) % hints.size
        }
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

    // Also close search on back press if open
    BackHandler(enabled = showTransactionSearch) {
        showTransactionSearch = false
        transactionSearch = ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Main content column ───────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            PayUTopBar(
                paymentMethods = state.paymentMethods,
                onDrawerClick = { showDrawer = !showDrawer },
                onCalendarClick = onGoToCalendar
            )

            if (state.isLoading) {
                HomeShimmer()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Hey, ${state.userName.ifBlank { "there" }} 👋",
                        color = screenPrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    AnimatedContent(
                        targetState = hints[hintIndex],
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()).togetherWith(
                                slideOutVertically { -it } + fadeOut()
                            )
                        },
                        label = "home_hint"
                    ) { hint ->
                        Text(
                            hint,
                            color = screenSecondaryText,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                        )
                    }

                    PaymentMethodHeroCarousel(
                        methods = state.paymentMethods,
                        userName = state.userName,
                        selectedMethodId = state.selectedPaymentMethodId,
                        monthlyExpenseById = state.paymentMethodMonthlyExpenseById,
                        onMethodClick = viewModel::togglePaymentMethodFilter
                    )

                    Spacer(Modifier.height(14.dp))

                    AnimatedPeriodChips(
                        options = listOf("Recent", "Weekly", "Monthly"),
                        selectedIndex = state.selectedTab,
                        onSelect = viewModel::setTab
                    )

                    Spacer(Modifier.height(14.dp))

                    AnimatedPeriodContent(
                        selectedTab = state.selectedTab,
                        header = state.periodHeader,
                        transactions = state.displayTransactions,
                        paymentMethods = state.paymentMethods,
                        selectedPaymentMethodId = state.selectedPaymentMethodId,
                        searchQuery = transactionSearch,
                        showSearchBar = showTransactionSearch,
                        onSearchToggle = { showTransactionSearch = !showTransactionSearch },
                        onSearchQueryChange = { transactionSearch = it },
                        onAddClick = onAddTransaction,
                        onDelete = { transaction ->
                            scope.launch {
                                viewModel.deleteTransaction(transaction)
                                val result = snackbarHostState.showSnackbar(
                                    message = "Transaction deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreTransaction(transaction)
                                }
                            }
                        }
                    )
                }
            }
        }

        // ── Drawer overlay ────────────────────────────────────────
        if (drawerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenOverlay)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showDrawer = false }
            )
            DraggablePaymentMethodsDrawerCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp, start = 14.dp, end = 14.dp),
                methods = state.paymentMethods,
                totalBalance = state.paymentMethods.sumOf { it.balance },
                drawerProgress = drawerProgress,
                primaryMethodId = state.primaryPaymentMethod?.id,
                onSetPrimary = viewModel::setPrimaryPaymentMethod,
                onAddMethod = { showAddMethodDialog = true },
                onDismiss = { showDrawer = false }
            )
        }

        // ── FAB ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = if (isDarkTheme) Color.White else Color(0xFFF0D7C7),
                contentColor = if (isDarkTheme) Color.Black else Color(0xFF2A221F)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }

        // ── Snackbar ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }

        // ── Search overlay — LIFTED HERE, above everything ────────
        // Uses imePadding() at this layer so it correctly clears
        // the keyboard on all devices without relying on inset
        // propagation through AnimatedContent.
        AnimatedVisibility(
            visible = showTransactionSearch,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // imePadding pushes this Box up by exactly the keyboard height
                .imePadding()
                // Extra breathing room above the keyboard
                .padding(horizontal = 16.dp, vertical = 12.dp),
            enter = slideInVertically { it } + fadeIn(tween(180)),
            exit = slideOutVertically { it } + fadeOut(tween(140))
        ) {
            val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
            val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
            OutlinedTextField(
                value = transactionSearch,
                onValueChange = { transactionSearch = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                placeholder = { Text("Search transactions", color = secondaryText) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TealGreen)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    focusedBorderColor = TealGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedPlaceholderColor = secondaryText,
                    unfocusedPlaceholderColor = secondaryText
                )
            )
        }
    }

    // ── Add payment method dialog ─────────────────────────────────
    if (showAddMethodDialog) {
        AddPaymentMethodDialog(
            atLimit = state.paymentMethods.size >= 5,
            onDismiss = { showAddMethodDialog = false },
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
                showAddMethodDialog = false
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteTransaction(
    transaction: Transaction,
    paymentMethod: PaymentMethod?,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == EndToStart) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        TransactionItem(transaction = transaction, paymentMethod = paymentMethod)
    }
}

@Composable
fun PeriodSummaryCard(header: PeriodHeaderState, selectedTab: Int) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val transition = updateTransition(targetState = selectedTab, label = "periodCardTransition")
    val startColor by transition.animateColor(
        label = "periodStartColor"
    ) { tab ->
        if (isDarkTheme) {
            when (tab) {
                0 -> Color(0xFF2E3A4C)
                1 -> Color(0xFF1B4D6B)
                else -> Color(0xFF2F3B55)
            }
        } else {
            when (tab) {
                0 -> Color(0xFFF1E8DD)
                1 -> Color(0xFFECE2D4)
                else -> Color(0xFFF5ECDD)
            }
        }
    }
    val endColor by transition.animateColor(
        label = "periodEndColor"
    ) { tab ->
        if (isDarkTheme) {
            when (tab) {
                0 -> Color(0xFF1F2937)
                1 -> Color(0xFF17455C)
                else -> Color(0xFF1D2438)
            }
        } else {
            when (tab) {
                0 -> Color(0xFFE8DDD0)
                1 -> Color(0xFFDCCFBF)
                else -> Color(0xFFE8E0D4)
            }
        }
    }
    AnimatedContent(
        targetState = header,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220)) + scaleIn(animationSpec = tween(220), initialScale = 0.96f))
                .togetherWith(
                    fadeOut(animationSpec = tween(160)) + scaleOut(animationSpec = tween(160), targetScale = 0.98f)
                )
        },
        label = "PeriodSummaryCard"
    ) { current ->
        GradientCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp),
            gradientColors = listOf(startColor, endColor)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        current.title,
                        color = primaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        current.subtitle,
                        color = secondaryText,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        current.amountLabel,
                        color = secondaryText,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    Text(
                        "₹${"%,.0f".format(current.amount)}",
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsDrawerCard(
    modifier: Modifier = Modifier,
    methods: List<PaymentMethod>,
    totalBalance: Double,
    drawerProgress: Animatable<Float, AnimationVector1D>,
    onAddMethod: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgGradient = if (isDarkTheme) {
        listOf(Color(0xFF1B1736), Color(0xFF10182F), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFF4EFE6), Color(0xFFE8DDCF), Color(0xFFD9CCB9))
    }
    val titleColor = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val cardColor = if (isDarkTheme) Color(0xFF232A4C) else LightCard
    val surfaceColor = if (isDarkTheme) Color(0xFF1A2040) else LightSurface
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else LightBorder
    val iconBackdrop = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(colors = bgGradient))
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Balances", color = titleColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Total across all payment methods",
                    color = bodyColor,
                    fontSize = 12.sp
                )
            }
            Text(
                "₹${"%,.0f".format(totalBalance)}",
                color = titleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (methods.isEmpty()) {
            Text(
                "No payment methods yet. Add one from here or from Profile.",
                color = bodyColor,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                methods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardColor)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(iconBackdrop),
                                contentAlignment = Alignment.Center
                            ) {
                                BankLogoBadge(
                                    bankName = method.bankName,
                                    size = 26.dp,
                                    logoSize = 16.dp,
                                    background = surfaceColor
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        paymentMethodDisplayName(method),
                                        color = titleColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (method.isPrimary) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (isDarkTheme) TealGreen.copy(alpha = 0.16f) else Color(0xFFD6ECE8))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                "Primary",
                                                color = if (isDarkTheme) TealGreen else TealGreenDark,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "Remaining balance",
                                    color = bodyColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            "₹${"%,.0f".format(method.balance)}",
                            color = titleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        PayUButton(
            text = "Add payment method",
            onClick = onAddMethod,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DraggablePaymentMethodsDrawerCard(
    modifier: Modifier = Modifier,
    methods: List<PaymentMethod>,
    totalBalance: Double,
    drawerProgress: Animatable<Float, AnimationVector1D>,
    primaryMethodId: String?,
    onSetPrimary: (String) -> Unit,
    onAddMethod: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgGradient = if (isDarkTheme) {
        listOf(Color(0xFF1B1736), Color(0xFF10182F), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFF4EFE6), Color(0xFFE9E1D6), Color(0xFFDCD1C0))
    }
    val titleColor = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyColor = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val cardColor = if (isDarkTheme) Color(0xFF232A4C) else LightCard
    val surfaceColor = if (isDarkTheme) Color(0xFF1A2040) else LightSurface
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else LightBorder
    val iconBackdrop = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
    val bankBadgeBg = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.04f)

    val scope = rememberCoroutineScope()
    val progress = drawerProgress.value.coerceIn(0f, 1f)
    val collapsedHeight = 68.dp
    val expandedHeight = lerp(collapsedHeight, 500.dp, progress)
    val contentAlpha = ((progress - 0.08f) / 0.28f).coerceIn(0f, 1f)
    val listFade = ((progress - 0.18f) / 0.18f).coerceIn(0f, 1f)
    val topAvatars = methods.take(3)
    val visibleMethods = remember(methods) { methods }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(expandedHeight)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = if (isDarkTheme) 0.28f else 0.12f),
                    ambientColor = Color.Black.copy(alpha = if (isDarkTheme) 0.18f else 0.08f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(colors = bgGradient)
                )
                .border(1.dp, borderColor, RoundedCornerShape(32.dp))
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta: Float ->
                        scope.launch {
                            drawerProgress.snapTo(
                                (drawerProgress.value + (delta / 450f)).coerceIn(0f, 1f)
                            )
                        }
                    },
                    onDragStopped = { velocity: Float ->
                        scope.launch {
                            val target = when {
                                velocity > 250f -> 0f
                                velocity < -250f -> 1f
                                drawerProgress.value > 0.5f -> 1f
                                else -> 0f
                            }
                            if (target == 0f) {
                                onDismiss()
                            } else {
                                drawerProgress.animateTo(1f, animationSpec = tween(220))
                            }
                        }
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .graphicsLayer { alpha = contentAlpha },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment methods",
                    color = titleColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (topAvatars.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(iconBackdrop)
                        )
                    } else {
                        topAvatars.forEachIndexed { index, method ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(iconBackdrop),
                                contentAlignment = Alignment.Center
                            ) {
                                BankLogoBadge(
                                    bankName = method.bankName,
                                    size = 22.dp,
                                    logoSize = 14.dp,
                                    background = Color.Transparent
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(iconBackdrop)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                            ) { onAddMethod() },
                        contentAlignment = Alignment.Center
                    ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add method",
                                tint = titleColor,
                                modifier = Modifier.size(18.dp)
                            )
                    }
                }
            }

            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = contentAlpha
                    translationY = (1f - progress) * 18f
                },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Remaining balances",
                            color = titleColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap outside to close",
                            color = bodyColor,
                            fontSize = 12.sp
                        )
                    }
                        Text(
                            "₹${"%,.0f".format(totalBalance)}",
                            color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .graphicsLayer { alpha = listFade },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleMethods, key = { it.id }) { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSetPrimary(method.id) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(surfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                BankLogoBadge(
                                    bankName = method.bankName,
                                    size = 26.dp,
                                    logoSize = 16.dp,
                                    background = bankBadgeBg
                                )
                            }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            paymentMethodDisplayName(method),
                                            color = titleColor,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        if (method.id == primaryMethodId || method.isPrimary) {
                                            Spacer(Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(surfaceColor)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    "Primary",
                                                    color = titleColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                        Text(
                                            "Tap to set primary",
                                            color = bodyColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                "₹${"%,.0f".format(method.balance)}",
                                color = titleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                PayUButton(
                    text = "Add payment method",
                    onClick = onAddMethod,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = contentAlpha }
                )
            }
        }
    }
}

@Composable
fun AnimatedPeriodChips(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val container = if (isDarkTheme) DarkCard else LightSurface
    val indicator = if (isDarkTheme) Color.White else Color(0xFFF6D7E0)
    val selectedText = if (isDarkTheme) Color.Black else Color(0xFF251B20)
    val unselectedText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val bounds = remember(options.size) { mutableStateOf(List(options.size) { Rect.Zero }) }
    val target = bounds.value.getOrNull(selectedIndex) ?: Rect.Zero

    val indicatorX by animateDpAsState(
        targetValue = if (target.width == 0f) 0.dp else with(density) { target.left.toDp() },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 240f),
        label = "chipIndicatorX"
    )
    val indicatorW by animateDpAsState(
        targetValue = if (target.width == 0f) 0.dp else with(density) { target.width.toDp() },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 240f),
        label = "chipIndicatorW"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(4.dp)
    ) {
        if (target.width > 0f) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorX)
                    .width(indicatorW)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(indicator)
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            bounds.value = bounds.value.toMutableList().also { list ->
                                if (index < list.size) {
                                    list[index] = Rect(
                                        pos.x,
                                        pos.y,
                                        pos.x + coords.size.width,
                                        pos.y + coords.size.height
                                    )
                                }
                            }
                        }
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) selectedText else unselectedText,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedPeriodContent(
    selectedTab: Int,
    header: PeriodHeaderState,
    transactions: List<Transaction>,
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethodId: String?,
    searchQuery: String,
    showSearchBar: Boolean,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onDelete: (Transaction) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val paymentMethodsById = remember(paymentMethods) { paymentMethods.associateBy { it.id } }
    val scope = rememberCoroutineScope()
    val visibleTransactions = remember(transactions, searchQuery) {
        if (searchQuery.isBlank()) transactions
        else {
            val query = searchQuery.trim().lowercase()
            transactions.filter { txn ->
                txn.category.label.lowercase().contains(query) ||
                        txn.note.lowercase().contains(query) ||
                        txn.amount.toString().contains(query)
            }
        }
    }
    val recentState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val weeklyState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val monthlyState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val listState = when (selectedTab) {
        0 -> recentState
        1 -> weeklyState
        else -> monthlyState
    }

    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            when (targetState) {
                0 -> (slideInHorizontally(animationSpec = tween(320)) { -it } + fadeIn(animationSpec = tween(220)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(animationSpec = tween(160)))
                1 -> (slideInVertically(animationSpec = tween(320)) { -it } + fadeIn(animationSpec = tween(220)))
                    .togetherWith(slideOutVertically(animationSpec = tween(240)) { -it } + fadeOut(animationSpec = tween(160)))
                else -> (slideInHorizontally(animationSpec = tween(320)) { it } + fadeIn(animationSpec = tween(220)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(animationSpec = tween(160)))
            }
        },
        label = "PeriodContentSwitcher"
    ) { tab ->
        // Search overlay is gone from here — it now lives in HomeScreen's
        // top-level Box so imePadding() works correctly on all devices.
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // Extra bottom padding when search bar is visible so last
            // item isn't hidden behind the floating search field.
            contentPadding = PaddingValues(
                bottom = if (showSearchBar) 160.dp else 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PeriodSummaryCard(header = header, selectedTab = selectedTab)
            }
            item {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your expenses",
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onSearchToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search transactions",
                            tint = if (showSearchBar || searchQuery.isNotBlank()) TealGreen else secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (selectedPaymentMethodId != null) {
                    val activeMethod = paymentMethodsById[selectedPaymentMethodId]
                    Text(
                        "Filtered by ${activeMethod?.let { paymentMethodDisplayName(it) } ?: "selected method"}",
                        color = secondaryText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            if (visibleTransactions.isEmpty()) {
                item { EmptyTransactionsState(onAddClick = onAddClick) }
            } else {
                items(visibleTransactions, key = { it.id }) { transaction ->
                    SwipeToDeleteTransaction(
                        transaction = transaction,
                        paymentMethod = paymentMethodsById[transaction.paymentMethodId],
                        onDelete = { onDelete(transaction) }
                    )
                }
            }
        }
    }
}
@Composable
fun TransactionItem(
    transaction: Transaction,
    paymentMethod: PaymentMethod?
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val rowSurface = if (isDarkTheme) DarkSurface else LightSurface
    val iconSurface = if (isDarkTheme) DarkCard else LightCard
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val badgeBg = if (isDarkTheme) TealGreen.copy(alpha = 0.12f) else Color(0xFFD6ECE8)
    val badgeLogoBg = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.04f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(rowSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(transaction.category.emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.category.label,
                color = titleText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                transaction.note.ifBlank { transaction.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) },
                color = bodyText,
                fontSize = 12.sp
            )
            if (paymentMethod != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (paymentMethod.type == com.sarthak.payu.data.model.PaymentMethodType.CASH) {
                        Text("₹", fontSize = 11.sp)
                    } else {
                        BankLogoBadge(
                            bankName = paymentMethod.bankName,
                            size = 18.dp,
                            logoSize = 12.dp,
                            background = badgeLogoBg
                        )
                    }
                    Text(
                        paymentMethodDisplayName(paymentMethod),
                        color = if (isDarkTheme) TealGreen else TealGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val isIncome = transaction.type == TransactionType.INCOME
            Text(
                "${if (isIncome) "+" else "-"}₹${"%,.0f".format(transaction.amount)}",
                color = if (isIncome) TealGreen else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Icon(
                if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isIncome) TealGreen else Color(0xFFEF4444),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SummaryChip(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    isIncome: Boolean
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isIncome) TealGreen.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isIncome) TealGreen else Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = secondaryText, fontSize = 12.sp)
            Text(
                "₹${"%,.0f".format(amount)}",
                color = titleText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PaymentMethodHeroCarousel(
    methods: List<PaymentMethod>,
    userName: String,
    selectedMethodId: String?,
    monthlyExpenseById: Map<String, Double>,
    onMethodClick: (String?) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val emptyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val shimmerTransition = rememberInfiniteTransition(label = "heroCardShimmer")
    val shimmerProgress by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroShimmerProgress"
    )
    val orderedMethods = remember(methods) {
        methods.sortedWith(compareByDescending<PaymentMethod> { it.isPrimary }.thenBy { it.bankName })
    }
    val cardWidth = (LocalConfiguration.current.screenWidthDp.dp * 0.84f)
    val listState = rememberLazyListState()
    val centeredMethodId by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + (item.size / 2)) - center)
            }?.let { item -> orderedMethods.getOrNull(item.index)?.id }
        }
    }

    if (orderedMethods.isEmpty()) {
        Text(
            "No payment methods yet. Add one from Profile.",
            color = emptyText,
            fontSize = 12.sp
        )
        return
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(orderedMethods, key = { it.id }) { method ->
            val centered = centeredMethodId == method.id
            val filtered = selectedMethodId == method.id
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { info -> info.index == orderedMethods.indexOf(method) }
            val distanceFraction = itemInfo?.let {
                val itemCenter = it.offset + (it.size / 2)
                ((kotlin.math.abs(itemCenter - viewportCenter)).toFloat() / (cardWidth.value * 0.72f)).coerceIn(0f, 1f)
            } ?: if (centered) 0f else 1f
            val dullness = distanceFraction.coerceIn(0f, 1f)
            FlippableHeroPaymentCard(
                method = method,
                userName = userName,
                selectedMethodId = selectedMethodId,
                centered = centered,
                dullness = dullness,
                monthlySpent = monthlyExpenseById[method.id] ?: 0.0,
                modifier = Modifier
                    .width(cardWidth)
                    .height(204.dp)
                    .padding(vertical = if (centered) 0.dp else 10.dp),
                onTap = { onMethodClick(method.id) }
            )
        }
    }
}

@Composable
fun EmptyTransactionsState(onAddClick: () -> Unit) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💸", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text("No transactions yet", color = titleText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text("Add your first transaction to get started", color = bodyText, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        PayUButton(text = "Add Transaction", onClick = onAddClick, modifier = Modifier.width(200.dp))
    }
}

@Composable
fun HomeShimmer() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(70.dp))
            ShimmerBox(modifier = Modifier.weight(1f).height(70.dp))
        }
        repeat(4) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(72.dp))
        }
    }
}
