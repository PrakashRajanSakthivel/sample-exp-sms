package com.example.expensesmstracker

import android.app.Application

class ExpenseTrackerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        TransactionRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao() // Add this line
        )
    }
}