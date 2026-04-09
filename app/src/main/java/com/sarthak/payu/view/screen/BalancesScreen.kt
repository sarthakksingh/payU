package com.sarthak.payu.view.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.data.model.Category
import com.sarthak.payu.data.model.TransactionType
import com.sarthak.payu.ui.theme.*
import com.sarthak.payu.view.components.*
import com.sarthak.payu.vm.BalancesViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.absoluteValue

@Composable
fun BalancesScreen(
    onGoToCalendar: () -> Unit = {},
    viewModel: BalancesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val card = if (isDarkTheme) DarkCard else LightCard
    val outline = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else LightBorder
    val blush = if (isDarkTheme) Color(0xFF1E2748) else Color(0xFFF7DDE5)
    val beige = if (isDarkTheme) Color(0xFF1B2444) else Color(0xFFF1E6D8)
    val pagerState = rememberPagerState(pageCount = { 3 })
    var showDrawer by remember { mutableStateOf(false) }
    val drawerProgress = remember { Animatable(0f) }

    LaunchedEffect(showDrawer) {
        if (showDrawer) drawerProgress.animateTo(1f, animationSpec = tween(280))
        else drawerProgress.animateTo(0f, animationSpec = tween(240))
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
            modifier = Modifier.fillMaxSize()
        ) {
            PayUTopBar(
                paymentMethods = state.paymentMethods,
                onDrawerClick = { showDrawer = true },
                onCalendarClick = onGoToCalendar
            )

            if (state.isLoading) {
                BalancesShimmer()
            } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Your Balances",
                    color = primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    "Manage your finances",
                    color = secondaryText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                AnalyticsCarousel(
                    pagerState = pagerState,
                    score = state.healthScore,
                    allTransactions = state.allTransactions,
                    monthlySummary = state.monthlySummary,
                    isDarkTheme = isDarkTheme
                )

                Spacer(Modifier.height(18.dp))

                if (state.categoryBreakdown.isNotEmpty()) {
                    Text(
                        "Spending by Category",
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    CategoryBarChart(
                        breakdown = state.categoryBreakdown,
                        isDarkTheme = isDarkTheme,
                        cardColor = card,
                        trackColor = outline
                    )
                }

                Spacer(Modifier.height(20.dp))

                AnalyticsStatGrid(
                    allTransactions = state.allTransactions,
                    monthlySummary = state.monthlySummary,
                    isDarkTheme = isDarkTheme,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    blush = blush,
                    beige = beige
                )

                Spacer(Modifier.height(32.dp))
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
                    ) { showDrawer = false }
            )

            DraggablePaymentMethodsDrawerCard(
                modifier = Modifier
                    .padding(top = 74.dp, start = 14.dp, end = 14.dp),
                methods = state.paymentMethods,
                totalBalance = state.paymentMethods.sumOf { it.balance },
                drawerProgress = drawerProgress,
                primaryMethodId = state.paymentMethods.firstOrNull { it.isPrimary }?.id
                    ?: state.paymentMethods.firstOrNull()?.id,
                onSetPrimary = { },
                onAddMethod = { },
                onDismiss = { showDrawer = false }
            )
        }
    }
}
}

