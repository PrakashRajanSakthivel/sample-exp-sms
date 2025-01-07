package com.example.expensesmstracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable function to display a list of transactions.
 *
 * @param transactions List of transactions to display.
 * @param modifier Modifier for the LazyColumn.
 */
@Composable
fun TransactionAdapter(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

/**
 * Composable function to display a single transaction item.
 *
 * @param transaction The transaction to display.
 * @param modifier Modifier for the transaction item.
 */
@Composable
fun TransactionItem(transaction: Transaction, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Text(
            text = "Type: ${transaction.type}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Amount: ${transaction.amount}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Description: ${transaction.description}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Data class representing a transaction.
 *
 * @param type The type of transaction (e.g., "Credit" or "Debit").
 * @param amount The amount of the transaction.
 * @param description The description of the transaction (e.g., SMS body).
 */
data class Transaction(
    val type: String,
    val amount: Double,
    val description: String
)