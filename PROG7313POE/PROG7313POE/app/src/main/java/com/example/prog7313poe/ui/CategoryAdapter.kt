package com.example.prog7313poe.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog7313poe.R
import com.example.prog7313poe.model.Category

class CategoryAdapter(
    private var categories: List<Category>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtCategoryName)
        val txtAmount: TextView = itemView.findViewById(R.id.txtCategoryAmount)
        val progress: ProgressBar = itemView.findViewById(R.id.progressCategory)
        val daysLeft: TextView = itemView.findViewById(R.id.txtDaysLeft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.txtName.text = category.name
        holder.txtAmount.text = "R${category.spentAmount.toInt()}/R${category.budgetLimit.toInt()}"
        holder.daysLeft.text = "📅7 days left"

        val percent = if (category.budgetLimit > 0) {
            ((category.spentAmount / category.budgetLimit) * 100).toInt()
        } else {
            0
        }

        holder.progress.progress = percent.coerceIn(0, 100)
        holder.progress.progressTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(category.color))
        holder.progress.progressBackgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.BLACK)
    }

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}