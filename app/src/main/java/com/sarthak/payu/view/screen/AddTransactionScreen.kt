package com.sarthak.payu.view.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sarthak.payu.data.model.Category
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.TransactionType
import com.sarthak.payu.ui.theme.DarkBg
import com.sarthak.payu.ui.theme.DarkBorder
import com.sarthak.payu.ui.theme.DarkCard
import com.sarthak.payu.ui.theme.DarkSurface
import com.sarthak.payu.ui.theme.LightBg
import com.sarthak.payu.ui.theme.LightBorder
import com.sarthak.payu.ui.theme.LightCard
import com.sarthak.payu.ui.theme.LightSurface
import com.sarthak.payu.ui.theme.TealGreen
import com.sarthak.payu.ui.theme.TealGreenDark
import com.sarthak.payu.ui.theme.TextPrimaryLight
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.ui.theme.TextSecondaryLight
import com.sarthak.payu.view.components.BankLogoBadge
import com.sarthak.payu.view.components.PayUButton
import com.sarthak.payu.view.components.PayUTextField
import com.sarthak.payu.vm.AddTransactionViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onGoToProfile: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val screenBg = if (isDarkTheme) DarkBg else LightBg
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val card = if (isDarkTheme) DarkCard else LightCard
    val border = if (isDarkTheme) DarkBorder else LightBorder

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryText)
            }
            Text(
                "Add Transaction",
                color = primaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedPeriodChips(
                    options = listOf("Expense", "Income"),
                    selectedIndex = if (state.selectedType == TransactionType.EXPENSE) 0 else 1,
                    onSelect = {
                        viewModel.onTypeSelect(if (it == 0) TransactionType.EXPENSE else TransactionType.INCOME)
                    }
                )

                PayUTextField(
                    value = state.amount,
                    onValueChange = viewModel::onAmountChange,
                    placeholder = "0.00",
                    label = "Amount (Rs)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = state.amountError != null,
                    errorMessage = state.amountError
                )

                Column {
                    PayUTextField(
                        value = state.note,
                        onValueChange = viewModel::onNoteChange,
                        placeholder = "e.g. Zomato order, Uber ride...",
                        label = "Note / Description"
                    )
                    AnimatedVisibility(
                        visible = state.suggestedCategory != null && state.classifierConfidence != "HIGH",
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        state.suggestedCategory?.let { suggested ->
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TealGreen.copy(alpha = 0.1f))
                                    .clickable { viewModel.acceptSuggestedCategory() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TealGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Suggested: ${suggested.emoji} ${suggested.label} - tap to apply",
                                    color = if (isDarkTheme) TealGreen else TealGreenDark,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Text("Date", color = primaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(card)
                        .clickable { showDatePicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        state.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        color = primaryText,
                        fontSize = 15.sp
                    )
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = secondaryText)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            "Payment Method",
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    PaymentMethodSelector(
                        selected = state.selectedPaymentMethod,
                        onClick = viewModel::onPaymentMethodClick
                    )
                }

                Text("Category", color = primaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(Category.entries.toTypedArray()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = state.selectedCategory == category,
                            onClick = { viewModel.onCategorySelect(category) }
                        )
                    }
                }
            }

            PayUButton(
                text = "Save Transaction",
                onClick = viewModel::saveTransaction,
                isLoading = state.isLoading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.toEpochDay() * 86400000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK", color = TealGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = secondaryText)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = surface,
                    titleContentColor = primaryText,
                    headlineContentColor = primaryText,
                    weekdayContentColor = secondaryText,
                    subheadContentColor = secondaryText,
                    dayContentColor = primaryText,
                    selectedDayContainerColor = TealGreen,
                    selectedDayContentColor = if (isDarkTheme) Color.Black else Color.White,
                    todayDateBorderColor = TealGreen,
                    todayContentColor = TealGreen
                )
            )
        }
    }

    if (state.showPaymentMethodSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissPaymentSheet,
            containerColor = surface,
            dragHandle = {
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .size(40.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Select Payment Method",
                    color = primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PaymentMethodSheetItem(
                    label = "None (Untagged)",
                    sublabel = "Don't tag a payment method",
                    emoji = "-",
                    bankName = null,
                    isSelected = state.selectedPaymentMethod == null,
                    onClick = { viewModel.onPaymentMethodSelect(null) }
                )

                Spacer(Modifier.height(8.dp))

                state.availablePaymentMethods.forEach { method ->
                    PaymentMethodSheetItem(
                        label = methodDisplayName(method),
                        sublabel = when (method.type) {
                            PaymentMethodType.CASH -> "Default option"
                            else -> if (method.isPrimary) "Primary account" else method.type.label
                        },
                        emoji = method.type.emoji,
                        bankName = if (method.type == PaymentMethodType.CASH) null else method.bankName,
                        isSelected = state.selectedPaymentMethod?.id == method.id,
                        isPrimary = method.isPrimary,
                        onClick = { viewModel.onPaymentMethodSelect(method) }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(4.dp))

                PaymentMethodSheetItem(
                    label = "Add new payment method",
                    sublabel = "Add a bank account, UPI, or card",
                    emoji = "+",
                    bankName = null,
                    isSelected = false,
                    onClick = viewModel::openAddPaymentMethodDialog
                )
            }
        }
    }

    if (state.showAddMethodDialog) {
        AddPaymentMethodDialog(
            atLimit = state.availablePaymentMethods.count { it.type != PaymentMethodType.CASH } >= 5,
            onDismiss = viewModel::dismissAddMethodDialog,
            onConfirm = { bankName, lastDigits, balance, type, accountNumber, cardNumber, cvv, expiryDate ->
                viewModel.confirmAddPaymentMethod(
                    bankName = bankName,
                    lastDigits = lastDigits,
                    type = type,
                    balance = balance.toDoubleOrNull() ?: 0.0,
                    accountNumber = accountNumber,
                    cardNumber = cardNumber,
                    cvv = cvv,
                    expiryDate = expiryDate
                )
            }
        )
    }
}

