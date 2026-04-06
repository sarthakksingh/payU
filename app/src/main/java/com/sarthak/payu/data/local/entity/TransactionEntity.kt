package com.sarthak.payu.data.local.entity



import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarthak.payu.data.model.Category
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: String,
    val note: String,
    val date: String, // ISO format yyyy-MM-dd
    val createdAt: Long = System.currentTimeMillis()
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    category = Category.valueOf(category),
    type = TransactionType.valueOf(type),
    note = note,
    date = LocalDate.parse(date),
    createdAt = createdAt
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    category = category.name,
    type = type.name,
    note = note,
    date = date.toString(),
    createdAt = createdAt
)