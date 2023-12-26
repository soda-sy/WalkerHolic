package com.example.walkerholic.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walkerholic.data.DiaryRecord
import com.example.walkerholic.databinding.ListDiaryRecordBinding

class DiaryAdapter : RecyclerView.Adapter<DiaryAdapter.DiaryHolder>() {
    var diaryRecords: List<DiaryRecord>? = null

    override fun getItemCount(): Int {
        return diaryRecords?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryHolder {
        val itemBinding = ListDiaryRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaryHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DiaryHolder, position: Int) {
        holder.itemBinding.tvFood.text = diaryRecords?.get(position)?.foodName.toString()
        holder.itemBinding.tvDate.text = diaryRecords?.get(position)?.date.toString()

        holder.itemBinding.clItem.setOnClickListener{
            clickListener?.onItemClick(it, position)
        }
    }

    class DiaryHolder(val itemBinding: ListDiaryRecordBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface OnItemClickListner {
        fun onItemClick(view: View, position: Int)
    }

    var clickListener: OnItemClickListner? = null

    fun setOnItemClickListener(listener: OnItemClickListner) {
        this.clickListener = listener
    }

    fun updateList(newList: ArrayList<DiaryRecord>) {
        diaryRecords = newList
        notifyDataSetChanged()
    }

}