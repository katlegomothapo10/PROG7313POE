package com.example.prog7313poe.ui

import android.app.AlertDialog
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Category
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class BudgetCategoriesActivity : AppCompatActivity() {

    private lateinit var adapter: CategoryAdapter
    private var categories: List<Category> = emptyList()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_categories)
        setupBottomNavigation(R.id.nav_budget)

        val db = AppDatabase.getDatabase(this)
        val recycler = findViewById<RecyclerView>(R.id.recyclerCategories)
        val btnAdd = findViewById<Button>(R.id.btnAddCategory)
        val btnDelete = findViewById<Button>(R.id.btnDeleteCategory)
        val heading = findViewById<TextView>(R.id.txtHeading)

        applyGradient(heading)

        adapter = CategoryAdapter(emptyList())
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            seedDefaultCategoriesIfNeeded(db, userId)

            db.categoryDao().getCategories(userId).collect { savedCategories ->
                categories = savedCategories
                adapter.updateData(savedCategories)
            }
        }

        btnAdd.setOnClickListener {
            showAddCategoryDialog()
        }

        btnDelete.setOnClickListener {
            showManageCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        showCategoryFormDialog()
    }

    private fun showCategoryFormDialog(category: Category? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etBudget = dialogView.findViewById<EditText>(R.id.etBudget)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        if (category != null) {
            etName.setText(category.name)
            etBudget.setText(category.budgetLimit.toString())
            btnSave.text = "Update category"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val budgetText = etBudget.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetText.toDoubleOrNull()
            if (budget == null || budget <= 0) {
                Toast.makeText(this, "Budget must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val categoryDao = AppDatabase.getDatabase(this@BudgetCategoriesActivity).categoryDao()

                val isRenaming = category == null || !category.name.equals(name, ignoreCase = true)
                if (isRenaming && categoryDao.categoryExists(userId, name) > 0) {
                    Toast.makeText(
                        this@BudgetCategoriesActivity,
                        "Category already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                if (category == null) {
                    categoryDao.insert(
                        Category(
                            userId = userId,
                            name = name,
                            color = "#FF7A1A",
                            budgetLimit = budget,
                            spentAmount = 0.0
                        )
                    )
                } else {
                    categoryDao.update(
                        category.copy(
                            name = name,
                            budgetLimit = budget
                        )
                    )

                    if (category.name != name) {
                        AppDatabase.getDatabase(this@BudgetCategoriesActivity)
                            .expenseDao()
                            .updateCategoryName(category.name, name)
                    }
                }

                Toast.makeText(
                    this@BudgetCategoriesActivity,
                    if (category == null) "Category added successfully" else "Category updated",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showManageCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryNames = categories.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Choose category")
            .setItems(categoryNames) { _, which ->
                val category = categories[which]
                showCategoryActionsDialog(category)
            }
            .show()
    }

    private fun showCategoryActionsDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle(category.name)
            .setItems(arrayOf("Edit category", "Delete category")) { _, which ->
                when (which) {
                    0 -> showCategoryFormDialog(category)
                    1 -> confirmDeleteCategory(category)
                }
            }
            .show()
    }

    private fun confirmDeleteCategory(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete ${category.name}?")
            .setMessage("This will remove the category from your budget list.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.getDatabase(this@BudgetCategoriesActivity)
                        .categoryDao()
                        .delete(category)

                    Toast.makeText(
                        this@BudgetCategoriesActivity,
                        "Category deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation(R.id.nav_budget)
    }
    private fun applyGradient(textView: TextView) {
        textView.viewTreeObserver.addOnGlobalLayoutListener {
            val width = textView.width.toFloat()

            val shader = LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf(
                    "#FFD700".toColorInt(), // gold
                    "#FF69B4".toColorInt()  // pink
                ),
                null,
                Shader.TileMode.CLAMP
            )

            textView.paint.shader = shader
        }
    }
}
