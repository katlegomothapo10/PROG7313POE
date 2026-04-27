package com.example.prog7313poe.ui

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.prog7313poe.R

class ViewImage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        setupBottomNavigation()

        val imageView = findViewById<ImageView>(R.id.fullImage)

        val uriString = intent.getStringExtra("imageUri")

        if (!uriString.isNullOrEmpty()) {
            val uri = Uri.parse(uriString)

            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            imageView.setImageBitmap(bitmap)
        }
    }
}
