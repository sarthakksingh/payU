package com.sarthak.payu.vm


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.payu.data.model.MonthlySummary
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.model.TransactionType
import com.sarthak.payu.data.repo.TransactionRepository
import com.sarthak.payu.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class PeriodHeaderState(
    val title: String = "",
    val subtitle: String = "",
    val amountLabel: String = "Total spent",
    val amount: Double = 0.0
)

data class HomeUiState(
    val userName: String = "",
    val recentTransactions: List<Transaction> = emptyList(),
    val weeklyTransactions: List<Transaction> = emptyList(),
    val monthlySummary: MonthlySummary? = null,
    val totalBalance: Double = 0.0,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val primaryPaymentMethod: PaymentMethod? = null,
    val paymentMethodMonthlyExpenseById: Map<String, Double> = emptyMap(),
    val selectedPaymentMethodId: String? = null,
    val periodSummary: MonthlySummary? = null,
    val displayTransactions: List<Transaction> = emptyList(),
    val periodHeader: PeriodHeaderState = PeriodHeaderState(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _selectedPaymentMethodId = MutableStateFlow<String?>(null)
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val weekFormatter = DateTimeFormatter.ofPattern("dd MMM")

    val state: StateFlow<HomeUiState> = combine(
        prefs.userName,
        repository.getAllTransactions(),
        prefs.paymentMethods,
        prefs.primaryPaymentMethod,
        _selectedTab,
        _selectedPaymentMethodId
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val userName = values[0] as String
        @Suppress("UNCHECKED_CAST")
        val all = values[1] as List<Transaction>
        @Suppress("UNCHECKED_CAST")
        val methods = values[2] as List<PaymentMethod>
        val primaryMethod = values[3] as PaymentMethod?
        val tab = values[4] as Int
        val selectedMethodId = values[5] as String?
        val today = LocalDate.now()
        val recentSorted = all.sortedByDescending { it.createdAt }
        val recent = filterByPaymentMethod(recentSorted, selectedMethodId).take(10)
        val recentSummary = buildMonthlySummary(recent)

        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val weeklyAll = all.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
        val weekly = filterByPaymentMethod(weeklyAll, selectedMethodId)
        val weeklySummary = buildMonthlySummary(weekly)

        val monthStart = today.withDayOfMonth(1)
        val monthEnd = monthStart.plusMonths(1).minusDays(1)
        val monthlyAll = all.filter { !it.date.isBefore(monthStart) && !it.date.isAfter(monthEnd) }
        val monthly = filterByPaymentMethod(monthlyAll, selectedMethodId)
        val monthlyPeriodSummary = buildMonthlySummary(monthly)

        val recentExpense = recent.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val weeklyExpense = weekly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val monthlyExpense = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val periodHeader = when (tab) {
            0 -> PeriodHeaderState(
                title = "Recent Activity",
                subtitle = "Latest 10 transactions",
                amountLabel = "Recent spent",
                amount = recentExpense
            )
            1 -> PeriodHeaderState(
                title = "${weekStart.format(weekFormatter)} - ${weekEnd.format(weekFormatter)}",
                subtitle = "Week starting Monday",
                amountLabel = "Total spent",
                amount = weeklyExpense
            )
            else -> PeriodHeaderState(
                title = today.format(monthFormatter),
                subtitle = "This month",
                amountLabel = "Total spent",
                amount = monthlyExpense
            )
        }
        val monthlyExpenseByMethod = monthlyAll
            .filter { it.type == TransactionType.EXPENSE && !it.paymentMethodId.isNullOrBlank() }
            .groupBy { it.paymentMethodId!! }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        HomeUiState(
            userName = userName,
            recentTransactions = recent,
            weeklyTransactions = weekly,
            monthlySummary = monthlyPeriodSummary,
            totalBalance = monthlyPeriodSummary.totalIncome - monthlyPeriodSummary.totalExpense,
            paymentMethods = methods,
            primaryPaymentMethod = primaryMethod,
            paymentMethodMonthlyExpenseById = monthlyExpenseByMethod,
            selectedPaymentMethodId = selectedMethodId,
            periodSummary = when (tab) {
                0 -> recentSummary
                1 -> weeklySummary
                else -> monthlyPeriodSummary
            },
            displayTransactions = when (tab) {
                0 -> recent
                1 -> weekly
                else -> monthly
            },
            periodHeader = periodHeader,
            isLoading = false,
            selectedTab = tab
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun setTab(index: Int) { _selectedTab.value = index }

    fun togglePaymentMethodFilter(methodId: String?) {
        _selectedPaymentMethodId.value = when {
            methodId == null -> null
            _selectedPaymentMethodId.value == methodId -> null
            else -> methodId
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            transaction.paymentMethodId?.let { methodId ->
                val delta = when (transaction.type) {
                    TransactionType.INCOME -> -transaction.amount
                    TransactionType.EXPENSE -> transaction.amount
                }
                if (methodId != "cash") {
                    prefs.adjustPaymentMethodBalance(methodId, delta)
                }
            }
        }
    }

    fun restoreTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
            transaction.paymentMethodId?.let { methodId ->
                val delta = when (transaction.type) {
                    TransactionType.INCOME -> transaction.amount
                    TransactionType.EXPENSE -> -transaction.amount
                }
                if (methodId != "cash") {
                    prefs.adjustPaymentMethodBalance(methodId, delta)
                }
            }
        }
    }

    fun addPaymentMethod(
        bankName: String,
        lastDigits: String,
        balance: Double,
        type: PaymentMethodType,
        accountNumber: String = "xxxx xxxx xxxx xxxx",
        cardNumber: String = "xxxx xxxx xxxx xxxx",
        cvv: String = "xxx",
        expiryDate: String = "xx/xx"
    ) {
        viewModelScope.launch {
            prefs.addPaymentMethod(
                PaymentMethod(
                    id = prefs.generateId(),
                    bankName = bankName,
                    lastDigits = lastDigits,
                    balance = balance,
                    isPrimary = false,
                    type = type,
                    accountNumber = accountNumber,
                    cardNumber = cardNumber,
                    cvv = cvv,
                    expiryDate = expiryDate
                )
            )
        }
    }

    fun setPrimaryPaymentMethod(methodId: String) {
        viewModelScope.launch {
            prefs.setPrimaryPaymentMethod(methodId)
        }
    }

    private fun filterByPaymentMethod(
        transactions: List<Transaction>,
        selectedMethodId: String?
    ): List<Transaction> {
        if (selectedMethodId.isNullOrBlank()) return transactions
        return transactions.filter { it.paymentMethodId == selectedMethodId }
    }

    private fun buildMonthlySummary(
        transactions: List<Transaction>
    ): MonthlySummary {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val breakdown = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        return MonthlySummary(income, expense, income - expense, breakdown)
    }
}
