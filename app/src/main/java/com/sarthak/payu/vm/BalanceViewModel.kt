package com.sarthak.payu.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.payu.data.model.Category
import com.sarthak.payu.data.model.MonthlySummary
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.repo.TransactionRepository
import com.sarthak.payu.utils.calculateFinancialHealthScore
import com.sarthak.payu.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class BalancesUiState(
    val monthlySummary: MonthlySummary? = null,
    val allTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: Map<Category, Double> = emptyMap(),
    val healthScore: Int = 0, // 0-100, shown as arc gauge
    val selectedMonth: String = "",
    val isLoading: Boolean = true,
    val paymentMethods: List<PaymentMethod> = emptyList()
)

@HiltViewModel
class BalancesViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val state: StateFlow<BalancesUiState> = combine(
        repository.getMonthlySummary(currentMonth),
        repository.getAllTransactions(),
        repository.getTotalExpense(),
        repository.getTotalIncome(),
        prefs.paymentMethods
    ) { summary, all, totalExpense, totalIncome, methods ->
        val score = calculateFinancialHealthScore(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalBalance = methods.sumOf { it.balance },
            paymentMethodsCount = methods.size
        )
        BalancesUiState(
            monthlySummary = summary,
            allTransactions = all,
            categoryBreakdown = summary.categoryBreakdown,
            healthScore = score,
            selectedMonth = currentMonth,
            isLoading = false,
            paymentMethods = methods
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalancesUiState())

}
