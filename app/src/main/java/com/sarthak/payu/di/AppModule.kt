package com.sarthak.payu.di


import android.content.Context
import androidx.room.Room
import com.sarthak.payu.data.local.PayUDatabase
import com.sarthak.payu.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PayUDatabase =
        Room.databaseBuilder(context, PayUDatabase::class.java, "payu_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: PayUDatabase): TransactionDao = db.transactionDao()
}