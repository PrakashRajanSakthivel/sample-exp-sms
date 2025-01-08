package com.example.expensesmstracker

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.expensesmstracker.ui.theme.ExpenseSMSTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var smsReceiver: SmsReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseSMSTrackerTheme {
                val context = LocalContext.current
                val viewModel: TransactionViewModel by viewModels {
                    TransactionViewModelFactory((application as ExpenseTrackerApplication).repository)
                }

                // Permission handling for READ_SMS
                val readSmsPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            Log.d("SmsReceiver", "READ_SMS permission granted")
                            val transactions = readSms(context)
                            transactions.forEach { transaction ->
                                viewModel.insert(transaction)
                            }
                        } else {
                            Log.d("SmsReceiver", "READ_SMS permission denied")
                        }
                    }
                )

                // Permission handling for RECEIVE_SMS
                val receiveSmsPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            Log.d("SmsReceiver", "RECEIVE_SMS permission granted")
                        } else {
                            Log.d("SmsReceiver", "RECEIVE_SMS permission denied")
                        }
                    }
                )

                // Check for permissions when the screen is first displayed
                LaunchedEffect(Unit) {
                    val readSmsPermission = Manifest.permission.READ_SMS
                    val receiveSmsPermission = Manifest.permission.RECEIVE_SMS

                    val hasReadSmsPermission = ContextCompat.checkSelfPermission(context, readSmsPermission) == PackageManager.PERMISSION_GRANTED
                    val hasReceiveSmsPermission = ContextCompat.checkSelfPermission(context, receiveSmsPermission) == PackageManager.PERMISSION_GRANTED

                    if (!hasReadSmsPermission) {
                        readSmsPermissionLauncher.launch(readSmsPermission)
                    }
                    if (!hasReceiveSmsPermission) {
                        receiveSmsPermissionLauncher.launch(receiveSmsPermission)
                    }
                }

                // Register the SmsReceiver
                LaunchedEffect(Unit) {
                    smsReceiver = SmsReceiver().apply {
                        onSmsReceived = { messageBody ->
                            Log.d("SmsReceiver", "onSmsReceived callback triggered with message: $messageBody")
                            // Insert the new transaction into the database
                            val transaction = Transaction(
                                type = if (messageBody.contains("credited")) "Credit" else "Debit",
                                amount = extractAmount(messageBody),
                                description = messageBody,
                                datetime = System.currentTimeMillis()
                            )
                            viewModel.insert(transaction)
                        }
                    }
                    val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
                    context.registerReceiver(smsReceiver, filter)
                    Log.d("SmsReceiver", "SmsReceiver registered successfully")
                }

                // Display the list of transactions and Add Category button
                val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
                val categories by viewModel.allCategories.collectAsState(initial = emptyList())

                // State for showing the Add Category dialog
                var showAddCategoryDialog by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Add Category Button
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Add Category")
                    }

                    // Display the list of transactions
                    LazyColumn {
                        items(transactions) { transaction ->
                            val category = categories.find { it.id == transaction.categoryId }
                            TransactionItem(transaction = transaction, category = category?.name)
                        }
                    }
                }

                // Add Category Dialog
                if (showAddCategoryDialog) {
                    AddCategoryDialog(
                        onDismiss = { showAddCategoryDialog = false },
                        onSave = { category ->
                            viewModel.insertCategory(category)
                            // Link transactions to categories after adding a new category
                            viewModel.linkTransactionsToCategories()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the SmsReceiver to avoid memory leaks
        unregisterReceiver(smsReceiver)
    }
}

@Composable
fun TransactionItem(transaction: Transaction, category: String?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Type: ${transaction.type}")
        Text(text = "Amount: ${transaction.amount}")
        Text(text = "Description: ${transaction.description}")
        Text(text = "Category: ${category ?: "Uncategorized"}")
    }
}

private fun readSms(context: Context): List<Transaction> {
    val transactionList = mutableListOf<Transaction>()
    val cursor: Cursor? = context.contentResolver.query(
        Uri.parse("content://sms/inbox"),
        null,
        null,
        null,
        "date DESC" // Sort by date to get the latest SMS first
    )

    if (cursor != null) {
        Log.d("SMSReader", "Cursor is not null. Reading SMS...")
        if (cursor.moveToFirst()) {
            do {
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                Log.d("SMSReader", "Read SMS: $body")
                if (body.contains("credited") || body.contains("debited")) {
                    val amount = extractAmount(body)
                    val type = if (body.contains("credited")) "Credit" else "Debit"
                    Log.d("SMSReader", "Transaction added: ${Transaction(
                        type = type,
                        amount = amount,
                        description = body,
                        datetime = System.currentTimeMillis()
                    )}")
                    transactionList.add(Transaction(
                        type = type,
                        amount = amount,
                        description = body,
                        datetime = System.currentTimeMillis()
                    ))
                }
            } while (cursor.moveToNext())
        } else {
            Log.d("SMSReader", "Cursor is empty. No SMS found.")
        }
        cursor.close()
    } else {
        Log.d("SMSReader", "Cursor is null. Unable to read SMS.")
    }

    return transactionList
}

// Function to extract amount from SMS body
private fun extractAmount(body: String): Double {
    val regex = Regex("""\d+(\.\d+)?""")
    return regex.find(body)?.value?.toDoubleOrNull() ?: 0.0
}