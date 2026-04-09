package com.sarthak.payu.utils

import kotlin.math.abs
import kotlin.math.roundToInt

fun calculateFinancialHealthScore(
    totalIncome: Double,
    totalExpense: Double,
    totalBalance: Double,
    paymentMethodsCount: Int
): Int {
    val hasActivity = totalIncome > 0.0 || totalExpense > 0.0 || totalBalance != 0.0
    if (!hasActivity) return 50

    val safeIncome = totalIncome.coerceAtLeast(0.0)
    val safeExpense = totalExpense.coerceAtLeast(0.0)
    val netWorth = totalBalance + (safeIncome - safeExpense)
    val spendingPressure = if (safeIncome > 0.0) {
        (safeExpense / safeIncome).coerceIn(0.0, 2.0)
    } else {
        (safeExpense / 12000.0).coerceIn(0.0, 1.0)
    }

    val savingsRate = if (safeIncome > 0.0) {
        ((safeIncome - safeExpense) / safeIncome).coerceIn(-0.5, 1.0)
    } else {
        when {
            totalBalance > 0.0 -> 0.40
            safeExpense > 0.0 -> 0.22
            else -> 0.30
        }
    }

    val balanceFactor = ((netWorth + 5000.0) / 15000.0).coerceIn(0.0, 1.0)
    val methodFactor = (paymentMethodsCount.coerceIn(0, 5) / 5.0) * 0.08
    val activityBoost = when {
        safeIncome > 0.0 && safeExpense > 0.0 -> 0.12
        safeIncome > 0.0 || safeExpense > 0.0 -> 0.08
        else -> 0.0
    }

    val base = 58.0
    val raw = base +
        (savingsRate * 26.0) +
        (balanceFactor * 18.0) +
        (methodFactor * 10.0) +
        (activityBoost * 10.0) -
        (spendingPressure * 12.0)

    return raw.roundToInt().coerceIn(0, 100)
}
