package com.sarthak.payu.data.repo

import com.sarthak.payu.data.local.dao.TransactionDao
import com.sarthak.payu.data.local.entity.toDomain
import com.sarthak.payu.data.local.entity.toEntity
import com.sarthak.payu.data.model.MonthlySummary
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { it.map { e -> e.toDomain() } }

    fun getTransactionsByMonth(yearMonth: String): Flow<List<Transaction>> =
        dao.getTransactionsByMonth(yearMonth).map { it.map { e -> e.toDomain() } }

    fun getTransactionsSince(date: LocalDate): Flow<List<Transaction>> =
        dao.getTransactionsSince(date.toString()).map { it.map { e -> e.toDomain() } }

    fun getTransactionsByPaymentMethod(methodId: String): Flow<List<Transaction>> =
        dao.getTransactionsByPaymentMethod(methodId).map { it.map { e -> e.toDomain() } }

    fun getMonthlySummary(yearMonth: String): Flow<MonthlySummary> =
        dao.getTransactionsByMonth(yearMonth).map { entities ->
            val transactions = entities.map { it.toDomain() }
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val breakdown = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            MonthlySummary(income, expense, income - expense, breakdown)
        }

    fun getTotalIncome(): Flow<Double> = dao.getTotalIncome().map { it ?: 0.0 }
    fun getTotalExpense(): Flow<Double> = dao.getTotalExpense().map { it ?: 0.0 }

    suspend fun addTransaction(transaction: Transaction) =
        dao.insertTransaction(transaction.toEntity())

    suspend fun deleteTransaction(transaction: Transaction) =
        dao.deleteById(transaction.id)
}