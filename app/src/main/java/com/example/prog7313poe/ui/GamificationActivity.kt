package com.example.prog7313poe

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GamificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamification)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val tvNextBadgeProgress = findViewById<TextView>(R.id.tvNextBadgeProgress)
        val progressNextBadge = findViewById<ProgressBar>(R.id.progressNextBadge)

        // Demo data - replace with actual user data
        val currentSaved = 350
        val targetSaved = 1000
        val percentage = (currentSaved * 100 / targetSaved)

        val progressText = "R$currentSaved/R$targetSaved"
        tvNextBadgeProgress.text = progressText
        progressNextBadge.progress = percentage
    }
}