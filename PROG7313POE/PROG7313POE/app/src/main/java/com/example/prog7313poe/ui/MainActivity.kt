package com.example.prog7313poe.ui

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TEMPORARY: Opens your Budget Categories screen first for testing
        startActivity(Intent(this, BudgetCategoriesActivity::class.java))
        finish()
        return

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textView = findViewById<TextView>(R.id.headingTextView)
        val textView2 = findViewById<TextView>(R.id.motoTextView)

        textView.text = HtmlCompat.fromHtml(
            getString(R.string.welcome_heading),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        textView2.text = HtmlCompat.fromHtml(
            getString(R.string.welcome_moto),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        textView.viewTreeObserver.addOnGlobalLayoutListener {
            applyGradient(textView)
        }

        textView2.viewTreeObserver.addOnGlobalLayoutListener {
            applyGradient(textView2)
        }

        val button2 = findViewById<Button>(R.id.btnStart)
        button2.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        val button3 = findViewById<Button>(R.id.btnLogin)
        button3.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
        }

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            try {
                db.userDao().getAllUsers().collect { users ->
                    Log.d("DEBUG", "Users size: ${users.size}")
                }
            } catch (e: Exception) {
                Log.e("FLOW_ERROR", "Error collecting flow", e)
            }
        }
    }

    private fun applyGradient(textView: TextView) {
        val width = textView.width.toFloat()
        val shader = LinearGradient(
            0f, 0f, width, textView.textSize,
            intArrayOf(
                "#FFD700".toColorInt(),
                "#FF69B4".toColorInt()
            ),
            null,
            Shader.TileMode.CLAMP
        )
        textView.paint.shader = shader
    }
}