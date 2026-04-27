package com.example.prog7313poe.ui

import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Category

suspend fun seedDefaultCategoriesIfNeeded(db: AppDatabase, userId: String) {
    val existingCount = db.categoryDao().categoryCount(userId)
    if (existingCount > 0) return

    val defaults = listOf(
        Category(userId = userId, name = "Groceries", color = "#29B6B2", budgetLimit = 3000.0),
        Category(userId = userId, name = "Transport", color = "#FFD97743", budgetLimit = 1500.0),
        Category(userId = userId, name = "Entertainment", color = "#9B3A62", budgetLimit = 500.0),
        Category(userId = userId, name = "Utilities", color = "#FF7A1A", budgetLimit = 2000.0)
    )

    defaults.forEach { db.categoryDao().insert(it) }
}
