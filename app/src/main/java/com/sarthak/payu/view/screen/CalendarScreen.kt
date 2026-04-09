package com.sarthak.payu.view.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.model.TransactionType
import com.sarthak.payu.ui.theme.DarkCard
import com.sarthak.payu.ui.theme.DarkSurface
import com.sarthak.payu.ui.theme.LightBg
import com.sarthak.payu.ui.theme.LightBorder
import com.sarthak.payu.ui.theme.LightCard
import com.sarthak.payu.ui.theme.LightSurface
import com.sarthak.payu.ui.theme.TextPrimaryLight
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.ui.theme.TextSecondaryLight
import com.sarthak.payu.vm.CalendarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val today = remember { LocalDate.now() }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val screenBg = if (isDarkTheme) MaterialTheme.colorScheme.background else LightBg
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val card = if (isDarkTheme) DarkCard else LightCard
    val border = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else LightBorder
    val monthStart = remember(today) { today.withDayOfMonth(1) }
    val monthEnd = remember(monthStart) { monthStart.withDayOfMonth(monthStart.lengthOfMonth()) }
    val monthLabel = remember(monthStart) {
        monthStart.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    val monthTransactions = remember(state.transactions, monthStart, monthEnd) {
        state.transactions.filter { !it.date.isBefore(monthStart) && !it.date.isAfter(monthEnd) }
    }
    val expenseByDate = remember(monthTransactions) {
        monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.date }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
    }
    val selectedMax = expenseByDate.values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val scope = rememberCoroutineScope()

    val selectedDateState = rememberPagerState(
        initialPage = (today.dayOfMonth - 1).coerceIn(0, monthStart.lengthOfMonth() - 1),
        pageCount = { monthStart.lengthOfMonth() }
    )
    val selectedDate = remember(selectedDateState.currentPage, monthStart) {
        monthStart.plusDays(selectedDateState.currentPage.toLong())
    }
    val selectedDateTransactions = remember(state.transactions, selectedDate) {
        state.transactions
            .filter { it.date == selectedDate }
            .sortedByDescending { it.createdAt }
    }
    val selectedExpense = selectedDateTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDarkTheme) Color.Black else Color.White)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDarkTheme) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            monthStart.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            color = secondaryText.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            monthLabel,
                            color = primaryText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

            }

            Spacer(Modifier.height(22.dp))

            DateScroller(
                pagerState = selectedDateState,
                monthStart = monthStart
            )

            Spacer(Modifier.height(14.dp))

    HeatmapCard(
                monthStart = monthStart,
                expenseByDate = expenseByDate,
                selectedDate = selectedDate,
                maxExpense = selectedMax,
                isDarkTheme = isDarkTheme,
                onDateSelected = { date ->
                    scope.launch {
                        selectedDateState.animateScrollToPage((date.dayOfMonth - 1).coerceIn(0, monthStart.lengthOfMonth() - 1))
                    }
                }
            )

            Spacer(Modifier.height(18.dp))

            AnimatedContent(
                targetState = selectedDate,
                transitionSpec = {
                    if (targetState.isAfter(initialState)) {
                        (slideInHorizontally { it / 3 } + fadeIn(tween(220))).togetherWith(
                            slideOutHorizontally { -it / 4 } + fadeOut(tween(160))
                        )
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn(tween(220))).togetherWith(
                            slideOutHorizontally { it / 4 } + fadeOut(tween(160))
                        )
                    }
                },
                label = "dayTransactions"
            ) { date ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DaySummaryCard(
                        date = date,
                        expense = selectedExpense,
                        count = selectedDateTransactions.count { it.type == TransactionType.EXPENSE }
                    )

                    if (selectedDateTransactions.isEmpty()) {
                        EmptyDayState(date)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 28.dp)
                        ) {
                            items(selectedDateTransactions, key = { it.id }) { txn ->
                                CalendarExpenseRow(txn)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCard(
    monthStart: LocalDate,
    expenseByDate: Map<LocalDate, Double>,
    selectedDate: LocalDate,
    maxExpense: Double,
    isDarkTheme: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val cardBackground = if (isDarkTheme) {
        Brush.linearGradient(listOf(Color(0xFF171A2B), Color(0xFF101422), Color(0xFF0F172A)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF7F2EA), Color(0xFFF1E7D9), Color(0xFFE7D9C7)))
    }
    val daysInMonth = monthStart.lengthOfMonth()
    val firstOffset = (monthStart.dayOfWeek.value + 6) % 7
    val cells = buildList {
        repeat(firstOffset) { add(null) }
        repeat(daysInMonth) { index -> add(monthStart.plusDays(index.toLong())) }
        while (size % 7 != 0) add(null)
    }
    val totalExpense = expenseByDate.values.sum()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBackground)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Expense heatmap", color = titleText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Tap a date below to inspect that day's expenses",
                    color = bodyText,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDarkTheme) DarkCard.copy(alpha = 0.85f) else LightCard)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("Month spent", color = bodyText, fontSize = 10.sp, maxLines = 1)
                Text(
                    "₹${"%,.0f".format(totalExpense)}",
                    color = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        HeatmapLegend(isDarkTheme)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val rows = cells.chunked(7)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { date ->
                        val amount = date?.let { expenseByDate[it] } ?: 0.0
                        val intensity = if (maxExpense <= 0.0) 0f else (amount / maxExpense).toFloat().coerceIn(0f, 1f)
                        val selected = date == selectedDate
                        HeatmapCell(
                            day = date?.dayOfMonth?.toString() ?: "",
                            amount = amount,
                            intensity = intensity,
                            selected = selected,
                            isDarkTheme = isDarkTheme,
                            onClick = { if (date != null) onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegend(isDarkTheme: Boolean) {
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Less", color = bodyText, fontSize = 10.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0.08f, 0.25f, 0.45f, 0.7f, 1f).forEach { value ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(expenseHeatColor(value, isDarkTheme))
                )
            }
        }
        Text("More", color = bodyText, fontSize = 10.sp)
    }
}

