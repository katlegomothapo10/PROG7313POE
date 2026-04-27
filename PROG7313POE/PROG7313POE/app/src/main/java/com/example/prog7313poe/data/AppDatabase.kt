package com.example.prog7313poe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313poe.model.User
import com.example.prog7313poe.model.Category
import com.example.prog7313poe.model.Expense
import com.example.prog7313poe.viewModel.UserDao
import com.example.prog7313poe.viewModel.ExpenseDao
import com.example.prog7313poe.viewModel.CategoryDao

@Database(
    entities = [User::class, Category::class, Expense::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
