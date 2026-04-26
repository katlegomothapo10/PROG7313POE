package com.example.prog7313poe.repository

import com.example.prog7313poe.data.CategoryDao
import com.example.prog7313poe.model.Category

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getCategories(userId: String) = categoryDao.getCategories(userId)

    suspend fun insert(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun categoryExists(userId: String, name: String): Int {
        return categoryDao.categoryExists(userId, name)
    }
}