@Composable
private fun HeatmapCell(
    day: String,
    amount: Double,
    intensity: Float,
    selected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val cellColor = expenseHeatColor(intensity, isDarkTheme)
    val borderColor = if (selected) if (isDarkTheme) Color.White else Color.Black else Color.Transparent
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cellColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = if (amount > 0.0) if (isDarkTheme) Color.White else TextPrimaryLight else if (isDarkTheme) Color.White.copy(alpha = 0.38f) else TextSecondaryLight.copy(alpha = 0.65f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DateScroller(
    pagerState: androidx.compose.foundation.pager.PagerState,
    monthStart: LocalDate
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dayLabel = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val centeredGradient = if (isDarkTheme) {
        listOf(Color(0xFF8569FF), Color(0xFF2B6CF6))
    } else {
        listOf(Color(0xFFF0E4D4), Color(0xFFDCC6AA))
    }
    val offGradient = if (isDarkTheme) {
        listOf(Color(0xFF1B2131), Color(0xFF131826))
    } else {
        listOf(Color(0xFFF8F2E8), Color(0xFFE8DDD0))
    }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = 64.dp
    val horizontalPadding = (screenWidth / 2) - (itemWidth / 2)

            HorizontalPager(
        state = pagerState,
        pageSize = PageSize.Fixed(itemWidth),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
    ) { page ->
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(-3f, 3f)
        val absOffset = abs(pageOffset)
        val scale = 1f - (0.18f * absOffset).coerceAtMost(0.45f)
        val alpha = 1f - (0.28f * absOffset).coerceAtMost(0.8f)
        val selectedDate = monthStart.plusDays(page.toLong())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.alpha = alpha
                    this.rotationY = pageOffset * 18f
                    this.translationY = absOffset * 10f
                },
            contentAlignment = Alignment.Center
        ) {
            val isCentered = page == pagerState.currentPage
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                    color = if (isCentered) if (isDarkTheme) Color.White else TextPrimaryLight else dayLabel.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .size(width = 58.dp, height = 80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                if (isCentered) centeredGradient else offGradient
                            )
                        )
                        .border(
                            1.dp,
                            if (isCentered) {
                                if (isDarkTheme) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.18f)
                            } else {
                                if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
                            },
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedDate.dayOfMonth.toString(),
                        fontSize = if (isCentered) 24.sp else 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkTheme) Color.White else TextPrimaryLight
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    date: LocalDate,
    expense: Double,
    count: Int
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(surface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM")),
            color = titleText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "₹${"%,.0f".format(expense)} spent on ${count} expense${if (count == 1) "" else "s"}",
            color = bodyText,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CalendarExpenseRow(transaction: Transaction) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val iconSurface = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else LightCard
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(transaction.category.emoji, fontSize = 18.sp)
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
        }
        Text(
            "-₹${"%,.0f".format(transaction.amount)}",
            color = Color(0xFFEF4444),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun EmptyDayState(date: LocalDate) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val titleText = if (isDarkTheme) Color.White else TextPrimaryLight
    val bodyText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("No expenses on this day", color = titleText, fontWeight = FontWeight.SemiBold)
        Text(
            date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            color = bodyText,
            fontSize = 12.sp
        )
    }
}

private fun expenseHeatColor(intensity: Float, isDarkTheme: Boolean): Color {
    return if (isDarkTheme) {
        when {
            intensity <= 0f -> Color(0xFF161C2B)
            intensity < 0.2f -> Color(0xFF243047)
            intensity < 0.4f -> Color(0xFF2E4E7B)
            intensity < 0.65f -> Color(0xFF4F6EF7)
            intensity < 0.85f -> Color(0xFF7A5CFF)
            else -> Color(0xFFB35CFF)
        }
    } else {
        when {
            intensity <= 0f -> Color(0xFFF1E7D9)
            intensity < 0.2f -> Color(0xFFEADCCB)
            intensity < 0.4f -> Color(0xFFDCC6AA)
            intensity < 0.65f -> Color(0xFFCDAA84)
            intensity < 0.85f -> Color(0xFFBB8D5D)
            else -> Color(0xFFA96F40)
        }
    }
}