@Composable
private fun AnalyticsCarousel(
    pagerState: androidx.compose.foundation.pager.PagerState,
    score: Int,
    allTransactions: List<com.sarthak.payu.data.model.Transaction>,
    monthlySummary: com.sarthak.payu.data.model.MonthlySummary?,
    isDarkTheme: Boolean
) {
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val card = if (isDarkTheme) DarkCard else LightCard
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val accentStart = if (isDarkTheme) Color(0xFF6C63FF) else Color(0xFFA78BFA)
    val accentEnd = if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF0EA5E9)

    val now = remember { LocalDate.now() }
    val monthStart = remember(now) { now.withDayOfMonth(1) }
    val monthTransactions = remember(allTransactions, monthStart) {
        allTransactions.filter { !it.date.isBefore(monthStart) && !it.date.isAfter(monthStart.withDayOfMonth(monthStart.lengthOfMonth())) }
    }
    val dailySpending = remember(monthTransactions) {
        monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.date }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .toSortedMap()
    }
    val selectedWeek = remember(monthTransactions) {
        val wf = WeekFields.ISO
        monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.date.get(wf.weekOfWeekBasedYear()) to it.date.get(wf.weekBasedYear()) }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    }

    HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(320.dp),
        pageSpacing = 12.dp,
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) { page ->
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
        val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .graphicsLayer {
                    rotationZ = pageOffset * -9f
                    translationX = pageOffset * -28f
                    translationY = absOffset * 28f
                    scaleX = 1f - absOffset * 0.06f
                    scaleY = 1f - absOffset * 0.06f
                    alpha = 1f - absOffset * 0.15f
                }
        ) {
            when (page) {
                0 -> OverviewGaugeCard(
                    score = score,
                    monthlySummary = monthlySummary,
                    surface = surface,
                    card = card,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    accentStart = accentStart,
                    accentEnd = accentEnd,
                    isDarkTheme = isDarkTheme
                )
                1 -> MonthlyTrendCard(
                    dailySpending = dailySpending,
                    surface = surface,
                    card = card,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    accentStart = accentStart,
                    accentEnd = accentEnd,
                    isDarkTheme = isDarkTheme
                )
                else -> CategoryDonutCard(
                    categoryBreakdown = monthlySummary?.categoryBreakdown.orEmpty(),
                    surface = surface,
                    card = card,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    accentStart = accentStart,
                    accentEnd = accentEnd,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun OverviewGaugeCard(
    score: Int,
    monthlySummary: com.sarthak.payu.data.model.MonthlySummary?,
    surface: Color,
    card: Color,
    primaryText: Color,
    secondaryText: Color,
    accentStart: Color,
    accentEnd: Color,
    isDarkTheme: Boolean
) {
    val scoreLabel = when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Strong"
        score >= 40 -> "Average"
        else -> "Needs improvement"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Financial health", color = primaryText, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            }
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(card)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Score", color = secondaryText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text("$score/100", color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            HealthScoreGauge(
                score = score,
                isDarkTheme = isDarkTheme,
                primaryText = primaryText,
                secondaryText = secondaryText
            )
        }
        Text(
            "Based on your total data",
            color = secondaryText,
            fontSize = 12.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Income",
                value = "₹${"%,.0f".format(monthlySummary?.totalIncome ?: 0.0)}",
                color = accentStart,
                isDarkTheme = isDarkTheme,
                primaryText = primaryText,
                secondaryText = secondaryText,
                cardColor = card
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Expense",
                value = "₹${"%,.0f".format(monthlySummary?.totalExpense ?: 0.0)}",
                color = accentEnd,
                isDarkTheme = isDarkTheme,
                primaryText = primaryText,
                secondaryText = secondaryText,
                cardColor = card
            )
        }
    }
}

@Composable
private fun MonthlyTrendCard(
    dailySpending: Map<LocalDate, Double>,
    surface: Color,
    card: Color,
    primaryText: Color,
    secondaryText: Color,
    accentStart: Color,
    accentEnd: Color,
    isDarkTheme: Boolean
) {
    val maxAmount = (dailySpending.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val animated by animateFloatAsState(targetValue = 1f, animationSpec = tween(900, easing = FastOutSlowInEasing), label = "trend")
    val days: List<Pair<LocalDate, Double>> = dailySpending.entries
        .toList()
        .takeLast(10)
        .map { entry: Map.Entry<LocalDate, Double> -> entry.key to entry.value }
        .ifEmpty { listOf(LocalDate.now() to 0.0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Spending trend", color = primaryText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Daily expenses for the current month", color = secondaryText, fontSize = 12.sp)
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val w = size.width
            val h = size.height
            if (days.size < 2) return@Canvas
            val stepX = w / (days.size - 1)
            val points = days.mapIndexed { index, (_, amount) ->
                Offset(index * stepX, h - ((amount / maxAmount).toFloat() * h * 0.82f * animated))
            }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val midX = (prev.x + curr.x) / 2f
                    quadraticBezierTo(prev.x, prev.y, midX, (prev.y + curr.y) / 2f)
                }
                lineTo(points.last().x, points.last().y)
            }
            drawPath(path, brush = Brush.linearGradient(listOf(accentStart, accentEnd)), style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            points.forEach { point ->
                drawCircle(Color.White, 7f, point)
                drawCircle(accentStart, 4f, point)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard(
                Modifier.weight(1f),
                "Avg day",
                "₹${"%,.0f".format(dailySpending.values.average().takeIf { !it.isNaN() } ?: 0.0)}",
                accentStart,
                isDarkTheme = isDarkTheme,
                primaryText = primaryText,
                secondaryText = secondaryText,
                cardColor = card
            )
            SummaryCard(
                Modifier.weight(1f),
                "Peak day",
                "₹${"%,.0f".format(dailySpending.values.maxOrNull() ?: 0.0)}",
                accentEnd,
                isDarkTheme = isDarkTheme,
                primaryText = primaryText,
                secondaryText = secondaryText,
                cardColor = card
            )
        }
    }
}

