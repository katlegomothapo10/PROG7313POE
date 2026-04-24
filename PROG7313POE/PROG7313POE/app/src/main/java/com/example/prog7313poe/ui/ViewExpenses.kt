package com.example.prog7313poe.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import kotlinx.coroutines.launch

class ViewExpenses : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_expenses)

        val db = AppDatabase.getDatabase(this)

        val container = findViewById<LinearLayout>(R.id.layoutExpenses)
        val heading = findViewById<TextView>(R.id.txtHeading)

        applyGradient(heading)

        lifecycleScope.launch {
            try {
                db.expenseDao().getAllExpenses().collect { expenses ->

                    container.removeAllViews()

                    if (expenses.isEmpty()) {
                        val empty = TextView(this@ViewExpenses)
                        empty.text = "No expenses yet"
                        empty.setTextColor(android.graphics.Color.WHITE)
                        empty.textSize = 16f
                        container.addView(empty)
                        return@collect
                    }

                    val grouped = expenses.groupBy { it.category }

                    for ((category, items) in grouped) {

                        val categoryTitle = TextView(this@ViewExpenses)
                        categoryTitle.text = category
                        categoryTitle.textSize = 20f
                        categoryTitle.setTextColor("#FFD700".toColorInt())
                        categoryTitle.setPadding(0, 20, 0, 8)

                        container.addView(categoryTitle)

                        val card = LinearLayout(this@ViewExpenses)
                        card.orientation = LinearLayout.VERTICAL
                        card.setBackgroundResource(R.drawable.btn_white)
                        card.setPadding(16, 16, 16, 16)

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, 16)
                        card.layoutParams = params

                        for ((index, expense) in items.withIndex()) {

                            val row = LinearLayout(this@ViewExpenses)
                            row.orientation = LinearLayout.HORIZONTAL
                            row.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            row.setPadding(0, 8, 0, 8)

                            val textContainer = LinearLayout(this@ViewExpenses)
                            textContainer.orientation = LinearLayout.VERTICAL
                            textContainer.layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )

                            val txt = TextView(this@ViewExpenses)
                            txt.text = "R${expense.amount} • ${expense.title}\n📅 ${expense.date}"
                            txt.setTextColor(android.graphics.Color.BLACK)
                            txt.textSize = 15f

                            textContainer.addView(txt)
                            row.addView(textContainer)

                            if (!expense.imageUri.isNullOrEmpty()) {

                                val btnView = AppCompatButton(this@ViewExpenses)
                                btnView.text = "View"
                                btnView.setBackgroundResource(R.drawable.btn_gradient)
                                btnView.backgroundTintList = null
                                btnView.setTextColor(resources.getColor(R.color.black))

                                val btnParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                btnParams.setMargins(16, 0, 0, 0)
                                btnView.layoutParams = btnParams

                                btnView.setOnClickListener {
                                    val intent = Intent(this@ViewExpenses, ViewImage::class.java)
                                    intent.putExtra("imageUri", expense.imageUri)
                                    startActivity(intent)
                                }

                                row.addView(btnView)
                            }

                            card.addView(row)

                            if (index != items.size - 1) {
                                val divider = View(this@ViewExpenses)
                                divider.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    2
                                )
                                divider.setBackgroundColor("#DDDDDD".toColorInt())

                                card.addView(divider)
                            }
                        }

                        container.addView(card)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

                val error = TextView(this@ViewExpenses)
                error.text = "Error loading expenses"
                error.setTextColor(android.graphics.Color.RED)

                container.removeAllViews()
                container.addView(error)
            }
        }
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