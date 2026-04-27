package com.example.prog7313poe.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Expense
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar

class ViewExpenses : AppCompatActivity() {

    private val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    private var allExpenses: List<Expense> = emptyList()
    private var fromDate: LocalDate? = null
    private var toDate: LocalDate? = null

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)
        setupBottomNavigation(R.id.nav_view_expenses)

        val db = AppDatabase.getDatabase(this)
        val container = findViewById<LinearLayout>(R.id.layoutExpenses)
        val heading = findViewById<TextView>(R.id.txtHeading)
        val fromInput = findViewById<EditText>(R.id.etFromDate)
        val toInput = findViewById<EditText>(R.id.etToDate)
        val clearFilter = findViewById<AppCompatButton>(R.id.btnClearFilter)

        applyGradient(heading)

        fromInput.setOnClickListener {
            showDatePicker(fromDate) { selected ->
                fromDate = selected
                fromInput.setText(selected.format(dateFormatter))
                renderExpenses(container)
            }
        }

        toInput.setOnClickListener {
            showDatePicker(toDate) { selected ->
                toDate = selected
                toInput.setText(selected.format(dateFormatter))
                renderExpenses(container)
            }
        }

        clearFilter.setOnClickListener {
            fromDate = null
            toDate = null
            fromInput.text.clear()
            toInput.text.clear()
            renderExpenses(container)
        }

        lifecycleScope.launch {
            try {
                db.expenseDao().getAllExpenses().collect { expenses ->
                    allExpenses = expenses
                    renderExpenses(container)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage(container, "Error loading expenses", android.graphics.Color.RED)
            }
        }
    }

    private fun renderExpenses(container: LinearLayout) {
        container.removeAllViews()

        val filteredExpenses = allExpenses.filter { expense ->
            val expenseDate = parseExpenseDate(expense.date) ?: return@filter false
            val afterStart = fromDate?.let { !expenseDate.isBefore(it) } ?: true
            val beforeEnd = toDate?.let { !expenseDate.isAfter(it) } ?: true

            afterStart && beforeEnd
        }

        if (filteredExpenses.isEmpty()) {
            showMessage(container, "No expenses found", android.graphics.Color.WHITE)
            return
        }

        filteredExpenses.groupBy { it.category }.forEach { (category, items) ->
            container.addView(createCategoryCard(category, items))
        }
    }

    private fun createCategoryCard(category: String, items: List<Expense>): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.btn_white)
        card.setPadding(18, 18, 18, 18)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 18)
        card.layoutParams = params

        val total = items.sumOf { it.amount }

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        val categoryTitle = TextView(this)
        categoryTitle.text = category
        categoryTitle.textSize = 20f
        categoryTitle.setTextColor(android.graphics.Color.BLACK)
        categoryTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        categoryTitle.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        val totalText = TextView(this)
        totalText.text = "Total: R${"%.2f".format(total)}"
        totalText.textSize = 16f
        totalText.setTextColor("#9B3A62".toColorInt())
        totalText.setTypeface(null, android.graphics.Typeface.BOLD)

        header.addView(categoryTitle)
        header.addView(totalText)
        card.addView(header)

        items.forEach { expense ->
            val divider = View(this)
            divider.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            ).apply {
                setMargins(0, 14, 0, 14)
            }
            divider.setBackgroundColor("#DDDDDD".toColorInt())
            card.addView(divider)

            card.addView(createExpenseRow(expense))
        }

        return card
    }

    private fun createExpenseRow(expense: Expense): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.VERTICAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val titleLine = TextView(this)
        titleLine.text = expense.title
        titleLine.setTextColor(android.graphics.Color.BLACK)
        titleLine.textSize = 16f
        titleLine.setTypeface(null, android.graphics.Typeface.BOLD)

        val details = TextView(this)
        details.text = "R${"%.2f".format(expense.amount)}  |  ${expense.date}"
        details.setTextColor("#333333".toColorInt())
        details.textSize = 14f
        details.setPadding(0, 4, 0, 6)

        val description = TextView(this)
        description.text = expense.description.ifBlank { "No description" }
        description.setTextColor("#333333".toColorInt())
        description.textSize = 14f
        description.setPadding(0, 0, 0, 10)

        val buttonContainer = LinearLayout(this)
        buttonContainer.orientation = LinearLayout.HORIZONTAL

        if (!expense.imageUri.isNullOrEmpty()) {
            val btnView = createRowButton("View")
            btnView.setOnClickListener {
                val intent = Intent(this, ViewImage::class.java)
                intent.putExtra("imageUri", expense.imageUri)
                startActivity(intent)
            }
            buttonContainer.addView(btnView)
        }

        val btnEdit = createRowButton("Edit")
        btnEdit.setOnClickListener {
            showExpenseOptions(expense)
        }
        buttonContainer.addView(btnEdit)

        row.addView(titleLine)
        row.addView(details)
        row.addView(description)
        row.addView(buttonContainer)

        return row
    }

    private fun createRowButton(text: String): AppCompatButton {
        val button = AppCompatButton(this)
        button.text = text
        button.setBackgroundResource(R.drawable.btn_gradient)
        button.backgroundTintList = null
        button.setTextColor(resources.getColor(R.color.black))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 12, 0)
        button.layoutParams = params

        return button
    }

    private fun showExpenseOptions(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle(expense.title)
            .setItems(arrayOf("Edit expense", "Delete expense")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, AddExpense::class.java)
                        intent.putExtra(AddExpense.EXTRA_EXPENSE_ID, expense.id)
                        startActivity(intent)
                    }
                    1 -> confirmDeleteExpense(expense)
                }
            }
            .show()
    }

    private fun confirmDeleteExpense(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete expense?")
            .setMessage("This will remove ${expense.title} from your expenses.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(this@ViewExpenses)
                    db.expenseDao().delete(expense)

                    val category = db.categoryDao().getCategoryByName(userId, expense.category)
                    if (category != null) {
                        db.categoryDao().update(
                            category.copy(
                                spentAmount = (category.spentAmount - expense.amount)
                                    .coerceAtLeast(0.0)
                            )
                        )
                    }

                    Toast.makeText(
                        this@ViewExpenses,
                        "Expense deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(initialDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
        val cal = Calendar.getInstance()
        if (initialDate != null) {
            cal.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                onDateSelected(LocalDate.of(year, month + 1, day))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun parseExpenseDate(date: String): LocalDate? {
        return try {
            LocalDate.parse(date, dateFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun showMessage(container: LinearLayout, message: String, color: Int) {
        container.removeAllViews()

        val text = TextView(this)
        text.text = message
        text.setTextColor(color)
        text.textSize = 16f
        text.gravity = Gravity.CENTER
        container.addView(text)
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation(R.id.nav_view_expenses)
    }

    private fun applyGradient(textView: TextView) {
        textView.viewTreeObserver.addOnGlobalLayoutListener {
            val width = textView.width.toFloat()

            val shader = android.graphics.LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf("#FFD700".toColorInt(), "#FF69B4".toColorInt()),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )

            textView.paint.shader = shader
        }
    }
}
