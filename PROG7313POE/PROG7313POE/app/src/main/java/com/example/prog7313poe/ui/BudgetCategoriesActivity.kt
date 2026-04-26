package com.example.prog7313poe.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313poe.R
import com.example.prog7313poe.viewModel.CategoryViewModel

class BudgetCategoriesActivity : AppCompatActivity() {

    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_categories)

        val recycler = findViewById<RecyclerView>(R.id.recyclerCategories)
        val btnAdd = findViewById<Button>(R.id.btnAddCategory)

        adapter = CategoryAdapter(emptyList())

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        viewModel.categories.observe(this) { categories ->
            adapter.updateData(categories)
        }

        btnAdd.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etBudget = dialogView.findViewById<EditText>(R.id.etBudget)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

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

            if (budgetText.isEmpty()) {
                Toast.makeText(this, "Please enter budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetText.toDoubleOrNull()

            if (budget == null || budget <= 0) {
                Toast.makeText(this, "Budget must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addCategory(
                name = name,
                color = "#FF7A1A",
                budgetLimit = budget,
                spentAmount = 0.0
            ) { success, message ->

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                if (success) {
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}