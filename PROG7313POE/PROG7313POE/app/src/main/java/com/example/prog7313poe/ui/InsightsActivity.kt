package com.example.prog7313poe.ui

import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Category
import com.example.prog7313poe.model.Expense
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class InsightsActivity : AppCompatActivity() {

    private val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    private var expenses: List<Expense> = emptyList()
    private var categories: List<Category> = emptyList()

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)
        setupBottomNavigation(R.id.nav_insights)

        val db = AppDatabase.getDatabase(this)
        val heading = findViewById<TextView>(R.id.txtHeading)
        val container = findViewById<LinearLayout>(R.id.layoutInsights)

        applyGradient(heading)

        lifecycleScope.launch {
            seedDefaultCategoriesIfNeeded(db, userId)
        }

        lifecycleScope.launch {
            db.expenseDao().getAllExpenses().collect {
                expenses = it
                renderInsights(container)
            }
        }

        lifecycleScope.launch {
            db.categoryDao().getCategories(userId).collect {
                categories = it
                renderInsights(container)
            }
        }
    }

    private fun renderInsights(container: LinearLayout) {
        container.removeAllViews()

        container.addView(
            createInsightCard(
                weeklyEntertainmentInsight(),
                "#C56A1A",
                "#FF7A45"
            )
        )

        container.addView(
            createInsightCard(
                groceriesInsight(),
                "#006F66",
                "#46E0D2"
            )
        )

        container.addView(
            createInsightCard(
                budgetAlertInsight(),
                "#7C145D",
                "#D9367E"
            )
        )

        val tip = TextView(this)
        tip.text = "Tip: Try the 7 day challenge to save extra!"
        tip.setTextColor(android.graphics.Color.WHITE)
        tip.textSize = 18f
        tip.setTypeface(null, android.graphics.Typeface.BOLD)
        tip.gravity = Gravity.CENTER
        tip.setPadding(0, 16, 0, 0)
        container.addView(tip)
    }

    private fun weeklyEntertainmentInsight(): String {
        val today = LocalDate.now()
        val currentWeekStart = today.minusDays(6)
        val previousWeekStart = today.minusDays(13)
        val previousWeekEnd = today.minusDays(7)

        val current = expenses
            .filter { it.category.equals("Entertainment", ignoreCase = true) }
            .filter { parseExpenseDate(it.date)?.let { date -> !date.isBefore(currentWeekStart) && !date.isAfter(today) } == true }
            .sumOf { it.amount }

        val previous = expenses
            .filter { it.category.equals("Entertainment", ignoreCase = true) }
            .filter { parseExpenseDate(it.date)?.let { date -> !date.isBefore(previousWeekStart) && !date.isAfter(previousWeekEnd) } == true }
            .sumOf { it.amount }

        if (current == 0.0 && previous == 0.0) {
            return "Track entertainment spending here once you add expenses."
        }

        if (previous <= 0.0) {
            return "Entertainment spending is R${"%.0f".format(current)} this week."
        }

        val percent = (((current - previous) / previous) * 100).toInt()
        val direction = if (percent >= 0) "more" else "less"

        return "${kotlin.math.abs(percent)}% $direction on entertainment: R${"%.0f".format(current)} vs R${"%.0f".format(previous)} last week."
    }

    private fun groceriesInsight(): String {
        val groceries = categories.firstOrNull { it.name.equals("Groceries", ignoreCase = true) }
            ?: return "Groceries budget will appear once categories are loaded."

        return if (groceries.spentAmount <= groceries.budgetLimit) {
            "On track with groceries budget so far: R${"%.0f".format(groceries.spentAmount)}/R${"%.0f".format(groceries.budgetLimit)}"
        } else {
            "Groceries budget has been exceeded: R${"%.0f".format(groceries.spentAmount)}/R${"%.0f".format(groceries.budgetLimit)}"
        }
    }

    private fun budgetAlertInsight(): String {
        val overBudget = categories
            .filter { it.budgetLimit > 0 && it.spentAmount > it.budgetLimit }
            .maxByOrNull { it.spentAmount - it.budgetLimit }

        return if (overBudget == null) {
            "Budget goals are currently on track."
        } else {
            "${overBudget.name} budget goals have not been met."
        }
    }

    private fun createInsightCard(text: String, startColor: String, endColor: String): TextView {
        val card = TextView(this)
        card.text = text
        card.setTextColor(android.graphics.Color.WHITE)
        card.textSize = 16f
        card.gravity = Gravity.CENTER
        card.setPadding(18, 18, 18, 18)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        card.layoutParams = params

        card.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor.toColorInt(), endColor.toColorInt())
        )

        return card
    }

    private fun parseExpenseDate(date: String): LocalDate? {
        return try {
            LocalDate.parse(date, dateFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation(R.id.nav_insights)
    }

    private fun applyGradient(textView: TextView) {
        textView.viewTreeObserver.addOnGlobalLayoutListener {
            val width = textView.width.toFloat()

            val shader = LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf("#FFD700".toColorInt(), "#FF69B4".toColorInt()),
                null,
                Shader.TileMode.CLAMP
            )

            textView.paint.shader = shader
        }
    }
}
