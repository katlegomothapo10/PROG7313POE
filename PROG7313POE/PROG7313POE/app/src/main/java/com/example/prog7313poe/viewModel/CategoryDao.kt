package com.example.prog7313poe.viewModel

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.prog7313poe.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategories(userId: String): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId AND LOWER(name) = LOWER(:name)")
    suspend fun categoryExists(userId: String, name: String): Int

    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name LIMIT 1")
    suspend fun getCategoryByName(userId: String, name: String): Category?
}