@Composable
fun PaymentMethodSelector(
    selected: PaymentMethod?,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val card = if (isDarkTheme) DarkCard else LightSurface
    val border = if (isDarkTheme) DarkBorder else LightBorder
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(card)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected != null) TealGreen.copy(alpha = 0.5f) else border,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selected == null || selected.type == PaymentMethodType.CASH) {
                Text("₹", fontSize = 22.sp)
            } else {
                BankLogoBadge(
                    bankName = selected.bankName,
                    size = 34.dp,
                    logoSize = 20.dp,
                    background = surface.copy(alpha = 0.8f)
                )
            }
            Column {
                Text(
                    if (selected != null) methodDisplayName(selected) else "Select payment method",
                    color = if (selected != null) primaryText else secondaryText,
                    fontSize = 14.sp,
                    fontWeight = if (selected != null) FontWeight.SemiBold else FontWeight.Normal
                )
                if (selected != null) {
                    Text(
                        when (selected.type) {
                            PaymentMethodType.CASH -> "Cash"
                            else -> if (selected.isPrimary) "Primary - ${selected.type.label}" else selected.type.label
                        },
                        color = secondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        }
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = secondaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PaymentMethodSheetItem(
    label: String,
    sublabel: String,
    emoji: String,
    bankName: String? = null,
    isSelected: Boolean,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surface = if (isDarkTheme) DarkSurface else LightSurface
    val card = if (isDarkTheme) DarkCard else LightCard
    val primaryText = if (isDarkTheme) Color.White else TextPrimaryLight
    val secondaryText = if (isDarkTheme) TextSecondary else TextSecondaryLight
    val selectionBg = if (isDarkTheme) TealGreen.copy(alpha = 0.12f) else Color(0xFFD6ECE8)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) selectionBg else card)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) TealGreen else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(surface),
                contentAlignment = Alignment.Center
            ) {
                if (bankName != null) {
                    BankLogoBadge(
                        bankName = bankName,
                        size = 26.dp,
                        logoSize = 16.dp,
                        background = surface.copy(alpha = 0.8f)
                    )
                } else {
                    Text(emoji, fontSize = 20.sp)
                }
            }
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = primaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (isPrimary) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDarkTheme) TealGreen.copy(alpha = 0.2f) else Color(0xFFD6ECE8))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
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
                Text(sublabel, color = secondaryText, fontSize = 12.sp)
            }
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealGreen, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun CategoryChip(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val card = if (isDarkTheme) DarkCard else LightCard
    val selectionBg = if (isDarkTheme) TealGreen.copy(alpha = 0.2f) else Color(0xFFD6ECE8)
    val labelColor = if (isSelected) TealGreen else if (isDarkTheme) TextSecondary else TextSecondaryLight
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) selectionBg else card)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) TealGreen else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(category.emoji, fontSize = 22.sp)
        Text(
            category.label,
            color = labelColor,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

fun methodDisplayName(method: PaymentMethod): String = when (method.type) {
    PaymentMethodType.CASH -> "Cash"
    else -> "${method.bankName} ••${method.lastDigits}"
}
