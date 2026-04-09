package com.sarthak.payu.data.local



import androidx.room.Database
import androidx.room.RoomDatabase
import com.sarthak.payu.data.local.dao.TransactionDao
import com.sarthak.payu.data.local.entity.TransactionEntity


@Database(
    entities = [TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PayUDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}