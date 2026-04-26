package com.example.prog7313poe.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val name: String,
    val color: String,
    val budgetLimit: Double,
    val spentAmount: Double = 0.0
)