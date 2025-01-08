package com.example.expensesmstracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("SELECT * FROM categories WHERE :description LIKE '%' || keywords || '%'")
    suspend fun getCategoryByDescription(description: String): Category?

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesSync(): List<Category>
}