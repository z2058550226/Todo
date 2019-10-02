package com.practice.todo.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practice.todo.R
import com.practice.todo.TodoEditActivity
import com.practice.todo.storage.database.entity.TodoItem
import com.practice.todo.util.bindView

class TodoListAdapter : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    private val dataList = mutableListOf<TodoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.mCbIsDone.isChecked = item.isDone
        holder.mTvTitle.text = item.title
        holder.itemView.setOnClickListener {
            TodoEditActivity.start(holder.itemView.context as Activity, item.id)
        }
    }

    fun updateAll(item: List<TodoItem>) {
        dataList.clear()
        dataList.addAll(item)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCbIsDone: CheckBox by bindView(R.id.mCbIsDone)
        val mTvTitle: TextView by bindView(R.id.mTvTitle)
    }
}