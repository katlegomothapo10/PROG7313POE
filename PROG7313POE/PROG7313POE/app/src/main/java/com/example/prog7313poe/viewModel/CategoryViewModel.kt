package com.example.prog7313poe.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Category
import com.example.prog7313poe.repository.CategoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    private val repository = CategoryRepository(categoryDao)

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"

    val categories = repository.getCategories(userId)

    fun addCategory(
        name: String,
        color: String,
        budgetLimit: Double,
        spentAmount: Double = 0.0,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {

            val exists = repository.categoryExists(userId, name)

            if (exists > 0) {
                onResult(false, "Category already exists")
                return@launch
            }

            val category = Category(
                userId = userId,
                name = name,
                color = color,
                budgetLimit = budgetLimit,
                spentAmount = spentAmount
            )

            repository.insert(category)
            onResult(true, "Category added successfully")
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.delete(category)
        }
    }
}