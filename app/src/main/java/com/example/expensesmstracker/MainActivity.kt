package com.example.expensesmstracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.expensesmstracker.ui.theme.ExpenseSMSTrackerTheme
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.expensesmstracker.ui.theme.ExpenseSMSTrackerTheme

import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.expensesmstracker.ui.theme.ExpenseSMSTrackerTheme


class MainActivity : ComponentActivity() {

    private lateinit var smsReceiver: SmsReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseSMSTrackerTheme {
                val context = LocalContext.current
                val smsList = remember { mutableStateOf<List<Transaction>>(emptyList()) }

                // Permission handling for READ_SMS
                val readSmsPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            Log.d("SmsReceiver", "READ_SMS permission granted")
                            smsList.value = readSms(context)
                        } else {
                            Log.d("SmsReceiver", "READ_SMS permission denied")
                            smsList.value = listOf(Transaction("Permission Denied", 0.0, "Cannot read SMS without permission"))
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
                            // Update the SMS list when a new SMS arrives
                            smsList.value = smsList.value + Transaction(
                                type = if (messageBody.contains("credited")) "Credit" else "Debit",
                                amount = extractAmount(messageBody),
                                description = messageBody
                            )
                        }
                    }
                    val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
                    context.registerReceiver(smsReceiver, filter)
                    Log.d("SmsReceiver", "SmsReceiver registered successfully")
                }

                // Display the list of transactions
                SMSReaderScreen(transactions = smsList.value)
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
fun SMSReaderScreen(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        TransactionAdapter(transactions = transactions)
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
                    Log.d("SMSReader", "Transaction added: ${Transaction(type, amount, body)}")
                    transactionList.add(Transaction(type, amount, body))
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

@Preview(showBackground = true)
@Composable
fun SMSReaderScreenPreview() {
    ExpenseSMSTrackerTheme {
        // Create a sample list of transactions for the preview
        val sampleTransactions = listOf(
            Transaction("Credit", 1000.0, "You have received Rs. 1000 credited to your account."),
            Transaction("Debit", 500.0, "Rs. 500 debited from your account for shopping.")
        )

        // Pass the sample transactions to SMSReaderScreen
        SMSReaderScreen(transactions = sampleTransactions)
    }
}



