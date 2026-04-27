package com.example.prog7313poe.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Category
import com.example.prog7313poe.model.Expense
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class AddExpense : AppCompatActivity() {

    private var imageUri: Uri? = null
    private var editingExpense: Expense? = null
    private var categories: List<Category> = emptyList()
    private var isUpdatingCategories = false
    private var categoryDropdownTouched = false
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)
        setupBottomNavigation(R.id.nav_add_expense)

        val db = AppDatabase.getDatabase(this)
        val expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, -1)

        val title = findViewById<EditText>(R.id.etTitle)
        val amount = findViewById<EditText>(R.id.etAmount)
        val description = findViewById<EditText>(R.id.etDescription)
        val date = findViewById<EditText>(R.id.etDate)
        val spinner = findViewById<Spinner>(R.id.spCategory)
        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val uploadBtn = findViewById<Button>(R.id.btnUpload)
        val saveBtn = findViewById<Button>(R.id.btnSave)
        val heading = findViewById<TextView>(R.id.txtHeading)

        if (expenseId != -1) {
            heading.text = "Edit Expense"
            saveBtn.text = "Update Expense"
        }
        applyGradient(heading)

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf("Manage categories")
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = categoryAdapter
        saveBtn.isEnabled = false

        spinner.setOnTouchListener { _, _ ->
            categoryDropdownTouched = true
            false
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isUpdatingCategories || !categoryDropdownTouched) return

                if (position >= categories.size) {
                    categoryDropdownTouched = false
                    startActivity(Intent(this@AddExpense, BudgetCategoriesActivity::class.java))
                    if (categories.isNotEmpty()) {
                        spinner.setSelection(0)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        lifecycleScope.launch {
            seedDefaultCategoriesIfNeeded(db, userId)

            if (expenseId != -1) {
                editingExpense = db.expenseDao().getExpenseById(expenseId)
                editingExpense?.let { expense ->
                    title.setText(expense.title)
                    description.setText(expense.description)
                    amount.setText(expense.amount.toString())
                    date.setText(expense.date)
                    selectEditingCategory(spinner)

                    if (!expense.imageUri.isNullOrEmpty()) {
                        imageUri = Uri.parse(expense.imageUri)
                        imgPreview.visibility = View.VISIBLE
                        imgPreview.setImageURI(imageUri)
                    }
                }
            }
        }

        lifecycleScope.launch {
            db.categoryDao().getCategories(userId).collect { savedCategories ->
                categories = savedCategories
                isUpdatingCategories = true
                categoryAdapter.clear()

                if (savedCategories.isEmpty()) {
                    categoryAdapter.add("Manage categories")
                    saveBtn.isEnabled = false
                } else {
                    categoryAdapter.addAll(savedCategories.map { it.name })
                    categoryAdapter.add("Manage categories")
                    saveBtn.isEnabled = true
                }

                categoryAdapter.notifyDataSetChanged()
                selectEditingCategory(spinner)
                isUpdatingCategories = false
            }
        }

        date.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d -> date.setText("$d/${m + 1}/$y") },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        saveBtn.setOnClickListener {
            val selectedCategory = categories.getOrNull(spinner.selectedItemPosition)
            val expenseAmount = amount.text.toString().toDoubleOrNull()

            if (selectedCategory == null) {
                Toast.makeText(this, "Please add a category first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (title.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expenseAmount == null || expenseAmount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newExpense = Expense(
                id = editingExpense?.id ?: 0,
                title = title.text.toString().trim(),
                description = description.text.toString().trim(),
                amount = expenseAmount,
                category = selectedCategory.name,
                date = date.text.toString(),
                imageUri = imageUri?.toString()
            )

            lifecycleScope.launch {
                if (editingExpense == null) {
                    db.expenseDao().insert(newExpense)
                    db.categoryDao().update(
                        selectedCategory.copy(
                            spentAmount = selectedCategory.spentAmount + expenseAmount
                        )
                    )
                    Toast.makeText(this@AddExpense, "Saved!", Toast.LENGTH_SHORT).show()
                } else {
                    updateExpenseAndCategoryTotals(db, editingExpense!!, newExpense)
                    Toast.makeText(this@AddExpense, "Updated!", Toast.LENGTH_SHORT).show()
                }

                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data
            if (imageUri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        imageUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Some pickers provide read access without persistable permissions.
                }

                val imgPreview = findViewById<ImageView>(R.id.imgPreview)
                imgPreview.visibility = View.VISIBLE
                imgPreview.setImageURI(imageUri)
            }
        }
    }

    private suspend fun updateExpenseAndCategoryTotals(
        db: com.example.prog7313poe.data.AppDatabase,
        oldExpense: Expense,
        newExpense: Expense
    ) {
        db.expenseDao().update(newExpense)

        val oldCategory = db.categoryDao().getCategoryByName(userId, oldExpense.category)
        if (oldCategory != null) {
            db.categoryDao().update(
                oldCategory.copy(
                    spentAmount = (oldCategory.spentAmount - oldExpense.amount).coerceAtLeast(0.0)
                )
            )
        }

        val newCategory = db.categoryDao().getCategoryByName(userId, newExpense.category)
        if (newCategory != null) {
            db.categoryDao().update(
                newCategory.copy(
                    spentAmount = newCategory.spentAmount + newExpense.amount
                )
            )
        }
    }

    private fun selectEditingCategory(spinner: Spinner) {
        val category = editingExpense?.category ?: return
        val index = categories.indexOfFirst { it.name == category }

        if (index >= 0 && spinner.selectedItemPosition != index) {
            spinner.setSelection(index)
        }
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation(R.id.nav_add_expense)
    }
    private fun applyGradient(textView: TextView) {
        textView.viewTreeObserver.addOnGlobalLayoutListener {
            val width = textView.width.toFloat()

            val shader = android.graphics.LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf(
                    "#FFD700".toColorInt(),
                    "#FF69B4".toColorInt()
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )

            textView.paint.shader = shader
        }
    }

    companion object {
        const val EXTRA_EXPENSE_ID = "expenseId"
    }
}
