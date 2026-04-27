package com.example.prog7313poe.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.prog7313poe.R

class NoSpendChallengeActivity : AppCompatActivity() {

    private var currentDays = 7
    private var currentDayProgress = 3
    private var moneySaved = 489

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_spend_challenge)
        setupBottomNavigation(R.id.nav_challenge)

        val heading = findViewById<TextView>(R.id.txtHeading)
        val btn3Days = findViewById<Button>(R.id.btn3Days)
        val btn7Days = findViewById<Button>(R.id.btn7Days)
        val btn30Days = findViewById<Button>(R.id.btn30Days)
        val btnStartNew = findViewById<Button>(R.id.btnStartNew)
        val tvCurrentDay = findViewById<TextView>(R.id.tvCurrentDay)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvMoneySaved = findViewById<TextView>(R.id.tvMoneySaved)

        applyGradient(heading)

        btn3Days.setOnClickListener {
            currentDays = 3
            currentDayProgress = 1
            moneySaved = 150
            updateDisplay(tvCurrentDay, progressBar, tvMoneySaved)
        }

        btn7Days.setOnClickListener {
            currentDays = 7
            currentDayProgress = 3
            moneySaved = 489
            updateDisplay(tvCurrentDay, progressBar, tvMoneySaved)
        }

        btn30Days.setOnClickListener {
            currentDays = 30
            currentDayProgress = 5
            moneySaved = 1200
            updateDisplay(tvCurrentDay, progressBar, tvMoneySaved)
        }

        btnStartNew.setOnClickListener {
            currentDayProgress = 1
            moneySaved = 0
            updateDisplay(tvCurrentDay, progressBar, tvMoneySaved)
        }

        updateDisplay(tvCurrentDay, progressBar, tvMoneySaved)
    }

    private fun updateDisplay(dayText: TextView, progressBar: ProgressBar, savedText: TextView) {
        dayText.text = "Day $currentDayProgress of $currentDays"
        val progress = (currentDayProgress * 100) / currentDays
        progressBar.progress = progress
        savedText.text = "R$moneySaved saved by not spending"
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation(R.id.nav_challenge)
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
