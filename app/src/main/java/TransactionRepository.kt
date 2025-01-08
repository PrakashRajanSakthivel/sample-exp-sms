package com.example.expensesmstracker

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    // Transaction methods
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    // Category methods
    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    suspend fun getCategoryById(categoryId: Long): Category? {
        return categoryDao.getCategoryById(categoryId)
    }

    suspend fun getCategoryByDescription(description: String): Category? {
        return categoryDao.getCategoryByDescription(description)
    }

    suspend fun addTransactionWithCategory(transaction: Transaction) {
        val category = categoryDao.getCategoryByDescription(transaction.description)
        val updatedTransaction = transaction.copy(categoryId = category?.id)
        transactionDao.insert(updatedTransaction)
    }

    suspend fun linkTransactionsToCategories() {
        val transactions = transactionDao.getAllTransactionsSync()
        val categories = categoryDao.getAllCategoriesSync()

        for (transaction in transactions) {
            for (category in categories) {
                if (category.keywords.any { keyword ->
                        transaction.description.contains(keyword, ignoreCase = true)
                    }) {
                    val updatedTransaction = transaction.copy(categoryId = category.id)
                    transactionDao.update(updatedTransaction)
                }
            }
        }
    }
}