@Composable
private fun CategoryDonutCard(
    categoryBreakdown: Map<Category, Double>,
    surface: Color,
    card: Color,
    primaryText: Color,
    secondaryText: Color,
    accentStart: Color,
    accentEnd: Color,
    isDarkTheme: Boolean
) {
    val sorted = categoryBreakdown.entries.sortedByDescending { it.value }.take(5)
    val total = categoryBreakdown.values.sum().takeIf { it > 0 } ?: 1.0
    val palette = sorted.map { (category, _) -> categoryColor(category) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Category mix", color = primaryText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Where the money went this month", color = secondaryText, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(126.dp)
                    .graphicsLayer { rotationZ = -16f }
            ) {
                val strokeWidth = size.minDimension * 0.18f
                val inset = strokeWidth / 2f + 4f
                val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                val arcTopLeft = Offset(inset, inset)
                var startAngle = -90f

                drawArc(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else LightBorder,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                if (sorted.isEmpty()) {
                    drawCircle(color = card, radius = size.minDimension * 0.26f)
                } else {
                    sorted.forEachIndexed { index, entry ->
                        val rawSweep = ((entry.value / total) * 360f).toFloat()
                        val sweep = (rawSweep - 5f).coerceAtLeast(3f)
                        drawArc(
                            color = palette[index % palette.size],
                            startAngle = startAngle + 2f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += rawSweep
                    }
                    drawCircle(color = surface, radius = size.minDimension * 0.25f)
                }
            }
        }
        Text(
            "Legend",
            color = primaryText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        if (sorted.isEmpty()) {
            Text("No category data yet.", color = secondaryText, fontSize = 10.sp)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sorted) { entry ->
                    val color = categoryColor(entry.key)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(card)
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                "${entry.key.emoji} ${entry.key.label}",
                                color = primaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "₹${"%,.0f".format(entry.value)}",
                            color = secondaryText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun categoryColor(category: Category): Color {
    return when (category) {
        else -> Color(android.graphics.Color.parseColor(category.colorHex))
    }
}

@Composable
private fun AnalyticsStatGrid(
    allTransactions: List<com.sarthak.payu.data.model.Transaction>,
    monthlySummary: com.sarthak.payu.data.model.MonthlySummary?,
    isDarkTheme: Boolean,
    primaryText: Color,
    secondaryText: Color,
    blush: Color,
    beige: Color
) {
    val wf = WeekFields.ISO
    val expenseTxns = allTransactions.filter { it.type == TransactionType.EXPENSE }
    val incomeTxns = allTransactions.filter { it.type == TransactionType.INCOME }
    val mostSpentDay = expenseTxns.groupBy { it.date }.maxByOrNull { (_, txns) -> txns.sumOf { it.amount } }
    val mostSpentWeek = expenseTxns.groupBy { it.date.get(wf.weekBasedYear()) to it.date.get(wf.weekOfWeekBasedYear()) }
        .maxByOrNull { (_, txns) -> txns.sumOf { it.amount } }
    val mostExpensiveItem = expenseTxns.maxByOrNull { it.amount }
    val highestIncome = incomeTxns.maxByOrNull { it.amount }
    val biggestMonthSpend = monthlySummary?.totalExpense ?: expenseTxns.sumOf { it.amount }
    val totalTransactions = allTransactions.size
    val expenseToIncomeRatio = if ((monthlySummary?.totalIncome ?: 0.0) > 0.0) {
        (monthlySummary?.totalExpense ?: 0.0) / monthlySummary!!.totalIncome
    } else {
        null
    }
    val cards = listOf(
        Triple("Most in a day", mostSpentDay?.value?.sumOf { it.amount }?.let { "₹${"%,.0f".format(it)}" } ?: "₹0", "${mostSpentDay?.key?.dayOfMonth ?: 0}"),
        Triple("Most in a week", mostSpentWeek?.value?.sumOf { it.amount }?.let { "₹${"%,.0f".format(it)}" } ?: "₹0", "Week"),
        Triple("Monthly spend", "₹${"%,.0f".format(biggestMonthSpend)}", "This month"),
        Triple("Biggest item", mostExpensiveItem?.let { "₹${"%,.0f".format(it.amount)}" } ?: "₹0", mostExpensiveItem?.note?.ifBlank { mostExpensiveItem.category.label } ?: "None"),
        Triple("Highest income", highestIncome?.let { "₹${"%,.0f".format(it.amount)}" } ?: "₹0", highestIncome?.note?.ifBlank { highestIncome.category.label } ?: "None"),
        Triple("Spend ratio", expenseToIncomeRatio?.let { "${(it * 100).toInt()}%" } ?: "N/A", "Expense vs income")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Stats", color = primaryText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            cards.chunked(2).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { idx, item ->
                        AnalyticsStatCard(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            title = item.first,
                            value = item.second,
                            subtitle = item.third,
                            isDarkTheme = isDarkTheme,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            accent = when ((rowIndex + idx) % 3) {
                                0 -> blush
                                1 -> beige
                                else -> Color(0xFFB6A6FF)
                            },
                            backgroundTint = if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color.White
                        )
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    isDarkTheme: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accent: Color,
    backgroundTint: Color
) {
    val card = if (isDarkTheme) DarkCard else LightSurface
    val backText = playfulInsight(title, value, subtitle)
    var flipped by remember(title) { mutableStateOf(false) }
    val flip = animateFloatAsState(
        targetValue = if (flipped) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "stat_flip"
    ).value

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(card)
            .border(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.05f) else LightBorder, RoundedCornerShape(22.dp))
            .clickable { flipped = !flipped }
            .graphicsLayer {
                rotationY = flip * 180f
                cameraDistance = 24f
            }
    ) {
        val frontAlpha = if (flip < 0.5f) 1f else 0f
        val backAlpha = if (flip >= 0.5f) 1f else 0f

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    alpha = frontAlpha
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accent.copy(alpha = if (isDarkTheme) 0.95f else 0.78f),
                                if (isDarkTheme) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("↻", color = if (isDarkTheme) Color.Black else TextPrimaryLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    accent.copy(alpha = if (isDarkTheme) 0.92f else 0.78f),
                                    backgroundTint
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(frontLabel(title), color = if (isDarkTheme) Color.Black else TextPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, color = secondaryText, fontSize = 11.sp)
                    Text(value, color = primaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = secondaryText, fontSize = 10.sp)
                    Text(flipHint(title), color = secondaryText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    rotationY = 180f
                    alpha = backAlpha
                    cameraDistance = 24f
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    accent.copy(alpha = if (isDarkTheme) 0.92f else 0.78f),
                                    backgroundTint
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(backLabel(title), color = if (isDarkTheme) Color.Black else TextPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tap again to flip back", color = secondaryText, fontSize = 10.sp)
                    Text(backText, color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
                    Text(backDetail(title), color = secondaryText, fontSize = 10.sp, lineHeight = 13.sp)
                }
            }
        }
    }
}

private fun frontLabel(title: String): String = when (title) {
    "Most in a day" -> "DAY"
    "Most in a week" -> "WK"
    "Monthly spend" -> "MO"
    "Biggest item" -> "BIG"
    "Highest income" -> "INC"
    "Spend ratio" -> "%"
    else -> "ST"
}

private fun backLabel(title: String): String = when (title) {
    "Most in a day" -> "DAY"
    "Most in a week" -> "WK"
    "Monthly spend" -> "MO"
    "Biggest item" -> "BIG"
    "Highest income" -> "INC"
    "Spend ratio" -> "%"
    else -> "ST"
}

private fun flipHint(title: String): String = when (title) {
    "Most in a day" -> "Tap for a wallet roast"
    "Most in a week" -> "Tap for weekly chaos"
    "Monthly spend" -> "Tap for month mood"
    "Biggest item" -> "Tap for the splurge"
    "Highest income" -> "Tap for the victory lap"
    "Spend ratio" -> "Tap for the reality check"
    else -> "Tap to flip"
}

private fun backDetail(title: String): String = when (title) {
    "Most in a day" -> "That day was your wallet's busiest shift."
    "Most in a week" -> "Weekly spending said: let me just do one more thing."
    "Monthly spend" -> "The month committed to the bit."
    "Biggest item" -> "That purchase definitely had a dramatic entrance."
    "Highest income" -> "Respect. That income showed up on time."
    "Spend ratio" -> "A tidy ratio, if by tidy we mean emotionally complicated."
    else -> "Your money is trying its best."
}

private fun playfulInsight(title: String, value: String, subtitle: String): String = when (title) {
    "Most in a day" -> "That day was your wallet's busiest shift."
    "Most in a week" -> "Weekly spending said: let me just do one more thing."
    "Monthly spend" -> "The month committed to the bit."
    "Biggest item" -> "That purchase definitely had a dramatic entrance."
    "Highest income" -> "Respect. That income showed up on time."
    "Spend ratio" -> "A tidy ratio, if by tidy we mean emotionally complicated."
    else -> "$title keeps the plot moving."
}
@Composable
fun HealthScoreGauge(
    score: Int,
    isDarkTheme: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "gauge_anim"
    )

    // Outer box: wide + tall enough so arc + text never overlap
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(252.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val scoreLabel = when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Strong"
            score >= 40 -> "Average"
            else -> "Needs improvement"
        }
        // Canvas draws only the arc — occupies top 180dp
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .align(Alignment.TopCenter)
        ) {
            val strokeWidth = 22.dp.toPx()
            // Use 85% of half-width so arc doesn't touch edges
            val radius = (size.width * 0.85f / 2f) - strokeWidth
            val cx = size.width / 2f
            // Place arc centre at bottom of canvas so the semicircle fits fully
            val cy = size.height - strokeWidth / 2f
            val startAngle = 180f
            val totalSweep = 180f

            fun arcOffset() = Offset(cx - radius, cy - radius)
            fun arcSize() = Size(radius * 2, radius * 2)

            // Background track
            drawArc(
                if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFE7E2DA),
                startAngle,
                totalSweep,
                false,
                arcOffset(), arcSize(), style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Teal (0–45%)
            drawArc(TealGreen, startAngle, totalSweep * 0.45f, false,
                arcOffset(), arcSize(), style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Pink (45–65%)
            drawArc(PinkAccent, startAngle + totalSweep * 0.45f, totalSweep * 0.20f, false,
                arcOffset(), arcSize(), style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Blue (65–80%)
            drawArc(BlueAccent, startAngle + totalSweep * 0.65f, totalSweep * 0.15f, false,
                arcOffset(), arcSize(), style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Gold (80–100%)
            drawArc(GoldAccent, startAngle + totalSweep * 0.80f, totalSweep * 0.20f, false,
                arcOffset(), arcSize(), style = Stroke(strokeWidth, cap = StrokeCap.Round))

            // Indicator dot
            val angle = startAngle + (animatedScore / 100f) * totalSweep
            val rad = Math.toRadians(angle.toDouble())
            val dotX = cx + radius * cos(rad).toFloat()
            val dotY = cy + radius * sin(rad).toFloat()
            drawCircle(if (isDarkTheme) Color.White else primaryText, strokeWidth / 2f, Offset(dotX, dotY))
            drawCircle(BlueAccent, strokeWidth / 3f, Offset(dotX, dotY))
        }

        // Text block sits below the arc — no overlap possible
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$score",
                color = primaryText,
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                lineHeight = 50.sp
            )
            Text(
                "$scoreLabel financial health",
                color = primaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun CategoryBarChart(
    breakdown: Map<Category, Double>,
    isDarkTheme: Boolean,
    cardColor: Color,
    trackColor: Color
) {
    val maxAmount = breakdown.values.maxOrNull() ?: 1.0
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "bar_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        breakdown.entries.sortedByDescending { it.value }.take(4).forEach { (category, amount) ->
            val fraction = ((amount / maxAmount) * animatedProgress).toFloat()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${category.emoji} ${category.label}",
                        color = if (isDarkTheme) Color.White else TextPrimaryLight,
                        fontSize = 13.sp
                    )
                    Text(
                        "₹${"%,.0f".format(amount)}",
                        color = if (isDarkTheme) TextSecondary else TextSecondaryLight,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(trackColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(TealGreen, PinkAccent)))
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color,
    isDarkTheme: Boolean,
    primaryText: Color,
    secondaryText: Color,
    cardColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .padding(12.dp)
    ) {
        Text(title, color = secondaryText, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = if (isDarkTheme) color else primaryText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun BalancesShimmer() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(260.dp), shape = RoundedCornerShape(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { ShimmerBox(modifier = Modifier.weight(1f).height(70.dp)) }
        }
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp))
    }
}




