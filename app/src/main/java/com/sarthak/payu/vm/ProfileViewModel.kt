package com.sarthak.payu.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.data.model.PaymentMethodType
import com.sarthak.payu.data.model.paymentMethodDisplayName
import com.sarthak.payu.data.repo.TransactionRepository
import com.sarthak.payu.utils.calculateFinancialHealthScore
import com.sarthak.payu.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val totalBalance: Double = 0.0,
    val totalSpending: Double = 0.0,
    val financialHealthScore: Int = 0,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val primaryMethod: PaymentMethod? = null,
    val isDarkMode: Boolean = true,
    val editName: String = "",
    val editEmail: String = "",
    val updateSuccess: Boolean = false,
    val showAddMethodDialog: Boolean = false,
    val showEditBalanceDialog: Boolean = false,
    val newBankName: String = "",
    val newLastDigits: String = "",
    val newBalance: String = "",
    val newMethodType: PaymentMethodType = PaymentMethodType.BANK,
    val newBankNameError: String? = null,
    val newLastDigitsError: String? = null,
    val newBalanceError: String? = null,
    val editingBalanceMethodId: String? = null,
    val editingBalanceMethodName: String = "",
    val editingBalanceValue: String = "",
    val editingBalanceError: String? = null,
    val atMethodLimit: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val repository: TransactionRepository
) : ViewModel() {
    private val extraFlow = MutableStateFlow(ProfileUiState())

    val state: StateFlow<ProfileUiState> = combine(
        prefs.userName,
        prefs.userEmail,
        prefs.isDarkMode,
        repository.getTotalExpense(),
        repository.getTotalIncome(),
        prefs.paymentMethods,
        extraFlow
    ) { values ->
        val name = values[0] as String
        val email = values[1] as String
        val dark = values[2] as Boolean
        val expense = values[3] as Double
        val income = values[4] as Double
        @Suppress("UNCHECKED_CAST")
        val methods = values[5] as List<PaymentMethod>
        val extra = values[6] as ProfileUiState
        extra.copy(
            name = name,
            email = email,
            isDarkMode = dark,
            totalBalance = methods.sumOf { it.balance },
            totalSpending = expense,
            financialHealthScore = calculateFinancialHealthScore(
                totalIncome = income,
                totalExpense = expense,
                totalBalance = methods.sumOf { it.balance },
                paymentMethodsCount = methods.size
            ),
            paymentMethods = methods,
            primaryMethod = methods.firstOrNull { it.isPrimary } ?: methods.firstOrNull(),
            editName = if (extra.editName.isBlank()) name else extra.editName,
            editEmail = if (extra.editEmail.isBlank()) email else extra.editEmail,
            atMethodLimit = methods.size >= 5
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun onEditNameChange(v: String) = extraFlow.update { it.copy(editName = v) }
    fun onEditEmailChange(v: String) = extraFlow.update { it.copy(editEmail = v) }

    fun saveProfile(name: String, email: String) {
        viewModelScope.launch { prefs.saveUser(name, email) }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch { prefs.toggleDarkMode(isDark) }
    }

    suspend fun buildTransactionsCsv(): String {
        val transactions = repository.getAllTransactions().first()
        val paymentMethods = prefs.paymentMethods.first().associateBy { it.id }

        fun escapeCsv(value: String): String {
            val needsQuotes = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
            val escaped = value.replace("\"", "\"\"")
            return if (needsQuotes) "\"$escaped\"" else escaped
        }

        val header = listOf(
            "id",
            "date",
            "type",
            "category",
            "amount",
            "note",
            "payment_method",
            "payment_method_id",
            "created_at"
        ).joinToString(",")

        val rows = transactions.joinToString("\n") { txn ->
            val method = txn.paymentMethodId?.let(paymentMethods::get)
            listOf(
                txn.id.toString(),
                txn.date.toString(),
                txn.type.name,
                txn.category.label,
                "%.2f".format(txn.amount),
                txn.note,
                method?.let(::paymentMethodDisplayName).orEmpty(),
                txn.paymentMethodId.orEmpty(),
                txn.createdAt.toString()
            ).joinToString(",") { escapeCsv(it) }
        }

        return buildString {
            append(header)
            if (rows.isNotBlank()) {
                append('\n')
                append(rows)
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

    fun updatePaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            prefs.updatePaymentMethod(method)
        }
    }

    fun logout() {
        viewModelScope.launch { prefs.logout() }
    }

    fun showAddDialog() = extraFlow.update {
        it.copy(
            showAddMethodDialog = true,
            showEditBalanceDialog = false,
            newBankName = "",
            newLastDigits = "",
            newBalance = "",
            newMethodType = PaymentMethodType.BANK,
            newBankNameError = null,
            newLastDigitsError = null,
            newBalanceError = null,
            editingBalanceMethodId = null,
            editingBalanceMethodName = "",
            editingBalanceValue = "",
            editingBalanceError = null
        )
    }

    fun dismissAddDialog() = extraFlow.update {
        it.copy(showAddMethodDialog = false, showEditBalanceDialog = false)
    }

    fun onNewBankNameChange(v: String) = extraFlow.update { it.copy(newBankName = v, newBankNameError = null) }

    fun onNewLastDigitsChange(v: String) {
        if (v.length <= 4 && v.all { c -> c.isDigit() }) {
            extraFlow.update { it.copy(newLastDigits = v, newLastDigitsError = null) }
        }
    }

    fun onNewBalanceChange(v: String) {
        if (v.isBlank() || v.toDoubleOrNull() != null) {
            extraFlow.update { it.copy(newBalance = v, newBalanceError = null) }
        }
    }

    fun onNewMethodTypeChange(t: PaymentMethodType) = extraFlow.update { it.copy(newMethodType = t) }

    fun confirmAddMethod() {
        val s = extraFlow.value
        var valid = true
        if (s.newBankName.isBlank()) {
            extraFlow.update { it.copy(newBankNameError = "Bank name required") }
            valid = false
        }
        if (s.newLastDigits.length < 3) {
            extraFlow.update { it.copy(newLastDigitsError = "Enter 3-4 digits") }
            valid = false
        }
        val balance = s.newBalance.takeIf { it.isNotBlank() }?.toDoubleOrNull()
        if (s.newBalance.isNotBlank() && balance == null) {
            extraFlow.update { it.copy(newBalanceError = "Enter a valid balance") }
            valid = false
        }
        if (!valid) return

        val method = PaymentMethod(
            id = prefs.generateId(),
            bankName = s.newBankName,
            lastDigits = s.newLastDigits,
            balance = balance ?: 0.0,
            isPrimary = false,
            type = s.newMethodType
        )

        viewModelScope.launch {
            prefs.addPaymentMethod(method)
            extraFlow.update { it.copy(showAddMethodDialog = false) }
        }
    }

    fun openEditBalanceDialog(method: PaymentMethod) {
        extraFlow.update {
            it.copy(
                showAddMethodDialog = false,
                showEditBalanceDialog = true,
                newBankName = method.bankName,
                newLastDigits = method.lastDigits,
                newBalance = method.balance.toString(),
                newMethodType = method.type,
                newBankNameError = null,
                newLastDigitsError = null,
                newBalanceError = null,
                editingBalanceMethodId = method.id,
                editingBalanceMethodName = method.bankName,
                editingBalanceValue = method.balance.toString(),
                editingBalanceError = null
            )
        }
    }

    fun dismissEditBalanceDialog() = extraFlow.update {
        it.copy(
            showEditBalanceDialog = false,
            editingBalanceMethodId = null,
            editingBalanceMethodName = "",
            editingBalanceValue = "",
            editingBalanceError = null
        )
    }

    fun onEditBalanceChange(v: String) {
        if (v.isBlank() || v.toDoubleOrNull() != null) {
            extraFlow.update { it.copy(editingBalanceValue = v, editingBalanceError = null) }
        }
    }

    fun confirmEditBalance() {
        val s = extraFlow.value
        val methodId = s.editingBalanceMethodId ?: return
        val balance = s.editingBalanceValue.toDoubleOrNull() ?: run {
            extraFlow.update { it.copy(editingBalanceError = "Enter a valid balance") }
            return
        }
        viewModelScope.launch {
            prefs.updatePaymentMethodBalance(methodId, balance)
            dismissEditBalanceDialog()
        }
    }

    fun removeMethod(id: String) {
        viewModelScope.launch { prefs.removePaymentMethod(id) }
    }

    fun setPrimary(id: String) {
        viewModelScope.launch { prefs.setPrimaryPaymentMethod(id) }
    }
}
