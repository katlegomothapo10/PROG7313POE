package com.example.prog7313poe.ui

import kotlinx.coroutines.flow.collect
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Html
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
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.R
import com.example.prog7313poe.ui.Signin
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textView = findViewById<TextView>(R.id.headingTextView)
        val textView2 = findViewById<TextView>(R.id.motoTextView)

        textView.viewTreeObserver.addOnGlobalLayoutListener {
            applyGradient(textView)

        }

        textView.text = HtmlCompat.fromHtml(
            getString(R.string.welcome_heading),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        textView2.viewTreeObserver.addOnGlobalLayoutListener {
            applyGradient(textView2)

        }

        textView2.text =
            HtmlCompat.fromHtml(getString(R.string.welcome_moto), HtmlCompat.FROM_HTML_MODE_LEGACY)

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

        val db = AppDatabase.Companion.getDatabase(this)

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