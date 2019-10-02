package com.practice.todo.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_item")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var isDone: Boolean = false
)