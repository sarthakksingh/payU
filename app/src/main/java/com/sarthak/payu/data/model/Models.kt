package com.sarthak.payu.data.model

import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }

enum class Category(
    val label: String,
    val emoji: String,
    val colorHex: String
) {
    FOOD("Food", "\uD83C\uDF54", "#4CAF50"),
    TRAVEL("Travel", "\u2708\uFE0F", "#2196F3"),
    SHOPPING("Shopping", "\uD83D\uDED2\uFE0F", "#E91E63"),
    ENTERTAINMENT("Entertainment", "\uD83C\uDFAC", "#9C27B0"),
    HEALTH("Health", "\uD83D\uDC8A", "#F44336"),
    EDUCATION("Education", "\uD83D\uDCD6", "#FF9800"),
    UTILITIES("Utilities", "\uD83D\uDCA1", "#00BCD4"),
    SALARY("Salary", "\uD83D\uDCB0", "#8BC34A"),
    FREELANCE("Freelance", "\uD83D\uDCBB", "#03A9F4"),
    INVESTMENT("Investment", "\uD83D\uDCC8", "#009688"),
    OTHER("Other", "\uD83D\uDCE6", "#607D8B")
}

// Payment Method
data class PaymentMethod(
    val id: String,                  // UUID
    val bankName: String,            // "HDFC", "SBI", custom name
    val lastDigits: String,          // 3 or 4 digits
    val balance: Double = 0.0,
    val isPrimary: Boolean = false,
    val type: PaymentMethodType = PaymentMethodType.BANK,
    val accountNumber: String = "xxxx xxxx xxxx xxxx",
    val cardNumber: String = "xxxx xxxx xxxx xxxx",
    val cvv: String = "xxx",
    val expiryDate: String = "xx/xx"
)

enum class PaymentMethodType(val label: String, val emoji: String) {
    BANK("Bank Account", "\uD83C\uDFE6"),
    CARD("Card", "\uD83D\uDCB3"),
    CASH("Cash", "\uD83D\uDCB5"),
    UPI("UPI", "\uD83D\uDCF1"),
    CREDIT_CARD("Credit Card", "\uD83D\uDCB3")
}

// Popular Indian banks for dropdown
val POPULAR_BANKS = listOf(
    "SBI", "HDFC", "ICICI", "Axis", "Kotak",
    "Punjab National Bank", "Bank of Baroda", "Canara Bank",
    "IDBI", "IndusInd", "Yes Bank", "Federal Bank",
    "Union Bank", "Indian Bank", "UCO Bank", "Other"
)


data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val category: Category,
    val type: TransactionType,
    val note: String,
    val date: LocalDate,
    val paymentMethodId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun paymentMethodDisplayName(method: PaymentMethod): String = when (method.type) {
    PaymentMethodType.CASH -> "Cash"
    else -> "${method.bankName} \u2022\u2022${method.lastDigits}"
}

fun paymentMethodSubtitle(method: PaymentMethod): String = when (method.type) {
    PaymentMethodType.CASH -> "Cash payment"
    else -> method.type.label
}

fun paymentMethodFrontNumber(method: PaymentMethod): String {
    val digits = method.lastDigits.filter { it.isDigit() }.takeLast(4)
    return if (digits.isBlank()) "\u2022\u2022\u2022\u2022" else "\u2022\u2022\u2022\u2022 $digits"
}

fun paymentMethodBackNumber(method: PaymentMethod): String = when (method.type) {
    PaymentMethodType.CARD -> method.cardNumber.ifBlank { "xxxx xxxx xxxx xxxx" }
    PaymentMethodType.CASH -> "xxxx"
    else -> method.accountNumber.ifBlank { "xxxx xxxx xxxx xxxx" }
}

fun paymentMethodCardDetail(method: PaymentMethod): String {
    val backNumber = paymentMethodBackNumber(method)
    return when (method.type) {
        PaymentMethodType.CARD -> backNumber
        PaymentMethodType.CASH -> "Cash"
        else -> backNumber
    }
}

fun paymentMethodExpiryDisplay(method: PaymentMethod): String {
    return if (method.expiryDate.isBlank()) "xx/xx" else method.expiryDate
}

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double = totalIncome - totalExpense,
    val categoryBreakdown: Map<Category, Double>
)

data class UserProfile(
    val name: String,
    val email: String,
    val totalBalance: Double
)

data class SavedProfile(
    val name: String,
    val email: String
)
