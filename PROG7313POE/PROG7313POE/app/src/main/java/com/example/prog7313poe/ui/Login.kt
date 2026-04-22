package com.example.prog7313poe.ui

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.prog7313poe.R
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textView = findViewById<TextView>(R.id.headingTextView)

        textView.viewTreeObserver.addOnGlobalLayoutListener {
            val width = textView.width.toFloat()

            val shader = LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf(
                    "#FFD700".toColorInt(), // gold (sun gold)
                    "#FF69B4".toColorInt()  // pink
                ),
                null,
                Shader.TileMode.CLAMP
            )

            textView.paint.shader = shader
        }
        textView.text = Html.fromHtml(getString(R.string.app_name), Html.FROM_HTML_MODE_LEGACY)


        val button2 = findViewById<Button>(R.id.btnSignin)

        button2.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
        }
        val button3 = findViewById<Button>(R.id.btnFacebook)

        button3.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://www.facebook.com")
            startActivity(intent)
        }

        val button4 = findViewById<Button>(R.id.btnGoogle)

        button4.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://www.google.com")
            startActivity(intent)
        }

        // 🔐 Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 🧾 Link UI elements
        val emailEt = findViewById<EditText>(R.id.btnEmail)
        val passwordEt = findViewById<EditText>(R.id.btnPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        // 🔵 LOGIN USER
        loginBtn.setOnClickListener {

            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}