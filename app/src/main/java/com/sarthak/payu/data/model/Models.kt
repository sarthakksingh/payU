package com.sarthak.payu.data.model

import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }

enum class Category(
    val label: String,
    val emoji: String,
    val colorHex: String
) {
    FOOD("Food", "🍔", "#4CAF50"),
    TRAVEL("Travel", "✈️", "#2196F3"),
    SHOPPING("Shopping", "🛍️", "#E91E63"),
    ENTERTAINMENT("Entertainment", "🎬", "#9C27B0"),
    HEALTH("Health", "💊", "#F44336"),
    EDUCATION("Education", "📚", "#FF9800"),
    UTILITIES("Utilities", "💡", "#00BCD4"),
    SALARY("Salary", "💰", "#8BC34A"),
    FREELANCE("Freelance", "💻", "#03A9F4"),
    INVESTMENT("Investment", "📈", "#009688"),
    OTHER("Other", "📦", "#607D8B")
}

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val category: Category,
    val type: TransactionType,
    val note: String,
    val date: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
)

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