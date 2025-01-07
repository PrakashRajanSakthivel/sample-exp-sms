package com.example.expensesmstracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions = repository.getAllTransactions()

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }
}