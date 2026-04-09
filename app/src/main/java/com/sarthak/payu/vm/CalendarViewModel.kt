package com.sarthak.payu.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarthak.payu.data.model.Transaction
import com.sarthak.payu.data.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CalendarUiState(
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {
    val state: StateFlow<CalendarUiState> = repository.getAllTransactions()
        .map { CalendarUiState(transactions = it.sortedByDescending { txn -> txn.date }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())
}
