package com.sarthak.payu.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.payu.data.model.Category
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.model.TransactionType
import com.sarthak.payu.data.repo.TransactionRepository
import com.sarthak.payu.utils.CategoryClassifier
import com.sarthak.payu.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val note: String = "",
    val selectedCategory: Category = Category.OTHER,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val paymentMethodTouched: Boolean = false,
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val suggestedCategory: Category? = null,
    val classifierConfidence: String = "LOW",
    val amountError: String? = null,
    val showPaymentMethodSheet: Boolean = false,
    val showAddMethodDialog: Boolean = false,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val prefs: UserPreferences
) : ViewModel() {

    private val cashPaymentMethod = PaymentMethod(
        id = CASH_METHOD_ID,
        bankName = "Cash",
        lastDigits = "",
        balance = 0.0,
        isPrimary = false,
        type = PaymentMethodType.CASH
    )

    private val _state = MutableStateFlow(AddTransactionUiState())

    val state: StateFlow<AddTransactionUiState> = combine(
        _state,
        prefs.paymentMethods,
        prefs.primaryPaymentMethod
    ) { s, methods, primaryMethod ->
        val availableMethods = listOf(cashPaymentMethod) + methods.filter { it.type != PaymentMethodType.CASH }
        s.copy(
            availablePaymentMethods = availableMethods,
            selectedPaymentMethod = if (s.paymentMethodTouched) {
                s.selectedPaymentMethod
            } else {
                null
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddTransactionUiState())

    fun onAmountChange(value: String) = _state.update { it.copy(amount = value, amountError = null) }

    fun onNoteChange(value: String) {
        _state.update { it.copy(note = value) }
        if (value.length >= 3) {
            val suggested = CategoryClassifier.classify(value)
            val confidence = CategoryClassifier.confidence(value)
            _state.update {
                it.copy(
                    suggestedCategory = suggested,
                    classifierConfidence = confidence,
                    selectedCategory = if (confidence == "HIGH") suggested else it.selectedCategory
                )
            }
        } else {
            _state.update { it.copy(suggestedCategory = null, classifierConfidence = "LOW") }
        }
    }

    fun onCategorySelect(category: Category) = _state.update { it.copy(selectedCategory = category) }
    fun onTypeSelect(type: TransactionType) = _state.update { it.copy(selectedType = type) }
    fun onDateChange(date: LocalDate) = _state.update { it.copy(selectedDate = date) }

    fun acceptSuggestedCategory() {
        _state.value.suggestedCategory?.let { suggested ->
            _state.update { it.copy(selectedCategory = suggested) }
        }
    }

    fun onPaymentMethodClick() {
        _state.update { it.copy(showPaymentMethodSheet = true) }
    }

    fun onPaymentMethodSelect(method: PaymentMethod?) {
        _state.update {
            it.copy(
                selectedPaymentMethod = method,
                paymentMethodTouched = true,
                showPaymentMethodSheet = false,
                showAddMethodDialog = false
            )
        }
    }

    fun openAddPaymentMethodDialog() {
        _state.update {
            it.copy(
                showPaymentMethodSheet = false,
                showAddMethodDialog = true
            )
        }
    }

    fun confirmAddPaymentMethod(
        bankName: String,
        lastDigits: String,
        type: PaymentMethodType,
        balance: Double = 0.0,
        accountNumber: String = "xxxx xxxx xxxx xxxx",
        cardNumber: String = "xxxx xxxx xxxx xxxx",
        cvv: String = "xxx",
        expiryDate: String = "xx/xx"
    ) {
        if (type == PaymentMethodType.CASH) {
            onPaymentMethodSelect(cashPaymentMethod)
            return
        }

        val method = PaymentMethod(
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

        viewModelScope.launch {
            prefs.addPaymentMethod(method)
            _state.update {
                it.copy(
                    selectedPaymentMethod = method,
                    paymentMethodTouched = true,
                    showAddMethodDialog = false
                )
            }
        }
    }

    fun dismissPaymentSheet() = _state.update { it.copy(showPaymentMethodSheet = false) }
    fun dismissAddMethodDialog() = _state.update { it.copy(showAddMethodDialog = false) }

    fun saveTransaction() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(amountError = "Enter a valid amount") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    category = s.selectedCategory,
                    type = s.selectedType,
                    note = s.note,
                    date = s.selectedDate,
                    paymentMethodId = s.selectedPaymentMethod?.id
                )
            )
            val method = s.selectedPaymentMethod
            if (method != null && method.type != PaymentMethodType.CASH) {
                val delta = if (s.selectedType == TransactionType.INCOME) amount else -amount
                prefs.adjustPaymentMethodBalance(method.id, delta)
            }
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    companion object {
        private const val CASH_METHOD_ID = "cash"
    }
}
