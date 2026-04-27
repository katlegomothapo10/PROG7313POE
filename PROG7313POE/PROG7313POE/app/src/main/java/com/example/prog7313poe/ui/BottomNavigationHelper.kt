package com.example.prog7313poe.ui

import android.content.Intent
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.example.prog7313poe.R
import com.google.android.material.bottomnavigation.BottomNavigationView

fun AppCompatActivity.setupBottomNavigation(@IdRes selectedItemId: Int? = null) {
    val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation) ?: return

    bottomNavigation.setOnItemSelectedListener { menuItem ->
        if (menuItem.itemId == selectedItemId) {
            return@setOnItemSelectedListener true
        }

        val destination = when (menuItem.itemId) {
            R.id.nav_dashboard -> Dashboard::class.java
            R.id.nav_add_expense -> AddExpense::class.java
            R.id.nav_view_expenses -> ViewExpenses::class.java
            R.id.nav_budget -> BudgetCategoriesActivity::class.java
            R.id.nav_challenge -> NoSpendChallengeActivity::class.java
            else -> null
        }

        destination?.let {
            startActivity(
                Intent(this, it).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
            true
        } ?: false
    }

    selectedItemId?.let {
        bottomNavigation.menu.findItem(it)?.isChecked = true
    }
}
