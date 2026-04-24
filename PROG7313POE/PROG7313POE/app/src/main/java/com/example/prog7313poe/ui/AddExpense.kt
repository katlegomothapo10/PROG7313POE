package com.example.prog7313poe.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.prog7313poe.R
import com.example.prog7313poe.data.AppDatabase
import com.example.prog7313poe.model.Expense
import kotlinx.coroutines.launch
import java.util.*

class AddExpense : AppCompatActivity() {

    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val db = AppDatabase.getDatabase(this)

        val title = findViewById<EditText>(R.id.etTitle)
        val amount = findViewById<EditText>(R.id.etAmount)
        val date = findViewById<EditText>(R.id.etDate)
        val spinner = findViewById<Spinner>(R.id.spCategory)
        val imgPreview = findViewById<ImageView>(R.id.imgPreview)

        val uploadBtn = findViewById<Button>(R.id.btnUpload)
        val saveBtn = findViewById<Button>(R.id.btnSave)

        val heading = findViewById<TextView>(R.id.txtHeading)
        applyGradient(heading)

        val categories = arrayOf("Groceries", "Transport", "Entertainment", "Bills")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        date.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    date.setText("$d/${m + 1}/$y")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        saveBtn.setOnClickListener {

            val expense = Expense(
                title = title.text.toString(),
                amount = amount.text.toString().toDouble(),
                category = spinner.selectedItem.toString(),
                date = date.text.toString(),
                imageUri = if (::imageUri.isInitialized) imageUri.toString() else null
            )

            lifecycleScope.launch {
                db.expenseDao().insert(expense)
                Toast.makeText(this@AddExpense, "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data!!

            contentResolver.takePersistableUriPermission(
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val imgPreview = findViewById<ImageView>(R.id.imgPreview)
            imgPreview.setImageURI(imageUri)
        }
    }
    private fun applyGradient(textView: TextView) {
        textView.viewTreeObserver.addOnGlobalLayoutListener {
            val width = textView.width.toFloat()

            val shader = android.graphics.LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf(
                    "#FFD700".toColorInt(),
                    "#FF69B4".toColorInt()
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )

            textView.paint.shader = shader
        }
    }
}