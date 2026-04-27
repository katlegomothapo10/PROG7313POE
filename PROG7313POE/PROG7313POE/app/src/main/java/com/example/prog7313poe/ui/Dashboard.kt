package com.example.prog7313poe.ui

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        setupBottomNavigation(R.id.nav_dashboard)

        val db = AppDatabase.getDatabase(this)

        val totalSaved = findViewById<TextView>(R.id.txtTotalSpent)
        val recentActivity = findViewById<TextView>(R.id.txtRecent)
        val btnExpense = findViewById<Button>(R.id.btnExpense)
        val usernameText = findViewById<TextView>(R.id.txtUsername)
        val greetingText = findViewById<TextView>(R.id.txtGreeting)

        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.email?.substringBefore("@") ?: "User"

        usernameText.text = "$username!"
        greetingText.text = "👋 Good day,"

        applyGradient(usernameText)
        applyGradient(greetingText)

        btnExpense.setOnClickListener {

            val options = arrayOf("Add Expense", "View Expenses")

            android.app.AlertDialog.Builder(this)
                .setTitle("Expense Options")
                .setItems(options) { _, which ->

                    when (which) {
                        0 -> {
                            startActivity(Intent(this, AddExpense::class.java))
                        }
                        1 -> {
                            startActivity(Intent(this, ViewExpenses::class.java))
                        }
                    }
                }
                .show()
        }

        val btnChallenge = findViewById<Button>(R.id.btnChallenge)
        btnChallenge.setOnClickListener {
            val intent = Intent(this, NoSpendChallengeActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            try {
                db.expenseDao().getAllExpenses().collect { expenses ->
                    val recent = if (expenses.isNotEmpty()) {
                        expenses.take(5).joinToString("\n\n") {
                            "🕒 ${it.title}\nR${it.amount} • ${it.category}"
                        }
                    } else {
                        "No recent activity"
                    }
                    recentActivity.text = recent
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lifecycleScope.launch {
            try {
                db.expenseDao().getTotalSpent().collect { total ->
                    totalSaved.text = "R${total ?: 0}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation(R.id.nav_dashboard)
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
