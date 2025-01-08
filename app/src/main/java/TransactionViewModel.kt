package com.example.expensesmstracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions: Flow<List<Transaction>> = repository.getAllTransactions()
    val allCategories: Flow<List<Category>> = repository.getAllCategories() // Add this line

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }
}