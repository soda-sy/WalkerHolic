package com.example.walkerholic.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walkerholic.data.FoodItem
import com.example.walkerholic.databinding.ListFoodBinding

class FoodAdapter : RecyclerView.Adapter<FoodAdapter.FoodHolder>() {
    var foods: List<FoodItem>? = null

    override fun getItemCount(): Int {
        return foods?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        val itemBinding = ListFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.itemBinding.tvFood.text = foods?.get(position)?.foodName.toString()
        holder.itemBinding.tvKcal.text = foods?.get(position)?.calories.toString() + "kcal"

        holder.itemBinding.clItem.setOnClickListener{
            clickListener?.onItemClick(it, position)
        }
    }

    class FoodHolder(val itemBinding: ListFoodBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnItemClickListner {
        fun onItemClick(view: View, position: Int)
    }

    var clickListener: OnItemClickListner? = null

    fun setOnItemClickListener(listener: OnItemClickListner) {
        this.clickListener = listener
    }

}