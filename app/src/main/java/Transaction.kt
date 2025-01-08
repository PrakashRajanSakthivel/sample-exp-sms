package com.example.expensesmstracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions") // Add @Entity annotation
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Add @PrimaryKey annotation
    val type: String, // Credit or Debit
    val amount: Double, // Transaction amount
    val description: String, // SMS description
    val datetime: Long = System.currentTimeMillis(), // Timestamp of the transaction
    val categoryId: Long? = null // Link to Category
)