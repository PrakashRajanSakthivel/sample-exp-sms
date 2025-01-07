package com.example.expensesmstracker
import android.app.Application

class ExpenseTrackerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) } // Pass TransactionDao
}